/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileSystemCache;
import com.top_logic.basic.io.PathUpdate;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.SimpleComponent;
import com.top_logic.mig.html.layout.TLLayout;

/**
 * Tests the {@link LayoutStorage}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TestLayoutStorage extends BasicTestCase {

	private static final String TEST_CONTENT = "test-content";

	private static final String TEST_CHANGED_CONTENT = "test-changed-content";

	private static final String LAYOUT_FILENAME = "newLayout.layout.xml";

	/**
	 * Tests if the storage updates incremental on filesystem events.
	 */
	public void testIncrementalUpdates() throws IOException, InterruptedException, ConfigurationException {
		File newFile = null;

		try {
			assertMissingLayout(LAYOUT_FILENAME);

			Iterator<PathUpdate> updates = FileSystemCache.getCache().getUpdates();

			newFile = writeLayoutIntoFilesystem(LAYOUT_FILENAME, TEST_CONTENT);
			waitForUpdate(updates);

			assertHasLayout(LAYOUT_FILENAME, TEST_CONTENT);

			writeLayoutIntoFilesystem(LAYOUT_FILENAME, TEST_CHANGED_CONTENT);
			waitForUpdate(updates);

			assertHasLayout(LAYOUT_FILENAME, TEST_CHANGED_CONTENT);

			assertTrue(newFile.delete());
			waitForUpdate(updates);

			assertMissingLayout(LAYOUT_FILENAME);
		} finally {
			if (newFile != null && newFile.exists()) {
				newFile.delete();
			}
		}
	}

	private void waitForUpdate(Iterator<PathUpdate> updates) throws InterruptedException {
		Thread.sleep(10);

		int sleepCnt = 0;
		while (!updates.hasNext()) {
			Thread.sleep(10);
			sleepCnt++;
			if (sleepCnt > 100) {
				// No update found, will fail later on.
				return;
			}
		}
		updates.next();
	}

	private void assertHasLayout(String layoutKey, String content) throws InterruptedException, ConfigurationException {
		TLLayout layout = LayoutStorage.getInstance().getLayout(layoutKey);
		assertNotNull(layout);
		Config config = layout.get();
		assertInstanceof(config, SimpleComponent.Config.class);
		assertEquals(content, ((SimpleComponent.Config) config).getContent());
	}

	private void assertMissingLayout(String layoutKey) {
		assertNull(LayoutStorage.getInstance().getLayout(layoutKey));
	}

	private File writeLayoutIntoFilesystem(String layoutKey, String content) throws IOException, FileNotFoundException {
		File newFile = new File(FileManager.getInstance().getIDEFile(LayoutConstants.LAYOUT_BASE_RESOURCE), layoutKey);

		newFile.createNewFile();

		SimpleComponent.Config config = TypedConfiguration.newConfigItem(SimpleComponent.Config.class);
		config.setContent(content);

		writeConfigTo(newFile, config);

		return newFile;
	}

	private void writeConfigTo(File file, LayoutComponent.Config config) throws IOException, FileNotFoundException {
		try (FileOutputStream fout = new FileOutputStream(file)) {
			try (Writer out = new OutputStreamWriter(fout, StringServices.CHARSET_UTF_8)) {
				try (ConfigurationWriter confWriter = new ConfigurationWriter(out)) {
					confWriter.write(LayoutComponent.Config.COMPONENT, LayoutComponent.Config.class, config);
				}
			} catch (XMLStreamException exception) {
				throw new IOException(exception);
			}
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestLayoutStorage}.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestLayoutStorage.class);
		test = ServiceTestSetup.createSetup(TestLayoutStorage.class, LayoutStorage.Module.INSTANCE);
		return WebappFileManagerTestSetup.setup(KBSetup.getSingleKBTest(test));
	}

}
