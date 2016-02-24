/*
 * JumpPath,java
 * 
 * Copyright (c) 2011 Jay Lawson <jaylawson39 at yahoo.com>. All rights reserved.
 * 
 * This file is part of MekHQ.
 * 
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */

package mekhq.campaign;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;

import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.universe.Planet;
import mekhq.campaign.universe.SpaceLocation;
import mekhq.campaign.universe.Star;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is an array list of planets for a jump path, from which we can derive
 * various statistics. We can also add in details about the jump path here, like if
 * the user would like to use recharge stations when available. For XML serialization, 
 * this object will need to spit out a list of planet names and then reconstruct 
 * the planets from that.
 * 
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class JumpPath implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 708430867050359759L;
	private ArrayList<SpaceLocation> path;
	
	public JumpPath() {
		path = new ArrayList<SpaceLocation>();
	}
	
	public JumpPath(ArrayList<SpaceLocation> p) {
		path = p;
	}
	
	public ArrayList<SpaceLocation> getPlanets() {
		return path;
	}
	
	public boolean isEmpty() {
		return path.isEmpty();
	}
	
	public SpaceLocation getFirstPlanet() {
		if(path.isEmpty()) {
			return null;
		} else {
			return path.get(0);
		}
	}
	
	public SpaceLocation getLastPlanet() {
		if(path.isEmpty()) {
			return null;
		} else {
			return path.get(path.size() - 1);
		}
	}
	
	/*
	public double getStartTime(double currentTransit) {
		double startTime = 0.0;
		if(null != getFirstPlanet()) {
			startTime = getFirstPlanet().getTimeToJumpPoint(1.0);
		}
		return startTime - currentTransit;
	}
	
	public double getEndTime() {
		double endTime = 0.0;
		if(null != getLastPlanet()) {
			endTime = getLastPlanet().getTimeToJumpPoint(1.0);
		}
		return endTime;
	}
	*/
	
	public double getTotalRechargeTime() {
		double rechargeTime = 0;
		for( int i = 0; i < path.size() - 1; ++ i ) {
			SpaceLocation loc = path.get(i);
			// If we can jump to the next location, we will
			if( loc.canJumpTo(path.get(i + 1)) ) {
				rechargeTime += loc.rechargeTime();
			}
			// Else we're using in-system transfer
		}
		return rechargeTime / 24.0;
	}

	public int getJumps() {
		return size()-1;
	}
	
	public double getTotalTime(double currentTransit) {	
		double totalTime = 0;
		for( int i = 0; i < path.size() - 1; ++ i ) {
			SpaceLocation loc = path.get(i);
			if( loc.canJumpTo(path.get(i + 1)) ) {
				// Jumping - recharge time + 1 hour for the jump
				// TODO - make the jump time dependent on the skill level of the crew
				totalTime += loc.rechargeTime() + 1;
			} else {
				// In-system transit
				totalTime += loc.travelTimeTo(path.get(i + 1));
			}
		}
		return totalTime / 24.0;
	}
	
	public void addLocation(Planet p) {
		path.add(p.getPointOnSurface());
	}
	
	/** Add one of the jump points of the given star */
	public void addLocation(Star s) {
		path.add(s.getJumpPoint(true));
	}

	public void addLocation(SpaceLocation l) {
		path.add(l);
	}
	/*
	public void addPlanets(ArrayList<Planet> planets) {
		path.addAll(planets);
	}
	*/
	
	public void removeFirstPlanet() {
		if(!path.isEmpty()) {
			path.remove(0);
		}
	}
	
	public int size() {
		return path.size();
	}
	
	public SpaceLocation get(int i) {
		if(i >= size()) {
			return null;
		} else {
			return path.get(i);
		}
	}
	
	/*
	public boolean contains(Planet planet) {
		return path.contains(planet);
	}
	*/
	
	public void writeToXml(PrintWriter pw1, int indent) {
		pw1.println(MekHqXmlUtil.indentStr(indent) + "<jumpPath>");
		for(SpaceLocation p : path) {
		pw1.println(MekHqXmlUtil.indentStr(indent+1)
				+"<loc>"
				+MekHqXmlUtil.escape(p.getName())
				+"</loc>");
		}
		pw1.println(MekHqXmlUtil.indentStr(indent) + "</jumpPath>");
		
	}
	
	public static JumpPath generateInstanceFromXML(Node wn, Campaign c) {
		JumpPath retVal = null;
		
		try {		
			retVal = new JumpPath();
			NodeList nl = wn.getChildNodes();
			
			for (int x=0; x<nl.getLength(); x++) {
				Node wn2 = nl.item(x);
				if (wn2.getNodeName().equalsIgnoreCase("loc")) {
					SpaceLocation loc = SpaceLocation.byName(wn2.getTextContent());
					if(null != loc) {
						retVal.addLocation(loc);
					} else {
						MekHQ.logError("Couldn't parse location " + wn2.getTextContent());
					}
				}
			}
		} catch (Exception ex) {
			// Errrr, apparently either the class name was invalid...
			// Or the listed name doesn't exist.
			// Doh!
			MekHQ.logError(ex);
		}
		
		return retVal;
	}
}