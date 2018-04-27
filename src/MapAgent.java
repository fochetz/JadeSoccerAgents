import java.io.IOException;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class MapAgent extends Agent {
	private static ArrayList<Position> obstacle_position = new ArrayList<Position>();
	private static ArrayList<Integer> obstacle_dim = new ArrayList<Integer>();
	public static MapAgent map_addr = null;
	private static  int ball_speed_x = 0;
	private static int ball_speed_y = 0;
	private static int state = 0; //0 map do nothing, 1 move the ball
	private static Position assist_pos = null;
	protected void setup()
	{
		map_addr = this;
		System.out.println("Map Started\n");
		addBehaviour(new StartBehaviour());
	}
	
	public static void add_obstacle (Position obstacle, Integer dim)
	{
		obstacle_position.add(obstacle);
		obstacle_dim.add(dim);
		
	}

	public static ArrayList<Position> get_obstacle_position() {
		return obstacle_position;
	}

	public static ArrayList<Integer> get_obstacle_dim() {
		return obstacle_dim;
	}
	
	public static void kick(Agent agent, int speed)
	{
		if(agent == BallAgent.get_ball_owner())
		{
			//player can kick
			ball_speed_x = speed;
			state = 1;
			BallAgent.release_possesion(agent);
			
		}
		else System.out.println("Different owner");
	}
	
	
	public static void assist(Agent agent, int speed, Position pos)
	{
		if (agent == BallAgent.get_ball_owner())
		{
			ball_speed_y = speed;
			state = 2;
			assist_pos = pos;
			BallAgent.release_possesion(agent);
			if(assist_pos.getY() < BallAgent.ball_position.getY())
			{
				ball_speed_y = - ball_speed_y;
			}
		}
	}
	private void send_goal()
	{
		System.out.println("Send Goal Packet");
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(new AID("captain", AID.ISLOCALNAME));
		msg.addReceiver(new AID("comrade",AID.ISLOCALNAME));
		msg.setOntology("goal");
		send(msg);
	}
	/*-----------------------------------------------------------------
	 * EXISTING BEHAVIOUR
	 ------------------------------------------------------------------*/
	
	private class ExistingBehaviour extends Behaviour{

		@Override
		public void action() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean done() {
			switch(state)
			{
					case 0:
						return false;
					case 1:
						//going to kick ball behaviour
						addBehaviour( new KickBallBehaviour());
						return true;
					case 2:
						addBehaviour ( new AssistBallBehaviour());
					default:
						return false;
			}

		}
		
	}
	
	
	/*----------------------------------------------------------------
	 * KICK BALL BEHAVIOUR
	 -----------------------------------------------------------------*/
	
	private class KickBallBehaviour extends Behaviour
	{

		@Override
		public void action() {
			block(1000);
			if( ball_speed_x != 0)
			{
				move_ball();
				check_if_goal();
			}
			block(100);
			
		}

		private void check_if_goal() {
			Position ball = BallAgent.ball_position;
			if(ball.getX() == 0 || ball.getX() == 550)
			{
				if (ball.getY() >= 125 && ball.getY() <= 225 && ball_speed_x !=0)
				{
					//GOAL!!
					System.out.println("GOALLLLL");
					ball_speed_x = 0;
					send_goal();
					state = 0;
					block(1000);
				}
			}
			
		}

		private void move_ball() {
			BallAgent.ball_position.setX(BallAgent.ball_position.getX() + ball_speed_x);
		}

		@Override
		public boolean done() {
			switch(state)
			{
				case 0:
					addBehaviour(new ExistingBehaviour());
					return true;
				default: return false;
			}
		}
		
	}
	
	private class AssistBallBehaviour extends Behaviour
	{
		public void action() {
			block(1000);
			move_ball();
			
		}
		private void move_ball() {
			if( !BallAgent.ball_position.equals(assist_pos))
			{
				BallAgent.ball_position.setY(BallAgent.ball_position.getY() + ball_speed_y);
				block(100);
			}
			else
			{
				state = 0;
				ball_speed_y = 0;
			}
			
			//block(1000);
		}

		public boolean done() {
			if(state == 0)
			{

				addBehaviour(new ExistingBehaviour());
				return true;
			}
			else return false;
		}
		
	}
	
	
	private class StartBehaviour extends Behaviour
	{

		@Override
		public void action() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean done() {
			try {
				if (System.in.read() != -1 )
				{
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.addReceiver(new AID("captain", AID.ISLOCALNAME));
					msg.addReceiver(new AID("comrade",AID.ISLOCALNAME));
					msg.setOntology(" ");
					send(msg);
					addBehaviour(new ExistingBehaviour());
					return true;
					
				}
				else
					return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		
	}
}
