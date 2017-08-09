tsp
===

Exploring some traveling salesman solutions.

This began as an attempt to implement a simple Branch and Bound tsp solution and experiment with its performance. It's not a generalized library of any kind yet and, while it passes my tests, it has not been verified against any pre-existing datasets.

## Running or Modifying

The project uses Maven and can be opened in most IDEs that support Java and Maven. I have worked on it both in Eclipse and IntelliJ without problems. If you simply with to build the latest jar yourself, you can clone the project and then execute this maven command from the tsp/tsp project root directory (the folder with the pom.xml):

```
>mvn package assembly:single -DskipTests=true
```

This will create tsp-0.0.1-SNAPSHOT-jar-with-dependencies.jar, which is an executable jar you can use to run the program as a standalone Java application.

## Supported Algorithms

The executable provides the user a sliding scale for computational speed vs. optimal route. 

The first 4 positions on the sliding scale use an n-nearest neighbor heuristic to filter the available destinations map prior to calculation, where n = the position on the slider (so the far left entry is 1-nearest neighbor, the middle entry is 3-nearest neighbors, etc). N-nearest neighbor basically means the branch and bound algorithm will only add the next n-closest destinations, of the destinations not yet visited, onto it's search space analysis, which significantly reduces the performance cost (from O^n to O^[whatever your n-nearest neighbors is]). 

The 5th position on the slider does not use any n-nearest filter, and is guaranteed to produce an optimal route. Be warned that its computation cost is exponential. It has been somewhat optimized and runs in multiple threads, but it will likely explode with sufficient points to navigate.

The problem also has explored a number of other permutations on traveling salesman solving. Some other solvers present in the application but not presented to the user include:
1. BasicTspSolver - the easiest to understand, simple, initial implementation
2. BasicOptimizedTspSolver - The above solver, converted to low-memory-cost data types and primitives to support solving larger paths in memory
3. MultiTspSolver - a multi-threaded solver using the logic of BasicTspSolver
4. MultiOptimizedTspSolver - the fastest optimal solver in the app, used by slider = 5
5. LambdaSolver - attempt to implement Branch and Bound using Java 8 lambda functions. Not very fast
6. OptimizedLambdaSolver - still not great
7. ForkJoinTspSolver - experiment to let Java's ForkJoinPool manage threads for multiple solvers. Not as fast as doing it myself, partially because the component threads don't actually produce discrete amounts of solvable work the way they would ideally in a ForkJoinPool
8. NearestNeighborSolver - initial implementation of the Nearest Neighbor heuristic in the basic solver
9. MultiOptimizedNearestNeighborSolver - by far the fastest solver currently implemented. Combines the n-nearest neighbor heuristic with the multiOptimizedTspSolver code. Used by slider positions 1-4 in the app.
10. SimulatedAnnealingTspSolver - experimental tsp solver using simulated annealing. Was not accurate enough to justify using the heuristic on small data sets, and offered less performance improvement than nearest neighbor.
