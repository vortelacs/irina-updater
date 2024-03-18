package com.irina.updater.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class FileChecksumManager {

    public static byte[] calculateChecksum(String file) {
        try {
            return MessageDigest.getInstance("SHA-512").digest(Files.readAllBytes(Paths.get(file)));

        } catch(NoSuchAlgorithmException | IOException e){
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }


}
