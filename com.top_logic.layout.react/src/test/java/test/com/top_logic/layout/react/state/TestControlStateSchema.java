/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.state;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonAppearance;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.ButtonSize;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.layout.react.control.button.KeyStroke;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.form.InputType;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl.Kind;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactPasswordInputControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.control.overlay.ReactDialogControl;
import com.top_logic.layout.react.control.overlay.ReactMenuControl;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl.Variant;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.control.tabbar.TabDefinition;
import com.top_logic.layout.react.control.toggle.ReactToggleButtonControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ButtonState;
import com.top_logic.layout.react.state.CheckboxState;
import com.top_logic.layout.react.state.ChildControl;
import com.top_logic.layout.react.state.DatePickerState;
import com.top_logic.layout.react.state.DialogState;
import com.top_logic.layout.react.state.DropdownSelectState;
import com.top_logic.layout.react.state.MenuState;
import com.top_logic.layout.react.state.NumberInputState;
import com.top_logic.layout.react.state.PasswordInputState;
import com.top_logic.layout.react.state.SelectState;
import com.top_logic.layout.react.state.SnackbarState;
import com.top_logic.layout.react.state.TabBarState;
import com.top_logic.layout.react.state.TextInputState;
import com.top_logic.layout.react.state.ToggleButtonState;
import com.top_logic.layout.react.state.WindowState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.annotate.ui.BooleanPresentation;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

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

	/**
	 * The kinds of a text field are sent as the schema spells them.
	 */
	public void testTextInputType() {
		assertSameNames(Arrays.asList(InputType.values()), InputType::htmlType, TextInputState.InputType.values(),
			TextInputState.InputType::valueOf);
	}

	/**
	 * The kinds of a date field are sent as the schema spells them.
	 */
	public void testDatePickerInputType() {
		assertSameNames(Arrays.asList(Kind.values()), Kind::inputType, DatePickerState.InputType.values(),
			DatePickerState.InputType::valueOf);
	}

	/**
	 * The shapes of a select field are sent as the schema spells them.
	 */
	public void testDropdownSelectDisplay() {
		assertSameNames(SelectDisplay.values(), DropdownSelectState.Display.values(),
			DropdownSelectState.Display::valueOf);
	}

	/**
	 * The kinds of a snackbar message are sent as the schema spells them.
	 */
	public void testSnackbarVariant() {
		assertSameNames(Variant.values(), SnackbarState.Variant.values(), SnackbarState.Variant::valueOf);
	}

	/**
	 * Every key of a text field is declared in {@link TextInputState}.
	 */
	public void testTextInputKeys() {
		ReactTextInputControl control = new ReactTextInputControl(createContext(), new AbstractFieldModel("text"));
		putFieldKeys(control);
		control.setInputType(InputType.URL);
		control.setIcon("css:fas fa-search");
		control.setClearable(true);
		control.setMultiline(3);
		control.setDebounce(Long.valueOf(100));
		control.setSendValueOnBlur(true);

		assertDeclared(state(control), TextInputState.class);
	}

	/**
	 * Every key of a password field is declared in {@link PasswordInputState}.
	 */
	public void testPasswordInputKeys() {
		ReactPasswordInputControl control =
			new ReactPasswordInputControl(createContext(), new AbstractFieldModel("secret"));
		putFieldKeys(control);
		control.setDebounce(Long.valueOf(100));

		assertDeclared(state(control), PasswordInputState.class);
	}

	/**
	 * Every key of a number field is declared in {@link NumberInputState}.
	 */
	public void testNumberInputKeys() {
		ReactNumberInputControl control = new ReactNumberInputControl(createContext(),
			new AbstractFieldModel(Double.valueOf(12.5)), NumberFormat.getInstance(Locale.GERMAN));
		putFieldKeys(control);

		Map<?, ?> state = state(control);
		assertDeclared(state, NumberInputState.class);
		assertEquals(NumberInputState.InputMode.DECIMAL.protocolName(), state.get(NumberInputState.INPUT_MODE__PROP));
	}

	/**
	 * Every key of a date field is declared in {@link DatePickerState}.
	 */
	public void testDatePickerKeys() {
		ReactDatePickerControl control =
			new ReactDatePickerControl(createContext(), new AbstractFieldModel(new Date()), Kind.DATE_TIME);
		putFieldKeys(control);

		assertDeclared(state(control), DatePickerState.class);
	}

	/**
	 * Every key of a select field and of its options is declared in {@link SelectState}.
	 */
	public void testSelectKeys() {
		ReactSelectFormFieldControl control = new ReactSelectFormFieldControl(createContext(),
			new SimpleSelectFieldModel("b", List.of("a", "b"), false), String::valueOf);
		putFieldKeys(control);

		Map<?, ?> state = state(control);
		assertDeclared(state, SelectState.class);
		assertEachDeclared(state.get(SelectState.OPTIONS__PROP), SelectState.Option.class);
	}

	/**
	 * Every key of a field choosing objects, of its options and of its value is declared in
	 * {@link DropdownSelectState}.
	 */
	public void testDropdownSelectKeys() {
		ReactDropdownSelectControl control = new ReactDropdownSelectControl(createContext(),
			new SimpleSelectFieldModel(List.of("b"), List.of("a", "b"), true), String::valueOf, null, false,
			SelectDisplay.CHIPS);
		putFieldKeys(control);

		Map<?, ?> state = state(control);
		assertDeclared(state, DropdownSelectState.class);
		assertEachDeclared(state.get(DropdownSelectState.OPTIONS__PROP), DropdownSelectState.Option.class);
		assertEachDeclared(state.get(DropdownSelectState.VALUE__PROP), DropdownSelectState.Option.class);
		assertTrue(state.containsKey(DropdownSelectState.OPTIONS__PROP));
	}

	/**
	 * Every key of a toggle button is declared in {@link ToggleButtonState}.
	 */
	public void testToggleButtonKeys() {
		assertDeclared(state(toggle()), ToggleButtonState.class);
	}

	/**
	 * Every key of a tab bar and of its tabs is declared in {@link TabBarState}.
	 */
	public void testTabBarKeys() {
		ReactTabBarControl control = new ReactTabBarControl(createContext(), null, List.of(
			new TabDefinition("a", "A", this::toggle, null).withIcon("css:fas fa-home"),
			new TabDefinition("b", "B", this::toggle, null)));
		// Creates the content of the active tab.
		control.attach();

		Map<?, ?> state = state(control);
		assertDeclared(state, TabBarState.class);
		assertEachDeclared(state.get(TabBarState.TABS__PROP), TabBarState.Tab.class);
		assertChild(state.get(TabBarState.ACTIVE_CONTENT__PROP));
	}

	/**
	 * Every key of a window is declared in {@link WindowState}.
	 */
	public void testWindowKeys() {
		ReactContext context = createContext();
		ReactWindowControl control = new ReactWindowControl(context, "Title", DisplayDimension.px(400), () -> {
			// Never closed.
		});
		control.setHeight(DisplayDimension.px(300));
		control.setResizable(true);
		control.setClosable(false);
		control.setChild(toggle());
		control.setToolbar(new ReactToolbarControl(context));
		control.setActions(List.of(toggle()));
		control.setCssClass("css");

		Map<?, ?> state = state(control);
		assertDeclared(state, WindowState.class);
		assertChild(state.get(WindowState.CHILD__PROP));
		assertChild(state.get(WindowState.TOOLBAR__PROP));
		assertChild(state.get(WindowState.FOOTER__PROP));
	}

	/**
	 * Every key of a dialog is declared in {@link DialogState}.
	 */
	public void testDialogKeys() {
		ReactDialogControl control = new ReactDialogControl(createContext(), true, () -> {
			// Never closed.
		});
		control.setChild(toggle());
		control.open();

		Map<?, ?> state = state(control);
		assertDeclared(state, DialogState.class);
		assertChild(state.get(DialogState.CHILD__PROP));
	}

	/**
	 * Every key of a menu and of its entries is declared in {@link MenuState}.
	 */
	public void testMenuKeys() {
		ReactMenuControl control = new ReactMenuControl(createContext(), null, List.of(
			MenuEntry.header("Header"),
			MenuEntry.item("a", "A", "css:fas fa-home", true, "css", true),
			MenuEntry.separator()),
			id -> {
				// Nothing to select.
			}, () -> {
				// Never closed.
			});
		control.open(3, 4);

		Map<?, ?> state = state(control);
		assertDeclared(state, MenuState.class);
		assertEachDeclared(state.get(MenuState.ITEMS__PROP), MenuState.Entry.class);
		assertEquals(List.of("header", "item", "separator"),
			((List<?>) state.get(MenuState.ITEMS__PROP)).stream()
				.map(entry -> ((Map<?, ?>) entry).get(MenuState.Entry.TYPE__PROP)).toList());
	}

	/**
	 * Every key of a snackbar is declared in {@link SnackbarState}.
	 */
	public void testSnackbarKeys() {
		ReactSnackbarControl control = new ReactSnackbarControl(createContext(), "Saved.", Variant.SUCCESS, () -> {
			// Nothing to do.
		});
		control.show();

		assertDeclared(state(control), SnackbarState.class);
	}

	private ReactToggleButtonControl toggle() {
		return new ReactToggleButtonControl(createContext(), "Toggle", true, (context, active) -> !active);
	}

	/** Puts the keys a field offers setters for. */
	private static void putFieldKeys(ReactFormFieldControl control) {
		control.setPlaceholder("placeholder");
		control.setSubmitListener(value -> {
			// Ignored.
		});
		control.setHidden(false);
		control.setCssClass("css");
	}

	private static Map<?, ?> state(ReactControl control) {
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	private static void assertDeclared(Map<?, ?> state, Class<?> message) {
		Set<Object> undeclared = new HashSet<>(state.keySet());
		undeclared.removeAll(properties(message));
		assertEquals("Keys not declared in " + message.getSimpleName() + ".", Set.of(), undeclared);
	}

	private static void assertEachDeclared(Object list, Class<?> message) {
		assertTrue("A list of " + message.getSimpleName() + ": " + list, list instanceof List<?>);
		assertFalse("Nothing to check in an empty list.", ((List<?>) list).isEmpty());
		for (Object entry : (List<?>) list) {
			assertDeclared((Map<?, ?>) entry, message);
		}
	}

	private static void assertChild(Object child) {
		assertTrue("A child control: " + child, child instanceof Map<?, ?>);
		assertDeclared((Map<?, ?>) child, ChildControl.class);
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
		assertSameNames(javaValues, ExternallyNamed::getExternalName, protocolValues, byName);
	}

	private static <E extends Enum<E>, P extends Enum<P> & ProtocolEnum> void assertSameNames(List<E> javaValues,
			Function<? super E, String> externalName, P[] protocolValues, Function<String, P> byName) {
		assertEquals("Number of values sent.", javaValues.size(), protocolValues.length);
		for (E value : javaValues) {
			P protocolValue = byName.apply(value.name());
			assertEquals("External name of " + value, protocolValue.protocolName(), externalName.apply(value));
		}
	}

	/**
	 * The names of the properties declared by the given state message, including the inherited ones.
	 */
	private static Set<String> properties(Class<?> message) {
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

	/**
	 * The suite of tests.
	 *
	 * <p>
	 * Several controls ask {@link Resources} for labels, which the services of the setup provide.
	 * </p>
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(new TestSuite(TestControlStateSchema.class), ResourcesModule.Module.INSTANCE));
	}

}
