/*
 * MissingEquipmentPart.java
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

import megamek.common.CriticalSlot;
import megamek.common.Entity;
import megamek.common.EquipmentType;
import megamek.common.Mounted;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.MissingPart;
import mekhq.campaign.parts.Part;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.unit.Unit;
import mekhq.campaign.universe.Era;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingEquipmentPart extends MissingPart {
	private static final long serialVersionUID = 2892728320891712304L;

	//crap equipmenttype is not serialized!
    protected transient EquipmentType type;
    protected String typeName;
	protected int equipmentNum = -1;
	protected double equipTonnage;

    public EquipmentType getType() {
        return type;
    }

    public int getEquipmentNum() {
    	return equipmentNum;
    }

    public void setEquipmentNum(int num) {
    	equipmentNum = num;
    }

    public MissingEquipmentPart() {
    	this(0, null, -1, null, 0);
    }

    public MissingEquipmentPart(double tonnage, EquipmentType et, int equipNum, Campaign c, double eTonnage) {
        // TODO Memorize all entity attributes needed to calculate cost
        // As it is a part bought with one entity can be used on another entity
        // on which it would have a different price (only tonnage is taken into
        // account for compatibility)
        super(c);
        this.type =et;
        if(null != type) {
        	this.name = type.getName();
        	this.typeName = type.getInternalName();
        }
        this.equipmentNum = equipNum;
        this.equipTonnage = eTonnage;
        get(Installable.class).setUnitTonnage(tonnage);
    }

    @Override
	public int getBaseTime() {
		return 120;
	}

	@Override
	public int getDifficulty() {
		return 0;
	}

    /**
     * Restores the equipment from the name
     */
    public void restore() {
        if (typeName == null) {
        	typeName = type.getName();
        } else {
            type = EquipmentType.get(typeName);
        }

        if (type == null) {
            System.err
            .println("Mounted.restore: could not restore equipment type \""
                    + name + "\"");
        }
    }

    @Override
    public double getTonnage() {
    	return equipTonnage;
    }

	@Override
	protected void loadFieldsFromXmlNode(Node wn) {
		NodeList nl = wn.getChildNodes();

		for (int x=0; x<nl.getLength(); x++) {
			Node wn2 = nl.item(x);
			if (wn2.getNodeName().equalsIgnoreCase("equipmentNum")) {
				equipmentNum = Integer.parseInt(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("typeName")) {
				typeName = wn2.getTextContent();
			} else if (wn2.getNodeName().equalsIgnoreCase("equipTonnage")) {
				equipTonnage = Double.parseDouble(wn2.getTextContent());
			}
		}
		restore();
	}

	@Override
	public int getAvailability(int era) {
		return type.getAvailability(Era.convertEra(era));
	}

	@Override
    public int getIntroDate() {
    	return getType().getIntroductionDate();
    }

    @Override
    public int getExtinctDate() {
    	return getType().getExtinctionDate();
    }

    @Override
    public int getReIntroDate() {
    	return getType().getReintruductionDate();
    }

	@Override
	public int getTechRating() {
		return type.getTechRating();
	}

	@Override
	public void fix() {
		Part replacement = findReplacement(false);
		if(null != replacement) {
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
		//According to official answer, if sticker prices are different then
		//they are not acceptable substitutes, so we need to check for that as
		//well
		//http://bg.battletech.com/forums/strategic-operations/(answered)-can-a-lance-for-a-35-ton-mech-be-used-on-a-40-ton-mech-and-so-on/
		Part newPart = getNewPart();
		newPart.setUnit(unit);
		if(part instanceof EquipmentPart) {
			EquipmentPart eqpart = (EquipmentPart)part;
			EquipmentType et = eqpart.getType();
			return type.equals(et) && getTonnage() == part.getTonnage() && part.getStickerPrice() == newPart.getStickerPrice();
		}
		return false;
	}

	@Override
    public String checkFixable() {
	    // The part is only fixable if the location is not destroyed.
        // be sure to check location and second location
	    Unit unit = get(Installable.class).getUnit();
        if(null != unit) {
            Mounted m = unit.getEntity().getEquipment(equipmentNum);
            if(null != m) {
                int loc = m.getLocation();
                if(loc == -1) {
                }
                if (unit.isLocationBreached(loc)) {
                    return unit.getEntity().getLocationName(loc) + " is breached.";
                }
                if (unit.isLocationDestroyed(loc)) {
                    return unit.getEntity().getLocationName(loc) + " is destroyed.";
                }
                loc = m.getSecondLocation();
                if(loc != Entity.LOC_NONE) {
                    if (unit.isLocationBreached(loc)) {
                        return unit.getEntity().getLocationName(loc) + " is breached.";
                    }
                    if (unit.isLocationDestroyed(loc)) {
                        return unit.getEntity().getLocationName(loc) + " is destroyed.";
                    }
                }
            }
        }
        return null;
    }

	@Override
	public boolean onBadHipOrShoulder() {
        Unit unit = get(Installable.class).getUnit();
		if(null != unit) {
			for(int loc = 0; loc < unit.getEntity().locations(); loc++) {
	            for (int i = 0; i < unit.getEntity().getNumberOfCriticals(loc); i++) {
	                CriticalSlot slot = unit.getEntity().getCritical(loc, i);

	                // ignore empty & system slots
	                if ((slot == null) || (slot.getType() != CriticalSlot.TYPE_EQUIPMENT)) {
	                    continue;
	                }
	                Mounted equip = unit.getEntity().getEquipment(equipmentNum);
	                Mounted m1 = slot.getMount();
	                Mounted m2 = slot.getMount2();
	                if (m1 == null && m2 == null) {
	                	continue;
	                }
	                if ((equip.equals(m1)) || (equip.equals(m2))) {
	                    if (unit.hasBadHipOrShoulder(loc)) {
	                        return true;
	                    }
	                }
	            }
	        }
		}
		return false;
	}

	@Override
    public void setUnit(Unit u) {
    	super.setUnit(u);
    	if(null != unit) {
    		equipTonnage = type.getTonnage(unit.getEntity());
    	}
    }

	@Override
	public Part getNewPart() {
		EquipmentPart epart = new EquipmentPart(getUnitTonnage(), type, -1, campaign);
		epart.setEquipTonnage(equipTonnage);
		return epart;
	}
/*
	private boolean hasReallyCheckedToday() {
		return checkedToday;
	}

	@Override
	public boolean hasCheckedToday() {
		//if this unit has been checked for any other equipment of this same type
		//then return false, regardless of whether this one has been checked
		if(null != unit) {
			for(Part part : unit.getParts()) {
				if(part.getId() == getId()) {
					continue;
				}
				if(part instanceof MissingEquipmentPart
						&& ((MissingEquipmentPart)part).getType().equals(type)
						&& ((MissingEquipmentPart)part).hasReallyCheckedToday()) {
					return true;
				}
			}
		}
		return super.hasCheckedToday();
	}
*/

	public int getLocation() {
    	if(null != unit) {
    		Mounted mounted = unit.getEntity().getEquipment(equipmentNum);
			if(null != mounted) {
				return mounted.getLocation();
			}
    	}
    	return -1;
    }


    public boolean isRearFacing() {
        Unit unit = get(Installable.class).getUnit();
    	if(null != unit) {
    		Mounted mounted = unit.getEntity().getEquipment(equipmentNum);
			if(null != mounted) {
				return mounted.isRearMounted();
			}
    	}
    	return false;
    }

	@Override
	public void updateConditionFromPart() {
        Unit unit = get(Installable.class).getUnit();
		if(null != unit) {
			Mounted mounted = unit.getEntity().getEquipment(equipmentNum);
			if(null != mounted) {
				mounted.setHit(true);
		        mounted.setDestroyed(true);
		        mounted.setRepairable(false);
		        unit.destroySystem(CriticalSlot.TYPE_EQUIPMENT, unit.getEntity().getEquipmentNum(mounted));
			}
		}
	}

	@Override
    public boolean isOmniPoddable() {
    	//TODO: is this on equipment type?
    	return true;
    }

	@Override
	public String getLocationName() {
		if(null != unit) {
			Mounted mounted = unit.getEntity().getEquipment(equipmentNum);
			if(null != mounted && mounted.getLocation() != -1) {
				return unit.getEntity().getLocationName(mounted.getLocation());
			}
    	}
		return null;
	}

	@Override
    public boolean isInLocation(String loc) {
		if(null == unit || null == unit.getEntity() || null == unit.getEntity().getEquipment(equipmentNum)) {
			return false;
		}

		Mounted mounted = unit.getEntity().getEquipment(equipmentNum);
		if(null == mounted) {
			return false;
		}
		int location = unit.getEntity().getLocationFromAbbr(loc);
		for (int i = 0; i < unit.getEntity().getNumberOfCriticals(location); i++) {
	            CriticalSlot slot = unit.getEntity().getCritical(location, i);
	            // ignore empty & non-hittable slots
	            if ((slot == null) || !slot.isEverHittable() || slot.getType()!=CriticalSlot.TYPE_EQUIPMENT
	            		|| null == slot.getMount()) {
	                continue;
	            }
	            if(unit.getEntity().getEquipmentNum(slot.getMount()) == equipmentNum) {
	            	return true;
	            }
		}
		//if we are still here, lets just double check by the mounted's location and secondary location
		if(mounted.getLocation() == location) {
			return true;
		}
		if(mounted.getSecondLocation() == location) {
			return true;
		}
		return false;
    }
}
