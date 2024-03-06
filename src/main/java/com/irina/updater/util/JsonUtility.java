package com.irina.updater.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JsonUtility {

    public static String getValueFromJsonByKey(File jsonFile, String key) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonFile);
        JsonNode valueNode = jsonNode.get(key);
        if (valueNode != null && valueNode.isTextual()) {
            return valueNode.asText();
        }
        return null;
    }

}
