/*
 * CurrentLocation.java
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
import java.util.Date;

import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.finances.Transaction;
import mekhq.campaign.universe.Planet;
import mekhq.campaign.universe.SpaceLocation;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This keeps track of a location, which includes both the planet
 * and the current position in-system. It may seem a little like
 * overkill to have a separate object here, but when we reach a point
 * where we want to let a force be in different locations, this will
 * make it easier to keep track of everything
 * 
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class CurrentLocation implements Serializable {
	private static final long serialVersionUID = -4337642922571022697L;
	
	private SpaceLocation currentLocation;
	//keep track of jump path
	private JumpPath jumpPath;
	/** Time spent during current recharge, in hours */
	private double rechargeTime;
	/** Time spent in current transit, in hours */
	private double transitTime;
	
	public CurrentLocation() {
		this((SpaceLocation)null, 0);
	}
	
	public CurrentLocation(Planet p, double time) {
		this(p.getPointOnSurface(), time);
	}
	
	public CurrentLocation(SpaceLocation loc) {
		this(loc, 0.0);
	}
	
	public CurrentLocation(SpaceLocation loc, double time) {
		this.currentLocation = loc;
		this.transitTime = time;
		this.rechargeTime = 0.0;
	}
	
	public void setCurrentLocation(SpaceLocation loc) {
		currentLocation = loc;
	}
	
	public void setTransitTime(double time) {
		transitTime = time;
	}
	
	public boolean isOnPlanet() {
		return null != currentLocation && currentLocation.isOnSurface();
	}
	
	public boolean isAtJumpPoint() {
		return null != currentLocation && currentLocation.isJumpPoint();
	}
	
	public boolean isInTransit() {
		return !isOnPlanet() && !isAtJumpPoint();
	}
	
	public SpaceLocation getCurrentLocation() {
		return currentLocation;
	}
	
	public double getTransitTime() {
		return transitTime;
	}
	
	public String getReport(Date date) {
		String toReturn = "<b>Current Location</b><br>";
		toReturn += currentLocation.getStar().getShortName(date) + "<br> ";
		if(null != jumpPath && !jumpPath.isEmpty()) {
			toReturn += "In transit to " + jumpPath.getLastPlanet().getDesc(date) + " ";
		}
		if(isOnPlanet()) {
			toReturn += "<i>On Planet</i>";
		} 
		else if(isAtJumpPoint()) {
			toReturn += "<i>At Jump Point</i>";
		} else {
			toReturn += "<i>" + Math.round(100.0*getTransitTime()/24.0)/100.0 + " days out </i>";
		}
		toReturn += ", <i>" + Math.round(100.0*rechargeTime/currentLocation.getRechargeTime()) + "% charged</i>";
		return "<html>" + toReturn + "</html>";
	}
	
	public JumpPath getJumpPath() {
		return jumpPath;
	}
	
	public void setJumpPath(JumpPath path) {
		jumpPath = path;
	}
	
	/**
	 * Check for a jump path and if found, do whatever needs to be done to move 
	 * forward
	 */
	public void newDay(final Campaign campaign) {
		newDay(campaign, 24.0);
	}
	
	public void newDay(final Campaign campaign, final double hours) {
		//recharge even if there is no jump path
		//because jumpships don't go anywhere
		// TODO: Keep a list of jump capable ships and where they are.
		double usedRechargeTime = Math.min(hours, currentLocation.getRechargeTime() - rechargeTime);
		if( usedRechargeTime > 0 ) {
			campaign.addReport("Jumpships spent " + Math.round(100.0 * usedRechargeTime)/100.0 + " hours recharging drives");
			rechargeTime += usedRechargeTime;
			if(rechargeTime >= currentLocation.getRechargeTime()) {
				campaign.addReport("Jumpship drives full charged");
			}
		}
		if( null == jumpPath || jumpPath.isEmpty() ) {
			return;
		}

		double usedTime = 0.0;
		while( !jumpPath.isEmpty() && usedTime < hours ) {
			SpaceLocation target = jumpPath.get(1);
			if( jumpPath.isFirstEdgeType(JumpPath.EdgeType.TRAVEL) ) {
				// We're doing a standard in-system burn. See if we reach the destination.
				double travelTime = Math.min(currentLocation.getTravelTimeTo(target) - transitTime, hours);
				if( travelTime > 0 ) {
					transitTime += travelTime;
					campaign.addReport("Dropships spent " + Math.round(100.0 * travelTime)/100.0
							+ " hours in transit to " + target.getDesc(campaign.getDate()));
				}
				if( travelTime <= hours - usedTime ) {
					// We made it within the alloted time window
					jumpPath.removeFirstPlanet();
					currentLocation = jumpPath.getFirstPlanet();
					campaign.addReport("Destination point reached");
					transitTime = 0.0;
				}
				usedTime += travelTime;
			} else if( jumpPath.isFirstEdgeType(JumpPath.EdgeType.RECHARGE) ) {
				usedTime = Math.max(usedTime, usedRechargeTime);
				if( rechargeTime >= currentLocation.getRechargeTime() ) {
					jumpPath.removeFirstPlanet();
					currentLocation = jumpPath.getFirstPlanet();
					campaign.addReport("Ready to jump to " + target.getDesc(campaign.getDate()));
				} else {
					//campaign.addReport("Waiting for Jumpship recharge");
				}
			} else if( jumpPath.isFirstEdgeType(JumpPath.EdgeType.JUMP) ) {
				double jumpTime = Math.min(hours - usedTime, 1.0); // 1h for jump preparations
				transitTime += jumpTime;
				usedTime += jumpTime;
				if( transitTime >= 1.0 ) {
					if(campaign.getCampaignOptions().payForTransport()) {
						if( !campaign.getFinances().debit(
								campaign.calculateCostPerJump(true),
								Transaction.C_TRANSPORT,
								"jump from " + currentLocation.getDesc(campaign.getDate()) + " to "
								+ target.getDesc(campaign.getDate()), campaign.getCalendar().getTime()) ) {
						    campaign.addReport("<font color='red'><b>You cannot afford to make the jump!</b></font>");
						    transitTime = 0.0; // We'll need to reschedule the jump once we can pay
						    return;
						}
					}
					jumpPath.removeFirstPlanet();
					currentLocation = jumpPath.getFirstPlanet();
	                campaign.addReport("Jumping to " + target.getStar().getName(campaign.getDate()));
	                transitTime = 0.0;
					rechargeTime = 0.0;
				}
			}
		}
		
		if( jumpPath.isEmpty() ) {
			// We're done traveling
			jumpPath = null;
		}
	}
	
	public void writeToXml(PrintWriter pw1, int indent) {
		pw1.println(MekHqXmlUtil.indentStr(indent) + "<location>");
		pw1.println(MekHqXmlUtil.indentStr(indent+1)
				+"<currentPlanetName>"
				+MekHqXmlUtil.escape(currentLocation.getName())
				+"</currentPlanetName>");
		pw1.println(MekHqXmlUtil.indentStr(indent+1)
				+"<transitTime>"
				+transitTime
				+"</transitTime>");
		pw1.println(MekHqXmlUtil.indentStr(indent+1)
				+"<rechargeTime>"
				+rechargeTime
				+"</rechargeTime>");
		if(null != jumpPath) {
			jumpPath.writeToXml(pw1, indent+1);
		}
		pw1.println(MekHqXmlUtil.indentStr(indent) + "</location>");
		
	}
	
	public static CurrentLocation generateInstanceFromXML(Node wn, Campaign c) {
		CurrentLocation retVal = null;
		
		try {		
			retVal = new CurrentLocation();
			NodeList nl = wn.getChildNodes();
			
			for (int x=0; x<nl.getLength(); x++) {
				Node wn2 = nl.item(x);
				if (wn2.getNodeName().equalsIgnoreCase("currentPlanetName")) {
					Planet p = c.getPlanet(wn2.getTextContent());
					if(null == p) {
						//whoops we cant find your planet man, back to Earth
						MekHQ.logError("Couldn't find planet named " + wn2.getTextContent());
						p = c.getPlanet("Terra");
						if(null == p) {
							//if that doesnt work then give the first planet we have
							p = c.getPlanets().get(0);
						}
					}
					retVal.currentLocation = p.getPointOnSurface();
				} else if (wn2.getNodeName().equalsIgnoreCase("transitTime")) {
					retVal.transitTime = Double.parseDouble(wn2.getTextContent());
				} else if (wn2.getNodeName().equalsIgnoreCase("rechargeTime")) {
					retVal.rechargeTime = Double.parseDouble(wn2.getTextContent());
				} else if (wn2.getNodeName().equalsIgnoreCase("jumpPath")) {
					retVal.jumpPath = JumpPath.generateInstanceFromXML(wn2, c);
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