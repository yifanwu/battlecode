package multiBot;

import battlecode.common.*;

public class GameConst {
	public int width;
	public MapLocation enemyHQ;
	public MapLocation homeHQ;
	// could add more!
	public GameConst (int width, MapLocation enemyHQ, MapLocation homeHQ) {
		this.width = width;
		this.enemyHQ = enemyHQ;
		this.homeHQ = homeHQ;
	}
}
