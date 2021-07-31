package me.neznamy.tab.api.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.yamlassist.YamlAssist;

/**
 * YAML implementation of ConfigurationFile
 */
public class YamlConfigurationFile extends ConfigurationFile {
	
	//instance of snakeyaml
	private Yaml yaml;

	/**
	 * Constructs new instance and tries to load configuration file
	 * @param source - source to copy file from if it does not exist
	 * @param destination - destination of the file to be copied file to if needed and loaded
	 * @throws IllegalStateException - when file does not exist and source is null
	 * @throws YAMLException - when file has invalid yaml syntax
	 * @throws IOException - when an I/O operation with the file fails
	 */
	@SuppressWarnings("unchecked")
	public YamlConfigurationFile(InputStream source, File destination) throws YAMLException, IOException {
		super(source, destination);
		FileInputStream input = null;
		try {
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			yaml = new Yaml(options);
			input = new FileInputStream(file);
			values = (Map<String, Object>) yaml.load(input);
			if (values == null) values = new LinkedHashMap<>();
			input.close();
		} catch (YAMLException e) {
			if (input != null) input.close();
			TabAPI tab = TabAPI.getInstance();
			tab.getErrorManager().startupWarn("File " + destination + " has broken syntax.");
			tab.setBrokenFile(file.getPath());
			tab.getPlatform().sendConsoleMessage("&6[TAB] Error message from yaml parser: " + e.getMessage(), true);
			List<String> suggestions = YamlAssist.getSuggestions(file);
			if (!suggestions.isEmpty()) {
				tab.getPlatform().sendConsoleMessage("&d[TAB] Suggestions to fix yaml syntax:", true);
				for (String suggestion : suggestions) {
					tab.getPlatform().sendConsoleMessage("&d[TAB] - " + suggestion, true);
				}
			}
			throw e;
		}
	}
	
	@Override
	public void save() {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			yaml.dump(values, writer);
			writer.close();
			fixHeader();
		} catch (IOException e) {
			TabAPI.getInstance().getErrorManager().criticalError("Failed to save yaml file " + file.getPath() + " with content " + values.toString(), e);
		}
	}
}