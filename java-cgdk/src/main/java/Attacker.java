import model.*;


public class Attacker implements Role{
	

    private static final double STRIKE_ANGLE = 1.0D * Math.PI / 180.0D;
    
    public static double ATTACK_ZONE_RADIUS = 200; //half of net height
    public static double ACCURACY = 1.7; // higher number - more accuracy. Should be >1
	public double centerY;
	
	private Hockeyist self;
	private World world;
	private Game game;
	private Move move;
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

	public void act(Hockeyist self, World world, Game game, Move move) {
		
		
		this.self = self;
		this.world = world;
		this.game = game;
		this.move = move;
		ownside = world.getMyPlayer();
		opponent = world.getOpponentPlayer();
		centerY = (ownside.getNetBottom()+ownside.getNetTop())/2;
		guys = world.getHockeyists();
		
		 if (world.getPuck().getOwnerHockeyistId() == self.getId()) {	            
	            getOffensiveXY();
	            getShootingPositions();
	            //if (!withinShootArea()&&(self.getLastAction()!=ActionType.SWING)) moveToShootingPosition();
	            if (!withinShootArea()) moveToShootingPosition();
	            else {
	            	double angleToNet = self.getAngleTo(attackX, attackY);
	            	move.setTurn(angleToNet);
	            
	            	if (Math.abs(angleToNet) < STRIKE_ANGLE/ACCURACY) {
	            		move.setAction(ActionType.SWING);
	            	}
	            
	            	if (self.getSwingTicks()>17)
	            		move.setAction(ActionType.STRIKE);
	            
	            	if ((Math.abs(angleToNet) < STRIKE_ANGLE/ACCURACY)&&(self.getSwingTicks()>15)) {
	            		move.setAction(ActionType.STRIKE);
	            	}
	            }
	        }
		 else if (world.getPuck().getOwnerPlayerId() == ownside.getId()) {
			 getReadyForPass();
		 }
		 else {
	            move.setSpeedUp(1.0D);
	            move.setTurn(self.getAngleTo(world.getPuck()));
	            move.setAction(ActionType.TAKE_PUCK);
	     }
		 if(self.getLastAction() == ActionType.SWING && world.getPuck().getOwnerHockeyistId() != self.getId()) move.setAction(ActionType.CANCEL_STRIKE);
			
		
	}
	
	private boolean withinShootArea() {
		if (self.getDistanceTo(shootPosX, shootPosY1)<=ATTACK_ZONE_RADIUS) return true;
		if (self.getDistanceTo(shootPosX, shootPosY2)<=ATTACK_ZONE_RADIUS) return true;
		return false;
	}
	
	private void getReadyForPass() {
		getShootingPositions();
		if (!isEnemyInUpperPart()) {
			if (self.getDistanceTo(shootPosX, shootPosY1)>ATTACK_ZONE_RADIUS) {
				double distScaleSpeed = self.getDistanceTo(shootPosX, shootPosY1)/(ATTACK_ZONE_RADIUS);
				move.setSpeedUp(distScaleSpeed*distScaleSpeed);
				move.setTurn(self.getAngleTo(shootPosX, shootPosY1));
			}
			else {
				move.setTurn(self.getAngleTo(world.getPuck()));
				handleIncomingPuck();
			}
		}
		else {
			if (self.getDistanceTo(shootPosX, shootPosY2)>ATTACK_ZONE_RADIUS) {
				double distScaleSpeed = self.getDistanceTo(shootPosX, shootPosY2)/(ATTACK_ZONE_RADIUS);
				move.setSpeedUp(distScaleSpeed*distScaleSpeed);
				move.setTurn(self.getAngleTo(shootPosX, shootPosY2));
			}
			else {
				move.setTurn(self.getAngleTo(world.getPuck()));
				handleIncomingPuck();
			}
		}
			
	}
	
	
	private boolean isEnemyInUpperPart() {
		double totalHeightSum = 0;
		for (int i=0; i<guys.length; i++) 
			if (!guys[i].isTeammate()&&guys[i].getType()!=HockeyistType.GOALIE)  totalHeightSum += guys[i].getY() - centerY; 
		if(totalHeightSum<0) return true;
		return false;
	}
	
	private void handleIncomingPuck() {
		
		
		//strike it if possible
		
		if ((self.getDistanceTo(world.getPuck())<game.getStickLength())
				&&(self.getAngleTo(world.getPuck())<game.getStickSector()/2))
				move.setAction(ActionType.TAKE_PUCK);
	}
	
	private void getShootingPositions() {
		shootPosX = world.getWidth()/2 + (opponent.getNetFront() - world.getWidth()/2)/4.5; //(world.getWidth()/2 + opponent.getNetFront())/2;
		shootPosY1 =  centerY - 300;
		shootPosY2 =  centerY + 300;
		
		//if (world.getTick()%10==0) System.out.println(shootPosX);
		
		
	}
	
	private void moveToShootingPosition() {
		if (self.getDistanceTo(shootPosX, shootPosY1)<self.getDistanceTo(shootPosX, shootPosY2)) {
			if (self.getDistanceTo(shootPosX, shootPosY1)>ATTACK_ZONE_RADIUS) {
					double distScaleSpeed = self.getDistanceTo(shootPosX, shootPosY1)/(ATTACK_ZONE_RADIUS*2);
					move.setSpeedUp(distScaleSpeed*distScaleSpeed);
					move.setTurn(self.getAngleTo(shootPosX, shootPosY1));
			}
			else move.setTurn(self.getAngleTo(attackX, attackY));
		}
		else {
			if (self.getDistanceTo(shootPosX, shootPosY2)>ATTACK_ZONE_RADIUS) {
				double distScaleSpeed = self.getDistanceTo(shootPosX, shootPosY2)/(ATTACK_ZONE_RADIUS*2);
				move.setSpeedUp(distScaleSpeed*distScaleSpeed);
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
		if (Math.abs(opponent.getNetTop() - enemyGoalie.getY())>=Math.abs(opponent.getNetBottom() - enemyGoalie.getY())) {
			attackY = opponent.getNetTop()-25;
		}
		else attackY = opponent.getNetBottom()+25;
		//attackY = 2*netY - enemyGoalie.getY();		
	
	}

}