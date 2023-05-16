import jig.misc.rd.ai.AgentFactory;
import jig.misc.rd.ai.RobotDefenseAgent;




public class Team2Factory implements AgentFactory {

	public RobotDefenseAgent createAgent(String name, String agentResource) {
		return new Team2Agent();
	}

}
