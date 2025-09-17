package com.nanaios.CompactMekanismMachinesPlus.common.tile;

import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import mekanism.api.*;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismChemicals;
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
import org.jetbrains.annotations.Nullable;

public class TileEntityCompactSPS extends TileEntityConfigurableMachine {

    @ContainerSync
    public IChemicalTank inputTank;

    @ContainerSync
    public IChemicalTank outputTank;

    private IEnergyContainer energyContainer;

    public double progress;

    public int inputProcessed = 0;

    public long receivedEnergy = 0L;
    public long lastReceivedEnergy = 0L;

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

            for (RelativeSide side: RelativeSide.values()) {
                energyConfig.setDataType(DataType.INPUT,side);
            }
        }

        configComponent.setupInputConfig(TransmissionType.CHEMICAL,inputTank);

        ejectorComponent = new TileComponentEjector(this, ()->Long.MAX_VALUE,()->Integer.MAX_VALUE,() -> Long.MAX_VALUE);
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
            ChemicalStack toAdd = MekanismChemicals.ANTIMATTER.asStack(inputProcessed / inputPerAntimatter);
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

    private long getMaxOutputGas() {
        return MekanismConfig.general.spsOutputTankCapacity.get();
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
        NBTUtils.setDoubleIfPresent(nbt, SerializationConstants.PROGRESS, val -> progress = val);
        NBTUtils.setIntIfPresent(nbt, SerializationConstants.PROCESSED, val -> inputProcessed = val);
        NBTUtils.setBooleanIfPresent(nbt, SerializationConstants.COULD_OPERATE, val -> couldOperate = val);
        NBTUtils.setLegacyEnergyIfPresent(nbt, SerializationConstants.ENERGY_USAGE, val -> receivedEnergy = val);
        NBTUtils.setDoubleIfPresent(nbt, SerializationConstants.LAST_PROCESSED, val -> lastProcessed = val);

    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);

        nbtTags.putDouble(SerializationConstants.PROGRESS, progress);
        nbtTags.putInt(SerializationConstants.PROCESSED, inputProcessed);
        nbtTags.putBoolean(SerializationConstants.COULD_OPERATE, couldOperate);
        nbtTags.putLong(SerializationConstants.ENERGY_USAGE, receivedEnergy);
        nbtTags.putDouble(SerializationConstants.LAST_PROCESSED, lastProcessed);
    }

    //以下初期化.
    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @Override
    public @Nullable IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);

        builder.addTank(inputTank = VariableCapacityChemicalTank.create(
                this::getMaxInputGas,
                ConstantPredicates.notExternal(),
                ConstantPredicates.alwaysTrueBi(),
                gas -> gas.is(MekanismChemicals.POLONIUM),
                ChemicalAttributeValidator.ALWAYS_ALLOW,
                listener
        ));
        builder.addTank(outputTank = VariableCapacityChemicalTank.create(
                this::getMaxOutputGas,
                ConstantPredicates.alwaysTrueBi(),
                ConstantPredicates.notExternal(),
                gas -> gas.is(MekanismChemicals.ANTIMATTER),
                ChemicalAttributeValidator.ALWAYS_ALLOW,
                listener
        ));

        return builder.build();
    }

    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }
}
