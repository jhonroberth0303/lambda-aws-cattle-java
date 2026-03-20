package com.cattle.rules;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import java.io.InputStream;

public class BovineCategoryRulesLoader {
    public static BovineCategoryRulesConfig loadRules(String yamlPath) {
        LoaderOptions loaderOptions = new LoaderOptions();
        Constructor constructor = new Constructor(BovineCategoryRulesConfig.class, loaderOptions);
        Yaml yaml = new Yaml(constructor);
        InputStream inputStream = BovineCategoryRulesLoader.class.getClassLoader().getResourceAsStream(yamlPath);
        if (inputStream == null) {
            throw new RuntimeException("No se encontró el archivo de reglas: " + yamlPath);
        }
        return yaml.load(inputStream);
    }
}
