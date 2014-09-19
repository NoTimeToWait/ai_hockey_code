import model.Game;
import model.Hockeyist;
import model.Move;
import model.World;


public interface Role {
	

	public void act(Hockeyist self, World world, Game game, Move move);

}
