/*
 * Garrison.java
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

@XmlRootElement(name="garrison")
@XmlAccessorType(XmlAccessType.FIELD)
public class Garrison {
	private static Marshaller marshaller;
	private static Unmarshaller unmarshaller;
	static {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Garrison.class);
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
			unmarshaller = jaxbContext.createUnmarshaller();
		} catch(JAXBException e) {
			MekHQ.logError(e);
		}
	}
	
	private String shortname;
	private String fullname;
	@XmlElement(defaultValue="General")
	private String nameGenerator;
	@XmlJavaTypeAdapter(BooleanValueAdapter.class)
	@XmlElement(defaultValue="false")
	private boolean clan;
	
	public Garrison() {
		this("SGU", "Some Garrison Unit");
	}
	
	public Garrison(String sname, String fname) {
		shortname = sname;
		fullname = fname;
		nameGenerator = "General";
		clan = false;
	}

	public String getShortName() {
		return shortname;
	}

	public String getFullName() {
		return fullname;
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

    public static Garrison getFromXML(InputStream is) {
    	try {
			return unmarshaller.unmarshal(new StreamSource(is), Garrison.class).getValue();
		} catch(JAXBException e) {
			MekHQ.logError(e);
		}
		return null;
	}
}