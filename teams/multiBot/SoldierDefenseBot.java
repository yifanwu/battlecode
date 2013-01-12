package multiBot;

import java.util.Arrays;

import battlecode.common.*;

public class SoldierDefenseBot extends BaseBot {
	
	public static final int DEFENSE_RANGE = 36; 
	public static final int HOME_RANGE = 49;
	public static final int HOME_SPACE = 9;
	public static final int MIN_ROUNDS_BEFORE_MINE = 100;
	
	public SoldierDefenseBot (RobotController rc) {
		super(rc);
	}
	
	public void run() throws GameActionException {
		
		MapLocation nearestEnemyBot = findClosestEnemyRobot();
		
		if (nearestEnemyBot != null && nearestEnemyBot.distanceSquaredTo(homeHQ) < DEFENSE_RANGE) {
			defenseAttack(nearestEnemyBot);
		} else if (myLoc.distanceSquaredTo(homeHQ) > HOME_RANGE) {
			defenseGoHome();
		} else {
			layDefenseMines();
		}
	}
	
	private void layDefenseMines() throws GameActionException {
		// TODO figure out a way to communicate
		MapLocation[] ourMines = rc.senseMineLocations(super.myLoc, HOME_RANGE, rc.getTeam());
		boolean isInPosition = (2*super.myLoc.x+super.myLoc.y)%5 == 0;
		boolean isNotLaid = !Arrays.asList(ourMines).contains(super.myLoc);
		boolean isNotTooClose = !(super.myLoc.distanceSquaredTo(homeHQ) < HOME_SPACE);  
		boolean isNotTooSoon = !(Clock.getRoundNum() < MIN_ROUNDS_BEFORE_MINE);
		if (isInPosition && isNotLaid && isNotTooClose && isNotTooSoon) {
			rc.layMine();		
			for(int i=0; i<200;i++) {
				System.out.println("Mine laid!!!");
			}
		} else {
			defenseGoHome();
		}
	}

	public void defenseAttack(MapLocation enemyBot) throws GameActionException {
		Direction dir = availableDirection(enemyBot);
		//TODO: add sophistication
		if (rc.canMove(dir)) {
			rc.move(dir);
		}
	}
	
	public void defenseGoHome() throws GameActionException {
		Direction dirEnemy = homeHQ.directionTo(enemyHQ);		
		Direction dir = availableDirection(homeHQ.add(dirEnemy, DEFENSE_RANGE));
		if (rc.canMove(dir)) {
			rc.move(dir);
		}
	}
	
}
