


import model.Game;
import model.Hockeyist;
import model.Move;
import model.World;


public interface Role {
	
	

	public void act(Hockeyist self, World world, Game game, Move move);
	//public void dispatch();
	
	public String getType();
	
	public Status getStatus();
	
	public Hockeyist getHockeyist();
	
	public Point getPassDirection();

}


