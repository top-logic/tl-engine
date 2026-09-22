/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

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
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormParticipant;
import com.top_logic.model.TransientObject;
import com.top_logic.model.listen.ModelScope;

/**
 * Leaving a sidebar item asks about the input left unsaved in the tabs it displays.
 *
 * <p>
 * A form sits in a tab, and the tab sits in the item the sidebar displays. Leaving either of them
 * takes the user away from what was typed, so both the tab switch and the item switch are refused
 * while the form holds it - which is what the browser back button and the address bar reach the
 * sidebar as, too.
 * </p>
 */
public class TestNestedDirtyScopes extends TestCase {

	/** The view displaying a sidebar whose first item holds a tab bar with a form. */
	private static final String VIEW = "nested-dirty-scopes.view.xml";

	/** The channel delivering the object the form displays. */
	private static final String EDITED = "edited";

	/** The item that is displayed and whose tab holds the form. */
	private static final String FIRST_ITEM = "first";

	/** The item the user switches to. */
	private static final String SECOND_ITEM = "second";

	/** The tab holding the form. */
	private static final String FORM_TAB = "alpha";

	/** The tab the user switches to. */
	private static final String OTHER_TAB = "beta";

	private File _webapp;

	private FileManager _fileManagerBefore;

	private SSEUpdateQueue _sseQueue;

	private ViewContext _viewContext;

	private ReactControl _root;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_webapp = Files.createTempDirectory("tl-nested-dirty-scopes").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		copyFixture(VIEW, new File(views, VIEW));
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));

		_sseQueue = new SSEUpdateQueue();
		ReactContext reactContext = new PageContext(_sseQueue);
		ViewElement view = ViewLoader.getOrLoadView(ViewLoader.fullPath(VIEW));
		_viewContext = new DefaultViewContext(reactContext, ViewLoader.fullPath(VIEW));
		_root = (ReactControl) view.createControl(_viewContext);

		// The form displays an object, without which it holds nothing that could be left unsaved.
		_viewContext.resolveChannel(new ChannelRef(EDITED)).set(new MockTLObject());

		// The displayed item and its displayed tab build their content when the page is rendered.
		_root.attach();
		_root.write(new TagWriter());
	}

	@Override
	protected void tearDown() throws Exception {
		_sseQueue.shutdown();
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);

		super.tearDown();
	}

	/**
	 * Tests that the tab holding the form is not left while the form holds unsaved input.
	 */
	public void testTheTabIsNotLeft() {
		FormControl form = editedForm();

		try {
			tabBar().revealChild(OTHER_TAB);
			fail("A tab holding unsaved input must not be left silently.");
		} catch (ChannelVetoException ex) {
			assertEquals("The form holding the input is what refuses the switch.",
				List.of(form), ex.getDirtyHandlers());
		}
		assertEquals("The refused switch leaves the tab displayed.", FORM_TAB, tabBar().getActiveTabId());
	}

	/**
	 * Tests that the item displaying that tab is not left either: what sits in the tab sits in the
	 * item, and the item switch disposes it just as the tab switch does.
	 */
	public void testTheItemDisplayingTheTabIsNotLeft() {
		FormControl form = editedForm();

		try {
			sidebar().revealChild(SECOND_ITEM);
			fail("An item whose tab holds unsaved input must not be left silently.");
		} catch (ChannelVetoException ex) {
			assertEquals("The form of the displayed tab is what refuses the switch.",
				List.of(form), ex.getDirtyHandlers());
		}
		assertEquals("The refused switch leaves the item displayed.",
			FIRST_ITEM, sidebar().activeRouteSegment().path());
	}

	/**
	 * Tests that both switches go through once the input is discarded, which is what the user
	 * answering the question does.
	 */
	public void testBothSwitchOnceTheInputIsDiscarded() {
		FormControl form = editedForm();

		form.executeDiscard();

		tabBar().revealChild(OTHER_TAB);
		assertEquals("Nothing is left unsaved, so the tab switches.", OTHER_TAB, tabBar().getActiveTabId());

		sidebar().revealChild(SECOND_ITEM);
		assertEquals("Nothing is left unsaved, so the item switches.",
			SECOND_ITEM, sidebar().activeRouteSegment().path());
	}

	/**
	 * Tests that a tab holding nothing unsaved is left without a question, and so is the item
	 * displaying it.
	 */
	public void testACleanTabIsLeft() {
		tabBar().revealChild(OTHER_TAB);
		assertEquals(OTHER_TAB, tabBar().getActiveTabId());

		sidebar().revealChild(SECOND_ITEM);
		assertEquals(SECOND_ITEM, sidebar().activeRouteSegment().path());
	}

	/**
	 * The form of the displayed tab, holding input the user typed and has not saved.
	 */
	private FormControl editedForm() {
		FormControl form = find(_root, FormControl.class);
		assertNotNull("The displayed tab builds the form.", form);

		form.enterEditMode();
		form.registerParticipant(new UnsavedInput());
		form.updateDirtyState();

		assertTrue("The form holds unsaved input.", form.isDirty());
		return form;
	}

	private ReactSidebarControl sidebar() {
		return find(_root, ReactSidebarControl.class);
	}

	private ReactTabBarControl tabBar() {
		return find(_root, ReactTabBarControl.class);
	}

	/**
	 * The first control of the given kind in the displayed tree, or {@code null} if the tree holds
	 * none.
	 */
	private static <T> T find(ReactControl control, Class<T> kind) {
		if (kind.isInstance(control)) {
			return kind.cast(control);
		}
		for (ReactControl child : control.displayedChildren()) {
			T found = find(child, kind);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private static void copyFixture(String name, File target) throws IOException {
		try (InputStream in = TestNestedDirtyScopes.class.getResourceAsStream(name)) {
			assertNotNull("Missing fixture: " + name, in);
			Files.copy(in, Path.of(target.toURI()), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * The context of the displayed page.
	 *
	 * <p>
	 * The object the form displays is transient and observed by nobody, so the page reports no
	 * {@link ModelScope} - which spares the test the knowledge base one is built from.
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
	 * Input the user typed into the form and has not saved.
	 */
	private static class UnsavedInput implements FormParticipant {

		@Override
		public boolean validate() {
			return true;
		}

		@Override
		public void persist(Transaction tx) {
			// Nothing to persist.
		}

		@Override
		public void cancel() {
			// Nothing to cancel.
		}

		@Override
		public void revealAll() {
			// No hidden errors.
		}

		@Override
		public boolean isDirty() {
			return true;
		}
	}

	/**
	 * The object the form displays; no attribute of it is read by these tests.
	 */
	private static class MockTLObject extends TransientObject {
		// Nothing but the identity of the object is used.
	}

	/**
	 * Test suite requiring the {@link TypeIndex} and the session resources the labels are resolved
	 * with, plus the {@link SchedulerService} the event stream's heartbeat is scheduled on.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestNestedDirtyScopes.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE));
	}

}
