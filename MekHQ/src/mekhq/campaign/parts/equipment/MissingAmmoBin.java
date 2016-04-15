/*
 * MissingAmmoBin.java
 * 
 * Copyright (c) 2009 Jay Lawson <jaylawson39 at yahoo.com>. All rights reserved.
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

package mekhq.campaign.parts.equipment;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import megamek.common.AmmoType;
import megamek.common.EquipmentType;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.Part;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.unit.Unit;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingAmmoBin extends MissingEquipmentPart {
	private static final long serialVersionUID = 2892728320891712304L;

	protected boolean oneShot;
	
    public MissingAmmoBin() {
    	this(null, -1, false, null);
    }
    
    public MissingAmmoBin(EquipmentType et, int equipNum, boolean singleShot, Campaign c) {
        super(0.0, et, equipNum, c, 1);
        this.oneShot = singleShot;
        if(null != name) {
        	this.name += " Bin";
        }
    }
	
	@Override
	public int getDifficulty() {
		return -2;
	}

	@Override 
	public void fix() {
		Part replacement = findReplacement(false);
        Unit unit = get(Installable.class).getUnit();
		if(null != replacement && null != unit) {
			Part actualReplacement = replacement.clone();
			unit.addPart(actualReplacement);
			campaign.addPart(actualReplacement, 0);
			replacement.decrementQuantity();
			((EquipmentPart)actualReplacement).setEquipmentNum(equipmentNum);
			remove(false);
			//assign the replacement part to the unit
			actualReplacement.updateConditionFromPart();
		}
	}
	
	@Override
	public boolean isAcceptableReplacement(Part part, boolean refit) {
		if(part instanceof AmmoBin) {
			EquipmentPart eqpart = (EquipmentPart)part;
			EquipmentType et = eqpart.getType();
			return type.equals(et) && ((AmmoBin)part).isOneShot() == oneShot;
		}
		return false;
	}
	
	public boolean isOneShot() {
		return oneShot;
	}

	private int getFullShots() {
    	int fullShots = ((AmmoType)type).getShots();
		if(oneShot) {
			fullShots = 1;
		}
		return fullShots;
    }
	
	@Override
	public Part getNewPart() {
		return new AmmoBin(type, -1, getFullShots(), oneShot, campaign);
	}
	
	@Override
	protected void loadFieldsFromXmlNode(Node wn) {
		super.loadFieldsFromXmlNode(wn);
		NodeList nl = wn.getChildNodes();
		
		for (int x=0; x<nl.getLength(); x++) {
			Node wn2 = nl.item(x);
			if (wn2.getNodeName().equalsIgnoreCase("equipmentNum")) {
				equipmentNum = Integer.parseInt(wn2.getTextContent());
			}
			else if (wn2.getNodeName().equalsIgnoreCase("typeName")) {
				typeName = wn2.getTextContent();
			} else if (wn2.getNodeName().equalsIgnoreCase("daysToWait")) {
                daysToWait = Integer.parseInt(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("oneShot")) {
				if(wn2.getTextContent().equalsIgnoreCase("true")) {
					oneShot = true;
				} else {
					oneShot = false;
				}
			} 
		}
		restore();
	}
}
