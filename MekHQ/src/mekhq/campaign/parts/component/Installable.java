package mekhq.campaign.parts.component;

import java.util.UUID;

import megamek.common.Entity;
import megamek.common.Protomech;
import mekhq.campaign.parts.Part;
import mekhq.campaign.unit.Unit;

/** A {@link Part} with an Installable component can be installed on a {@link Unit} */
public class Installable extends Component {
    private int unitTonnage = -1;
    private boolean isTonnageLimited = false;
    
    private Unit unit = null;
    private UUID unitId = null;
    private int[] locations = new int[]{Entity.LOC_NONE};

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit u) {
        this.unit = u;
        if(null != unit) {
            unitId = unit.getId();
            unitTonnage = (int) u.getEntity().getWeight();
        } else {
            unitId = null;
        }
    }

    public UUID getUnitId() {
        return unitId;
    }

    public void setUnitId(UUID unitId) {
        this.unitId = unitId;
    }

    public int getUnitTonnage() {
        return unitTonnage;
    }

    public void setUnitTonnage(int unitTonnage) {
        this.unitTonnage = unitTonnage;
    }
    
    public Entity getEntity() {
        return isInstalled() ? unit.getEntity() : null;
    }
    
    /** @return the {@link Entity} this part is installed on, or <code>null</code> if it's not installed or the Entity is of the wrong class */
    public <T extends Entity> T getEntity(Class<T> entityClass) {
        if(null == entityClass) {
            return null;
        }
        Entity entity = getEntity();
        if((null != entity) && entityClass.isInstance(entity)) {
            return entityClass.cast(entity);
        }
        return null;
    }
    
    public boolean isInstalled() {
        return (null != unit);
    }
    
    public int[] getLocations() {
        return locations;
    }
    
    public void setLocations(int ... locations) {
        this.locations = locations;
    }
    
    public int getMainLocation() {
        return locations.length > 0 ? locations[0] : Entity.LOC_NONE;
    }
    
    public String getLocationName() {
        return (null != getEntity()) ? getEntity().getLocationName(getMainLocation()) : null;
    }

    public boolean isInLocation(String loc) {
        if(isInstalled()) {
            final Entity entity = unit.getEntity();
            if(null != entity) {
                final int searchLocation = entity.getLocationFromAbbr(loc);
                for(int location : locations) {
                    if(location == searchLocation) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public boolean isMountedOnDestroyedLocation() {
        if(null == unit) {
            return false;
        }
        for(int loc : locations) {
            if(unit.isLocationDestroyed(loc)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        if(isInstalled()) {
            return "mounted: " + unit; //$NON-NLS-1$
        } else {
            return "(installable)"; //$NON-NLS-1$
        }
    }

    public boolean isTonnageLimited() {
        return isTonnageLimited;
    }

    public void setTonnageLimited(boolean isTonnageLimited) {
        this.isTonnageLimited = isTonnageLimited;
    }
}
