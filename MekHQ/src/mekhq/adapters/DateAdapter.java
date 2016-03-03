package mekhq.adapters;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date> {
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public Date unmarshal(final String xml) throws Exception {
		return dateFormat.parse(xml);
	}

	@Override
	public String marshal(final Date object) throws Exception {
		return dateFormat.format(object);
	}
}
