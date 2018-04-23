import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class CapAgent extends Agent {
	private String name;
	private String other_agent = new String ("comrade");
	private Position actual_pos;
	private String ontology = new String("can_move");
	private String content = new String ("attack");
	//private float my_ball_distance = 0;
	private boolean canGoToBall = false;
	//for obstacle calculation
	private ArrayList<Position> obstacle_position;
	private ArrayList<Integer> obstacle_dim;
	private int state = 0;// 0 attack, 1 defend, 2 have the ball
	public final int max_speed = 30;
	public final int ball_possection_distance = 10;
	public final int kick_power = 50;
	public int speed_ball_assist = 1;
	
	Position position_to_reach = null;
	Position next_pos = null;
	protected void setup()
	{
		System.out.println("Captain Started, adding behaviour");
		name = getAID().getLocalName();
		actual_pos = new Position (100,125);
		blockingReceive();
		addBehaviour(new GoToBallBehaviour());
		
		//for test
		//MapAgent.add_obstacle(new Position(400,175),10);
		
	}

	private void send_msg()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(new AID(other_agent, AID.ISLOCALNAME));
		msg.setOntology(ontology);
		msg.setContent(content);
		send(msg);
	}
	
	private void receive_msg()
	{
		ACLMessage msg = receive();
		if (msg != null)
		{
			if(msg.getOntology().equals("ball_distance"))
			{
				float other_distance = Float.parseFloat(msg.getContent());
				float my_distance = actual_pos.calculate_distance(BallAgent.ball_position);
				if (other_distance < my_distance)
				{
					canGoToBall = false;
					state = 1;
					ontology = new String("can_move");
					content = new String("attack");
				}
				else if (other_distance >= my_distance)
				{
					canGoToBall = true;
					state = 0;
					ontology = new String ("can_move");
					content = new String("defend");
				}
			}
			else if(msg.getOntology().equals("goal"))
			{
				state = 8;
			}
		}
		//else no msg received
		
	}
	private void check_ball_possesion()
	{
		if (actual_pos.calculate_distance(BallAgent.ball_position) < ball_possection_distance )
		{
			if(BallAgent.have_i_the_ball(this)) state = 2;
		}
	}
	
	/*-------------------------------------------------------------
	 * GO TO BALL BEHAVIOUR
	 --------------------------------------------------------------*/
	private class GoToBallBehaviour extends Behaviour 
	{

		public void action() {
			block(1000);
			receive_msg();
			send_msg();
			if(state == 0 && canGoToBall == true)
			{

				move();
			}
			check_ball_possesion();
			
		}
		private void move()
		{
			Position ball_position = BallAgent.ball_position;
			float diff_position = ball_position.getX() - actual_pos.getX();
			float speed_x, speed_y;
			//calculate speed over x
			if(diff_position > max_speed)
			{
				speed_x=max_speed;
			}
			else if (diff_position < -max_speed)
			{
				speed_x=-max_speed;
			}
			else 
			{
				speed_x = diff_position;
			
			}
			//calculate speed over y
			diff_position = ball_position.getY() - actual_pos.getY();
			if(diff_position > max_speed)
			{
				speed_y=max_speed;
			}
			else if (diff_position < -max_speed)
			{
				speed_y=-max_speed;
			}
			else 
			{
				speed_y = diff_position;
			}
			actual_pos.setX(actual_pos.getX() + speed_x);
			actual_pos.setY(actual_pos.getY() + speed_y);
			//integrate possible rotation
		}
		
		public boolean done() {
			
			switch(state)
			{
				case 0:
					return false;
				case 1:
					System.out.print(name + ": Comrade is closer to ball, i will play defensive");
					addBehaviour(new PlayDefensiveBehaviour());
					return true;
				case 2:
					System.out.println(name + ": Ball reached!");
					addBehaviour(new BallHoldingBehaviour());
					return true;
				default : 
					return false;
			}
		}
		
	}
	/*-------------------------------
	 * have ball behaviour
	 ---------------------------------*/
	private class BallHoldingBehaviour extends Behaviour {

		public void action() {
			
		}
		
		private boolean no_obstacle()
		{
		
		}
		
		
		

		public boolean done() {
			switch (state)
			{
				case 2:
					return false;
				case 3:
					System.out.println(name + ": Obstacle in front of me! Help!");
					
				default: return false;
			}
		}
	}
	
	/*---------------------------------------------
	 * play defensive behaviour
	 ---------------------------------------------*/
	private class PlayDefensiveBehaviour extends Behaviour {

		public void action() {
			block(1000);
			receive_msg();
			send_msg();
			check_ball_possesion();			
		}

		public boolean done() {
			switch(state)
			{
				case 0:
					myAgent.addBehaviour(new GoToBallBehaviour ());
					return true;
				case 1:
					return false;
				case 2:
					myAgent.addBehaviour(new BallHoldingBehaviour());
					return true;
				
				default :
					return false;
			}
		}
		

	}
}
