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
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.w3c.dom.Node;

import mekhq.MekHQ;
import mekhq.adapters.SpaceLocationAdapter;
import mekhq.campaign.universe.Planet;
import mekhq.campaign.universe.SpaceLocation;
import mekhq.campaign.universe.Star;

/**
 * This is an array list of planets for a jump path, from which we can derive
 * various statistics. We can also add in details about the jump path here, like if
 * the user would like to use recharge stations when available. For XML serialization, 
 * this object will need to spit out a list of planet names and then reconstruct 
 * the planets from that.
 * 
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
@XmlRootElement(name="jumpPath")
public class JumpPath implements Serializable {
	private static final long serialVersionUID = 708430867050359759L;
	@XmlElement(name="start")
	@XmlJavaTypeAdapter(SpaceLocationAdapter.class)
	private SpaceLocation start;
	@XmlElement(name="loc")
	private List<Edge> path;
	
	public JumpPath() {
		path = new ArrayList<Edge>();
		start = null;
	}
	
	public JumpPath(List<Edge> p) {
		path = new ArrayList<Edge>(p);
		start = !path.isEmpty() ? path.get(0).source : null;
	}
	
	public List<SpaceLocation> getPlanets() {
		List<SpaceLocation> result = new ArrayList<SpaceLocation>(path.size());
		if( path.isEmpty() ) {
			return result;
		}
		result.add(start);
		for( Edge n : path ) {
			result.add(n.target);
		}
		return result;
	}
	
	public boolean isEmpty() {
		return path.isEmpty();
	}
	
	public SpaceLocation getFirstPlanet() {
		return start;
	}
	
	public SpaceLocation getLastPlanet() {
		if(path.isEmpty()) {
			return start;
		} else {
			return path.get(path.size() - 1).target;
		}
	}
	
	/** @return a copy of the edges specified in this path */
	public List<Edge> getEdges() {
		return new ArrayList<Edge>(path);
	}
	
	/** @return the last jump point in the path */
	public SpaceLocation getLastJumpPoint() {
		if( path.isEmpty() ) {
			return start.isJumpPoint() ? start : null;
		}
		for( int i = path.size() - 1; i >= 0; -- i ) {
			SpaceLocation loc = path.get(i).target;
			if( loc.isJumpPoint() ) {
				return loc;
			}
		}
		return start.isJumpPoint() ? start : null;
	}
	
	/**
	 * Remove all the edges going past the last jump point.
	 * This can leave the path empty if it doesn't go past any jump points.
	 */
	public void removeEverythingPastLastJumpPoint() {
		for( int i = path.size() - 1; i >= 0; -- i ) {
			SpaceLocation loc = path.get(i).target;
			if( !loc.isJumpPoint() ) {
				path.remove(i);
			}
		}
		if( path.isEmpty() ) {
			if( !start.isJumpPoint() ) {
				start = null;
			}
		}
		return;
	}

	/** @return the final recharge amount after the last edge */
	public double getFinalRechargeAmount() {
		if( path.isEmpty() ) {
			return 0.0;
		}
		return path.get(path.size() - 1).rechargeAmount;
	}
	
	/** @return the amount of time for the first path element, in hours, if it's a regular transit */
	public double getStartTime() {
		if( !path.isEmpty() ) {
			Edge firstEdge = path.get(0);
			return firstEdge.type == EdgeType.TRAVEL
					? firstEdge.source.getTravelTimeTo(firstEdge.target) : 0.0;
		}
		return 0.0;
	}
	
	/** @return the amount of time for the last path element, in hours, if it's a regular transit */
	public double getEndTime() {
		if( !path.isEmpty() ) {
			Edge lastEdge = path.get(path.size() - 1);
			return lastEdge.type == EdgeType.TRAVEL
					? lastEdge.source.getTravelTimeTo(lastEdge.target) : 0.0;
		}
		return 0.0;
	}
	
	/** @return total recharge time needed in terran days */
	public double getTotalRechargeTime() {
		double rechargeTime = 0;
		for( Edge e : path ) {
			if( e.type == EdgeType.RECHARGE ) {
				rechargeTime += e.source.getRechargeTime();
			}
		}
		return rechargeTime / 24.0;
	}

	/** @return amount of jumps needed for this path */
	public int getJumps() {
		int jumps = 0;
		for( Edge e : path ) {
			if( e.type == EdgeType.JUMP ) {
				++ jumps;
			}
		}
		return jumps;
	}
	
	/** @return total travel time in terran days */
	public double getTotalTime(double currentTransit) {	
		double totalTime = 0;
		for( Edge e : path ) {
			switch( e.type ) {
				case TRAVEL:
					totalTime += e.source.getTravelTimeTo(e.target);
					break;
				case RECHARGE:
					totalTime += e.source.getRechargeTime();
					break;
				case JUMP:
					// TODO - make the jump time dependent on the skill level of the crew
					totalTime += 1.0;
					break;
				default:
					// Nothing to do here yet
			}
		}
		return totalTime / 24.0;
	}
	
	/** Add a location on the surface of the planet */
	public boolean addLocation(Planet p) {
		return addLocation(p.getPointOnSurface());
	}
	
	/** Add one of the jump points of the given star */
	public boolean addLocation(Star s) {
		return addLocation(s.getPreferredJumpPoint());
	}

	/** Add a location to the end of the path. Includes a recharge action as necessary. */
	public boolean addLocation(SpaceLocation l) {
		if( null == l ) {
			return false;
		}
		if( null == start ) {
			start = l;
			return true;
		}
		SpaceLocation lastPoint = getLastPlanet();
		if( lastPoint.equals(l) ) {
			// Why?
			return false;
		}
		if( lastPoint.inSameSystemAs(l) ) {
			// Traditional transfer
			path.add(Edge.createTransfer(lastPoint, l, getFinalRechargeAmount()));
			return true;
		}
		// Else try a jump
		boolean rechargeNeeded = (getFinalRechargeAmount() <= 1.0);
		Edge rechargeEdge = null;
		if( rechargeNeeded ) {
			rechargeEdge = Edge.createRecharge(getLastPlanet());
			if( null == rechargeEdge ) {
				return false;
			}
		}
		Edge jumpEdge = Edge.createJump(getLastPlanet(), l);
		if( null != jumpEdge && (null != rechargeEdge || !rechargeNeeded) ) {
			if( rechargeNeeded ) {
				path.add(rechargeEdge);
			}
			path.add(jumpEdge);
			return true;
		}
		return false;
	}
	
	/*
	public void addPlanets(ArrayList<Planet> planets) {
		path.addAll(planets);
	}
	*/
	
	/**
	 * Append this path to the current one. If the start and end locations don't math, try
	 * to insert a simple edge between them.
	 * <p>
	 * No validation of the appended path is being made.
	 * 
	 * @return true if the path was successfully appended, false otherwise.
	 */
	public boolean appendPath(JumpPath other) {
		// If we're empty, just replace
		if( null == start ) {
			start = other.start;
			path = new ArrayList<Edge>(other.path);
			return true;
		}
		// Else, try to stitch it together
		if( !getLastPlanet().equals(other.start) ) {
			if( !addLocation(other.start) ) {
				return false;
			}
		}
		path.addAll(other.path);
		return true;
	}
	
	public void removeFirstPlanet() {
		if(!path.isEmpty()) {
			start = path.get(0).target;
			path.remove(0);
		} else {
			start = null;
		}
	}
	
	/** @return the amount of path edges */
	public int size() {
		return path.size();
	}
	
	/** @return the space location at a given index, from 0 to size() */
	public SpaceLocation get(int i) {
		if( i < 0 || i > path.size() ) {
			return null;
		} else if( i == 0 ) {
			return start;
		} else {
			return path.get(i - 1).target;
		}
	}
	
	public boolean isFirstEdgeType(EdgeType type) {
		if( path.isEmpty() ) {
			return false;
		}
		return path.get(0).type == type;
	}

	public boolean contains(Star star) {
		if( null == star ) {
			return false;
		}
		for( Edge e : path ) {
			if( star.equals(e.target.getStar()) ) {
				return true;
			}
		}
		return star.equals(start.getStar());
	}
	
	public void writeToXml(PrintWriter pw1, int indent) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(JumpPath.class, JumpPath.Edge.class, SpaceLocation.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
			marshaller.marshal(this, pw1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static JumpPath generateInstanceFromXML(Node wn, Campaign c) {
		try {		
			JAXBContext jaxbContext = JAXBContext.newInstance(JumpPath.class, JumpPath.Edge.class, SpaceLocation.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			unmarshaller.setProperty("jaxb.fragment", Boolean.TRUE);
			return unmarshaller.unmarshal(wn, JumpPath.class).getValue();
		} catch (Exception ex) {
			MekHQ.logError(ex);
		}
		return null;
	}
	
	/**
	 * A jump path edge, encapsulating data like the source and target location (SpaceLocation),
	 * the type of edge/mode of travel (conventional burn, jumping, recharging), and possibly more.
	 */
	public static final class Edge implements Serializable {
		private static final long serialVersionUID = -5889539269632520576L;
		
		@XmlJavaTypeAdapter(SpaceLocationAdapter.class)
		public SpaceLocation source;
		@XmlJavaTypeAdapter(SpaceLocationAdapter.class)
		public SpaceLocation target;
		public double rechargeAmount = 0.0;
		public EdgeType type;

		public static Edge createJump(SpaceLocation start, SpaceLocation end) {
			if( null != start && null != end && start.canJumpTo(end) ) {
				return new Edge(start, end, EdgeType.JUMP, 0.0);
			}
			return null;
		}
		
		public static Edge createRecharge(SpaceLocation at) {
			if( null != at && !Double.isInfinite(at.getRechargeTime()) ) {
				return new Edge(at, at, EdgeType.RECHARGE, 1.0);
			}
			return null;
		}
		
		public static Edge createTransfer(SpaceLocation start, SpaceLocation end, double currentRechargeAmount) {
			if( null != start && null != end && start.inSameSystemAs(end) ) {
				return new Edge(start, end, EdgeType.TRAVEL, currentRechargeAmount);
			}
			return null;
		}

		private Edge(SpaceLocation source, SpaceLocation target, EdgeType type, double rechargeAmount) {
			this.source = source;
			this.target = target;
			this.type = type;
			this.rechargeAmount = rechargeAmount;
		}
	
		/** @return human-readable description */
		public String getDesc(Date when) {
			switch(type) {
				case TRAVEL:
					return "Travelling from (" + source.getDesc(when) + ") to (" + target.getDesc(when) + "), "
							+ "default travel time " + (source.getTravelTimeTo(target) / 24) + " days";
				case RECHARGE:
					return "Recharging at (" + source.getDesc(when) + ") for " + (source.getRechargeTime()/24) + " days";
				case JUMP:
					return "Jumping from (" + source.getDesc(when) + ") to (" + target.getDesc(when) + ")";
				default:
					return "Undefined action";
			}
		}
	}
	
	public static enum EdgeType {
		WAIT, TRAVEL, JUMP, RECHARGE;
	}

}