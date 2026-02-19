package edu.eci.arsw.blueprints.filters;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiltersTest {

    @Test
    void redundancyFilterShouldRemoveConsecutiveDuplicates() {
        RedundancyFilter filter = new RedundancyFilter();
        List<Point> points = Arrays.asList(
                new Point(10, 10),
                new Point(10, 10),
                new Point(20, 20),
                new Point(20, 20),
                new Point(10, 10));
        Blueprint bp = new Blueprint("john", "test", points);

        Blueprint result = filter.apply(bp);

        List<Point> resPoints = result.getPoints();
        assertEquals(3, resPoints.size());
        assertEquals(10, resPoints.get(0).x());
        assertEquals(20, resPoints.get(1).x());
        assertEquals(10, resPoints.get(2).x());
    }

    @Test
    void undersamplingFilterShouldKeepEverySecondPoint() {
        UndersamplingFilter filter = new UndersamplingFilter();
        List<Point> points = Arrays.asList(
                new Point(10, 10),
                new Point(20, 20),
                new Point(30, 30),
                new Point(40, 40),
                new Point(50, 50));
        Blueprint bp = new Blueprint("john", "test", points);

        Blueprint result = filter.apply(bp);

        List<Point> resPoints = result.getPoints();
        assertEquals(3, resPoints.size());
        assertEquals(10, resPoints.get(0).x());
        assertEquals(30, resPoints.get(1).x());
        assertEquals(50, resPoints.get(2).x());
    }
}
