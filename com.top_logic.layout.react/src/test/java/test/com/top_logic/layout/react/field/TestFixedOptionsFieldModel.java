/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.field.FixedOptionsFieldModel;

/**
 * Tests for {@link FixedOptionsFieldModel} - a field offered as a choice among fixed options.
 */
public class TestFixedOptionsFieldModel extends TestCase {

	private static final List<Boolean> YES_NO = List.of(Boolean.TRUE, Boolean.FALSE);

	/** The options are the ones given, and a single value is chosen unless asked otherwise. */
	public void testOptions() {
		FixedOptionsFieldModel choice = new FixedOptionsFieldModel(new AbstractFieldModel(null), YES_NO);

		assertEquals(YES_NO, choice.getOptions());
		assertFalse(choice.isMultiple());
	}

	/**
	 * A selection written by a select control reaches the field as the chosen option itself, an
	 * empty selection as no value; the field's value is what the choice reads.
	 */
	public void testValueRoundTrip() {
		AbstractFieldModel field = new AbstractFieldModel(null);
		FixedOptionsFieldModel choice = new FixedOptionsFieldModel(field, YES_NO, false, Boolean.FALSE);

		choice.setValue(List.of(Boolean.TRUE));
		assertEquals(Boolean.TRUE, field.getValue());
		assertEquals(Boolean.TRUE, choice.getValue());

		choice.setValue(List.of(Boolean.FALSE));
		assertEquals(Boolean.FALSE, field.getValue());
		assertEquals(Boolean.FALSE, choice.getValue());

		choice.setValue(Collections.emptyList());
		assertNull(field.getValue());
		assertNull(choice.getValue());

		field.setValue(Boolean.TRUE);
		assertEquals(Boolean.TRUE, choice.getValue());
	}

	/** Where several options are chosen, the field receives the selection as it is. */
	public void testMultipleValue() {
		AbstractFieldModel field = new AbstractFieldModel(null);
		FixedOptionsFieldModel choice = new FixedOptionsFieldModel(field, List.of("a", "b", "c"), true, null);

		choice.setValue(List.of("a", "c"));
		assertEquals(List.of("a", "c"), field.getValue());
		assertTrue(choice.isMultiple());
	}

	/**
	 * A stated mandatory flag overrides the field's, in both directions; without one, the field
	 * decides.
	 */
	public void testMandatoryOverride() {
		AbstractFieldModel optional = new AbstractFieldModel(null);
		AbstractFieldModel mandatory = new AbstractFieldModel(null);
		mandatory.setMandatory(true);

		FixedOptionsFieldModel twoValued = new FixedOptionsFieldModel(optional, YES_NO, false, Boolean.TRUE);
		assertTrue(twoValued.isMandatory());
		assertFalse(twoValued.isNullable());

		FixedOptionsFieldModel triState = new FixedOptionsFieldModel(mandatory, YES_NO, false, Boolean.FALSE);
		assertFalse(triState.isMandatory());
		assertTrue(triState.isNullable());

		assertFalse(new FixedOptionsFieldModel(optional, YES_NO).isMandatory());
		assertTrue(new FixedOptionsFieldModel(mandatory, YES_NO).isMandatory());
	}

	/** Editability and validation are the field's. */
	public void testDelegatesEditabilityAndValidation() {
		AbstractFieldModel field = new AbstractFieldModel(null);
		FixedOptionsFieldModel choice = new FixedOptionsFieldModel(field, YES_NO);

		field.setEditable(false);
		assertFalse(choice.isEditable());

		field.setEditable(true);
		field.setDisabled(true);
		assertTrue(choice.isDisabled());
	}

	/**
	 * A change of the field is reported to the listeners of the choice with the choice as its
	 * source, and a removed listener detaches the choice from the field.
	 */
	public void testListenersSeeTheChoiceAsSource() {
		AbstractFieldModel field = new AbstractFieldModel(null);
		FixedOptionsFieldModel choice = new FixedOptionsFieldModel(field, YES_NO, false, Boolean.TRUE);

		List<FieldModel> sources = new ArrayList<>();
		List<Object> values = new ArrayList<>();
		FieldModelListener listener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				sources.add(source);
				values.add(newValue);
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				sources.add(source);
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				sources.add(source);
				// The mandatory state read from the source is the one of the choice, not the field's.
				assertTrue(source.isMandatory());
			}
		};
		choice.addListener(listener);

		choice.setValue(List.of(Boolean.FALSE));
		field.setEditable(false);

		assertEquals(List.of(Boolean.FALSE), values);
		assertFalse(sources.isEmpty());
		for (FieldModel source : sources) {
			assertSame(choice, source);
		}

		choice.removeListener(listener);
		sources.clear();
		field.setValue(Boolean.TRUE);
		assertTrue("A removed listener is not notified.", sources.isEmpty());
	}

	/** New options are reported to the options listeners. */
	public void testSetOptions() {
		FixedOptionsFieldModel choice = new FixedOptionsFieldModel(new AbstractFieldModel(null), YES_NO);
		List<Object> reported = new ArrayList<>();
		choice.addOptionsListener((source, newOptions) -> reported.add(newOptions));

		choice.setOptions(List.of(Boolean.TRUE));

		assertEquals(List.of(Boolean.TRUE), choice.getOptions());
		assertEquals(List.of(List.of(Boolean.TRUE)), reported);
	}

}
