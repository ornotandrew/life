import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

/**
 * Sequential Implementation of Conway's Game of Life
 * @author Andrew van Rooyen
 * August 2013
 */
public class GameOfLifeSequential {
	public static GameWindow window;
	static final int mult = 8192; //Must be bigger than expected gridsize, or stuff will break
	//8192 was chosen because it's 2^13
	//~ choose gridsize to be 5000. This doesnt really affect much besides the hashing process 
	//Integer can handle grid sizes of up to 23171 with this hashing method, then more bits are needed
	
	
	public static void main(String[] args) {
		window = new GameWindow(mult, false);
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
		Set<Integer> nextState = new HashSet<Integer>(state);
		Set<Integer> nextMask = new HashSet<Integer>();
		Iterator<Integer> keys = GameWindow.mask.iterator();
		int coord;
		while(keys.hasNext()){
			coord = keys.next();
			if(check(state, coord))
				nextState.add(coord);
			else
				nextState.remove(coord);
			if(nextState.contains(coord)!=state.contains(coord))
				for(int around: GameWindow.getNine(coord))
					nextMask.add(around);
		}
		GameWindow.mask = nextMask;
		return nextState;
	}

	public static void makeMask(Set<Integer> state){
		Set<Integer> stateMask = new HashSet<Integer>();
		Iterator<Integer> coords = state.iterator();
		int coord;
		while(coords.hasNext()){
			coord = coords.next();
			for(int around: GameWindow.getNine(coord)){
				stateMask.add(around);
			}
		}
		GameWindow.mask = stateMask;
	}
	
	public static boolean check(Set<Integer> state, int coord){
		if(coord>mult*5000){ //Kills everything outside the "grid"
			System.out.println("Grid overflowed");
			return false;
		}
		int count = 0;
		for(int around : GameWindow.getEight(coord))
			if(state.contains(around))
				count++;
		if((state.contains(coord) && (count==2||count==3)) || (!state.contains(coord) && count==3)){
			return true;
		}
		else
			return false;
	}
	

	
	
}