package com.irina.updater.util;

import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ManifestGenerator {
    public static void generateUpdateManifest(String manifestPath, Map<String,String> changedFiles) throws IOException {


            JSONObject jsonObject = new JSONObject();
            List<String> ignoredPaths = getIgnoredPaths();
            jsonObject.put("changes",  changedFiles);
            jsonObject.put("ignorePath", ignoredPaths);
            FileWriter file = new FileWriter(manifestPath);
            file.write(jsonObject.toJSONString());
            file.close();

    }



    public static List<String> getIgnoredPaths(){
        return List.of();
    }
}
