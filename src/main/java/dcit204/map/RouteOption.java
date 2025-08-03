package dcit204.map;

import java.util.List;
import java.util.Objects;

public class RouteOption {
    private final List<String> path;
    private final double distance;
    private final int time;
    private final List<String> landmarks;

    public RouteOption(List<String> path, double distance, int time, List<String> landmarks) {
        this.path = path;
        this.distance = distance;
        this.time = time;
        this.landmarks = landmarks;
    }

    public List<String> getPath() {
        return path;
    }

    public double getDistance() {
        return distance;
    }

    public int getTime() {
        return time;
    }

    public List<String> getLandmarks() {
        return landmarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteOption that = (RouteOption) o;
        return Double.compare(that.distance, distance) == 0 &&
                time == that.time &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, distance, time);
    }

    @Override
    public String toString() {
        return "Route from " + path.get(0) + " to " + path.get(path.size() - 1) +
                ", distance: " + distance + "m, time: " + time + " min";
    }
}