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

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author gleich
 * 
 * Return a population with all agents who have activities within a certain geographic area or 
 * pass through this area by car (and ignore all other agents).
 * 
 */
public class ExtractSelectedAgentsStreamReading {

	private final String inputPopulationPath;
	private final String outputPopulationPath;
	private final Set<Id<Person>> agentSet;
	private Scenario inputScenario;

	public static void main(String[] args) {
		ExtractSelectedAgentsStreamReading extractor;
		Set<Id<Person>> agentSet;

		if (args.length == 3) {
			String inputPopulationPath = args[0];
			String outputPopulationPath = args[1];
			String agentIdString = args[2];
			agentSet = new HashSet<>();
			agentSet.add(Id.createPersonId(agentIdString));
			extractor = new ExtractSelectedAgentsStreamReading(inputPopulationPath, outputPopulationPath, agentSet);
		} else {
			throw new RuntimeException("invalid number of args");
		}
		extractor.run();
	}

	ExtractSelectedAgentsStreamReading(String inputPopulationPath, String outputPopulationPath,
                                       Set<Id<Person>> agentSet){
		this.inputPopulationPath = inputPopulationPath;
		this.outputPopulationPath = outputPopulationPath;
		this.agentSet = agentSet;
	}
	
	private void run(){
		initialize();
		System.out.println("initialize done");
		StreamingPopulationWriter popWriter = new StreamingPopulationWriter();
		popWriter.writeStartPlans(outputPopulationPath);
		
		StreamingPopulationReader spr = new StreamingPopulationReader(inputScenario);
		spr.addAlgorithm(person -> {
			if(agentSet.contains(person.getId())){
				popWriter.writePerson(person);
			}
		}
		);
		spr.readFile(inputPopulationPath);
		popWriter.writeEndPlans();
		System.out.println("ExtractAgentsInArea done");
	}
	
	private void initialize(){		
		inputScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
	}

}
