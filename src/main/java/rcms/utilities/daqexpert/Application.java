package rcms.utilities.daqexpert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Application {

	public static final String NM_URL = "nm.url";
	public static final String NM_API_CREATE = "nm.api.create";
	public static final String NM_API_CLOSE = "nm.api.close";
	public static final String SNAPSHOTS_DIR = "snapshots";

	private final Properties prop;

	public static Application get() {
		if (instance == null) {
			throw new RuntimeException("Not initialized");
		}
		return instance;
	}

	public static void initialize(String propertiesFile) {
		String message = "Required property missing ";
		instance = new Application(propertiesFile);
		if (!instance.prop.containsKey(NM_URL))
			throw new RuntimeException(message + NM_URL);
		if (!instance.prop.containsKey(SNAPSHOTS_DIR))
			throw new RuntimeException(message + SNAPSHOTS_DIR);
		if (!instance.prop.containsKey(NM_API_CREATE))
			throw new RuntimeException(message + NM_API_CREATE);
		if (!instance.prop.containsKey(NM_API_CLOSE))
			throw new RuntimeException(message + NM_API_CLOSE);
	}

	private Application(String propertiesFile) {
		prop = load(propertiesFile);
	}

	private static Application instance;

	private Properties load(String propertiesFile) {

		try {
			FileInputStream propertiesInputStream = new FileInputStream(propertiesFile);
			Properties properties = new Properties();
			properties.load(propertiesInputStream);

			return properties;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot run application without configuration file");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot run application without configuration file");
		}
	}

	public Properties getProp() {
		return prop;
	}
}
