package multiBot;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.*;

import battlecode.common.*;

public class HQBot extends BaseBot{

	private static final int MAX_SOLDIERS = 10000;
	
	private static int totalSoldiers = 0;
	private static MapLocation enemyHQ;
	private static MapLocation homeHQ;
	private static ArrayList<MapLocation> mines;
	//private static double halfDistBetweenHQ;
	
	public HQBot(RobotController rc) {
		super(rc);
		//code to execute one time
		mines = new ArrayList<MapLocation>();
		enemyHQ = rc.senseEnemyHQLocation();
		homeHQ = rc.senseHQLocation();
		//halfDistBetweenHQ = DistBetweenHQ() / 4;
	}
	
	public void run() throws GameActionException {
		//code to execute for the whole match
		
		//reserveChannelJam(); for testing
		
		if (rc.isActive()) {
			if (rc.getTeamPower() < 10) {
				if (!rc.hasUpgrade(Upgrade.FUSION)) {
					rc.researchUpgrade(Upgrade.FUSION);
				}
				else if (!rc.hasUpgrade(Upgrade.PICKAXE)) {
					rc.researchUpgrade(Upgrade.PICKAXE);
				}
				else rc.researchUpgrade(Upgrade.NUKE);
			}
			else {
				// Spawn a soldier
				Direction dir = getDirForSpawn(enemyHQ);
				if (rc.canMove(dir) && (totalSoldiers < MAX_SOLDIERS)) {
					rc.spawn(dir);
					totalSoldiers++;
				}
			}
		}
	
		updateMineLocations();
		
//		reserveChannelJam(); //jamming makes it so no information gets through

// test code for mine communication
/*
		if (Clock.getRoundNum() == 10) mineReport(rc.senseEnemyHQLocation());
		if (Clock.getRoundNum() == 12) mineReport(rc.senseHQLocation());
		if (Clock.getRoundNum() == 14) mineDefuseReport(rc.senseHQLocation());
		if (Clock.getRoundNum() == 16) mineDefuseReport(rc.senseEnemyHQLocation());
		
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
	
	//TODO: test consensus messaging
	//TODO: deal with channel hijacking
	private static void updateMineLocations() throws GameActionException {
		MapLocation loc = decodeLoc(rc.readBroadcast(MineReportChannel));
		if (loc != null) {
			if(!inMLArrayList(mines, loc)) { //add to list of mines if not in it
				mines.add(loc);
			}
			rc.broadcast(MineReportChannel, INVALID_CODE);
		}

		loc = decodeLoc(rc.readBroadcast(MineDefuseChannel));
		if (loc != null) {
			mines.remove(loc);
			rc.broadcast(MineDefuseChannel, INVALID_CODE);
		}
		
		//Deal with case where mineListenchannel has been hijacked?
		for(int channel: MineListenChannels) {
			rc.broadcast(channel, encodeMsg(mines.size()));
			for (int i=0;i<mines.size();i++) {
				rc.broadcast(channel + i + 1, encodeLoc(mines.get(i)));
			}
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
Window size: x 
Viewport size: x