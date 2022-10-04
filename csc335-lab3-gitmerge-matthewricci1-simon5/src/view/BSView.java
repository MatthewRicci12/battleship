package view;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import controller.BSController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import model.BSModel;
import model.Directions;
import objects.Ship;

/**
 * This is the main-containing face of the battleship game. The view uses socket
 * code in a particular protocol in order to play the game. Please follow the
 * following instructions to play the game
 *
 * SERVER COMMAND LINE ARGUMENTS:
 * server (port number)
 * 
 * CLIENT COMMAND LINE ARGUMENTS:
 * client (port number)
 * 
 * 1. Make a run configuration with the arguments "server 4000" or whatever
 * port number you like. Do the same, but with "client 4000". Name them something
 * like "ServerBS" and "ClientBS" respectively.
 * 2. Open up 2 console windows.
 * 3. Run both configurations, putting one running instance on one console window
 * and the other on the other. 
 * 4. The game will not start, and the sockets will not connect, until all ships
 * are placed. Have the server and client both place their ships. If you press "r",
 * you are able to rotate the ship.
 * 5. Important: THE SERVER ALWAYS MAKES THE FIRST MOVE. So as the server, go ahead
 * and click your guess square, followed by the client. Repeat until one side wins.
 * 6. If you wish to play again, simply re-run this file in the same manner as 
 * described above.
 * 
 * As for implementation details, I use a VBox and an HBox in the scene in order to
 * center 2 gridPanes. To those grid panes I add Rectangle objects. Each Rectangle
 * object is tracked in two data structures: an array (when I need to do something to
 * every rectangle), and a Map for when I want to access a very particular rectangle.
 * Instead of "x" and "y" I use the terms "outerArray" and "innerArray", referring
 * to the first index of a 2d grid and then the second, e.g. [outerArray][innerArray].
 * This is less confusing than x, which is normally horizontal, and y, which is normally
 * vertical, even though the first index actually refers to the height of the underlying grid.
 * This file uses the Observer/Observable pattern with its Controller's BSModel.
 * 
 * @author Matthew Ricci
 * @author Eleanor Simon
 * @version JDK 14.0.1
 *
 */


public class BSView extends Application implements Observer {
	/**
	 * A way to transport these variables from main, which is static, to be
	 * used by an instance later.
	 */
	private static String whichEndStore;
	/**
	 * Same as whichEndStore, but for the port number.
	 */
	private static int portStore;
	/**
	 * The only acceptable string you can type, if you want to play as the server.
	 */
	private static final String SERVER_STRING = "server";
	/**
	 * The only acceptable string you can type, if you want to play as the client.
	 */
	private static final String CLIENT_STRING = "client";
	
	/**
	 * An array, tracking the state of the ships. Mostly used by the model
	 * to determine how many are sunked. Used here to determine the length
	 * of a ship in relation to placing it.
	 */
	private Ship[] shipStates;
	/**
	 * The current ship we are placing.
	 */
	private Ship curShip;
	/**
	 * The current direction we are trying to place the ship. Set to UP by default.
	 */
	private Directions curDir = Directions.UP;

	/**
	 * The grid that the player uses, although it's only ever really updated by
	 * the model.
	 */
	private char[][] grid = new char[BSModel.GRID_SIZE][BSModel.GRID_SIZE];
	/**
	 * The grid that the opponent uses, although it's only ever really updated by
	 * the model.
	 */
	private char[][] otherGrid = new char[BSModel.GRID_SIZE][BSModel.GRID_SIZE];
	/**
	 * The associated controller for this game.
	 */
	private BSController controller;
	/**
	 * A string, tracking if you're the server or client. Only used to check which
	 * socket to make.
	 */
	private String whichEnd;
	/**
	 * The port number that the server and client agree to connect to.
	 */
	private int port;
	/**
	 * Whether or not it is currently your turn.
	 */
	private boolean myTurn;
	
	/**
	 * A map, that maps an outerArray, to an innerArray to a rectangle. Used for picking out
	 * individual rectangles.
	 */
	private static Map<Integer, Map<Integer, Rectangle>> myPointsToRects = new HashMap<>();
	/**
	 * The same as myPointsToRects, but for the grid on the right.
	 */
	private static Map<Integer, Map<Integer, Rectangle>> otherPointsToRects = new HashMap<>();
	/**
	 * The current ship's index.
	 */
	private static int curShipIndex = 0;
	/**
	 * A List of all the user's rectangles. Useful for when something needs to
	 * be done to ALL the rectangles and not just one. Usually for stuff like
	 * event handlers.
	 */
	private List<Rectangle> allMyRects = new ArrayList<>();
	/**
	 * Same as allMyRects, but for the grid on the right.
	 */
	private List<Rectangle> allOtherRects = new ArrayList<>();
	/**
	 * The current rectangle that is being referenced and used.
	 */
	private Rectangle curRect;
	/**
	 * The associated ServerSocket, only if this instance is a server.
	 */
	private ServerSocket server;
	/**
	 * The associated Socket, if this instance is a client.
	 */
	private Socket socket;
	/**
	 * The output stream for this instance. Used to send Point objects.
	 */
	private ObjectOutputStream output;
	/**
	 * The input stream for this instance. Used to receive Point objects.
	 */
	private ObjectInputStream input;
	/**
	 * A boolean denoting whether or not you have won.
	 */
	private boolean iWin = true; 
	
	/**
	 * The constructor, that resets the shipStates, grid, and controller. Note that
	 * this is the only constructor.
	 */
	public BSView() {
		shipStates = null;
		grid = null;
		controller = null;
	}
	
	public static void main(String[] args) {
		//Collect the program commands and make them static temporarily.
		String whichEnd = args[0];
		int port = Integer.parseInt(args[1]);
		if (whichEnd.equals(SERVER_STRING) || whichEnd.equals(CLIENT_STRING)) {
			whichEndStore = whichEnd;
		} else {
			String message = "The command line arguments must be in "
					+ "the following format: server/client (port number)";
			throw new IllegalArgumentException(message);
		}
		portStore = port;
	    launch(args);
	}


	/**
	 * The start method as defined in the JavaFX API.
	 * 
	 * The start method as defined in the JavaFX API. This is where the main curGame
	 * object is made and the scene is set.
	 */
	@Override
	public void start(Stage arg0) throws Exception {
		BSView curGame = new BSView();
		curGame.reset();
		Stage main = new Stage();
		main.setTitle("My JavaFX Application");
		main.setScene(curGame.buildScene()); //Build scene = place ships
		main.show();
		
	}
	

	
	/**
	 * A testing method that initializes the rectangle Maps and Lists.
	 * 
	 * Mostly a method meant for the tester. Prevents a NullPointerException
	 * caused by having an empty map of points to rects.
	 */
	public void initRects() {
		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 10; col++) {
				//Make rectangle
				Rectangle rect = new Rectangle((double) row, (double) col, 50, 50);
				rect.setFill(Color.BLUE); rect.setStroke(Color.BLACK);
				System.out.println();
				
				//Add it to the list of all my rects, as well as to the Map of 
				//points to rects.
				allMyRects.add(rect);
				addToMap((int) rect.getX(), (int) rect.getY(), rect, Turn.SERVER);
			
			}
		}
		
		//Repeat but for the other grid.
		for (int col = 0; col < 10; col++) {
			for (int row = 0; row < 10; row++) {
				Rectangle rect = new Rectangle((double) row, (double) col, 50, 50);
				rect.setFill(Color.BLUE); rect.setStroke(Color.BLACK);
				allOtherRects.add(rect);
				addToMap((int) rect.getX(), (int) rect.getY(), rect, Turn.CLIENT);
			}
		}
	}
	
	/**
	 * Initialize the first scene with the grids and the event handlers.
	 * 
	 * This method builds the Scene. It makes a VBox, adds an HBox, and into that
	 * goes 2 GridPanes. This is also where all the Rectangle objects are instantiated
	 * for use later. Their default events are also configured h ere.
	 * 
	 * @return scene a Scene of which the GUI is comprised of.
	 */
	public Scene buildScene() {
		VBox vbox = new VBox();
		HBox hbox = new HBox();
		vbox.getChildren().add(hbox);
		
		GridPane myGrid = new GridPane();
		GridPane otherGrid = new GridPane();
		hbox.getChildren().add(myGrid);
		hbox.getChildren().add(otherGrid);
		hbox.setSpacing(10.0);
		
		
//		EventHandler<MouseEvent> hnd = new EventHandler<MouseEvent>() {
//			public void handle(MouseEvent ev) { . . .             }
//			}
//public static final EventType<MouseEvent> MOUSE_ENTERED
		

		//MY GRID
		myGrid.setPadding(new Insets(10, 10, 10, 10));
		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 10; col++) {
				Rectangle rect = new Rectangle((double) row, (double) col, 50, 50);
				//Make and add rectangle
				rect.setFill(Color.BLUE); rect.setStroke(Color.BLACK);
				System.out.println();
				
				allMyRects.add(rect);
				addToMap((int) rect.getX(), (int) rect.getY(), rect, Turn.SERVER);
				myGrid.setConstraints(rect, col, row);
				myGrid.getChildren().add(rect);
				
				//Set events
				rect.setOnMouseEntered((event) -> {
					int outerArray = (int) rect.getX();
					int innerArray = (int) rect.getY();
					curRect = getRect(outerArray, innerArray, Turn.SERVER);
					hoverShip(curRect);
				});
				rect.setOnMouseExited((event) -> {
					int outerArray = (int) rect.getX();
					int innerArray = (int) rect.getY();
					curRect = getRect(outerArray, innerArray, Turn.SERVER);
					unhoverShip(curRect);
				});
				rect.setOnMouseClicked((event) -> {
					int outerArray = (int) rect.getX();
					int innerArray = (int) rect.getY();
					System.out.println("Clicked.");
					permanentShip(curRect);
				});
			}
			
		}
		
		//OTHER GRID
		otherGrid.setPadding(new Insets(10, 10, 10, 10));
		for (int col = 0; col < 10; col++) {
			for (int row = 0; row < 10; row++) {
				Rectangle rect = new Rectangle((double) row, (double) col, 50, 50);
				rect.setFill(Color.BLUE); rect.setStroke(Color.BLACK);
				allOtherRects.add(rect);
				otherGrid.setConstraints(rect, col, row);
				addToMap((int) rect.getX(), (int) rect.getY(), rect, Turn.CLIENT);
				otherGrid.getChildren().add(rect);
			}
		}
		
	
		//Set that scene! As well as the rotate event.
		Scene scene = new Scene(vbox, 1075, 700);
		scene.setOnKeyTyped(e -> {
			String text = e.getCharacter();
			if (text.equals("r")) {
				if (curDir == Directions.UP) {
					unhoverShip(curRect);
					curDir = Directions.LEFT;
				} else {
					unhoverShip(curRect);
					curDir = Directions.UP;
				}
			hoverShip(curRect);
			
			}
			
		});
		
		
		return scene;
	}


	/**
	 * Reset the instance variables for this game.
	 * 
	 * Resets the instance variables for the current game and sets a new
	 * BSController and BSModel. This is also where the server/client end
	 * is collected and the grids are made.
	 */
	public void reset() {
		
		whichEnd = whichEndStore;
		port = portStore;
		
		BSModel model = new BSModel();
		model.addAsObserver(this);
		controller = new BSController(model);
		
		controller.initShipStates();
		controller.initGrid(WhichGrid.THIS_GRID);
		controller.initGrid(WhichGrid.OTHER_GRID);
	}
	
	
	/**
	 * Turns the grid gray where the ship would look like, if it were clicked.
	 * 
	 * This method is where the hover-over method occurs. When you hover over a square
	 * while placing ships, this is the method that takes care of the preview. First it 
	 * checks if it's valid. If it's valid, it uses simple math to calculate which 
	 * Rectangles will turn gray.
	 * 
	 * @param rect a Rectangle object that is the origin of the hover-over
	 * lambda.
	 */
	private void hoverShip(Rectangle rect) {
		int outerArray = (int) rect.getX();
		int innerArray = (int) rect.getY();
		boolean overlapsOtherShip = false;
		boolean notInRange = false;
		Ship curShip = null;
		/*
		 * If we're done placing all the ships, we can go ahead and start
		 * doing our socket code.
		 */
		try {
			curShip = shipStates[curShipIndex];
		} catch (IndexOutOfBoundsException|NullPointerException e) {
			prepareForConnection();
		} 
		int shipLength = curShip.getLength();
		switch (curDir) {
			//Mathematically see in advance if it's in range, or collides. 
			case UP:
				if ((outerArray + 1) - shipLength < 0) {
					notInRange = true; break;
				}
				for (int tempOuter = outerArray; tempOuter > (outerArray - shipLength); tempOuter--) {
					if (controller.getMyGrid()[tempOuter][innerArray] == 's') {
						overlapsOtherShip = true;
						break;
					}
				}
				break;
			case LEFT:
				if ((innerArray + 1) - shipLength < 0) {
					notInRange = true; break;
				}
				for (int tempInner = innerArray; tempInner > (innerArray - shipLength); tempInner--) {
					if (controller.getMyGrid()[outerArray][tempInner] == 's') {
						overlapsOtherShip = true;
						break;
					}
				}
				break;
		}
		
		
		
		/*
		 * It's in range, and it doesn't overlap another ship. Set those rectangles
		 * to gray.
		 */
		if (notInRange == false && overlapsOtherShip == false) {
			switch (curDir) {
			case UP:
				for (int tempOuter = outerArray; tempOuter > (outerArray - shipLength); tempOuter--) {
					Rectangle otherRect = getRect(tempOuter, innerArray, Turn.SERVER);
					otherRect.setFill(Color.GRAY);
				}
				break;
			case LEFT:
				for (int tempInner = innerArray; tempInner > (innerArray - shipLength); tempInner--) {
					
					Rectangle otherRect = getRect(outerArray, tempInner, Turn.SERVER);

					otherRect.setFill(Color.GRAY);
				}
				break;
			}
		}
	

		}
	
	/**
	 * Un-dos a non-selected ship preview, turning those squares back to blue.
	 * 
	 * When your mouse exits a square where it was previously hovered over, this
	 * is the method that takes care of restoring it to blue. It is largely the
	 * same process as hovering.
	 * 
	 * @param rect
	 */
	private void unhoverShip(Rectangle rect) {
		//Mostly the same code as hoverShip so check that out.
		int outerArray = (int) rect.getX();
		int innerArray = (int) rect.getY();
		boolean overlapsOtherShip = false;
		boolean notInRange = false;
		Ship curShip = null;
		try {
			curShip = shipStates[curShipIndex];
		} catch (IndexOutOfBoundsException|NullPointerException e) {
			prepareForConnection();
		} 
		int shipLength = curShip.getLength();
		switch (curDir) {
			case UP:
				if ((outerArray + 1) - shipLength < 0) {
					notInRange = true; break;
				}
				for (int tempOuter = outerArray; tempOuter > (outerArray - shipLength); tempOuter--) {
					if (controller.getMyGrid()[tempOuter][innerArray] == 's') {
						overlapsOtherShip = true;
						break;
					}
				}
				break;
			case LEFT:
				if ((innerArray + 1) - shipLength < 0) {
					notInRange = true; break;
				}
				for (int tempInner = innerArray; tempInner > (innerArray - shipLength); tempInner--) {
					if (controller.getMyGrid()[outerArray][tempInner] == 's') {
						overlapsOtherShip = true;
						break;
					}
				}
				break;
		}
		
		
		
		if (notInRange == false && overlapsOtherShip == false) {
			switch (curDir) {
			case UP:
				for (int tempOuter = outerArray; tempOuter > (outerArray - shipLength); tempOuter--) {
					Rectangle otherRect = getRect(tempOuter, innerArray, Turn.SERVER);
					otherRect.setFill(Color.BLUE);
				}
				break;
			case LEFT:
				for (int tempInner = innerArray; tempInner > (innerArray - shipLength); tempInner--) {
					Rectangle otherRect = getRect(outerArray, tempInner, Turn.SERVER);
					otherRect.setFill(Color.BLUE);
			}
			break;
		}
		}
		
		
	}
	
	/**
	 * Permanently places the ship and updates the grid.
	 * 
	 * Permanently places the ship, once the user clicks and decide where their ship
	 * wants to go. It also has a similar algorithm to hovering and unhovering, except
	 * it will disable the event handlers on those squares, so they can no longer
	 * interact with them. The grid will also be updated with the ships.
	 * 
	 * @param rect the Rectangle, that is the origin of where the ship will be placed.
	 */
	private void permanentShip(Rectangle rect) {
		int outerArray = (int) rect.getX();
		int innerArray = (int) rect.getY();
		boolean overlapsOtherShip = false;
		boolean notInRange = false;
		Ship curShip = null;
		try {
			curShip = shipStates[curShipIndex];
		} catch (IndexOutOfBoundsException|NullPointerException e) {
			prepareForConnection();
		} 
		int shipLength = curShip.getLength();
		switch (curDir) {
			case UP:
				if ((outerArray + 1) - shipLength < 0) {
					notInRange = true; break;
				}
				for (int tempOuter = outerArray; tempOuter > (outerArray - shipLength); tempOuter--) {
					if (controller.getMyGrid()[tempOuter][innerArray] == 's') {
						overlapsOtherShip = true;
						break;
					}
				}
				break;
			case LEFT:
				if ((innerArray + 1) - shipLength < 0) {
					notInRange = true; break;
				}
				for (int tempInner = innerArray; tempInner > (innerArray - shipLength); tempInner--) {
					if (controller.getMyGrid()[outerArray][tempInner] == 's') {
						overlapsOtherShip = true;
						break;
					}
				}
				break;
		}
		
		
		
		if (notInRange == false && overlapsOtherShip == false) {
			System.out.println("Pass if");
			switch (curDir) {
			case UP:
				for (int tempOuter = outerArray; tempOuter > (outerArray - shipLength); tempOuter--) {
					Rectangle otherRect = getRect(tempOuter, innerArray, Turn.SERVER);
					otherRect.setFill(Color.GRAY);
					controller.placeShip(new Point(tempOuter, innerArray), curShip, curDir);
					otherRect.setOnMouseClicked((event) -> {});
					otherRect.setOnMouseEntered((event) -> {});
					otherRect.setOnMouseExited((event) -> {});
					printGrid();
				}
				curShipIndex++;
				break;
			case LEFT:
				for (int tempInner = innerArray; tempInner > (innerArray - shipLength); tempInner--) {
					
					Rectangle otherRect = getRect(outerArray, tempInner, Turn.SERVER);

					otherRect.setFill(Color.GRAY);
					controller.placeShip(new Point(outerArray, tempInner), curShip, curDir);
					otherRect.setOnMouseClicked((event) -> {});
					otherRect.setOnMouseEntered((event) -> {});
					otherRect.setOnMouseExited((event) -> {});
					printGrid();
				}
				curShipIndex++;
				break;
			}
		}
	
	}

	/**
	 * Conveniently shows the states of the grids.
	 * 
	 * A convenience method that simply prints out a text-version of your grid
	 * vs. the other grid. Not used for much but debugging.
	 */
	public void printGrid() {
		for (int i = 0; i < grid.length; i++) {
			System.out.print(Arrays.toString(grid[i]));
			System.out.print("      ");
			System.out.print(Arrays.toString(otherGrid[i]));
			System.out.print("\n");
		}
	}
	
	/**
	 * Adds a rectangle to the map of points to rectangles.
	 * 
	 * Given a rectangle, which grid, and some coordinates, this method does the
	 * important task of adding Rectangles to their respective Maps, where they can
	 * conveniently be retrieved with a corresponding coordinate pair. 
	 * 
	 * @param outerArray the "y" coordinate of a point
	 * @param innerArray the "x" coordinate of a point
	 * @param rect a Rectangle object
	 * @param turn whether or not it's going to your grid, or the other grid.
	 */
	private void addToMap(int outerArray, int innerArray, Rectangle rect, Turn turn) {
		System.out.println();
		switch (turn) {
			case SERVER:
				if (myPointsToRects.containsKey(outerArray)) {
					Map<Integer, Rectangle> row = myPointsToRects.get(outerArray);
					System.out.println();
					row.put(innerArray, rect);
				} else {
					Map<Integer, Rectangle> row = new HashMap<>();
					row.put(innerArray, rect); 
					System.out.println();
					myPointsToRects.put(outerArray, row);
					
				}
				break;
			case CLIENT:
				if (otherPointsToRects.containsKey(outerArray)) {
					Map<Integer, Rectangle> row = otherPointsToRects.get(outerArray);
					row.put(innerArray, rect);
				} else {
					Map<Integer, Rectangle> row = new HashMap<>();
					row.put(innerArray, rect); 
					otherPointsToRects.put(outerArray, row); 
				}
				break;
		}
	}
	
	/**
	 * Retrieves the rectangle from the map of points to rectangles.
	 * 
	 * A lifesaving method that conveniently retrieves a Rectangle given a coordinate
	 * pair. This interfaces the complicated nature of the nested Map for convenience.
	 * 
	 * @param outerArray the "y" coordinate of the rectangle
	 * @param innerArray the "x" coordinate of the rectangle
	 * @param turn whether or not it's from your, or the other grid's Map.
	 * @return a Rectangle object correspodning to the coordinate pair.
	 */
	private Rectangle getRect(int outerArray, int innerArray, Turn turn) {
		switch (turn) {
			case SERVER:
				return myPointsToRects.get(outerArray).get(innerArray);
			case CLIENT:
				return otherPointsToRects.get(outerArray).get(innerArray);
		}
		return null;
	}
	
	
	/**
	 * The overriden update() method from the Observer interface.
	 * 
	 * The overriden update() method of the Observer interface. It is able
	 * to scan for 4 different kinds of args based on the type of update. This
	 * makes it very easy to know exactly what needs to be updated. 
	 * 
	 * @param o an Observable
	 * @param arg any Object
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof Ship[]) { //give me the newly formed shipStates
			shipStates = (Ship[]) arg;
		} else if (arg instanceof Object[]) {
			arg = (Object[]) arg;
			int length = ((Object[]) arg).length;
			if (length == 2) { //Grid was initialized
				WhichGrid whichGrid = (WhichGrid) ((Object[]) arg)[1];
				char[][] updatedGrid = (char[][]) ((Object[]) arg)[0];
				
				switch(whichGrid) {
					case THIS_GRID:
						grid = updatedGrid; 
						break;
					case OTHER_GRID:
						otherGrid = updatedGrid; 
						break;
				} 
			} else if (length == 4) {
				/*
				 * We must have made a move. Color the appropriate square
				 * white if it was a miss, red if a hit.
				 */
				System.out.println("Now we're in View.update method");
				WhichGrid whichGrid = (WhichGrid) ((Object[]) arg)[1];
				char[][] updatedGrid = (char[][]) ((Object[]) arg)[0];
				Point point = (Point) ((Object[]) arg)[2];
				Move move = (Move) ((Object[]) arg)[3];
				System.out.println("Point received from model: " + point);
				System.out.println("MOVE:" + move);
				int outerArray = (int) point.getX();
				int innerArray = (int) point.getY();
				
				Rectangle rect = null;
				switch(whichGrid) {
				case THIS_GRID:
					grid = updatedGrid; 
					rect = getRect(outerArray, innerArray, Turn.SERVER);
					break;
				case OTHER_GRID:
					otherGrid = updatedGrid; 
					rect = getRect(outerArray, innerArray, Turn.CLIENT);
					break;
			}
	
				System.out.println("\n");
				if (move == Move.HIT) {
					rect.setFill(Color.RED);
				} else {
					rect.setFill(Color.WHITE);
				}
			}
			
		} else if (arg instanceof Alert) {
			Alert alert = (Alert) arg;
			alert.showAndWait();

		}
	}
	
	/**
	 * Ends the ship-placement turn and prepares the events as well as the socket code.
	 * 
	 * This method is called when the ships are done being placed. It does all
	 * the pre-work before the socket connection, including making your grid
	 * totally non-interactive, and prepping the event handlers for the other 
	 * grid to start sending Point objects. Based on the command line arguments,
	 * the appropriate socket function is called.
	 */
	private void prepareForConnection() {
		for (Rectangle rect : allMyRects) {
			rect.setOnMouseClicked((event) -> {});
			rect.setOnMouseEntered((event) -> {});
			rect.setOnMouseExited((event) -> {});
		}
		for (Rectangle rect : allOtherRects) {
			rect.setOnMouseClicked((event) -> {
				//Only send if it's my turn
				if (myTurn) {
					System.out.println("I made a move.");
					int outerArray = (int) rect.getX();
					int innerArray = (int) rect.getY();
					Point point = new Point(innerArray, outerArray);
					try {
						//Should update THEIR grid.
						System.out.println("My move: " + point);
						boolean success = controller.makeMove(point, myTurn);
						System.out.println("Move was valid: " + success);
						if (success) {
							myTurn = false;
							output.writeObject(point);
							System.out.println("Move was valid: " + success);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
		
	
		if (whichEnd.equals("server")) {
			Thread connectionThread = new Thread(() -> {
				try {
					myTurn = true;
					makeServerSocket(port);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			connectionThread.start();
		} else {
			Thread connectionThread = new Thread(() -> {
				try {
					myTurn = false;
					makeClientSocket(port);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			connectionThread.start();
		}

	}
	
	/**
	 * The reader for the server side socket.
	 * 
	 * Makes the server socket and accepts a connection. It first trades
	 * grids with the client so it has a copy. The socket closing, and the
	 * game being over are two conditions that trigger the endGame() method.
	 * This also makes it clear who won or who lost.
	 * 
	 * @param port the port number specified in the program arguments.
	 * @throws IOException
	 */
	private void makeServerSocket(int port) throws IOException {
		server = new ServerSocket(port);
		socket = server.accept();
		System.out.println("Accepted");
		output = new
		ObjectOutputStream(socket.getOutputStream());
		input = new
		ObjectInputStream(socket.getInputStream());
		
		//TRADE GRIDS
		output.writeObject(grid);
		try {
			char[][] theirGrid = (char[][]) input.readObject();
			otherGrid = theirGrid;
			controller.updateTheirGrid(theirGrid);
			System.out.println("Grid received.");
			printGrid();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		//END TRADE GRIDS
		
		
		
		Object inputObject = null;
		while (!socket.isClosed()) {

			
			//get hit
			try {
				inputObject = (Point) input.readObject();
				if (!myTurn) {
					System.out.println("I have received a message.");
					controller.makeMove((Point) inputObject, myTurn);
					System.out.println("That message is : " + inputObject);
					//Will update Model
	
					myTurn = true;
					if (controller.isGameOver()) {
						System.out.println("Game is over so I closed the socket.");
						iWin = false;
						socket.close();
						endGame();
					}
				}
				
			} catch (ClassNotFoundException|IOException e) {
				socket.close();
				endGame();
			}
		}
		
		
		server.close();
	}
	
	/**
	 * The reader for the client-side socket.
	 * 
	 * A symmetrical method to the makeClientSocket() method. This is essentially
	 * the same code, so see makeServerSocket() for more details.
	 * 
	 * @param port the port number specified in the program arguments
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void makeClientSocket(int port) throws UnknownHostException, IOException {
		socket = new Socket("localhost", port);
		output = new
		ObjectOutputStream(socket.getOutputStream());
		input = new
		ObjectInputStream(socket.getInputStream());
		
		
		//TRADE GRIDS
				output.writeObject(grid);
				try {
					char[][] theirGrid = (char[][]) input.readObject();
					otherGrid = theirGrid;
					controller.updateTheirGrid(theirGrid);
					System.out.println("Grid received.");
					printGrid();
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					endGame();
				} 

				//TRADE GRIDS
		
		
		
		Object inputObject = null;
		while (!socket.isClosed()) {

			
			//get hit
			try {
				inputObject = (Point) input.readObject();
				if (!myTurn) {
					controller.makeMove((Point) inputObject, myTurn);
					myTurn = true;
					if (controller.isGameOver()) {
						System.out.println("Game is over so I closed the socket.");
						iWin = false;
						socket.close();
						endGame();
					}
				}
				
			} catch (ClassNotFoundException|IOException e) {
				socket.close();
				endGame();
			}
		}
		
		socket.close();
	}
	
	
	/**
	 * Ends the game, disabling interaction and displaying the enemy ships to the
	 * loser.
	 * 
	 * When the game is over, this method will disable any further interaction 
	 * and show the loser the enemy ships.
	 * 
	 * @throws IOException
	 */
	private void endGame() throws IOException {
		
		for (Rectangle rect : allOtherRects) {
			rect.setOnMouseClicked((event) -> {});
		}
		input.close();
		output.close();
		
		for (Ship ship : shipStates) {
			System.out.println(ship);
		}
		
		if (iWin) {
			System.out.println("You win!");
			//controller.endGame(iWin);
		} else {
			/*
			 * Change all the un-hit squares to gray to show.
			 */
			for (int outerArray = 0; outerArray < BSModel.GRID_SIZE; outerArray++) {
				for (int innerArray = 0; innerArray < BSModel.GRID_SIZE; innerArray++) {
					if (otherGrid[outerArray][innerArray] == 's') {
						Rectangle rect = getRect(outerArray, innerArray, Turn.CLIENT);	
						rect.setFill(Color.GRAY);
					}
				}
			}
			System.out.println("You lose! :(");
			//controller.endGame(iWin);
		}
		
		
	}
//

	
	/**
	 * Testing method allowing you to access the controller, bypassing the GUI.
	 * 
	 * A testing method that allows the testing suite to access the controller
	 * in order to do things without first going through the GUI.
	 * 
	 * @return controller, the associated BSController
	 */
	public BSController getController() {
		return controller;
	}
	

	

//
	public void quickPlaceShips(Map<Point, Directions> points) {
		
	}
	
	
}
