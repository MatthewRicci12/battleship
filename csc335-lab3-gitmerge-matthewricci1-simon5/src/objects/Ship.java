package objects;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * This object represents a Ship, which stores state such as its length, its name,
 * whether or not it is sunk, its representation, and most importantly how many
 * hits it has taken. In addition, a point is mapped to a ship, so when a hit is made,
 * it is easy to find out exactly which Ship took the hit.
 * 
 * @author Matthew Ricci
 * @version JDK 14.0.1
 *
 */
public class Ship implements Cloneable {
	/**
	 * The length of this ship.
	 */
	private int length;
	/**
	 * Whether or not this ship is sunk.
	 */
	private boolean isSunk;
	/**
	 * A char[] representation of the ship, represented by 'o'.
	 */
	private char[] rep;
	/**
	 * The name of the ship. Good for debugging.
	 */
	private String name;
	/**
	 * How many hits the ship has taken. If it equals length, we know we have
	 * a sunken ship.
	 */
	private int numOfHits = 0;
	/**
	 * Points mapping to Ship objects for easy hit retrieval.
	 */
	private static Map<Integer, Map<Integer, Ship>> points = new HashMap<>();
	
	
	/**
	 * The constructor. Give it a length and a name, and you're in business. 
	 * 
	 * 
	 * @param length how long the ship is
	 * @param name the name of the ship for debugging purposes
	 */
	public Ship(int length, String name) {
		this.length = length;
		this.name = name;
		isSunk = false;
	}

	/**
	 * Set the length.
	 * 
	 * Set the length method. Good for when you want to clone it.
	 * 
	 * @param length
	 */
	public void setLength(int length) {
		this.length = length;
	}
	
	/**
	 * Set the ship name.
	 * 
	 * Set the name of the ship.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the ship's length.
	 * 
	 * Get the length of the ship.
	 * 
	 * @return length the length of the ship
	 */
	public int getLength() {
		return length;
	}
	
	
	
	/**
	 * Add a point that is associated with this Ship.
	 * 
	 * A point, what will be associated with this Ship, so when a hit is made,
	 * we know which Ship to update. It also goes into the master list of points
	 * to ships.
	 * 
	 * @param point
	 */
	public void addPoint(Point point) {
		//System.out.println("Ship.addPoint" + point + " Ship: " + this);
		int outerArray = point.y;
		int innerArray = point.x;
		if (points.containsKey(innerArray)) {
			 Map<Integer, Ship> innerMap = points.get(innerArray);
			 innerMap.put(outerArray, this);
		} else {
			Map<Integer, Ship> innerMap = new HashMap<>();
			innerMap.put(outerArray, this);
			points.put(innerArray, innerMap);
		}
	}
	
	/**
	 * Gets the Ship at that point.
	 * 
	 * Retrieves the point that was made by the addPoint() method, so you know
	 * which Ship was hit. 
	 * 
	 * @param point the point that you want to get the ship at
	 * @return the Ship object that is at that point
	 */
	public static Ship getShipAt(Point point) {
		int outerArray = point.y;
		int innerArray = point.x;
		return points.get(innerArray).get(outerArray);
	}
	
	/**
	 * Sets a char[] representation for the ship.
	 * 
	 * Set the representation for this ship as a series of 'o' chars.
	 */
	public void setRep() {
		rep = new char[length];
		for (int i = 0; i < length; i++) {
			rep[i] = 'o';
		}
	}
	
	/**
	 * The ship has been hit so we increment number of hits, check if it's sunk.
	 * 
	 * The ship has been hit, so we're going to increment the number of hits
	 * and then check if it's sunk.
	 */
	public void setHit() {
		System.out.println("Ship.setHit. Ship: " + this);
		numOfHits++;
		System.out.println("Num of Hits: " + numOfHits);
		checkSunk();
	}
	
	/**
	 * Check if the ship is sunk or not.
	 * 
	 * If the number of hits == length, we know the ship is sunk. Otherwise, it's
	 * clearly not.
	 */
	public void checkSunk() {
		isSunk = (numOfHits == length) ? true : false;
	}
	
	/**
	 * Get the isSunk field.
	 * 
	 * A simple getter for the isSunk field.
	 * 
	 * @return isSunk which is true if the ship has been sunk, false if not.
	 */
	public boolean isSunk() {
		return isSunk;
		
	}
	
	/**
	 * Prints a convenient string representation of this Ship.
	 * 
	 * A convenient toString() override that displays the ship's name, its length,
	 * and how many hits it has sustained.
	 *
	 */
	@Override
	public String toString() {
		return String.format("Ship name: %s, Ship length: %d, num of hits: %d", name, length, numOfHits);
	}
	
	/**
	 * Overrides clone() of Object.
	 * 
	 * A simple override of the Object clone() method.
	 *
	 * @return Object which should be a ship
	 * @throws CloneNotSupportedException
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
