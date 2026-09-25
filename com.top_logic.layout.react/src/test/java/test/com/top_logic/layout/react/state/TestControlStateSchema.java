/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.state;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import junit.framework.TestCase;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonAppearance;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.ButtonSize;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.layout.react.control.button.KeyStroke;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ButtonState;
import com.top_logic.layout.react.state.CheckboxState;
import com.top_logic.layout.react.state.ControlState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.annotate.ui.BooleanPresentation;
import com.top_logic.tool.boundsec.HandlerResult;

import de.haumacher.msgbuf.data.ProtocolEnum;

/**
 * Tests that the controls and the state schema in {@code state.proto} agree: every state key a
 * control puts is declared in the message of its component, and every enumeration value it sends is
 * spelled as the schema declares it.
 */
public class TestControlStateSchema extends TestCase {

	/**
	 * The state keys put by the recording controls, including those put from their constructors.
	 */
	private static final Set<String> KEYS = new HashSet<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		KEYS.clear();
	}

	/**
	 * A {@link ReactButtonControl} recording the keys it puts into its state.
	 */
	private static final class RecordingButton extends ReactButtonControl {

		RecordingButton(ReactContext context) {
			super(context, "Label", ctx -> HandlerResult.DEFAULT_RESULT);
		}

		@Override
		protected void putState(String key, Object value) {
			KEYS.add(key);
			super.putState(key, value);
		}
	}

	/**
	 * A {@link ReactCheckboxControl} recording the keys it puts into its state.
	 */
	private static final class RecordingCheckbox extends ReactCheckboxControl {

		RecordingCheckbox(ReactContext context, BooleanPresentation presentation, boolean triState) {
			super(context, new AbstractFieldModel(Boolean.TRUE), presentation, triState);
		}

		@Override
		protected void putState(String key, Object value) {
			KEYS.add(key);
			super.putState(key, value);
		}

		/** Puts every field key the base class offers setters for. */
		void putFieldKeys() {
			setPlaceholder("placeholder");
			setSubmitListener(value -> {
				// Ignored.
			});
			setEditable(false);
			setMandatory(true);
			setHasError(true);
			setHasWarnings(true);
			setErrorMessage("error");
			setHidden(true);
			setCssClass("css");
		}
	}

	private static ReactContext createContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * Every key a {@link ReactButtonControl} puts is a property of {@link ButtonState}, and every
	 * property of {@link ButtonState} is put by some setter of the control.
	 */
	public void testButtonKeys() {
		RecordingButton button = new RecordingButton(createContext());
		button.setDisabled(true);
		button.setActive(true);
		button.setImage(null);
		button.setTooltip("tooltip");
		button.setKeyGesture(KeyStroke.ENTER);
		button.setDisplayMode(ButtonDisplayMode.ICON_LABEL);
		button.setCssClasses("a b");
		button.setAppearance(ButtonAppearance.PRIMARY);
		button.setTone(ButtonTone.DANGER);
		button.setSize(ButtonSize.SMALL);
		button.setNavigateUrl("https://example.com/");
		button.setNavigateNewWindow(true);
		button.setHidden(true);
		button.setCssClass("css");

		assertEquals(properties(ButtonState.class), KEYS);
	}

	/**
	 * Every key a {@link ReactCheckboxControl} puts is a property of {@link CheckboxState}.
	 */
	public void testCheckboxKeys() {
		new RecordingCheckbox(createContext(), BooleanPresentation.SWITCH, false).putFieldKeys();
		new RecordingCheckbox(createContext(), BooleanPresentation.CHECKBOX, true);

		Set<String> undeclared = new HashSet<>(KEYS);
		undeclared.removeAll(properties(CheckboxState.class));
		assertEquals("State keys not declared in the schema.", Set.of(), undeclared);
		assertTrue(KEYS.contains(CheckboxState.TRI_STATE__PROP));
		assertTrue(KEYS.contains(CheckboxState.DISPLAY__PROP));
	}

	/**
	 * The display modes are sent as the schema spells them.
	 */
	public void testButtonDisplayMode() {
		assertSameNames(ButtonDisplayMode.values(), ButtonState.DisplayMode.values(), ButtonState.DisplayMode::valueOf);
	}

	/**
	 * The appearances are sent as the schema spells them; the default is not sent.
	 */
	public void testButtonAppearance() {
		assertSameNames(withoutDefault(ButtonAppearance.values(), ButtonAppearance.DEFAULT),
			ButtonState.Appearance.values(), ButtonState.Appearance::valueOf);
	}

	/**
	 * The tones are sent as the schema spells them; the default is not sent.
	 */
	public void testButtonTone() {
		assertSameNames(withoutDefault(ButtonTone.values(), ButtonTone.DEFAULT), ButtonState.Tone.values(),
			ButtonState.Tone::valueOf);
	}

	/**
	 * The sizes are sent as the schema spells them; the default is not sent.
	 */
	public void testButtonSize() {
		assertSameNames(withoutDefault(ButtonSize.values(), ButtonSize.DEFAULT), ButtonState.Size.values(),
			ButtonState.Size::valueOf);
	}

	/**
	 * The shapes of a checkbox are sent as the schema spells them.
	 */
	public void testCheckboxDisplay() {
		assertSameNames(new BooleanPresentation[] { BooleanPresentation.CHECKBOX, BooleanPresentation.SWITCH },
			CheckboxState.Display.values(), CheckboxState.Display::valueOf);
	}

	private static <E extends Enum<E> & ExternallyNamed> List<E> withoutDefault(E[] values, E defaultValue) {
		List<E> result = new ArrayList<>(Arrays.asList(values));
		result.remove(defaultValue);
		return result;
	}

	private static <E extends Enum<E> & ExternallyNamed, P extends Enum<P> & ProtocolEnum> void assertSameNames(
			E[] javaValues, P[] protocolValues, Function<String, P> byName) {
		assertSameNames(Arrays.asList(javaValues), protocolValues, byName);
	}

	private static <E extends Enum<E> & ExternallyNamed, P extends Enum<P> & ProtocolEnum> void assertSameNames(
			List<E> javaValues, P[] protocolValues, Function<String, P> byName) {
		assertEquals("Number of values sent.", javaValues.size(), protocolValues.length);
		for (E value : javaValues) {
			P protocolValue = byName.apply(value.name());
			assertEquals("External name of " + value, protocolValue.protocolName(), value.getExternalName());
		}
	}

	/**
	 * The names of the properties declared by the given state message, including the inherited ones.
	 */
	private static Set<String> properties(Class<? extends ControlState> message) {
		Set<String> result = new HashSet<>();
		for (Field field : message.getFields()) {
			if (Modifier.isStatic(field.getModifiers()) && field.getName().endsWith("__PROP")) {
				try {
					result.add((String) field.get(null));
				} catch (IllegalAccessException ex) {
					throw new AssertionError(ex);
				}
			}
		}
		return result;
	}

}
