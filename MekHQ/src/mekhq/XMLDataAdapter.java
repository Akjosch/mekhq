package mekhq;

import org.w3c.dom.Node;

/**
 * Simple XML data parsers
 */
public interface XMLDataAdapter<T> {
	public static XMLDataAdapter<String> STRING = new XMLDataAdapter<String>() {
		@Override public String parse(Node n) { return n.getTextContent(); }
	};
	public static XMLDataAdapter<Integer> INT = new XMLDataAdapter<Integer>() {
		@Override public Integer parse(Node n) { return Integer.parseInt(n.getTextContent()); }
	};
	public static XMLDataAdapter<Double> DOUBLE = new XMLDataAdapter<Double>() {
		@Override public Double parse(Node n) { return Double.parseDouble(n.getTextContent()); }
	};
	public static XMLDataAdapter<Boolean> BOOL = new XMLDataAdapter<Boolean>() {
		@Override public Boolean parse(Node n) { return Boolean.parseBoolean(n.getTextContent()); }
	};

	T parse(Node n);
}
