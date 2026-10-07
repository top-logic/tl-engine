/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.MoveElementArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactValueListControl;
import com.top_logic.layout.react.control.form.RemoveElementArguments;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a field holding several values is edited as a list of single-value controls: one
 * control per value, each writing its own position back into the collection.
 */
public class TestReactValueListControl extends TestCase {

	/**
	 * Every value is displayed by a control of its own, holding that value; the values read as one
	 * text.
	 */
	public void testControlPerValue() {
		ValueList list = list(Arrays.asList("T1", "T2"), FieldSpec.of(String.class, "Texts"));

		assertEquals(2, list.elements().size());
		assertEquals("T1", list.valueOf(0));
		assertEquals("T2", list.valueOf(1));
		assertEquals(ReactValueListControl.LAYOUT_INLINE, list.layout());
	}

	/**
	 * A value that is a text of several lines takes a line of its own.
	 */
	public void testMultilineValuesAreBlocks() {
		ValueList list =
			list(Arrays.asList("T1", "T2"), FieldSpec.of(String.class, "Texts").setMultilineRows(3));

		assertEquals(ReactValueListControl.LAYOUT_BLOCK, list.layout());
	}

	/**
	 * Entering a value writes back exactly that position; the control of an untouched value is the
	 * one that was there before, holding the value it held.
	 */
	public void testEditingOneValue() {
		ValueList list = list(Arrays.asList("T1", "T2"), FieldSpec.of(String.class, "Texts"));
		ReactControl first = list.elements().get(0);

		list.type(1, "X");

		assertEquals(Arrays.asList("T1", "X"), list.getFieldModel().getValue());
		assertSame("The untouched value keeps its control", first, list.elements().get(0));
		assertEquals("T1", list.valueOf(0));
		assertEquals("X", list.valueOf(1));
	}

	/**
	 * The add button appends an empty value, which appears as a further control to enter it in.
	 */
	public void testAddValue() {
		ValueList list = list(Arrays.asList("T1", "X"), FieldSpec.of(String.class, "Texts"));

		list.add();

		assertEquals(Arrays.asList("T1", "X", null), list.getFieldModel().getValue());
		assertEquals(3, list.elements().size());
		assertNull(list.valueOf(2));
	}

	/**
	 * The remove button drops the value at the position it names; what is left keeps its order.
	 */
	public void testRemoveValue() {
		ValueList list = list(Arrays.asList("T1", "X", null), FieldSpec.of(String.class, "Texts"));

		list.remove(0);

		assertEquals(Arrays.asList("X", null), list.getFieldModel().getValue());
		assertEquals(2, list.elements().size());
		assertEquals("X", list.valueOf(0));
		assertNull(list.valueOf(1));
	}

	/**
	 * A value moved to a later position is stored there, and the values it passes move up.
	 */
	public void testMoveValueForwards() {
		ValueList list = list(Arrays.asList("T1", "T2", "T3"), ordered());
		List<ReactControl> before = List.copyOf(list.elements());

		list.move(0, 2);

		assertEquals(Arrays.asList("T2", "T3", "T1"), list.getFieldModel().getValue());
		assertEquals("T2", list.valueOf(0));
		assertEquals("T3", list.valueOf(1));
		assertEquals("T1", list.valueOf(2));
		assertControlsInPlace(before, list);
	}

	/**
	 * A value moved to an earlier position is stored there, and the values it passes move down.
	 */
	public void testMoveValueBackwards() {
		ValueList list = list(Arrays.asList("T1", "T2", "T3"), ordered());
		List<ReactControl> before = List.copyOf(list.elements());

		list.move(2, 0);

		assertEquals(Arrays.asList("T3", "T1", "T2"), list.getFieldModel().getValue());
		assertEquals("T3", list.valueOf(0));
		assertEquals("T1", list.valueOf(1));
		assertEquals("T2", list.valueOf(2));
		assertControlsInPlace(before, list);
	}

	/**
	 * A move naming a position that holds no value is not carried out.
	 */
	public void testMoveOutOfRange() {
		ValueList list = list(Arrays.asList("T1", "T2"), ordered());

		list.move(2, 0);
		list.move(-1, 0);
		list.move(0, 2);
		list.move(0, -1);

		assertEquals(Arrays.asList("T1", "T2"), list.getFieldModel().getValue());
	}

	/**
	 * A value moved to the position it already holds stays where it is.
	 */
	public void testMoveToSamePosition() {
		ValueList list = list(Arrays.asList("T1", "T2"), ordered());
		List<ReactControl> before = List.copyOf(list.elements());

		list.move(1, 1);

		assertEquals(Arrays.asList("T1", "T2"), list.getFieldModel().getValue());
		assertControlsInPlace(before, list);
	}

	/**
	 * The values of a field that is only displayed cannot be arranged.
	 */
	public void testMoveNotEditable() {
		AbstractFieldModel model = new AbstractFieldModel(Arrays.asList("T1", "T2"));
		model.setEditable(false);
		ValueList list = list(model, ordered());

		list.move(0, 1);

		assertEquals(Arrays.asList("T1", "T2"), list.getFieldModel().getValue());
	}

	/**
	 * A field whose values form a set offers no arranging, and a move is not carried out.
	 */
	public void testUnorderedValuesAreNotArranged() {
		ValueList list = list(Arrays.asList("T1", "T2"), FieldSpec.of(String.class, "Texts"));

		assertEquals(Boolean.FALSE, list.ordered());

		list.move(0, 1);

		assertEquals(Arrays.asList("T1", "T2"), list.getFieldModel().getValue());
	}

	/**
	 * A field whose order is part of its value offers arranging.
	 */
	public void testOrderedValuesAreArranged() {
		ValueList list = list(Arrays.asList("T1", "T2"), ordered());

		assertEquals(Boolean.TRUE, list.ordered());
	}

	/**
	 * A collection written from elsewhere is taken over: the controls follow the values it holds.
	 */
	public void testExternalChange() {
		ValueList list = list(Arrays.asList("T1", "T2"), FieldSpec.of(String.class, "Texts"));

		list.getFieldModel().setValue(List.of("A"));

		assertEquals(1, list.elements().size());
		assertEquals("A", list.valueOf(0));
	}

	/**
	 * A field that cannot be changed displays its values, and none of them can be entered.
	 */
	public void testNonEditable() {
		AbstractFieldModel model = new AbstractFieldModel(Arrays.asList("T1", "T2"));
		model.setEditable(false);
		ValueList list = list(model, FieldSpec.of(String.class, "Texts"));

		assertEquals(Boolean.FALSE, list.editableOf(0));
		assertEquals(Boolean.FALSE, list.editableOf(1));

		list.type(0, "X");

		assertEquals("A value of a field that is only displayed is not taken",
			Arrays.asList("T1", "T2"), list.getFieldModel().getValue());
	}

	/**
	 * Making the field editable makes its values editable with it.
	 */
	public void testEditabilityFollows() {
		AbstractFieldModel model = new AbstractFieldModel(Arrays.asList("T1", "T2"));
		model.setEditable(false);
		ValueList list = list(model, FieldSpec.of(String.class, "Texts"));

		model.setEditable(true);

		assertEquals(Boolean.TRUE, list.editableOf(0));
		assertEquals(Boolean.TRUE, list.editableOf(1));
	}

	/**
	 * The values of a disabled field are shown as inactive inputs, not as displayed values.
	 */
	public void testDisabled() {
		AbstractFieldModel model = new AbstractFieldModel(Arrays.asList("T1", "T2"));
		model.setDisabled(true);
		ValueList list = list(model, FieldSpec.of(String.class, "Texts"));

		assertEquals(Boolean.TRUE, list.disabledOf(0));
		assertEquals(Boolean.TRUE, list.disabledOf(1));
		assertEquals(Boolean.FALSE, list.editableOf(0));
	}

	/**
	 * Disabling and enabling the field disables and enables its values with it, also where the
	 * field is not editable either way.
	 */
	public void testDisabledFollows() {
		AbstractFieldModel model = new AbstractFieldModel(Arrays.asList("T1", "T2"));
		model.setEditable(false);
		ValueList list = list(model, FieldSpec.of(String.class, "Texts"));
		assertEquals(Boolean.FALSE, list.disabledOf(0));

		model.setDisabled(true);

		assertEquals(Boolean.TRUE, list.disabledOf(0));
		assertEquals(Boolean.TRUE, list.disabledOf(1));

		model.setEditable(true);
		model.setDisabled(false);

		assertEquals(Boolean.FALSE, list.disabledOf(0));
		assertEquals(Boolean.TRUE, list.editableOf(0));
	}

	/**
	 * A list saved while disabled shows its values as read-only values afterwards: none of them can
	 * be entered, and a value sent for one of them is not taken.
	 */
	public void testViewAfterDisabled() {
		AbstractFieldModel model = new AbstractFieldModel(Arrays.asList("T1", "T2"));
		ValueList list = list(model, FieldSpec.of(String.class, "Texts"));
		mode(model, true, false);
		mode(model, false, true);

		mode(model, false, false);

		assertReadOnly(list);
		list.type(0, "X");
		assertEquals("A value of a field that is only displayed is not taken",
			Arrays.asList("T1", "T2"), model.getValue());
	}

	/**
	 * A displayed list that is edited while disabled shows its values as inactive inputs.
	 */
	public void testDisabledAfterView() {
		AbstractFieldModel model = new AbstractFieldModel(Arrays.asList("T1", "T2"));
		model.setEditable(false);
		ValueList list = list(model, FieldSpec.of(String.class, "Texts"));

		mode(model, false, true);

		for (int n = 0; n < 2; n++) {
			assertEquals(Boolean.FALSE, list.editableOf(n));
			assertEquals(Boolean.TRUE, list.disabledOf(n));
		}
		list.type(0, "X");
		assertEquals(Arrays.asList("T1", "T2"), model.getValue());
	}

	/**
	 * A list that stops being disabled while it is edited makes its values editable again.
	 */
	public void testEditableAfterDisabled() {
		AbstractFieldModel model = new AbstractFieldModel(Arrays.asList("T1", "T2"));
		model.setEditable(false);
		ValueList list = list(model, FieldSpec.of(String.class, "Texts"));
		mode(model, false, true);

		mode(model, true, false);

		for (int n = 0; n < 2; n++) {
			assertEquals(Boolean.TRUE, list.editableOf(n));
			assertEquals(Boolean.FALSE, list.disabledOf(n));
		}
		list.type(0, "X");
		assertEquals(Arrays.asList("X", "T2"), model.getValue());
	}

	/**
	 * A value added to a list that goes back to being displayed after being disabled is read-only,
	 * like the values that were there before.
	 */
	public void testExternalChangeAfterDisabled() {
		AbstractFieldModel model = new AbstractFieldModel(Arrays.asList("T1"));
		ValueList list = list(model, FieldSpec.of(String.class, "Texts"));
		mode(model, false, true);
		mode(model, false, false);

		model.setValue(Arrays.asList("T1", "T2"));

		assertReadOnly(list);
	}

	/**
	 * Sets the state of the list the way a form field does when its mode is applied: the disabled
	 * state first, then the editability.
	 */
	private static void mode(AbstractFieldModel model, boolean editable, boolean disabled) {
		model.setDisabled(disabled);
		model.setEditable(editable);
	}

	private static void assertReadOnly(ValueList list) {
		for (int n = 0; n < list.elements().size(); n++) {
			assertEquals("Value " + n + " is not editable", Boolean.FALSE, list.editableOf(n));
			assertEquals("Value " + n + " is not disabled", Boolean.FALSE, list.disabledOf(n));
		}
	}

	/**
	 * An unordered collection is written back as a list in the order its values are displayed in,
	 * so that a value keeps the position it was entered at.
	 */
	public void testUnorderedValues() {
		FieldModel model = new AbstractFieldModel(new LinkedHashSet<>(Arrays.asList("T1", "T2")));
		ValueList list = list(model, FieldSpec.of(String.class, "Texts"));

		list.type(1, "X");

		Object stored = list.getFieldModel().getValue();
		assertTrue("The collection is written back as a list, got: " + stored, stored instanceof List<?>);
		assertEquals(Arrays.asList("T1", "X"), stored);
	}

	/**
	 * A field holding nothing displays no value; the add button starts the first one.
	 */
	public void testEmptyField() {
		ValueList list = list(new AbstractFieldModel(null), FieldSpec.of(String.class, "Texts"));

		assertEquals(0, list.elements().size());

		list.add();

		assertEquals(1, list.elements().size());
		assertEquals(Collections.singletonList(null), list.getFieldModel().getValue());
	}

	private static FieldSpec ordered() {
		return FieldSpec.of(String.class, "Texts").setOrdered(true);
	}

	private static void assertControlsInPlace(List<ReactControl> before, ValueList list) {
		assertEquals("Arranging the values creates no control", before.size(), list.elements().size());
		for (int n = 0; n < before.size(); n++) {
			assertSame("The control at position " + n + " stays where it is", before.get(n),
				list.elements().get(n));
		}
	}

	private static ValueList list(Object value, FieldSpec spec) {
		return list(new AbstractFieldModel(value), spec);
	}

	private static ValueList list(FieldModel model, FieldSpec spec) {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		return new ValueList(context, model, spec, FieldControlRegistry.TEXT);
	}

	/**
	 * A {@link ReactValueListControl} exposing its server-side state, and the gestures of its
	 * client, for assertions.
	 */
	private static final class ValueList extends ReactValueListControl {

		ValueList(ReactContext context, FieldModel listModel, FieldSpec fieldSpec,
				ReactFieldControlProvider elementProvider) {
			super(context, listModel, fieldSpec, elementProvider);
		}

		/** The controls editing the single values, in value order. */
		List<ReactControl> elements() {
			return displayedChildren();
		}

		/** How the values are arranged. */
		Object layout() {
			return getState(LAYOUT);
		}

		/** Whether the client offers arranging the values. */
		Object ordered() {
			return getState(ORDERED);
		}

		/** The value the control at the given position holds. */
		Object valueOf(int index) {
			return elements().get(index).scriptingScalarState().get(FieldState.VALUE__PROP);
		}

		/** Whether the control at the given position can be changed. */
		Object editableOf(int index) {
			return elements().get(index).scriptingScalarState().get(FieldState.EDITABLE__PROP);
		}

		/** Whether the control at the given position is shown as an inactive input. */
		Object disabledOf(int index) {
			return elements().get(index).scriptingScalarState().get(FieldState.DISABLED__PROP);
		}

		/** Enters the given text into the control at the given position, as its client does. */
		void type(int index, String text) {
			elements().get(index).executeClientCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
				Map.of(FieldValueArguments.VALUE, text));
		}

		/** Appends an empty value, as the client's add button does. */
		void add() {
			executeClientCommand(CMD_ADD_ELEMENT, Map.of());
		}

		/** Drops the value at the given position, as the client's remove button does. */
		void remove(int index) {
			executeClientCommand(CMD_REMOVE_ELEMENT,
				Map.of(RemoveElementArguments.INDEX, Integer.valueOf(index)));
		}

		/** Moves the value at the given position to the given one, as the client's gesture does. */
		void move(int index, int targetIndex) {
			executeClientCommand(CMD_MOVE_ELEMENT,
				Map.of(MoveElementArguments.INDEX, Integer.valueOf(index),
					MoveElementArguments.TARGET_INDEX, Integer.valueOf(targetIndex)));
		}
	}

	/**
	 * The test suite, started with the resource bundles a field's messages need.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactValueListControl.class, ResourcesModule.Module.INSTANCE));
	}

}
