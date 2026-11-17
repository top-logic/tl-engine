/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.util.Utils;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeWithFallbackStorage;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.FallbackFieldDisplay;
import com.top_logic.element.meta.form.FieldModeObserver;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.model.copy.CopyOperation;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for {@link FallbackFieldDisplay}: the field of an attribute with an
 * {@link AttributeWithFallbackStorage} in a form built by the {@link AttributeFormFactory}.
 *
 * <p>
 * The fallback attribute is derived from the attribute {@code name}, the explicit value is stored
 * in the attribute {@code valueStorage}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestFallbackFieldDisplay extends AbstractSearchExpressionTest {

	private static final String TYPE = "TestFallbackFieldDisplay:Item";

	private static final String NAME = "name";

	private static final String VALUE = "value";

	private static final String VALUE_STORAGE = "valueStorage";

	private AttributeFormContext _form;

	private TLFormObject _item;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_form = new AttributeFormContext(ResPrefix.forTest("TestFallbackFieldDisplay"));
		_item = _form.createObject(type(), null);
	}

	@Override
	protected void tearDown() throws Exception {
		_item = null;
		_form = null;
		super.tearDown();
	}

	public void testDerivedFallbackWithoutField() {
		FormField nameField = field(NAME);
		FormField valueField = field(VALUE);

		nameField.setValue("first");
		assertEquals("first", valueField.getPlaceholder());
		assertEmpty(valueField.getValue());

		nameField.setValue("second");
		assertEquals("Fallback derived from another field is updated.", "second", valueField.getPlaceholder());
	}

	public void testFallbackChangeInInactiveField() {
		FormField nameField = field(NAME);
		FormField valueField = field(VALUE);
		nameField.setValue("first");

		valueField.setImmutable(true);
		assertEquals("Inactive field displays fallback as value.", "first", valueField.getValue());
		assertNull(valueField.getPlaceholder());

		nameField.setValue("second");
		assertEquals("Inactive field displays changed fallback.", "second", valueField.getValue());
		assertNull(valueField.getPlaceholder());

		valueField.setImmutable(false);
		assertEmpty(valueField.getValue());
		assertEquals("Fallback did not become an explicit value.", "second", valueField.getPlaceholder());
		assertEmpty(storedValue(VALUE));
	}

	public void testExplicitValueInInactiveField() {
		FormField nameField = field(NAME);
		FormField valueField = field(VALUE);
		nameField.setValue("fallback");
		valueField.setValue("explicit");

		valueField.setImmutable(true);
		assertEquals("explicit", valueField.getValue());

		nameField.setValue("other");
		assertEquals("explicit", valueField.getValue());

		valueField.setImmutable(false);
		assertEquals("explicit", valueField.getValue());
		assertEquals("other", valueField.getPlaceholder());
		assertEquals("explicit", storedValue(VALUE));
	}

	public void testStorageAttributeChange() {
		FormField storageField = field(VALUE_STORAGE);
		FormField nameField = field(NAME);
		FormField valueField = field(VALUE);
		nameField.setValue("fallback");

		storageField.setValue("explicit");
		assertEquals("Change of storage attribute is displayed.", "explicit", valueField.getValue());

		valueField.setImmutable(true);
		storageField.setValue(null);
		assertEquals("Removing the explicit value displays the fallback.", "fallback", valueField.getValue());
	}

	public void testDynamicallyHidden() {
		FormField nameField = field(NAME);
		FormField valueField = field(VALUE);
		nameField.setValue("fallback");

		FieldModeObserver observer = modeObserver(valueField);
		observer.valueChanged(FormVisibility.HIDDEN);
		observer.valueChanged(FormVisibility.EDITABLE);

		assertEmpty(valueField.getValue());
		assertEquals("Hiding the field keeps the fallback.", "fallback", valueField.getPlaceholder());
	}

	public void testDynamicallyHiddenClearsExplicitValue() {
		FormField nameField = field(NAME);
		FormField valueField = field(VALUE);
		nameField.setValue("fallback");
		valueField.setValue("explicit");

		FieldModeObserver observer = modeObserver(valueField);
		observer.valueChanged(FormVisibility.HIDDEN);
		assertTrue("Hiding the field clears the explicit value.", Utils.isEmpty(storedValue(VALUE)));

		observer.valueChanged(FormVisibility.EDITABLE);
		assertEmpty(valueField.getValue());
		assertEquals("fallback", valueField.getPlaceholder());
	}

	public void testDynamicallyReadOnly() {
		FormField nameField = field(NAME);
		FormField valueField = field(VALUE);
		nameField.setValue("fallback");

		FieldModeObserver observer = modeObserver(valueField);
		observer.valueChanged(FormVisibility.READ_ONLY);
		assertEquals("Read-only field displays fallback.", "fallback", valueField.getValue());
		assertEmpty(storedValue(VALUE));

		nameField.setValue("other");
		assertEquals("other", valueField.getValue());
	}

	public void testStorageAttributeOfOverlay() {
		FormField nameField = field(NAME);
		FormField valueField = field(VALUE);
		nameField.setValue("fallback");
		valueField.setValue("explicit");

		assertEquals("Storage attribute without field reflects the explicit value.", "explicit",
			_item.tValue(part(VALUE_STORAGE)));
		assertEquals("explicit", _item.tValue(part(VALUE)));

		valueField.setValue(null);
		assertEmpty(_item.tValue(part(VALUE_STORAGE)));
		assertEquals("fallback", _item.tValue(part(VALUE)));
	}

	public void testCopyOverlay() {
		FormField nameField = field(NAME);
		FormField valueField = field(VALUE);
		nameField.setValue("fallback");
		valueField.setValue("explicit");

		TLObject copy = transientCopy();
		assertEquals("Copy keeps the entered explicit value.", "explicit", copy.tValue(part(VALUE_STORAGE)));
		assertEquals("explicit", copy.tValue(part(VALUE)));
	}

	public void testCopyOverlayDisplayingFallback() {
		FormField nameField = field(NAME);
		FormField valueField = field(VALUE);
		nameField.setValue("fallback");
		valueField.setImmutable(true);
		assertEquals("fallback", valueField.getValue());

		TLObject copy = transientCopy();
		assertEmpty(copy.tValue(part(VALUE_STORAGE)));
	}

	private TLObject transientCopy() {
		CopyOperation operation = CopyOperation.initial();
		operation.setContext(null, null);
		operation.setTransient(true);
		TLObject copy = (TLObject) operation.copyReference(_item);
		operation.finish();
		return copy;
	}

	private FieldModeObserver modeObserver(FormField field) {
		return new FieldModeObserver(field, _form.getAttributeUpdateContainer(), null, _item, part(VALUE), true);
	}

	/**
	 * The value that is transferred to the model, when the form is stored.
	 */
	private Object storedValue(String name) {
		_form.getAttributeUpdateContainer().update();
		AttributeUpdate update = _item.getUpdate(part(name));
		return update.getCorrectValues();
	}

	private static void assertEmpty(Object value) {
		assertTrue("Expected empty value, got: " + value, Utils.isEmpty(value));
	}

	private FormField field(String name) {
		AttributeUpdate update = _item.newCreateUpdate(part(name));
		return (FormField) AttributeFormFactory.getInstance().toFormMember(update,
			_form.getAttributeUpdateContainer());
	}

	private static TLStructuredTypePart part(String name) {
		return type().getPart(name);
	}

	private static TLClass type() {
		return (TLClass) TLModelUtil.findType(TYPE);
	}

	public static Test suite() {
		return suite(TestFallbackFieldDisplay.class);
	}

}
