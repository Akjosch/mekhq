package mekhq;

import java.io.FileInputStream;

// @FunctionalInterface in Java 8 - straight up java.util.function.Consumer<InputStream>
public interface FileParser {
	void parse(FileInputStream is);
}
