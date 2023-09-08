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

			// Give the file system a chance to record the creation.
			Thread.sleep(10);

			assertHasTemplate(TEMPLATE_FILENAME);

			file.delete();

			// Give the file system a chance to record the deletion.
			Thread.sleep(10);

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
		assertNull(_templateService.getComponentDefinition(templateName));
	}

	private void assertHasTemplate(String templateName) throws InterruptedException {
		assertNotNull(_templateService.getComponentDefinition(templateName));
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
