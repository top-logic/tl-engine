/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.ViewOverlayMerger;

/**
 * Tests that {@link ViewOverlayMerger} writes the merged view of a module and its overlay, and
 * reports an overlay that cannot be merged as an error naming the view and the cause.
 */
public class TestViewOverlayMerger extends TestCase {

	/** Path of the view the base module and the overlay module both provide. */
	private static final String VIEW = ViewLoader.VIEW_BASE_PATH + "app.view.xml";

	/** Id of the element the overlays position their element against. */
	private static final String ANCHOR = "system";

	/** Id of an element the base view does not have. */
	private static final String MISSING_ANCHOR = "system-separator";

	/** The base view: a stack with the element {@link #ANCHOR}. */
	private static final String BASE = """
			<view>
				<stack id="items">
					<text id="application"/>
					<text id="system"/>
				</stack>
			</view>
			""";

	private Path _baseModule;

	private Path _overlayModule;

	private File _target;

	private FileManager _oldFileManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_baseModule = Files.createTempDirectory("base-module");
		_overlayModule = Files.createTempDirectory("overlay-module");
		_target = Files.createTempDirectory("target-webapp").toFile();
		writeView(_baseModule, BASE);

		// The overlay module depends on the base module and takes precedence.
		_oldFileManager = FileManager.setInstance(MultiFileManager.createMultiFileManager(_overlayModule, _baseModule));
	}

	@Override
	protected void tearDown() throws Exception {
		FileManager.setInstance(_oldFileManager);
		FileUtilities.deleteR(_baseModule.toFile());
		FileUtilities.deleteR(_overlayModule.toFile());
		FileUtilities.deleteR(_target);
		super.tearDown();
	}

	/** An overlay that positions its element against an element of the base is merged and written. */
	public void testMergedViewIsWritten() throws IOException {
		writeView(_overlayModule, overlay(ANCHOR));

		BufferingProtocol log = new BufferingProtocol();
		ViewOverlayMerger.mergeOverlaidViews(log, _target);

		assertFalse(log.getError(), log.hasErrors());
		String merged = Files.readString(targetView(), StandardCharsets.UTF_8);
		assertTrue(merged, merged.contains("id=\"added\""));
		assertTrue(merged, merged.contains("id=\"" + ANCHOR + "\""));
	}

	/**
	 * An overlay that positions its element against an element the base does not have is reported
	 * as an error naming the view and the missing element, and aborts on the error check.
	 */
	public void testMissingReferenceIsReported() throws IOException {
		writeView(_overlayModule, overlay(MISSING_ANCHOR));

		BufferingProtocol log = new BufferingProtocol();
		ViewOverlayMerger.mergeOverlaidViews(log, _target);

		assertTrue("A dangling reference must be reported as an error.", log.hasErrors());
		String error = log.getError();
		assertTrue(error, error.contains(VIEW));
		assertTrue(error, error.contains("'" + MISSING_ANCHOR + "'"));
		assertFalse("A view that cannot be merged is not written.", targetView().toFile().exists());

		try {
			log.checkErrors();
			fail("The error check must abort after a view could not be merged.");
		} catch (AbortExecutionException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(VIEW));
		}
	}

	private Path targetView() {
		return _target.toPath().resolve(VIEW.substring(1));
	}

	private static String overlay(String anchor) {
		return """
				<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<stack id="items">
						<text id="added" config:position="before" config:reference="%s"/>
					</stack>
				</view>
				""".formatted(anchor);
	}

	private static void writeView(Path module, String content) throws IOException {
		Path file = module.resolve(VIEW.substring(1));
		Files.createDirectories(file.getParent());
		Files.writeString(file, content, StandardCharsets.UTF_8);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view,
	 * and the {@link ResourcesModule}, which writes the labels of the view.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestViewOverlayMerger.class,
			TypeIndex.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
