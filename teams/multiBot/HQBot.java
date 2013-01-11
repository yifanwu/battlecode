package multiBot;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.*;

import battlecode.common.*;

public class HQBot extends BaseBot{

	private static final int MAX_SOLDIERS = 10000;
	private static final int CLEAR_CHANNEL = -1;
	
	private static int totalSoldiers = 0;
	private static MapLocation enemyHQ;
	private static MapLocation homeHQ;
	private static ArrayList<MapLocation> mines;
	//private static double halfDistBetweenHQ;
	
	public HQBot(RobotController rc, GameConst GC) {
		super(rc, GC);
		//code to execute one time
		mines = new ArrayList<MapLocation>();
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
		if (Clock.getRoundNum() == 10) mineReport(rc.senseEnemyHQLocation());
		if (Clock.getRoundNum() == 12) mineReport(rc.senseHQLocation());
		if (Clock.getRoundNum() == 14) mineDefuseReport(rc.senseHQLocation());
		if (Clock.getRoundNum() == 16) mineDefuseReport(rc.senseEnemyHQLocation());
	*/	
		updateMineLocations();
		
		/*
		System.out.println(mines.toString());
		
		MapLocation[] a = mineListen();
		if(a.length > 0) {
			System.out.println("Heard mine(s) at these locations:");
			for(int i=0;i<a.length;i++) {
				System.out.println(a[i].toString());
			}
		}
		*/
		
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
	
	private static void updateMineLocations() throws GameActionException {
		int newMine = rc.readBroadcast(MineReportChannel);
		if (newMine != CLEAR_CHANNEL) {
			MapLocation loc = decodeLoc(newMine);
			if(!inMLArrayList(mines, loc)) { //add to list of mines if not in it
				mines.add(loc);
			}
			rc.broadcast(MineReportChannel, CLEAR_CHANNEL);
		}
		
		int defusedMine = rc.readBroadcast(MineDefuseChannel);
		if (defusedMine != CLEAR_CHANNEL) {
			mines.remove(decodeLoc(defusedMine));
			rc.broadcast(MineDefuseChannel, CLEAR_CHANNEL);
		}
		
		rc.broadcast(MineListenChannel, mines.size());
		for (int i=0;i<mines.size();i++) {
			rc.broadcast(MineListenChannel + i + 1, encodeLoc(mines.get(i)));
		}
	}
	
	private static boolean inMLArrayList(ArrayList<MapLocation> L, MapLocation loc) {
		for(MapLocation x: L) {
			if(loc.equals(x)) return true;
		}
		return false;
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
