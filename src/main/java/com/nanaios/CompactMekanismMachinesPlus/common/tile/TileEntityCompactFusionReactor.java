package com.nanaios.CompactMekanismMachinesPlus.common.tile;

import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusContainerTypes;
import mekanism.api.*;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.lasers.ILaserReceptor;
import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.registries.GeneratorsChemicals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class TileEntityCompactFusionReactor extends TileEntityConfigurableMachine implements ILaserReceptor {

    public static final String HEAT_TAB = FusionReactorMultiblockData.HEAT_TAB;
    public static final String FUEL_TAB =  FusionReactorMultiblockData.FUEL_TAB;
    public static final String STATS_TAB = FusionReactorMultiblockData.STATS_TAB;

    //public static final int MAX_INJECTION = 98;//this is the effective cap in the GUI, as text field is limited to 2 chars
    //Reaction characteristics
    private static final double burnTemperature = 100_000_000;
    private static final double burnRatio = 1;
    //Thermal characteristics
    private static final long plasmaHeatCapacity = 100;
    private static final double caseHeatCapacity = 1;
    private static final double inverseInsulation = 100_000;
    //Heat transfer metrics
    private static final double plasmaCaseConductivity = 0.2;

    @ContainerSync(tags = HEAT_TAB)
    public IExtendedFluidTank waterTank;

    @ContainerSync(tags = HEAT_TAB)
    public IChemicalTank steamTank;

    @ContainerSync
    private boolean burning = false;

    @ContainerSync
    public IEnergyContainer energyContainer;
    public IHeatCapacitor heatCapacitor;

    @ContainerSync
    private double lastCaseTemperature;

    @ContainerSync(tags = HEAT_TAB)
    private double lastPlasmaTemperature;

    private final double biomeAmbientTemp;

    @ContainerSync
    public double lastTransferLoss;

    @ContainerSync
    public double lastEnvironmentLoss;

    @ContainerSync(tags = FUEL_TAB)
    public IChemicalTank deuteriumTank;

    @ContainerSync(tags = FUEL_TAB)
    public IChemicalTank tritiumTank;

    @ContainerSync(tags = FUEL_TAB)
    public IChemicalTank fuelTank;

    @ContainerSync(tags = {FUEL_TAB, HEAT_TAB, STATS_TAB}, getter = "getInjectionRate", setter = "setInjectionRate")
    private int injectionRate = 2;
    @ContainerSync(tags = {FUEL_TAB, HEAT_TAB, STATS_TAB})
    private long lastBurned;

    private int maxWater = injectionRate * MekanismGeneratorsConfig.generators.fusionWaterPerInjection.get();
    private long maxSteam = injectionRate * MekanismGeneratorsConfig.generators.fusionSteamPerInjection.get();

    private InputInventorySlot reactorSlot;

    public double plasmaTemperature;

    private boolean clientBurning;
    private double clientTemp;

    public TileEntityCompactFusionReactor(BlockPos pos, BlockState state) {
        super(CompactPlusBlocks.COMPACT_FUSION_REACTOR, pos, state);

        addCapabilityResolver(BasicCapabilityResolver.create(Capabilities.LASER_RECEPTOR,() -> this));

        biomeAmbientTemp = HeatAPI.getAmbientTemp(this.getLevel(),pos);
        lastPlasmaTemperature = biomeAmbientTemp;
        lastCaseTemperature = biomeAmbientTemp;
        plasmaTemperature = biomeAmbientTemp;

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.CHEMICAL);
        if (gasConfig !=null){
            gasConfig.addSlotInfo( DataType.INPUT, new ChemicalSlotInfo(true,false, fuelTank));
            gasConfig.addSlotInfo( DataType.INPUT_1, new ChemicalSlotInfo(true,false, deuteriumTank));
            gasConfig.addSlotInfo( DataType.INPUT_2, new ChemicalSlotInfo(true,false, tritiumTank));
            gasConfig.addSlotInfo( DataType.OUTPUT, new ChemicalSlotInfo(false,true, steamTank));
            gasConfig.setDataType(DataType.INPUT, RelativeSide.TOP);
            gasConfig.setDataType(DataType.INPUT_1, RelativeSide.LEFT);
            gasConfig.setDataType(DataType.INPUT_2, RelativeSide.RIGHT);
            gasConfig.setDataType(DataType.OUTPUT, RelativeSide.BOTTOM);
            gasConfig.setEjecting(true);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if(energyConfig != null) {
            energyConfig.addSlotInfo(DataType.OUTPUT,new EnergySlotInfo(false,true,energyContainer));
            for (RelativeSide side : RelativeSide.values()) {
                energyConfig.setDataType(DataType.OUTPUT,side);
            }

            energyConfig.setEjecting(true);
        }

        ConfigInfo fluidConfig = configComponent.getConfig(TransmissionType.FLUID);
        if(fluidConfig != null) {
            fluidConfig.addSlotInfo(DataType.INPUT,new FluidSlotInfo(true,false,waterTank));
            for (RelativeSide side : RelativeSide.values()) {
                fluidConfig.setDataType(DataType.INPUT,side);
            }

            fluidConfig.setCanEject(false);
        }

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if(itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT,new InventorySlotInfo(true,false,reactorSlot));
            for (RelativeSide side : RelativeSide.values()) {
                itemConfig.setDataType(DataType.INPUT,side);
            }


            itemConfig.setCanEject(false);
        }

        ejectorComponent = new TileComponentEjector(this, ()->Long.MAX_VALUE,()->Integer.MAX_VALUE,()-> Long.MAX_VALUE);
        ejectorComponent.setOutputData(configComponent,TransmissionType.CHEMICAL,TransmissionType.ENERGY);
        ejectorComponent.setCanTankEject(tank -> tank == steamTank);
        ejectorComponent.setCanEject(type -> canFunction());
    }

    //update系統.

    @Override
    public boolean onUpdateServer() {
        boolean needsPacket = super.onUpdateServer();
        long fuelBurned = 0;
        //Only thermal transfer happens unless we're hot enough to burn.
        if (getPlasmaTemp() >= burnTemperature) {
            //If we're not burning, yet we need a hohlraum to ignite
            if (!burning && hasHohlraum()) {
                vaporiseHohlraum();
            }

            //Only inject fuel if we're burning
            if (isBurning()) {
                injectFuel();
                fuelBurned = burnFuel();
                if (fuelBurned == 0) {
                    setBurning(false);
                    setActive(false);
                } else {
                    setActive(true);
                }
            } else {
                setActive(false);
            }
        } else {
            setActive(false);
            setBurning(false);
        }

        if (lastBurned != fuelBurned) {
            lastBurned = fuelBurned;
        }

        //Perform the heat transfer calculations
        transferHeat();
        updateHeatCapacitors(null);
        updateTemperatures();

        if (isBurning() != clientBurning || Math.abs(getLastPlasmaTemp() - clientTemp) > 1_000_000) {
            clientBurning = isBurning();
            clientTemp = getLastPlasmaTemp();
            needsPacket = true;
        }
        return needsPacket;
    }

    @Override
    public void receiveLaserEnergy(long energy) {
        this.addTemperatureFromEnergyInput(energy);
    }

    //以下レーザメソッド系統.
    @Override
    public boolean canLasersDig() {
        return false;
    }
    //以下融合炉メソッド.

    public void addTemperatureFromEnergyInput(long energyAdded) {
        if (isBurning()) {
            setPlasmaTemp(getPlasmaTemp() + (double) energyAdded / plasmaHeatCapacity);
        } else {
            setPlasmaTemp(getPlasmaTemp() + (double) energyAdded / plasmaHeatCapacity *10);
        }
    }

    public void updateTemperatures() {
        lastPlasmaTemperature = getPlasmaTemp();
        lastCaseTemperature = heatCapacitor.getTemperature();
    }

    private boolean hasHohlraum() {
        if (!reactorSlot.isEmpty()) {
            ItemStack hohlraum = reactorSlot.getStack();
            if (hohlraum.getItem() instanceof ItemHohlraum) {
                IChemicalHandler gasHandlerItem = Capabilities.CHEMICAL.getCapability(hohlraum);
                if (gasHandlerItem != null && gasHandlerItem.getChemicalTanks() > 0) {
                    //Validate something didn't go terribly wrong, and we actually do have the tank we expect to have
                    return gasHandlerItem.getChemicalInTank(0).getAmount() == gasHandlerItem.getChemicalTankCapacity(0);
                }
            }
        }
        return false;
    }

    private void injectFuel() {
        long amountNeeded = fuelTank.getNeeded();
        long amountAvailable = 2 * Math.min(deuteriumTank.getStored(), tritiumTank.getStored());
        long amountToInject = Math.min(amountNeeded, Math.min(amountAvailable, injectionRate));
        amountToInject -= amountToInject % 2;
        long injectingAmount = amountToInject / 2;
        MekanismUtils.logMismatchedStackSize(deuteriumTank.shrinkStack(injectingAmount, Action.EXECUTE), injectingAmount);
        MekanismUtils.logMismatchedStackSize(tritiumTank.shrinkStack(injectingAmount, Action.EXECUTE), injectingAmount);
        fuelTank.insert(GeneratorsChemicals.FUSION_FUEL.asStack(amountToInject), Action.EXECUTE, AutomationType.INTERNAL);
    }

    private void vaporiseHohlraum() {
        ItemStack hohlraum = reactorSlot.getStack();
        IChemicalHandler gasHandlerItem = Capabilities.CHEMICAL.getCapability(hohlraum);
        if (gasHandlerItem != null && gasHandlerItem.getChemicalTanks() > 0) {
            fuelTank.insert(gasHandlerItem.getChemicalInTank(0), Action.EXECUTE, AutomationType.INTERNAL);
            lastPlasmaTemperature = getPlasmaTemp();
            reactorSlot.setEmpty();
            setBurning(true);
        }
    }

    public void setBurning(boolean burn) {
        if (burning != burn) {
            burning = burn;
        }
    }

    private long burnFuel() {
        long fuelBurned = MathUtils.clampToLong(Mth.clamp((lastPlasmaTemperature - burnTemperature) * burnRatio, 0, fuelTank.getStored()));
        MekanismUtils.logMismatchedStackSize(fuelTank.shrinkStack(fuelBurned, Action.EXECUTE), fuelBurned);
        setPlasmaTemp(getPlasmaTemp() + (MathUtils.multiplyClamped(MekanismGeneratorsConfig.generators.energyPerFusionFuel.get(), fuelBurned) / (double)plasmaHeatCapacity));
        return fuelBurned;
    }


    public long getPassiveGeneration(boolean active, boolean current) {
        double temperature = current ? getLastCaseTemp() : getMaxCasingTemperature(active);
        return MathUtils.clampToLong(MekanismGeneratorsConfig.generators.fusionThermocoupleEfficiency.get() *
                MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get() * temperature);
    }

    public double getLastCaseTemp() {
        return lastCaseTemperature;
    }

    public double getMaxCasingTemperature(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0;
        long injectionRate = Math.max(this.injectionRate, lastBurned);
        return MathUtils.multiplyClamped(MekanismGeneratorsConfig.generators.energyPerFusionFuel.get(), injectionRate)
                / (k + MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get());
    }

    public int getMinInjectionRate(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0;
        double caseAirConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
        double aMin = burnTemperature * burnRatio * plasmaCaseConductivity * (k + caseAirConductivity) /
                (MekanismGeneratorsConfig.generators.energyPerFusionFuel.get() * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) -
                        plasmaCaseConductivity * (k + caseAirConductivity));
        return 2 * Mth.ceil(aMin / 2D);
    }

    public double getMaxPlasmaTemperature(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0;
        double caseAirConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
        long injectionRate = Math.max(this.injectionRate, lastBurned);
        return injectionRate * MekanismGeneratorsConfig.generators.energyPerFusionFuel.get() / plasmaCaseConductivity *
                (plasmaCaseConductivity + k + caseAirConductivity) / (k + caseAirConductivity);
    }

    public double getIgnitionTemperature(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0;
        double caseAirConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
        double energyPerFusionFuel = MekanismGeneratorsConfig.generators.energyPerFusionFuel.get();
        return burnTemperature * energyPerFusionFuel * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) /
                (energyPerFusionFuel * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) - plasmaCaseConductivity * (k + caseAirConductivity));
    }

    private void transferHeat() {
        //Transfer from plasma to casing
        double plasmaCaseHeat = plasmaCaseConductivity * (lastPlasmaTemperature - lastCaseTemperature);
        if (Math.abs(plasmaCaseHeat) > HeatAPI.EPSILON) {
            setPlasmaTemp(getPlasmaTemp() - plasmaCaseHeat / plasmaHeatCapacity);
            heatCapacitor.handleHeat(plasmaCaseHeat);
        }

        //Transfer from casing to water if necessary
        double caseWaterHeat = MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() * (lastCaseTemperature - biomeAmbientTemp);
        if (Math.abs(caseWaterHeat) > HeatAPI.EPSILON) {
            int waterToVaporize = (int) (HeatUtils.getSteamEnergyEfficiency() * caseWaterHeat / HeatUtils.getWaterThermalEnthalpy());
            waterToVaporize = Math.min(waterToVaporize, Math.min(waterTank.getFluidAmount(), MathUtils.clampToInt(steamTank.getNeeded())));
            if (waterToVaporize > 0) {
                MekanismUtils.logMismatchedStackSize(waterTank.shrinkStack(waterToVaporize, Action.EXECUTE), waterToVaporize);
                steamTank.insert(MekanismChemicals.STEAM.asStack(waterToVaporize), Action.EXECUTE, AutomationType.INTERNAL);
                caseWaterHeat = waterToVaporize * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
                heatCapacitor.handleHeat(-caseWaterHeat);
            }
        }

        HeatAPI.HeatTransfer heatTransfer = simulate();
        lastEnvironmentLoss = heatTransfer.environmentTransfer();
        lastTransferLoss = heatTransfer.adjacentTransfer();

        //Passive energy generation
        double caseAirHeat = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get() * (lastCaseTemperature - biomeAmbientTemp);
        if (Math.abs(caseAirHeat) > HeatAPI.EPSILON) {
            heatCapacitor.handleHeat(-caseAirHeat);
            energyContainer.insert(MathUtils.clampToLong(caseAirHeat * MekanismGeneratorsConfig.generators.fusionThermocoupleEfficiency.get()), Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    public long getSteamPerTick(boolean current) {
        double temperature = current ? this.getLastCaseTemp() : this.getMaxCasingTemperature(true);
        return MathUtils.clampToLong(HeatUtils.getSteamEnergyEfficiency() * MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() * temperature / HeatUtils.getWaterThermalEnthalpy());
    }

    public void setInjectionRate(int rate) {
        if (injectionRate != rate) {
            injectionRate = rate;
            maxWater = injectionRate * MekanismGeneratorsConfig.generators.fusionWaterPerInjection.get();
            maxSteam = injectionRate * MekanismGeneratorsConfig.generators.fusionSteamPerInjection.get();
            if (getLevel() != null && !isRemote()) {
                if (!waterTank.isEmpty()) {
                    waterTank.setStackSize(Math.min(waterTank.getFluidAmount(), waterTank.getCapacity()), Action.EXECUTE);
                }
                if (!steamTank.isEmpty()) {
                    steamTank.setStackSize(Math.min(steamTank.getStored(), steamTank.getCapacity()), Action.EXECUTE);
                }
            }
            //markDirty();
        }
    }

    public void setPlasmaTemp(double temp) {
        if (plasmaTemperature != temp) {
            //CompactMekanismMachinesPlus.LOGGER.info(String.format("set plasma temp to %f.",temp));
            plasmaTemperature = temp;

            //markDirty();
        }
    }

    public int getInjectionRate() {
        return injectionRate;
    }

    public double getLastPlasmaTemp() {
        return lastPlasmaTemperature;
    }

    public double getCaseTemp() {
        return heatCapacitor.getTemperature();
    }

    public int getMaxWater() {
        return maxWater;
    }

    public long getMaxSteam() {
        return maxSteam;
    }

    public boolean isBurning() {return burning;}

    public double getPlasmaTemp() {
        return plasmaTemperature;
    }

    private static double getInverseConductionCoefficient() {
        return 1 / MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
    }

    public void setInjectionRateFromPacket(int rate) {
        this.setInjectionRate(Mth.clamp(rate - (rate % 2), 0, FusionReactorMultiblockData.MAX_INJECTION));
        markForSave();
    }

    //以下初期化系メソッド.


    @Override
    public @Nullable IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);

        builder.addTank(deuteriumTank = VariableCapacityChemicalTank.input(
                MekanismGeneratorsConfig.generators.fusionFuelCapacity.get(),
                chemical -> chemical.is(GeneratorTags.Chemicals.DEUTERIUM),
                this
        ));
        builder.addTank(tritiumTank = VariableCapacityChemicalTank.input(
                MekanismGeneratorsConfig.generators.fusionFuelCapacity.get(),
                chemical -> chemical.is(GeneratorTags.Chemicals.TRITIUM),
                this
        ));
        builder.addTank(fuelTank = VariableCapacityChemicalTank.input(
                MekanismGeneratorsConfig.generators.fusionFuelCapacity.get(),
                chemical -> chemical.is(GeneratorTags.Chemicals.FUSION_FUEL),
                this
        ));
        builder.addTank(steamTank = VariableCapacityChemicalTank.output(
                this::getMaxSteam,
                chemical -> chemical.is(MekanismChemicals.STEAM),
                this
        ));

        return builder.build();
    }

    @NotNull
    @Override
    public IFluidTankHolder getInitialFluidTanks(IContentsListener listener){
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this);

        builder.addTank(waterTank = VariableCapacityFluidTank.input(
                this::getMaxWater,
                fluid -> fluid.is(FluidTags.WATER),
                this
        ));

        return builder.build();
    }

    public IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature){
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(this::getDirection);
        builder.addCapacitor(heatCapacitor = VariableHeatCapacitor.create(caseHeatCapacity, TileEntityCompactFusionReactor::getInverseConductionCoefficient, () -> inverseInsulation, () -> biomeAmbientTemp, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = MachineEnergyContainer.output(MekanismGeneratorsConfig.generators.fusionEnergyCapacity.get(),listener));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        builder.addSlot(reactorSlot = InputInventorySlot.at(stack -> stack.getItem() instanceof ItemHohlraum, listener, 80, 39));
        return builder.build();
    }

    //セーブ系統


    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);

        //温度の更新
        setPlasmaTemp(nbt.getDouble(SerializationConstants.PLASMA_TEMP));
        updateTemperatures();

        //点火状態更新
        setBurning(nbt.getBoolean(SerializationConstants.BURNING));

        //注入レート更新
        setInjectionRate(nbt.getInt(SerializationConstants.INJECTION_RATE));
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        nbtTags.putDouble(SerializationConstants.PLASMA_TEMP, plasmaTemperature);
        nbtTags.putInt(SerializationConstants.INJECTION_RATE, getInjectionRate());
        nbtTags.putBoolean(SerializationConstants.BURNING, burning);
    }


    //その他.

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        if (container.getType() == CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_FUEL.get()) {
            addTabContainerTracker(container,TileEntityCompactFusionReactor.FUEL_TAB);
        } else if (container.getType() == CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_HEAT.get()) {
            addTabContainerTracker(container,TileEntityCompactFusionReactor.HEAT_TAB);
        } else if (container.getType() == CompactPlusContainerTypes.COMPACT_FUSION_REACTOR_STATS.get()) {
            addTabContainerTracker(container,TileEntityCompactFusionReactor.STATS_TAB);
        }
    }

    private void addTabContainerTracker(MekanismContainer container, String tab) {
        SyncMapper.INSTANCE.setup(container, TileEntityCompactFusionReactor.class, ()-> this, tab);
    }

    protected IContentsListener createSaveAndComparator() {
        return this.createSaveAndComparator(this);
    }

    protected IContentsListener createSaveAndComparator(IContentsListener contentsListener) {
        return contentsListener;
    }

    public  IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

}
