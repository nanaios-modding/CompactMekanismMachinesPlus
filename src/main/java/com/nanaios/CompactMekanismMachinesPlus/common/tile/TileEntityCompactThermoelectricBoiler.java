package com.nanaios.CompactMekanismMachinesPlus.common.tile;

import com.nanaios.CompactMekanismMachinesPlus.common.registries.CompactPlusBlocks;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes.CooledCoolant;
import mekanism.api.chemical.gas.attribute.GasAttributes.HeatedCoolant;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class TileEntityCompactThermoelectricBoiler extends TileEntityConfigurableMachine {
    //public static final Object2BooleanMap<UUID> hotMap = new Object2BooleanOpenHashMap<>();

    public static final double CASING_HEAT_CAPACITY = 50;
    private static final double CASING_INVERSE_INSULATION_COEFFICIENT = 100_000;
    private static final double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1;

    private static final double COOLANT_COOLING_EFFICIENCY = 0.4;

    @ContainerSync
    public IGasTank superheatedCoolantTank;

    @ContainerSync
    public IGasTank cooledCoolantTank;

    @ContainerSync
    public VariableCapacityFluidTank waterTank;

    @ContainerSync
    public IGasTank steamTank;

    @ContainerSync
    public VariableHeatCapacitor heatCapacitor;

    private double biomeAmbientTemp;

    @ContainerSync
    public double lastEnvironmentLoss;

    @ContainerSync
    public int lastBoilRate;

    @ContainerSync
    public int lastMaxBoil;

    @ContainerSync
    public int superheatingElements;

    @ContainerSync(setter = "setWaterVolume")
    private int waterVolume;

    @ContainerSync(setter = "setSteamVolume")
    private int steamVolume;

    private int waterTankCapacity;
    private long superheatedCoolantCapacity, steamTankCapacity, cooledCoolantCapacity;

    public BlockPos upperRenderLocation;

    public float prevWaterScale;
    public float prevSteamScale;


    public TileEntityCompactThermoelectricBoiler(BlockPos pos, BlockState state) {
        super(CompactPlusBlocks.COMPACT_THERMOELECTRIC_BOILER, pos, state);

        configComponent = new TileComponentConfig(this,TransmissionType.GAS,TransmissionType.FLUID,TransmissionType.HEAT);

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if(gasConfig != null) {

        }

        ConfigInfo fluidConfig =configComponent.getConfig(TransmissionType.FLUID);
        if(fluidConfig != null) {

        }

        ConfigInfo heatConfig = configComponent.getConfig(TransmissionType.HEAT);
        if(heatConfig != null) {

        }

        ejectorComponent = new TileComponentEjector(this,() -> Long.MAX_VALUE,() -> Integer.MAX_VALUE, () -> FloatingLong.MAX_VALUE);
        ejectorComponent.setOutputData(configComponent,TransmissionType.GAS,TransmissionType.FLUID);
        ejectorComponent.setCanEject(type ->  MekanismUtils.canFunction(this));
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        //hotMap.put(inventoryID, getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP - 0.01);
        // external heat dissipation
        lastEnvironmentLoss = simulateEnvironment();
        // update temperature
        updateHeatCapacitors(null);
        // handle coolant heat transfer
        if (!superheatedCoolantTank.isEmpty()) {
            superheatedCoolantTank.getStack().ifAttributePresent(HeatedCoolant.class, coolantType -> {
                long toCool = Math.round(TileEntityCompactThermoelectricBoiler.COOLANT_COOLING_EFFICIENCY * superheatedCoolantTank.getStored());
                toCool = MathUtils.clampToLong(toCool * (1 - heatCapacitor.getTemperature() / HeatUtils.HEATED_COOLANT_TEMP));
                GasStack cooledCoolant = coolantType.getCooledGas().getStack(toCool);
                toCool = Math.min(toCool, toCool - cooledCoolantTank.insert(cooledCoolant, Action.EXECUTE, AutomationType.INTERNAL).getAmount());
                if (toCool > 0) {
                    double heatEnergy = toCool * coolantType.getThermalEnthalpy();
                    heatCapacitor.handleHeat(heatEnergy);
                    superheatedCoolantTank.shrinkStack(toCool, Action.EXECUTE);
                }
            });
        }
        // handle water heat transfer
        if (getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP && !waterTank.isEmpty()) {
            double heatAvailable = getHeatAvailable();
            lastMaxBoil = (int) Math.floor(HeatUtils.getSteamEnergyEfficiency() * heatAvailable / HeatUtils.getWaterThermalEnthalpy());

            int amountToBoil = Math.min(lastMaxBoil, waterTank.getFluidAmount());
            amountToBoil = Math.min(amountToBoil, MathUtils.clampToInt(steamTank.getNeeded()));
            if (!waterTank.isEmpty()) {
                waterTank.shrinkStack(amountToBoil, Action.EXECUTE);
            }
            if (steamTank.isEmpty()) {
                steamTank.setStack(MekanismGases.STEAM.getStack(amountToBoil));
            } else {
                steamTank.growStack(amountToBoil, Action.EXECUTE);
            }

            heatCapacitor.handleHeat(-amountToBoil * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency());
            lastBoilRate = amountToBoil;
        } else {
            lastBoilRate = 0;
            lastMaxBoil = 0;
        }
        float waterScale = MekanismUtils.getScale(prevWaterScale, waterTank);
        if (waterScale != prevWaterScale) {
            prevWaterScale = waterScale;
        }
        float steamScale = MekanismUtils.getScale(prevSteamScale, steamTank);
        if (steamScale != prevSteamScale) {
            prevSteamScale = steamScale;
        }
    }

    private double getHeatAvailable() {
        double heatAvailable = (heatCapacitor.getTemperature() - HeatUtils.BASE_BOIL_TEMP) * (heatCapacitor.getHeatCapacity() * MekanismConfig.general.boilerWaterConductivity.get());
        return Math.min(heatAvailable, MekanismConfig.general.superheatingHeatTransfer.get() * superheatingElements);
    }

    public int getWaterVolume() {
        return waterVolume;
    }

    public void setWaterVolume(int volume) {
        if (waterVolume != volume) {
            waterVolume = volume;
            waterTankCapacity = volume * MekanismConfig.general.boilerWaterPerTank.get();
            superheatedCoolantCapacity = volume * MekanismConfig.general.boilerHeatedCoolantPerTank.get();
        }
    }

    public int getSteamVolume() {
        return steamVolume;
    }

    public void setSteamVolume(int volume) {
        if (steamVolume != volume) {
            steamVolume = volume;
            steamTankCapacity = volume * MekanismConfig.general.boilerSteamPerTank.get();
            cooledCoolantCapacity = volume * MekanismConfig.general.boilerCooledCoolantPerTank.get();
        }
    }

    public long getBoilCapacity() {
        double boilCapacity = MekanismConfig.general.superheatingHeatTransfer.get() * superheatingElements / HeatUtils.getWaterThermalEnthalpy();
        return MathUtils.clampToLong(boilCapacity * HeatUtils.getSteamEnergyEfficiency());
    }
}
