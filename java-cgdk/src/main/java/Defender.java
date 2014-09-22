
import model.*;

public class Defender implements Role {
	

    private static final double STRIKE_ANGLE = 1.0D * Math.PI / 180.0D;
	public static double DEFEND_ZONE_RADIUS = 100; //half of net height
	private static boolean CATCH_PUCK = true;
	public double centerY;
	
	private Hockeyist self;
	private World world;
	private Game game;
	private Move move;
	private Player ownside;
	private Player opponent;
	private Status status = Status.NONE;
	private Hockeyist[] guys;
	private Hockeyist ourGoalie;
	private Hockeyist ourBuddy;
	private double defendX;
	private double defendY;
		
	//coords of our net  center poing, where enemy goal will count
	private double goalDefX;
	private double goalDefY;
	
	public String getType() {
		return "Defender";
	}
	
	public Status getStatus() {
		return status;
	}
	
	//later with implemented Prediction model could possible return final puck "meeting" position
	//right now only general shooting direction
	public Point getPassDirection(){
		return calcPassDirection();
	}
	
	public void act(Hockeyist self, World world, Game game, Move move){
		this.self = self;
		this.world = world;
		this.game = game;
		this.move = move;
		ownside = world.getMyPlayer();
		opponent = world.getOpponentPlayer();
		centerY = (ownside.getNetBottom()+ownside.getNetTop())/2;
		
		goalDefX = ownside.getNetFront();
		goalDefY = centerY;
		
		guys = world.getHockeyists();
		
		//TODO add here catching puck if this guy in close proximity (will get there faster than others)
		moveToNet();
		if (world.getPuck().getOwnerHockeyistId() == self.getId()) handlePass();
		else handleIncomingPuck();
		
		if(self.getLastAction() == ActionType.SWING && world.getPuck().getOwnerHockeyistId() != self.getId()) move.setAction(ActionType.CANCEL_STRIKE);
		HeadQuarters.update(this, world, game, move);
	}
	
	private void moveToNet() {
		
		getDefensiveXY();
		move.setTurn(self.getAngleTo(world.getPuck()));

		
		double dist = self.getDistanceTo(defendX, defendY); //Math.hypot(defendX-self.getX(), defendY-self.getY());
		
		//short dist and in front of this guy
		if ((dist>self.getRadius()*2)&&(dist<=self.getRadius()*3)&&(self.getSpeedX()<1)
			&&(Math.abs(self.getAngleTo(defendX, defendY))<0.5)){
			
			double speedScaleAngle = 1.0-Math.abs(self.getAngleTo(defendX, defendY))/Math.PI;
			move.setSpeedUp(0.5*speedScaleAngle*speedScaleAngle);
	        move.setTurn(self.getAngleTo(defendX, defendY));
		}

		//if the guy is TOO far from defense point he needs to get there quickly
		if (dist>self.getRadius()*3) {
			double speedScaleAngle = 1.0-Math.abs(self.getAngleTo(defendX, defendY))/Math.PI;
			move.setSpeedUp(speedScaleAngle*speedScaleAngle);
	        move.setTurn(self.getAngleTo(defendX, defendY));
		}

		if ((dist>self.getRadius()/2) && (dist<self.getRadius()*6+30)
				&&(Math.abs(self.getAngleTo(defendX, defendY))>Math.PI-0.05)) {
				//if we find that defense point right at guy's back and he is quite far away from it
				//we slowly progress backwards
			        move.setTurn(self.getAngleTo(world.getPuck()));
					move.setSpeedUp(-1.0D);
				}
		
	}
	
	private void handlePass() {
		/* this has to be reworked by adding "line of sight" check
		for (int i=0; i<guys.length; i++) {
			if ((guys[i].getType() != HockeyistType.GOALIE) &&
					guys[i].isTeammate() && guys[i].getId()!=self.getId()) {
				this.ourBuddy = guys[i];
				break;
			}
		}
		double angleToNet = self.getAngleTo(ourBuddy);
    	move.setTurn(angleToNet);
    	if ((Math.abs(angleToNet) < STRIKE_ANGLE/2)) {
    		move.setAction(ActionType.STRIKE);
    	}*/
		status = Status.PASS_AVAILABLE;
		Point passPoint = calcPassDirection();
		double angleForPass = self.getAngleTo(passPoint.getX(), passPoint.getY());
		double angleToOwnNet = self.getAngleTo(goalDefX, goalDefY);
		move.setTurn(angleForPass);
		if ((Math.abs(angleForPass) < STRIKE_ANGLE/1.4) && isTeammateReadyToCatchPass() || (isEnemyInProximity()&&(angleToOwnNet>Math.PI/6))) {
			move.setAction(ActionType.STRIKE);
		}
		
	}
	
	private Point calcPassDirection() {
		if (!isEnemyInUpperPart())
			return new Point(world.getWidth()/2 - (opponent.getNetFront() - world.getWidth()/2)/3, 0);
		else 
			return new Point(world.getWidth()/2 - (opponent.getNetFront() - world.getWidth()/2)/3, world.getHeight());
			
	}
	
	private void handleIncomingPuck() {
		if (HeadQuarters.getLastPuckOwner().equals("Opponent")) status = Status.DEFENDING;
		double angleToOwnNet = Math.abs(self.getAngleTo(goalDefX, goalDefY));
		//strike it if possible
		
		/*if ((self.getDistanceTo(world.getPuck())<game.getStickLength())
				&&(self.getAngleTo(world.getPuck())<game.getStickSector()/2)
				&&(angleToOwnNet>Math.PI/6) //make sure to check we are not facing our own net so we won't have autogoal
				&& !CATCH_PUCK)
				move.setAction(ActionType.STRIKE);*/
		if ((self.getDistanceTo(world.getPuck())<game.getStickLength())
				&&(self.getAngleTo(world.getPuck())<game.getStickSector()/2)
				&&(angleToOwnNet>Math.PI/6) //make sure to check we are not facing our own net so we won't have autogoal
				&& CATCH_PUCK)
				move.setAction(ActionType.TAKE_PUCK);
	}
	
	
	//checks whether player is on blue team side
	//returns false if player is on red team (right side)
	private boolean isBlueTeam() {
		if (ownside.getNetFront()<world.getWidth()/2) return true;
		return false;
		
	}
	
/*
	//check if enemy has puck
	private boolean isEnemyWithPuck() {
		if (world.getPuck().getOwnerPlayerId()==opponent.getId()) return true;
		return false;
	}
	
	//check if puck is on our side
	private boolean isPuckOnOurField() {
		if (Math.abs(world.getPuck().getX()-ownside.getNetFront())>world.getWidth()/2) return false;
		return true;
	}
	
	private boolean inNetDefendZone(Unit target) {
		if (Math.hypot(target.getX()-ownside.getNetFront(), target.getY()-world.getWidth()/2)<this.DEFEND_ZONE_RADIUS) return true;
		return false;
	}
	*/
	
	//check if enemy is close to our Goalie
	private boolean isEnemyInProximity(){
		for (int i=0; i<guys.length; i++) 
			if (self.getDistanceTo(guys[i])<game.getStickLength()*2 && !guys[i].isTeammate())  return true;
		return false;
	}
	
	private boolean isEnemyInUpperPart() {
		double totalHeightSum = 0;
		for (int i=0; i<guys.length; i++) 
			if (!guys[i].isTeammate()&&guys[i].getType()!=HockeyistType.GOALIE)  totalHeightSum += guys[i].getY() - centerY; 
		if(totalHeightSum<0) return true;
		return false;
	}
	
	
	
	private boolean isTeammateReadyToCatchPass() {
		for (int i=0; i<guys.length; i++) 
			if (self.getDistanceTo(guys[i])>world.getWidth()/2 && guys[i].isTeammate())  return true;
		
		return false;
	}
	
	//finds best defensive position on the field for current tick with account of enemy position,
	//puck position, goalie position, another defender position etc
	private void getDefensiveXY(){
		if (isBlueTeam()) defendX = ownside.getNetFront() + DEFEND_ZONE_RADIUS+5;
		else defendX = ownside.getNetFront() - DEFEND_ZONE_RADIUS-5;
		if (world.getTick()>6000) {
			if (isBlueTeam()) defendX = ownside.getNetFront() + 40;
			else defendX = ownside.getNetFront() - 40;
		}
		defendY = centerY;
		
	}

	public Hockeyist getHockeyist() {
		return self;
	}
	
	

}