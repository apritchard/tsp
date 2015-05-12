package com.amp.tsp.app;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import com.amp.tsp.mapping.Sector;

public interface SelectionListener {
	void finished(List<Sector> path, BufferedImage screenShot, int distance,
			Map<String, Point> points, List<String> startPoints,
			List<String> endPoints);
}
