package mekhq.adapters;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import megamek.common.EquipmentType;
import mekhq.campaign.universe.Planet;

public class SocioIndustrialDataAdapter extends XmlAdapter<String, Planet.SocioIndustrialData> {
	private static Map<String, Integer> stringToEquipmentTypeMap = new HashMap<String, Integer>(6);
	private static Map<Integer, String> equipmentTypeToStringMap = new HashMap<Integer, String>(6);
	static {
		stringToEquipmentTypeMap.put("A", EquipmentType.RATING_A);
		stringToEquipmentTypeMap.put("B", EquipmentType.RATING_B);
		stringToEquipmentTypeMap.put("C", EquipmentType.RATING_C);
		stringToEquipmentTypeMap.put("D", EquipmentType.RATING_D);
		stringToEquipmentTypeMap.put("E", EquipmentType.RATING_E);
		stringToEquipmentTypeMap.put("F", EquipmentType.RATING_F);
		equipmentTypeToStringMap.put(EquipmentType.RATING_A, "A");
		equipmentTypeToStringMap.put(EquipmentType.RATING_B, "B");
		equipmentTypeToStringMap.put(EquipmentType.RATING_C, "C");
		equipmentTypeToStringMap.put(EquipmentType.RATING_D, "D");
		equipmentTypeToStringMap.put(EquipmentType.RATING_E, "E");
		equipmentTypeToStringMap.put(EquipmentType.RATING_F, "F");
	}
	
	public static int convertRatingToCode(String rating) {
		Integer result = stringToEquipmentTypeMap.get(rating.toUpperCase(Locale.ROOT));
		return null != result ? result.intValue() : EquipmentType.RATING_C;
	}
	
	public static String convertCodeToRating(int code) {
		String result = equipmentTypeToStringMap.get(code);
		return null != result ? result : "?";
	}
	
	@Override
	public Planet.SocioIndustrialData unmarshal(String v) throws Exception {
		String[] socio = v.split("-");
		Planet.SocioIndustrialData result = new Planet.SocioIndustrialData();
		if(socio.length >= 5) {
			result.tech = convertRatingToCode(socio[0]);
			result.industry = convertRatingToCode(socio[1]);
			result.rawMaterials = convertRatingToCode(socio[2]);
			result.output = convertRatingToCode(socio[3]);
			result.agriculture = convertRatingToCode(socio[4]);
		}
		return result;
	}

	@Override
	public String marshal(Planet.SocioIndustrialData v) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(convertCodeToRating(v.tech)).append("-");
		sb.append(convertCodeToRating(v.industry)).append("-");
		sb.append(convertCodeToRating(v.rawMaterials)).append("-");
		sb.append(convertCodeToRating(v.output)).append("-");
		sb.append(convertCodeToRating(v.agriculture));
		return sb.toString();
	}

}
