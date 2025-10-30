package com.nanaios.CompactMekanismMachinesPlus.common.mixin;

import net.michanide.mekanismneutronactivator.common.tile.machine.TileEntityFusionNeutronActivator;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.loading.LoadingModList;

public class MixinMekanismNeutronActivator {
    public static boolean isLoaded() {
        return LoadingModList.get().getModFileById("mekanismneutronactivator") != null;
    }

    public static void setFuelBurned(BlockEntity entity,long value) {
        if(entity instanceof TileEntityFusionNeutronActivator tile) {
            tile.setFuelBurned(value);
        }
    }
}
