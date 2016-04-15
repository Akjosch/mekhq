/*
 * TankLocation.java
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

import megamek.common.CriticalSlot;
import megamek.common.Entity;
import megamek.common.EquipmentType;
import megamek.common.IArmorState;
import megamek.common.ILocationExposureStatus;
import megamek.common.Mounted;
import megamek.common.Tank;
import megamek.common.TargetRoll;
import megamek.common.TechConstants;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.SkillType;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class TankLocation extends Part {
	private static final long serialVersionUID = -122291037522319765L;
	protected int damage;
	protected boolean breached;

    public TankLocation() {
    	this(0, 0, null);
    }
    
    protected TankLocation(Installable installable, Campaign campaign) {
        this(installable.getMainLocation(), installable.getUnitTonnage(), campaign);
    }
    
    public TankLocation clone() {
    	TankLocation clone = new TankLocation(get(Installable.class), campaign);
        clone.copyBaseData(this);
    	clone.damage = this.damage;
    	clone.breached = this.breached;
    	return clone;
    }
    
    public TankLocation(int loc, int tonnage, Campaign c) {
        super(c);
        this.damage = 0;
        this.breached = false;
        this.name = "Tank Location";
        switch(loc) {
            case(Tank.LOC_FRONT):
                this.name = "Vehicle Front";
                break;
            case(Tank.LOC_LEFT):
                this.name = "Vehicle Left Side";
                break;
            case(Tank.LOC_RIGHT):
                this.name = "Vehicle Right Side";
                break;
            case(Tank.LOC_REAR):
                this.name = "Vehicle Rear";
                break;
        }
        add(new Installable());
        get(Installable.class).setLocations(loc);
        get(Installable.class).setUnitTonnage(tonnage);
        get(Installable.class).setTonnageLimited(true);
        computeCost();
    }
    
    protected void computeCost () {
    	//TODO: implement
    }

    @Override
    public boolean isSamePartType(Part part) {
        return part instanceof TankLocation 
        		&& get(Installable.class).getMainLocation() == part.get(Installable.class).getMainLocation()
        		&& get(Installable.class).getUnitTonnage() == part.get(Installable.class).getUnitTonnage();
    }	
    
    @Override
    public boolean isSameStatus(Part part) {
    	return super.isSameStatus(part) && this.getDamage() == ((TankLocation)part).getDamage();
    }

    public int getDamage() {
    	return damage;
    }

	@Override
	protected void loadFieldsFromXmlNode(Node wn) {
		NodeList nl = wn.getChildNodes();
		
		for (int x=0; x<nl.getLength(); x++) {
			Node wn2 = nl.item(x);
			
			if (wn2.getNodeName().equalsIgnoreCase("loc")) {
		        get(Installable.class).setLocations(Integer.parseInt(wn2.getTextContent()));
			} else if (wn2.getNodeName().equalsIgnoreCase("damage")) {
				damage = Integer.parseInt(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("breached")) {
				if (wn2.getTextContent().equalsIgnoreCase("true"))
					breached = true;
				else
					breached = false;
			} 
		}
	}

	@Override
	public int getAvailability(int era) {
		return EquipmentType.RATING_A;
	}

	@Override
	public int getTechRating() {
		return EquipmentType.RATING_B;
	}

    @Override
	public int getTechLevel() {
		return TechConstants.T_ALLOWED_ALL;
	}

	@Override
	public void fix() {
		super.fix();
        Entity entity = get(Installable.class).getEntity();
        int loc = get(Installable.class).getMainLocation();
		if(isBreached()) {
			breached = false;
			entity.setLocationStatus(loc, ILocationExposureStatus.NORMAL, true);
			for (int i = 0; i < entity.getNumberOfCriticals(loc); i++) {
	            CriticalSlot slot = entity.getCritical(loc, i);
	            // ignore empty & non-hittable slots
	            if (slot == null) {
	                continue;
	            }
	            slot.setBreached(false);
	            Mounted m = slot.getMount();
	            if(null != m) {
	            	m.setBreached(false);
	            }
			}
		} else {
		    damage = 0;
		    if(null != entity) {
			    entity.setInternal(entity.getOInternal(loc), loc);
			}
		}
	}

	@Override
	public MissingPart getMissingPart() {
		//cant replace locations
		return null;
	}

	@Override
	public void remove(boolean salvage) {
        Entity entity = get(Installable.class).getEntity();
		if(null != entity) {
		    entity.setInternal(IArmorState.ARMOR_DESTROYED, get(Installable.class).getMainLocation());
			Part spare = campaign.checkForExistingSparePart(this);
			if(!salvage) {
				campaign.removePart(this);
			} else if(null != spare) {
				spare.incrementQuantity();
				campaign.removePart(this);
			}
			get(Installable.class).getUnit().removePart(this);
		}
		get(Installable.class).setUnit(null);
		updateConditionFromEntity(false);
	}

	@Override
	public void updateConditionFromEntity(boolean checkForDestruction) {
        Entity entity = get(Installable.class).getEntity();
		if(null != entity) {
	        int loc = get(Installable.class).getMainLocation();
			if(IArmorState.ARMOR_DESTROYED == entity.getInternal(loc)) {
				remove(false);
			} else {
				damage = entity.getOInternal(loc) - entity.getInternal(loc);	
				if(get(Installable.class).getUnit().isLocationBreached(loc)) {
					breached = true;
				} 
			}
		}
	}
	
	@Override 
	public int getBaseTime() {
		return 60;
	}
	
	@Override
	public int getDifficulty() {
		return 0;
	}

	public boolean isBreached() {
		return breached;
	}
	
	@Override
	public boolean needsFixing() {
		return damage > 0 || breached;
	}
	
	@Override
    public String getDetails() {
		if(isBreached()) {
			return "Breached";
		} else {
			return  damage + " point(s) of damage";
		}
    }

	@Override
	public void updateConditionFromPart() {
		//shouldn't get here
	}
	
	@Override
    public String checkFixable() {
        return null;
    }
	
	@Override
	public boolean isSalvaging() {
		return false;
	}
	
	@Override
	public String checkScrappable() {
		return "Vehicle locations cannot be scrapped";
	}
	
	@Override
	public boolean canNeverScrap() {
		return true;
	}

	@Override
	public double getTonnage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getStickerPrice() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public TargetRoll getAllMods(Person tech) {
		if(isBreached() && !isSalvaging()) {
			return new TargetRoll(TargetRoll.AUTOMATIC_SUCCESS, "fixing breach");
		}
		return super.getAllMods(tech);
	}
	
	@Override
	public String getDesc() {
		if(!isBreached() || isSalvaging()) {
			return super.getDesc();
		}
		String toReturn = "<html><font size='2'";
		String scheduled = "";
		if (getAssignedTeamId() != null) {
			scheduled = " (scheduled) ";
		}
	
		toReturn += ">";
		toReturn += "<b>Seal " + getName() + "</b><br/>";
		toReturn += getDetails() + "<br/>";
		toReturn += "" + getTimeLeft() + " minutes" + scheduled;
		toReturn += "</font></html>";
		return toReturn;
	}
	
	 @Override
	 public boolean isRightTechType(String skillType) {
		 return skillType.equals(SkillType.S_TECH_MECHANIC);
	 }
	 
	 public void doMaintenanceDamage(int d) {
	     Entity entity = get(Installable.class).getEntity();
	     if(null != entity) {
	         int loc = get(Installable.class).getMainLocation();
             int points = entity.getInternal(loc);
             points = Math.max(points -d, 1);
             entity.setInternal(points, loc);
             updateConditionFromEntity(false);
	     }
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
