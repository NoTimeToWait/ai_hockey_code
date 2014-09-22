import java.util.ArrayList;
import model.*;

public class HeadQuarters{
	// HeadQuarters class implements centralized coordination and order dispatch depending on field situation analysis.
	private static World world;
	private static Game game;
	private static Move[] orders;
	private static ArrayList<Role> team = new ArrayList<Role>();
	private static int tickCount=0;
	private static String lastPuckOwner = "";
	
	/*private static ArrayList<Double> ticks = new ArrayList<Double>();
	private static ArrayList<Double> ticks2 = new ArrayList<Double>();
	
	private static double puckY = 0;
	private static double puckX = 0;
	
	private static double puckRicochetX;
	private static double puckRicochetY;
	
	private static double puckNewX = 0;
	
	private static double puckSpeedY;
	
	public static boolean flag = false;
	public static boolean flag2 = false;*/
	
	//called once per tick by each teammate at the end of Role.act() implementation
	public static void update(Role teammate, World world, Game game, Move move) {
		if (team.size()*2==world.getHockeyists().length-2) team = new ArrayList<Role>();
		team.add(teammate);
		
		if (world.getPuck().getOwnerPlayerId() == world.getMyPlayer().getId()) lastPuckOwner = "MyPlayer";
		if (world.getPuck().getOwnerPlayerId() == world.getOpponentPlayer().getId()) lastPuckOwner = "Opponent";
		/*
		//ricochetY
		if ((world.getPuck().getY() <175) && flag && (puckRicochetX==0)) {
			if (tickCount==0) tickCount = world.getTick();
			puckRicochetX = world.getPuck().getX();
			puckRicochetY = world.getPuck().getY();
			flag2 = false;
		}
		puckSpeedY=world.getPuck().getSpeedY();
		
		if (Math.abs(world.getPuck().getY()-puckY)<10 && world.getTick()>tickCount+4 && flag) {
			flag = false;
			double x1 = Math.abs(world.getPuck().getX()- puckRicochetX);
			double x2 = Math.abs(puckX - puckRicochetX);
			double y1 = Math.abs(world.getPuck().getY() - puckRicochetY);
			double y2 = Math.abs(puckY - puckRicochetY);
			double angle1 = Math.atan(x1/y1);
			double angle2 = Math.atan(x2/y2);
			double angleCoef = angle1/angle2;
			System.out.println("Angle coef:"+Math.toDegrees(angle2) + " " + Math.toDegrees(angle1) + "X,Y" + puckRicochetX + " " + puckRicochetY);
			System.out.println("Init XY:"+ puckX + " " + puckY  + "X,Y" + world.getPuck().getX() + " " + world.getPuck().getY());
			System.out.println();
			puckX=0; puckY=0; puckNewX = 0;puckRicochetX=0; tickCount=0;
		}*/
		
	}
	/*
	public static void updateTime(World world) {
		puckY = world.getPuck().getY();
		puckX = world.getPuck().getX();
		flag = true;
		flag2 = true;
	}
	*/
	
	public static Role askDefenders(Status status) {
		Role role;
		for (int i=0; i<team.size(); i++) {
			role = team.get(i);
			if (role.getType().equalsIgnoreCase("defender") &&
				role.getStatus() == status) 
					return role;
		}
		return null;
	}
	
	public static Role askAttackers(Status status) {
		return null;
		
	}
	
	public static String getLastPuckOwner() {
		return lastPuckOwner;
	}
	
	
	
	

}
