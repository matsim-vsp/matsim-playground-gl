package playground.gleich.plans;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * @author gleich
 * 
 * Return a population with all agents who have activities within a certain geographic area or 
 * pass through this area by car (and ignore all other agents).
 * 
 */
public class MoveCarAgentsToCar2StreamReading {

	public static void main(String[] args) {
		String carMode = TransportMode.car;
		String car2Mode = "car2";
		double probability = 0.5;
		String inputPopulationPath = "/home/gregor/git/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.5-10pct/input/berlin-v5.5-10pct.plans.xml.gz";
		String outputPopulationPath = "/home/gregor/git/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.5-10pct/input/berlin-v5.5-10pct.plans_" + probability + "_" + carMode + "_to_" + car2Mode + ".xml.gz";

		Scenario inputScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

		System.out.println("initialize done");
		StreamingPopulationWriter popWriter = new StreamingPopulationWriter();
		popWriter.writeStartPlans(outputPopulationPath);
		
		StreamingPopulationReader spr = new StreamingPopulationReader(inputScenario);
		spr.addAlgorithm(person -> {
			if (Math.random() < probability) {
				for (Plan plan: person.getPlans()) {
					for (PlanElement planElement: plan.getPlanElements()) {
						if (planElement instanceof Leg) {
							Leg leg = (Leg) planElement;
							if (leg.getMode().equals(carMode)) {
								if (! TripStructureUtils.getRoutingMode(leg).equals(carMode)) {
									throw new RuntimeException("car leg with routing mode: " + TripStructureUtils.getRoutingMode(leg));
								}
								leg.setMode(car2Mode);
								TripStructureUtils.setRoutingMode(leg, car2Mode);
							}
							// access/egress walk legs
							if (TripStructureUtils.getRoutingMode(leg).equals(carMode)) {
								TripStructureUtils.setRoutingMode(leg, car2Mode);
							}
						} else if (planElement instanceof Activity) {
							Activity activity = (Activity) planElement;
							if (activity.getType().equals(PlanCalcScoreConfigGroup.createStageActivityType(carMode))) {
								activity.setType(PlanCalcScoreConfigGroup.createStageActivityType(car2Mode));
							}
						}
					}
				}
			}
			popWriter.writePerson(person);
		}
		);
		spr.readFile(inputPopulationPath);
		popWriter.writeEndPlans();
		System.out.println("ExtractAgentsInArea done");
	}

}
