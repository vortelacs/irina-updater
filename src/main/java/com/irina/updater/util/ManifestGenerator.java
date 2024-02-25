package com.irina.updater.util;

import org.json.simple.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ManifestManager {

    public void generateManifest(String path) throws IOException {

        Map<String, String> changedFiles = checkChanges();

        if(!changedFiles.isEmpty()){
            JSONObject jsonObject = new JSONObject();
            List<String> ignoredPaths = getIgnoredPaths();
            jsonObject.put("ignorePath", ignoredPaths);
            jsonObject.put("changes",  changedFiles);
            FileWriter file = new FileWriter("manifest.json");
            file.write(jsonObject.toJSONString());
            file.close();
        }

    }

    public Map<String, String> checkChanges(){
//        Map<String, String> fileList = FileChecksumVerifier.getCheckSumList();
        return Map.of();
    }




    public List<String> getIgnoredPaths(){
        return List.of();
    }
}
