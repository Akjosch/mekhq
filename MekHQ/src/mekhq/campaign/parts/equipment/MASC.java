/*
 * MASC.java
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

import java.util.GregorianCalendar;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import megamek.common.EquipmentType;
import megamek.common.MiscType;
import megamek.common.TechConstants;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.MissingPart;
import mekhq.campaign.parts.Part;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.unit.Unit;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MASC extends EquipmentPart {
	private static final long serialVersionUID = 2892728320891712304L;

	protected int engineRating;
	
	public MASC() {
    	this(0, null, -1, null, 0);
    }
    
    public MASC(double tonnage, EquipmentType et, int equipNum, Campaign c, int rating) {
        super(tonnage, et, equipNum, c);
        this.engineRating = rating;
        equipTonnage = calculateTonnage();
    }
    
    @Override
    public MASC clone() {
    	MASC clone = new MASC(get(Installable.class).getUnitTonnage(), getType(), getEquipmentNum(), campaign, engineRating);
        clone.copyBaseData(this);
    	return clone;
    }
 
    @Override
    public void setUnit(Unit u) {
    	super.setUnit(u);
    	if(null != unit && null != unit.getEntity().getEngine()) {
    		engineRating = unit.getEntity().getEngine().getRating();
    	}
    }
    
    private double calculateTonnage() {
    	if(null == type) {
    		return 0;
    	}
    	//supercharger tonnage will need to be set by hand in parts store
        if (TechConstants.isClan(type.getTechLevel(campaign.getCalendar().get(GregorianCalendar.YEAR)))) {
            return Math.round(getUnitTonnage() / 25.0f);
        }
        return Math.round(getUnitTonnage() / 20.0f);
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
    
    private boolean isSupercharger() {
    	return type.hasSubType(MiscType.S_SUPERCHARGER);
    }
    
    
    @Override
    public boolean isSamePartTypeAndStatus (Part part) {
    	if(needsFixing() || part.needsFixing()) {
    		return false;
    	}
        return part instanceof MASC
        		&& getType().equals(((EquipmentPart)part).getType())
        		&& getTonnage() == part.getTonnage()
        		&& getEngineRating() == ((MASC)part).getEngineRating();
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
	public MissingPart getMissingPart() {
		return new MissingMASC(get(Installable.class).getUnitTonnage(), type, equipmentNum, campaign, equipTonnage, engineRating);
	}
	
	@Override
	public String getDetails() {
		if(null != unit) {
			return super.getDetails();
		}
		if(isSupercharger()) {
			return super.getDetails() + ", " + getEngineRating() + " rating";
		}
		return super.getDetails() + ", " + get(Installable.class).getUnitTonnage() + " tons, " + getEngineRating() + " rating";
	 }
	
	@Override
    public boolean isOmniPoddable() {
    	return false;
    }
}