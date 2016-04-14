package mekhq.campaign.parts.component;

import mekhq.campaign.parts.Part;

/** Part components */
public abstract class Component {
    protected Part owner;
    
    protected Component(Part owner) {
        this.owner = owner;
    }
    
    public void setOwner(Part owner) {
        this.owner = owner;
    }
    
    public Part getOwner() {
        return owner;
    }
}
