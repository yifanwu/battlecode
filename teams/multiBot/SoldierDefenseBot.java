package multiBot;

import java.util.Arrays;

import battlecode.common.*;

public class SoldierDefenseBot extends BaseBot {
	
	protected static int defenseRange = 36; 
	protected static int homeRange = 49;
	protected static final int HOME_SPACE = 9;
	protected static final int MIN_ROUNDS_BEFORE_MINE = 100;


	public SoldierDefenseBot (RobotController rc) { //code for initializing
		super(rc);
		homeRange = (int)Math.min(rc.getMapWidth(), rc.getMapHeight())/2;
		defenseRange = homeRange - 2;
	}
	
	public void run() throws GameActionException {
		if(rc.isActive()) {
			MapLocation nearestEnemyBot = findClosestEnemyRobot();
			
			if (nearestEnemyBot != null && nearestEnemyBot.distanceSquaredTo(homeHQ) < defenseRange) {
				defenseAttack(nearestEnemyBot);
			} else if (myLoc.distanceSquaredTo(homeHQ) > homeRange) {
				defenseGoHome();
			} else {
				layDefenseMines();
			}
		}
	}
	//TODO: defense should not die to neutral mines
	
	private void layDefenseMines() throws GameActionException {
		// TODO figure out a way to communicate

		boolean isInPosition = (myLoc.y % 3 == myLoc.x % 2); //(2*myLoc.x+myLoc.y)%5 == 0;
		
		if (Clock.getRoundNum() > MIN_ROUNDS_BEFORE_MINE && isInPosition && (rc.senseMine(myLoc) == null)) {
			rc.layMine();
		} else {
			defensePatrol();
		}
	}

	public void defenseAttack(MapLocation enemyBot) throws GameActionException {
		Direction dir = availableDirection(enemyBot);
		//TODO: add sophistication
		if (rc.canMove(dir)) {
			rc.move(dir);
		}
	}
	
	protected void defensePatrol() throws GameActionException {
		
		;
	}
	
	public void defenseGoHome() throws GameActionException {		
		Direction dir = availableDirection(homeHQ);
		if (rc.canMove(dir)) {
			rc.move(dir);
		}
	}
	
}
