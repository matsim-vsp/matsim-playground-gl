/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
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
package playground.gleich.analysis.pt;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.Vehicles;

/** 
 * 
// * @param netFile
// * @param inScheduleFile: possible to use normal schedule file with all lines, not only minibus
// * @param inTransitVehicleFile: possible to use normal vehicle file with all lines, not only minibus
// * @param coordRefSystem
 */
public class CalculatePtOperatingCosts {
	
	private final Network network;
	private final TransitSchedule inSchedule;
	private final Vehicles inTransitVehicles;
	private final String coordRefSystem;
	private final String minibusIdentifier;

	public CalculatePtOperatingCosts(String netFile, String inScheduleFile, String inTransitVehicleFile, String coordRefSystem, String minibusIdentifier) {
		this.coordRefSystem = coordRefSystem;
		this.minibusIdentifier = minibusIdentifier;
		
		// read files
		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(netFile);
		config.transit().setTransitScheduleFile(inScheduleFile);
		config.transit().setVehiclesFile(inTransitVehicleFile);
		config.global().setCoordinateSystem(coordRefSystem); // coordinate reference system should be irrelevant, no need to make it configurable
		Scenario scenario = ScenarioUtils.loadScenario(config);
		this.network = scenario.getNetwork(); 
		this.inSchedule = scenario.getTransitSchedule();
		this.inTransitVehicles = scenario.getTransitVehicles();
	}

	/**
	 * Example for usage + convenience
	 * @param args
	 */
	public static void main(String[] args) {
		String networkFile = "/home/gregor/git/shared-svn/projects/avoev/matsim-input-files/vulkaneifel/v0/optimizedNetwork.xml.gz";
//		String inScheduleFile = "/home/gregor/git/shared-svn/projects/avoev/matsim-input-files/vulkaneifel/v0/optimizedSchedule.xml.gz";
//		String inTransitVehicleFile = "/home/gregor/git/shared-svn/projects/avoev/matsim-input-files/vulkaneifel/v0/optimizedVehicles.xml.gz";
		String inScheduleFile = "/home/gregor/git/shared-svn/projects/avoev/matsim-input-files/vulkaneifel/v1/optimizedSchedule_all-buses-split.xml.gz";
		String inTransitVehicleFile = "/home/gregor/git/shared-svn/projects/avoev/matsim-input-files/vulkaneifel/v1/optimizedVehicles_all-buses-split.xml.gz";
//		String networkFile = "/home/gregor/git/shared-svn/projects/avoev/matsim-input-files/gladbeck_umland/v0/optimizedNetwork.xml.gz";
//		String inScheduleFile = "/home/gregor/git/shared-svn/projects/avoev/matsim-input-files/gladbeck_umland/v1/optimizedSchedule_nonSB-bus-split-at-hubs.xml.gz";
//		String inTransitVehicleFile = "/home/gregor/git/shared-svn/projects/avoev/matsim-input-files/gladbeck_umland/v1/optimizedVehicles_nonSB-bus-split-at-hubs.xml.gz";
//		String inScheduleFile = "/home/gregor/git/shared-svn/projects/avoev/matsim-input-files/gladbeck_umland/v0/optimizedSchedule.xml.gz";
//		String inTransitVehicleFile = "/home/gregor/git/shared-svn/projects/avoev/matsim-input-files/gladbeck_umland/v0/optimizedVehicles.xml.gz";

		String coordRefSystem = "epsg:25832";
		String minibusIdentifier = "";

		double costPerHour = 1;
		double costPerKm = 1;
		double costPerDayFixVeh = 1;

//		String networkFile = "/home/gregor/git/runs-svn/capetown-minibuses/output-minibus-wo-transit/100pct/2018-12-03_100pct_1500it/output_network.xml.gz";
//		String inScheduleFile = "/home/gregor/git/runs-svn/capetown-minibuses/output-minibus-wo-transit/100pct/2018-12-03_100pct_1500it/ITERS/it.1500/1500.transitScheduleScored.xml.gz";
//		String inTransitVehicleFile = "/home/gregor/git/runs-svn/capetown-minibuses/output-minibus-wo-transit/100pct/2018-12-03_100pct_1500it/ITERS/it.1500/1500.transitVehicles.xml.gz";
		// input files with formal transit
//		String inScheduleFile = "/home/gregor/git/capetown/output-minibus-w-transit/2018-11-09/ITERS/it.100/100.transitSchedule.xml.gz";
//		String inTransitVehicleFile = "/home/gregor/git/capetown/output-minibus-w-transit/2018-11-09/ITERS/it.100/100.transitVehicles.xml.gz";
//		String networkFile = "/home/gregor/git/matsim/contribs/av/src/test/resources/intermodal_scenario/network.xml";
//		String inScheduleFile = "/home/gregor/git/matsim/contribs/av/src/test/resources/intermodal_scenario/transitschedule.xml";
//		String inTransitVehicleFile = "/home/gregor/git/matsim/contribs/av/src/test/resources/intermodal_scenario/transitVehicles.xml";

//		String coordRefSystem = "SA_Lo19";
//		String minibusIdentifier = "para_";
		
//		double costPerHour = 15;
//		double costPerKm = 1.75;
//		double costPerDayFixVeh = 700;
		
		// add vehicle types
		CalculatePtOperatingCosts costCalculator = new CalculatePtOperatingCosts(networkFile, inScheduleFile, inTransitVehicleFile, coordRefSystem, minibusIdentifier);
		costCalculator.run(costPerHour, costPerKm, costPerDayFixVeh);
	}
	
	public void run(double costPerHour, double costPerKm, double costPerDayFixVeh) {
		double hoursDriven = 0.0;
		double kmDriven = 0.0;
		int numVehUsed = 0;
		Set<Id<Vehicle>> vehIds = new HashSet<>();
		
		for (TransitLine line: inSchedule.getTransitLines().values()) {
			for (TransitRoute route: line.getRoutes().values()) {
				double routeLength = network.getLinks().get(route.getRoute().getStartLinkId()).getLength();
				for (Id<Link> linkId: route.getRoute().getLinkIds()) {
					routeLength = routeLength + network.getLinks().get(linkId).getLength();
				}
				routeLength = routeLength + network.getLinks().get(route.getRoute().getEndLinkId()).getLength();

				for (Departure dep: route.getDepartures().values()) {
					if (! vehIds.contains(dep.getVehicleId())) {
						vehIds.add(dep.getVehicleId());
					}
				}
				// travel time according to schedule, not exact
//				double startTime = Double.isFinite(route.getStops().get(0).getArrivalOffset()) ? route.getStops().get(0).getArrivalOffset() : route.getStops().get(0).getDepartureOffset();
				double endTime = route.getStops().get(route.getStops().size() - 1).getDepartureOffset().or(route.getStops().get(route.getStops().size() - 1)::getArrivalOffset).seconds();
				double travelTime = endTime; // don't subtract offset at first stop, the vehicle will depart at offset 0 anyway ?!
				hoursDriven = hoursDriven + travelTime * route.getDepartures().size() / 3600; // travelTime is in sec
				kmDriven = kmDriven + routeLength * route.getDepartures().size() / 1000; // routeLength is in m
			}
		}
		
		// a vehicle could be used on multiple lines, so calculate according to that
		numVehUsed = numVehUsed + vehIds.size();
		
		double totalCost = hoursDriven * costPerHour + kmDriven * costPerKm + numVehUsed * costPerDayFixVeh;
		System.out.println("hoursDriven: " + hoursDriven + " -> cost " + hoursDriven * costPerHour);
		System.out.println("kmDriven: " + kmDriven + " -> cost " + kmDriven * costPerKm + " ; km per veh per day: " + kmDriven / numVehUsed);
		System.out.println("numVehUsed: " + numVehUsed + " -> cost " + numVehUsed * costPerDayFixVeh);
		System.out.println("totalCost: " + totalCost);
	}
	
	// tested with matsim/contribs/av/src/test/resources/intermodal_scenario
//	hoursDriven: 26.666666666666664 -> cost 399.99999999999994
//	kmDriven: 1000.0 -> cost 2000.0 ; km per veh per day: 250.0
//	numVehUsed: 4 -> cost 2400.0
//	totalCost: 4800.0

}
