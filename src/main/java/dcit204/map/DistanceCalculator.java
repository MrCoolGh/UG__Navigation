package dcit204.map;

import java.util.*;

public class DistanceCalculator {

    // Calculate distance between two locations in the graph
    public double calculateDistance(String start, String end, Map<String, Map<String, Double>> graph) {
        // Use Dijkstra's algorithm to find the shortest path
        Map<String, Double> distances = new HashMap<>();
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(
                Comparator.comparingDouble(node -> node.distance)
        );
        Set<String> visited = new HashSet<>();

        // Initialize
        for (String vertex : graph.keySet()) {
            if (vertex.equals(start)) {
                distances.put(vertex, 0.0);
                priorityQueue.add(new Node(vertex, 0.0));
            } else {
                distances.put(vertex, Double.MAX_VALUE);
                priorityQueue.add(new Node(vertex, Double.MAX_VALUE));
            }
        }

        // Process vertices
        while (!priorityQueue.isEmpty()) {
            Node current = priorityQueue.poll();

            if (visited.contains(current.name)) {
                continue;
            }

            visited.add(current.name);

            if (current.name.equals(end)) {
                return distances.get(end);
            }

            // Explore neighbors
            Map<String, Double> neighbors = graph.get(current.name);
            if (neighbors != null) {
                for (Map.Entry<String, Double> neighbor : neighbors.entrySet()) {
                    if (!visited.contains(neighbor.getKey())) {
                        double newDist = distances.get(current.name) + neighbor.getValue();

                        if (newDist < distances.get(neighbor.getKey())) {
                            // Found a better path
                            distances.put(neighbor.getKey(), newDist);

                            // Update priority queue
                            priorityQueue.add(new Node(neighbor.getKey(), newDist));
                        }
                    }
                }
            }
        }

        return distances.getOrDefault(end, Double.MAX_VALUE);
    }

    // Calculate the total distance of a path
    public double calculatePathDistance(List<String> path, Map<String, Map<String, Double>> graph) {
        double totalDistance = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            String current = path.get(i);
            String next = path.get(i + 1);

            Map<String, Double> neighbors = graph.get(current);
            if (neighbors != null && neighbors.containsKey(next)) {
                totalDistance += neighbors.get(next);
            } else {
                // If direct edge doesn't exist, use the shortest path
                totalDistance += calculateDistance(current, next, graph);
            }
        }

        return totalDistance;
    }

    // Helper class for nodes in Dijkstra's algorithm
    private static class Node {
        String name;
        double distance;

        Node(String name, double distance) {
            this.name = name;
            this.distance = distance;
        }
    }

    // Implement Vogel's Approximation Method for finding initial solution
    public Map<String, String> vogelApproximationMethod(Map<String, Map<String, Double>> graph) {
        // This is a simplified implementation of VAM for the route-finding context
        // In a real implementation, we'd have supply and demand constraints

        Map<String, String> assignments = new HashMap<>();
        Set<String> assignedSources = new HashSet<>();
        Set<String> assignedDestinations = new HashSet<>();

        List<String> allLocations = new ArrayList<>(graph.keySet());

        // Continue until all locations are assigned
        while (assignedSources.size() < allLocations.size() && assignedDestinations.size() < allLocations.size()) {
            String bestSource = null;
            String bestDestination = null;
            double bestCost = Double.MAX_VALUE;

            // Find the location with the largest opportunity cost
            for (String source : allLocations) {
                if (assignedSources.contains(source)) {
                    continue;
                }

                // Find two smallest costs for this source
                double smallest = Double.MAX_VALUE;
                double secondSmallest = Double.MAX_VALUE;
                String smallestDest = null;

                Map<String, Double> neighbors = graph.get(source);
                if (neighbors != null) {
                    for (Map.Entry<String, Double> entry : neighbors.entrySet()) {
                        String dest = entry.getKey();
                        double cost = entry.getValue();

                        if (!assignedDestinations.contains(dest)) {
                            if (cost < smallest) {
                                secondSmallest = smallest;
                                smallest = cost;
                                smallestDest = dest;
                            } else if (cost < secondSmallest) {
                                secondSmallest = cost;
                            }
                        }
                    }
                }

                // Calculate opportunity cost (difference between two smallest)
                double opportunityCost = secondSmallest - smallest;

                // If this source has the best opportunity cost so far, save it
                if (smallestDest != null && smallest < bestCost) {
                    bestSource = source;
                    bestDestination = smallestDest;
                    bestCost = smallest;
                }
            }

            // Make the assignment
            if (bestSource != null && bestDestination != null) {
                assignments.put(bestSource, bestDestination);
                assignedSources.add(bestSource);
                assignedDestinations.add(bestDestination);
            } else {
                break;  // No more valid assignments
            }
        }

        return assignments;
    }
}