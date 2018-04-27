import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class BallAgent extends Agent {
	static BallAgent ball = null;
	static Position ball_position = new Position(275,175);
	static private Agent my_owner= null;
	private static int speed = 0;
	
	static boolean have_i_the_ball(Agent agent)
	{
		if(my_owner == null || my_owner == agent)
		{
			my_owner = agent;
			return true;
		}
		else
		{
			//implement some stochastic behaviour in case two agent want the ball
			return false;
		}
		
	}
	static void release_possesion(Agent agent)
	{
		if(agent == my_owner)
		{
			//releas possesion of the ball
			my_owner = null;
		}
	}
	public static Agent get_ball_owner() {
		return my_owner;
	}
	public static void set_agent(Agent agent)
	{
		my_owner = agent;
	}
	
	private class MovingBehaviour extends Behaviour
	{

		@Override
		public void action() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
}
