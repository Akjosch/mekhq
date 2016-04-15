package mekhq.campaign.parts.component;

import mekhq.campaign.unit.Unit;

public class InstallableManager {
    public static boolean canBeInstalled(Installable installable, Unit unit) {
        if(null == installable) {
            // You can't install nothing
            return false;
        }
        if(null == unit || null == unit.getEntity()) {
            // deinstalling is generally fine
            return true;
        }
        if(installable.isTonnageLimited() && installable.getUnitTonnage() != unit.getEntity().getWeight()) {
            return false;
        }
        return true;
    }
}
