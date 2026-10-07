/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.DropdownSelectState;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.view.form.BooleanControlProvider;
import com.top_logic.model.annotate.ui.BooleanDisplay;
import com.top_logic.model.annotate.ui.BooleanPresentation;

/**
 * Tests for {@link BooleanControlProvider} - the shape a truth value is displayed in, taken from the
 * field or overridden by the configuration of the provider.
 */
public class TestBooleanControlProvider extends TestCase {

	/** Label of the built field, which decides no display. */
	private static final String LABEL = "Active";

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;

		super.tearDown();
	}

	/** A configuration stating no display leaves the field to say how it is displayed. */
	public void testTheFieldDecidesWhereTheConfigurationSaysNothing() {
		assertEquals(BooleanPresentation.CHECKBOX,
			presentationOf(provider(null), BooleanPresentation.CHECKBOX, false));
		assertEquals(BooleanPresentation.SWITCH,
			presentationOf(provider(null), BooleanPresentation.SWITCH, false));
	}

	/** A configured display overrides what the field says, so one form field deviates. */
	public void testTheConfiguredDisplayOverridesTheField() {
		assertEquals(BooleanPresentation.SWITCH,
			presentationOf(provider(BooleanPresentation.SWITCH), BooleanPresentation.CHECKBOX, false));
		assertEquals(BooleanPresentation.CHECKBOX,
			presentationOf(provider(BooleanPresentation.CHECKBOX), BooleanPresentation.SWITCH, false));
	}

	/**
	 * A value that may also be unknown stays a box that is ticked: a switch has no third position
	 * to show "no value" in.
	 */
	public void testATriStateValueIsNoSwitch() {
		assertEquals(BooleanPresentation.CHECKBOX,
			presentationOf(provider(BooleanPresentation.SWITCH), BooleanPresentation.SWITCH, true));
	}

	/**
	 * Radio buttons are a select field choosing between yes and no, its options side by side; a
	 * two-valued field offers no choice of no value.
	 */
	public void testRadioButtonsAreASelectField() {
		ReactDropdownSelectControl control = select(provider(null), BooleanPresentation.RADIO, false);

		assertEquals(SelectDisplay.RADIO, control.getDisplay());
		assertEquals(Orientation.HORIZONTAL, control.getOrientation());
		assertEquals(Boolean.TRUE, state(control).get(FieldState.MANDATORY__PROP));
	}

	/**
	 * A select is a list that opens on demand without an input to filter its two values by; a
	 * tri-state field offers the choice of no value.
	 */
	public void testASelectIsAListWithoutFilter() {
		ReactDropdownSelectControl control =
			select(provider(BooleanPresentation.SELECT), BooleanPresentation.CHECKBOX, true);

		assertEquals(SelectDisplay.DROPDOWN, control.getDisplay());
		assertFalse(control.hasFilter());
		assertEquals(Boolean.FALSE, state(control).get(FieldState.MANDATORY__PROP));
	}

	/** The options are yes and no, labelled as a truth value is labelled everywhere. */
	public void testTheOptionsAreYesAndNo() {
		ReactDropdownSelectControl control = select(provider(null), BooleanPresentation.RADIO, true);

		List<?> options = (List<?>) state(control).get(DropdownSelectState.OPTIONS__PROP);
		assertEquals(2, options.size());
		assertEquals(MetaLabelProvider.INSTANCE.getLabel(Boolean.TRUE),
			((Map<?, ?>) options.get(0)).get(DropdownSelectState.Option.LABEL__PROP));
		assertEquals(MetaLabelProvider.INSTANCE.getLabel(Boolean.FALSE),
			((Map<?, ?>) options.get(1)).get(DropdownSelectState.Option.LABEL__PROP));
	}

	/** The select control the given provider builds for a boolean field displayed as stated. */
	private ReactDropdownSelectControl select(BooleanControlProvider provider, BooleanPresentation fieldDisplay,
			boolean triState) {
		ReactControl control = control(provider, fieldDisplay, triState);
		assertTrue("A choice between yes and no is a select control, but is shown by " + control.getClass(),
			control instanceof ReactDropdownSelectControl);
		return (ReactDropdownSelectControl) control;
	}

	/** The state the given control would send to the client. */
	private static Map<?, ?> state(ReactControl control) {
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	/** The display is configured by the name the switch is known by in a view. */
	public void testTheConfiguredDisplayIsReadFromItsName() throws Exception {
		BooleanControlProvider.Config config = readConfig(
			"<input-control " + BooleanControlProvider.Config.DISPLAY + "='"
				+ BooleanPresentation.SWITCH.getExternalName() + "'/>");

		assertEquals(BooleanPresentation.SWITCH, config.getDisplay());
	}

	/** The same name asks for the switch in the model annotation of an attribute. */
	public void testTheAnnotatedDisplayIsReadFromItsName() throws Exception {
		BooleanDisplay annotation = (BooleanDisplay) read(BooleanDisplay.class,
			"<boolean-display presentation='" + BooleanPresentation.SWITCH.getExternalName() + "'/>");

		assertEquals(BooleanPresentation.SWITCH, annotation.getPresentation());
	}

	/** The shape the given provider displays the described value in. */
	private BooleanPresentation presentationOf(BooleanControlProvider provider, BooleanPresentation fieldDisplay,
			boolean triState) {
		ReactControl control = control(provider, fieldDisplay, triState);

		assertTrue("A truth value shown in place is a checkbox control, but is shown by " + control.getClass(),
			control instanceof ReactCheckboxControl);
		return ((ReactCheckboxControl) control).getPresentation();
	}

	/** The control the given provider builds for a boolean field displayed as stated. */
	private ReactControl control(BooleanControlProvider provider, BooleanPresentation fieldDisplay,
			boolean triState) {
		FieldSpec field = FieldSpec.of(Boolean.class, LABEL)
			.setBooleanPresentation(fieldDisplay)
			.setTriState(triState);
		return provider.createControl(_context, field, new AbstractFieldModel(null));
	}

	/** A provider configured for the given display, or for none if {@code null} is given. */
	private static BooleanControlProvider provider(BooleanPresentation display) {
		BooleanControlProvider.Config config =
			TypedConfiguration.newConfigItem(BooleanControlProvider.Config.class);
		if (display != null) {
			config.update(config.descriptor().getProperty(BooleanControlProvider.Config.DISPLAY), display);
		}
		return (BooleanControlProvider) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(config);
	}

	/** The provider configuration the given XML declares. */
	private static BooleanControlProvider.Config readConfig(String xml) throws Exception {
		return (BooleanControlProvider.Config) read(BooleanControlProvider.Config.class, xml);
	}

	/** The configuration of the given kind the given XML declares. */
	private static Object read(Class<?> configType, String xml) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestBooleanControlProvider.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			rootTag(xml), TypedConfiguration.getConfigurationDescriptor(configType));

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		Object result = reader.read();
		context.checkErrors();
		return result;
	}

	/** The name of the root element of the given XML. */
	private static String rootTag(String xml) {
		int end = xml.indexOf(' ');
		return xml.substring(1, end);
	}

	/** Suite loading the application configuration the read configurations are checked against. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestBooleanControlProvider.class);
	}

}
