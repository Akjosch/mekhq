/*
 * MissingMASC.java
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

import megamek.common.EquipmentType;
import megamek.common.MiscType;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.Part;
import mekhq.campaign.unit.Unit;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingMASC extends MissingEquipmentPart {
	private static final long serialVersionUID = 2892728320891712304L;

	protected int engineRating;
	
	public MissingMASC() {
    	this(0, null, -1, null, 0, 0);
    }
    
    public MissingMASC(int tonnage, EquipmentType et, int equipNum, Campaign c, double etonnage, int rating) {
        super(tonnage, et, equipNum, c, etonnage);
        this.engineRating = rating;
    }
 
    @Override
    public void setUnit(Unit u) {
    	super.setUnit(u);
    	if(null != unit && null != unit.getEntity().getEngine()) {
    		engineRating = unit.getEntity().getEngine().getRating();
    	}
    }
    
    @Override
    public long getStickerPrice() {
    	if (isSupercharger()) {
    		return engineRating * 10000;
    	} else {           
            return (long)(engineRating * getTonnage() * 1000);
        }
    }
    
    public int getEngineRating() {
    	return engineRating;
    }
    
	@Override
	protected void loadFieldsFromXmlNode(Node wn) {
		NodeList nl = wn.getChildNodes();
		
		for (int x=0; x<nl.getLength(); x++) {
			Node wn2 = nl.item(x);
			if (wn2.getNodeName().equalsIgnoreCase("equipmentNum")) {
				equipmentNum = Integer.parseInt(wn2.getTextContent());
			}
			else if (wn2.getNodeName().equalsIgnoreCase("typeName")) {
				typeName = wn2.getTextContent();
			}
			else if (wn2.getNodeName().equalsIgnoreCase("equipTonnage")) {
				equipTonnage = Double.parseDouble(wn2.getTextContent());
			}
			else if (wn2.getNodeName().equalsIgnoreCase("engineRating")) {
				engineRating = Integer.parseInt(wn2.getTextContent());
			}
		}
		restore();
	}
	
	@Override
	public boolean isAcceptableReplacement(Part part, boolean refit) {
		if(part instanceof MASC) {
			EquipmentPart eqpart = (EquipmentPart)part;
			EquipmentType et = eqpart.getType();
			return type.equals(et) && getTonnage() == part.getTonnage()
					&& ((MASC)part).getEngineRating() == engineRating;
		}
		return false;
	}
	
	private boolean isSupercharger() {
		return type.hasSubType(MiscType.S_SUPERCHARGER);
	}
	
	@Override
	public Part getNewPart() {
		MASC epart = new MASC(getUnitTonnage(), type, -1, campaign, engineRating);
		epart.setEquipTonnage(equipTonnage);
		return epart;
	}
	
	@Override
    public boolean isOmniPoddable() {
    	return false;
    }
}