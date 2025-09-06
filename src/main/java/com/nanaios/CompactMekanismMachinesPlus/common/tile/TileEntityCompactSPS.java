package com.nanaios.CompactMekanismMachinesPlus.common.tile;

import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import mekanism.api.*;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityCompactSPS extends TileEntityConfigurableMachine {

    @ContainerSync
    public IGasTank inputTank;

    @ContainerSync
    public IGasTank outputTank;

    @ContainerSync
    private IEnergyContainer energyContainer;

    //public final SyncableCoilData coilData = new SyncableCoilData();

    @ContainerSync
    public double progress;

    @ContainerSync
    public int inputProcessed = 0;

    public FloatingLong receivedEnergy = FloatingLong.ZERO;
    @ContainerSync
    public FloatingLong lastReceivedEnergy = FloatingLong.ZERO;

    @ContainerSync
    public double lastProcessed;

    public boolean couldOperate;

    public TileEntityCompactSPS(BlockPos pos, BlockState state) {
        super(CompactPlusBlocks.COMPACT_SPS, pos, state);

        configComponent = new TileComponentConfig(this, TransmissionType.GAS,TransmissionType.ENERGY);

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if(gasConfig != null) {
            gasConfig.addSlotInfo(DataType.INPUT,new ChemicalSlotInfo.GasSlotInfo(true,false,inputTank));
            gasConfig.addSlotInfo(DataType.OUTPUT,new ChemicalSlotInfo.GasSlotInfo(false ,true,outputTank));
            gasConfig.setDataType(DataType.INPUT, RelativeSide.LEFT);
            gasConfig.setDataType(DataType.OUTPUT, RelativeSide.RIGHT);

            gasConfig.setEjecting(true);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if(energyConfig != null) {
            energyConfig.addSlotInfo(DataType.INPUT,new EnergySlotInfo(true,false,energyContainer));
            energyConfig.setDataType(DataType.INPUT,RelativeSide.values());
        }

        ejectorComponent = new TileComponentEjector(this, ()->Long.MAX_VALUE,()->Integer.MAX_VALUE,()-> FloatingLong.create(Long.MAX_VALUE));
        ejectorComponent.setOutputData(configComponent,TransmissionType.GAS);
        ejectorComponent.setCanEject(type -> MekanismUtils.canFunction(this));

    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();

        receivedEnergy = energyContainer.extract(energyContainer.getEnergy(), Action.EXECUTE, AutomationType.INTERNAL);

        double processed = 0;
        couldOperate = canOperate();

        if (couldOperate && !receivedEnergy.isZero()) {
            setActive(true);
            double lastProgress = progress;
            final int inputPerAntimatter = MekanismConfig.general.spsInputPerAntimatter.get();
            long inputNeeded = (inputPerAntimatter - inputProcessed) + inputPerAntimatter * (outputTank.getNeeded() - 1);
            double processable = receivedEnergy.doubleValue() / MekanismConfig.general.spsEnergyPerInput.get().doubleValue();
            if (processable + progress >= inputNeeded) {
                processed = process(inputNeeded);
                progress = 0;
            } else {
                processed = processable;
                progress += processable;
                long toProcess = MathUtils.clampToLong(progress);
                long actualProcessed = process(toProcess);
                if (actualProcessed < toProcess) {
                    //If we processed less than we intended to we need to adjust how much our values actually changed by
                    long processedDif = toProcess - actualProcessed;
                    progress -= processedDif;
                    processed -= processedDif;
                }
                progress %= 1;
            }
            if (lastProgress != progress) {
                //markDirty();
            }
        } else {
            setActive(false);
        }

        lastReceivedEnergy = receivedEnergy;
        receivedEnergy = FloatingLong.ZERO;
        lastProcessed = processed;
    }

    private long process(long operations) {
        if (operations == 0) {
            return 0;
        }
        long processed = inputTank.shrinkStack(operations, Action.EXECUTE);
        int lastInputProcessed = inputProcessed;
        //Limit how much input we actually increase the input processed by to how much we were actually able to remove from the input tank
        inputProcessed += MathUtils.clampToInt(processed);
        final int inputPerAntimatter = MekanismConfig.general.spsInputPerAntimatter.get();
        if (inputProcessed >= inputPerAntimatter) {
            GasStack toAdd = MekanismGases.ANTIMATTER.getStack(inputProcessed / inputPerAntimatter);
            outputTank.insert(toAdd, Action.EXECUTE, AutomationType.INTERNAL);
            inputProcessed %= inputPerAntimatter;
        }
        if (lastInputProcessed != inputProcessed) {
            //markDirty();
        }
        return processed;
    }

    private boolean canOperate() {
        return !inputTank.isEmpty() && outputTank.getNeeded() > 0;
    }

    private long getMaxInputGas() {
        return MekanismConfig.general.spsInputPerAntimatter.get() * 2L;
    }

    public double getProcessRate() {
        return Math.round((lastProcessed / MekanismConfig.general.spsInputPerAntimatter.get()) * 1_000) / 1_000D;
    }

    public double getScaledProgress() {
        return (inputProcessed + progress) / MekanismConfig.general.spsInputPerAntimatter.get();
    }

    //セーブ系統
    @Override
    public void load(@NotNull CompoundTag nbtTags) {
        super.load(nbtTags);
        NBTUtils.setDoubleIfPresent(nbtTags, NBTConstants.PROGRESS, val -> progress = val);
        NBTUtils.setIntIfPresent(nbtTags, NBTConstants.PROCESSED, val -> inputProcessed = val);
        NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.COULD_OPERATE, val -> couldOperate = val);
        NBTUtils.setFloatingLongIfPresent(nbtTags, NBTConstants.ENERGY_USAGE, val -> receivedEnergy = val);
        NBTUtils.setDoubleIfPresent(nbtTags, NBTConstants.LAST_PROCESSED, val -> lastProcessed = val);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        nbtTags.putDouble(NBTConstants.PROGRESS, progress);
        nbtTags.putInt(NBTConstants.PROCESSED, inputProcessed);
        nbtTags.putBoolean(NBTConstants.COULD_OPERATE, couldOperate);
        nbtTags.putString(NBTConstants.ENERGY_USAGE, receivedEnergy.toString());
        nbtTags.putDouble(NBTConstants.LAST_PROCESSED, lastProcessed);
    }

    //以下初期化
    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection,this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @NotNull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection,this::getConfig);
        builder.addTank(inputTank = ChemicalTankBuilder.GAS.create(
                MekanismConfig.general.spsInputPerAntimatter.get() * 2L,
                ChemicalTankBuilder.GAS.notExternal,
                ChemicalTankBuilder.GAS.alwaysTrueBi,
                gas -> gas == MekanismGases.POLONIUM.get(),
                ChemicalAttributeValidator.ALWAYS_ALLOW,
                listener
        ));

        builder.addTank(outputTank = VariableCapacityChemicalTankBuilder.GAS.output(() -> MekanismConfig.general.spsOutputTankCapacity.get(), gas -> gas == MekanismGases.ANTIMATTER.get() ,listener));
        return builder.build();
    }

    public  IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }
}
