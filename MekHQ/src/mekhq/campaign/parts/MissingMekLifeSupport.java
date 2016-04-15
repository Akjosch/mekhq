/*
 * MissingMekLifeSupport.java
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

package mekhq.campaign.parts;

import megamek.common.CriticalSlot;
import megamek.common.Entity;
import megamek.common.EquipmentType;
import megamek.common.Mech;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.unit.Unit;

import org.w3c.dom.Node;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingMekLifeSupport extends MissingPart {
	private static final long serialVersionUID = -1989526319692474127L;

	public MissingMekLifeSupport() {
		this(null);
	}
	
	public MissingMekLifeSupport(Campaign c) {
        super(c);
        this.name = "Mech Life Support System";
    }
	
	@Override 
	public int getBaseTime() {
		return 180;
	}
	
	@Override
	public int getDifficulty() {
		return -1;
	}

	@Override
	public double getTonnage() {
		//TODO: what should this tonnage be?
		return 0;
	}

	@Override
	protected void loadFieldsFromXmlNode(Node wn) {
		// Do nothing - no fields to load.
	}

	@Override
	public int getAvailability(int era) {
		return EquipmentType.RATING_C;
	}

	@Override
	public int getTechRating() {
		return EquipmentType.RATING_C;
	}

	@Override
	public boolean isAcceptableReplacement(Part part, boolean refit) {
		return part instanceof MekLifeSupport;
	}
	
	 
    @Override
    public String checkFixable() {
        Entity entity = get(Installable.class).getEntity();
    	if(null == entity) {
    		return null;
    	}
        for(int i = 0; i < entity.locations(); i++) {
        	if(entity.getNumberOfCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_LIFE_SUPPORT, i) > 0) {
            	if(get(Installable.class).getUnit().isLocationBreached(i)) {
            		return entity.getLocationName(i) + " is breached.";
            	}
            	if(get(Installable.class).getUnit().isLocationDestroyed(i)) {
            		return entity.getLocationName(i) + " is destroyed.";
            	}
            }
        }
        return null;
    }

	@Override
	public Part getNewPart() {
		return new MekLifeSupport(campaign);
	}

	@Override
	public void updateConditionFromPart() {
        Unit unit = get(Installable.class).getUnit();
		if(null != unit) {
			unit.destroySystem(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_LIFE_SUPPORT);
		}
	}

	@Override
	public int getLocation() {
		if(null != unit) {
			Entity entity = unit.getEntity();
			for (int i = 0; i < entity.locations(); i++) {
				if (entity.getNumberOfCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_LIFE_SUPPORT, i) > 0) {
					return i;
				}
			}
		}
		return Entity.LOC_NONE;
	}
	
	@Override
	public int getIntroDate() {
		return EquipmentType.DATE_NONE;
	}

	@Override
	public int getExtinctDate() {
		return EquipmentType.DATE_NONE;
	}

	@Override
	public int getReIntroDate() {
		return EquipmentType.DATE_NONE;
	}
	
	@Override
    public boolean isInLocation(String loc) {
		 if(null == unit || null == unit.getEntity() || !(unit.getEntity() instanceof Mech)) {
			 return false;
		 }
		 if(((Mech)unit.getEntity()).getCockpitType() == Mech.COCKPIT_TORSO_MOUNTED) {
			 if(unit.getEntity().getLocationFromAbbr(loc) == Mech.LOC_LT
					 || unit.getEntity().getLocationFromAbbr(loc) == Mech.LOC_RT) {
				 return true;
			 }
		 } else if (unit.getEntity().getLocationFromAbbr(loc) == Mech.LOC_HEAD) {
             return true;
         }
		 return false;	
    }
	
}
