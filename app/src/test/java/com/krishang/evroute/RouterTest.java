package com.krishang.evroute;

import static org.junit.Assert.*;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class RouterTest {
    private static final List<Router.Road> ROADS = List.of(
            new Router.Road("A", "B", 100), new Router.Road("B", "C", 50));

    private static Router.Battery battery(int start) {
        return new Router.Battery(60_000, start, 6_000, 200);
    }

    @Test public void workingChargerMakesRouteFeasible() {
        var route = Router.plan(ROADS, Set.of("B"), "A", "C", battery(30_000)).orElseThrow();
        assertEquals(150, route.km());
        assertEquals(List.of(new Router.Step("A", 30_000, false),
                new Router.Step("B", 10_000, false), new Router.Step("B", 60_000, true),
                new Router.Step("C", 50_000, false)), route.steps());
        assertTrue(Router.plan(ROADS, Set.of(), "A", "C", battery(30_000)).isEmpty());
    }

    @Test public void exactReserveAvoidsUnnecessaryCharge() {
        var route = Router.plan(ROADS, Set.of("B"), "A", "C", battery(36_000)).orElseThrow();
        assertEquals(new Router.Step("C", 6_000, false), route.steps().get(2));
        assertTrue(Router.plan(ROADS, Set.of(), "A", "C", battery(35_999)).isEmpty());
    }

    @Test public void stationIsReachableWithExactlyZeroEnergyButNotOneWhLess() {
        assertTrue(Router.plan(ROADS, Set.of("B"), "A", "C", battery(20_000)).isPresent());
        assertTrue(Router.plan(ROADS, Set.of("B"), "A", "C", battery(19_999)).isEmpty());
    }

    @Test public void longerPathWithChargeBeatsInfeasibleShortcut() {
        var roads = List.of(new Router.Road("A", "B", 20), new Router.Road("B", "C", 120),
                new Router.Road("A", "D", 60), new Router.Road("D", "B", 30));
        assertEquals(210, Router.plan(roads, Set.of("D"), "A", "C", battery(24_000))
                .orElseThrow().km());
    }

    @Test public void emptyGraphAllowsOnlyAlreadyFeasibleArrival() {
        assertEquals(0, Router.plan(List.of(), Set.of(), "A", "A", battery(6_000)).orElseThrow().km());
        assertTrue(Router.plan(List.of(), Set.of("A"), "A", "A", battery(5_999)).isEmpty());
        assertTrue(Router.plan(List.of(), Set.of(), "A", "C", battery(60_000)).isEmpty());
    }

    @Test public void invalidInputFailsAndLargeRoadDoesNotOverflow() {
        assertThrows(IllegalArgumentException.class, () -> battery(-1));
        assertThrows(IllegalArgumentException.class, () -> battery(60_001));
        assertThrows(IllegalArgumentException.class, () -> new Router.Battery(10, 5, 11, 1));
        assertThrows(IllegalArgumentException.class, () -> new Router.Battery(10, 5, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Router.Road("A", "B", 0));
        assertTrue(Router.plan(List.of(new Router.Road("A", "B", Integer.MAX_VALUE)),
                Set.of(), "A", "B", battery(60_000)).isEmpty());
    }
}
