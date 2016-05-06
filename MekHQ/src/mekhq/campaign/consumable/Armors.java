package mekhq.campaign.consumable;

import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import mekhq.MekHQ;

public class Armors {
    // Marshaller / unmarshaller instances
    private static Marshaller marshaller;
    public static Unmarshaller unmarshaller;
    static {
        try {
            JAXBContext context = JAXBContext.newInstance(ArmorList.class, Armor.class);
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            unmarshaller = context.createUnmarshaller();
            // For debugging only!
            unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
        } catch(JAXBException e) {
            MekHQ.logError(e);
        }
    }

    public static void writeArmor(Armor armor, OutputStream os) {
        try {
            marshaller.marshal(armor, os);
        } catch(JAXBException e) {
            MekHQ.logError(e);
        }
    }
    

    @XmlRootElement(name="armors")
    public static class ArmorList {
        @XmlElementRef
        public List<Armor> armors;
    }

}
