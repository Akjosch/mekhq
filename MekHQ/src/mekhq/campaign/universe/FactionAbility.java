package mekhq.campaign.universe;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Abilities from IO. Might be useful to modify campaign behavior of those.
 */
@XmlRootElement(name="factionAbility")
public class FactionAbility {
	// Defaults
	public final static FactionAbility BOOMING_ECONOMY = new FactionAbility("booming_economy").setResourcePointMod(10);
	public final static FactionAbility CLOSED_STATE = new FactionAbility("closed_state").setDesertionCheckMod(-2).setMoraleCheckMod(1);
	public final static FactionAbility DECENTRALIZED_STATE = new FactionAbility("decentralized_state"){
		@Override
		public int getMoraleCheckMod(Planet planet, boolean isAttacking) {
			return /* planet.isOriginallyOwnedBy(faction) && */ !isAttacking ? -2 : 0;
		}
	}.setResourcePointMod(-20);
	public final static FactionAbility DESPOTIC_STATE = new FactionAbility("despotic_state").setResourcePointMod(-10);
	public final static FactionAbility DUG_IN = new FactionAbility("dug_in"){
		@Override
		public int getLossesMod(Planet planet, boolean isAttacking) {
			return /* planet.isOriginallyOwnedBy(faction) && */ !isAttacking ? -10 : 0;
		}
		
		@Override
		public int getDamageMod(Planet planet, boolean isAttacking) {
			return /* planet.isOriginallyOwnedBy(faction) && */ !isAttacking ? 5 : 0;
		}
	};
	public final static FactionAbility FANATICAL_DEFENSE = new FactionAbility("fanatical_defense"){
		@Override
		public int getMoraleCheckMod(Planet planet, boolean isAttacking) {
			return /* planet.isOriginallyOwnedBy(faction) && */ !isAttacking ? -2 : 0;
		}

		@Override
		public int getLossesMod(Planet planet, boolean isAttacking) {
			return /* planet.isOriginallyOwnedBy(faction) && */ !isAttacking ? 10 : 0;
		}
		
		@Override
		public int getDamageMod(Planet planet, boolean isAttacking) {
			return /* planet.isOriginallyOwnedBy(faction) && */ !isAttacking ? 10 : 0;
		}
	};
	public final static FactionAbility FANATICAL_OFFENSE = new FactionAbility("fanatical_offense") {
		@Override
		public int getMoraleCheckMod(Planet planet, boolean isAttacking) {
			return isAttacking ? -2 : 0;
		}

		@Override
		public int getLossesMod(Planet planet, boolean isAttacking) {
			return isAttacking ? 10 : 0;
		}
		
		@Override
		public int getDamageMod(Planet planet, boolean isAttacking) {
			return isAttacking ? 10 : 0;
		}
	};
	public final static FactionAbility FLAWED_DOCTRINE = new FactionAbility("flawed_doctrine").setEngagementRollMod(1).setInitiativeMod(-1);
	public final static FactionAbility INFERIOR_BLACK_OPS = new FactionAbility("inferior_black_ops"); // TODO
	public final static FactionAbility JURY_RIG_EXPERTS = new FactionAbility("jury_rig_experts"); // TODO
	public final static FactionAbility LOGISTICS_EXPERTS = new FactionAbility("logistics_experts"); // TODO
	public final static FactionAbility LOSTECH = new FactionAbility("lostech"); // TODO
	public final static FactionAbility MERCHANT_KINGS = new FactionAbility("merchant_kings").setResourcePointMod(20);
	public final static FactionAbility OPEN_STATE = new FactionAbility("open_state").setMoraleCheckMod(-1); // TODO easier to infiltrate
	public final static FactionAbility PARLIAMENTARY_CHAOS = new FactionAbility("parilamentary_chaos"); // TODO This one will be "fun"
	public final static FactionAbility POISON_PILL = new FactionAbility("poison_pill"); // TODO
	public final static FactionAbility PRODUCTION_SPEC_BATTLEMECH = new FactionAbility("production_specialist_battlemech"); // TODO
	public final static FactionAbility PRODUCTION_SPEC_AEROSPACE = new FactionAbility("production_specialist_aerospace"); // TODO
	public final static FactionAbility PRODUCTION_SPEC_ARMOR = new FactionAbility("production_specialist_armor"); // TODO
	public final static FactionAbility PRODUCTION_SPEC_INFANTRY = new FactionAbility("production_specialist_infantry"); // TODO
	public final static FactionAbility PRODUCTION_SPEC_ARTILLERY = new FactionAbility("production_specialist_artillery"); // TODO
	public final static FactionAbility PRODUCTION_ISSUES_BATTLEMECH = new FactionAbility("production_issues_battlemech"); // TODO
	public final static FactionAbility PRODUCTION_ISSUES_AEROSPACE = new FactionAbility("production_issues_aerospace"); // TODO
	public final static FactionAbility PRODUCTION_ISSUES_ARMOR = new FactionAbility("production_issues_armor"); // TODO
	public final static FactionAbility PRODUCTION_ISSUES_INFANTRY = new FactionAbility("production_issues_infantry"); // TODO
	public final static FactionAbility PRODUCTION_ISSUES_ARTILLERY = new FactionAbility("production_issues_artillery"); // TODO
	public final static FactionAbility SUPERIOR_BLACK_OPS = new FactionAbility("superior_black_ops"); // TODO
	public final static FactionAbility SUPERIOR_DOCTRINE = new FactionAbility("superior_doctrine").setEngagementRollMod(-1); // TODO To-Hit reduction
	public final static FactionAbility SUPPLY_PROBLEMS = new FactionAbility("supply_problems"); // TODO
	public final static FactionAbility STALLED_ECONOMY = new FactionAbility("stalled_economy").setResourcePointMod(-10);
	public final static FactionAbility STATE_RUN = new FactionAbility("state_run").setResourcePointMod(25); // TODO -1 initiative on attack
	public final static FactionAbility UNSTEADY_GROUND = new FactionAbility("unsteady_ground"); // TODO
	public final static FactionAbility UNSTEADY_AERO = new FactionAbility("unsteady_aero"); // TODO
	
	@XmlElement
	private String id;
	@XmlElement
	public Integer resourcePointMod = null;
	@XmlElement
	public Integer desertionCheckMod = null;
	@XmlElement
	public Integer moraleCheckMod = null;
	@XmlElement
	public Integer engagementRollMod = null;
	@XmlElement
	public Integer initiativeMod = null;
	
	protected FactionAbility(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public int getResourcePointMod() {
		return null != resourcePointMod ? resourcePointMod.intValue() : 0;
	}
	
	protected FactionAbility setResourcePointMod(int mod) {
		resourcePointMod = Integer.valueOf(mod);
		return this;
	}
	
	public int getDesertionCheckMod() {
		return null != desertionCheckMod ? desertionCheckMod.intValue() : 0;
	}
	
	protected FactionAbility setDesertionCheckMod(int mod) {
		desertionCheckMod = Integer.valueOf(mod);
		return this;
	}

	protected final int getMoraleCheckMod() {
		return null != moraleCheckMod ? moraleCheckMod.intValue() : 0;
	}
	
	public int getMoraleCheckMod(Planet planet, boolean isAttacking) {
		return getMoraleCheckMod();
	}
	
	protected FactionAbility setMoraleCheckMod(int mod) {
		moraleCheckMod = Integer.valueOf(mod);
		return this;
	}

	public int getEngagementRollMod() {
		return null != engagementRollMod ? engagementRollMod.intValue() : 0;
	}
	
	protected FactionAbility setEngagementRollMod(int mod) {
		engagementRollMod = Integer.valueOf(mod);
		return this;
	}

	public int getInitiativeMod() {
		return null != initiativeMod ? initiativeMod.intValue() : 0;
	}
	
	protected FactionAbility setInitiativeMod(int mod) {
		initiativeMod = Integer.valueOf(mod);
		return this;
	}

	/** Damages to this unit in combat */
	public int getLossesMod(Planet planet, boolean isAttacking) {
		return 0;
	}
	
	/** Damage to the enemy */
	public int getDamageMod(Planet planet, boolean isAttacking) {
		return 0;
	}

}
