package mekhq.campaign.material;

import java.util.EnumSet;
import java.util.Locale;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import megamek.common.EquipmentType;
import mekhq.Utilities;
import mekhq.adapter.BooleanValueAdapter;
import mekhq.adapter.MaterialUsageSetAdapter;
import mekhq.adapter.TechRatingAdapter;

@XmlRootElement(name="material")
@XmlAccessorType(XmlAccessType.FIELD)
public class Material {
    @XmlAttribute(required=true)
    private String id;
    private String name;
    @XmlJavaTypeAdapter(BooleanValueAdapter.class)
    private Boolean template;
    private String base;
    // TODO: image/icon
    /** Value of the raw material, per ton */
    private Double value;
    private Double valueMultiplier;
    private StorageType storageType;
    @XmlJavaTypeAdapter(MaterialUsageSetAdapter.class)
    private EnumSet<MaterialUsage> usage;
    @XmlElement(name="tr")
    @XmlJavaTypeAdapter(TechRatingAdapter.class)
    private Integer techRating;
    // TODO: Availability
    
    // Fluff
    @XmlElement(name="desc")
    private String description;
    
    private transient Material baseMaterial;
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isTemplate() {
        return (null != template) && template.booleanValue();
    }
    
    public double getValue() {
        double baseValue = 0.0;
        
        // Use our value if we have one, else pull the one from the base template
        if(null != value) {
            baseValue = value.doubleValue();
        } else if(null != baseMaterial) {
            baseValue = baseMaterial.getValue();
        }
        
        if(null != valueMultiplier) {
            baseValue *= valueMultiplier.doubleValue();
        }
        
        return baseValue;
    }
    
    public StorageType getStorageType() {
        StorageType result = storageType;
        if((null == result) && (null != baseMaterial)) {
            result = baseMaterial.getStorageType();
        }
        return Utilities.nonNull(result, StorageType.BULK);
    }
    
    public EnumSet<MaterialUsage> getUsage() {
        EnumSet<MaterialUsage> result = usage;
        if((null == result) && (null != baseMaterial)) {
            result = baseMaterial.getUsage();
        }
        return Utilities.nonNull(result, EnumSet.noneOf(MaterialUsage.class));
    }
    
    public String getDescription() {
        return Utilities.nonNull(description, "");
    }
    
    public int getTechRating() {
        return (null != techRating) ? techRating.intValue() : EquipmentType.RATING_C;
    }
    
    // JAXB marshalling support
    
    @SuppressWarnings("unused")
    private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        if(null == name) {
            name = id;
        }
        id = id.toUpperCase(Locale.ROOT);
        if(null != base) {
            base = base.toUpperCase(Locale.ROOT);
            baseMaterial = Materials.getMaterial(base);
            if(null == baseMaterial) {
                throw new RuntimeException(String.format("Base material '%s' undefined.", base));
            }
            if(!baseMaterial.isTemplate()) {
                throw new RuntimeException(String.format("Base material '%s' is not a material template.", base));
            }
        }
        if(!Materials.registerMaterial(this)) {
            throw new RuntimeException(String.format("Material '%s' was already defined.", id));
        }
    }
    
    @SuppressWarnings("unused")
    private boolean beforeMarshal(Marshaller marshaller) {
        if(null != baseMaterial) {
            base = baseMaterial.id;
        }
        return true;
    }
}
