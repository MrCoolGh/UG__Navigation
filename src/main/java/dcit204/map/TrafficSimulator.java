package dcit204.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class TrafficSimulator {
    private final Random random = new Random();

    // Apply traffic conditions to adjust estimated travel times
    public List<RouteOption> applyTrafficConditions(List<RouteOption> routes) {
        List<RouteOption> adjustedRoutes = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        LocalTime timeOfDay = now.toLocalTime();

        for (RouteOption route : routes) {
            // Create a new route with adjusted time
            int adjustedTime = calculateAdjustedTime(route.getTime(), route.getPath(), dayOfWeek, timeOfDay);

            adjustedRoutes.add(new RouteOption(
                    route.getPath(),
                    route.getDistance(),
                    adjustedTime,
                    route.getLandmarks()
            ));
        }

        return adjustedRoutes;
    }

    // Calculate adjusted time based on traffic conditions
    private int calculateAdjustedTime(int baseTime, List<String> path, DayOfWeek day, LocalTime time) {
        double trafficMultiplier = 1.0;

        // Apply time-of-day factor
        if (isRushHour(time)) {
            trafficMultiplier *= 1.5;  // 50% longer during rush hour
        } else if (isLateNight(time)) {
            trafficMultiplier *= 0.8;  // 20% faster during late night
        }

        // Apply day-of-week factor
        if (isWeekend(day)) {
            trafficMultiplier *= 0.9;  // 10% faster during weekends
        }

        // Apply location-specific factors
        for (String location : path) {
            if (isHighTrafficArea(location)) {
                trafficMultiplier *= 1.2;  // 20% slower in high traffic areas
                break;  // Only apply once
            }
        }

        // Apply some randomness (±10%)
        trafficMultiplier *= (0.9 + random.nextDouble() * 0.2);

        return (int) Math.ceil(baseTime * trafficMultiplier);
    }

    // Check if it's rush hour
    private boolean isRushHour(LocalTime time) {
        return (time.isAfter(LocalTime.of(7, 0)) && time.isBefore(LocalTime.of(10, 0))) ||
                (time.isAfter(LocalTime.of(16, 0)) && time.isBefore(LocalTime.of(19, 0)));
    }

    // Check if it's late night
    private boolean isLateNight(LocalTime time) {
        return time.isAfter(LocalTime.of(22, 0)) || time.isBefore(LocalTime.of(5, 0));
    }

    // Check if it's weekend
    private boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    // Check if location is in a high traffic area
    private boolean isHighTrafficArea(String location) {
        return location.contains("Hall") || location.contains("Gate") ||
                location.contains("Market") || location.contains("Bank");
    }

    // Get current traffic level for a specific location (for UI display)
    public String getTrafficLevel(String location) {
        LocalTime time = LocalTime.now();

        if (isHighTrafficArea(location) && isRushHour(time)) {
            return "Heavy";
        } else if (isHighTrafficArea(location) || isRushHour(time)) {
            return "Moderate";
        } else {
            return "Light";
        }
    }

    // Calculate delay in minutes for a specific route due to traffic
    public int calculateDelay(RouteOption route) {
        int baseTime = route.getTime();
        int adjustedTime = calculateAdjustedTime(
                baseTime,
                route.getPath(),
                LocalDateTime.now().getDayOfWeek(),
                LocalDateTime.now().toLocalTime()
        );

        return adjustedTime - baseTime;
    }
}