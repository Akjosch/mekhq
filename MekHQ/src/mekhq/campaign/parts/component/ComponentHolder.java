package mekhq.campaign.parts.component;

/**
 * An interface for objects which can hold components
 * 
 * (currently just Part)
 */
public interface ComponentHolder {
    /** @return the component associated with this component holder, or <code>null</code> */
    <T extends Component> T get(Class<T> cls);
    
    /**
     * Add a component to this component holder. Only one component of a given class per component holder
     * possible. If there is already a component in place, doesn't replace it.
     * 
     * @return <code>false</code> if there already was a component in place, else <code>true</code>
     */
    boolean add(Component component);
    
    /** Like add(), but replaces the component if there was one already */
    void replace(Component component);
    
    /** Remove a given component class */
    void remove(Class<? extends Component> cls);
    
    /** @return <code>true</code> is this component holder has the given component class,
     *          <code>false</code> otherwise */
    public boolean has(Class<? extends Component> cls);
}
