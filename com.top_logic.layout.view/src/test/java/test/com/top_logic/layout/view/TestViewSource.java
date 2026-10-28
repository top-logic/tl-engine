/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests that the control of a view names the file of the view as its
 * {@link ReactControl#getViewSource() view source}, wherever the view is displayed.
 */
public class TestViewSource extends TestCase {

	private static final String PLAIN_VIEW = "plain.view.xml";

	private static final String REFERRING_VIEW = "referring.view.xml";

	private File _webapp;

	private FileManager _fileManagerBefore;

	private SSEUpdateQueue _sseQueue;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_webapp = Files.createTempDirectory("tl-view-source").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		write(new File(views, PLAIN_VIEW), "<view><stack/></view>");
		write(new File(views, REFERRING_VIEW), "<view><view-ref view=\"" + PLAIN_VIEW + "\"/></view>");
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));
		_sseQueue = new SSEUpdateQueue();
	}

	@Override
	protected void tearDown() throws Exception {
		_sseQueue.shutdown();
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);
		super.tearDown();
	}

	/**
	 * A view displayed by whatever opens it - a tile stack, a dialog, a window - names its file.
	 */
	public void testViewNamesItsFile() throws Exception {
		assertEquals(ViewLoader.fullPath(PLAIN_VIEW), createControl(PLAIN_VIEW).getViewSource());
	}

	/**
	 * A view whose content is a referenced view leaves the content to the referenced view, which is
	 * what is displayed there.
	 */
	public void testReferencedViewKeepsItsFile() throws Exception {
		assertEquals(ViewLoader.fullPath(PLAIN_VIEW), createControl(REFERRING_VIEW).getViewSource());
	}

	private ReactControl createControl(String viewRef) throws Exception {
		DefaultReactContext reactContext =
			new DefaultReactContext("", "test", _sseQueue, new ReactWindowRegistry("test")) {
				@Override
				public ModelScope getModelScope() {
					return null;
				}
			};
		String path = ViewLoader.fullPath(viewRef);
		ViewElement view = ViewLoader.getOrLoadView(path);
		return (ReactControl) view.createControl(new DefaultViewContext(reactContext, path));
	}

	private static void write(File file, String content) throws Exception {
		Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
	}

	/**
	 * Test suite requiring the {@link TypeIndex}, plus the {@link SchedulerService} the event
	 * stream's heartbeat is scheduled on.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestViewSource.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE, CommandCliqueService.Module.INSTANCE));
	}

}
