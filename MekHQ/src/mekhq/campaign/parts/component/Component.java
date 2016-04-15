package mekhq.campaign.parts.component;

/**
 * Components are data-only classes. They consist of fields, getters, setters and
 * some annotations to allow for their easy serialisation.
 * <p>
 * Under no circumstances should any program logic be ever put in them.
 * <p>
 * In some cases, multiple component holders can share the same component, but
 * the default assumption is that each holder which needs a specific component
 * type has its own instance of it.
 */
public abstract class Component {
    protected ComponentHolder owner;
    
    public void setOwner(ComponentHolder owner) {
        this.owner = owner;
    }
    
    public ComponentHolder getOwner() {
        return owner;
    }
}
