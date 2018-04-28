import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class ObstacleFixAgent extends Agent {
	Position obstacle_pos;
	private final int dimension = 10;
	private String name;
	private String ontology = new String("Obstacle");
	protected void setup()
	{
		name = getAID().getLocalName();
		if (name.equals("first"))
		{
			obstacle_pos = new Position(400,175);
		}
		MapAgent.add_obstacle(obstacle_pos,dimension);
	}
	
}
