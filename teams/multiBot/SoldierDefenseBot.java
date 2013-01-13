package multiBot;

import java.util.Arrays;

import battlecode.common.*;

public class SoldierDefenseBot extends BaseBot {
	
	protected static int defenseRange = 36; 
	protected static int homeRange = 100;
	protected static final int HOME_SPACE = 9;
	protected static final int MIN_ROUNDS_BEFORE_MINE = 50;


	public SoldierDefenseBot (RobotController rc) { //code for initializing
		super(rc);
		homeRange = (rc.getMapWidth()+rc.getMapHeight())/2;
		defenseRange = homeRange - 2;
	}
	
	public void run() throws GameActionException {
		if(rc.isActive()) {
			MapLocation nearestEnemyBot = findClosestEnemyRobot();
			
			if (nearestEnemyBot != null && nearestEnemyBot.distanceSquaredTo(homeHQ) < defenseRange) {
				defenseAttack(nearestEnemyBot); //attack enemies if nearby
			} else if (myLoc.distanceSquaredTo(homeHQ) > homeRange) {
				//moveToLocAndDefuseMine(homeHQ); //return home
				rc.layMine();
			} else {
				if (VERBOSE) {
					System.out.println("laying mine");
				}
				//layDefenseMines();
				rc.layMine();
			}
		}
	}
	
	private void layDefenseMines() throws GameActionException {
		// TODO figure out a way to communicate
		boolean isInPosition = true;
		//boolean isInPosition = (myLoc.y+2*(myLoc.x%2) )%4 == 0; //(2*myLoc.x+myLoc.y)%5 == 0;
		
		if (Clock.getRoundNum() > MIN_ROUNDS_BEFORE_MINE && isInPosition && (rc.senseMine(myLoc) == null)) {
			rc.layMine();
		} else {
			defensePatrol();
		}
	}

	public void defenseAttack(MapLocation enemyBot) throws GameActionException {
		Direction dir = availableDirection(enemyBot);
		//TODO: add sophistication
		moveToLocAndDefuseMine(myLoc.add(dir, 1));
	}
	
	protected void defensePatrol() throws GameActionException {
		//TODO: implement smarter mining if desired
		
        Direction dir = Direction.values()[(int)(Math.random()*8)];
        moveToLocAndDefuseMine(myLoc.add(dir,1));
        
		/*
		MapLocation[] nearbyMines = rc.senseMineLocations(myLoc, 1, rc.getTeam());
		MapLocation dest;
		if(nearbyMines.length == 0) {
			if(myLoc.y % 3 == 1) ; // go to lower y
		}
		else if (nearbyMines.length == 1) {
			if (nearbyMines[0].x == myLoc.x);
		}
		else if (nearbyMines.length == 2) { //todo: fix
			MapLocation wrongSide = nearbyMines[0].add(nearbyMines[0].directionTo(nearbyMines[1]), 1);
			dest = wrongSide.add(wrongSide.directionTo(myLoc), 1);
		}
		else { //area is all mined up
			dest = enemyHQ; 
		}
*/
		//moveToLocAndDefuseMine(dest);
	}
	
		
}
