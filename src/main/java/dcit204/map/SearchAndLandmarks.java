package dcit204.map;

import java.util.*;

public class SearchAndLandmarks {
    private final RouteFinder routeFinder = new RouteFinder();
    private final DistanceCalculator distanceCalculator = new DistanceCalculator();

    // Find routes that pass through or near a specific landmark
    public List<RouteOption> findRoutesByLandmark(String landmark, Map<String, Map<String, Double>> graph) {
        List<RouteOption> routes = new ArrayList<>();

        // Get all locations
        List<String> allLocations = new ArrayList<>(graph.keySet());

        // Generate some sample routes passing through the landmark
        for (int i = 0; i < Math.min(3, allLocations.size()); i++) {
            String start = getRandomLocation(allLocations, landmark);
            String end = getRandomLocation(allLocations, start, landmark);

            // Find a route from start to landmark
            List<RouteOption> firstLegRoutes = routeFinder.findRoutes(start, landmark, graph);

            // Find a route from landmark to end
            List<RouteOption> secondLegRoutes = routeFinder.findRoutes(landmark, end, graph);

            if (!firstLegRoutes.isEmpty() && !secondLegRoutes.isEmpty()) {
                // Combine the routes
                RouteOption firstLeg = firstLegRoutes.get(0);
                RouteOption secondLeg = secondLegRoutes.get(0);

                List<String> combinedPath = new ArrayList<>(firstLeg.getPath());
                // Remove the duplicate landmark node
                combinedPath.remove(combinedPath.size() - 1);
                combinedPath.addAll(secondLeg.getPath());

                double totalDistance = firstLeg.getDistance() + secondLeg.getDistance();
                int totalTime = firstLeg.getTime() + secondLeg.getTime();

                Set<String> combinedLandmarks = new HashSet<>(firstLeg.getLandmarks());
                combinedLandmarks.addAll(secondLeg.getLandmarks());
                combinedLandmarks.add(landmark); // Ensure the selected landmark is included

                routes.add(new RouteOption(
                        combinedPath,
                        totalDistance,
                        totalTime,
                        new ArrayList<>(combinedLandmarks)
                ));
            }
        }

        return routes;
    }

    // Get a random location that's not the excluded locations
    private String getRandomLocation(List<String> locations, String... exclude) {
        List<String> available = new ArrayList<>(locations);

        for (String exclusion : exclude) {
            available.remove(exclusion);
        }

        if (available.isEmpty()) {
            return null;
        }

        return available.get(new Random().nextInt(available.size()));
    }

    // Binary search to find a location by prefix
    public List<String> searchLocationsByPrefix(String prefix, List<String> locations) {
        List<String> results = new ArrayList<>();

        // Convert to lowercase for case-insensitive search
        prefix = prefix.toLowerCase();

        // Linear search for matches (binary search requires exact match and sorted list)
        for (String location : locations) {
            if (location.toLowerCase().startsWith(prefix)) {
                results.add(location);
            }
        }

        return results;
    }

    // Find routes that pass through multiple specified landmarks
    public List<RouteOption> findRoutesWithMultipleLandmarks(String start, String end,
                                                             List<String> requiredLandmarks,
                                                             Map<String, Map<String, Double>> graph) {
        // Sort landmarks to optimize the path
        List<String> sortedLandmarks = optimizeLandmarkOrder(start, end, requiredLandmarks, graph);

        List<String> fullPath = new ArrayList<>();
        fullPath.add(start);

        double totalDistance = 0;
        int totalTime = 0;

        // Generate path through each landmark in sequence
        String current = start;

        for (String landmark : sortedLandmarks) {
            if (!current.equals(landmark)) {
                List<RouteOption> legRoutes = routeFinder.findRoutes(current, landmark, graph);

                if (!legRoutes.isEmpty()) {
                    RouteOption leg = legRoutes.get(0);

                    // Add all but the first location (to avoid duplicates)
                    List<String> legPath = leg.getPath();
                    for (int i = 1; i < legPath.size(); i++) {
                        fullPath.add(legPath.get(i));
                    }

                    totalDistance += leg.getDistance();
                    totalTime += leg.getTime();
                    current = landmark;
                }
            }
        }

        // Add final leg to destination if needed
        if (!current.equals(end)) {
            List<RouteOption> finalLegRoutes = routeFinder.findRoutes(current, end, graph);

            if (!finalLegRoutes.isEmpty()) {
                RouteOption finalLeg = finalLegRoutes.get(0);

                // Add all but the first location (to avoid duplicates)
                List<String> legPath = finalLeg.getPath();
                for (int i = 1; i < legPath.size(); i++) {
                    fullPath.add(legPath.get(i));
                }

                totalDistance += finalLeg.getDistance();
                totalTime += finalLeg.getTime();
            }
        }

        // Create combined route
        List<RouteOption> result = new ArrayList<>();
        result.add(new RouteOption(fullPath, totalDistance, totalTime, requiredLandmarks));

        // Generate alternative routes using different optimization strategies
        generateAlternativeRoutes(start, end, requiredLandmarks, graph, result);

        return result;
    }

    // Generate alternative routes to provide multiple options
    private void generateAlternativeRoutes(String start, String end,
                                           List<String> landmarks,
                                           Map<String, Map<String, Double>> graph,
                                           List<RouteOption> results) {
        // Try different landmark orderings to generate alternative routes
        if (landmarks.size() <= 1) {
            return;  // No alternatives with 0 or 1 landmark
        }

        // Try a reversed order (except start and end)
        List<String> reversedLandmarks = new ArrayList<>(landmarks);
        Collections.reverse(reversedLandmarks);

        List<String> fullPath = new ArrayList<>();
        fullPath.add(start);

        double totalDistance = 0;
        int totalTime = 0;

        String current = start;

        for (String landmark : reversedLandmarks) {
            if (!current.equals(landmark)) {
                List<RouteOption> legRoutes = routeFinder.findRoutes(current, landmark, graph);

                if (!legRoutes.isEmpty()) {
                    RouteOption leg = legRoutes.get(0);

                    // Add all but the first location
                    List<String> legPath = leg.getPath();
                    for (int i = 1; i < legPath.size(); i++) {
                        fullPath.add(legPath.get(i));
                    }

                    totalDistance += leg.getDistance();
                    totalTime += leg.getTime();
                    current = landmark;
                }
            }
        }

        // Add final leg to destination if needed
        if (!current.equals(end)) {
            List<RouteOption> finalLegRoutes = routeFinder.findRoutes(current, end, graph);

            if (!finalLegRoutes.isEmpty()) {
                RouteOption finalLeg = finalLegRoutes.get(0);

                // Add all but the first location
                List<String> legPath = finalLeg.getPath();
                for (int i = 1; i < legPath.size(); i++) {
                    fullPath.add(legPath.get(i));
                }

                totalDistance += finalLeg.getDistance();
                totalTime += finalLeg.getTime();
            }
        }

        // Add the alternative route if it's different
        RouteOption alternativeRoute = new RouteOption(fullPath, totalDistance, totalTime, landmarks);
        if (!results.contains(alternativeRoute) && !fullPath.isEmpty()) {
            results.add(alternativeRoute);
        }

        // Try another alternative using a different algorithm - NearestNeighbor approach
        // This simulates a greedy algorithm approach
        if (landmarks.size() >= 3) {
            List<String> greedyPath = new ArrayList<>();
            greedyPath.add(start);

            Set<String> unvisited = new HashSet<>(landmarks);
            current = start;
            totalDistance = 0;
            totalTime = 0;

            while (!unvisited.isEmpty()) {
                // Find nearest unvisited landmark
                String nearest = null;
                double minDistance = Double.MAX_VALUE;

                for (String landmark : unvisited) {
                    double distance = distanceCalculator.calculateDistance(current, landmark, graph);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearest = landmark;
                    }
                }

                if (nearest != null) {
                    List<RouteOption> legRoutes = routeFinder.findRoutes(current, nearest, graph);

                    if (!legRoutes.isEmpty()) {
                        RouteOption leg = legRoutes.get(0);

                        // Add all but the first location
                        List<String> legPath = leg.getPath();
                        for (int i = 1; i < legPath.size(); i++) {
                            greedyPath.add(legPath.get(i));
                        }

                        totalDistance += leg.getDistance();
                        totalTime += leg.getTime();
                        current = nearest;
                        unvisited.remove(nearest);
                    } else {
                        unvisited.remove(nearest);  // Can't reach this landmark, skip it
                    }
                } else {
                    break;  // No reachable landmarks left
                }
            }

            // Add final leg to destination
            if (!current.equals(end)) {
                List<RouteOption> finalLegRoutes = routeFinder.findRoutes(current, end, graph);

                if (!finalLegRoutes.isEmpty()) {
                    RouteOption finalLeg = finalLegRoutes.get(0);

                    // Add all but the first location
                    List<String> legPath = finalLeg.getPath();
                    for (int i = 1; i < legPath.size(); i++) {
                        greedyPath.add(legPath.get(i));
                    }

                    totalDistance += finalLeg.getDistance();
                    totalTime += finalLeg.getTime();
                }
            }

            // Add the greedy route if it's different
            RouteOption greedyRoute = new RouteOption(greedyPath, totalDistance, totalTime, landmarks);
            if (!results.contains(greedyRoute) && !greedyPath.isEmpty()) {
                results.add(greedyRoute);
            }
        }
    }

    // Optimize the order of landmarks to minimize total distance
    private List<String> optimizeLandmarkOrder(String start, String end,
                                               List<String> landmarks,
                                               Map<String, Map<String, Double>> graph) {
        // This is a simplified version of the Traveling Salesman Problem
        // For a small number of landmarks, we can use a greedy approach

        List<String> result = new ArrayList<>();
        Set<String> remaining = new HashSet<>(landmarks);
        String current = start;

        while (!remaining.isEmpty()) {
            String next = null;
            double minDistance = Double.MAX_VALUE;

            // Find the closest unvisited landmark
            for (String landmark : remaining) {
                double distance = calculateApproximateDistance(current, landmark, graph);

                if (distance < minDistance) {
                    minDistance = distance;
                    next = landmark;
                }
            }

            if (next != null) {
                result.add(next);
                remaining.remove(next);
                current = next;
            } else {
                break;
            }
        }

        return result;
    }

    // Calculate approximate distance between two points
    private double calculateApproximateDistance(String from, String to, Map<String, Map<String, Double>> graph) {
        return distanceCalculator.calculateDistance(from, to, graph);
    }
}