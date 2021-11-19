/*
 * Copyright (c) 2020-2021 Polyhedral Development
 *
 * The Terra Core Addons are licensed under the terms of the MIT License. For more details,
 * reference the LICENSE file in this module's root directory.
 */

package com.dfsek.terra.addons.yaml;

import com.dfsek.tectonic.yaml.YamlConfiguration;

import com.dfsek.terra.addons.manifest.api.AddonInitializer;
import com.dfsek.terra.api.Platform;
import com.dfsek.terra.api.addon.BaseAddon;
import com.dfsek.terra.api.event.events.config.ConfigurationDiscoveryEvent;
import com.dfsek.terra.api.event.functional.FunctionalEventHandler;
import com.dfsek.terra.api.inject.annotations.Inject;


public class YamlAddon implements AddonInitializer {
    @Inject
    private Platform platform;
    
    @Inject
    private BaseAddon addon;
    
    @Override
    public void initialize() {
        platform.getEventManager()
                .getHandler(FunctionalEventHandler.class)
                .register(addon, ConfigurationDiscoveryEvent.class)
                .then(event -> event.getLoader().open("", ".yml").thenEntries(entries -> entries.forEach(entry -> {
                    platform.getDebugLogger().info("Discovered config " + entry.getKey());
                    event.register(entry.getKey(), new YamlConfiguration(entry.getValue(), entry.getKey()));
                })))
                .failThrough();
    }
}
