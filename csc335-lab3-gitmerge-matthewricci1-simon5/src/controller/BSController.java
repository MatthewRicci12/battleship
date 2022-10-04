package controller;

import java.awt.Point;

import model.BSModel;
import model.Directions;
import objects.Ship;
import view.Turn;
import view.WhichGrid;

/**
 * 
 * The controller for the BSView. Not much to say here; it just responds to certain
 * events and controls the model.
 * 
 * @author Matthew Ricci
 * @version JDK 14.0.1
 *
 */

public class BSController {
	/**
   * The associated model for this controller.
	 */
	private BSModel model;
	/**
	 * A boolean that tracks whether the game is over or not.
	 */
	private boolean isGameOver;
	/**
	 * Tracks whose turn it is.
	 */
	private Turn curTurn;
	
	/**
	 * The basic constructor that resets if the game is over, and gives it
	 * a new model.
	 * 
	 * @param model the associated model for this game.
	 */
	public BSController(BSModel model) {
		this.model = model;
		curTurn = Turn.SERVER;
		isGameOver = false;
	}
	
	/**
	 * Calls endGame() of BSModel.
	 * 
	 * Invokes the endgame() method of BSModel.
	 * 
	 * @param iWin a boolean that is true if you won, false if not.
	 */
	public void endGame(boolean iWin) {
		model.endGame(iWin);
	}
	
	
	/**
	 * Tells the model to initialize ship states.
	 * 
	 * Invokes the initShipStates() method of BSModel.
	 */
	public void initShipStates() {
		model.initShipStates();
	}
	
	/**
	 * Tells the model to initialize one of the two grids. 
	 * 
	 * Tells the controller to initialize the grid, either yours or the other
	 * depending on what is passed in.
	 * 
	 * @param whichGrid whether or not it's yours, or the other grid.
	 */
	public void initGrid(WhichGrid whichGrid) {
		model.initGrid(whichGrid);
	}
	
	/**
	 * Invokes the model to place a ship.
	 * 
	 * Invokes the model to place a ship if it passed through the view's
	 * own checks. While you see a GUI it is backed by a 2d array of characters.
	 * 
	 * @param point a Point object corresponding to the origin of that ship
	 * @param ship the Ship you want to place on that Point
	 * @param dir which direction the Ship is pointing - up or left.
	 */
	public void placeShip(Point point, Ship ship, Directions dir) {
		model.updateGridWithShips(point, ship, dir);
	}
	
	/**
	 * A testing oriented method that allows you to place a ship on the other grid.
	 * 
	 * A testing oriented method that bypasses the GUI by allowing you to
	 * decide where the other player's ships should be.
	 * 
	 * @param point a Point object corresponding to the origin of that ship
	 * @param ship the Ship you want to place on that Point
	 * @param dir which direction the Ship is pointing - up or left.
	 */
	public void placeShipOnOther(Point point, Ship ship, Directions dir) {
		model.updateOtherGridWithShips(point, ship, dir);
	}
	
	/**
	 * Checks if the game is over.
	 * 
	 * Invokes the model to check whether or not the game is over.
	 * 
	 * @return true if the game is over, false if not.
	 */
	public boolean isGameOver() {
		return model.isGameOver();
	}
	
	/**
	 * Tells the model that a move has been made.
	 * 
	 * The method that handles clicking a spot to make a move. myTurn controls
	 * whose grid it's on.
	 * 
	 * @param point the Point you want to make a hit at.
	 * @param myTurn whose turn it is; if your turn, it goes on their grid. Otherwise,
	 * it goes on yours.
	 * @return true if the move was valid, false if not.
	 */
	public boolean makeMove(Point point, boolean myTurn) {
		System.out.println("Controller.makeMove, point: " + point);
		System.out.println("Controller.makeMove, myTurn: " + myTurn);
		boolean success = model.updateGridWithHit(point, myTurn);
		isGameOver();
		return success;
	}
	
	/**
	 * Testing oriented method, allows you to access model grid.
	 * 
	 * A testing oriented method that allows direct access to the model's grid.
	 * 
	 * @return model.grid your grid as stored in the model
	 */
	public char[][] getMyGrid() {
		return model.grid;
	}
	
	/**
	 * Testing oriented method, allows you to access model other grid.
	 * 
	 * A testing oriented method that allows direct access to the model's other grid.
	 * 
	 * @return model.otherGrid their grid as stored in the model
	 */
	public char[][] getOtherGrid() {
		return model.otherGrid;
	}
	
	/**
	 * Set the model's other grid for testing purposes.
	 * 
	 * A simple setter for the model's other grid. You just pass in the char[][]
	 * that you want it to be. Mostly a testing method.
	 * 
	 * @param newGrid a char[][] corresponding to the other grid as stroed in
	 * the model.
	 */
	public void updateTheirGrid(char[][] newGrid) {
		model.otherGrid = newGrid;
	}
	
	/**
	 * Access the ship states for testing purposes.
	 * 
	 * A testing method that allows you to access the shipStates, since normally
	 * that is only access through the update() method of the BSView.
	 * 
	 * @return model.shipStates a Ship[] of the current shipStates.
	 */
	public Ship[] getShipStates() {
		return model.shipStates;
	}
	
}
