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
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.view.form.SelectControlProvider;

/**
 * Tests for {@link SelectControlProvider} - the shape the options of a select field are offered in,
 * and what the client is handed to draw it with.
 */
public class TestSelectControlProvider extends TestCase {

	/** Label of the built field, which decides no display. */
	private static final String LABEL = "Colors";

	/** The options the fields under test are picked from. */
	private static final List<String> OPTIONS = List.of("red", "green", "blue");

	/** The state key telling the client whether it holds the option list. */
	private static final String OPTIONS_LOADED = "optionsLoaded";

	/** The state key carrying the option list. */
	private static final String OPTION_LIST = "options";

	/** The state key naming the shape the options are offered in. */
	private static final String DISPLAY = "display";

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

	/** Without a display stated, the options are offered in a list that opens on demand. */
	public void testTheOptionsAreOfferedInADropdownByDefault() {
		assertEquals(SelectDisplay.DROPDOWN, new SelectControlProvider().getDisplay());
		assertEquals(SelectDisplay.DROPDOWN, provider(null).getDisplay());
		assertEquals(SelectDisplay.DROPDOWN, control(provider(null)).getDisplay());
	}

	/** A list that opens on demand is drawn by the component that can open one. */
	public void testADropdownIsDrawnByTheDropdownComponent() {
		assertEquals("TLDropdownSelect", control(provider(null)).getReactModule());
	}

	/**
	 * A list that opens on demand is not handed its options: it asks for them the first time it is
	 * opened, so an option list that is expensive to build is built only where it is read.
	 */
	public void testADropdownIsNotHandedItsOptions() {
		ReactDropdownSelectControl control = control(provider(null));

		assertEquals(Boolean.FALSE, control.scriptingScalarState().get(OPTIONS_LOADED));
		assertNull("The list the client does not hold is not sent to it.",
			control.scriptingScalarState().get(OPTION_LIST));
	}

	/** A cloud of toggles and a bar of segments are drawn by components of their own. */
	public void testEachDisplayIsDrawnByItsOwnComponent() {
		assertEquals("TLOptionChips", control(provider(SelectDisplay.CHIPS)).getReactModule());
		assertEquals("TLSegmentedChoice", control(provider(SelectDisplay.SEGMENTED)).getReactModule());
	}

	/**
	 * A display showing every option is handed the complete list right away: it has nothing to open
	 * at which it could ask for it.
	 */
	public void testADisplayShowingEveryOptionIsHandedTheOptions() {
		assertOptionsHandedOver(control(provider(SelectDisplay.CHIPS)));
		assertOptionsHandedOver(control(provider(SelectDisplay.SEGMENTED)));
	}

	/** The client is told which shape to draw by the name the shape is configured under. */
	public void testTheDisplayReachesTheClientByItsName() {
		assertTrue("A list that opens on demand is the shape a select field has anyway.",
			!state(control(provider(null))).contains(quoted(DISPLAY)));
		assertTrue("The cloud of toggles names itself in the state.",
			state(control(provider(SelectDisplay.CHIPS)))
				.contains(quoted(DISPLAY) + ":" + quoted(SelectDisplay.CHIPS.getExternalName())));
		assertTrue("The bar of segments names itself in the state.",
			state(control(provider(SelectDisplay.SEGMENTED)))
				.contains(quoted(DISPLAY) + ":" + quoted(SelectDisplay.SEGMENTED.getExternalName())));
	}

	/**
	 * The shape is how the field looks, not what it says: it is left out of what a headless
	 * interface reads, which sees the same options and the same value in every shape.
	 */
	public void testTheDisplayIsNotPartOfWhatTheFieldSays() {
		assertNull(control(provider(SelectDisplay.CHIPS)).scriptingScalarState().get(DISPLAY));
	}

	/** The display is configured by the name each shape is known by in a view. */
	public void testTheConfiguredDisplayIsReadFromItsName() throws Exception {
		assertEquals(SelectDisplay.CHIPS, readDisplay(SelectDisplay.CHIPS));
		assertEquals(SelectDisplay.SEGMENTED, readDisplay(SelectDisplay.SEGMENTED));
		assertEquals(SelectDisplay.DROPDOWN, readDisplay(SelectDisplay.DROPDOWN));
	}

	/** A configuration stating no display offers the options in a list that opens on demand. */
	public void testTheUnconfiguredDisplayIsADropdown() throws Exception {
		SelectControlProvider.Config config = readConfig("<input-control/>");

		assertEquals(SelectDisplay.DROPDOWN, config.getDisplay());
	}

	/** Asserts that the given control holds the complete option list for its client. */
	private void assertOptionsHandedOver(ReactDropdownSelectControl control) {
		Map<String, Object> state = control.scriptingScalarState();

		assertEquals("The client is told that it holds the option list.",
			Boolean.TRUE, state.get(OPTIONS_LOADED));
		assertEquals("Every option is in the list handed over.",
			OPTIONS.size(), ((List<?>) state.get(OPTION_LIST)).size());
	}

	/** The control the given provider builds for a field picked from {@link #OPTIONS}. */
	private ReactDropdownSelectControl control(SelectControlProvider provider) {
		FieldSpec field = FieldSpec.of(String.class, LABEL);
		SimpleSelectFieldModel model = new SimpleSelectFieldModel(null, OPTIONS, false);

		ReactControl control = provider.createControl(_context, field, model);
		assertTrue("A selection is made on a select control, but is made on " + control.getClass(),
			control instanceof ReactDropdownSelectControl);
		return (ReactDropdownSelectControl) control;
	}

	/** The state the given control sends to its client. */
	private static String state(ReactDropdownSelectControl control) {
		return control.stateAsJSON();
	}

	/** The given name as it appears in the state sent to the client. */
	private static String quoted(String name) {
		return '"' + name + '"';
	}

	/** A provider configured for the given display, or for none if {@code null} is given. */
	private static SelectControlProvider provider(SelectDisplay display) {
		SelectControlProvider.Config config =
			TypedConfiguration.newConfigItem(SelectControlProvider.Config.class);
		if (display != null) {
			config.update(config.descriptor().getProperty(SelectControlProvider.Config.DISPLAY), display);
		}
		return (SelectControlProvider) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(config);
	}

	/** The display a provider configuration naming the given one declares. */
	private static SelectDisplay readDisplay(SelectDisplay display) throws Exception {
		return readConfig("<input-control " + SelectControlProvider.Config.DISPLAY + "='"
			+ display.getExternalName() + "'/>").getDisplay();
	}

	/** The provider configuration the given XML declares. */
	private static SelectControlProvider.Config readConfig(String xml) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSelectControlProvider.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			rootTag(xml), TypedConfiguration.getConfigurationDescriptor(SelectControlProvider.Config.class));

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		Object result = reader.read();
		context.checkErrors();
		return (SelectControlProvider.Config) result;
	}

	/** The name of the root element of the given XML. */
	private static String rootTag(String xml) {
		int end = xml.indexOf(' ');
		if (end < 0) {
			end = xml.indexOf('/');
		}
		return xml.substring(1, end);
	}

	/**
	 * Test suite requiring the {@link LabelProviderService} the options are labelled and imaged by,
	 * and the {@link ModelResolver} naming an option in what a headless interface reads.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestSelectControlProvider.class,
				LabelProviderService.Module.INSTANCE,
				ModelResolver.Module.INSTANCE));
	}

}
