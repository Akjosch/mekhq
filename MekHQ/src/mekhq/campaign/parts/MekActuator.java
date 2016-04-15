/*
 * MekActuator.java
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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import megamek.common.BipedMech;
import megamek.common.Compute;
import megamek.common.CriticalSlot;
import megamek.common.EquipmentType;
import megamek.common.Mech;
import megamek.common.TechConstants;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.personnel.SkillType;
import mekhq.campaign.unit.Unit;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MekActuator extends Part {
	private static final long serialVersionUID = 719878556021696393L;
	protected int type;

	public MekActuator() {
		this(0, 0, null);
	}
	
	@Override
    public MekActuator clone() {
		MekActuator clone = new MekActuator(get(Installable.class).getUnitTonnage(), type, get(Installable.class).getMainLocation(), campaign);
        clone.copyBaseData(this);
		return clone;
	}
	
    public int getType() {
        return type;
    }
    
    public MekActuator(int tonnage, int type, Campaign c) {
        this(tonnage, type, Mech.LOC_NONE, c);
    }
    
    public MekActuator(double tonnage, int type, int loc, Campaign c) {
    	super(c);
        this.type = type;
        Mech m = new BipedMech();
        this.name = m.getSystemName(type) + " Actuator" ;
        add(new Installable());
        get(Installable.class).setLocations(loc);
        get(Installable.class).setUnitTonnage(tonnage);
        get(Installable.class).setTonnageLimited(true);
    }

    @Override
    public double getTonnage() {
    	//TODO: how much do actuators weight?
    	//apparently nothing
    	return 0;
    }
    
    @Override
    public long getStickerPrice() {
        long unitCost = 0;
        switch (getType()) {
            case (Mech.ACTUATOR_UPPER_ARM) : {
                unitCost = 100;
                break;
            }
            case (Mech.ACTUATOR_LOWER_ARM) : {
                unitCost = 50;
                break;
            }
            case (Mech.ACTUATOR_HAND) : {
                unitCost = 80;
                break;
            }
            case (Mech.ACTUATOR_UPPER_LEG) : {
                unitCost = 150;
                break;
            }
            case (Mech.ACTUATOR_LOWER_LEG) : {
                unitCost = 80;
                break;
            }
            case (Mech.ACTUATOR_FOOT) : {
                unitCost = 120;
                break;
            }
            case (Mech.ACTUATOR_HIP) : {
                // not used
                unitCost = 0;
                break;
            }
            case (Mech.ACTUATOR_SHOULDER) : {
                // not used
                unitCost = 0;
                break;
            }
        }
        return Math.round(get(Installable.class).getUnitTonnage() * unitCost);
    }

    @Override
    public boolean isSamePartType (Part part) {
        return part instanceof MekActuator
                && getType() == ((MekActuator)part).getType()
                && get(Installable.class).getUnitTonnage() == part.get(Installable.class).getUnitTonnage();
    }
    
	@Override
	protected void loadFieldsFromXmlNode(Node wn) {
		NodeList nl = wn.getChildNodes();
		
		for (int x=0; x<nl.getLength(); x++) {
			Node wn2 = nl.item(x);
			
			if (wn2.getNodeName().equalsIgnoreCase("type")) {
				type = Integer.parseInt(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("location")) {
			    get(Installable.class).setLocations(Integer.parseInt(wn2.getTextContent()));
			} 
		}
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
	public void fix() {
		super.fix();
		Unit unit = get(Installable.class).getUnit();
		if(null != unit) {
			unit.repairSystem(CriticalSlot.TYPE_SYSTEM, type, get(Installable.class).getMainLocation());
		}
	}
	
	@Override
	public int getTechLevel() {
		return TechConstants.T_ALLOWED_ALL;
	}

	@Override
	public MissingPart getMissingPart() {
		return new MissingMekActuator(get(Installable.class).getUnitTonnage(), type, get(Installable.class).getMainLocation(), campaign);
	}

	@Override
	public void remove(boolean salvage) {
        Unit unit = get(Installable.class).getUnit();
		if(null != unit) {
			unit.destroySystem(CriticalSlot.TYPE_SYSTEM, type, get(Installable.class).getMainLocation());
			Part spare = campaign.checkForExistingSparePart(this);
			if(!salvage) {
				campaign.removePart(this);
			} else if(null != spare) {
				spare.incrementQuantity();
				campaign.removePart(this);
			}
			unit.removePart(this);
			Part missing = getMissingPart();
			unit.addPart(missing);
			campaign.addPart(missing, 0);
		}	
		get(Installable.class).setUnit(null);
		updateConditionFromEntity(false);
		get(Installable.class).setLocations(Mech.LOC_NONE);
	}

	@Override
	public void updateConditionFromEntity(boolean checkForDestruction) {
		int priorHits = hits;
        Unit unit = get(Installable.class).getUnit();
		if(null != unit) {
			//check for missing equipment
		    int location = get(Installable.class).getMainLocation();
			if(unit.isSystemMissing(type, location)) {
				remove(false);
				return;
			}
			hits = unit.getEntity().getDamagedCriticals(CriticalSlot.TYPE_SYSTEM, type, location);
			if(checkForDestruction 
					&& hits > priorHits 
					&& Compute.d6(2) < campaign.getCampaignOptions().getDestroyPartTarget()) {
				remove(false);
				return;
			}
		}
	}
	
	@Override 
	public int getBaseTime() {
		if(isSalvaging()) {
			return 90;
		}
		return 120;
	}
	
	@Override
	public int getDifficulty() {
		if(isSalvaging()) {
			return -3;
		}
		return 0;
	}

	@Override
	public boolean needsFixing() {
		return hits > 0;
	}
	
	@Override
	public String getDetails() {
        Unit unit = get(Installable.class).getUnit();
		if(null != unit) {
			return unit.getEntity().getLocationName(get(Installable.class).getMainLocation());
		}
		return get(Installable.class).getUnitTonnage() + " tons";
	}

	@Override
	public void updateConditionFromPart() {
        Unit unit = get(Installable.class).getUnit();
		if(null != unit) {
			if(hits > 0) {
				unit.damageSystem(CriticalSlot.TYPE_SYSTEM, type, get(Installable.class).getMainLocation(), 1);
			} else {
				unit.repairSystem(CriticalSlot.TYPE_SYSTEM, type, get(Installable.class).getMainLocation());
			}
		}	
	}
	
	@Override
	public String checkFixable() {
        Unit unit = get(Installable.class).getUnit();
		if(null == unit) {
			return null;
		}
		if(isSalvaging()) {
			return null;
		}
		int location = get(Installable.class).getMainLocation();
		if(unit.isLocationBreached(location)) {
			return unit.getEntity().getLocationName(location) + " is breached.";
		}
		if(get(Installable.class).isMountedOnDestroyedLocation()) {
			return unit.getEntity().getLocationName(location) + " is destroyed.";
		}
		return null;
	}
	
	@Override
	public boolean onBadHipOrShoulder() {
        Unit unit = get(Installable.class).getUnit();
		return null != unit && unit.hasBadHipOrShoulder(get(Installable.class).getMainLocation());
	}
	
	@Override
	public boolean isPartForEquipmentNum(int index, int loc) {
		return index == type && loc == get(Installable.class).getMainLocation();
	}
	
	@Override
	public boolean isRightTechType(String skillType) {
		return skillType.equals(SkillType.S_TECH_MECH);
	}
	
	@Override
	public boolean isOmniPoddable() {
		return type == Mech.ACTUATOR_LOWER_ARM || type == Mech.ACTUATOR_HAND;
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
	
}
