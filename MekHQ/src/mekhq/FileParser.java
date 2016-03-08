package mekhq;

import java.io.InputStream;

// @FunctionalInterface in Java 8 - straight up Consumer<InputStream>
public interface FileParser {
	void parse(InputStream is);
}
