/*
 * Factory.java
 * 
 * Copyright (c) 2009 Jay Lawson <jaylawson39 at yahoo.com>. All rights reserved.
 * Written by Dylan Myers <ralgith@gmail.com>
 * 
 * This file is part of MekHQ.
 * 
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */

package mekhq.campaign.universe;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.transform.stream.StreamSource;

import mekhq.MekHQ;
import mekhq.adapters.BooleanValueAdapter;

@XmlRootElement(name="factory")
@XmlAccessorType(XmlAccessType.FIELD)
public class Factory {
	private static Marshaller marshaller;
	private static Unmarshaller unmarshaller;
	static {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Factory.class);
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
			unmarshaller = jaxbContext.createUnmarshaller();
		} catch(JAXBException e) {
			MekHQ.logError(e);
		}
	}
	
	private String shortname;
	private String fullname;
	private String nameGenerator;
	private String owner; // Faction shortname that owns it
	private String planet;
	@XmlJavaTypeAdapter(BooleanValueAdapter.class)
	@XmlElement(defaultValue="false")
	private boolean clan;
	
	// TODO: Need some more variables here for holding units and components produced
	
	public Factory() {
		this("GF", "Generic Factory");
	}
	
	public Factory(String sname, String fname) {
		shortname = sname;
		fullname = fname;
		nameGenerator = "General";
		owner = "IND";
		planet = "";
		clan = false;
	}

	public String getShortName() {
		return shortname;
	}

	public String getFullName() {
		return fullname;
	}
	
	public String getOwnerId() {
		return owner;
	}
	
	public Faction getOwner() {
		return Faction.getFaction(owner);
	}
	
	public String getPlanetId() {
		return planet;
	}
	
	public Planet getPlanet() {
		return Planets.getInstance().getPlanetById(planet);
	}
	
	public boolean isClan() {
		return clan;
	}
	
	public void setClan(boolean tf) {
		clan = tf;
	}
	
	public String getNameGenerator() {
		return nameGenerator;
	}
    
	public void writeToXml(OutputStream os) {
		try {
			marshaller.marshal(this, os);
		} catch (Exception e) {
			MekHQ.logError(e);
		}
	}

    public static Factory getFromXML(InputStream is) {
    	try {
			return unmarshaller.unmarshal(new StreamSource(is), Factory.class).getValue();
		} catch(JAXBException e) {
			MekHQ.logError(e);
		}
		return null;
	}
}