package mekhq.campaign.universe;

public enum FactionGroup {
    ALL(null), IS(ALL), CLAN(ALL), PERIPHERY(IS); 

    public final FactionGroup parent;
    
    private FactionGroup(FactionGroup parent) {
        this.parent = parent;
    }
    
    /** @return true if the two faction groups are the same or this is a sub-group of the other */
    public boolean is(FactionGroup other) {
        if( null == other ) {
            return false;
        }
        if( this == other ) {
            return true;
        }
        if( null != parent ) {
            return parent.is(other);
        }
        return false;
    }
}
