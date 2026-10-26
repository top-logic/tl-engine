/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.ReactResourceCellControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ControlState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.element.ResourceDisplayByExpression;
import com.top_logic.model.TLModule;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link ResourceDisplayByExpression}: the parts of the display its expressions compute,
 * the object's own parts where it computes none, and the state its control sends.
 */
public class TestResourceDisplayByExpression extends BasicTestCase {

	/** The tag a display is read from. */
	private static final String DISPLAY = "display";

	/** An icon an expression computes, as an icon resource. */
	private static final String CAR = "css:fas fa-car-side";

	/** State key of the label of a {@link ReactResourceCellControl}. */
	private static final String LABEL_STATE = "label";

	/** State key of the CSS icon of a {@link ReactResourceCellControl}. */
	private static final String ICON_CSS_STATE = "iconCss";

	/** The object displayed. */
	private TLModule _module;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_module = ModelService.getApplicationModel().getModule("tl.core");
	}

	@Override
	protected void tearDown() throws Exception {
		_module = null;
		super.tearDown();
	}

	/** Each expression computes its part of the display from the displayed object. */
	public void testComputedParts() throws Exception {
		ResourceDisplayByExpression display = display(
			"label=\"'L:' + $value\" icon=\"'" + CAR + "'\" css-class=\"['a', 'b']\" tooltip=\"'&lt;b>' + $value\"");
		String own = MetaResourceProvider.INSTANCE.getLabel(_module);

		assertEquals("L:" + own, display.getLabel(_module));
		assertEquals(CAR, display.getImage(_module, Flavor.DEFAULT).toEncodedForm());
		assertEquals("a b", display.getCssClass(_module));
		assertEquals("The tooltip text is written as HTML.", "&lt;b&gt;" + own, display.getTooltip(_module));
	}

	/** An internationalized label is shown in the language of the user. */
	public void testInternationalizedLabel() throws Exception {
		ResourceDisplayByExpression display = display("label=\"#('Device'@en, 'Gerät'@de)\"");

		String label = display.getLabel(_module);
		assertTrue(label, label.equals("Device") || label.equals("Gerät"));
	}

	/** A display computing nothing shows the object as its type registers it. */
	public void testUnsetPartsFallBackToTheObject() throws Exception {
		ResourceDisplayByExpression display = display("");
		MetaResourceProvider own = MetaResourceProvider.INSTANCE;

		assertEquals(own.getLabel(_module), display.getLabel(_module));
		assertEquals(own.getImage(_module, Flavor.DEFAULT), display.getImage(_module, Flavor.DEFAULT));
		assertEquals(own.getCssClass(_module), display.getCssClass(_module));
		assertEquals(own.getTooltip(_module), display.getTooltip(_module));
		assertEquals(own.getType(_module), display.getType(_module));
	}

	/**
	 * A label or an icon computed as nothing is the object's own, a class or a tooltip computed as
	 * nothing is none.
	 */
	public void testNothingComputed() throws Exception {
		ResourceDisplayByExpression display =
			display("label='null' icon='null' css-class='[]' tooltip='null'");
		MetaResourceProvider own = MetaResourceProvider.INSTANCE;

		assertEquals(own.getLabel(_module), display.getLabel(_module));
		assertEquals(own.getImage(_module, Flavor.DEFAULT), display.getImage(_module, Flavor.DEFAULT));
		assertNull(display.getCssClass(_module));
		assertNull(display.getTooltip(_module));
	}

	/**
	 * The control of the display sends the computed parts, and takes back a class and an icon the
	 * object it shows next does not have.
	 */
	public void testControlState() throws Exception {
		ResourceDisplayByExpression display = display("label=\"'L:' + $value\""
			+ " icon=\"$value == 'a' ? '" + CAR + "' : null\""
			+ " css-class=\"$value == 'a' ? 'mark' : null\"");
		ReactContext context =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		ReactControl control = display.createControl(context, "a");
		assertTrue(control instanceof ReactResourceCellControl);

		Map<?, ?> state = state(control);
		assertEquals("L:a", state.get(LABEL_STATE));
		assertEquals("mark", state.get(ControlState.CSS_CLASS__PROP));
		assertEquals(CAR.substring("css:".length()), state.get(ICON_CSS_STATE));

		((ReactResourceCellControl) control).update("b");
		state = state(control);
		assertEquals("L:b", state.get(LABEL_STATE));
		assertNull("The class of the earlier value is taken back.", state.get(ControlState.CSS_CLASS__PROP));
		assertNull("The icon of the earlier value is taken back.", state.get(ICON_CSS_STATE));
	}

	/** A display does not lead away from where it is shown unless it says so. */
	public void testLinkIsOffByDefault() {
		ResourceDisplayByExpression.Config config =
			TypedConfiguration.newConfigItem(ResourceDisplayByExpression.Config.class);
		assertFalse(config.getLink());
		assertTrue(config.getImage());
	}

	/** The state of the given control as the client receives it. */
	private static Map<?, ?> state(ReactControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + json, ex);
		}
	}

	/** The display read from a {@value #DISPLAY} tag with the given attributes. */
	private static ResourceDisplayByExpression display(String attributes) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestResourceDisplayByExpression.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(DISPLAY,
			TypedConfiguration.getConfigurationDescriptor(ResourceDisplayByExpression.Config.class)));
		reader.setSource(CharacterContents.newContent("<" + DISPLAY + " " + attributes + "/>"));
		ResourceDisplayByExpression.Config config = (ResourceDisplayByExpression.Config) reader.read();
		ResourceDisplayByExpression result = context.getInstance(config);
		context.checkErrors();
		return result;
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The expressions are compiled and evaluated against the application model, and an
	 *           object shows its own parts as the {@link LabelProviderService} registers them.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestResourceDisplayByExpression.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE));
	}

}
