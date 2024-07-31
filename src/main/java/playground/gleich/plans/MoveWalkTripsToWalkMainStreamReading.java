/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2022 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.gleich.plans;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.algorithms.TripsToLegsAlgorithm;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.router.RoutingModeMainModeIdentifier;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * @author gleich
 * 
 * Return a population with all agents who have activities within a certain geographic area or 
 * pass through this area by car (and ignore all other agents).
 * 
 */
public class MoveWalkTripsToWalkMainStreamReading {

	public static void main(String[] args) {
		String fromMode = TransportMode.walk;
		String toMode = "walkMain";
		double probability = 1.0;
		String inputPopulationPath = "../../ilsMount/matsim-berlin/calibration-3rd/input/v6.0/berlin-v6.0-10pct.plans.xml.gz";
		String outputPopulationPath = "../../ilsMount/matsim-berlin/calibration-3rd/input/v6.0/berlin-v6.0-10pct.plans_" + toMode + "_unrouted.xml.gz";
		Scenario inputScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

		TripsToLegsAlgorithm tripsToLegsAlgorithm = new TripsToLegsAlgorithm(new RoutingModeMainModeIdentifier());

		System.out.println("initialize done");
		StreamingPopulationWriter popWriter = new StreamingPopulationWriter();
		popWriter.writeStartPlans(outputPopulationPath);
		
		StreamingPopulationReader spr = new StreamingPopulationReader(inputScenario);
		spr.addAlgorithm(person -> {

				for (Plan plan: person.getPlans()) {
					tripsToLegsAlgorithm.run(plan);
					if (Math.random() < probability) {
					for (PlanElement planElement: plan.getPlanElements()) {
						if (planElement instanceof Leg) {
							Leg leg = (Leg) planElement;
							if (leg.getMode().equals(fromMode)) {
								if (TripStructureUtils.getRoutingMode(leg).equals(fromMode)) {
									leg.setMode(toMode);
									TripStructureUtils.setRoutingMode(leg, toMode);
								}
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
