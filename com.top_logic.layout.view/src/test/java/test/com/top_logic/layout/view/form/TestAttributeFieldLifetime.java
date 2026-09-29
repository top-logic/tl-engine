/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.util.model.ModelService;

/**
 * Tests that an {@link AttributeFieldControl} lives exactly as long as the chrome it displays
 * itself through.
 *
 * <p>
 * A form can outlive one of its fields: a field inside a switch, a visible-if or a list item is
 * disposed while the form stays. Such a field must no longer follow the form - otherwise every
 * later change of the form builds input controls into the disposed chrome that nobody disposes,
 * and the form keeps the field reachable for the lifetime of the session.
 * </p>
 */
public class TestAttributeFieldLifetime extends BasicTestCase {

	private ItemFixture _items;

	private FormControl _form;

	private ViewChannel _input;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_items = new ItemFixture("test.attributeFieldLifetime");

		TLObject initial = _items.newItem("initial");
		_form = new FormControl(new HeadlessReactContext(), initial, "no model", NoTokenHandling.INSTANCE);
		_input = new DefaultViewChannel("input");
		_input.set(initial);
		_form.setInputChannel(_input);
	}

	@Override
	protected void tearDown() throws Exception {
		_input = null;
		_form = null;
		_items = null;

		super.tearDown();
	}

	/**
	 * Tests that a live field follows the form to the object it switches to.
	 */
	public void testALiveFieldFollowsTheForm() {
		CountingField field = newField();
		ReactFormFieldChromeControl chrome = field.createChromeControl();
		ReactControl inner = field.getInnerControl();

		_input.set(_items.newItem("next"));

		assertEquals("A live field is told about the object switch.", 1, field.notifications());
		assertNotNull("A live field keeps its model.", field.getResolvedPart());
		assertSame("The field rebinds its input rather than building a new one.", inner, field.getInnerControl());
		assertSame(chrome, field.getChromeControl());

		_form.revealAllValidation();
		assertEquals("A live field takes part in the editing of the form.", 1, field.reveals());
	}

	/**
	 * Tests that a field whose chrome was disposed is no longer reached by the form: it builds no
	 * input control, holds no model and takes no part in the form.
	 */
	public void testADisposedFieldIgnoresTheForm() {
		CountingField field = newField();
		ReactFormFieldChromeControl chrome = field.createChromeControl();
		ReactControl inner = field.getInnerControl();

		chrome.cleanupTree();

		assertTrue("Disposing the chrome disposes the input it shows.", inner.isDisposed());
		assertNull("A disposed field releases its model.", field.getResolvedPart());

		_input.set(_items.newItem("next"));
		_input.set(null);
		_input.set(_items.newItem("last"));

		assertEquals("A disposed field is no listener of the form any more.", 0, field.notifications());
		assertSame("A disposed field builds no input control.", inner, field.getInnerControl());

		_form.revealAllValidation();
		assertEquals("A disposed field takes no part in the editing of the form.", 0, field.reveals());
	}

	/**
	 * Tests that a field disposed while it showed no object - the placeholder a form without an
	 * object displays - is released as well.
	 */
	public void testADisposedPlaceholderIgnoresTheForm() {
		_input.set(null);

		CountingField field = newField();
		ReactFormFieldChromeControl chrome = field.createChromeControl();
		ReactControl placeholder = field.getInnerControl();

		chrome.cleanupTree();

		_input.set(_items.newItem("next"));

		assertEquals("A disposed placeholder is no listener of the form any more.", 0, field.notifications());
		assertSame("A disposed placeholder builds no input control.", placeholder, field.getInnerControl());
		assertNull("A disposed placeholder builds no model.", field.getResolvedPart());
	}

	private CountingField newField() {
		return new CountingField(_form.getReactContext(), _form, _form);
	}

	/**
	 * {@link AttributeFieldControl} for {@link ItemFixture#NAME} counting the calls through which
	 * the form reaches it.
	 */
	private static class CountingField extends AttributeFieldControl {

		private int _notifications;

		private int _reveals;

		CountingField(ReactContext context, FormModel formModel, FormControl formControl) {
			super(context, formModel, formControl, ItemFixture.NAME, null, false, null);
		}

		@Override
		public void onFormStateChanged(FormModel source) {
			_notifications++;
			super.onFormStateChanged(source);
		}

		@Override
		public void revealAll() {
			_reveals++;
			super.revealAll();
		}

		/** The number of form state changes the field was told about. */
		int notifications() {
			return _notifications;
		}

		/** The number of times the form asked the field, as one of its participants, to reveal errors. */
		int reveals() {
			return _reveals;
		}
	}

	/**
	 * React context of a test whose objects are transient and observed by nobody, and which
	 * therefore has no {@link ModelScope}.
	 */
	private static final class HeadlessReactContext extends DefaultReactContext {

		HeadlessReactContext() {
			super("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		}

		@Override
		public ModelScope getModelScope() {
			return null;
		}
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The fields resolve their input controls through the {@link FieldControlService},
	 *           which depends on the {@link ModelService} and therefore on a knowledge base.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestAttributeFieldLifetime.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}

}
