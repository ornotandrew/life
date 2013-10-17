import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class GameWindow extends JFrame {
	static int mult;
	public static int gen = 0;
	public static boolean running;
	public static Set<Integer> state;
	public static Set<Integer> mask = new HashSet<Integer>();
	static Random rand;
	String[] ICOptionStrings = {"R-Pentomino", "Gosper Glider Gun", "Random Sparse", "Random Heavy", "User"};
	Grid grid;
	boolean isThreaded;
	Timer timer, updater;
	double tstart, tend, time = 0;
	JCheckBox showSteps;
	JComboBox<String> ICOptions;
	JTextField genInput;
	JButton startButton;
	JLabel timePanel, numGen, population;
	
	GameStarter s;
	
	public GameWindow(int m, boolean thr){
		super();
		isThreaded = thr;
		mult = m;
		rand = new Random();
		//Make UI
		if(isThreaded)
			setTitle("Game of Life");
		else
			setTitle("Sequential Game of Life");
		setSize(1200,740);
		setResizable( false );
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		add(createBar(), BorderLayout.PAGE_END);
		
		//Make Timers
		timer = new Timer(100, new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	time+=100;
		    	timePanel.setText("Time: "+time/1000);
		    }
		});
		updater = new Timer(100, new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	updateUI(GameWindow.state);
		    }
		});
		
		grid = new Grid();
		add(grid, BorderLayout.CENTER);
		updateUI(rpentomino());
		setVisible(true);
	}

	public void updateUI(Set<Integer> newState){
		state = newState;
		population.setText("Population: "+state.size());
		grid.repaint();
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
	
	public static HashSet<Integer> gosper(){
		HashSet<Integer> gosp = new HashSet<Integer>();
		int[][] coords = {
				{-5,7},
				{-4,5}, {-4,7},
				{-3,-5}, {-3,-4}, {-3,3}, {-3,4}, {-3,17}, {-3,18},
				{-2,-6}, {-2,-2}, {-2,3}, {-2,4}, {-2,17}, {-2,18},
				{-1,-17}, {-1,-16}, {-1,-7}, {-1,-1}, {-1,3}, {-1,4},
				{0,-17}, {0,-16}, {0,-7}, {0,-3}, {0,-1}, {0,0}, {0,5}, {0,7},
				{1,-7}, {1,-1}, {1,7},
				{2,-6}, {2,-2},
				{3,-5}, {3,-4}
		};
		for(int[] coord: coords){
			gosp.add(coord[0]*mult+coord[1]);
			for(int c: getNine(coord[0]*mult+coord[1]))
				mask.add(c);
		}
		return gosp;
	}
	
	public static HashSet<Integer> rpentomino(){
		HashSet<Integer> pent = new HashSet<Integer>();
		int[][] coords = {
				{-1,0}, {-1,1},
				{0,-1}, {0,0},
				{1,0}
		};
		for(int[] coord: coords){
			pent.add(coord[0]*mult+coord[1]);
			for(int c: getNine(coord[0]*mult+coord[1]))
				mask.add(c);
		}
		return pent;
	}
	
	//fills out a 500x500 block
	//var fill [0,2] is a percentage. will be lower in practice because of overlapping not being handled (no real need), so 200% is valid
	public static HashSet<Integer> random(double fill){
		HashSet<Integer> random = new HashSet<Integer>();
		int max_coord = 501*mult;
		if(fill<0||fill>2)
			fill = 1;
		for(double i=0; i<250000*fill; i++){
			int coord = (rand.nextBoolean()? 1 : -1)*rand.nextInt(max_coord);
			random.add(coord);
			for(int c: getNine(coord))
				mask.add(c);
		}
		return random;
	}
	
	class Grid extends JPanel{
		Grid(){
			super.setBackground(new Color(25,25,25));
			ClickListener l = new ClickListener(); 
			addMouseListener(l);
			addMouseMotionListener(l);
			}

		public void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        for(int y=-49; y<50; y++){
				for(int x=-86; x<87; x++){
					Color c = (state.contains(y*mult+x)? Color.white : Color.black);
//					if(mask.contains(y*mult+x) && !state.contains(y*mult+x))
//						c = Color.red;
					g.setColor(c);
					g.fillRect(7*(x+86), 7*(y+49), 6, 6);
				}
			}
	    }
	}
	
	class ClickListener extends MouseAdapter{
		int coord, prevCoord = 300;
		public void mousePressed(MouseEvent e){
			ICOptions.setSelectedIndex(4);
			coord = (e.getY()/7-58)*mult+e.getX()/7-115;
			setBlock(coord);
			updateUI(state);
		}
		public void mouseDragged(MouseEvent e){
			coord = (e.getY()/7-58)*mult+e.getX()/7-115;
			if(coord!=prevCoord){
				setBlock(coord);
				prevCoord = coord;
				updateUI(state);
			}
		}
		public void setBlock(int coord){
			if(state.contains(coord))
				state.remove(coord);
			else{
				state.add(coord);
				for(int c: getNine(coord))
					mask.add(c);
			}
		}
	}
	
	class ComboListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
	        int option = ICOptions.getSelectedIndex();
	        if(option==4)
	        	return;
	        mask.clear();
	        if(option==0)
	        	updateUI(rpentomino());
	        else if(option==1)
	        	updateUI(gosper());
	        else if(option==2)
	        	updateUI(random(0.5));
	        else if(option==3)
	        	updateUI(random(2));
        	gen = 0;
        	if(timer!=null)
        		timer.stop();
        	time = 0;
        	numGen.setText("Generations: 0");
		}
		
	}
	
	class StartListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			if(running){
				running = false;
				timer.stop();
			}
			else{
				s = new GameStarter();
				s.execute();
			}
		}
	}

	
	public class GameStarter extends SwingWorker<Set<Integer>, Set<Integer>>{
		
		@Override
		protected Set<Integer> doInBackground() throws Exception {
			startButton.setText("Stop");
			GameWindow.running = true;
			ICOptions.setSelectedIndex(4);
			int generations = Integer.parseInt(genInput.getText());
			tstart = System.currentTimeMillis();
			timer.start();
			if(showSteps.isSelected()){
				updater.start();
				for(int g=0; g<generations; g++){
					if(isThreaded)
						GameWindow.state = GameOfLife.run(GameWindow.state, 1);
					else
						GameWindow.state = GameOfLifeSequential.run(GameWindow.state, 1);
					numGen.setText("Generations: "+GameWindow.gen);
				}
				updater.stop();
			}
			else{
				if(isThreaded)
					GameWindow.state = GameOfLife.run(GameWindow.state, generations);
				else
					GameWindow.state = GameOfLifeSequential.run(GameWindow.state, generations);
				numGen.setText("Generations: "+GameWindow.gen);
			}
			timer.stop();
			tend = System.currentTimeMillis();
			//Get time manually. Should almost always match timer time, but the timer thread can lag with heavy input
			timePanel.setText("Time: "+Math.floor((tend-tstart)/100)/10);
			startButton.setText("Start");
			GameWindow.running = false;
			return GameWindow.state;
		}
		
		@Override
		protected void process(List<Set<Integer>> states){
			Iterator<Set<Integer>> iter = states.iterator();
			updateUI(iter.next());
		}
		
		@Override
		protected void done(){
			try {
				updateUI(get());
			} catch (Exception e) {
				startButton.setText("Start");
				GameWindow.running = false;
				System.out.println("Could not start - Check start options");
				e.printStackTrace();
			}
		}
	}
	
	JPanel createBar(){
		JPanel bottomBar = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		
		//Initial condition chooser
		c.gridy = c.gridx = 0; c.insets = new Insets(5,20,5,0);
		bottomBar.add(new JLabel("Initial Conditions"), c);
		c.insets = new Insets(5,10,5,0);
		ICOptions = new JComboBox<String>(ICOptionStrings);
		ICOptions.addActionListener (new ComboListener());
		c.gridx = 1;
		bottomBar.add(ICOptions, c);
		
		//Generations Chooser
		c.gridx = 2; c.insets = new Insets(5,10,5,0);
		bottomBar.add(new JLabel("Generations"), c);
		genInput = new JTextField(4);
		genInput.setHorizontalAlignment(JTextField.RIGHT);
		c.gridx = 3; c.insets = new Insets(5,10,5,0);
		bottomBar.add(genInput, c);
		
		//Show steps
		showSteps = new JCheckBox("Show Steps");
		c.gridx = 4; c.insets = new Insets(5,10,5,0);
		bottomBar.add(showSteps, c);
		
		//Start button
		startButton = new JButton("Start");
		startButton.addActionListener(new StartListener());
		c.weightx = 1;
		c.gridx = 5; c.insets = new Insets(5,10,5,0);
		bottomBar.add(startButton, c);
		c.weightx = 0;
		
		//Population Counter
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 6; c.insets = new Insets(5,10,5,20);
		population = new JLabel("Population: 0");
		bottomBar.add(population, c);
		
		//Generations Counter
		c.gridx = 7; c.insets = new Insets(5,10,5,20);
		numGen = new JLabel("Generations: 0");
		bottomBar.add(numGen, c);
				
		//Time
		timePanel = new JLabel("Time: 00:00");
		c.gridx = 8; c.insets = new Insets(5,10,5,20);
		bottomBar.add(timePanel, c);
		
		
		
		return bottomBar;
	}
	
	
}
