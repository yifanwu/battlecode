package multiBot;
import java.util.*;

import battlecode.common.*;

public class HQBot extends BaseBot{

	private static final int MAX_SOLDIERS = 10000;

	private static final int NEUTRAL_ENCAMPMENT_CHANNEL = 3;

	private static int totalSoldiers = 0;
	private static MapLocation enemyHQ;
	private static MapLocation homeHQ;
	private static ArrayList<MapLocation> mines;
	//private static double halfDistBetweenHQ;
	private static HashMap<MapLocation, EncampmentStatus> encampments; 

	
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
			if (Clock.getRoundNum() == 1) {
				senseAllEncampments();
				sendRobotToNeutralEncampment();
			}

			//System.out.println(GameConstants.UNIT_ENERGON_UPKEEP);
			if (rc.getTeamPower() < 10) {
                rc.setIndicatorString(0, "Researching!");
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
	
	//TODO: assignEncampment jobs
	protected static void assignEncampmentJobs() {
		MapLocation locs[] = rc.senseAllEncampmentSquares();
		int count = 0;
		while(count < locs.length/2) {
			//find lowest unassigned
			count++;
		}		
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
	
	private static void senseAllEncampments() {
		MapLocation[] allEncampments = rc.senseAllEncampmentSquares();
		for (MapLocation m : allEncampments) {
			encampments.put(m, EncampmentStatus.NEUTRAL);
		}
	}
	

	private static void updateAlliedEncampmentLocations() {
		MapLocation[] alliedEncampments = rc.senseAlliedEncampmentSquares();
		
		for (MapLocation m : alliedEncampments) {
			encampments.put(m, EncampmentStatus.ALLY);
		}
	}
	
	private void sendRobotToNeutralEncampment() throws GameActionException {
		for (MapLocation m : encampments.keySet()) {
			if (encampments.get(m) == EncampmentStatus.NEUTRAL) {
				int encodedLocation = super.encodeLoc(m);
				rc.broadcast(NEUTRAL_ENCAMPMENT_CHANNEL + Clock.getRoundNum() + 1, encodedLocation);
				encampments.put(m, EncampmentStatus.DISPATCHED);
				break;
 			}
		}
	}

}