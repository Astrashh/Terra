package com.dfsek.terra.addons.manifest.impl.config;

import ca.solostudios.strata.version.Version;
import com.dfsek.tectonic.annotations.Default;
import com.dfsek.tectonic.annotations.Value;
import com.dfsek.tectonic.config.ConfigTemplate;

import com.dfsek.terra.api.util.StringIdentifiable;

import java.util.Collections;
import java.util.List;


@SuppressWarnings("FieldMayBeFinal")
public class AddonManifest implements ConfigTemplate, StringIdentifiable {
    @Value("schema-version")
    private int schemaVersion;
    
    @Value("id")
    private String id;
    
    @Value("version")
    private Version version;
    
    @Value("license")
    private String license;
    
    @Value("contributors")
    private List<String> contributors;
    
    @Value("entrypoints")
    private List<String> entryPoints;
    
    @Value("depends")
    @Default
    private List<DependencyConfig> dependencies = Collections.emptyList();
    
    @Value("website")
    private WebsiteConfig website;
    
    @Override
    public String getID() {
        return id;
    }
    
    public int getSchemaVersion() {
        return schemaVersion;
    }
    
    public Version getVersion() {
        return version;
    }
    
    public List<String> getContributors() {
        return contributors;
    }
    
    public List<String> getEntryPoints() {
        return entryPoints;
    }
    
    public String getLicense() {
        return license;
    }
    
    public WebsiteConfig getWebsite() {
        return website;
    }
    
    public List<DependencyConfig> getDependencies() {
        return dependencies;
    }
}
