
import model.*;

public class Semidefender implements Role {
	


    public static double ATTACK_ZONE_RADIUS = 125;
    private static final double STRIKE_ANGLE = 1.0D * Math.PI / 180.0D;
	public static double DEFEND_ZONE_RADIUS = 80; //half of net height
	public static double ACCURACY = 1.7; // higher number - more accuracy. Should be >1
    public static double FORCE = 15; // swing orce before shoot puck/ Should be between 0 and 20(not recommended), where 0 - shoot puck without swing
	
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
	
	private double attackX;
	private double attackY;
		
	//coords of our net  center poing, where enemy goal will count
	private double goalDefX;
	private double goalDefY;
	
	private double shootPosX;
	private double shootPosY1;
	private double shootPosY2;
	
	private Point movePoint=null;
	
	private boolean isEnemyInUpperPart = false;
	
	public String getType() {
		return "Defender";
	}
	
	public Status getStatus() {
		return status;
	}
	
	//later with implemented Prediction model could possible return final puck "meeting" position
	//right now only general shooting direction
	//public Point getPassDirection(){
	//	return passPoint;
	//}
		
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
		if (world.getPuck().getOwnerHockeyistId() == self.getId()) attackOpponentNet();//handlePass();
		else handleIncomingPuck();
		
		if(self.getLastAction() == ActionType.SWING && world.getPuck().getOwnerHockeyistId() != self.getId()) move.setAction(ActionType.CANCEL_STRIKE);
		HeadQuarters.update(this, world, game, move);
	}
	
	private void moveToNet() {
		
		getDefensiveXY();
		move.setTurn(self.getAngleTo(world.getPuck()));

		
		double dist = self.getDistanceTo(defendX, defendY); //Math.hypot(defendX-self.getX(), defendY-self.getY());
		
		//short dist and in front of this guy
		if ((dist>self.getRadius()*1.5)&&(dist<=self.getRadius()*4)&&(self.getSpeedX()<1)
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
				&&(Math.abs(self.getAngleTo(defendX, defendY))>Math.PI-0.2)) {
				//if we find that defense point right at guy's back and he is quite far away from it
				//we slowly progress backwards
			        move.setTurn(self.getAngleTo(self.getX() + (self.getX()- defendX), self.getY() + (self.getY()- defendY)));
					move.setSpeedUp(-1.0D);
				}
		
		if (world.getPuck().getOwnerPlayerId() != ownside.getId() && 
				 world.getPuck().getOwnerPlayerId() != opponent.getId() &&
				 world.getPuck().getDistanceTo(self)<game.getStickLength()*1.5){
	            move.setSpeedUp(1.0D);
	            move.setTurn(self.getAngleTo(world.getPuck()));
	            move.setAction(ActionType.TAKE_PUCK);
	     }
		
	}
	
	private void handleIncomingPuck() {
		movePoint = null;
		if (HeadQuarters.getLastPuckOwner().equals("Opponent")) {
			status = Status.DEFENDING;
		}
		double angleToOwnNet = Math.abs(self.getAngleTo(goalDefX, goalDefY));
		if ((self.getDistanceTo(world.getPuck())<game.getStickLength())
				&&(self.getAngleTo(world.getPuck())<game.getStickSector()/2)
				&&(angleToOwnNet>Math.PI/6)) //make sure to check we are not facing our own net so we won't have autogoal
				move.setAction(ActionType.TAKE_PUCK);
	}
	
	private void attackOpponentNet() {
		if (movePoint==null) movePoint = calcAvoidMovement();
		else movePoint = calcAttackMovement();
		
	}
	
	private Point calcAvoidMovement() {

		getShootingPositions();
		getOffensiveXY();
		double totalHeightSum = 0;
		for (int i=0; i<guys.length; i++) 
			if (!guys[i].isTeammate()&&guys[i].getType()!=HockeyistType.GOALIE)  totalHeightSum += guys[i].getY() - centerY; 

		if (totalHeightSum<0) isEnemyInUpperPart = true;
		if (totalHeightSum>=0) isEnemyInUpperPart = false;
		
		Point target;
		target = new Point(self.getX() - (self.getX() - opponent.getNetFront())/5, isEnemyInUpperPart? 750:170);
		
		
		
		return target;
	}
	
	private Point calcAttackMovement() {
		Point target = movePoint;
		if (self.getY()<600 && self.getY()>300) {

			move.setSpeedUp(1.0D);
			move.setTurn(self.getAngleTo(target.getX(), target.getY()));
		}
		else {
			getShootingPositions();
			getOffensiveXY();
			target = new Point(shootPosX, self.getY()<world.getHeight()/2? shootPosY1:shootPosY2 );
			double newAttackZone = ATTACK_ZONE_RADIUS+50;
			if (self.getDistanceTo(target.getX(), target.getY())>newAttackZone+100) {
				double distScaleSpeed = self.getDistanceTo(target.getX(), target.getY())/(newAttackZone);
				move.setSpeedUp(distScaleSpeed);
				move.setTurn(self.getAngleTo(target.getX(), target.getY()));
			}
			else {
				double angleToNet = self.getAngleTo(attackX, attackY);
	        	move.setTurn(angleToNet);
        		movePoint = null;
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
		return target;
	}
		
	//checks whether player is on blue team side
	//returns 1 if player is on red team (right side)
	private int isBlueTeam() {
		if (ownside.getNetFront()<world.getWidth()/2) return -1;
		return 1;
		
	}
				
		
	//finds best defensive position on the field for current tick with account of enemy position,
	//puck position, goalie position, another defender position etc
	private void getDefensiveXY(){
		defendX = ownside.getNetFront() - isBlueTeam()*DEFEND_ZONE_RADIUS;
		
		if (world.getTick()>6000) defendX = ownside.getNetFront() - isBlueTeam()*60;
		
		if (self.getOriginalPositionIndex()%2==0) defendY = centerY+40;
		if (self.getOriginalPositionIndex()%2!=0) defendY = centerY-40;
	}
	
private void getOffensiveXY(){
		
        double netX = 0.5D * (opponent.getNetBack() + opponent.getNetFront());
        double netY = 0.5D * (opponent.getNetBottom() + opponent.getNetTop());
        Hockeyist enemyGoalie=null;
        attackX = netX;
		//look for our goalie
				for (int i=0; i<guys.length; i++) {
					if ((guys[i].getType() == HockeyistType.GOALIE) && !guys[i].isTeammate()) {
						enemyGoalie = guys[i];
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
	
	private void getShootingPositions() {
		shootPosX = world.getWidth()/2 + (opponent.getNetFront() - world.getWidth()/2)/2; //(world.getWidth()/2 + opponent.getNetFront())/2;
		shootPosY1 =  centerY - 200;
		shootPosY2 =  centerY + 200;
	}

	public Hockeyist getHockeyist() {
		return self;
	}

	@Override
	public Point getPassDirection() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}