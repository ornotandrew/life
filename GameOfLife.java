import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Parallel Implementation of Conway's Game of Life
 * @author Andrew van Rooyen
 * August 2013
 */
public class GameOfLife {
	public static GameWindow window;
	static final ForkJoinPool fjPool = new ForkJoinPool();
	static final int mult = 8192; //Must be bigger than expected gridsize, or stuff will break
	//8192 was chosen because it's 2^13
	//~ choose gridsize to be 5000. This doesnt really affect much besides the hashing process 
	//Integer can handle grid sizes of up to 23171 with this hashing method, then more bits are needed
	static Set<Integer> nextMask, nextState, state;
	
	
	public static void main(String[] args) {
		window = new GameWindow(mult, true);
	}

	public static Set<Integer> run(Set<Integer> state, int generations){
		for(int i=0; i<generations; i++){
			if(!GameWindow.running) break;
			state = step(state);
			GameWindow.gen++;
		}
		return state;
	}
	
	public static Set<Integer> step(Set<Integer> state){
		GameOfLife.state = state;
		nextState = new HashSet<Integer>();
		
		//Parallel stuff
		Iterator<Integer> work = state.iterator();
		Set<StepThread> threads = new HashSet<StepThread>();
		StepThread t;
		while(work.hasNext()){
			t = new StepThread(work.next());
			threads.add(t);
			fjPool.execute(t);
		}
		
		
		Iterator<StepThread> threadsIter = threads.iterator();
		while(threadsIter.hasNext())
			threadsIter.next().join();
		
		
		
		
		
		//End Parallel stuff
		return nextState;
	}
	
	public static boolean check(Set<Integer> state, int coord){
		if(coord>mult*5000){ //Kills everything outside the "grid"
			System.out.println("Grid overflowed");
			return false;
		}
		int count = 0;
		for(int around : getEight(coord))
			if(state.contains(around))
				count++;
		if((state.contains(coord) && (count==2||count==3)) || (!state.contains(coord) && count==3)){
			return true;
		}
		else
			return false;
	}
	
	public static int[] getNine(int coord){
		int[] nine = {
				coord-mult-1,
				coord-mult,
				coord-mult+1,
				coord-1,
				coord,
				coord+1,
				coord+mult-1,
				coord+mult,
				coord+mult+1
			};
		return nine;
	}
	public static int[] getEight(int coord){
		int[] eight = {
				coord-mult-1,
				coord-mult,
				coord-mult+1,
				coord-1,
				coord+1,
				coord+mult-1,
				coord+mult,
				coord+mult+1
			};
		return eight;
	}
	
	@SuppressWarnings("serial")
	public static class StepThread extends RecursiveAction{
		int coord;
		
		public StepThread(int coord){
			this.coord = coord;
		}
		
		@Override
		protected void compute() {
			for(int around: getNine(coord))
				if(check(state, around))
					synchronized(nextState){
						nextState.add(around);
					}
		}
		
	}

}












