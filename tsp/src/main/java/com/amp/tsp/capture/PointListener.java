package com.amp.tsp.capture;

import java.awt.Point;
import java.util.List;
import java.util.Map;

public interface PointListener {
	void notifySelection(Map<String, Point> points, List<String> startPoints, List<String> endPoints);
}
