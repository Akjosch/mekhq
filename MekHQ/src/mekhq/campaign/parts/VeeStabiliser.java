/*
 * VeeStabiliser.java
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

import megamek.common.Compute;
import megamek.common.EquipmentType;
import megamek.common.Tank;
import megamek.common.TechConstants;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.personnel.SkillType;
import mekhq.campaign.unit.Unit;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class VeeStabiliser extends Part {
	private static final long serialVersionUID = 6708245721569856817L;

	public VeeStabiliser() {
		this(0, null);
	}
	
	public VeeStabiliser(int loc, Campaign c) {
        super(c);
        this.name = "Vehicle Stabiliser";
        add(new Installable());
        get(Installable.class).setLocations(loc);
    }
	
	public VeeStabiliser clone() {
		VeeStabiliser clone = new VeeStabiliser(get(Installable.class).getMainLocation(), campaign);
        clone.copyBaseData(this);
		return clone;
	}

    @Override
    public boolean isSamePartType(Part part) {
        return part instanceof VeeStabiliser;
    }

	@Override
	protected void loadFieldsFromXmlNode(Node wn) {
		NodeList nl = wn.getChildNodes();
		
		for (int x=0; x<nl.getLength(); x++) {
			Node wn2 = nl.item(x);
			
			if (wn2.getNodeName().equalsIgnoreCase("loc")) {
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
	public int getTechLevel() {
		return TechConstants.T_ALLOWED_ALL;
	}

	@Override
	public void fix() {
		super.fix();
		Tank tank = get(Installable.class).getEntity(Tank.class);
		if(null != tank) {
		    tank.clearStabiliserHit(get(Installable.class).getMainLocation());
		}
	}

	@Override
	public MissingPart getMissingPart() {
		return new MissingVeeStabiliser(get(Installable.class).getMainLocation(), campaign);
	}

	@Override
	public void remove(boolean salvage) {
        Tank tank = get(Installable.class).getEntity(Tank.class);
		if(null != tank) {
		    tank.setStabiliserHit(get(Installable.class).getMainLocation());
			Part spare = campaign.checkForExistingSparePart(this);
			if(!salvage) {
				campaign.removePart(this);
			} else if(null != spare) {
				spare.incrementQuantity();
				campaign.removePart(this);
			}
			get(Installable.class).getUnit().removePart(this);
			Part missing = getMissingPart();
			get(Installable.class).getUnit().addPart(missing);
			campaign.addPart(missing, 0);
		}
		get(Installable.class).setUnit(null);
		updateConditionFromEntity(false);
	}

	@Override
	public void updateConditionFromEntity(boolean checkForDestruction) {
        Tank tank = get(Installable.class).getEntity(Tank.class);
		if(null != tank) {
			int priorHits = hits;
			if(tank.isStabiliserHit(get(Installable.class).getMainLocation())) {
				hits = 1;
			} else {
				hits = 0;
			}
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
		return 60;
	}
	
	@Override
	public int getDifficulty() {
		if(isSalvaging()) {
			return 0;
		}
		return 1;
	}

	@Override
	public boolean needsFixing() {
		return hits > 0;
	}

	@Override
	public void updateConditionFromPart() {
        Tank tank = get(Installable.class).getEntity(Tank.class);
		if(null != tank) {
		    int loc = get(Installable.class).getMainLocation();
			if(hits > 0 && !tank.isStabiliserHit(loc)) {
			    tank.setStabiliserHit(loc);
			}
			else if(hits == 0 && tank.isStabiliserHit(loc)) {
			    tank.clearStabiliserHit(loc);
			}
		}
	}

	@Override
	public String checkFixable() {
        Unit unit = get(Installable.class).getUnit();
        int loc = get(Installable.class).getMainLocation();
		if(null != unit && !isSalvaging() && unit.isLocationBreached(loc)) {
    		return unit.getEntity().getLocationName(loc) + " is breached.";
		}
		return null;
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
    public String getDetails() {
		if(get(Installable.class).isInstalled()) {
			return get(Installable.class).getLocationName();
		}
		return "";
    }
	
	@Deprecated
	public void setLocation(int l) {
	    get(Installable.class).setLocations(l);
	}
	
	@Override
	public boolean isRightTechType(String skillType) {
		return skillType.equals(SkillType.S_TECH_MECHANIC);
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
