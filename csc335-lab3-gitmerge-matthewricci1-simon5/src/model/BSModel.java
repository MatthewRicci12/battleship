package model;

import java.awt.Point;
import java.util.Observable;
import java.util.Observer;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import objects.Ship;
import view.Move;
import view.WhichGrid;
/**
 * 
 * The all-important model for this game of battleship. It stores many areas of state,
 * and informs the BSView should any of it change. Here are stored miscellania such as
 * ship names, as well as important information such as those ships' sizes. Most importantly,
 * the grids are stored as a char[][] for low memory-cost checks and storage. The shipStates
 * also control the game, 
 * 
 * @author Matthew Ricci
 * @version JDK 14.0.1
 *
 */
public class BSModel extends Observable {
	/**
	 * The dimension of the square grid.
	 */
	public static final int GRID_SIZE = 10;
	/**
	 * The corresponding sizes of the ships.
	 */
	public static final int[] SHIP_SIZES = {5, 4, 3, 2, 2, 1, 1};
	/**
	 * The names of the ships used in this game. To find their lengths, match up
	 * indices with SHIP_SIZES.
	 */
	public static final String[] SHIP_NAMES = {"carrier", "battleship", "cruiser", "destroyer",
			"destroyer2", "raft", "raft2"};
	/**
	 * Tracking the state of the ships, so we can check if they're sunken or not.
	 */
	public Ship[] shipStates = new Ship[SHIP_SIZES.length];
	/**
	 * Your grid represented as a char[][].
	 */
	public char[][] grid = new char[GRID_SIZE][GRID_SIZE]; //9 outer rows,
	/**
	 * The other grid represented as a char[][].
	 */
	public char[][] otherGrid = new char[GRID_SIZE][GRID_SIZE];


	
	
	
	/**
	 * The constructor. Simply allows it to be made.
	 */
	public BSModel() {
	}
	
	
	/**
	 * Adds an Observer to this Observable.
	 * 
	 * Lets you add an observer. Of course, the BSView is generally going to
	 * be the only observer, but this allows you to add it.
	 * 
	 * @param o the Observer that you want to pass in.
	 */
	public void addAsObserver(Observer o) {
		addObserver(o);
	}
	
	/**
	 * Update the grid with a hit.
	 * 
	 * Updates the grid with a hit. If it's your turn, it makes sure to only update
	 * the enemy grid. Otherwise, it will update your grid. This is where the 2d
	 * grid with letters shines. It allows for a quick retrieval and check. Some
	 * debug messages are here for convenience. See the in-line comments for more
	 * information.
	 * 
	 * @param point the Point that you want to hit
	 * @param myTurn true if it's your turn, false if not
	 * @return true if the hit was valid, false if not
	 */
	public boolean updateGridWithHit(Point point, boolean myTurn) {
		int outerArray = point.y;
		int innerArray = point.x;

		System.out.println("Model.updateGridWith Hit. Outerarray: " + outerArray + " innerArray: " + innerArray);
		System.out.println("Model.updateGridWithHit. Myturn: " + myTurn);
		boolean myShipThere = grid[outerArray][innerArray] == 's';
		boolean theirShipThere = otherGrid[outerArray][innerArray] == 's';
		boolean theyAlreadyHitMe = grid[outerArray][innerArray] == 'h';
		boolean iAlreadyHitThem = otherGrid[outerArray][innerArray] == 'h';
		boolean theyAlreadyMissed = grid[outerArray][innerArray] == 'm';
		boolean iAlreadyMissed = otherGrid[outerArray][innerArray] == 'm';
		Move move = null;
		boolean moveValid = true;
		
		if (myTurn) {
			if (!iAlreadyHitThem && !iAlreadyMissed) { //If I've not made the move yet
				if (theirShipThere) { //There's a ship part there
					System.out.println("Their ship there.");
					otherGrid[outerArray][innerArray] = 'h';
					move = Move.HIT;
				} else { //Miss
					otherGrid[outerArray][innerArray] = 'm';
					move = Move.MISS;
				}
			} else {
				//The square is already red/white. Do nothing.
				moveValid = false;
			}
		} else { //Same as above, but if it's not your turn.
			if (!theyAlreadyHitMe && !theyAlreadyMissed) {
				if (myShipThere) {
					grid[outerArray][innerArray] = 'h';
					move = Move.HIT;
					updateShipStates(point);
				} else {
					grid[outerArray][innerArray] = 'm';
					move = Move.MISS;
				}
			} else {
				//The square is already red/white. Do nothing.
				moveValid = false;
			}
		}
		
		

	   if (moveValid) { //Only notify observers if valid. 
		   setChanged();
		   if (myTurn) {
			   notifyObservers(new Object[] {otherGrid, WhichGrid.OTHER_GRID, new Point(outerArray, innerArray), move});
		   } else {
			   notifyObservers(new Object[] {grid, WhichGrid.THIS_GRID, new Point(outerArray, innerArray), move});
		   }
	   }
	   return moveValid;
	   
	}
	
	/**
	 * A ship has been hit so we update it.
	 * 
	 * A ship has been hit. The Ship that corresponds to that point is retrieved,
	 * and setHit() is called, registering a hit to it.
	 * 
	 * @param point the Point at which the ship was hit.
	 */
	public void updateShipStates(Point point) {
		System.out.println("Model updateShipStates: " + point);
		Ship hitShip = Ship.getShipAt(point);
		System.out.println("hitShip: " + hitShip);
		for (Ship ship : shipStates) {
			if (ship.equals(hitShip)) {
				ship.setHit();
			}
		}
		setChanged();
		notifyObservers(shipStates);
	}
	
	/**
	 * Verifies if the game is over by checking ship states.
	 * 
	 * Checks if the game is over. If even one ship is not sunk, then the game must
	 * go on. Otherwise, all ships must be sunk for someone and the game must end.
	 * 
	 * @return true if the game is over, false if not
	 */
	public boolean isGameOver() {
		/**
		 * checks if all ships are sunk
		 */
		for (Ship ship : shipStates) {
			if (!ship.isSunk()) return false;
		}
		return true;
	}
	
	
	/**
	 * Updates the grid with ships when they are permanently placed.
	 * 
	 * When the user decides to place a ship, this is the method that updates the 2d
	 * array representing the grid and notifies the view for GUI processing. It does
	 * a few boolean checks just like the GUI does and then is able to place it.
	 * 
	 * @param point the point origin of the ship
	 * @param ship the Ship object containing important information about that ship,
	 * such as its length
	 * @param dir whether or not the ship is pointed up or left.
	 */
	public void updateGridWithShips(Point point, Ship ship, Directions dir) {
		/**
		 * updates grid with ships
		 */
		int outerArray = point.x;
		int innerArray = point.y;
		boolean shipCollidesWithOtherShip = false;
		boolean shipGoesOffGrid = false;
		//System.out.println("Model.updateGridWithShips: " + point);
		
		switch (dir) {
			case UP:
				//Ship goes off board
				if (outerArray - ship.getLength() < -1) {
					shipGoesOffGrid = true;
					break;
				}
				//There's an 's' in its path
				int checkOuterArray = outerArray;
				for (int i = 0; i < ship.getLength(); i++) {
					if (grid[checkOuterArray][innerArray] == 's') {
						shipCollidesWithOtherShip = true;
					}
					checkOuterArray--;
				}
				if (!shipCollidesWithOtherShip && !shipGoesOffGrid) {
					for (int i = 0; i < ship.getLength(); i++) {
						grid[outerArray][innerArray] = 's'; 
						ship.addPoint(new Point(innerArray, outerArray));
						outerArray--;
					}
				}
				break;
			case LEFT:
				if (innerArray - ship.getLength() < - 1) {
					shipGoesOffGrid = true;
					break;
				}
				int checkInnerArray = innerArray;
				for (int i = 0; i < ship.getLength(); i++) {
					if (grid[outerArray][checkInnerArray] == 's') {
						shipCollidesWithOtherShip = true;
					}
					checkInnerArray--;
				}
				if (!shipCollidesWithOtherShip && !shipGoesOffGrid) {
					for (int i = 0; i < ship.getLength(); i++) {
						grid[outerArray][innerArray] = 's'; 
						ship.addPoint(new Point(innerArray, outerArray));
						innerArray--;
					}
				}
				break;
		}
		
	    setChanged();
	    notifyObservers(new Object[] {grid, WhichGrid.THIS_GRID});
	}
	
	/**
	 * A testing oriented method, allowing you to place ships on the other grid.
	 * 
	 * A testing oriented method that allows bypassing of the GUI so you can place
	 * ships on the other board for testing purposes. It works the same, except it places
	 * it on the other grid instead of having to wait for a socket connection.
	 * 
	 * @param point the point origin of the ship
	 * @param ship the Ship object containing important information about that ship,
	 * such as its length
	 * @param dir whether or not the ship is pointed up or left.
	 */
	public void updateOtherGridWithShips(Point point, Ship ship, Directions dir) {
		int outerArray = point.x;
		int innerArray = point.y;
		boolean shipCollidesWithOtherShip = false;
		boolean shipGoesOffGrid = false;
		//System.out.println("Model.updateGridWithShips: " + point);
		
		switch (dir) {
			case UP:
				if (outerArray - ship.getLength() < -1) {
					shipGoesOffGrid = true;
					break;
				}
				int checkOuterArray = outerArray;
				for (int i = 0; i < ship.getLength(); i++) {
					if (otherGrid[checkOuterArray][innerArray] == 's') {
						shipCollidesWithOtherShip = true;
					}
					checkOuterArray--;
				}
				if (!shipCollidesWithOtherShip && !shipGoesOffGrid) {
					for (int i = 0; i < ship.getLength(); i++) {
						otherGrid[outerArray][innerArray] = 's'; 
						ship.addPoint(new Point(innerArray, outerArray));
						outerArray--;
					}
				}
				break;
			case LEFT:
				if (innerArray - ship.getLength() < - 1) {
					shipGoesOffGrid = true;
					break;
				}
				int checkInnerArray = innerArray;
				for (int i = 0; i < ship.getLength(); i++) {
					if (otherGrid[outerArray][checkInnerArray] == 's') {
						shipCollidesWithOtherShip = true;
					}
					checkInnerArray--;
				}
				if (!shipCollidesWithOtherShip && !shipGoesOffGrid) {
					for (int i = 0; i < ship.getLength(); i++) {
						otherGrid[outerArray][innerArray] = 's'; 
						ship.addPoint(new Point(innerArray, outerArray));
						innerArray--;
					}
				}
				break;
		}
		
	    setChanged();
	    notifyObservers(new Object[] {otherGrid, WhichGrid.OTHER_GRID});
	}
	

	
	/**
	 * Makes empty grids to initialize them.
	 * 
	 * Make the empty grids for the view to base off of. This is an important part
	 * of the reset() method in BSView.
	 * 
	 * @param whichGrid whether or not it's yours or their grid
	 */
	public void initGrid(WhichGrid whichGrid) {
		for (int row = 0; row < GRID_SIZE; row++) { //9
			char[] curRow = new char[GRID_SIZE]; //10
			for (int col = 0; col < GRID_SIZE; col++) { //10
				curRow[col] = ' ';
			}
			grid[row] = curRow;
		}
	    setChanged();
	    if (whichGrid == WhichGrid.THIS_GRID) {
	    	notifyObservers(new Object[] {grid, whichGrid});
	    } else {
	    	notifyObservers(new Object[] {otherGrid, whichGrid});
	    }
	 

	}
	
	/**
	 * Initializes the Ship[] tracking the state of the ships.
	 * 
	 * Initializes the ship states for the reset() method in BSView. This will
	 * populate the Ship[] with all the corresponding Ships in their default values.
	 * Note that we use the Prototype pattern here since the Ships only really vary
	 * in length.
	 */
	public void initShipStates() {	
		Ship carrier = new Ship(SHIP_SIZES[0], SHIP_NAMES[0]); carrier.setRep(); //PROTOTYPE
		shipStates[0] = carrier;   
	    for (int i = 1; i < SHIP_SIZES.length; i++) {
	    	try {
				Ship curShip = (Ship) carrier.clone(); //Clone the ship
				curShip.setLength(SHIP_SIZES[i]); curShip.setName(SHIP_NAMES[i]); 
				curShip.setRep();
				shipStates[i] = curShip;
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
	    }
	    
	    setChanged();
	    notifyObservers(shipStates);
	}
	
	/**
	 * Ends the game, showing an Alert.
	 * 
	 * End the game by making an Alert object to pass to the BSView. Makes different
	 * ones depending on if you won or lost.
	 * 
	 * @param iWin true if you won, false if not.
	 */
	public void endGame(boolean iWin) {
		if (iWin) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setContentText("You win!");
			setChanged();
			notifyObservers(alert);
		} else {
			Alert alert = new Alert(AlertType.INFORMATION); 
			alert.setContentText("You lost! Better luck next time.");
			alert.showAndWait();
			setChanged();
			notifyObservers(alert);
		}
	}
}
