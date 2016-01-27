package com.amp.tsp.capture;

import java.util.List;
import java.util.Map;

import com.amp.tsp.parse.YamlPoint3d;

public interface PointListener {
	void notifySelection(Map<String, YamlPoint3d> points,
			List<String> startingPoints, List<String> endingPoints,
			List<String> warpPoints);
}
