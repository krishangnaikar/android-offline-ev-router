package com.krishang.evroute;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

final class Router {
    record Road(String from, String to, int km) {
        Road {
            if (from == null || to == null || km <= 0) {
                throw new IllegalArgumentException("Road needs two endpoints and positive distance");
            }
        }
    }

    record Battery(int capacityWh, int startWh, int reserveWh, int whPerKm) {
        Battery {
            if (capacityWh <= 0 || startWh < 0 || startWh > capacityWh
                    || reserveWh < 0 || reserveWh > capacityWh || whPerKm <= 0) {
                throw new IllegalArgumentException("Invalid battery or consumption");
            }
        }
    }

    record Step(String location, int remainingWh, boolean charged) {}
    record Route(long km, List<Step> steps) {}

    private record Label(Step step, long km, int stops, Label previous) {}

    private static final Comparator<Label> ORDER = Comparator.comparingLong(Label::km)
            .thenComparingInt(Label::stops);

    static Optional<Route> plan(List<Road> roads, Set<String> stations, String origin,
                                String destination, Battery battery) {
        Map<String, List<Road>> outgoing = new HashMap<>();
        for (Road road : roads) {
            outgoing.computeIfAbsent(road.from(), ignored -> new ArrayList<>()).add(road);
        }
        var queue = new PriorityQueue<>(ORDER);
        Map<String, List<Label>> settled = new HashMap<>();
        queue.add(new Label(new Step(origin, battery.startWh(), false), 0, 0, null));
        while (!queue.isEmpty()) {
            Label current = queue.remove();
            Step step = current.step();
            var labels = settled.computeIfAbsent(step.location(), ignored -> new ArrayList<>());
            if (labels.stream().anyMatch(label -> label.step().remainingWh() >= step.remainingWh())) {
                continue;
            }
            labels.add(current);
            if (step.location().equals(destination) && step.remainingWh() >= battery.reserveWh()) {
                var path = new ArrayDeque<Step>();
                for (Label label = current; label != null; label = label.previous()) {
                    path.addFirst(label.step());
                }
                return Optional.of(new Route(current.km(), List.copyOf(path)));
            }
            if (!step.location().equals(destination) && stations.contains(step.location())
                    && step.remainingWh() < battery.capacityWh()) {
                queue.add(new Label(new Step(step.location(), battery.capacityWh(), true),
                        current.km(), current.stops() + 1, current));
            }
            for (Road road : outgoing.getOrDefault(step.location(), List.of())) {
                long needed = (long) road.km() * battery.whPerKm();
                if (needed <= step.remainingWh()) {
                    queue.add(new Label(new Step(road.to(), step.remainingWh() - (int) needed, false),
                            current.km() + road.km(), current.stops(), current));
                }
            }
        }
        return Optional.empty();
    }
}
