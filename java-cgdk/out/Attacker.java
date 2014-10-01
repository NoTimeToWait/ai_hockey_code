package model;
import HeadQuarters;
import Point;
import Role;
import Status;
import model.*;


public class Attacker implements Role{
	

    private static final double STRIKE_ANGLE = 1.0D * Math.PI / 180.0D;
    
    public static double ATTACK_ZONE_RADIUS = 175; //half of net height
    public static double SPEED_ZONE_RADIUS = 150;
    public static double ACCURACY = 1.7; // higher number - more accuracy. Should be >1
    public static double FORCE = 15; // swing orce before shoot puck/ Should be between 0 and 20(not recommended), where 0 - shoot puck without swing
	public double centerY;
	
	private Hockeyist self;
	private World world;
	private Game game;
	private Move move;
	private Status status = Status.NONE;
	private Player ownside;
	private Player opponent;
	private Hockeyist[] guys;
	private Hockeyist enemyGoalie;
	//from where to shoot (circle area around two points)
	private double shootPosX;
	private double shootPosX2;
	private double shootPosY1;
	private double shootPosY2;
	private double shootPosY3;
	//where to shoot
	private double attackX;
	private double attackY;
	//enemy net coord
	private double netX;
	private double netY;
/*
	public void act(Hockeyist self, World world, Game game, Move move) {
		HeadQuarters.update(this, world, game, move, self);
		double Y = 230;
		if (world.getPuck().getOwnerHockeyistId() == self.getId()) {
            	double angleToNet = self.getAngleTo(900, Y);
            	double dif = self.getY() - 150;
            	double angleToShoot = self.getAngleTo(self.getX()-dif*3, 150);
            	if (self.getDistanceTo(900, Y)>1) {
            		move.setTurn(angleToNet);
                	move.setSpeedUp(0.1D*self.getDistanceTo(900, Y)/150);
            	}
            	else {
            		move.setTurn(angleToShoot);
            	}
            	if (Math.abs(angleToShoot) < 0.005 && self.getSpeedX()<0.01) {
            		
            		System.out.println("----------"+(self.getX()-dif));
            		
            		move.setAction(ActionType.STRIKE);
            		HeadQuarters.updateTime(world);
            	}
            	
            	
        }
		else {
            move.setSpeedUp(1.0D);
            move.setTurn(self.getAngleTo(world.getPuck()));
            move.setAction(ActionType.TAKE_PUCK);
		}
	}
	*/
	public String getType() {
		return "Attacker";
	}
	
	public Status getStatus() {
		return status;
	}
	
	//right now not necessary since we have only 1 attacker on the field (no need to pass). But later with more hockeyists on the field
	//could possibly implement this
	public Point getPassDirection(){
		return new Point(shootPosX, shootPosY1);
	}
	
	public void act(Hockeyist self, World world, Game game, Move move) {
		
		
		this.self = self;
		this.world = world;
		this.game = game;
		this.move = move;
		ownside = world.getMyPlayer();
		opponent = world.getOpponentPlayer();
		centerY = (ownside.getNetBottom()+ownside.getNetTop())/2;
		guys = world.getHockeyists();
		
		getOffensiveXY();
        getShootingPositions();
        if (world.getPuck().getOwnerPlayerId() == opponent.getId()) {
        	moveToStandByPosition();
        }
		 if (world.getPuck().getOwnerHockeyistId() == self.getId()) {	            
	            
	            //if (!withinShootArea()&&(self.getLastAction()!=ActionType.SWING)) moveToShootingPosition();
	            if (!withinShootArea()&&world.getTick()<6000) moveToShootingPosition();
	            else {
	            	double angleToNet = self.getAngleTo(attackX, attackY);
	            	move.setTurn(angleToNet);
	            
	            	if (Math.abs(angleToNet) < STRIKE_ANGLE/ACCURACY) {
	            		move.setAction(ActionType.SWING);
	            	}
	            
	            	if (self.getSwingTicks()>18)
	            		move.setAction(ActionType.STRIKE);
	            
	            	if ((Math.abs(angleToNet) < STRIKE_ANGLE/ACCURACY)&&(self.getSwingTicks()>FORCE)) {
	            		move.setAction(ActionType.STRIKE);
	            	}
	            }
	        }
		 else if (world.getPuck().getOwnerPlayerId() == ownside.getId()) {
			 getReadyForPass();
		 }
		 else pickUpPuck(); 
		 
		 //swing "freezing" fix
		 if(self.getLastAction() == ActionType.SWING && world.getPuck().getOwnerHockeyistId() != self.getId()) move.setAction(ActionType.CANCEL_STRIKE);
		 HeadQuarters.update(this, world, game, move);
		
	}
	
	private boolean withinShootArea() {
		if (self.getDistanceTo(shootPosX, shootPosY1)<=ATTACK_ZONE_RADIUS) return true;
		if (self.getDistanceTo(shootPosX, shootPosY2)<=ATTACK_ZONE_RADIUS) return true;
		return false;
	}
	
	private void pickUpPuck() {
		if (world.getPuck().getOwnerPlayerId() != ownside.getId() && 
				 world.getPuck().getOwnerPlayerId() != opponent.getId() &&
				 world.getPuck().getSpeedX()<2 &&
				 isPuckInProximity()){
	            move.setSpeedUp(1.0D);
	            move.setTurn(self.getAngleTo(world.getPuck()));
	            move.setAction(ActionType.TAKE_PUCK);
	     }	  
		else moveToStandByPosition();
	}
	
	private boolean isPuckInProximity() {
		for (int i=0; i<guys.length; i++) {
			if (/*!guys[i].isTeammate() &&*/ guys[i].getType()!=HockeyistType.GOALIE && 
				world.getPuck().getDistanceTo(guys[i])<world.getPuck().getDistanceTo(self)+self.getRadius()*3)
					return false;
		}
		return true;
	}
	
	private void getReadyForPass() {
		getShootingPositions();
		Role availableDefender = HeadQuarters.askDefenders(Status.PASS_AVAILABLE); 
		if (availableDefender == null) {
			moveToStandByPosition();
			return;
		}
		else {
			moveToPassReadyPosition(availableDefender);
		}
			
	}
	/*
	private boolean isEnemyInUpperPart() {
		double totalHeightSum = 0;
		for (int i=0; i<guys.length; i++) 
			if (!guys[i].isTeammate()&&guys[i].getType()!=HockeyistType.GOALIE)  totalHeightSum += guys[i].getY() - centerY; 
		if(totalHeightSum<0) return true;
		return false;
	}
	*/
	private void handleIncomingPuck() {
		if ((self.getDistanceTo(world.getPuck())<game.getStickLength())
				&&(self.getAngleTo(world.getPuck())<game.getStickSector()/2))
				move.setAction(ActionType.TAKE_PUCK);
	}
	
	private void getShootingPositions() {
		shootPosX = world.getWidth()/2 + (opponent.getNetFront() - world.getWidth()/2)/2; //(world.getWidth()/2 + opponent.getNetFront())/2;
		shootPosY1 =  centerY - 200;
		shootPosY2 =  centerY + 200;
		
		//if (world.getTick()%10==0) System.out.println(shootPosX);
		
		
	}
	
	private void moveToStandByPosition() {
		double standByPosX = world.getWidth()/2 + (opponent.getNetFront() - world.getWidth()/2)/2.5;
		if (self.getDistanceTo(standByPosX, centerY)>SPEED_ZONE_RADIUS) {
				double distScaleSpeed = self.getDistanceTo(standByPosX, centerY)/(SPEED_ZONE_RADIUS*2);
				move.setSpeedUp(distScaleSpeed);
				move.setTurn(self.getAngleTo(standByPosX, centerY));
				
		}
		if (self.getDistanceTo(world.getPuck())<game.getStickLength()*1.5) {
			move.setTurn(self.getAngleTo(world.getPuck()));
		}
		if (self.getDistanceTo(world.getPuck())<game.getStickLength()
				&&(self.getAngleTo(world.getPuck())<game.getStickSector()/2))
				move.setAction(ActionType.TAKE_PUCK);
		
		else {
			move.setTurn(self.getAngleTo(world.getPuck()));
			status = Status.STAND_BY;
		}
	}
	
	private void moveToPassReadyPosition(Role defender) {
		Point passDirection = defender.getPassDirection();
		double readyPosX = world.getWidth()/2 + (opponent.getNetFront() - world.getWidth()/2)/4;
		double readyPosY1 = centerY - 300;
		double readyPosY2 = centerY + 300;
		//TODO change to exact
		if (passDirection.getY()<100) {
			if (self.getDistanceTo(readyPosX, readyPosY1)>SPEED_ZONE_RADIUS) {
				double distScaleSpeed = self.getDistanceTo(readyPosX, readyPosY1)/(SPEED_ZONE_RADIUS);
				move.setSpeedUp(distScaleSpeed*distScaleSpeed);
				move.setTurn(self.getAngleTo(readyPosX, readyPosY1));
			}
			else {
				move.setTurn(self.getAngleTo(world.getPuck()));
				if (self.getAngleTo(world.getPuck())<STRIKE_ANGLE && self.getDistanceTo(world.getPuck())>game.getStickLength()) 
						move.setSpeedUp(1.0D);
				handleIncomingPuck();
			}
		}
		else {
			if (self.getDistanceTo(readyPosX, readyPosY2)>SPEED_ZONE_RADIUS) {
				double distScaleSpeed = self.getDistanceTo(readyPosX, readyPosY2)/(SPEED_ZONE_RADIUS);
				move.setSpeedUp(distScaleSpeed*distScaleSpeed);
				move.setTurn(self.getAngleTo(readyPosX, readyPosY2));
			}
			else {
				move.setTurn(self.getAngleTo(world.getPuck()));
				if (self.getAngleTo(world.getPuck())<STRIKE_ANGLE && self.getDistanceTo(world.getPuck())>game.getStickLength()) 
						move.setSpeedUp(0.1D);
				handleIncomingPuck();
			}
		}
	}
	
	private void moveToShootingPosition() {
		double newAttackRadius = ATTACK_ZONE_RADIUS+25;
		if (self.getDistanceTo(shootPosX, shootPosY1)<self.getDistanceTo(shootPosX, shootPosY2)) {
			if (self.getDistanceTo(shootPosX, shootPosY1)>newAttackRadius) {
					double distScaleSpeed = self.getDistanceTo(shootPosX, shootPosY1)/(newAttackRadius*2);
					move.setSpeedUp(distScaleSpeed);
					move.setTurn(self.getAngleTo(shootPosX, shootPosY1));
			}
			else move.setTurn(self.getAngleTo(attackX, attackY));
		}
		else {
			if (self.getDistanceTo(shootPosX, shootPosY2)>newAttackRadius) {
				double distScaleSpeed = self.getDistanceTo(shootPosX, shootPosY2)/(newAttackRadius*2);
				move.setSpeedUp(distScaleSpeed);
				move.setTurn(self.getAngleTo(shootPosX, shootPosY2));
			}
			else move.setTurn(self.getAngleTo(attackX, attackY));
		}
		
	}
	
	//find best point to shoot
	private void getOffensiveXY(){
		

        netX = 0.5D * (opponent.getNetBack() + opponent.getNetFront());
        netY = 0.5D * (opponent.getNetBottom() + opponent.getNetTop());
        
        attackX = netX;
		//look for our goalie
				for (int i=0; i<guys.length; i++) {
					if ((guys[i].getType() == HockeyistType.GOALIE) && !guys[i].isTeammate()) {
						this.enemyGoalie = guys[i];
						break;
					}
				}
		if (enemyGoalie==null) {
			attackY = netY;
			return;
		}
				
		if (Math.abs(opponent.getNetTop() - enemyGoalie.getY())>=Math.abs(opponent.getNetBottom() - enemyGoalie.getY())) {
			attackY = opponent.getNetTop()-25;
		}
		else attackY = opponent.getNetBottom()+25;
		
		//if (world.getTick()>6000) attackY = netY;	
	
	}
	
	public Hockeyist getHockeyist() {
		return self;
	}
}