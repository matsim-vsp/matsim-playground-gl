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
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * @author gleich
 * 
 * Return a population with all agents who have activities within a certain geographic area or 
 * pass through this area by car (and ignore all other agents).
 * 
 */
public class ModifyIncomeStreamReading {

	public static void main(String[] args) {
		String inputPopulationPath = "/home/gregor/git/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.5-10pct/input/berlin-v5.5-10pct.plans.xml.gz";
		String outputPopulationPath = "/home/gregor/git/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.5-10pct/input/berlin-v5.5-10pct.plans_income_all_identical.xml.gz";

		Scenario inputScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

		System.out.println("initialize done");
		StreamingPopulationWriter popWriter = new StreamingPopulationWriter();
		popWriter.writeStartPlans(outputPopulationPath);
		
		StreamingPopulationReader spr = new StreamingPopulationReader(inputScenario);
		spr.addAlgorithm(person -> {
			Double oldIncome = PersonUtils.getIncome(person);
			if (oldIncome != null) {
				double newIncome = 1;
				PersonUtils.setIncome(person, newIncome);
			}
			popWriter.writePerson(person);
		}
		);
		spr.readFile(inputPopulationPath);
		popWriter.writeEndPlans();
		System.out.println("ModifyIncomeStreamReading done");
	}

}
