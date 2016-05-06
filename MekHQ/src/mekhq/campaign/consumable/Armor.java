package mekhq.campaign.consumable;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import megamek.common.EquipmentType;
import mekhq.Utilities;
import mekhq.adapter.BooleanValueAdapter;
import mekhq.adapter.MaterialAdapter;
import mekhq.adapter.TechRatingAdapter;
import mekhq.campaign.material.Material;
import mekhq.campaign.material.MaterialUsage;

/**
 * base Armor information
 */
@XmlSeeAlso({Armor.Mech.class, Armor.SupportVehicle.class})
@XmlRootElement(name="armor")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Armor {
    @XmlAttribute(required=true)
    protected String id;
    /** Defaults to material's name */
    protected String name;
    @XmlElement(name="mat")
    @XmlJavaTypeAdapter(MaterialAdapter.class)
    protected Material material;
    /** Value of the armor, per ton; defaults to material's value if not set */
    protected Double value;
    /** Tech rating of the armor (A-F); default so the material's tech rating */
    @XmlElement(name="tr")
    @XmlJavaTypeAdapter(TechRatingAdapter.class)
    protected Integer techRating;

    // MegaMek's EquipmentType and clan/is specific flags
    protected Integer mmId;
    protected Boolean mmClan;

    // Fluff
    protected String ref;
    @XmlElement(name="desc")
    protected String description;

    public String getId() {
        return id;
    }
    
    public String getName() {
        return (null != name) ? name : material.getName();
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public double getValue() {
        return (null != value) ? value.doubleValue() : material.getValue();
    }
    
    public int getTechRating() {
        return (null != techRating) ? techRating.intValue() : material.getTechRating();
    }

    public int getEquipmentTypeId() {
        return (null != mmId) ? mmId.intValue() : EquipmentType.T_ARMOR_UNKNOWN;
    }
    
    public boolean isClan() {
        return (null == mmClan) || mmClan.booleanValue();
    }
    
    public boolean isIS() {
        return (null == mmClan) || !mmClan.booleanValue();
    }
    
    public String getRef() {
        return Utilities.nonNull(ref, "");
    }
    
    public String getDescription() {
        return Utilities.nonNull(description, "");
    }
    
    // JAXB marshalling support
    
    @SuppressWarnings("unused")
    private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        if(null == material) {
            throw new RuntimeException(String.format("Armor '%s' is missing material definition.", id));
        }
        if(!material.hasUsage(MaterialUsage.ARMOR)) {
            throw new RuntimeException(String.format("Material '%s' is not used for armors.", material.getId()));
        }
    }
    
    /** Mech specific armor data */
    @XmlRootElement(name="mecharmor")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Mech extends Armor {
        /** Amount of "floating" critical slots this armor takes; default 0 */
        private int crits;
        /**
         * Amount of "constant" critical slots this armor takes everywhere but
         * in the head and center torso locations. Defaults to 0, only used by
         * stealth armor (which has 2).
         */
        @XmlElement(name="fixed")
        private int fixedCrits;
        /**
         * Amount of critical slots per location when using patchwork armor.
         * Defaults to 0.
         */
        @XmlElement(name="patch")
        private int patchworkCrits;
        /**
         * Is this armor capable of being mounted on an OmniMech chasis?
         * True for all but the Hardened armor.
         */
        @XmlJavaTypeAdapter(BooleanValueAdapter.class)
        private Boolean omni;
        /**
         * Points per ton, default 16
         */
        private Double points;
        /**
         * Point mass in kg for fractional accounting, defaults to
         * 1000 / (points per ton), rounded to the nearest 1/10th of a kg.
         */
        @XmlElement(name="mass")
        private Double pointMass;
        
        public int getFloatingCrits() {
            return crits;
        }
        
        public int getFixedCrits() {
            return fixedCrits;
        }
        
        public int getPatchworkCrits() {
            return patchworkCrits;
        }
        
        public boolean isOmniCapable() {
            return (null == omni) || omni.booleanValue();
        }
        
        public double getPointsPerTon() {
            return (null == points) ? 16.0 : points.doubleValue();
        }
        
        public double getKgPerPoint() {
            return (null == pointMass)
                ? Math.round(10000.0 / getPointsPerTon()) / 10.0 : pointMass.doubleValue();
        }
        
        // JAXB marshalling support
        
        @SuppressWarnings("unused")
        private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
            if(null == points) {
                points = Double.valueOf(16.0);
            }
        }
    }
    
    /** Support vehicle specific armor data */
    @XmlRootElement(name="svarmor")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SupportVehicle extends Armor {
        /** Amount of equipment slots this armor takes; default 0 */
        private int slots;
        
        /**
         * Point mass in kg
         */
        @XmlElement(name="mass")
        private Double pointMass;

        /**
         * Point cost, defaults to value / 1000 * pointMass, rounded
         * to the nearest full C-bill
         */
        @XmlElement(name="cost")
        private Double pointCost;
        
        /** Does it require armored chasis? Default false */
        @XmlJavaTypeAdapter(BooleanValueAdapter.class)
        private Boolean armored;
        
        // JAXB marshalling support
        
        @SuppressWarnings("unused")
        private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
            if(null == pointMass) {
                throw new RuntimeException(String.format("Support vehicle armor '%s' is missing the armor point mass.", id));
            }
            if(null == pointCost) {
                pointCost = Double.valueOf(Math.round(getValue() / 1000.0 * pointMass));
            }
        }
        
        public int getSlots() {
            return slots;
        }
        
        public double getPointMass() {
            return pointMass.doubleValue();
        }
        
        public double getPointCost() {
            return pointCost.doubleValue();
        }
        
        public boolean requiresArmoredChasis() {
            return (null != armored) && armored.booleanValue();
        }
    }

    
    /**
     * Wrapper around {@link EquipmentType} armors.
     * <p>
     * PATCHWORK is not a real armor type.
     */
    public static enum ArmorType {
        STANDARD(EquipmentType.T_ARMOR_STANDARD, " (IS)", 1.0),
        STANDARD_CLAN(EquipmentType.T_ARMOR_STANDARD, " (Clan)", 1.0),
        
        // Mech armor (TM 56)
        STEALTH(EquipmentType.T_ARMOR_STEALTH), 
        LIGHT_FERRO(EquipmentType.T_ARMOR_LIGHT_FERRO, 59.0),
        FERRO_FIBROUS(EquipmentType.T_ARMOR_FERRO_FIBROUS, " (IS)", 1.12, 55.8),
        FERRO_FIBROUS_CLAN(EquipmentType.T_ARMOR_FERRO_FIBROUS, " (Clan)", 1.2, 52.1),
        HEAVY_FERRO(EquipmentType.T_ARMOR_HEAVY_FERRO, 50.4),
        
        // IndustrialMech armor (TM 72)
        HEAVY_INDUSTRIAL(EquipmentType.T_ARMOR_HEAVY_INDUSTRIAL),
        INDUSTRIAL(EquipmentType.T_ARMOR_INDUSTRIAL, 93.3),
        COMMERCIAL(EquipmentType.T_ARMOR_COMMERCIAL, 41.7),

        // ProtoMech armor (TM 86)
        PROTOMEK("ProtoMech", 12500.0, 1.25, 50.0),
        
        // Aerospace armor (TM 192)
        LIGHT_ALUM(EquipmentType.T_ARMOR_LIGHT_ALUM, 59.0),
        ALUM(EquipmentType.T_ARMOR_ALUM, " (IS)", 1.12, 55.8),
        ALUM_CLAN(EquipmentType.T_ARMOR_ALUM, " (Clan)", 1.2, 52.1),
        HEAVY_ALUM(EquipmentType.T_ARMOR_HEAVY_ALUM, 50.4),
        
        // Battle Armor armor (TN 169)
        BA_STANDARD(EquipmentType.T_ARMOR_BA_STANDARD, " (IS)", 1.25, 50.0),
        BA_STANDARD_CLAN(EquipmentType.T_ARMOR_BA_STANDARD, " (Clan)", 2.5, 25.0),
        BA_STANDARD_ADVANCED(EquipmentType.T_ARMOR_BA_STANDARD_ADVANCED, "", 1.5625, 40.0),
        BA_STANDARD_PROTOTYPE(EquipmentType.T_ARMOR_BA_STANDARD_PROTOTYPE, "", 0.625, 100.0),
        BA_STEALTH_BASIC(EquipmentType.T_ARMOR_BA_STEALTH_BASIC, " (IS)", 12.5/11.0, 55.0),
        BA_STEALTH_BASIC_CLAN(EquipmentType.T_ARMOR_BA_STEALTH_BASIC, " (Clan)", 6.25/3.0, 30.0),
        BA_STEALTH_IMP(EquipmentType.T_ARMOR_BA_STEALTH_IMP, " (IS)", 3.125/3.0, 60.0),
        BA_STEALTH_IMP_CLAN(EquipmentType.T_ARMOR_BA_STEALTH_IMP, " (Clan)", 12.5/7.0, 35.0),
        BA_STEALTH_PROTOTYPE(EquipmentType.T_ARMOR_BA_STEALTH_PROTOTYPE, "", 0.625, 100.0),
        BA_STEALTH(EquipmentType.T_ARMOR_BA_STEALTH, " (IS)", 3.125/3.0, 60.0),
        BA_STEALTH_CLAN(EquipmentType.T_ARMOR_BA_STEALTH, " (Clan)", 12.5/7.0, 35.0),
        BA_FIRE_RESIST(EquipmentType.T_ARMOR_BA_FIRE_RESIST, "", 6.25/3.0, 30.0),
        BA_MIMETIC(EquipmentType.T_ARMOR_BA_MIMETIC, "", 1.25, 50.0),
        
        // Tactical Operations armor types (p. 279ff)
        FERRO_LAMELLOR(EquipmentType.T_ARMOR_FERRO_LAMELLOR, 71.4),
        HARDENED(EquipmentType.T_ARMOR_HARDENED),
        REFLECTIVE(EquipmentType.T_ARMOR_REFLECTIVE, " (IS)", 1.0),
        REFLECTIVE_CLAN(EquipmentType.T_ARMOR_REFLECTIVE, " (Clan)", 1.0),
        // MODULAR("Modular", 10000.0, 0.625, 100.0), // TODO: Is it an armor type?
        REACTIVE(EquipmentType.T_ARMOR_REACTIVE, " (IS)", 1.0),
        REACTIVE_CLAN(EquipmentType.T_ARMOR_REACTIVE, " (Clan)", 1.0),
        STEALTH_VEHICLE(EquipmentType.T_ARMOR_STEALTH_VEHICLE),
        // Added via errata
        BA_REACTIVE(EquipmentType.T_ARMOR_BA_REACTIVE, " (IS)", 3.125/3.0, 60.0),
        BA_REACTIVE_CLAN(EquipmentType.T_ARMOR_BA_REACTIVE, " (Clan)", 12.5/7.0, 35.0),
        BA_REFLECTIVE(EquipmentType.T_ARMOR_BA_REFLECTIVE, " (IS)", 12.5/11.0, 55.0),
        BA_REFLECTIVE_CLAN(EquipmentType.T_ARMOR_BA_REFLECTIVE, " (Clan)", 6.25/3.0, 30.0),
        
        // Advanced aerospace armor types (SO 152)
        FERRO_IMP(EquipmentType.T_ARMOR_FERRO_IMP, " (IS)", 1.25),
        FERRO_IMP_CLAN(EquipmentType.T_ARMOR_FERRO_IMP, " (Clan)", 1.5),
        FERRO_CARBIDE(EquipmentType.T_ARMOR_FERRO_CARBIDE, " (IS)", 1.5),
        FERRO_CARBIDE_CLAN(EquipmentType.T_ARMOR_FERRO_CARBIDE, " (Clan)", 1.75),
        LAMELLOR_FERRO_CARBIDE(EquipmentType.T_ARMOR_LAMELLOR_FERRO_CARBIDE, " (IS)", 1.75),
        LAMELLOR_FERRO_CARBIDE_CLAN(EquipmentType.T_ARMOR_LAMELLOR_FERRO_CARBIDE, " (Clan)", 2.0),
        
        // Interstellar Operations armor types (p. 64ff)
        EDP(EquipmentType.T_ARMOR_EDP, "", 2.5/3.0, 75.0),
        FERRO_FIBROUS_PROTO(EquipmentType.T_ARMOR_FERRO_FIBROUS_PROTO, 55.8),
        FERRO_ALUM_PROTO(EquipmentType.T_ARMOR_FERRO_ALUM_PROTO, 55.8),
        ANTI_PENETRATIVE_ABLATION(EquipmentType.T_ARMOR_ANTI_PENETRATIVE_ABLATION, 83.3),
        BALLISTIC_REINFORCED(EquipmentType.T_ARMOR_BALLISTIC_REINFORCED, 83.3),
        HEAT_DISSIPATING(EquipmentType.T_ARMOR_HEAT_DISSIPATING, 100.0),
        IMPACT_RESISTANT(EquipmentType.T_ARMOR_IMPACT_RESISTANT, 71.4),
        
        PRIMITIVE(EquipmentType.T_ARMOR_PRIMITIVE),
        
        // Special case, values depend on TR and BAR, so there's a bunch of them.
        SUPPORT_VEHICLE_BAR2_TRA("Support Vehicle (BAR 2, TR A)", 50000.0/40.0, 62.5/40.0, 40.0),
        SUPPORT_VEHICLE_BAR2_TRB("Support Vehicle (BAR 2, TR B)", 50000.0/25.0, 62.5/25.0, 25.0),
        SUPPORT_VEHICLE_BAR2_TRC("Support Vehicle (BAR 2, TR C)", 50000.0/16.0, 62.5/16.0, 16.0),
        SUPPORT_VEHICLE_BAR2_TRD("Support Vehicle (BAR 2, TR D)", 50000.0/13.0, 62.5/13.0, 13.0),
        SUPPORT_VEHICLE_BAR2_TRE("Support Vehicle (BAR 2, TR E)", 50000.0/12.0, 62.5/12.0, 12.0),
        SUPPORT_VEHICLE_BAR2_TRF("Support Vehicle (BAR 2, TR F)", 50000.0/11.0, 62.5/11.0, 11.0),
        SUPPORT_VEHICLE_BAR3_TRA("Support Vehicle (BAR 3, TR A)", 100000.0/60.0, 62.5/60.0, 60.0),
        SUPPORT_VEHICLE_BAR3_TRB("Support Vehicle (BAR 3, TR B)", 100000.0/38.0, 62.5/38.0, 38.0),
        SUPPORT_VEHICLE_BAR3_TRC("Support Vehicle (BAR 3, TR C)", 100000.0/24.0, 62.5/24.0, 24.0),
        SUPPORT_VEHICLE_BAR3_TRD("Support Vehicle (BAR 3, TR D)", 100000.0/19.0, 62.5/19.0, 19.0),
        SUPPORT_VEHICLE_BAR3_TRE("Support Vehicle (BAR 3, TR E)", 100000.0/17.0, 62.5/17.0, 17.0),
        SUPPORT_VEHICLE_BAR3_TRF("Support Vehicle (BAR 3, TR F)", 100000.0/16.0, 62.5/16.0, 16.0),
        SUPPORT_VEHICLE_BAR4_TRB("Support Vehicle (BAR 4, TR B)", 150000.0/50.0, 62.5/50.0, 50.0),
        SUPPORT_VEHICLE_BAR4_TRC("Support Vehicle (BAR 4, TR C)", 150000.0/32.0, 62.5/32.0, 32.0),
        SUPPORT_VEHICLE_BAR4_TRD("Support Vehicle (BAR 4, TR D)", 150000.0/26.0, 62.5/26.0, 26.0),
        SUPPORT_VEHICLE_BAR4_TRE("Support Vehicle (BAR 4, TR E)", 150000.0/23.0, 62.5/23.0, 23.0),
        SUPPORT_VEHICLE_BAR4_TRF("Support Vehicle (BAR 4, TR F)", 150000.0/21.0, 62.5/21.0, 21.0),
        SUPPORT_VEHICLE_BAR5_TRB("Support Vehicle (BAR 5, TR B)", 200000.0/63.0, 62.5/63.0, 63.0),
        SUPPORT_VEHICLE_BAR5_TRC("Support Vehicle (BAR 5, TR C)", 200000.0/40.0, 62.5/40.0, 40.0),
        SUPPORT_VEHICLE_BAR5_TRD("Support Vehicle (BAR 5, TR D)", 200000.0/32.0, 62.5/32.0, 32.0),
        SUPPORT_VEHICLE_BAR5_TRE("Support Vehicle (BAR 5, TR E)", 200000.0/28.0, 62.5/28.0, 28.0),
        SUPPORT_VEHICLE_BAR5_TRF("Support Vehicle (BAR 5, TR F)", 200000.0/26.0, 62.5/26.0, 26.0),
        SUPPORT_VEHICLE_BAR6_TRC("Support Vehicle (BAR 6, TR C)", 250000.0/48.0, 62.5/48.0, 48.0),
        SUPPORT_VEHICLE_BAR6_TRD("Support Vehicle (BAR 6, TR D)", 250000.0/38.0, 62.5/38.0, 38.0),
        SUPPORT_VEHICLE_BAR6_TRE("Support Vehicle (BAR 6, TR E)", 250000.0/34.0, 62.5/34.0, 34.0),
        SUPPORT_VEHICLE_BAR6_TRF("Support Vehicle (BAR 6, TR F)", 250000.0/32.0, 62.5/32.0, 32.0),
        SUPPORT_VEHICLE_BAR7_TRC("Support Vehicle (BAR 7, TR C)", 300000.0/56.0, 62.5/56.0, 56.0),
        SUPPORT_VEHICLE_BAR7_TRD("Support Vehicle (BAR 7, TR D)", 300000.0/45.0, 62.5/45.0, 45.0),
        SUPPORT_VEHICLE_BAR7_TRE("Support Vehicle (BAR 7, TR E)", 300000.0/40.0, 62.5/40.0, 40.0),
        SUPPORT_VEHICLE_BAR7_TRF("Support Vehicle (BAR 7, TR F)", 300000.0/37.0, 62.5/37.0, 37.0),
        SUPPORT_VEHICLE_BAR8_TRD("Support Vehicle (BAR 8, TR D)", 400000.0/51.0, 62.5/51.0, 51.0),
        SUPPORT_VEHICLE_BAR8_TRE("Support Vehicle (BAR 8, TR E)", 400000.0/45.0, 62.5/45.0, 45.0),
        SUPPORT_VEHICLE_BAR8_TRF("Support Vehicle (BAR 8, TR F)", 400000.0/42.0, 62.5/42.0, 42.0),
        SUPPORT_VEHICLE_BAR9_TRD("Support Vehicle (BAR 9, TR D)", 500000.0/57.0, 62.5/57.0, 57.0),
        SUPPORT_VEHICLE_BAR9_TRE("Support Vehicle (BAR 9, TR E)", 500000.0/51.0, 62.5/51.0, 51.0),
        SUPPORT_VEHICLE_BAR9_TRF("Support Vehicle (BAR 9, TR F)", 500000.0/47.0, 62.5/47.0, 47.0),
        SUPPORT_VEHICLE_BAR10_TRD("Support Vehicle (BAR 10, TR D)", 625000.0/63.0, 62.5/63.0, 63.0),
        SUPPORT_VEHICLE_BAR10_TRE("Support Vehicle (BAR 10, TR E)", 625000.0/56.0, 62.5/56.0, 56.0),
        SUPPORT_VEHICLE_BAR10_TRF("Support Vehicle (BAR 10, TR F)", 625000.0/52.0, 62.5/52.0, 52.0);

        /** EquipmentType ID */
        public final int id;
        public final String name;
        public final double costPerTon;
        public final double pointMultiplier;
        /** For fractional accounting and patchwork armor */
        public final double kgPerPoint;
        
        public static ArmorType byId(int id, boolean clan) {
            switch(id) {
                case EquipmentType.T_ARMOR_STANDARD: return clan ? STANDARD_CLAN : STANDARD;
                case EquipmentType.T_ARMOR_STEALTH: return STEALTH;
                case EquipmentType.T_ARMOR_LIGHT_FERRO: return LIGHT_FERRO;
                case EquipmentType.T_ARMOR_FERRO_FIBROUS: return clan ? FERRO_FIBROUS_CLAN : FERRO_FIBROUS;
                case EquipmentType.T_ARMOR_HEAVY_FERRO: return HEAVY_FERRO;
                case EquipmentType.T_ARMOR_HEAVY_INDUSTRIAL: return HEAVY_INDUSTRIAL;
                case EquipmentType.T_ARMOR_INDUSTRIAL: return INDUSTRIAL;
                case EquipmentType.T_ARMOR_COMMERCIAL: return COMMERCIAL;
                case EquipmentType.T_ARMOR_LIGHT_ALUM: return LIGHT_ALUM;
                case EquipmentType.T_ARMOR_ALUM: return clan ? ALUM_CLAN : ALUM;
                case EquipmentType.T_ARMOR_HEAVY_ALUM: return HEAVY_ALUM;
                case EquipmentType.T_ARMOR_BA_STANDARD: return clan ? BA_STANDARD_CLAN : BA_STANDARD;
                case EquipmentType.T_ARMOR_BA_STANDARD_ADVANCED: return BA_STANDARD_ADVANCED;
                case EquipmentType.T_ARMOR_BA_STANDARD_PROTOTYPE: return BA_STANDARD_PROTOTYPE;
                case EquipmentType.T_ARMOR_BA_STEALTH_BASIC: return clan ? BA_STEALTH_BASIC_CLAN : BA_STEALTH_BASIC;
                case EquipmentType.T_ARMOR_BA_STEALTH_IMP: return clan ? BA_STEALTH_IMP_CLAN : BA_STEALTH_IMP;
                case EquipmentType.T_ARMOR_BA_STEALTH_PROTOTYPE: return BA_STEALTH_PROTOTYPE;
                case EquipmentType.T_ARMOR_BA_STEALTH: return clan ? BA_STEALTH_CLAN : BA_STEALTH;
                case EquipmentType.T_ARMOR_BA_FIRE_RESIST: return BA_FIRE_RESIST;
                case EquipmentType.T_ARMOR_BA_MIMETIC: return BA_MIMETIC;
                case EquipmentType.T_ARMOR_FERRO_LAMELLOR: return FERRO_LAMELLOR;
                case EquipmentType.T_ARMOR_HARDENED: return HARDENED;
                case EquipmentType.T_ARMOR_REFLECTIVE: return clan ? REFLECTIVE_CLAN : REFLECTIVE;
                case EquipmentType.T_ARMOR_REACTIVE: return clan ? REACTIVE_CLAN : REACTIVE;
                case EquipmentType.T_ARMOR_STEALTH_VEHICLE: return STEALTH_VEHICLE;
                case EquipmentType.T_ARMOR_BA_REACTIVE: return clan ? BA_REACTIVE_CLAN : BA_REACTIVE;
                case EquipmentType.T_ARMOR_BA_REFLECTIVE: return clan ? BA_REFLECTIVE_CLAN : BA_REFLECTIVE;
                case EquipmentType.T_ARMOR_FERRO_IMP: return clan ? FERRO_IMP_CLAN : FERRO_IMP;
                case EquipmentType.T_ARMOR_FERRO_CARBIDE: return clan ? FERRO_CARBIDE_CLAN : FERRO_CARBIDE;
                case EquipmentType.T_ARMOR_LAMELLOR_FERRO_CARBIDE: return clan ? LAMELLOR_FERRO_CARBIDE_CLAN : LAMELLOR_FERRO_CARBIDE;
                case EquipmentType.T_ARMOR_EDP: return EDP;
                case EquipmentType.T_ARMOR_FERRO_FIBROUS_PROTO: return FERRO_FIBROUS_PROTO;
                case EquipmentType.T_ARMOR_FERRO_ALUM_PROTO: return FERRO_ALUM_PROTO;
                case EquipmentType.T_ARMOR_ANTI_PENETRATIVE_ABLATION: return ANTI_PENETRATIVE_ABLATION;
                case EquipmentType.T_ARMOR_BALLISTIC_REINFORCED: return BALLISTIC_REINFORCED;
                case EquipmentType.T_ARMOR_HEAT_DISSIPATING: return HEAT_DISSIPATING;
                case EquipmentType.T_ARMOR_IMPACT_RESISTANT: return IMPACT_RESISTANT;
                case EquipmentType.T_ARMOR_PRIMITIVE: return PRIMITIVE;
                default: return PRIMITIVE;
            }
        }
        
        private ArmorType(int id) {
            this(id, "", EquipmentType.armorPointMultipliers[id]);
        }
        
        private ArmorType(int id, double kgPerPoint) {
            this(id, "", EquipmentType.armorPointMultipliers[id], kgPerPoint);
        }
        
        private ArmorType(int id, String nameSuffix, double pointMultiplier) {
            this(id, EquipmentType.armorNames[id] + nameSuffix, EquipmentType.armorCosts[id],
                pointMultiplier, 62.5 / pointMultiplier);
        }
        
        private ArmorType(int id, String nameSuffix, double pointMultiplier, double kgPerPoint) {
            this(id, EquipmentType.armorNames[id] + nameSuffix, EquipmentType.armorCosts[id],
                pointMultiplier, kgPerPoint);
        }
        
        // Armor types MegaMek doesn't have
        private ArmorType(String name, double costPerTon, double pointMultiplier) {
            this(-1, name, costPerTon, pointMultiplier, 62.5 / pointMultiplier);
        }
        
        private ArmorType(String name, double costPerTon, double pointMultiplier, double kgPerPoint) {
            this(-1, name, costPerTon, pointMultiplier, kgPerPoint);
        }

        private ArmorType(int id, String name, double costPerTon, double pointMultiplier, double kgPerPoint) {
            this.id = id;
            this.name = name;
            this.costPerTon = costPerTon;
            this.pointMultiplier = pointMultiplier;
            this.kgPerPoint = kgPerPoint;
        }
    }
}
