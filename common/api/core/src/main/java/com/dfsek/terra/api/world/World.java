package com.dfsek.terra.api.world;

import com.dfsek.terra.api.Handle;
import com.dfsek.terra.api.config.WorldConfig;
import com.dfsek.terra.api.world.biome.generation.BiomeProvider;
import com.dfsek.terra.api.world.chunk.generation.ChunkGenerator;


public interface World extends Handle {
    long getSeed();
    
    int getMaxHeight();
    
    int getMinHeight();
    
    ChunkGenerator getGenerator();
    
    BiomeProvider getBiomeProvider();
    
    WorldConfig getConfig();
}