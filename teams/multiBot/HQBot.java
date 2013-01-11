package multiBot;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import battlecode.common.*;

public class HQBot extends BaseBot{

	private static final int MAX_SOLDIERS = 10000;
	private static int NumChannelGroups = 4;
	private static int ChannelGroup = 0;
	private static int NumSavedChannels = 25;
	private static Queue<Integer> SavedChannels = new LinkedList<Integer>();
	private static int NumJamMessages = 10;
	private static Random RandomInt = new Random();
	private static int totalSoldiers = 0;
	private static MapLocation enemyHQ;
	private static MapLocation homeHQ;
	//private static double halfDistBetweenHQ;
	
	public HQBot(RobotController rc, GameConst GC) {
		super(rc, GC);
		//code to execute one time
		NumChannelGroups = (int)(3 + GameConstants.BROADCAST_READ_COST);
		NumSavedChannels =
			(int)(Math.min(GameConstants.BROADCAST_MAX_CHANNELS/(GameConstants.BROADCAST_SEND_COST), 50));
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
		
		channelSweep();
		//channelJam();
		
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
	
	//Sweep a portion of the open channels, looking for ones that are in use, adding them to SavedChannels
	private static void channelSweep() throws GameActionException {
		System.out.println("Scanning between " +
				(int)(GameConstants.BROADCAST_MAX_CHANNELS*((double)(ChannelGroup)/NumChannelGroups)) + " and " +
						GameConstants.BROADCAST_MAX_CHANNELS*((double)(ChannelGroup + 1)/NumChannelGroups));
		for(int i=(int)(GameConstants.BROADCAST_MAX_CHANNELS*((double)(ChannelGroup)/NumChannelGroups));
			i<=GameConstants.BROADCAST_MAX_CHANNELS*((double)(ChannelGroup + 1)/NumChannelGroups);
			i++) {
			if(isReservedChannel(i))
				continue;
			if (rc.readBroadcast(i) != 0) {
				SavedChannels.add(i);
			}

			while (SavedChannels.size() > NumSavedChannels) {
				SavedChannels.remove();
			}
		}
		
		if(++ChannelGroup >= NumChannelGroups)
			ChannelGroup = 0;
		System.out.println("Number of channel groups: " + NumChannelGroups);
	}
	
	
	/* Offensive broadcasting strategy 
	 * Sweep a bunch of channels each turn
	 * Keep a record of the last NumSavedChannels channels that have been found with non-zero data
	 * Jam with various messages */
	
	//Jams all channels in SavedChannels
	private static void channelJam() throws GameActionException {
		for(int channel: SavedChannels ) {
			singleChannelJam(channel);
			System.out.println("Jamming channel " + channel);
		}
	}
	
	//Jams a single channel with a variety of messages
	//Good for testing robustness to enemy jamming (i.e. use it on reserved channels)
	private static void singleChannelJam(int channel) throws GameActionException {
		int x = Clock.getRoundNum() % NumJamMessages;
		int orig = rc.readBroadcast(channel);
		if(x == 0) {
			rc.broadcast(channel, -1);
		}
		else if (x == 1) {
			rc.broadcast(channel, orig-1);
		}
		else if (x == 2) {
			rc.broadcast(channel, orig+1);	
		}
		else if (x == 3) {
			rc.broadcast(channel, 0);
		}
		else if (x == 4) {
			rc.broadcast(channel, Integer.MAX_VALUE);
		}
		else if (x == 5) {
			rc.broadcast(channel, Integer.MIN_VALUE);
		}
		else if (x == 6) {
			rc.broadcast(channel, orig-5);
		}
		else if (x == 7) {
			rc.broadcast(channel, orig+5);
		}
		else { //weighted extra
			rc.broadcast(channel, (int)(RandomInt.nextInt()));
		}	
	}
	
	//Checks if channel c is a reserved channel
	private static boolean isReservedChannel(int c) {
		for(int x: ReservedChannels) {
			if(c == x)
				return true;
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
