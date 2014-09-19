import model.*;

public class Defender implements Role {
	
	public static double DEFEND_ZONE_RADIUS = 100; //half of net height
	public double centerY;
	
	private Hockeyist self;
	private World world;
	private Game game;
	private Move move;
	private Player ownside;
	private Player opponent;
	private Hockeyist[] guys;
	private Hockeyist ourGoalie;
	private double defendX;
	private double defendY;
	
	//coords of our net  center poing, where enemy goal will count
	private double goalDefX;
	private double goalDefY;
	
	
	
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
		handleIncomingPuck();
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

		if ((dist>self.getRadius()/2) && (dist<self.getRadius()*5)
				&&(Math.abs(self.getAngleTo(defendX, defendY))>Math.PI-0.05)) {
				//if we find that defense point right at guy's back and he is quite far away from it
				//we slowly progress backwards
			        move.setTurn(self.getAngleTo(world.getPuck()));
					move.setSpeedUp(-1.0D);
				}
		
	}
	
	private void handleIncomingPuck() {
		
		//make sure to check we are not facing our own net so we won't have autogoal
		double angleToOwnNet = Math.abs(self.getAngleTo(goalDefX, goalDefY));
		//strike it if possible
		if ((self.getDistanceTo(world.getPuck())<game.getStickLength())
				&&(self.getAngleTo(world.getPuck())<game.getStickSector()/2)
				&&(angleToOwnNet>Math.PI/6))
				move.setAction(ActionType.STRIKE);
	}
	
	
	//checks whether player is on blue team side
	//returns false if player is on red team (right side)
	private boolean isBlueTeam() {
		if (ownside.getNetFront()<world.getWidth()/2) return true;
		return false;
		
	}
	

	//check if enemy has puck
	private boolean isEnemyWithPuck() {
		if (world.getPuck().getOwnerPlayerId()!=opponent.getId()) return false;
		return true;
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
	
	//finds best defensive position on the field for current tick with account of enemy position,
	//puck position, goalie position, another defender position etc
	public void getDefensiveXY(){
		/*obsolete, mb will have some use in future
		//find defendX
		if (isBlueTeam()) defendX = ownside.getNetFront()+self.getRadius();
		else defendX = ownside.getNetFront()-self.getRadius();
		
		//look for our goalie
		for (int i=0; i<guys.length; i++) {
			if ((guys[i].getType() == HockeyistType.GOALIE) &&
				(Math.abs(guys[i].getX() - ownside.getNetFront())<world.getWidth()/2)) {
				this.ourGoalie = guys[i];
				break;
			}
		}
		
		//find defendY
		if (world.getPuck().getY()<= ourGoalie.getY()) defendY = ourGoalie.getY() + 2*self.getRadius();
		else defendY = ourGoalie.getY() - 2*self.getRadius();
		*/
		if (isBlueTeam()) defendX = ownside.getNetFront() + DEFEND_ZONE_RADIUS+5;
		else defendX = ownside.getNetFront() - DEFEND_ZONE_RADIUS-5;
		
		defendY = centerY;
		
	}
	
	

}
