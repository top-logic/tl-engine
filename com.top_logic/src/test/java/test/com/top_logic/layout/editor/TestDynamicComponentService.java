/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.editor;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.WebappFileManagerTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.mig.html.layout.LayoutConstants;

/**
 * Tests the {@link DynamicComponentService}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TestDynamicComponentService extends BasicTestCase {

	private static final String TEMPLATE_FILENAME = "simpleComponent.template.xml";

	/**
	 * Maximum time to wait for an asynchronous {@link java.nio.file.WatchService} event to be
	 * delivered and processed before failing.
	 */
	private static final long UPDATE_TIMEOUT = 10_000;

	private DynamicComponentService _templateService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_templateService = DynamicComponentService.getInstance();
	}

	/**
	 * Tests if the template cache updates incremental on filesystem events.
	 */
	public void testIncrementalUpdates() throws IOException, InterruptedException {
		File file = null;

		try {
			assertNull(_templateService.getComponentDefinition(TEMPLATE_FILENAME));

			file = createTemplateFile();

			assertHasTemplate(TEMPLATE_FILENAME);

			file.delete();

			assertMissingTemplate(TEMPLATE_FILENAME);
		} finally {
			if (file != null && file.exists()) {
				file.delete();
			}
		}
	}

	private File createTemplateFile() throws IOException {
		FileManager fileManager = FileManager.getInstance();
		BinaryData data = fileManager.getData(TestDynamicComponentDefinition.qualifyLayout("test1.xml"));
		File newTemplate = new File(fileManager.getIDEFile(LayoutConstants.LAYOUT_BASE_RESOURCE), TEMPLATE_FILENAME);
		FileUtilities.copyToFile(data, newTemplate);
		return newTemplate;
	}

	private void assertMissingTemplate(String templateName) throws InterruptedException {
		// The file-system watch event is delivered asynchronously with unbounded latency; poll
		// until the deletion has been processed instead of relying on a fixed sleep.
		assertTrue("Template '" + templateName + "' still registered after deletion.",
			awaitUntil(UPDATE_TIMEOUT, () -> _templateService.getComponentDefinition(templateName) == null));
	}

	private void assertHasTemplate(String templateName) throws InterruptedException {
		// The file-system watch event is delivered asynchronously with unbounded latency; poll
		// until the creation has been processed instead of relying on a fixed sleep.
		assertTrue("Template '" + templateName + "' not registered after creation.",
			awaitUntil(UPDATE_TIMEOUT, () -> _templateService.getComponentDefinition(templateName) != null));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestDynamicComponentService}.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestDynamicComponentService.class);
		test = ServiceTestSetup.createSetup(TestDynamicComponentService.class, DynamicComponentService.Module.INSTANCE);
		return WebappFileManagerTestSetup.setup(KBSetup.getSingleKBTest(test));
	}

}
