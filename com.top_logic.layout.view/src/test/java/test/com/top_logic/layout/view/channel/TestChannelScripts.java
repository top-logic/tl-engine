/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.values.edit.DefaultValueModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelConfig;
import com.top_logic.layout.view.channel.ChannelFactory;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DerivedChannelConfig;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.channel.ValueChannelConfig;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.search.ui.ScriptContextVariablesResolver;
import com.top_logic.util.model.ModelService;

/**
 * Tests the scripts of channels, which see the inputs of the channel as variables named after
 * their channels or by their {@code as} name, and which must not declare parameters.
 */
public class TestChannelScripts extends BasicTestCase {

	private static final String DERIVED = "derived-channel";

	private static final String CHANNEL = "channel";

	private static final String GENERIC_COMMAND = "generic-command";

	private ViewContext _context;

	private ViewChannel _a;

	private ViewChannel _b;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_a = new DefaultViewChannel("a");
		_b = new DefaultViewChannel("b");
		_context.registerChannel("a", _a);
		_context.registerChannel("b", _b);
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_a = null;
		_b = null;
		super.tearDown();
	}

	/**
	 * Tests that the expression of a derived channel sees its inputs by channel name and by
	 * {@code as} name, and is computed again when an input changes.
	 */
	public void testDerivedExpressionSeesNamedInputs() throws Exception {
		_a.set(1);
		_b.set(2);
		ViewChannel derived = createChannel(
			"<derived-channel name='sum' inputs='a, b as second' expr='$a * 10 + $second'/>");

		assertEquals(12, ((Number) derived.get()).intValue());

		_b.set(5);
		assertEquals(15, ((Number) derived.get()).intValue());
	}

	/**
	 * Tests that the reverse expression of a derived channel sees the written value as
	 * {@value DerivedChannelConfig#VALUE} and the inputs by name.
	 */
	public void testReverseSeesValueAndInputs() throws Exception {
		_a.set(7);
		_b.set(3);
		ViewChannel derived = createChannel(
			"<derived-channel name='shifted' inputs='a, b as offset' expr='$a + $offset'"
				+ " reverse='$" + DerivedChannelConfig.VALUE + " - $offset'/>");
		assertEquals(10, ((Number) derived.get()).intValue());

		derived.set(20);

		assertEquals(17, ((Number) _a.get()).intValue());
		assertEquals(20, ((Number) derived.get()).intValue());
	}

	/**
	 * Tests that the initialization function of a value channel sees its inputs by name.
	 */
	public void testInitSeesNamedInputs() throws Exception {
		_a.set(4);
		ViewChannel channel = createChannel("<channel name='doubled' inputs='a as p' init='$p * 2'/>");

		assertEquals(8, ((Number) channel.get()).intValue());

		_a.set(5);
		assertEquals(10, ((Number) channel.get()).intValue());
	}

	/**
	 * Tests that a channel script declaring a parameter is a configuration error.
	 */
	public void testLambdaHeaderIsAnError() throws Exception {
		assertInstantiationFails(DERIVED, "<derived-channel name='x' inputs='a, b as second' expr='v -> $v'/>",
			"$a, $second");
		assertInstantiationFails(DERIVED,
			"<derived-channel name='x' inputs='a' expr='$a' reverse='v -> $v'/>", "$a, $value");
		assertInstantiationFails(CHANNEL, "<channel name='x' inputs='a as p' init='v -> $v'/>", "$p");
	}

	/**
	 * Tests that a script without any variable that declares a parameter is a configuration error
	 * as well, since it would yield a function.
	 */
	public void testLambdaHeaderWithoutVariablesIsAnError() throws Exception {
		assertInstantiationFails(CHANNEL, "<channel name='x' init='v -> 10'/>", "\"v\"");
	}

	/**
	 * Tests that a lambda as an argument within a script is allowed.
	 */
	public void testInnerLambdaIsAllowed() throws Exception {
		_a.set(List.of(1, 2, 3));
		ViewChannel derived = createChannel(
			"<derived-channel name='count' inputs='a' expr='$a.filter(x -> $x > 1).size()'/>");

		assertEquals(2, ((Number) derived.get()).intValue());
	}

	/**
	 * Tests that a script of an action chain declaring a parameter is a configuration error.
	 */
	public void testLambdaHeaderInChainIsAnError() throws Exception {
		assertInstantiationFails(GENERIC_COMMAND,
			"<generic-command inputs='a'><execute-script function='v -> $v'/></generic-command>", "$a");
	}

	/**
	 * Tests that the script editor offers the variables the runtime evaluates the channel scripts
	 * with.
	 */
	public void testEditorScope() throws Exception {
		DerivedChannelConfig derived = (DerivedChannelConfig) parse(DERIVED,
			"<derived-channel name='x' inputs='a, b as second' expr='$a' reverse='$value'/>");

		assertEquals(List.of("a", "second"), editorVariables(derived, DerivedChannelConfig.EXPR));
		assertEquals(List.of("a", "second", DerivedChannelConfig.VALUE),
			editorVariables(derived, DerivedChannelConfig.REVERSE));

		ValueChannelConfig value = (ValueChannelConfig) parse(CHANNEL, "<channel name='x' inputs='a as p' init='$p'/>");
		assertEquals(List.of("p"), editorVariables(value, ValueChannelConfig.INIT));

		// The runtime evaluates scripts using every offered variable.
		_a.set(1);
		_b.set(2);
		ViewChannel channel = createChannel(
			"<derived-channel name='y' inputs='a, b as second' expr='$a + $second' reverse='$value - $second'/>");
		channel.set(10);
		assertEquals(8, ((Number) _a.get()).intValue());
	}

	/**
	 * Tests that a supplied value is renamed by the property {@link Supplies#renamedBy()} of the
	 * configuration holding the script or of a configuration containing it.
	 */
	public void testSuppliedValueRenamed() throws Exception {
		Holder holder = TypedConfiguration.newConfigItem(Holder.class);
		Part part = TypedConfiguration.newConfigItem(Part.class);
		holder.getParts().add(part);

		assertEquals(List.of(Part.ROW), editorVariables(part, Part.SCRIPT));

		holder.setRowAs("ticket");
		assertEquals(List.of("ticket"), editorVariables(part, Part.SCRIPT));
		assertEquals(List.of("ticket"),
			InputScript.variables(part, part.descriptor().getProperty(Part.SCRIPT)));
	}

	private static List<String> editorVariables(ConfigurationItem config, String property) {
		return ScriptContextVariablesResolver
			.resolve(new DefaultValueModel(config, config.descriptor().getProperty(property)));
	}

	private ViewChannel createChannel(String xml) throws ConfigurationException {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestChannelScripts.class);
		ChannelConfig config = (ChannelConfig) parse(xml.startsWith("<" + DERIVED) ? DERIVED : CHANNEL, xml);
		ChannelFactory factory = instantiation.getInstance(config);
		instantiation.checkErrors();
		ViewChannel channel = factory.createChannel(_context);
		_context.registerChannel(config.getName(), channel);
		return channel;
	}

	/**
	 * Asserts that instantiating the given configuration reports an error whose text contains the
	 * given fragment.
	 */
	private static void assertInstantiationFails(String tag, String xml, String expected)
			throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(log);
		PolymorphicConfiguration<?> config = (PolymorphicConfiguration<?>) parse(tag, xml);
		instantiation.getInstance(config);
		assertTrue("A script declaring a parameter must be rejected: " + xml, log.hasErrors());
		assertTrue("The error names '" + expected + "': " + log.getError(), log.getError().contains(expected));
	}

	private static ConfigurationItem parse(String tag, String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestChannelScripts.class);
		Map<String, ConfigurationDescriptor> descriptors = Map.of(
			DERIVED, TypedConfiguration.getConfigurationDescriptor(DerivedChannelConfig.class),
			CHANNEL, TypedConfiguration.getConfigurationDescriptor(ValueChannelConfig.class),
			GENERIC_COMMAND, TypedConfiguration.getConfigurationDescriptor(GenericViewCommand.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(tag, descriptors.get(tag)));
		reader.setSource(CharacterContents.newContent(xml));
		ConfigurationItem result = reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * A configuration renaming the value its parts are evaluated for.
	 */
	public interface Holder extends ConfigurationItem {

		/** Configuration name for {@link #getRowAs()}. */
		String ROW_AS = "row-as";

		/** Configuration name for {@link #getParts()}. */
		String PARTS = "parts";

		/**
		 * The name of the value the scripts of the parts are evaluated for.
		 */
		@Name(ROW_AS)
		String getRowAs();

		/** @see #getRowAs() */
		void setRowAs(String value);

		/**
		 * The parts with scripts.
		 */
		@Name(PARTS)
		List<Part> getParts();
	}

	/**
	 * A configuration with a script over a supplied value that its container may rename.
	 */
	public interface Part extends Inputs, ConfigPart {

		/** Configuration name for {@link #getScript()}. */
		String SCRIPT = "script";

		/** Default name of the supplied value. */
		String ROW = "row";

		/**
		 * A script over the supplied value.
		 */
		@Name(SCRIPT)
		@Supplies(value = ROW, renamedBy = Holder.ROW_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getScript();
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The scripts are compiled and evaluated against the application model and the
	 *           knowledge base.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestChannelScripts.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
