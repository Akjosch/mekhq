package mekhq.campaign.unit;

import java.io.PrintWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.w3c.dom.Node;

import mekhq.MekHQ;
import mekhq.adapters.SpaceLocationAdapter;
import mekhq.campaign.universe.SpaceLocation;

/**
 * Basic implementation of a jump ship, for now only tracking location and charge.
 */
@XmlRootElement(name="jumpship")
@XmlAccessorType(XmlAccessType.FIELD)
public class JumpShipUnit {
	private static Marshaller marshaller;
	private static Unmarshaller unmarshaller;
	static {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(JumpShipUnit.class);
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
			unmarshaller = jaxbContext.createUnmarshaller();
		} catch(JAXBException e) {
			MekHQ.logError(e);
		}
	}
	
	private double charge;
	@XmlJavaTypeAdapter(SpaceLocationAdapter.class)
	private SpaceLocation location;
	
	public double getCharge() {
		return charge;
	}
	
	/** @return the remaining charge-up time in hours */
	public double getRemainingChargeTime() {
		double localRechargeTime = null != location ? location.getRechargeTime() : Double.POSITIVE_INFINITY;
		if( Double.isInfinite(localRechargeTime) ) {
			return Double.POSITIVE_INFINITY;
		}
		return (1.0 - charge) * localRechargeTime;
	}
	
	public void setCharge(double charge) {
		if( charge < 0.0 ) {
			charge = 0.0;
		}
		if( charge > 1.0 ) {
			charge = 1.0;
		}
		this.charge = charge;
	}
	
	/** @return actual charge amount added */
	public double addCharge(double charge) {
		if( charge < 0.0 ) {
			return 0.0;
		}
		double previousCharge = this.charge;
		setCharge(this.charge + charge);
		return this.charge - previousCharge;
	}
	
	/** @return actual hours spent charging */
	public double addChargeHours(double hours) {
		double localRechargeTime = null != location ? location.getRechargeTime() : Double.POSITIVE_INFINITY;
		if( hours < 0.0 || Double.isInfinite(localRechargeTime) ) {
			return 0.0;
		}
		return addCharge(hours / localRechargeTime) * localRechargeTime;
	}
	
	public boolean isCharged() {
		return charge >= 1.0;
	}
	
	public SpaceLocation getLocation() {
		return location;
	}
	
	public void setLocation(SpaceLocation location) {
		this.location = location;
	}
	
	/** This method doesn't check if the jump is possible, just if the location is valid */
	public void jumpTo(SpaceLocation loc) {
		if( null == loc || !loc.isJumpPoint() ) {
			return;
		}
		this.location = loc;
		this.charge = 0.0;
	}
	
	public void writeToXml(PrintWriter pw) {
		try {
			marshaller.marshal(this, pw);
		} catch (Exception e) {
			MekHQ.logError(e);
		}
	}
	
	public static JumpShipUnit generateInstanceFromXML(Node wn) {
		try {
			return unmarshaller.unmarshal(wn, JumpShipUnit.class).getValue();
		} catch (Exception e) {
			MekHQ.logError(e);
		}
		return null;
	}
}
