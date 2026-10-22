/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.select;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.DropdownSelectState;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a {@link ReactDropdownSelectControl} finds the option its value stands for by
 * {@link Object#equals(Object)}: a value that is equal to an option but not the same instance (a
 * string read from a channel, a number parsed from a configuration) is sent to the client with the
 * id of that option, so the client marks the option as selected.
 */
public class TestSelectOptionLookup extends TestCase {

	/** The command loading the option list of a display that opens on demand. */
	private static final String CMD_LOAD_OPTIONS = "loadOptions";

	/** A display showing every option is handed its options with the value right away. */
	public void testAnEqualValueSelectsItsOptionInAShapeShowingAllOptions() {
		Select select = select(copyOf("percent"), SelectDisplay.SEGMENTED);

		assertEquals(List.of(select.optionId("percent")), select.valueIds());
	}

	/** A display opening on demand indexes its options when they are loaded. */
	public void testAnEqualValueSelectsItsOptionWhenTheOptionsAreLoaded() {
		Select select = select(copyOf("scale"), SelectDisplay.DROPDOWN);

		select.loadOptions();

		assertEquals(List.of(select.optionId("scale")), select.valueIds());
	}

	/** A value set later is found among the options as well. */
	public void testAnEqualValueSetLaterSelectsItsOption() {
		Select select = select(null, SelectDisplay.SEGMENTED);
		assertEquals(List.of(), select.valueIds());

		select.model().setValue(copyOf("angle"));

		assertEquals(List.of(select.optionId("angle")), select.valueIds());
	}

	/** The id the client sends back resolves to the option it was allocated for. */
	public void testTheClientChoosesByTheOptionId() {
		Select select = select(copyOf("percent"), SelectDisplay.SEGMENTED);

		select.choose(select.optionId("angle"));

		assertEquals(List.of("angle"), select.model().getValue());
		assertEquals(List.of(select.optionId("angle")), select.valueIds());
	}

	/** A value that equals no option keeps an id of its own, so it is still displayed. */
	public void testAValueThatIsNoOptionKeepsItsOwnId() {
		Select select = select("other", SelectDisplay.SEGMENTED);

		List<String> valueIds = select.valueIds();
		assertEquals(1, valueIds.size());
		assertFalse(select.optionIds().contains(valueIds.get(0)));
	}

	/** A string equal to the given one, but a different instance. */
	private static String copyOf(String value) {
		return new String(value);
	}

	private static Select select(Object value, SelectDisplay display) {
		SimpleSelectFieldModel model =
			new SimpleSelectFieldModel(value, List.of("percent", "scale", "angle"), false);
		return new Select(context(), model, display);
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * A {@link ReactDropdownSelectControl} over string options, exposing its server-side state.
	 */
	private static final class Select extends ReactDropdownSelectControl {

		private final SimpleSelectFieldModel _model;

		Select(ReactContext context, SimpleSelectFieldModel model, SelectDisplay display) {
			super(context, model, String::valueOf, null, false, display);
			_model = model;
		}

		SimpleSelectFieldModel model() {
			return _model;
		}

		void loadOptions() {
			executeClientCommand(CMD_LOAD_OPTIONS, Map.of());
		}

		void choose(String optionId) {
			executeClientCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
				Map.of(FieldValueArguments.VALUE, List.of(optionId)));
		}

		/** The ids of the descriptors the state sends as the value. */
		List<String> valueIds() {
			return ids(FieldState.VALUE__PROP);
		}

		/** The ids of the descriptors the state sends as the options. */
		List<String> optionIds() {
			return ids(DropdownSelectState.OPTIONS__PROP);
		}

		/** The id of the option with the given label. */
		String optionId(String label) {
			for (Map<?, ?> descriptor : descriptors(DropdownSelectState.OPTIONS__PROP)) {
				if (label.equals(descriptor.get(DropdownSelectState.Option.LABEL__PROP))) {
					return (String) descriptor.get(DropdownSelectState.Option.VALUE__PROP);
				}
			}
			throw new AssertionError("No option labelled '" + label + "'.");
		}

		private List<String> ids(String key) {
			List<String> ids = new ArrayList<>();
			for (Map<?, ?> descriptor : descriptors(key)) {
				ids.add((String) descriptor.get(DropdownSelectState.Option.VALUE__PROP));
			}
			return ids;
		}

		@SuppressWarnings("unchecked")
		private List<Map<?, ?>> descriptors(String key) {
			Object state = getState(key);
			return state == null ? List.of() : (List<Map<?, ?>>) state;
		}
	}

	/**
	 * The test suite, started with the resource bundles the empty option label needs.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestSelectOptionLookup.class, ResourcesModule.Module.INSTANCE));
	}

}
