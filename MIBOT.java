/*
 * Copyright (c) 2022 Marius Ionescu and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package MIBOT;

import robocode.*;
import robocode.RateControlRobot;
import java.awt.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.geom.Point2D;
import robocode.util.Utils;

/**
 * MIBot - a sample robot by Marius Ionescu.
 * <p>
 * This robot target enemy coordinates.
 *
 * @author Marius Ionescu (original)
 */
public class MIBOT extends AdvancedRobot {
	double predictPos = 20;
	int min = 0;
	int max = 20;
	int random_int;
    double powerBullet = 3; //bullet power
	double speedBullet; //bullet speed
	double oldHeadEnemy;
	double enemyEnergy;
	
	double direction = 1;
	double timeElapsed;

	/**
	 * run: main run function
	 */
	public void run() {
		// Set colors
		setBodyColor(Color.orange);
		setGunColor(Color.red);
		setRadarColor(Color.blue);
		setBulletColor(Color.white);
		setScanColor(Color.yellow);
		
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
	    speedBullet = predictPos - Math.PI / 2 * powerBullet;
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		random_int = (int)Math.floor(Math.random()*(max-min+1)+min);
	}
	
	/**
	 * onHitWall:  collision event with wall.
	 */
	public void onHitWall(HitWallEvent e) {
		changeDirection();
	}

	/**
	 * onScannedRobot:  predict and fire
	*/
	public void onScannedRobot(ScannedRobotEvent e) {
	    random_int = (int)Math.floor(Math.random()*(max-min+1)+min);
		double turnRobot = getABearing(e) + Math.PI / 2;
		turnRobot -= Math.max(0.5, e.getDistance() * random_int);

		if(enemyEnergy > e.getEnergy()){
			direction=-direction;
			setMaxTurnRate(turnRobot);
			setMaxVelocity(turnRobot);
			setTurnLeft(90 * direction);
		}	
	    enemyEnergy = e.getEnergy();	
		
		setTurnRightRadians(Utils.normalRelativeAngle(-getHeadingRadians()));	
		setMaxVelocity(100 / getTurnRemaining());
		setAhead(100 * direction);
		
		//Finding the heading and heading change.
		double enemyHead = e.getHeadingRadians();
		double enemyHeadChange = enemyHead - oldHeadEnemy;
		oldHeadEnemy = enemyHead;
		
		double pX = getX() + e.getDistance() * Math.sin(getABearing(e));
		double pY = getY() + e.getDistance() * Math.cos(getABearing(e));
		
		timeElapsed = 0;
		//while product timeEllapsed and speedBullet less than getEnemyDistance
		while((timeElapsed * speedBullet) <  getEnemyDistance(pX, pY)){	
			
			//predict enemy current X and Y
			pX += Math.sin(enemyHead) * e.getVelocity();
			pY += Math.cos(enemyHead) * e.getVelocity();
			
			//Enemy's heading changes
			enemyHead += enemyHeadChange;
			
			//predict pX & pY enemy with random
			pX = predictX(pX);
			pY = predictY(pY);
			timeElapsed++;
		}
		
		//Turn gun and fire
		setTurnGunRightRadians(Utils.normalRelativeAngle(predictAim(pX, pY) - getGunHeadingRadians()));
		setFire(powerBullet);
		setTurnRadarRightRadians(Utils.normalRelativeAngle(getABearing(e)-getRadarHeadingRadians())*2);	
	}

	/**
	 * onHitRobot: turn 180 and change direction!
	 */
	public void onHitRobot(HitRobotEvent e) {
		if (e.isMyFault()) {
			changeDirection();
		}
	}
	
	//predict pX with battleField and random
	public double predictX(double pX)
	{
		return Math.max(Math.min(pX, getBattleFieldWidth()), random_int);
	}
	
	//predict pY with battleField and random
	public double predictY(double pY)
	{
		return Math.max(Math.min(pY, getBattleFieldHeight()), random_int);
	}
	
	//get bearing enemy
	public double getABearing(ScannedRobotEvent e)
	{
		return e.getBearingRadians() + getHeadingRadians();
	}
	
	//get enemy distance
	public double getEnemyDistance(double pX, double pY)
	{
		return Point2D.Double.distance(getX(), getY(), pX, pY);
	}
	
	//predict target
	public double predictAim(double pX, double pY)
	{
		return Utils.normalAbsoluteAngle(Math.atan2(pX - getX(), pY - getY()));
	}

	//change direction
	public void changeDirection() {
		direction = -direction;
	}
}