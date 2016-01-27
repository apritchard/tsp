package com.amp.tsp.app;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import com.amp.tsp.mapping.Sector;
import com.amp.tsp.parse.YamlPoint3d;

public interface SelectionListener {
	void finished(List<Sector> path, BufferedImage captureScreen, int distance,
			Map<String, YamlPoint3d> points, List<String> startingPoints,
			List<String> endingPoints, List<String> warpPoints);
}
