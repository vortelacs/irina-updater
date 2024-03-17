package com.irina.updater.util;

public class VersionParser {

    public static long parseNumbers(String numbers) {
        String[] segments = numbers.split("\\.");
        if (segments.length != 4) {
            throw new IllegalArgumentException("Invalid format: must contain four segments");
        }

        long result = 0;
        for (int i = 0; i < 4; i++) {
            int segmentValue = Integer.parseInt(segments[i]);
            if (segmentValue < 0 || segmentValue > 65535) {
                throw new IllegalArgumentException("Invalid segment value: " + segmentValue);
            }
            result |= ((long) segmentValue) << ((3 - i) * 16);
        }
        return result;
    }

}
