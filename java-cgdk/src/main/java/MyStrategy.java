import model.*;

import static java.lang.StrictMath.PI;
import static java.lang.StrictMath.abs;

public final class MyStrategy implements Strategy {
    private static final double STRIKE_ANGLE = 1.0D * PI / 180.0D;
    
    private Role role;
    @Override
    public void move(Hockeyist self, World world, Game game, Move move) {
    	
    	if (role==null) {
    		if (self.getX()==300||self.getX()==900) role = new Defender();
    		else role = new Attacker();
    	}
    	
    	//if (world.getTick()==5999) System.out.println(world.getMyPlayer().getGoalCount()+" "+world.getOpponentPlayer().getGoalCount());
    	role.act(self, world, game, move);
        /*
    	if (world.getPuck().getOwnerHockeyistId() == self.getId()) {
            Player opponentPlayer= world.getOpponentPlayer();

            double netX = 0.5D * (opponentPlayer.getNetBack() + opponentPlayer.getNetFront());
            double netY = 0.5D * (opponentPlayer.getNetBottom() + opponentPlayer.getNetTop());
            
            double dist = Math.abs(self.getX() - netX);
            
            double tan = (self.getY()-150)/(dist/2);
            
            double angleToNet = (Math.atan(tan)*1.5 - PI) - self.getAngle();
            move.setTurn(PI);
            if (abs(angleToNet) < STRIKE_ANGLE) {
                move.setAction(ActionType.STRIKE);
            }
            
            
        } else {
            move.setSpeedUp(1.0D);
            move.setTurn(self.getAngleTo(world.getPuck()));
            move.setAction(ActionType.TAKE_PUCK);
        }
        */
    }
}
