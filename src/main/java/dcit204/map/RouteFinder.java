package dcit204.map;

import java.util.*;

public class RouteFinder {
    private final DistanceCalculator distanceCalculator = new DistanceCalculator();

    // Find routes from start to destination using Dijkstra's algorithm
    public List<RouteOption> findRoutes(String start, String destination, Map<String, Map<String, Double>> graph) {
        List<RouteOption> routes = new ArrayList<>();

        // Find the shortest path using Dijkstra's algorithm
        RouteOption shortestRoute = dijkstraAlgorithm(graph, start, destination);
        if (shortestRoute != null) {
            routes.add(shortestRoute);
        }

        // Find alternative routes using A* algorithm
        RouteOption aStarRoute = aStarAlgorithm(graph, start, destination);
        if (aStarRoute != null && !routes.contains(aStarRoute)) {
            routes.add(aStarRoute);
        }

        // Add alternative routes (possibly longer but with other advantages)
        addAlternativeRoutes(routes, graph, start, destination);

        return routes;
    }

    // Dijkstra's algorithm for finding shortest path
    private RouteOption dijkstraAlgorithm(Map<String, Map<String, Double>> graph, String start, String destination) {
        // Priority queue for processing vertices
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(node -> node.distance));

        // Distance map
        Map<String, Double> distances = new HashMap<>();

        // Previous node map for path reconstruction
        Map<String, String> previous = new HashMap<>();

        // Initialize
        for (String vertex : graph.keySet()) {
            if (vertex.equals(start)) {
                distances.put(vertex, 0.0);
                priorityQueue.add(new Node(vertex, 0.0));
            } else {
                distances.put(vertex, Double.MAX_VALUE);
                priorityQueue.add(new Node(vertex, Double.MAX_VALUE));
            }
            previous.put(vertex, null);
        }

        // Process vertices
        while (!priorityQueue.isEmpty()) {
            Node current = priorityQueue.poll();

            if (current.name.equals(destination)) {
                // Found destination, reconstruct path
                return constructRoute(previous, distances, start, destination);
            }

            if (current.distance == Double.MAX_VALUE) {
                break;  // Unreachable
            }

            // Explore neighbors
            Map<String, Double> neighbors = graph.get(current.name);
            if (neighbors != null) {
                for (Map.Entry<String, Double> neighbor : neighbors.entrySet()) {
                    double newDist = distances.get(current.name) + neighbor.getValue();

                    if (newDist < distances.get(neighbor.getKey())) {
                        // Found a better path
                        distances.put(neighbor.getKey(), newDist);
                        previous.put(neighbor.getKey(), current.name);

                        // Update priority queue
                        priorityQueue.add(new Node(neighbor.getKey(), newDist));
                    }
                }
            }
        }

        return null;  // No path found
    }

    // A* algorithm for finding optimal path with heuristics
    private RouteOption aStarAlgorithm(Map<String, Map<String, Double>> graph, String start, String destination) {
        // This is a simplified A* implementation for the university campus
        // In a real implementation, we'd use actual geographic coordinates for the heuristic

        // Priority queue for processing vertices
        PriorityQueue<Node> openSet = new PriorityQueue<>(
                Comparator.comparingDouble(node -> node.distance + node.heuristic)
        );

        // Distance map (g-scores)
        Map<String, Double> gScore = new HashMap<>();

        // Previous node map for path reconstruction
        Map<String, String> previous = new HashMap<>();

        // Initialize
        for (String vertex : graph.keySet()) {
            gScore.put(vertex, Double.MAX_VALUE);
            previous.put(vertex, null);
        }

        gScore.put(start, 0.0);
        openSet.add(new Node(start, 0.0, heuristic(start, destination, graph)));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.name.equals(destination)) {
                // Found destination, reconstruct path
                return constructRoute(previous, gScore, start, destination);
            }

            // Explore neighbors
            Map<String, Double> neighbors = graph.get(current.name);
            if (neighbors != null) {
                for (Map.Entry<String, Double> neighbor : neighbors.entrySet()) {
                    double tentativeGScore = gScore.get(current.name) + neighbor.getValue();

                    if (tentativeGScore < gScore.get(neighbor.getKey())) {
                        // Found a better path
                        previous.put(neighbor.getKey(), current.name);
                        gScore.put(neighbor.getKey(), tentativeGScore);

                        // Add to open set with updated score
                        openSet.add(new Node(
                                neighbor.getKey(),
                                tentativeGScore,
                                heuristic(neighbor.getKey(), destination, graph)
                        ));
                    }
                }
            }
        }

        return null;  // No path found
    }

    // Simplified heuristic function (would use actual geographic coordinates in a real implementation)
    private double heuristic(String start, String destination, Map<String, Map<String, Double>> graph) {
        // This is a very simple heuristic
        // In a real implementation, we'd use geographic distance
        return 0;  // Equivalent to Dijkstra's for now
    }

    // Add alternative routes using techniques like Vogel's Approximation Method
    private void addAlternativeRoutes(List<RouteOption> routes, Map<String, Map<String, Double>> graph, String start, String destination) {
        // Add a route that may pass through important landmarks
        // For demonstration, we'll find a path through a random intermediate node

        // First, get a random location that's not start or destination
        List<String> potentialIntermediates = new ArrayList<>(graph.keySet());
        potentialIntermediates.removeIf(loc -> loc.equals(start) || loc.equals(destination));

        if (!potentialIntermediates.isEmpty()) {
            // Choose a random intermediate location
            String intermediate = potentialIntermediates.get(new Random().nextInt(potentialIntermediates.size()));

            // Find path from start to intermediate
            RouteOption firstLeg = dijkstraAlgorithm(graph, start, intermediate);

            // Find path from intermediate to destination
            RouteOption secondLeg = dijkstraAlgorithm(graph, intermediate, destination);

            if (firstLeg != null && secondLeg != null) {
                // Combine the two legs
                List<String> combinedPath = new ArrayList<>(firstLeg.getPath());
                // Remove the duplicate intermediate node
                combinedPath.remove(combinedPath.size() - 1);
                combinedPath.addAll(secondLeg.getPath());

                // Calculate total distance and time
                double totalDistance = firstLeg.getDistance() + secondLeg.getDistance();
                int totalTime = firstLeg.getTime() + secondLeg.getTime();

                // Combine landmarks
                Set<String> combinedLandmarks = new HashSet<>(firstLeg.getLandmarks());
                combinedLandmarks.addAll(secondLeg.getLandmarks());

                // Create the combined route option
                RouteOption alternativeRoute = new RouteOption(
                        combinedPath,
                        totalDistance,
                        totalTime,
                        new ArrayList<>(combinedLandmarks)
                );

                // Add to routes if it's different from existing routes
                if (!routes.contains(alternativeRoute)) {
                    routes.add(alternativeRoute);
                }
            }
        }

        // Additional alternative routes could be added here
    }

    // Reconstruct the path from previous nodes map
    private RouteOption constructRoute(Map<String, String> previous, Map<String, Double> distances, String start, String destination) {
        List<String> path = new ArrayList<>();
        String current = destination;

        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }

        // Check if path is valid
        if (path.isEmpty() || !path.get(0).equals(start)) {
            return null;
        }

        // Calculate distance
        double distance = distances.get(destination);

        // Estimate time (assuming average walking speed)
        int time = (int) Math.ceil(distance / 60);  // Simple estimate: 60m per minute

        // Create a list of landmarks along the route (simplified version)
        List<String> landmarks = identifyLandmarks(path);

        return new RouteOption(path, distance, time, landmarks);
    }

    // Identify landmarks along a route
    private List<String> identifyLandmarks(List<String> path) {
        // In a real implementation, we'd have a database of landmarks and their locations
        // For now, we'll just assume some locations are landmarks
        List<String> landmarks = new ArrayList<>();

        for (String location : path) {
            if (location.toLowerCase().contains("hall") ||
                    location.toLowerCase().contains("library") ||
                    location.toLowerCase().contains("bank") ||
                    location.toLowerCase().contains("center")) {
                landmarks.add(location);
            }
        }

        return landmarks;
    }

    // Helper class for nodes in path-finding algorithms
    private static class Node {
        String name;
        double distance;
        double heuristic;

        Node(String name, double distance) {
            this.name = name;
            this.distance = distance;
            this.heuristic = 0;
        }

        Node(String name, double distance, double heuristic) {
            this.name = name;
            this.distance = distance;
            this.heuristic = heuristic;
        }
    }
}