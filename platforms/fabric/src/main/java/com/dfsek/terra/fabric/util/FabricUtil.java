/*
 * This file is part of Terra.
 *
 * Terra is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Terra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Terra.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.dfsek.terra.fabric.util;

import com.dfsek.terra.fabric.config.BiomeColors;

import com.mojang.serialization.Lifecycle;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Supplier;

import com.dfsek.terra.api.block.entity.BlockEntity;
import com.dfsek.terra.api.block.entity.Container;
import com.dfsek.terra.api.block.entity.MobSpawner;
import com.dfsek.terra.api.block.entity.Sign;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.world.biome.Biome;
import com.dfsek.terra.fabric.mixin.access.BiomeEffectsAccessor;


public final class FabricUtil {
    
    private static final Map<RegistryKey<net.minecraft.world.biome.Biome>, List<RegistryKey<net.minecraft.world.biome.Biome>>>
            terraVanillaBiomes = new HashMap<>();
    
    public static String createBiomeID(ConfigPack pack, com.dfsek.terra.api.registry.key.RegistryKey biomeID) {
        return pack.getID()
                   .toLowerCase() + "/" + biomeID.getNamespace().toLowerCase(Locale.ROOT) + "/" + biomeID.getID().toLowerCase(Locale.ROOT);
    }
    
    /**
     * Clones a Vanilla biome and injects Terra data to create a Terra-vanilla biome delegate.
     *
     * @param biome The Terra BiomeBuilder.
     * @param pack  The ConfigPack this biome belongs to.
     */
    public static void registerBiome(Biome biome, ConfigPack pack, DynamicRegistryManager registryManager,
                                     com.dfsek.terra.api.registry.key.RegistryKey id) {
        Registry<net.minecraft.world.biome.Biome> biomeRegistry = registryManager.get(Registry.BIOME_KEY);
        net.minecraft.world.biome.Biome vanilla = ((ProtoPlatformBiome) biome.getPlatformBiome()).get(biomeRegistry);
        
        GenerationSettings.Builder generationSettings = new GenerationSettings.Builder();
        
        BiomeEffectsAccessor accessor = (BiomeEffectsAccessor) vanilla.getEffects();
        BiomeEffects.Builder effects = new BiomeEffects.Builder()
                .grassColorModifier(accessor.getGrassColorModifier());
        
        if(biome.getContext().has(BiomeColors.class)) {
            BiomeColors biomeColors = biome.getContext().get(BiomeColors.class);
            
            if(biomeColors.getWaterColor() == null) {
                effects.waterColor(vanilla.getWaterColor());
            } else {
                effects.waterColor(biomeColors.getWaterColor());
            }
            
            if(biomeColors.getWaterFogColor() == null) {
                effects.waterFogColor(vanilla.getWaterFogColor());
            } else {
                effects.waterFogColor(biomeColors.getWaterFogColor());
            }
            
            if(biomeColors.getFogColor() == null) {
                effects.fogColor(vanilla.getFogColor());
            } else {
                effects.fogColor(biomeColors.getFogColor());
            }
            
            if(biomeColors.getSkyColor() == null) {
                effects.skyColor(vanilla.getSkyColor());
            } else {
                effects.skyColor(biomeColors.getSkyColor());
            }
            
            if(biomeColors.getGrassColor() == null) {
                accessor.getGrassColor().ifPresent(effects::grassColor);
            } else  {
                effects.grassColor(biomeColors.getGrassColor());
            }
            
            if(biomeColors.getFoliageColor() == null) {
                accessor.getFoliageColor().ifPresent(effects::foliageColor);
            } else {
                effects.foliageColor(biomeColors.getFoliageColor());
            }
            
        } else {
            effects.waterColor(accessor.getWaterColor())
                   .waterFogColor(accessor.getWaterFogColor())
                   .fogColor(accessor.getFogColor())
                   .skyColor(accessor.getSkyColor());
            accessor.getFoliageColor().ifPresent(effects::foliageColor);
            accessor.getGrassColor().ifPresent(effects::grassColor);
        }
        
        net.minecraft.world.biome.Biome minecraftBiome = new net.minecraft.world.biome.Biome.Builder()
                .precipitation(vanilla.getPrecipitation())
                .category(vanilla.getCategory())
                .temperature(vanilla.getTemperature())
                .downfall(vanilla.getDownfall())
                .effects(effects.build())
                .spawnSettings(vanilla.getSpawnSettings())
                .generationSettings(generationSettings.build())
                .build();
        
        Identifier identifier = new Identifier("terra", FabricUtil.createBiomeID(pack, id));
        
        if(biomeRegistry.containsId(identifier)) {
            ((ProtoPlatformBiome) biome.getPlatformBiome()).setDelegate(biomeRegistry.get(identifier));
        } else {
            Registry.register(biomeRegistry, identifier, minecraftBiome);
            ((ProtoPlatformBiome) biome.getPlatformBiome()).setDelegate(minecraftBiome);
            terraVanillaBiomes.computeIfAbsent(biomeRegistry.getKey(vanilla).orElseThrow(), b -> new ArrayList<>()).add(
                    biomeRegistry.getKey(minecraftBiome).orElseThrow());
        }
    }
    
    public static Map<RegistryKey<net.minecraft.world.biome.Biome>, List<RegistryKey<net.minecraft.world.biome.Biome>>> getTerraVanillaBiomes() {
        return terraVanillaBiomes;
    }
    
    public static BlockEntity createState(WorldAccess worldAccess, BlockPos pos) {
        net.minecraft.block.entity.BlockEntity entity = worldAccess.getBlockEntity(pos);
        if(entity instanceof SignBlockEntity) {
            return (Sign) entity;
        } else if(entity instanceof MobSpawnerBlockEntity) {
            return (MobSpawner) entity;
        } else if(entity instanceof LootableContainerBlockEntity) {
            return (Container) entity;
        }
        return null;
    }
}
