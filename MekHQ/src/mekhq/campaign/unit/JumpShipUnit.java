package mekhq.campaign.unit;

import java.io.PrintWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.w3c.dom.Node;

import mekhq.adapters.SpaceLocationAdapter;
import mekhq.campaign.universe.SpaceLocation;

/**
 * Basic implementation of a jump ship, for now only tracking location and charge.
 */
@XmlRootElement(name="jumpship")
public class JumpShipUnit {
	@XmlElement
	private double charge;
	@XmlElement
	@XmlJavaTypeAdapter(SpaceLocationAdapter.class)
	private SpaceLocation location;
	
	@XmlTransient
	public double getCharge() {
		return charge;
	}
	
	/** @return the remaining charge-up time in hours */
	@XmlTransient
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
	
	@XmlTransient
	public SpaceLocation getLocation() {
		return location;
	}
	
	public void setLocation(SpaceLocation location) {
		this.location = location;
	}
	
	public void writeToXml(PrintWriter pw) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(JumpShipUnit.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
			marshaller.marshal(this, pw);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static JumpShipUnit generateInstanceFromXML(Node wn) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(JumpShipUnit.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			unmarshaller.setProperty("jaxb.fragment", Boolean.TRUE);
			JumpShipUnit jumpShip = unmarshaller.unmarshal(wn, JumpShipUnit.class).getValue();
			return jumpShip;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
