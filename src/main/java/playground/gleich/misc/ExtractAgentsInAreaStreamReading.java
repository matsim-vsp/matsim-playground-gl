package playground.gleich.misc;

import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.pt.router.TransitActsRemover;
import org.matsim.utils.gis.matsim2esri.network.Links2ESRIShape;

import playground.gleich.utilsFromOthers.jbischoff.JbUtils;

/**
 * @author gleich
 * 
 * Return a population with all agents who have activities within a certain geographic area or 
 * pass through this area by car (and ignore all other agents).
 * 
 */
public class ExtractAgentsInAreaStreamReading {
	
	private String inputNetworkPath;
	private String inputPopulationPath;
	private String studyAreaShpPath;
	private String studyAreaShpKey;
	private String studyAreaShpElement;
	private String outputPopulationPath;
	private String outputLinksInAreaShpPath;
	private String outputLinksInAreaShpCoordinateSystem;
	private Scenario inputScenario;
	private Network networkEnclosedInStudyArea;
	private Set<Id<Link>> linksInArea = new HashSet<>();
	private Geometry geometryStudyArea;
	private boolean selectAgentsByRoutesThroughArea;
	private boolean selectAgentsByActivitiesInArea;
	private boolean simplifyOutputPlan;
	
	public static void main(String[] args) {
		ExtractAgentsInAreaStreamReading extractor;

		if (args.length == 8) {
			String inputNetworkPath = args[0];
			String inputPopulationPath = args[1];
			String studyAreaShpPath = args[2];
			String studyAreaShpKey = args[3];
			String studyAreaShpElement = args[4];
			String outputPopulationPath = args[5];
			boolean selectAgentsByActivitiesInArea = Boolean.parseBoolean(args[6]);
			boolean selectAgentsByRoutesThroughArea = Boolean.parseBoolean(args[7]);
			extractor = new ExtractAgentsInAreaStreamReading(inputNetworkPath, inputPopulationPath, studyAreaShpPath, 
					studyAreaShpKey, studyAreaShpElement, outputPopulationPath, selectAgentsByActivitiesInArea, 
					selectAgentsByRoutesThroughArea);
			
		} else if (args.length == 12) {
			String inputNetworkPath = args[0];
			String inputPopulationPath = args[1];
			String studyAreaShpPath = args[2];
			String studyAreaShpKey = args[3];
			String studyAreaShpElement = args[4];
			String outputPopulationPath = args[5];
			boolean selectAgentsByActivitiesInArea = Boolean.parseBoolean(args[6]);
			boolean selectAgentsByRoutesThroughArea = Boolean.parseBoolean(args[7]);
			String outputLinksInAreaCsvPath = args[8];
			String outputLinksInAreaShpPath = args[9];
			String outputLinksInAreaShpCoordinateSystem = args[10];
			boolean simplifyOutputPlan = Boolean.parseBoolean(args[11]);
			extractor = new ExtractAgentsInAreaStreamReading(inputNetworkPath, inputPopulationPath, studyAreaShpPath, 
					studyAreaShpKey, studyAreaShpElement, outputPopulationPath, selectAgentsByActivitiesInArea, 
					selectAgentsByRoutesThroughArea, outputLinksInAreaCsvPath, 
					outputLinksInAreaShpPath, outputLinksInAreaShpCoordinateSystem, simplifyOutputPlan);
			
		} else {
			throw new RuntimeException("invalid number of args");
		}
		extractor.run();
	}
	
	ExtractAgentsInAreaStreamReading(String inputNetworkPath, String inputPopulationPath, String studyAreaShpPath, 
			String studyAreaShpKey, String studyAreaShpElement, String outputPopulationPath, 
			boolean selectAgentsByRoutesThroughArea, boolean selectAgentsByActivitiesInArea){
		new ExtractAgentsInAreaStreamReading(inputNetworkPath, inputPopulationPath, studyAreaShpPath, 
				studyAreaShpKey, studyAreaShpElement, outputPopulationPath, selectAgentsByActivitiesInArea, 
				selectAgentsByRoutesThroughArea, "", "", "", true);
	}
	
	ExtractAgentsInAreaStreamReading(String inputNetworkPath, String inputPopulationPath, String studyAreaShpPath, 
			String studyAreaShpKey, String studyAreaShpElement, String outputPopulationPath, 
			boolean selectAgentsByRoutesThroughArea, boolean selectAgentsByActivitiesInArea, 
			String outputLinksInAreaCsvPath, 
			String outputLinksInAreaShpPath, String outputLinksInAreaShpCoordinateSystem, 
			boolean simplifyOutputPlan){
		this.inputNetworkPath = inputNetworkPath;
		this.inputPopulationPath = inputPopulationPath;
		this.studyAreaShpPath = studyAreaShpPath;
		this.studyAreaShpKey = studyAreaShpKey;
		this.studyAreaShpElement = studyAreaShpElement;
		this.outputPopulationPath = outputPopulationPath;
		this.selectAgentsByRoutesThroughArea = selectAgentsByRoutesThroughArea;
		this.selectAgentsByActivitiesInArea = selectAgentsByActivitiesInArea;
		this.outputLinksInAreaShpPath = outputLinksInAreaShpPath;
		this.outputLinksInAreaShpCoordinateSystem = outputLinksInAreaShpCoordinateSystem;
		this.simplifyOutputPlan = simplifyOutputPlan;
	}
	
	private void run(){
		initialize();
		System.out.println("initialize done");
		StreamingPopulationWriter popWriter = new StreamingPopulationWriter();
		popWriter.writeStartPlans(outputPopulationPath);
		
		StreamingPopulationReader spr = new StreamingPopulationReader(inputScenario);
		spr.addAlgorithm(new PersonAlgorithm() {

			@Override
			public void run(Person person) {
				if(selectAgentsByRoutesThroughArea){
					if(hasRouteThroughArea(person)){
						if (simplifyOutputPlan) removeTransitActsCarRoutesDepartureTimesAndActivityLinkIds(person);
						popWriter.writePerson(person);
					} else if(selectAgentsByActivitiesInArea){
						if(hasActivityInArea(person)){
							if (simplifyOutputPlan) removeTransitActsCarRoutesDepartureTimesAndActivityLinkIds(person);
							popWriter.writePerson(person);
						}
					}
				} else if(selectAgentsByActivitiesInArea){
					if(hasActivityInArea(person)){
						if (simplifyOutputPlan) removeTransitActsCarRoutesDepartureTimesAndActivityLinkIds(person);
						popWriter.writePerson(person);
					}
				}
			}
		}
				);
		spr.readFile(inputPopulationPath);
		popWriter.writeEndPlans();
		System.out.println("ExtractAgentsInArea done");
	}
	
	private void initialize(){		
		inputScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(inputScenario.getNetwork()).readFile(inputNetworkPath);
		geometryStudyArea = JbUtils.readShapeFileAndExtractGeometry(studyAreaShpPath,studyAreaShpKey).get(studyAreaShpElement);
		findLinksInArea();
	}
	
	/** Find all links whose start and end are situated within the area */
	private void findLinksInArea() {
		networkEnclosedInStudyArea = NetworkUtils.createNetwork();
		for(Link link: inputScenario.getNetwork().getLinks().values()){
			if(geometryStudyArea.contains(MGC.coord2Point(link.getFromNode().getCoord())) &&
					geometryStudyArea.contains(MGC.coord2Point(link.getToNode().getCoord()))){
				linksInArea.add(link.getId());
				if( !outputLinksInAreaShpPath.equals("") ){
					Node fromNode = link.getFromNode();
					Node newNetworkFromNode; 
					if(!networkEnclosedInStudyArea.getNodes().containsKey(fromNode.getId())){
						newNetworkFromNode = NetworkUtils.createNode(fromNode.getId(), fromNode.getCoord());
						networkEnclosedInStudyArea.addNode(newNetworkFromNode);
					} else {
						newNetworkFromNode = networkEnclosedInStudyArea.getNodes().get(fromNode.getId());
					}
					Node toNode = link.getToNode();
					Node newNetworkToNode;
					if(!networkEnclosedInStudyArea.getNodes().containsKey(toNode.getId())){
						newNetworkToNode = NetworkUtils.createNode(toNode.getId(), toNode.getCoord());
						networkEnclosedInStudyArea.addNode(newNetworkToNode);
					} else {
						newNetworkToNode = networkEnclosedInStudyArea.getNodes().get(toNode.getId());
					}
					Link newNetworkLink = NetworkUtils.createAndAddLink(networkEnclosedInStudyArea, link.getId(), 
							newNetworkFromNode, newNetworkToNode, link.getLength(), link.getFreespeed(), 
							link.getCapacity(), link.getNumberOfLanes());
					newNetworkLink.setAllowedModes(link.getAllowedModes());
				}
			}
		}
		if(!outputLinksInAreaShpPath.equals("")){
			Links2ESRIShape shp = new Links2ESRIShape(networkEnclosedInStudyArea, outputLinksInAreaShpPath, outputLinksInAreaShpCoordinateSystem);
			shp.write();
		}
	}

	private boolean hasRouteThroughArea(Person p) {		
		Plan plan = p.getSelectedPlan();
		for (PlanElement pe : plan.getPlanElements()){
			if (pe instanceof Leg){
				Leg leg = (Leg) pe;
				if (leg.getRoute() != null && leg.getRoute() instanceof NetworkRoute){
					NetworkRoute route = (NetworkRoute) leg.getRoute();
					for(Id<Link> link: linksInArea){
						if(route.getLinkIds().contains(link)){
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean hasActivityInArea(Person p) {
		Plan plan = p.getSelectedPlan();
		for (PlanElement pe : plan.getPlanElements()){
			if (pe instanceof Activity){
				Activity act = (Activity) pe;
				if (!act.getType().contains("pt interaction")){
					Coord coord = act.getCoord();
					if (geometryStudyArea.contains(MGC.coord2Point(coord))){
						return true;
					}
				}
			}
		}
		return false;
	}

	private void removeTransitActsCarRoutesDepartureTimesAndActivityLinkIds(Person p){
		Plan plan = p.getSelectedPlan();
		new TransitActsRemover().run(plan);
		for (PlanElement pe : plan.getPlanElements()){
			if (pe instanceof Leg){
				Leg leg = (Leg) pe;
				leg.setDepartureTimeUndefined();
				leg.setTravelTimeUndefined();
				leg.setRoute(null);
			} else if (pe instanceof Activity){
				Activity act =  (Activity) pe;
				act.setLinkId(null);
				act.setFacilityId(null);
				act.setStartTimeUndefined();
			}
		}
	}
}
