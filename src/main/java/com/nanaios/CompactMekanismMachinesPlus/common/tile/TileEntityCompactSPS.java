package com.nanaios.CompactMekanismMachinesPlus.common.tile;

import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import mekanism.api.*;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityCompactSPS extends TileEntityConfigurableMachine {

    public IChemicalTank inputTank;

    public IChemicalTank outputTank;

    private IEnergyContainer energyContainer;

    public double progress;

    public int inputProcessed = 0;

    public long receivedEnergy = 0L;
    public long lastReceivedEnergy = 0L;

    @ContainerSync
    public double lastProcessed;

    public boolean couldOperate;

    public TileEntityCompactSPS(BlockPos pos, BlockState state) {
        super(CompactPlusBlocks.COMPACT_SPS, pos, state);

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.CHEMICAL);
        if(gasConfig != null) {
            gasConfig.addSlotInfo(DataType.INPUT,new ChemicalSlotInfo(true,false,inputTank));
            gasConfig.addSlotInfo(DataType.OUTPUT,new ChemicalSlotInfo(false ,true,outputTank));
            gasConfig.setDataType(DataType.INPUT, RelativeSide.LEFT);
            gasConfig.setDataType(DataType.OUTPUT, RelativeSide.RIGHT);

            gasConfig.setEjecting(true);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if(energyConfig != null) {
            energyConfig.addSlotInfo(DataType.INPUT,new EnergySlotInfo(true,false,energyContainer));

            for (RelativeSide side : RelativeSide.values()) {
                energyConfig.setDataType(DataType.INPUT,side);
            }
        }

        configComponent.setupInputConfig(TransmissionType.CHEMICAL,inputTank);

        ejectorComponent = new TileComponentEjector(this, ()->Long.MAX_VALUE,()->Integer.MAX_VALUE,()-> FloatingLong.create(Long.MAX_VALUE));
        ejectorComponent.setOutputData(configComponent,TransmissionType.CHEMICAL);
        ejectorComponent.setCanTankEject(tank -> tank == outputTank);

    }

    @Override
    protected boolean onUpdateServer() {
        boolean needsPacket = super.onUpdateServer();
        double processed = 0;
        couldOperate = canOperate();
        if (couldOperate && receivedEnergy > 0L) {
            double lastProgress = progress;
            final int inputPerAntimatter = MekanismConfig.general.spsInputPerAntimatter.get();
            long inputNeeded = (inputPerAntimatter - inputProcessed) + inputPerAntimatter * (outputTank.getNeeded() - 1);
            double processable = (double) receivedEnergy / MekanismConfig.general.spsEnergyPerInput.get();
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
        }

        if (receivedEnergy != lastReceivedEnergy || processed != lastProcessed) {
            needsPacket = true;
        }
        if (!chemicalOutputTargets.isEmpty() && !outputTank.isEmpty()) {
            ChemicalUtil.emit(getActiveOutputs(chemicalOutputTargets), outputTank);
        }
        lastReceivedEnergy = receivedEnergy;
        receivedEnergy = 0L;
        lastProcessed = processed;
        return needsPacket;
    }

    private long process(long operations) {
        if (operations == 0) {
            return 0;
        }
        long processed = inputTank.shrinkStack(operations, Action.EXECUTE);
        //Limit how much input we actually increase the input processed by to how much we were actually able to remove from the input tank
        inputProcessed += MathUtils.clampToInt(processed);
        final int inputPerAntimatter = MekanismConfig.general.spsInputPerAntimatter.get();
        if (inputProcessed >= inputPerAntimatter) {
            ChemicalStack toAdd = MekanismGases.ANTIMATTER.getStack(inputProcessed / inputPerAntimatter);
            outputTank.insert(toAdd, Action.EXECUTE, AutomationType.INTERNAL);
            inputProcessed %= inputPerAntimatter;
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

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        NBTUtils.setDoubleIfPresent(nbt, NBTConstants.PROGRESS, val -> progress = val);
        NBTUtils.setIntIfPresent(nbt, NBTConstants.PROCESSED, val -> inputProcessed = val);
        NBTUtils.setBooleanIfPresent(nbt, NBTConstants.COULD_OPERATE, val -> couldOperate = val);
        NBTUtils.setFloatingLongIfPresent(nbt, NBTConstants.ENERGY_USAGE, val -> receivedEnergy = val);
        NBTUtils.setDoubleIfPresent(nbt, NBTConstants.LAST_PROCESSED, val -> lastProcessed = val);

    }

    //セーブ系統
    @Override
    public void loadAdditonal(@NotNull CompoundTag nbtTags) {
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
