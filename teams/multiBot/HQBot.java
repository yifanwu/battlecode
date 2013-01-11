package multiBot;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import battlecode.common.*;

public class HQBot extends BaseBot{

	private static final int MAX_SOLDIERS = 10000;

	private static int totalSoldiers = 0;
	private static MapLocation enemyHQ;
	private static MapLocation homeHQ;
	//private static double halfDistBetweenHQ;
	
	public HQBot(RobotController rc, GameConst GC) {
		super(rc, GC);
		//code to execute one time

		enemyHQ = rc.senseEnemyHQLocation();
		homeHQ = rc.senseHQLocation();
		//halfDistBetweenHQ = DistBetweenHQ() / 4;
	}
	
	public void run() throws GameActionException {
		//code to execute for the whole match
		//TODO: dummy right now
		if (rc.isActive()) {
			// Spawn a soldier
			Direction dir = getDirForSpawn(enemyHQ);
			if (rc.canMove(dir) && (totalSoldiers < MAX_SOLDIERS)) {
				rc.spawn(dir);
				totalSoldiers++;
			}
		}

		
		/*
		 * Broadcasting scheme
		 * 1: 
		 * 2: offense
		 
		isFarEnemyFound = false;
		int encoded = encodeLoc(closestEnemyNearHome);
		rc.broadcast(2, encoded);
		//rc.broadcast(1, ) TODO: messaging for defense
		//broadcast(data)
		// make sure that the hq is not blocked by yielding the robots in front //
		Robot[] blockingRobots = rc.senseNearbyGameObjects(Robot.class,2,rc.getTeam());
		System.out.println(blockingRobots.length);
		
		if (blockingRobots.length > 0) {
			
			for (Robot r:blockingRobots) {
				rc.broadcast(r.getID(), MOVEAWAY);
			}
		}
		*/	
	}
	

	private static Direction getDirForSpawn(MapLocation enemyHQ) {
		// randomize the direction
		double rand = Math.random();
		Direction dir = rc.getLocation().directionTo(enemyHQ);
		if (rand < 0.25) {
			dir = dir.rotateRight();
		} else if (rand < 0.5) {
			dir = dir.rotateLeft();							
		}
		return dir;
	}

	
}
