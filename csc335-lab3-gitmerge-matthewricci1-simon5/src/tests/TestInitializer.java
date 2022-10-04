package tests;

import java.awt.Point;
import java.util.Arrays;

import model.BSModel;
import model.Directions;

/**
 * 
 * I couldn't get JUnit's "Before" or "BeforeClass" methods to work, so this
 * is a throwaway class for sets of points, comparisons, etc.
 * 
 * @author Matthew Ricci
 * @version JDK 14.0.1
 *
 */
public class TestInitializer {
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
	static Point[] receivedHits = new Point[]{
			//A few misses
			new Point(9,8), new Point(8,0), new Point(4,5),
			//sink carrier
			new Point(0,0), new Point(0,1), new Point(0,2), new Point(0,3), new Point(0,4),
			//sink battleship
			new Point(2,0), new Point(3,0), new Point(4,0), new Point(5,0),
			//sink cruiser
			new Point(1,4), new Point(1,3), new Point(1,2),
			//sink destroyer
			new Point(2,2), new Point(3,2),
			//sink destroyer2
			new Point(4,4), new Point(4,3),
			//sink raft
			new Point(5,4),
			//sink raft2
			new Point(7,4)
	};
	static Point[] givenHits = new Point[] {
			//Mostly misses
			new Point(9,9), new Point(6,4), new Point(8,5),
			new Point(1,5), new Point(3,5), new Point(7,5),
			new Point(1,6), new Point(3,6), new Point(7,6),
			new Point(2,0), new Point(8,0), new Point(9,0),
			new Point(3,7), new Point(3,1), new Point(8,6),
			new Point(4,8), new Point(2,9),
			
			//a few hits
			new Point(7,4),
			new Point(1,4), new Point(1,3),
			new Point(0,0), new Point(1,2)
	};
	public static char[][] getIdealShipPlacement() {
		//{"carrier", "battleship", "cruiser", "destroyer",
		//"destroyer2", "raft", "raft2"};
		//Carrier points
		for (int row = 0; row < BSModel.GRID_SIZE; row++) {
			char[] thisRow = idealShipPlacement[row];
			for (int col = 0; col < BSModel.GRID_SIZE; col++) {
				thisRow[col] = ' ';
			}
		}
		idealShipPlacement[0][0] = 's'; idealShipPlacement[1][0] = 's';
		idealShipPlacement[2][0] = 's'; idealShipPlacement[3][0] = 's';
		idealShipPlacement[4][0] = 's';
		
		//Battleship points
		idealShipPlacement[0][2] = 's'; idealShipPlacement[0][3] = 's';
		idealShipPlacement[0][4] = 's'; idealShipPlacement[0][5] = 's';
		
		//Cruiser points
		idealShipPlacement[4][1] = 's'; idealShipPlacement[3][1] = 's';
		idealShipPlacement[2][1] = 's';
		
		//Destroyer points
		idealShipPlacement[2][2] = 's'; idealShipPlacement[2][3] = 's';
		
		//Destroyer2 points
		idealShipPlacement[4][4] = 's'; idealShipPlacement[3][4] = 's';
		
		//Raft points
		idealShipPlacement[4][5] = 's';
		
		//Raft2 points
		idealShipPlacement[4][7] = 's';
		
		return idealShipPlacement;
	}
	
	public static void print2DArray(char[][] array) {
		for (int row = 0; row < BSModel.GRID_SIZE; row++) {
			char[] thisRow = array[row];
			System.out.println(Arrays.toString(thisRow));
			}
		}
}
