package mekhq.campaign.parts.component;

import mekhq.campaign.parts.Part;

/**
 * Part components are data-only classes. They consist of fields, getters, setters and
 * some annotations to allow for their easy serialisation.
 * <p>
 * Under no circumstances should any program logic be ever put in them.
 * <p>
 * In some cases, multiple Parts can share the same Component, but the default assumption is
 * that each Part which needs a specific Component type has its own instance of it.
 */
public abstract class Component {
    protected Part owner;
    
    public void setOwner(Part owner) {
        this.owner = owner;
    }
    
    public Part getOwner() {
        return owner;
    }
}
