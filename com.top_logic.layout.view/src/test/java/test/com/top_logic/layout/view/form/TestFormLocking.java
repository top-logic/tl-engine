/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.base.locking.handler.DefaultLockHandler;
import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.base.locking.token.Token;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.FormElement;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests that a {@link FormElement} locks the persistent object its form edits, with a lock handler
 * of its own for every displayed form, and how a form deals with an object someone else has
 * locked.
 */
@SuppressWarnings("javadoc")
public class TestFormLocking extends AbstractModelAccessTest {

	private static final String FORM = "form";

	private static final String INPUT = "item";

	private static final String EDIT_MODE = "editMode";

	private static final String EDIT_MODE_FORM = "<form input='" + INPUT + "' editMode='" + EDIT_MODE + "'/>";

	/** The forms created by the test, disposed after it. */
	private List<FormControl> _forms;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_forms = new ArrayList<>();
	}

	@Override
	protected Person initialUser() {
		// Bypasses the model access rights: the tests are about locking, not about the edit right.
		return _root;
	}

	@Override
	protected void tearDown() throws Exception {
		for (FormControl form : _forms) {
			form.cleanupTree();
		}
		_forms = null;
		super.tearDown();
	}

	/**
	 * (a) Two forms of the same element edit two different objects: both get their lock.
	 */
	public void testFormsOfOneElementLockDifferentObjects() throws Exception {
		FormElement element = formElement("<form input='" + INPUT + "'/>");
		FormControl first = display(element, _project);
		FormControl second = display(element, _otherProject);

		assertTrue(first.enterEditMode());
		assertTrue("A lock of another form must not stand in the way.", second.enterEditMode());

		assertLocked(_project);
		assertLocked(_otherProject);
	}

	/**
	 * (b) Two forms of the same element edit the same object: the second one fails with the
	 * conflict of the lock held by the first one, not with the handler's own lock.
	 */
	public void testSecondFormOnSameObjectReportsTheConflict() throws Exception {
		FormElement element = formElement("<form input='" + INPUT + "'/>");
		FormControl first = display(element, _project);
		FormControl second = display(element, _project);

		assertTrue(first.enterEditMode());
		try {
			second.enterEditMode();
			fail("The object is locked by the first form.");
		} catch (TopLogicException ex) {
			assertEquals(com.top_logic.base.locking.token.I18NConstants.ERROR_LOCK_CONFLICT.getKey(),
				ex.getErrorKey().getKey());
		}
		assertTrue(first.isEditMode());
		assertFalse(second.isEditMode());
	}

	/**
	 * (c) A form leaving edit mode releases its own lock only.
	 */
	public void testReleaseKeepsTheLockOfAnotherForm() throws Exception {
		FormElement element = formElement("<form input='" + INPUT + "'/>");
		FormControl first = display(element, _project);
		FormControl second = display(element, _otherProject);
		assertTrue(first.enterEditMode());
		assertTrue(second.enterEditMode());

		first.executeCancel();

		assertFalse(first.isEditMode());
		assertNotLocked(_project);
		assertLocked(_otherProject);
	}

	/**
	 * (d) A form without a lock handler configured locks its object when entering edit mode.
	 */
	public void testLockingByDefault() throws Exception {
		FormElement.Config config = formConfig("<form input='" + INPUT + "'/>");
		assertEquals(DefaultLockHandler.class, config.getLockHandler().getImplementationClass());

		FormControl form = display(formElement(config), _project);
		assertNotLocked(_project);

		assertTrue(form.enterEditMode());

		assertLocked(_project);
	}

	/**
	 * (e) A form with {@link NoTokenHandling} locks nothing.
	 */
	public void testNoTokenHandling() throws Exception {
		FormElement element = formElement("<form input='" + INPUT + "'>"
			+ "<lockHandler class='" + NoTokenHandling.class.getName() + "'/>"
			+ "</form>");
		FormControl first = display(element, _project);
		FormControl second = display(element, _project);

		assertTrue(first.enterEditMode());
		assertNotLocked(_project);
		assertTrue("Without locks, a second form edits the same object.", second.enterEditMode());
	}

	/**
	 * (f) A form starting in edit mode locks a persistent object.
	 */
	public void testInitialEditModeLocksPersistentObject() throws Exception {
		FormElement element = formElement("<form input='" + INPUT + "' initial-edit-mode='true'/>");
		FormControl form = display(element, _project);

		assertTrue(form.isEditMode());
		assertLocked(_project);
	}

	/**
	 * (f) A form starting in edit mode does not lock a transient object.
	 */
	public void testInitialEditModeDoesNotLockTransientObject() throws Exception {
		TLObject draft = TransientObjectFactory.INSTANCE.createObject(type(PROJECT));
		FormElement element = formElement("<form input='" + INPUT + "' initial-edit-mode='true'/>");
		FormControl first = display(element, draft);
		FormControl second = display(element, draft);

		assertTrue(first.isEditMode());
		assertTrue("A transient object is not locked.", second.isEditMode());
	}

	/**
	 * (g) Disposing a form in edit mode releases its lock.
	 */
	public void testCleanupReleasesTheLock() throws Exception {
		FormElement element = formElement("<form input='" + INPUT + "'/>");
		FormControl form = display(element, _project);
		assertTrue(form.enterEditMode());
		assertLocked(_project);

		form.cleanupTree();
		_forms.remove(form);

		assertNotLocked(_project);
	}

	/**
	 * A form displayed with its edit-mode channel set, whose object someone else has locked,
	 * opens read-only and tells the user why, instead of failing.
	 */
	public void testStartInEditModeOnLockedObjectOpensReadOnly() throws Exception {
		FormElement element = formElement("<form input='" + INPUT + "'/>");
		FormControl first = display(element, _project);
		assertTrue(first.enterEditMode());

		Window window = new Window(_project, true);
		FormControl second = window.display(formElement(EDIT_MODE_FORM));

		assertFalse(second.isEditMode());
		assertEquals(Boolean.FALSE, window._editMode.get());
		assertEquals(List.of(shownReadOnly(_project)), window._shown);
		assertTrue("The lock of the first form is kept.", first.isEditMode());
		assertLocked(_project);
	}

	/**
	 * A form displayed with its edit-mode channel set on an object nobody has locked starts in
	 * edit mode and locks the object.
	 */
	public void testStartInEditModeOnFreeObjectLocks() throws Exception {
		Window window = new Window(_project, true);
		FormControl form = window.display(formElement(EDIT_MODE_FORM));

		assertTrue(form.isEditMode());
		assertEquals(Boolean.TRUE, window._editMode.get());
		assertEquals(List.of(), window._shown);
		assertLocked(_project);
	}

	/**
	 * A form editable whenever an object is available, switching to an object someone else has
	 * locked, shows it read-only and tells the user why.
	 */
	public void testSwitchToLockedObjectOpensReadOnly() throws Exception {
		FormElement element = formElement("<form input='" + INPUT + "'/>");
		FormControl first = display(element, _project);
		assertTrue(first.enterEditMode());

		Window window = new Window(_otherProject, false);
		FormControl second =
			window.display(formElement("<form input='" + INPUT + "' initial-edit-mode='true'/>"));
		assertTrue(second.isEditMode());
		assertLocked(_otherProject);

		window._input.set(_project);

		assertFalse(second.isEditMode());
		assertEquals(List.of(shownReadOnly(_project)), window._shown);
		assertNotLocked(_otherProject);
		assertTrue(first.isEditMode());
	}

	/**
	 * The Edit command on an object someone else has locked fails with the lock conflict, and
	 * the form stays read-only.
	 */
	public void testEditCommandOnLockedObjectFails() throws Exception {
		FormElement element = formElement("<form input='" + INPUT + "'/>");
		FormControl first = display(element, _project);
		assertTrue(first.enterEditMode());

		Window window = new Window(_project, false);
		FormControl second = window.display(formElement(EDIT_MODE_FORM));
		try {
			second.executeCommand(FormControl.CMD_EDIT, Map.of());
			fail("The object is locked by the first form.");
		} catch (TopLogicException ex) {
			assertEquals(lockConflict(_project).getDetails().getKey(), ex.getDetails().getKey());
		}
		assertFalse(second.isEditMode());
		assertEquals(Boolean.FALSE, window._editMode.get());
		assertEquals("A refused command is reported by the command dispatch, not by the form.",
			List.of(), window._shown);
	}

	/**
	 * The text the user reads when a form shows the given object read-only because someone else
	 * has locked it.
	 */
	private static String shownReadOnly(TLObject object) {
		// Rendered like the shown message, in the language of the display.
		return "info: " + text(Fragments.message(com.top_logic.layout.view.I18NConstants.FORM_SHOWN_READ_ONLY))
			+ " " + text(Fragments.message(lockConflict(object).getDetails()));
	}

	/**
	 * The failure of acquiring the lock of the given object, which someone else holds.
	 */
	private static TopLogicException lockConflict(TLObject object) {
		Lock probe = LockService.getInstance().createLock(Token.DEFAULT_OPERATION, object);
		try {
			probe.lock();
		} catch (TopLogicException ex) {
			return ex;
		}
		probe.unlock();
		throw new AssertionError("Expected the object to be locked: " + object);
	}

	/**
	 * Displays the given object with the given element in a view context of its own, as a window
	 * of its own displays the view.
	 */
	private FormControl display(FormElement element, TLObject object) {
		return new Window(object, false).display(element);
	}

	/**
	 * A window displaying a form, with the channels the form is bound to and the messages shown in
	 * it.
	 */
	private class Window {

		final DefaultViewChannel _input = new DefaultViewChannel(INPUT);

		final DefaultViewChannel _editMode = new DefaultViewChannel(EDIT_MODE);

		final List<String> _shown = new ArrayList<>();

		private final ViewContext _context;

		Window(TLObject object, boolean editMode) {
			_context = new DefaultViewContext(
				new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")))
					.withErrorSink(new ErrorSink() {
						@Override
						public void showError(HTMLFragment content) {
							_shown.add("error: " + text(content));
						}

						@Override
						public void showWarning(HTMLFragment content) {
							_shown.add("warning: " + text(content));
						}

						@Override
						public void showInfo(HTMLFragment content) {
							_shown.add("info: " + text(content));
						}
					});
			_input.set(object);
			_editMode.set(Boolean.valueOf(editMode));
			_context.registerChannel(INPUT, _input);
			_context.registerChannel(EDIT_MODE, _editMode);
		}

		FormControl display(FormElement element) {
			FormControl result = (FormControl) element.createControl(_context);
			_forms.add(result);
			((ReactControl) result).attach();
			return result;
		}
	}

	/** The text a message shown to the user reads, with the markup around it stripped. */
	private static String text(HTMLFragment shown) {
		TagWriter out = new TagWriter();
		try {
			shown.write(new DummyDisplayContext(), out);
		} catch (IOException ex) {
			throw new AssertionError(ex);
		}
		return out.toString().replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
	}

	private static void assertLocked(TLObject object) {
		assertTrue("Expected the object to be locked: " + object, isLocked(object));
	}

	private static void assertNotLocked(TLObject object) {
		assertFalse("Expected the object not to be locked: " + object, isLocked(object));
	}

	/**
	 * Whether a lock for editing the given object is held, probed by trying to acquire it.
	 */
	private static boolean isLocked(TLObject object) {
		Lock probe = LockService.getInstance().createLock(Token.DEFAULT_OPERATION, object);
		if (probe.tryLock()) {
			probe.unlock();
			return false;
		}
		return true;
	}

	private static FormElement formElement(String xml) throws ConfigurationException {
		return formElement(formConfig(xml));
	}

	private static FormElement formElement(FormElement.Config config) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFormLocking.class);
		FormElement result = (FormElement) context.getInstance(config);
		context.checkErrors();
		return result;
	}

	private static FormElement.Config formConfig(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFormLocking.class);
		ConfigurationReader reader = new ConfigurationReader(context,
			Collections.singletonMap(FORM, TypedConfiguration.getConfigurationDescriptor(FormElement.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		FormElement.Config result = (FormElement.Config) reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suiteWith(TestFormLocking.class, LockService.Module.INSTANCE);
	}
}
