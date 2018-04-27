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
	private int state = 0;// 0 attack, 1 defend, 2 have the ball, 3 wait for passing the ball, 4 for reach specific pos
	//5 waiting ball, 6 wait other receive ball, 7 ball kicked, 8 goal scored
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
	
	private void send_msg_pos(Position p)
	{
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(new AID(other_agent, AID.ISLOCALNAME));
		msg.setOntology("assist_pos");
		msg.setContent(p.getX()+"/"+p.getY());
		send(msg);
	}
	private void send_position_reached()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(new AID(other_agent, AID.ISLOCALNAME));
		msg.setOntology("position_reached");
		send(msg);
	}
	
	private void send_assist()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(new AID(other_agent, AID.ISLOCALNAME));
		msg.setOntology("assist_done");
		send(msg);
		
	}
	
	private void send_ball_received()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(new AID(other_agent, AID.ISLOCALNAME));
		msg.setOntology("ball_received");
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
			else if(msg.getOntology().equals("assist_pos"))
			{
				String[] parts = msg.getContent().split(" */ *");
				float x = Float.parseFloat(parts[0]);
				float y = Float.parseFloat(parts[1]);
				position_to_reach = new Position(x,y);
				state = 4;
			}
			else if(msg.getOntology().equals("position_reached"))
			{
				state = 6;
			}
			else if(msg.getOntology().equals("assist_done"))
			{
				state = 5;
			}
			else if(msg.getOntology().equals("goal"))
			{
				state = 8;
			}
			else if(msg.getOntology().equals("ball_received"))
			{
				state = 2;
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
			block(1000);
			if (no_obstacle())
			{
				//kick
				MapAgent.kick(myAgent, kick_power);
				state = 7;
			}
			else
			{
				// can't kick
				next_pos = calculate_position_for_assist();
				send_msg_pos(next_pos);
				state = 3;
			}
			block(1000);
			
		}
		
		private boolean no_obstacle()
		{
			obstacle_position = MapAgent.get_obstacle_position();
			obstacle_dim = MapAgent.get_obstacle_dim();
			if(obstacle_position == null || obstacle_dim == null ) return true;
			if(obstacle_position.size() != obstacle_dim.size()) return true;
				for(int i = 0; i< obstacle_position.size();i++)
				{
					if((obstacle_position.get(i).getY() + obstacle_dim.get(i) > actual_pos.getY()) && ((obstacle_position.get(i).getY() - obstacle_dim.get(i)) < actual_pos.getY()))
					{
						return false;
					}
				}
		
			return true;
		}
		
		private Position calculate_position_for_assist()
		{
			Set<Position> possible_pos = new HashSet<Position>();
			Set<Position> remove_pos = new HashSet<Position>();
			for(int i = 125;i <=225; i++)
			{
				possible_pos.add(new Position(actual_pos.getX(),(float)i));
			}
			for(Position p : possible_pos)
			{
				for(int i = 0; i< obstacle_position.size();i++)
				{
					if((obstacle_position.get(i).getY() + obstacle_dim.get(i) > p.getY()) && ((obstacle_position.get(i).getY() - obstacle_dim.get(i)) < p.getY()))
					{
						remove_pos.add(p);
					}
				}
			}
			possible_pos.removeAll(remove_pos);
			if (possible_pos.isEmpty()) return null;
			int index = new Random().nextInt(possible_pos.size());
			Iterator<Position> iter = possible_pos.iterator();
			for ( int i=0; i < index ; i++)
			{
				iter.next();
			}
			return iter.next();
		}

		public boolean done() {
			switch (state)
			{
				case 2:
					return false;
				case 3:
					System.out.println(name + ": Obstacle in front of me! Help!");
					addBehaviour(new WaitingForAssistBehaviour());
					return true;
				case 7:
					System.out.println(name + ": ball kicked!");
					addBehaviour(new BallKickedBehaviour());
					return true;
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
				case 4:
					System.out.println(name + ": comrade need help. I will go to position " + position_to_reach.getX() + "," + position_to_reach.getY());
					myAgent.addBehaviour(new ReachPositionBehaviour());
					return true;
				case 8:
					System.out.println(name + ": GOAL!");
					myAgent.addBehaviour(new GoalBehaviour());
					return true;
				default :
					return false;
			}
		}
		

	}

	private class WaitingForAssistBehaviour extends Behaviour {

		public void action() {
			block(1000);
			//send_msg_pos(next_pos);
			receive_msg();
			block(1000);
		}

		public boolean done() {
			switch(state)
			{
				case 6:
					MapAgent.assist(myAgent, speed_ball_assist, next_pos);
					System.out.println(name + ": I will pass the ball!");
					addBehaviour(new WaitingOtherReceiveAssistBehaviour());
					return true;
					default:return false;
			}
		}
		
	}
	
	private class ReachPositionBehaviour extends Behaviour{

		public void action() {
			block(1000);
			move_to_pos();
			if(actual_pos.equals(position_to_reach))
			{
				send_position_reached();
				state = 5;
				block(1000);
			}
		}

		public boolean done() {
			switch(state)
			{
				case 5:
					System.out.println(name + ": reached position, waiting for ball!");
					addBehaviour(new WaitingBallBehaviour());
					return true;
					default:
						return false;
			}
		}
		private void move_to_pos()
		{
			float diff_position = position_to_reach.getX() - actual_pos.getX();
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
			diff_position = position_to_reach.getY() - actual_pos.getY();
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
	private class WaitingBallBehaviour extends Behaviour
	{

		public void action() {
			block(1000);
			check_ball_possesion();
			
		}

		public boolean done() {
			switch(state)
			{
				case 2:
					System.out.println(name + ": ball received!");
					addBehaviour(new BallHoldingBehaviour());
					return true;
					default:
						return false;
			}
		}
		
	}
	private class WaitingOtherReceiveAssistBehaviour extends Behaviour
	{

		public void action() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean done() {
			if(state != 2){
				return false;
			}
			else 
				{
					System.out.println(name + ": comrade received the ball!");
					addBehaviour(new PlayDefensiveBehaviour());
					return true;
				}
		}
		
	}
	
	private class BallKickedBehaviour extends Behaviour
	{
		public void action()
		{
			//do something
			receive_msg();
		}
		
		
		public boolean done()
		{
			switch (state)
			{
				case 8:
					System.out.println(name + ": GOAL!");
					addBehaviour(new GoalBehaviour());
					return true;
				default:
					return false;
					
				
			}
		}
	}
	boolean first_time = true;
	private class GoalBehaviour extends Behaviour
	{
		public void action()
		{
			if(first_time == true)
			{
				System.out.println(name + ": Goal Behaviour");
				first_time = false;
			}
			
		}
		
		public boolean done()
		{
			return false;
			//if start position reach, wait that the ball reach its starting point
		}
	}
	
	
}
