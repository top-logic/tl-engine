/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.element.PanelElement;

/**
 * Tests that re-parsing a view XML file after modification on disk produces updated configuration.
 *
 * <p>
 * This verifies the core behavior that cache invalidation relies on: when a {@code .view.xml} file
 * changes, parsing it again yields the new content.
 * </p>
 */
public class TestViewReload extends TestCase {

	/**
	 * Tests that modifying a view XML file and re-parsing it yields the updated configuration.
	 */
	public void testReloadOnFileChange() throws Exception {
		File v1 = toFile("reload-v1.view.xml");
		File v2 = toFile("reload-v2.view.xml");

		// Use the module's target directory for temp files to avoid /tmp access issues.
		File targetDir = new File("target");
		targetDir.mkdirs();
		File tempFile = File.createTempFile("view-reload-test-", ".view.xml", targetDir);
		try {
			// Copy v1 to temp file and parse.
			Files.copy(v1.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			ViewElement.Config config1 = parseViewFile(tempFile);
			PanelElement.Config panel1 = (PanelElement.Config) config1.getContent().get(0);
			assertEquals(ResKey.builder().add(Locale.ENGLISH, "Version 1").build(), panel1.getTitle());

			// Wait to ensure the file timestamp changes (ext4 has 1-second resolution).
			long timestampBefore = tempFile.lastModified();
			Thread.sleep(1100);

			// Copy v2 over the temp file.
			Files.copy(v2.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			long timestampAfter = tempFile.lastModified();
			assertTrue("File timestamp must have changed after overwrite",
				timestampAfter > timestampBefore);

			// Re-parse and verify updated content.
			ViewElement.Config config2 = parseViewFile(tempFile);
			PanelElement.Config panel2 = (PanelElement.Config) config2.getContent().get(0);
			assertEquals(ResKey.builder().add(Locale.ENGLISH, "Version 2").build(), panel2.getTitle());
		} finally {
			tempFile.delete();
		}
	}

	private ViewElement.Config parseViewFile(File file) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestViewReload.class);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource((Content) BinaryDataFactory.createBinaryData(file));

		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();
		return config;
	}

	private File toFile(String name) throws URISyntaxException {
		URL url = TestViewReload.class.getResource(name);
		assertNotNull("Test resource not found: " + name, url);
		return new File(url.toURI());
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestViewReload.class, TypeIndex.Module.INSTANCE);
	}
}
