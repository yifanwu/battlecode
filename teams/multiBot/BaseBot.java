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
	protected static RobotController rc;
	protected MapLocation myLoc;
	protected GameConst GC;
	

	//Communication variables
	protected static int[] MineListenChannels = {5024, 6609, 9113};
	protected static int MineReportChannel = 2073;
	protected static int MineDefuseChannel = 2074;
	protected static int[] ReservedChannels =
		{MineListenChannels[0], MineListenChannels[1], MineListenChannels[2],
		MineReportChannel, MineDefuseChannel, MineListenChannels[0] +2};
	protected static final int ENCODING_PRIME = 24631;
	protected static final int INVALID_CODE = 0;
	
	//Jamming variables
	protected static int NumChannelGroups = 4;
	protected static int ChannelGroup = 0;
	protected static int NumSavedChannels = 25;
	protected static Queue<Integer> SavedChannels = new LinkedList<Integer>();
	protected static int NumJamMessages = 10;
	protected static Random RandomInt = new Random();
	
	public BaseBot(RobotController myRc, GameConst GC) {
		rc = myRc;
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
			//System.out.println("Yielding now");
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
	
	//finds the index of the item in arr that is closest to target
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
			//TODO: need canSense before using senseRobotInfo
			locArr[i] = rc.senseRobotInfo(robots[i]).location;
		}
		
		return nearestMapLocation(locArr, target);
	}
	
	//Gives a list of enemy mines
	//Returns an empty array if none
	//Add special value too
	//null values where garbage
	//TODO: add consensus value-checking for mines?
	protected static MapLocation[] mineListen() throws GameActionException {
		int[] numMines = new int[MineListenChannels.length];
		for(int j=0;j<MineListenChannels.length;j++) {
			numMines[j] = decodeMsg(rc.readBroadcast(MineListenChannels[j]));
		}
		MapLocation[] mines = new MapLocation[0];
		
		int maj = majority(numMines);
		if(maj >= 0) {
			mines = new MapLocation[numMines[maj]];
			for (int i=0;i<numMines[maj];i++) {
				mines[i] = decodeLoc(rc.readBroadcast(MineListenChannels[maj] + i + 1));
			}	
		}
		
		return mines;
	}

	//find index of majority value or return -1 if none
	private static int majority(int[] arr) {
		for(int i=0;i<arr.length;i++) {
			if(countValue(arr,arr[i]) > arr.length/2) {
				return i;
			}
		}
		return -1;
	}
	
	private static int countValue(int[] arr, int value) {
		int count = 0;
		for(int x: arr) {
			if (x==value)
				count++;
		}
		return count;
	}
	
	//Reports location of enemy mine in encoded form
	protected static void mineReport(MapLocation loc) throws GameActionException {
		rc.broadcast(MineReportChannel, encodeLoc(loc));
	}
	
	//Reports location of defused mine
	protected static void mineDefuseReport(MapLocation loc) throws GameActionException {
		rc.broadcast(MineDefuseChannel, encodeLoc(loc)); 
	}
	
	protected static int encodeMsg(int msg) {
		return msg*ENCODING_PRIME;
	}
	
	//INVALID_CODE means error
	protected static int decodeMsg(int msg) {
		if (msg % ENCODING_PRIME != 0 || msg == 0) return INVALID_CODE;
		else return msg/ENCODING_PRIME;
	}
	
	protected static int encodeLoc(MapLocation loc) {
		return encodeMsg(insecureEncodeLoc(loc));
	}
	
	//returns null if location is invalid
	protected static MapLocation decodeLoc(int msg) {
		return insecureDecodeLoc(decodeMsg(msg));
	}
	
	//encodes a location as an int
	//add 1 so 0 is not valid
	protected static int insecureEncodeLoc(MapLocation loc) {
		return (loc.x * 1000 + loc.y + 1); // assuming maximum is capped at 1000		
	}
	
	//returns null if the location is invalid
	protected static MapLocation insecureDecodeLoc (int msg) {
		msg--;
		int y = msg % 1000;
		int x = msg / 1000;
		if(y < 0 || y >= rc.getMapHeight()|| x < 0 || x >= rc.getMapWidth()) {
			return null;
		}
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
	
	
	//Jams reserved channels--for testing purposes
	protected static void reserveChannelJam() throws GameActionException {
		for(int channel: ReservedChannels) {
			singleChannelJam(channel);
		}
	}
	
	/* Offensive broadcasting strategy 
	 * Sweep a bunch of channels each turn
	 * Keep a record of the last NumSavedChannels channels that have been found with non-zero data
	 * Jam with various messages */
	
	//Jams all channels in SavedChannels
	protected static void channelJam() throws GameActionException {
		for(int channel: SavedChannels ) {
			singleChannelJam(channel);
			//System.out.println("Jamming channel " + channel);
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
