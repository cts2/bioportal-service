package edu.mayo.cts2.framework.plugin.service.bioportal.profile;

import java.io.File;

import edu.mayo.cts2.framework.core.config.PluginConfig;
import edu.mayo.cts2.framework.core.config.TestServerContext;
import edu.mayo.cts2.framework.core.config.option.OptionHolder;

public class TestPluginConfig extends PluginConfig {

	protected TestPluginConfig() {
		super(new OptionHolder(null), new File("src/test/resources/cache"), new TestServerContext());
	}

}
