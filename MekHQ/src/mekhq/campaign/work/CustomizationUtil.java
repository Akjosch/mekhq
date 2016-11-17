package mekhq.campaign.work;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

import megamek.common.Entity;
import megamek.common.MechSummary;
import mekhq.Utilities;
import mekhq.campaign.CampaignOptions;
import mekhq.campaign.GameEffect;

public final class CustomizationUtil {
    private static Map<UnitModel, Set<UnitModel>> extraValidRefits = new HashMap<>();
    /** A map from specific refit actions to their type. TODO: Make it configurable. */
    private static Map<WorkActionType, RefitType> refitActionTypes = new HashMap<>();
    static {
        refitActionTypes.put(WorkActionType.WEAPON_REPLACEMENT_EASY, RefitType.A);
        refitActionTypes.put(WorkActionType.WEAPON_REPLACEMENT_MODERATE, RefitType.B);
        refitActionTypes.put(WorkActionType.CHANGE_ARMOR_TYPE, RefitType.C);
        refitActionTypes.put(WorkActionType.WEAPON_REPLACEMENT_HARD, RefitType.C);
        refitActionTypes.put(WorkActionType.CHANGE_ARMOR_DISTRIBUTION, RefitType.C);
        refitActionTypes.put(WorkActionType.MOVE_COMPONENT, RefitType.C);
        refitActionTypes.put(WorkActionType.ADD_AMMO_BIN, RefitType.C);
        refitActionTypes.put(WorkActionType.ADD_HEAT_SINK, RefitType.C);
        refitActionTypes.put(WorkActionType.INSTALL_NEW_ITEM, RefitType.D);
        refitActionTypes.put(WorkActionType.INSTALL_ECM, RefitType.D);
        refitActionTypes.put(WorkActionType.INSTALL_C3, RefitType.D);
        refitActionTypes.put(WorkActionType.INSTALL_TARCOMP, RefitType.D);
        refitActionTypes.put(WorkActionType.CHANGE_HEAT_SINK_TYPE, RefitType.D);
        refitActionTypes.put(WorkActionType.CHANGE_ENGINE_RATING, RefitType.D);
        refitActionTypes.put(WorkActionType.CHANGE_MYOMER_TYPE, RefitType.E);
        refitActionTypes.put(WorkActionType.INSTALL_CASE, RefitType.E);
        refitActionTypes.put(WorkActionType.CHANGE_IS_TYPE, RefitType.F);
        refitActionTypes.put(WorkActionType.CHANGE_ENGINE_TYPE, RefitType.F);
        refitActionTypes.put(WorkActionType.CHANGE_GYRO_TYPE, RefitType.F);
        refitActionTypes.put(WorkActionType.CHANGE_GYRO_TYPE, RefitType.F);
    }
    
    private static void addExtraValidRefit(UnitModel a, UnitModel b) {
        Set<UnitModel> refits = extraValidRefits.get(Objects.requireNonNull(a));
        if(null == refits) {
            refits = new HashSet<>();
            extraValidRefits.put(a, refits);
        }
        refits.add(Objects.requireNonNull(b));
        refits = extraValidRefits.get(b);
        if(null == refits) {
            refits = new HashSet<>();
            extraValidRefits.put(b, refits);
        }
        refits.add(a);
    }
    
    private static boolean hasExtraValidRefit(UnitModel a, UnitModel b) {
        Set<UnitModel> refits = extraValidRefits.get(Objects.requireNonNull(a));
        return (null != refits) && refits.contains(Objects.requireNonNull(b));
    }
    
    public static RefitType getRefitType(WorkActionType wa) {
        return Utilities.nonNull(refitActionTypes.get(wa), RefitType.NO_CHANGE);
    }
    
    /** @return <tt>true</tt> if the two entities have the same base chassis, <tt>false</tt> otherwise */
    public static boolean isSameChasis(Entity a, Entity b) {
        if(Objects.equals(a, b)) {
            return true;
        }
        if(null == a) {
            return false;
        }
        if(hasExtraValidRefit(new UnitModel(a), new UnitModel(b))) {
            return true;
        }
        return Objects.equals(a.getChassis(), b.getChassis())
            && (a.getWeight() == b.getWeight())
            && (a.isOmni() == b.isOmni());
    }
    
    /** @return a work plan to refit one type of entity into another, or <tt>null</tt> if there isn't one */
    public static RefitPlan getRefitPlan(Entity from, Entity to) {
        return getRefitPlan(from, to, 9999, null);
    }
    
    /** @return a work plan to refit one type of entity into another, or <tt>null</tt> if there isn't one */
    public static RefitPlan getRefitPlan(Entity from, Entity to, int year, CampaignOptions options) {
        if((null == from) || (null == to) || !isSameChasis(from, to)) {
            return null;
        }
        if(null != options) {
            // Check some options, if requested
            if(options.allowCanonOnly()
                && (!from.isCanon() || !to.isCanon())) {
                return null;
            }
            if(options.limitByYear()
                && ((from.getYear() > year) || (to.getYear() > year))) {
                return null;
            }
            if((options.getTechLevel() < from.getTechLevel()) || (options.getTechLevel() < to.getTechLevel())) {
                return null;
            }
        }
        List<WorkActionType> stuffToDo = new ArrayList<>();
        
        // Do we need to change the armor type?
        if(from.getArmorType(0) != to.getArmorType(0)) {
            stuffToDo.add(WorkActionType.CHANGE_ARMOR_TYPE);
        }
        return null;
    }
    
    public static class WorkAction extends GameEffect {
        WorkActionType type;
        
        public WorkAction(WorkActionType type, String desc, Consumer<IntUnaryOperator> action) {
            super(desc, action);
            this.type = type;
        }
    }
    
    public static class RefitPlan {
        
    }
    
    private static class UnitModel {
        public String type;
        public String chassis;
        public String model;
        
        public UnitModel(MechSummary ms) {
            this.type = ms.getUnitType();
            this.chassis = ms.getChassis();
            this.model = ms.getModel();
        }
        
        public UnitModel(Entity en) {
            this.type = Entity.getEntityTypeName(en.getEntityType());
            this.chassis = en.getChassis();
            this.model = en.getModel();
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) {
                return true;
            }
            if((null == obj) || (getClass() != obj.getClass())) {
                return false;
            }
            final UnitModel other = (UnitModel) obj;
            return Objects.equals(type, other.type) && Objects.equals(chassis, other.chassis)
                && Objects.equals(model, other.model);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(type, chassis, model);
        }

        @Override
        public String toString() {
            return type + "[" + chassis + " " + model + "]";
        }
    }
}
