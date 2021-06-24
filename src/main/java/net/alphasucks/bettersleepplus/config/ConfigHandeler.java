package net.alphasucks.bettersleepplus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.File;
import java.io.IOException;

import static net.fabricmc.loader.api.FabricLoader.getInstance;


public class ConfigHandeler {
    public static ConfigHandeler instance;
    public static boolean waitUntilWriten = false;
    private static File configPath = getInstance().getConfigDir().normalize().resolve("bettersleepplus.yaml").toFile();
    public BSPConfig config;
    private ObjectMapper mapper;

    public ConfigHandeler(BSPConfig config) {
        this.config = config;
        instance = this;
        mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
    }




    public void updateConfig() {
        waitUntilWriten = true;
        try {
            mapper.readValue(configPath, BSPConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        waitUntilWriten = false;
    }

    public BSPConfig getValues() {
        return config;
    }
}
