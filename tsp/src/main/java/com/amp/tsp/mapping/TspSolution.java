package com.amp.tsp.mapping;

import java.util.List;
import java.util.Set;

public class TspSolution {
	public static int MAX_ACCURACY = 5;
	public static int MIN_ACCURACY = 1;
	
	public static HasSectors forSectors(Set<Sector> sectors){
		return new TspBuilder(sectors);
	}
	
	public interface HasSectors extends Complete {
		public HasSeeds usingSeeds(List<List<Sector>> seeds);
		public Complete usingConstraints(List<Constraint> constraints);
	}

	public interface HasSeeds {
		public Complete optional();
		public Complete required();
	}
	
	public interface Complete {
		public TspSolver accuracy(int accuracy);
	}

	static class TspBuilder implements HasSectors, HasSeeds, Complete {
		private Set<Sector> sectors;
		private List<List<Sector>> seeds;
		private boolean useSeedsOnly;
		private List<Constraint> constraints;	
		
		TspBuilder(Set<Sector> sectors){
			this.sectors = sectors;
		}

		@Override
		public TspSolver accuracy(int accuracy) {
			if(accuracy >= MAX_ACCURACY){
				return new MultiOptimizedTspSolver(this);
			} else {
				return new MultiOptimizedNearestNeighborTspSolver(accuracy, this);
			}
		}

		@Override
		public Complete optional() {
			return this;
		}

		@Override
		public Complete required() {
			this.useSeedsOnly = true;
			return this;
		}

		@Override
		public HasSeeds usingSeeds(List<List<Sector>> seeds) {
			this.seeds =seeds;
			return this;
		}

		@Override
		public Complete usingConstraints(List<Constraint> constraints) {
			this.constraints = constraints;
			return this;
		}
		
		public Set<Sector> getSectors() {
			return sectors;
		}

		public List<List<Sector>> getSeeds() {
			return seeds;
		}

		public boolean isUseSeedsOnly() {
			return useSeedsOnly;
		}

		public List<Constraint> getConstraints() {
			return constraints;
		}			
	}

}
