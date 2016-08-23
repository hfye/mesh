package com.gentics.mesh.demo;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.gentics.mesh.cli.BootstrapInitializer;
import com.gentics.mesh.error.MeshSchemaException;
import com.gentics.mesh.search.SearchProvider;

public class DemoDumpGenerator {

	public static void main(String[] args) throws Exception {
		new DemoDumpGenerator().dump();
	}

	static {
		System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "dump");
	}

	private void dump() throws Exception {
		// Cleanup in preparation for dumping the demo data
		cleanup();
		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DemoDumpConfiguration.class)) {
			ctx.start();
			ctx.registerShutdownHook();
			// Initialize mesh
			BootstrapInitializer boot = ctx.getBean(BootstrapInitializer.class);
			DemoDataProvider provider = ctx.getBean("demoDataProvider", DemoDataProvider.class);
			SearchProvider searchProvider = ctx.getBean("searchProvider", SearchProvider.class);
			invokeDump(boot, provider);
			searchProvider.getNode().close();
			System.exit(0);
		}

	}

	/**
	 * Cleanup the dump directories and remove the existing mesh config.
	 * 
	 * @throws IOException
	 */
	public void cleanup() throws IOException {
		FileUtils.deleteDirectory(new File("target" + File.separator + "dump"));
		File confFile = new File("mesh.json");
		if (confFile.exists()) {
			confFile.delete();
		}
	}

	/**
	 * Invoke the demo database setup.
	 * 
	 * @param boot
	 * @param provider
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws MeshSchemaException
	 * @throws InterruptedException
	 */
	public void invokeDump(BootstrapInitializer boot, DemoDataProvider provider)
			throws JsonParseException, JsonMappingException, IOException, MeshSchemaException, InterruptedException {
		boot.initMandatoryData();
		boot.initPermissions();
		//boot.initSearchIndex();
		//boot.invokeChangelog();

		// Setup demo data
		provider.setup();
		//boot.initSearchIndex();
	}

}
