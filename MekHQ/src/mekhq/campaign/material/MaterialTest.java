package mekhq.campaign.material;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class MaterialTest {

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
        System.out.println(Materials.getMaterial("X").getValue());
        System.out.println(Materials.getMaterials(MaterialUsage.AMMO).size());
        System.out.println(Materials.getMaterials(MaterialUsage.CRAFT).size());
        Materials.writeMaterial(Materials.getMaterial("X"), System.out);
        Materials.writeMaterial(Materials.getMaterial("X_BASE"), System.out);
        Materials.writeMaterial(Materials.getMaterial("WATER"), System.out);
    }

}
