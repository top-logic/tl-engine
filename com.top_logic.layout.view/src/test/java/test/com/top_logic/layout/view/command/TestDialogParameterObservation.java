/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.view.model.DeletableItem;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.DialogResultHandler;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelBindingConfig;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests that the parameters of a dialog drop an object deleted while the dialog is displayed, and
 * that the channels the dialog binds from its opener are left to the opener.
 */
public class TestDialogParameterObservation extends TestCase {

	/** The dialog view: a panel, reading none of the channels. */
	private static final String VIEW = "dialog-parameters.view.xml";

	/** The name of the parameter channel of the dialog. */
	private static final String PARAMETER = "item";

	/** The name of the opener's channel bound into the dialog. */
	private static final String BOUND = "selection";

	private File _webapp;

	private FileManager _fileManagerBefore;

	private SSEUpdateQueue _sseQueue;

	private Dialogs _dialogs;

	private ViewContext _page;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_webapp = Files.createTempDirectory("tl-dialog-parameters").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		copyFixture(VIEW, new File(views, VIEW));
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));

		_sseQueue = new SSEUpdateQueue();
		_dialogs = new Dialogs();
		_sseQueue.setDialogManager(_dialogs);
		_page = new DefaultViewContext(new PageContext(_sseQueue));
	}

	@Override
	protected void tearDown() throws Exception {
		_sseQueue.shutdown();
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);

		super.tearDown();
	}

	/**
	 * Tests that a parameter of the dialog drops its object when it is deleted while the dialog is
	 * displayed, and when it was deleted while the dialog was not displayed.
	 */
	public void testParameterDropsDeletedObject() {
		DeletableItem item = new DeletableItem("item");
		OpenDialogAction.openDialog(_page, ViewLoader.fullPath(VIEW), false, Map.of(PARAMETER, item), List.of());
		ReactControl dialog = _dialogs.opened();
		ViewChannel parameter = channel(dialog, PARAMETER);
		assertSame(item, parameter.get());

		item.markDeleted();
		assertSame("Nothing is observed before the dialog is displayed.", item, parameter.get());

		dialog.attach();
		assertNull("Displaying the dialog drops the object deleted before.", parameter.get());

		dialog.detach();
		DeletableItem other = new DeletableItem("other");
		parameter.set(other);
		other.markDeleted();
		assertSame("Nothing is observed while the dialog is not displayed.", other, parameter.get());

		dialog.attach();
		assertNull("Displaying the dialog again drops the object deleted meanwhile.", parameter.get());
	}

	/**
	 * Tests that a channel the dialog binds from its opener is not observed by the dialog.
	 */
	public void testBoundChannelLeftToOpener() {
		DeletableItem item = new DeletableItem("item");
		DefaultViewChannel selection = new DefaultViewChannel(BOUND);
		selection.set(item);
		_page.registerChannel(BOUND, selection);

		OpenDialogAction.openDialog(_page, ViewLoader.fullPath(VIEW), false, Map.of(), List.of(binding(BOUND)));
		ReactControl dialog = _dialogs.opened();
		assertSame(selection, channel(dialog, BOUND));

		item.markDeleted();
		dialog.attach();
		assertSame("The opener's channel follows the display of the opener.", item, selection.get());
	}

	private static ViewChannel channel(ReactControl dialog, String name) {
		return ((ViewContext) dialog.getReactContext()).resolveChannel(new ChannelRef(name));
	}

	private static ChannelBindingConfig binding(String name) {
		ChannelBindingConfig binding = TypedConfiguration.newConfigItem(ChannelBindingConfig.class);
		binding.update(binding.descriptor().getProperty(ChannelBindingConfig.CHANNEL), name);
		binding.update(binding.descriptor().getProperty(ChannelBindingConfig.TO), new ChannelRef(name));
		return binding;
	}

	private static void copyFixture(String name, File target) throws IOException {
		try (InputStream in = TestDialogParameterObservation.class.getResourceAsStream(name)) {
			assertNotNull("Missing fixture: " + name, in);
			Files.copy(in, Path.of(target.toURI()), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Stands in for the dialogs of a window, holding the one that was opened.
	 */
	private static class Dialogs implements DialogManager {

		private ReactControl _open;

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
				DialogResultHandler<Void> handler) {
			assertNull("One dialog at a time.", _open);
			_open = child;
			return null;
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			_open = null;
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			throw new UnsupportedOperationException("Nothing is stacked on the dialog of these tests.");
		}

		/** The content of the dialog the user is shown. */
		ReactControl opened() {
			assertNotNull("A dialog is open.", _open);
			return _open;
		}
	}

	/**
	 * The context of the displayed page.
	 *
	 * <p>
	 * The test objects are deleted by marking them, not by a commit, so the page reports no
	 * {@link ModelScope}: a channel validates its value when it is attached, which is what these
	 * tests observe.
	 * </p>
	 */
	private static final class PageContext extends DefaultReactContext {

		PageContext(SSEUpdateQueue sseQueue) {
			super("", "test", sseQueue, new ReactWindowRegistry("test"));
		}

		@Override
		public ModelScope getModelScope() {
			return null;
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} and the session resources the labels are resolved
	 * with, the {@link ThemeFactory} the panel takes its icons from, plus the
	 * {@link SchedulerService} the event stream's heartbeat is scheduled on.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestDialogParameterObservation.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE, SchedulerService.Module.INSTANCE,
				CommandCliqueService.Module.INSTANCE));
	}
}
