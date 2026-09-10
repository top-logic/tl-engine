/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.wysiwyg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ForwardingReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.wysiwyg.I18NConstants;
import com.top_logic.layout.react.wysiwyg.ObjectLinkConfig;
import com.top_logic.layout.react.wysiwyg.ReactWysiwygControl;
import com.top_logic.util.Resources;

/**
 * Tests the editor's way of inserting a link to an application object: the button it offers, the
 * selection the object arrives from, and the insertion request the client answers.
 *
 * @see ReactWysiwygControl#insertAtCursor(String)
 */
public class TestInsertObjectLink extends TestCase {

	/** The command the client sends when the user asks for a link to an object. */
	private static final String CMD_INSERT_OBJECT_LINK = "insertObjectLink";

	/** State offering the button that sends {@link #CMD_INSERT_OBJECT_LINK}. */
	private static final String OBJECT_LINK = "objectLink";

	/** Field of {@link #OBJECT_LINK} holding the text of the button. */
	private static final String BUTTON_LABEL = "label";

	/** Field of {@link #OBJECT_LINK} holding the icon class of the button. */
	private static final String BUTTON_ICON = "icon";

	/** State asking the client to insert markup at the cursor. */
	private static final String INSERT = "insert";

	/** Field of {@link #INSERT} counting the insertions. */
	private static final String INSERT_SEQ = "seq";

	/** Field of {@link #INSERT} holding the markup to insert. */
	private static final String INSERT_HTML = "html";

	/** The command the client sends when the text of the editor changed. */
	private static final String CMD_VALUE_CHANGED = ReactFormFieldControl.CMD_VALUE_CHANGED;

	/** The {@link #CMD_VALUE_CHANGED} argument holding the text. */
	private static final String ARG_VALUE = "value";

	/** The view the object is picked in, as far as the tests get to opening it. */
	private static final String SELECTION_VIEW = "selection/pick-object.view.xml";

	/** Markup standing for a link, so that no test depends on how a link is written. */
	private static final String MARKUP = "<a href=\"?id=42\" class=\"tlObject\">Some object</a>";

	/**
	 * The place messages to the user are collected at, so that a test can tell a silent failure
	 * from a reported one.
	 */
	private static final class Messages implements ErrorSink {

		private final List<HTMLFragment> _errors = new ArrayList<>();

		@Override
		public void showError(HTMLFragment content) {
			_errors.add(content);
		}

		@Override
		public void showWarning(HTMLFragment content) {
			// Not part of what is tested here.
		}

		@Override
		public void showInfo(HTMLFragment content) {
			// Not part of what is tested here.
		}

		List<HTMLFragment> errors() {
			return _errors;
		}
	}

	private Messages _messages;

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_messages = new Messages();
		_context = new ForwardingReactContext(new DefaultReactContext("", "test", new SSEUpdateQueue())) {
			@Override
			public ErrorSink getErrorSink() {
				return _messages;
			}
		};
	}

	/** An editor told where to pick objects offers the button that leads there. */
	public void testTheButtonIsOfferedWhereObjectsCanBePicked() {
		Map<String, Object> button = objectLinkState(withSelection());

		assertNotNull("The editor offers no button.", button);
		assertEquals(Resources.getInstance().getString(I18NConstants.INSERT_OBJECT_LINK),
			button.get(BUTTON_LABEL));
		assertNotNull("The button has no icon.", button.get(BUTTON_ICON));
	}

	/** An editor that picks no objects offers no such button. */
	public void testWithoutASelectionTheButtonIsNotOffered() {
		ReactWysiwygControl editor = withoutSelection();

		assertNull(objectLinkState(editor));
		assertNull("There is nothing that could publish an object.", editor.getObjectLinkResult());
	}

	/** Asking an editor that picks no objects for a link says so instead of doing nothing. */
	public void testWithoutASelectionTheCommandIsReported() {
		ReactWysiwygControl editor = withoutSelection();

		editor.executeCommand(CMD_INSERT_OBJECT_LINK, Map.of());

		assertEquals(1, _messages.errors().size());
		assertNull(insertState(editor));
	}

	/** Asking for a link where no dialog can be displayed says so instead of doing nothing. */
	public void testWithoutADialogManagerTheCommandIsReported() {
		ReactWysiwygControl editor = withSelection();

		editor.executeCommand(CMD_INSERT_OBJECT_LINK, Map.of());

		assertEquals("A context displaying no dialogs leads the selection nowhere.",
			1, _messages.errors().size());
		assertNull(insertState(editor));
	}

	/** An insertion reaches the client as a request of its own. */
	public void testAnInsertionIsRequestedFromTheClient() {
		ReactWysiwygControl editor = withSelection();

		editor.insertAtCursor(MARKUP);

		Map<String, Object> insert = insertState(editor);
		assertNotNull("Nothing was asked of the client.", insert);
		assertEquals(MARKUP, insert.get(INSERT_HTML));
		assertEquals(Integer.valueOf(1), insert.get(INSERT_SEQ));
	}

	/** Inserting the same markup twice is two requests, not one. */
	public void testTheSameMarkupCanBeInsertedAgain() {
		ReactWysiwygControl editor = withSelection();

		editor.insertAtCursor(MARKUP);
		editor.insertAtCursor(MARKUP);

		assertEquals(Integer.valueOf(2), insertState(editor).get(INSERT_SEQ));
	}

	/** A carried-out insertion is taken back, so that a client mounting anew does not repeat it. */
	public void testACarriedOutInsertionIsTakenBack() {
		ReactWysiwygControl editor = withSelection();
		editor.insertAtCursor(MARKUP);

		editor.executeCommand(CMD_VALUE_CHANGED, Map.of(ARG_VALUE, "<p>" + MARKUP + "</p>"));

		assertNull("The insertion stands although the client carried it out.", insertState(editor));
	}

	/** Ordinary typing changes nothing about a pending insertion. */
	public void testTextArrivingBeforeAnInsertionLeavesNothingBehind() {
		ReactWysiwygControl editor = withSelection();

		editor.executeCommand(CMD_VALUE_CHANGED, Map.of(ARG_VALUE, "<p>typed</p>"));

		assertNull(insertState(editor));
	}

	/** Nothing is inserted for a selection that published no object. */
	public void testAnEmptySelectionInsertsNothing() {
		ReactWysiwygControl editor = withSelection();

		editor.getObjectLinkResult().set(null);

		assertNull(insertState(editor));
	}

	/** Nothing is inserted for something that is no object of the application. */
	public void testSomethingThatIsNoObjectInsertsNothing() {
		ReactWysiwygControl editor = withSelection();

		editor.getObjectLinkResult().set("not an object");

		assertNull("A link can lead to an object only.", insertState(editor));
	}

	/** A selection publishes on a channel named "result" unless it says otherwise. */
	public void testTheSelectionPublishesOnAResultChannel() {
		ObjectLinkConfig selection = TypedConfiguration.newConfigItem(ObjectLinkConfig.class);

		assertEquals(ObjectLinkConfig.RESULT_CHANNEL_DEFAULT, selection.getResultChannel());
	}

	/** An editor picking objects in {@link #SELECTION_VIEW}. */
	private ReactWysiwygControl withSelection() {
		ObjectLinkConfig selection = TypedConfiguration.newConfigItem(ObjectLinkConfig.class);
		selection.setDialogView(SELECTION_VIEW);
		return editor(selection);
	}

	/** An editor that formats text and picks no objects. */
	private ReactWysiwygControl withoutSelection() {
		return editor(null);
	}

	private ReactWysiwygControl editor(ObjectLinkConfig selection) {
		return new ReactWysiwygControl(_context, new AbstractFieldModel(null), selection);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> objectLinkState(ReactWysiwygControl editor) {
		return (Map<String, Object>) editor.scriptingScalarState().get(OBJECT_LINK);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> insertState(ReactWysiwygControl editor) {
		return (Map<String, Object>) editor.scriptingScalarState().get(INSERT);
	}

	/**
	 * Test suite requiring the session resources a message to the user is composed from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestInsertObjectLink.class, ThreadContextManager.Module.INSTANCE));
	}

}
