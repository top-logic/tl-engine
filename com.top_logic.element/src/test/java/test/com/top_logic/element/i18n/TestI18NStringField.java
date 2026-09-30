/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package test.com.top_logic.element.i18n;

import java.util.List;
import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.layout.form.model.TestDateTimeField;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.i18n.I18NField;
import com.top_logic.element.i18n.I18NStringField;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.StringField;

/**
 * Test for {@link I18NStringField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestI18NStringField extends BasicTestCase {

	private Locale _deLocale;

	private Locale _enLocale;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<Locale> supportedLocales = ResourcesModule.getInstance().getSupportedLocales();
		supportedLocales.forEach(locale -> {
			if ("de".equals(locale.getLanguage())) {
				_deLocale = locale;
			} else if ("en".equals(locale.getLanguage())) {
				_enLocale = locale;
			}
		});
		assertNotNull(_deLocale);
		assertNotNull(_enLocale);
	}

	/**
	 * Simple tests of {@link FormField#setValue(Object)} and {@link FormField#getValue()}.
	 */
	public void testSimpleValue() {
		I18NStringField field = newField(!FormFactory.MANDATORY, !FormFactory.IMMUTABLE);
		assertNull(field.getValue());

		ResKey key = newKey("Wert1", null);
		field.setValue(key);
		assertEquals(key, field.getValue());
		assertEquals("Wert1", deField(field).getValue());
		assertEquals(null, enField(field).getValue());

		key = newKey("Wert2", "value1");
		field.setValue(key);
		assertEquals(key, field.getValue());
		assertEquals("Wert2", deField(field).getValue());
		assertEquals("value1", enField(field).getValue());

		key = newKey(null, "value2");
		field.setValue(key);
		assertEquals(key, field.getValue());
		assertEquals(null, deField(field).getValue());
		assertEquals("value2", enField(field).getValue());
	}

	/**
	 * Tests the update of language fields on the client.
	 */
	public void testClientUpdate() {
		I18NStringField field = newField(!FormFactory.MANDATORY, !FormFactory.IMMUTABLE);
		assertNull(field.getValue());

		updateField(deField(field), "Wert1");
		assertEquals(newKey("Wert1", null), field.getValue());
		updateField(deField(field), "Wert2");
		assertEquals(newKey("Wert2", null), field.getValue());
		updateField(enField(field), "Value1");
		assertEquals(newKey("Wert2", "Value1"), field.getValue());
		updateField(enField(field), "Value2");
		updateField(deField(field), null);
		assertEquals(newKey(null, "Value2"), field.getValue());
	}

	/**
	 * Tests an {@link I18NField} with fallback, i.e. a value that is displayed as placeholder in
	 * the language fields.
	 */
	public void testFallback() {
		I18NStringField field = newField(!FormFactory.MANDATORY, !FormFactory.IMMUTABLE);
		assertNull(field.getValue());

		field.setPlaceholder(newKey("Wert1", null));
		assertEquals("Wert1", deField(field).getPlaceholder());
		assertEquals(null, enField(field).getPlaceholder());

		field.setPlaceholder(newKey("Wert2", "Value2"));
		assertEquals("Wert2", deField(field).getPlaceholder());
		assertEquals("Value2", enField(field).getPlaceholder());
		assertNull(field.getValue());

		field.setValue(newKey(null, "Value3"));
		assertNull("Field has a value (as whole), so no fallback is displayed.", deField(field).getPlaceholder());

		field.setValue(null);
		assertEquals("Wert2", deField(field).getPlaceholder());
		assertEquals("Value2", enField(field).getPlaceholder());

		updateField(deField(field), "Wert1");
		assertNull("Field has a value (as whole), so no fallback is displayed.", enField(field).getPlaceholder());
	}

	/**
	 * Tests mandatory {@link I18NField}.
	 */
	public void testMandatory() {
		I18NStringField field = newField(FormFactory.MANDATORY, !FormFactory.IMMUTABLE);
		assertFalse(field.hasError());
		field.checkAll();

		assertTrue(field.hasError());
		assertTrue(deField(field).hasError());
		assertTrue(enField(field).hasError());

		updateField(deField(field), "Wert1");

		assertFalse(field.hasError());
		assertFalse(deField(field).hasError());
		assertFalse(enField(field).hasError());

		updateField(deField(field), null);

		assertTrue(field.hasError());
		assertTrue(deField(field).hasError());
		assertTrue(enField(field).hasError());

		field.setValue(newKey(null, "Value1"));
		assertFalse(field.hasError());
		assertFalse(deField(field).hasError());
		assertFalse(enField(field).hasError());
	}

	private ResKey newKey(String de, String en) {
		Builder builder = ResKey.builder();
		if (de != null) {
			builder.add(_deLocale, de);
		}
		if (en != null) {
			builder.add(_deLocale, en);
		}
		return builder.build();
	}

	private StringField deField(I18NStringField field) {
		return languageField(field, _deLocale);
	}

	private StringField enField(I18NStringField field) {
		return languageField(field, _deLocale);
	}

	private StringField languageField(I18NStringField field, Locale locale) {
		List<StringField> languageFields = field.getLanguageFields();
		return languageFields.stream().filter(lf -> lf.get(I18NField.LANGUAGE) == locale).findFirst().get();
	}

	private void updateField(FormField field, String value) {
		try {
			FormFieldInternals.updateFieldNoClientUpdate(field, value);
		} catch (VetoException ex) {
			throw new IllegalArgumentException("Field has no veto listeners", ex);
		}
	}

	private I18NStringField newField(boolean mandatory, boolean immutable) {
		return I18NStringField.newI18NStringField("field", mandatory, immutable, false, FormFactory.NO_CONSTRAINT);
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestDateTimeField}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestDateTimeField.class);
	}
}

