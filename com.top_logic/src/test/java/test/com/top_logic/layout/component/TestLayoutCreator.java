/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.io.FileSystemCache;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.component.LayoutCreator;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.layout.processor.CompileTimeApplication;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * Tests that {@link LayoutCreator} writes the expanded layouts of an application, and reports a
 * layout that cannot be created as an error naming the layout and the cause.
 */
public class TestLayoutCreator extends TestCase {

	/** Directory of the test layouts relative to the layout directory. */
	private static final String DIR = "test/layoutCreator/";

	/** A layout without overlays, copied unchanged. */
	private static final String PLAIN = DIR + "plain.layout.xml";

	/** A layout with an argument overlay that adds a reference to {@link #PLAIN}. */
	private static final String VALID = DIR + "valid.layout.xml";

	/** A layout calling a template that does not exist. */
	private static final String MISSING_TEMPLATE = DIR + "missingTemplate.layout.xml";

	/** Name of the template {@link #MISSING_TEMPLATE} calls. */
	private static final String NO_SUCH_TEMPLATE = DIR + "noSuch.template.xml";

	/** A layout whose overlay is not well-formed XML. */
	private static final String MALFORMED_OVERLAY = DIR + "malformedOverlay.layout.xml";

	private static final String TABBAR_CALL = """
			<?xml version="1.0" encoding="utf-8" ?>
			<config:template-call
				xmlns:config="http://www.top-logic.com/ns/config/6.0"
				template="%s"
			>
				<arguments/>
			</config:template-call>
			""";

	private static final String TABBAR_TEMPLATE = "com.top_logic/tabbar.template.xml";

	private static final String REFERENCE_OVERLAY = """
			<?xml version="1.0" encoding="utf-8" ?>
			<arguments>
				<components>
					<layout-reference resource="%s"/>
				</components>
			</arguments>
			""".formatted(PLAIN);

	private static final String MALFORMED = """
			<?xml version="1.0" encoding="utf-8" ?>
			<arguments>
				<components>
			""";

	private File _target;

	private Map<String, List<BinaryData>> _overlays;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_target = Files.createTempDirectory("target-webapp").toFile();
		_overlays = new LinkedHashMap<>();

		addLayout(PLAIN, TABBAR_CALL.formatted(TABBAR_TEMPLATE), null);
		addLayout(VALID, TABBAR_CALL.formatted(TABBAR_TEMPLATE), REFERENCE_OVERLAY);
	}

	@Override
	protected void tearDown() throws Exception {
		FileUtilities.deleteR(moduleRoot().resolve(ModuleLayoutConstants.LAYOUT_PATH + "/" + DIR).toFile());
		refreshLayouts();
		FileUtilities.deleteR(_target);
		super.tearDown();
	}

	/** The layouts of an application without defects are written without errors. */
	public void testValidLayoutsAreWritten() throws IOException {
		BufferingProtocol log = createLayouts();

		assertFalse(log.getError(), log.hasErrors());
		assertValidLayoutsWritten();
	}

	/**
	 * Each layout that cannot be created is reported naming the layout and the cause, is not
	 * written, and aborts on the error check, while the valid layouts are written anyway.
	 */
	public void testDefectiveLayoutsAreReported() throws IOException {
		addLayout(MISSING_TEMPLATE, TABBAR_CALL.formatted(NO_SUCH_TEMPLATE), REFERENCE_OVERLAY);
		addLayout(MALFORMED_OVERLAY, TABBAR_CALL.formatted(TABBAR_TEMPLATE), MALFORMED);

		BufferingProtocol log = createLayouts();

		assertTrue("Defective layouts must be reported as errors.", log.hasErrors());
		String error = log.getError();
		assertTrue(error, error.contains("'" + MISSING_TEMPLATE + "'"));
		assertTrue(error, error.contains(NO_SUCH_TEMPLATE));
		assertTrue(error, error.contains("'" + MALFORMED_OVERLAY + "'"));
		assertFalse(error, error.contains("'" + VALID + "'"));
		assertFalse("A layout that cannot be created is not written.", target(MISSING_TEMPLATE).exists());
		assertFalse("A layout that cannot be created is not written.", target(MALFORMED_OVERLAY).exists());
		assertValidLayoutsWritten();

		try {
			log.checkErrors();
			fail("The error check must abort after a layout could not be created.");
		} catch (AbortExecutionException ex) {
			String message = ex.getMessage();
			assertTrue(message, message.contains(MISSING_TEMPLATE));
			assertTrue(message, message.contains(NO_SUCH_TEMPLATE));
			assertTrue(message, message.contains(MALFORMED_OVERLAY));
		}
	}

	private void assertValidLayoutsWritten() throws IOException {
		assertTrue("A layout without overlays is copied.", target(PLAIN).exists());
		String valid = Files.readString(target(VALID).toPath(), StandardCharsets.UTF_8);
		assertTrue(valid, valid.contains(PLAIN));
	}

	private BufferingProtocol createLayouts() throws IOException {
		CompileTimeApplication application =
			CompileTimeApplication.createCompileTimeApplication(new File(ModuleLayoutConstants.PATH_TO_MODULE_ROOT),
				new File(ModuleLayoutConstants.WEBAPP_DIR));
		refreshLayouts();
		BufferingProtocol log = new BufferingProtocol();
		ThreadContext.inSystemContext(TestLayoutCreator.class,
			() -> LayoutCreator.createLayoutsForTheme(log, ThemeFactory.getTheme(), _target, application, _overlays));
		return log;
	}

	private File target(String layoutKey) {
		return new File(_target, ModuleLayoutConstants.LAYOUT_PATH + "/" + layoutKey);
	}

	private void addLayout(String layoutKey, String layout, String overlay) throws IOException {
		write(layoutKey, layout);
		if (overlay == null) {
			_overlays.put(layoutKey, Collections.emptyList());
		} else {
			String overlayKey = layoutKey.substring(0,
				layoutKey.length() - LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX.length())
				+ LayoutModelConstants.LAYOUT_XML_OVERLAY_FILE_SUFFIX;
			File overlayFile = write(overlayKey, overlay);
			_overlays.put(layoutKey, Collections.singletonList(BinaryDataFactory.createBinaryData(overlayFile)));
		}
	}

	/**
	 * Makes the layouts written to the file system visible through the {@link FileManager}.
	 */
	private static void refreshLayouts() {
		FileManager.getInstance().refresh(ModuleLayoutConstants.LAYOUT_RESOURCE_PREFIX + DIR);
	}

	/**
	 * The root directory of the test module, which takes precedence over the modules of the test
	 * environment, see {@link ModuleRootSetup}.
	 */
	private static Path moduleRoot() {
		return FileManager.getInstance().getPaths().get(0);
	}

	private File write(String layoutKey, String content) throws IOException {
		Path file = moduleRoot().resolve(ModuleLayoutConstants.LAYOUT_PATH + "/" + layoutKey);
		Files.createDirectories(file.getParent());
		Files.writeString(file, content, StandardCharsets.UTF_8);
		return file.toFile();
	}

	/**
	 * Test suite requiring the services the layouts of an application are created with.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(TestLayoutCreator.class,
			ThemeFactory.Module.INSTANCE,
			DynamicComponentService.Module.INSTANCE,
			SafeHTML.Module.INSTANCE,
			CommandGroupRegistry.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(new ModuleRootSetup(test));
	}

	/**
	 * {@link TestSetup} installing a {@link MultiFileManager} with a fresh module root directory in
	 * front of the modules of the test environment, which provide the templates.
	 * 
	 * <p>
	 * The {@link FileManager} is installed before the services start, so that the
	 * {@link FileSystemCache} covers the module root.
	 * </p>
	 */
	private static class ModuleRootSetup extends TestSetup {

		private FileManager _before;

		private Path _root;

		ModuleRootSetup(Test test) {
			super(test);
		}

		@Override
		protected void setUp() throws Exception {
			super.setUp();
			_root = Files.createTempDirectory(TestLayoutCreator.class.getSimpleName()).toRealPath();
			List<Path> paths = new ArrayList<>();
			paths.add(_root);
			paths.addAll(FileManager.getInstance().getPaths());
			_before = FileManager.setInstance(MultiFileManager.createMultiFileManager(paths));
		}

		@Override
		protected void tearDown() throws Exception {
			FileManager.setInstance(_before);
			FileUtilities.deleteR(_root.toFile());
			super.tearDown();
		}
	}

}
