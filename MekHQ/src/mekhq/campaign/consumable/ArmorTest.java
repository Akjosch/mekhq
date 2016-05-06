package mekhq.campaign.consumable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import mekhq.campaign.consumable.Armors.ArmorList;
import mekhq.campaign.material.Materials;

public final class ArmorTest {

    public static void main(String[] args) {
        File materialFile = new File("data/universe/materials.xml");
        try(InputStream is = new FileInputStream(materialFile)) {
            Materials.loadMaterials(is);
        } catch(FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        File armorFile = new File("data/universe/armors.xml");
        try(InputStream is = new FileInputStream(armorFile)) {
            Armors.ArmorList list = (ArmorList) Armors.unmarshaller.unmarshal(is);
            for(Armor armor : list.armors) {
                Armors.writeArmor(armor, System.out);
            }
        } catch(FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
