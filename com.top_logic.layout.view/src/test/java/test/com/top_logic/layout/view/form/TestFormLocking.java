/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.base.locking.handler.DefaultLockHandler;
import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.base.locking.token.Token;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.DefaultReactContext;
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
 * of its own for every displayed form.
 */
@SuppressWarnings("javadoc")
public class TestFormLocking extends AbstractModelAccessTest {

	private static final String FORM = "form";

	private static final String INPUT = "item";

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
	 * Displays the given object with the given element in a view context of its own, as a window
	 * of its own displays the view.
	 */
	private FormControl display(FormElement element, TLObject object) {
		ViewContext context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		DefaultViewChannel input = new DefaultViewChannel(INPUT);
		input.set(object);
		context.registerChannel(INPUT, input);
		FormControl result = (FormControl) element.createControl(context);
		_forms.add(result);
		((ReactControl) result).attach();
		return result;
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
