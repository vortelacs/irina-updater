package com.irina.updater.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IgnoreFileParser {

    public static IgnorePaths compile(List<String> content) {
        String[][] parsed = parse(content);
        List<Pattern> positives = compilePatterns(parsed[0]);
        List<Pattern> negatives = compilePatterns(parsed[1]);

        return new IgnorePaths() {
            @Override
            public boolean accepts(String input) {
                if (input.startsWith("/")) input = input.substring(1);
                return matchesAny(negatives, input) || !matchesAny(positives, input);
            }

            @Override
            public boolean denies(String input) {
                if (input.startsWith("/")) input = input.substring(1);
                return !(matchesAny(negatives, input) || !matchesAny(positives, input));
            }

            @Override
            public boolean maybe(String input) {
                if (input.startsWith("/")) input = input.substring(1);
                return matchesAny(negatives, input) || !matchesAny(positives, input);
            }
        };


    }

//    public static IgnorePaths compile(String content) {
//        String[][] parsed = parse(content);
//        List<Pattern> positives = compilePatterns(parsed[0]);
//        List<Pattern> negatives = compilePatterns(parsed[1]);
//
//        return new IgnorePaths() {
//            @Override
//            public boolean accepts(String input) {
//                if (input.startsWith("/")) input = input.substring(1);
//                return matchesAny(negatives, input) || !matchesAny(positives, input);
//            }
//
//            @Override
//            public boolean denies(String input) {
//                if (input.startsWith("/")) input = input.substring(1);
//                return !(matchesAny(negatives, input) || !matchesAny(positives, input));
//            }
//
//            @Override
//            public boolean maybe(String input) {
//                if (input.startsWith("/")) input = input.substring(1);
//                return matchesAny(negatives, input) || !matchesAny(positives, input);
//            }
//        };
//
//
//    }

    public static String[][] parse(List<String> content) {
        List<String> positives = new ArrayList<>();
        List<String> negatives = new ArrayList<>();

        for (String line : content) {
            line = line.trim();
            if (!line.isEmpty() && line.charAt(0) != '#') {
                if (line.startsWith("!")) {
                    line = line.substring(1);
                    negatives.add(line.startsWith("/") ? line.substring(1) : line);
                } else {
                    positives.add(line.startsWith("/") ? line.substring(1) : line);
                }
            }
        }

        return new String[][]{positives.toArray(new String[0]), negatives.toArray(new String[0])};
    }

    private static List<Pattern> compilePatterns(String[] patterns) {
        List<Pattern> compiledPatterns = new ArrayList<>();
        for (String pattern : patterns) {
            String regexPattern = prepareRegexPattern(pattern);
            compiledPatterns.add(Pattern.compile(regexPattern));
        }
        return compiledPatterns;
    }

    private static boolean matchesAny(List<Pattern> patterns, String input) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    private static String prepareRegexPattern(String pattern) {
        return escapeRegex(pattern).replace("**", "(.+)").replace("*", "([^/]+)");
    }

    private static String escapeRegex(String pattern) {
        return pattern.replaceAll("[-\\[\\]{}()\\^$|?.\\\\+]", "\\\\$0");
    }

    public interface IgnorePaths {
        boolean accepts(String input);

        boolean denies(String input);

        boolean maybe(String input);
    }
}
