package com.stockmonitor.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CronTimeParser {

    /**
     * Helper method to parse a simple cron string and extract hour and minute.
     * This parser assumes the cron expression is in the 6-field format:
     * "seconds minutes hours day-of-month month day-of-week"
     * And that seconds, minutes, and hours are fixed integer values (not '*', '/', '-', ',').
     *
     * @param cronExpression The cron expression string (e.g., "0 30 9 * * MON-FRI")
     * @return A SimpleTime object containing hour and minute, or null if parsing fails or the format is not simple.
     */
    public static SimpleTime parseSimpleCronTime(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            System.err.println("Error: Cron expression is null or empty.");
            return null;
        }

        // Regex to extract the first three parts (seconds, minutes, hours) as numbers
        // This is more robust than splitting by space if there are multiple spaces
        Pattern pattern = Pattern.compile("^(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+.*");
        Matcher matcher = pattern.matcher(cronExpression.trim());

        if (!matcher.matches()) {
            System.err.println("Error: Cron expression '" + cronExpression + "' does not match the expected simple time format (fixed seconds, minutes, hours).");
            return null;
        }

        try {
            int seconds = Integer.parseInt(matcher.group(1)); // First capturing group: seconds
            int minutes = Integer.parseInt(matcher.group(2)); // Second capturing group: minutes
            int hours = Integer.parseInt(matcher.group(3));   // Third capturing group: hours

            // Basic validation for time ranges
            if (seconds < 0 || seconds > 59 || minutes < 0 || minutes > 59 || hours < 0 || hours > 23) {
                System.err.println("Error: Invalid time values (out of range) in cron expression '" + cronExpression + "'.");
                return null;
            }

            return new SimpleTime(hours, minutes, seconds);

        } catch (NumberFormatException e) {
            System.err.println("Error parsing cron time parts to numbers: " + e.getMessage() + " for expression: " + cronExpression);
            return null;
        }
    }

    /**
     * Nested class to hold parsed hour, minute, and second.
     */
    public static class SimpleTime {
        private final int hour;
        private final int minute;
        private final int second; // Added second for completeness

        public SimpleTime(int hour, int minute, int second) {
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }

        public int getHour() {
            return hour;
        }

        public int getMinute() {
            return minute;
        }

        public int getSecond() {
            return second;
        }

        @Override
        public String toString() {
            return String.format("%02d:%02d:%02d", hour, minute, second);
        }
    }
}