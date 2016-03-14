package mekhq.campaign.universe;

import java.io.Serializable;
import java.util.Comparator;

public final class SpectralTypeComparator implements Comparator<String>, Serializable {
	private static final long serialVersionUID = -9125023805505796234L;

	@Override
	public int compare(String sc1, String sc2) {
		final Star.SpectralDefinition scDef1 = StarUtil.parseSpectralType(sc1);
		final Star.SpectralDefinition scDef2 = StarUtil.parseSpectralType(sc2);
		if( scDef1.spectralClass < scDef2.spectralClass ) {
			return -1;
		}
		if( scDef1.spectralClass > scDef2.spectralClass ) {
			return 1;
		}
		if( scDef1.subtype < scDef2.subtype ) {
			return -1;
		}
		if( scDef1.subtype > scDef2.subtype ) {
			return 1;
		}
		final int lum1 = luminosityValue(scDef1.luminosity);
		final int lum2 = luminosityValue(scDef2.luminosity);
		if( lum1 < lum2 ) {
			return -1;
		}
		if( lum1 > lum2 ) {
			return 1;
		}
		// String comparison, to catch additional spectral type designators
		return sc1.compareTo(sc2);
	}
	
	private int luminosityValue(String lum) {
		switch(lum) {
			case Star.LUM_0: return 0;
			case Star.LUM_IA: return 1;
			case Star.LUM_I: return 2;
			case Star.LUM_IAB: return 3;
			case Star.LUM_IB: return 4;
			case Star.LUM_II_EVOLVED: return 5;
			case Star.LUM_II: return 6;
			case Star.LUM_III_EVOLVED: return 7;
			case Star.LUM_III: return 8;
			case Star.LUM_IV_EVOLVED: return 9;
			case Star.LUM_IV: return 10;
			case Star.LUM_V_EVOLVED: return 11;
			case Star.LUM_V: return 12;
			case Star.LUM_VI: return 13;
			case Star.LUM_VI_PLUS: return 14;
			case Star.LUM_VII: return 15;
			default: return 99;
		}
	}
}
