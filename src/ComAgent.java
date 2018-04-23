import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class ComAgent extends Agent{
	
	private String name;
	private String other_agent = new String("captain");
	public Position actual_pos;
	//speed over x and y
	float speed_x, speed_y;
	//identificate if the agent have the permision to go to the ball
	private boolean canGoToBall = false;
	// for obstacle calculation
	private ArrayList<Position> obstacle_position;
	private ArrayList<Integer> obstacle_dim;
	private int state = 0;
	public final int max_speed = 30;
	public final int ball_possection_distance = 10;
	public final int kick_power = 50;

	public int speed_ball_assist = 1;
	
	private Position position_to_reach = null;
	private Position next_pos = null;
	
	/*-------------------------------------------
	 * setup
	 --------------------------------------------*/
	protected void setup(){
		System.out.println("Comrade Started, adding behaviour");
		name = getAID().getLocalName();
		actual_pos = new Position(100,225);
		blockingReceive();
		addBehaviour(new GoToBallBehaviour());
		}
	public void send_message()
	{
			float my_distance = actual_pos.calculate_distance(BallAgent.ball_position);
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(new AID(other_agent, AID.ISLOCALNAME));
			msg.setOntology("ball_distance");
			msg.setContent(Float.toString(my_distance));
			send(msg);
	}
	
	
	public void receive_message()
	{
		ACLMessage msg = receive();
		if(msg != null )
		{
			if (msg.getOntology().equals("can_move"))
			{
				if(msg.getContent().equals("attack"))
				{
					canGoToBall=true;
					state = 0;
				}
				else if(msg.getContent().equals("defend"))
				{
					canGoToBall=false;
					state = 1;
				}
			}
			
			//check other onthology message
		}
		//no message received
	}
	private void check_ball_possesion()
	{
		if (actual_pos.calculate_distance(BallAgent.ball_position) < ball_possection_distance )
		{
			if(BallAgent.have_i_the_ball(this))state = 2;
		}
	}
	private class GoToBallBehaviour extends Behaviour 
	{
		
		public void action() {
			block(1000);
			send_message();
			receive_message();
			if(canGoToBall && state == 0)
			{
				//function move:
				move();
				//
				//System.out.println(getAID().getName() + " " + actual_pos.getX() + " " + actual_pos.getY());
			}

			check_ball_possesion();
			//block(1000);
		}

		public boolean done() {
			// modify
			switch(state)
			{
				case 0:
					return false;
				case 1:
					System.out.println(name + ": captain is closer to ball, i will play defensive");
					addBehaviour(new PlayDefensiveBehaviour());
					return true;
				case 2:
					System.out.println(name + ": Ball Reached!");
					addBehaviour(new BallHoldingBehaviour());
					return true;
				default : 
					return false;
			}
		}
		
		private void move()
		{
			Position ball_position = BallAgent.ball_position;
			float diff_position = ball_position.getX() - actual_pos.getX();
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
		
				
	}
	private class BallHoldingBehaviour extends Behaviour {

		public void action() {
			block(1000);
			if (no_obstacle())
			{
				//kick
				
			}
			else
			{
				
			}
			
		}
		
		
		
		}

		public boolean done() {
			switch (state)
			{
				case 2:
					return false;
				default: return false;
			}
		}
	}
	private class PlayDefensiveBehaviour extends Behaviour {

		public void action() {
			block(1000);
			send_message();
			receive_message();
			
		}

		public boolean done() {
			switch(state){
			case 0:
				myAgent.addBehaviour(new GoToBallBehaviour());
				return true;
			case 1:
				return false;
			case 2:
				myAgent.addBehaviour(new BallHoldingBehaviour());
				return true;
			default: 
				return false;
			}
		}
		
		
		
	}
	
}
