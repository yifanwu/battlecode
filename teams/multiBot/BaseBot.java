package multiBot;

import static multiBot.BaseBot.ChannelGroup;
import static multiBot.BaseBot.NumChannelGroups;
import static multiBot.BaseBot.NumJamMessages;
import static multiBot.BaseBot.NumSavedChannels;
import static multiBot.BaseBot.RandomInt;
import static multiBot.BaseBot.SavedChannels;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import battlecode.common.*;

public abstract class BaseBot {
	protected final boolean VERBOSE = true; 
	private static final int MAX_SQUARE_RADIUS = 10000;
	static protected RobotController rc;
	protected MapLocation myLoc;
	protected GameConst GC;
	

	//Communication variables
	protected static int MineListenChannel = 5024;
	protected static int MineReportChannel = 2073;
	protected static int MineDefuseChannel = 2074;
	protected static int[] ReservedChannels = {MineListenChannel, MineReportChannel, MineDefuseChannel};
	
	//Jamming variables
	protected static int NumChannelGroups = 4;
	protected static int ChannelGroup = 0;
	protected static int NumSavedChannels = 25;
	protected static Queue<Integer> SavedChannels = new LinkedList<Integer>();
	protected static int NumJamMessages = 10;
	protected static Random RandomInt = new Random();
	
	public BaseBot(RobotController rc, GameConst GC) {
		rc = rc;
		this.myLoc = rc.getLocation();
		this.GC = GC;
		NumChannelGroups = (int)(100 + GameConstants.BROADCAST_READ_COST);
		NumSavedChannels =
			(int)(Math.min(GameConstants.BROADCAST_MAX_CHANNELS/(GameConstants.BROADCAST_SEND_COST), 10));
	}
	
	public abstract void run() throws GameActionException;
	
	public void loop() {
		while(true) {
			try {
				// Execute turn
				run();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			// End turn
			System.out.println("Yielding now");
			rc.yield();
		}
	}
	
	protected Direction availableDirection(MapLocation destination) {
		Direction lookingAtCurrently = null;
		
		int dist = rc.getLocation().distanceSquaredTo(destination);
        if (dist > 0 && rc.isActive()) {
            Direction dir = rc.getLocation().directionTo(destination);
            int[] directionOffsets = {0,1,-1,2,-2};
            lookingAtCurrently = dir;
            for (int d : directionOffsets){
                lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
                
                if(rc.canMove(lookingAtCurrently)){
                    break;
                }
            }
        }
        
        return lookingAtCurrently;
	}
	
	protected MapLocation findClosestEnemyEncampment() throws GameActionException {
		MapLocation[] nearbyEncampments = rc.senseEncampmentSquares(myLoc, MAX_SQUARE_RADIUS, Team.NEUTRAL);        
        MapLocation closestEncampment = nearestMapLocation(nearbyEncampments, myLoc);
        
        return closestEncampment;
    }
	
	protected MapLocation findClosestEnemyRobot() throws GameActionException {
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,MAX_SQUARE_RADIUS, rc.getTeam().opponent());        
        MapLocation closestEnemy = nearestBotLocation(enemyRobots, myLoc);
        
        return closestEnemy;
    }
	
	protected MapLocation nearestMapLocation(MapLocation arr[], MapLocation target) {
		int best = -1;
		int bestDist = -1;
		for (int i=0;i<arr.length;i++) {
			int dist = arr[i].distanceSquaredTo(target);
			if ((bestDist == -1) || (bestDist > dist) || (bestDist == dist && arr[best].x > arr[i].x) ||
					(bestDist == dist && arr[best].x == arr[i].x && arr[best].y > arr[i].y)) {
				bestDist = dist;
				best = i;
			}
		}
		if (best == -1) {
			return null;
		} else {
			return arr[best];
		}
	}
	
	protected MapLocation nearestBotLocation(Robot robots[], MapLocation target) throws GameActionException {
		MapLocation[] locArr = new MapLocation[robots.length];  
		
		for (int i = 0; i<robots.length; i++) {
			locArr[i] = rc.senseRobotInfo(robots[i]).location;
		}
		
		return nearestMapLocation(locArr, target);
	}
	
	//Gives a list of enemy mines
	protected static MapLocation[] mineListen() throws GameActionException {
		int numMines = rc.readBroadcast(MineListenChannel);
		MapLocation[] mines = new MapLocation[numMines];
		for (int i=0;i<numMines;i++) {
			mines[i] = decodeLoc(rc.readBroadcast(MineListenChannel + i + 1));
		}
		return mines;
	}
	
	//Reports location of enemy mine
	protected static void mineReport(MapLocation loc) throws GameActionException {
		rc.broadcast(MineReportChannel, encodeLoc(loc));
	}
	
	//Reports location of defused mine
	protected static void mineDefuseReport(MapLocation loc) throws GameActionException {
		rc.broadcast(MineDefuseChannel, encodeLoc(loc)); 
	}
	
	protected static int encodeLoc(MapLocation loc) {
		return (loc.x * 1000 + loc.y); // assuming maximum is capped at 1000		
	}
	
	protected static MapLocation decodeLoc (int msg) {
		int y = msg % 1000;
		int x = msg/1000;
		MapLocation result = new MapLocation(x,y);
		return result;
	}
	
	protected static void sweepAndJam() throws GameActionException {
		channelSweep();
		channelJam();
	}
	
	//Sweep a portion of the open channels, looking for ones that are in use, adding them to SavedChannels
	protected static void channelSweep() throws GameActionException {
		for(int i=(int)(GameConstants.BROADCAST_MAX_CHANNELS*((double)(ChannelGroup)/NumChannelGroups));
			i<=GameConstants.BROADCAST_MAX_CHANNELS*((double)(ChannelGroup + 1)/NumChannelGroups);
			i++) {
			if(isReservedChannel(i))
				continue;
			if (rc.readBroadcast(i) != 0) {
				SavedChannels.add(i);
			}
		}

		while (SavedChannels.size() > NumSavedChannels) {
			SavedChannels.remove();
		}
		
		if(++ChannelGroup >= NumChannelGroups)
			ChannelGroup = 0;
	}
	
	/* Offensive broadcasting strategy 
	 * Sweep a bunch of channels each turn
	 * Keep a record of the last NumSavedChannels channels that have been found with non-zero data
	 * Jam with various messages */
	
	//Jams all channels in SavedChannels
	protected static void channelJam() throws GameActionException {
		for(int channel: SavedChannels ) {
			singleChannelJam(channel);
			System.out.println("Jamming channel " + channel);
		}
	}
	
	//Jams a single channel with a variety of messages
	//Good for testing robustness to enemy jamming (i.e. use it on reserved channels)
	protected static void singleChannelJam(int channel) throws GameActionException {
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
			rc.broadcast(channel, RandomInt.nextInt());
		}	
	}
	
	//Checks if channel c is a reserved channel
	protected static boolean isReservedChannel(int c) {
		for(int x: ReservedChannels) {
			if(c == x)
				return true;
		}
		return false;
	}
	
	protected static void Log(String msg)
	{
		System.out.println("Turn " + Clock.getRoundNum() + ": " + msg);
	}	
	
}
