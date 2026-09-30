/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.core.workspace.Workspace;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLCompare;
import com.top_logic.layout.component.LayoutModelProcessor;

/**
 * Test cases for template layout parsing and expanding.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestLayoutModelProcessor extends TestCase {

	private static final String PATH_TO_ACTUAL_MASTERFRAME =
		"/" + ModuleLayoutConstants.WEBAPP_DIR + "/WEB-INF/layouts/themes/core/masterFrame.xml";

	private static final String PATH_TO_EXPECTED_MASTERFRAME =
		"/" + ModuleLayoutConstants.WEBAPP_DIR + "/WEB-INF/layouts/expectedMasterFrame.xml";

	private static final String TEMPLATE_PATH =
		ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/layout/processor/app";
	private File testDirectory;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		generateFileFixture();

	}

	private void generateFileFixture() {
		createTestDirectory();
		copyContentFromTemplateToTestDirectory();
	}

	private void createTestDirectory() {
		testDirectory = BasicTestCase.createdCleanTestDir("app");
	}

	private void copyContentFromTemplateToTestDirectory() {
		File testDirectoryTemplate = new File(TEMPLATE_PATH);
		boolean successful = FileUtilities.copyR(testDirectoryTemplate, testDirectory);
		if (!successful) {
			throw new IllegalStateException("Could not create test directory structure!");
		}
	}

	public void testAliasResolving() throws IOException {
		String before =
			System.setProperty(Workspace.WORKSPACE_PROPERTY, testDirectory.getParentFile().getCanonicalPath());
		try {
			generateActualMasterFrame();
			Document actualMasterFrame = getActualMasterFrame();
			Document expectedMasterFrame = getExpectedMasterFrame();
			assertEquals(actualMasterFrame, expectedMasterFrame);
		} finally {
			if (before == null) {
				System.clearProperty(Workspace.WORKSPACE_PROPERTY);
			} else {
				System.setProperty(Workspace.WORKSPACE_PROPERTY, before);
			}
		}
	}

	private void assertEquals(Document actualMasterFrame, Document expectedMasterFrame) {
		XMLCompare compare = new XMLCompare(new AssertProtocol(), false, FilterFactory.trueFilter());
		compare.assertEqualsNode(expectedMasterFrame, actualMasterFrame);
	}

	private Document getExpectedMasterFrame() {
		String pathExpectedMasterFrame =
			testDirectory.getAbsolutePath() + PATH_TO_EXPECTED_MASTERFRAME;
		return getMasterFrame(pathExpectedMasterFrame);
	}

	private Document getMasterFrame(String pathMasterFrame) {
		try {
			return DOMUtil.parseStripped(new FileInputStream(new File(pathMasterFrame)));
		} catch (FileNotFoundException ex) {
			BasicTestCase.fail("Could not parse masterframe: " + pathMasterFrame, ex);
		} catch (XMLStreamException ex) {
			BasicTestCase.fail("Could not parse masterframe: " + pathMasterFrame, ex);
		}

		throw new IllegalStateException();
	}

	private Document getActualMasterFrame() {
		String pathActualMasterFramePath =
			testDirectory.getAbsolutePath() + PATH_TO_ACTUAL_MASTERFRAME;
		return getMasterFrame(pathActualMasterFramePath);
	}

	private void generateActualMasterFrame() {
		try {
			File testWebapp = new File(testDirectory, ModuleLayoutConstants.WEBAPP_DIR).getCanonicalFile();
			String[] arguments = { LayoutModelProcessor.MODULE_ARGUMENT, testDirectory.getAbsolutePath(), "inline" };
			new LayoutModelProcessor() {
				@Override
				protected void installFileManager() {
					FileManager.setInstance(new DefaultFileManager(testWebapp));
				}
			}.run(arguments);
		} catch (XPathExpressionException ex) {
			fail("Could not create masterframe!");
		} catch (IOException ex) {
			fail("Could not create masterframe!");
		}
	}
}
