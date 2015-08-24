/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wezro.dedication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author florian
 */
public class Helper {

    private static final Map<String, TimeUnit> base = new HashMap<>();
    private static final Map<String, Container> suffixes = new HashMap<>();
    private static final TimeUnit[] units;
    private static final Map<TimeUnit, WordFor> names;

    static {
        base.put("ms", TimeUnit.MILLISECONDS);
        base.put("h", TimeUnit.HOURS);
        base.put("m", TimeUnit.MINUTES);
        base.put("s", TimeUnit.SECONDS);
        base.put("d", TimeUnit.DAYS);

        for (Map.Entry<String, TimeUnit> entry : base.entrySet()) {
            suffixes.put(entry.getKey(), new Container(entry.getValue(), entry.getKey()));
        }

        units = new TimeUnit[]{TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS, TimeUnit.MILLISECONDS};
        names = new HashMap<>();
        names.put(TimeUnit.DAYS, new WordFor("day", "days"));
        names.put(TimeUnit.HOURS, new WordFor("hour", "hours"));
        names.put(TimeUnit.MINUTES, new WordFor("minute", "minutes"));
        names.put(TimeUnit.SECONDS, new WordFor("second", "seconds"));
        names.put(TimeUnit.MILLISECONDS, new WordFor("milli second", "milli seconds"));
    }

    public static long timeStringAsMillis(final String input) {
        final String matchable = input.toLowerCase();
        long time = 0;
        boolean found = false;
        for (Map.Entry<String, Container> en : suffixes.entrySet()) {
            Pattern pat = en.getValue().getPattern();
            Matcher matcher = pat.matcher(matchable);
            found = matcher.matches() || found;
            if (matcher.matches()) {
                if (matcher.groupCount() > 0) {
                    throw new IllegalArgumentException("Unmatchable string, pattern more than once: " + matchable);
                } else if (matcher.groupCount() == 0) {
                    time += parseTimePartString(matcher.group(), en.getValue());
                    break;
                }
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Cannot parse timestring. No time values found. (" + matchable + ")");
        }

        return time;
    }

    public static String millisAsString(long millis) {

        StringBuilder out = new StringBuilder();

        for (TimeUnit unit : units) {
            long value = unit.convert(millis, TimeUnit.MILLISECONDS);
            if (value > 0) {

                if (out.length() > 0) {
                    out.append(" ");
                }
                out.append(value).append(" ").append(names.get(unit).getFor(value));
                millis -= TimeUnit.MILLISECONDS.convert(value, unit);

            }
        }

        return out.toString();
    }

    private static long parseTimePartString(final String value, Container matching) {
        return matching.getUnit().toMillis(Long.parseLong(value.replace(matching.getStrSuffix(), "")));
    }

    public static Map<String, Container> getSuffixes() {
        return suffixes;
    }

    public final static class WordFor {

        private final String singular;
        private final String plural;

        public WordFor(String singular, String plural) {
            this.singular = singular;
            this.plural = plural;
        }

        public String getFor(long value) {
            if (value == 0 || value != 1) {
                return plural;
            } else {
                return singular;
            }
        }
    }

    public final static class Container {

        private final TimeUnit unit;
        private final String strSuffix;
        private final String timeParts;
        private final Pattern pattern;

        public Container(TimeUnit unit, String strSuffix) {
            this.unit = unit;
            this.strSuffix = strSuffix;
            this.timeParts = "[0-9]+" + strSuffix;
            this.pattern = Pattern.compile(timeParts);
        }

        public TimeUnit getUnit() {
            return unit;
        }

        public String getStrSuffix() {
            return strSuffix;
        }

        public String getTimeParts() {
            return timeParts;
        }

        public Pattern getPattern() {
            return pattern;
        }

    }
}
