package com.amp.tsp.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by APritchard on 12/19/2016.
 */
public class SimulatedAnnealingTspSolver extends TspSolver {

  private List<Sector> workPath;
  private List<Sector> previousWorkPath;
  private static final Random random = new Random();

  public SimulatedAnnealingTspSolver(TspSolution.TspBuilder builder) {
    super(builder);
  }

  @Override
  public List<Sector> solve() {
    generateInitialPath();
    List<Sector> bestPath = workPath;
    int bestDistance = getBoundForPath(bestPath);
    logger.info("Starting path: " + TspUtilities.routeString(bestPath));
    logger.info("Distance: " + bestDistance);

    double startingTemperature = 100.0;
    double coolingRate = 0.99999;
    int numberOfIterations = 100000000;

    double t = startingTemperature;

    for(int i = 0; i < numberOfIterations; i++){
      if(t < 0.1) {
//        logger.info("SHUFFLING");
        generateInitialPath();
        t = startingTemperature;
      }
      swapCities();
      int currentDistance = getBoundForPath(workPath);
      if (currentDistance == bestDistance){
        continue; //no change, don't math
      }
      if(currentDistance < bestDistance){
        bestDistance = currentDistance;
        bestPath = new ArrayList(workPath);
        logger.info(String.format("Best Path: %s \nDistance: %d", TspUtilities.routeString(bestPath), bestDistance));
      } else {
        double math = Math.exp((currentDistance - bestDistance) / t);
        if(math < Math.random()) {
          logger.info("REVERT (" + math + ")");
          revertSwap();
        }
      }
      t *= coolingRate;
      if(i %1000000 == 0){
        logger.info(String.format("i(%d) best(%d)", i, bestDistance));
      }
    }

    logger.info(String.format("Returning [%s] Distance: [%d]", TspUtilities.routeString(bestPath), bestDistance));
    return bestPath;
  }


  private void generateInitialPath(){
    if(workPath == null){
      workPath = new ArrayList<>(sectors);
    }
    Collections.shuffle(workPath);
  }

  private void swapCities(){
    int a = random.nextInt(workPath.size());
    int b = random.nextInt(workPath.size());

    previousWorkPath = workPath;
    Sector temp = workPath.get(a);
    workPath.set(a, workPath.get(b));
    workPath.set(b, temp);
  }

  private void revertSwap(){
    workPath = previousWorkPath;
  }
}
