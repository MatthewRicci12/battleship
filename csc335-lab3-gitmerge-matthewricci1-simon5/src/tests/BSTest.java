package tests;
import static org.junit.Assert.assertTrue;

import java.awt.Point;

import org.junit.jupiter.api.Test;

import controller.BSController;
import model.BSModel;
import model.Directions;
import objects.Ship;
import view.BSView;

/**
 * 
 * JUnit test suite to show that the backend works great and to get all non-GUI
 * and non-networking coverage.
 * 
 * @author Matthew Ricci
 * @version JDK 14.0.1
 *
 */
public class BSTest {
	static Point[] listOfPoints = new Point[]{
			new Point(4,0),
			new Point(0,5),
			new Point(4,1),
			new Point(2,3),
			new Point(4,4), 
			new Point(4,5),
			new Point(4,7)};
	static Directions[] correspondingDirs = new Directions[]{Directions.UP, Directions.LEFT, 
			Directions.UP, Directions.LEFT, Directions.UP, Directions.UP,
			Directions.UP};
	static char[][] idealShipPlacement = new char[BSModel.GRID_SIZE][BSModel.GRID_SIZE];
	
	
	
	/*
	 * This test shows how simply calling placeShip on the points
	 * that are listed above, combined with corresponding directions
	 * leads to a grid that looks like what you want.
	 */
	@Test
	void test_ship_placement() {
		idealShipPlacement = TestInitializer.getIdealShipPlacement();
		
		BSView curGame = new BSView();
		curGame.reset();
		BSController controller = curGame.getController();
		Ship[] shipStates = controller.getShipStates();
		for (int i = 0; i < shipStates.length; i++) {
			controller.placeShip(listOfPoints[i], 
					shipStates[i], correspondingDirs[i]);
		}

		//I did it this way since assertEquals for arrays is deprecated.
		
		boolean shipPlacementCorrect = true;
		for (int row = 0; row < BSModel.GRID_SIZE; row++) {
			char[] myRow = controller.getMyGrid()[row];
			char[] idealRow = idealShipPlacement[row];
			for (int col = 0; col < BSModel.GRID_SIZE; col++) {
				if (myRow[col] != idealRow[col]) {
					shipPlacementCorrect = false;
					break;
				}
			}
		}
		System.out.println("GRID COMPARE");
		TestInitializer.print2DArray(controller.getMyGrid());
		TestInitializer.print2DArray(idealShipPlacement);
		assertTrue(shipPlacementCorrect);
		
		
	}
	
	
	/*
	 * This method simulates a game of 2 identical boards, where one board
	 * is victorious over the over. We will show how hits and misses correspond
	 * to the hits and how gameOver detects when all the ships are sunk.
	 */
	@Test
	void test_hits_and_misses_game_over() {
		BSView curGame = new BSView();
		curGame.reset();
		curGame.initRects();
		BSController controller = curGame.getController();
		Ship[] shipStates = controller.getShipStates();
		for (int i = 0; i < shipStates.length; i++) {
			controller.placeShip(listOfPoints[i], 
					shipStates[i], correspondingDirs[i]);
		}
		for (int i = 0; i < shipStates.length; i++) {
			controller.placeShipOnOther(listOfPoints[i], 
					shipStates[i], correspondingDirs[i]);
		}
	
		
		
		
		//curGame.printGrid();
		Point[] givenHits = TestInitializer.givenHits;
		Point[] receivedHits = TestInitializer.receivedHits;
		int hitNumber = 0;
		int getHitNumber = 0;
		boolean myTurn = true;
		boolean isGameOver = controller.isGameOver();
		//System.out.println("Given hits length: " + givenHits.length);
		//System.out.println("Received hits length: " + receivedHits.length);
		while (isGameOver != true) {
			//System.out.println("ABOUT TO MAKE A HIT.");
			Point hit = givenHits[hitNumber];
			//System.out.println("TEST HIT: " + hit);
			controller.makeMove(hit, myTurn);
			hitNumber++;
			//System.out.println();
			//System.out.println("Hit Number: " + hitNumber);
			myTurn = false;
			
			//System.out.println("ABOUT TO RECEIVE A HIT.");
			Point receivedHit = receivedHits[getHitNumber];
			controller.makeMove(receivedHit, myTurn);
			getHitNumber++;
			//System.out.println("Get Hit Number: " + getHitNumber);
			myTurn = true;
			isGameOver = controller.isGameOver();
		}
		System.out.println("PRINT GRID");
		curGame.printGrid();
		
			
	}
	
	@Test
	void test_edge_cases_and_errors() {
		BSView curGame = new BSView();
		curGame.reset();
		curGame.initRects();
		BSController controller = curGame.getController();
		controller.updateTheirGrid(controller.getMyGrid());
		/*
		 * At the end, despite adding multiple ships, all I will have is a
		 * single destroyer in the top left corner.
		 */
	
		char[][] idealShipGrid = new char[10][10];
		idealShipGrid[0][0] = 's'; idealShipGrid[1][0] = 's';
		idealShipGrid[2][0] = 's'; idealShipGrid[3][0] = 's';
		idealShipGrid[4][0] = 's';
		
		idealShipGrid[0][1] = 's'; idealShipGrid[0][2] = 's';
		idealShipGrid[0][3] = 's'; idealShipGrid[0][4] = 's';
		System.out.println("IDEAL SHIP GRID");
		TestInitializer.print2DArray(idealShipGrid);
		
		/*
		 * What I'm gonna do now, is grab a few ships, and 
		 * place them in ways that violate either the overlapping
		 * ship condition or the going off grid condition (check coverage).
		 * For coverage's sake I'm simply going to repeat it for the right grid
		 */
		//Place carrier first
		Ship carrier = controller.getShipStates()[0]; 
		controller.placeShip(new Point(4,0), carrier, Directions.UP);
		Ship battleship = controller.getShipStates()[1]; //4
		Ship cruiser = controller.getShipStates()[2]; //3
		Ship destroyer = controller.getShipStates()[3]; //2
		Ship destroyer2 = controller.getShipStates()[4]; //1
		
		//Place the rest
		controller.placeShip(new Point(0, 3), battleship, Directions.LEFT);
		controller.placeShip(new Point(0, 4), battleship, Directions.LEFT);
		controller.placeShip(new Point(2, 3), cruiser, Directions.UP);
		controller.placeShip(new Point(4, 0), destroyer, Directions.LEFT);
		controller.placeShip(new Point(0, 4), destroyer2, Directions.UP);
		
		//Ta-da
		System.out.println("MY GRID");
		TestInitializer.print2DArray(controller.getMyGrid());

		controller.placeShipOnOther(new Point(4,0), carrier, Directions.UP);
		controller.placeShipOnOther(new Point(0, 3), battleship, Directions.LEFT);
		controller.placeShip(new Point(0, 4), battleship, Directions.LEFT);
		controller.placeShipOnOther(new Point(2, 3), cruiser, Directions.UP);
		controller.placeShipOnOther(new Point(4, 0), destroyer, Directions.LEFT);
		controller.placeShipOnOther(new Point(0, 4), destroyer2, Directions.UP);
		System.out.println("OTHER GRID");
		TestInitializer.print2DArray(controller.getOtherGrid());
		
		//Testing hitting/missing twice for coverage's sake.
		//My turn = true --> Affect other grid
		boolean myTurn = true;
		controller.makeMove(new Point(4,0), myTurn);
		controller.makeMove(new Point(4,0), myTurn);
		controller.makeMove(new Point(9,9), myTurn);
		controller.makeMove(new Point(9,9), myTurn);
		myTurn = false;
		controller.makeMove(new Point(4,0), myTurn);
		controller.makeMove(new Point(4,0), myTurn);
		controller.makeMove(new Point(9,9), myTurn);
		controller.makeMove(new Point(9,9), myTurn);
		

	}
	
}
