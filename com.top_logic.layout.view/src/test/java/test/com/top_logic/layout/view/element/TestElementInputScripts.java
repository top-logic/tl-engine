/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.List;

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
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.values.edit.DefaultValueModel;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputRefFormat;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.element.AbstractTableElement.CriterionConfig;
import com.top_logic.layout.view.element.AbstractTableElement.PresetConfig;
import com.top_logic.layout.view.element.AlertElement;
import com.top_logic.layout.view.element.ProgressElement;
import com.top_logic.layout.view.element.ScrollLinkElement;
import com.top_logic.layout.view.element.SwitchElement;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.element.VisibleIfElement;
import com.top_logic.layout.view.list.ObjectListElement;
import com.top_logic.layout.view.table.FilterStateConfig;
import com.top_logic.layout.view.table.TableExportCommand;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.ui.ScriptContextVariablesResolver;
import com.top_logic.util.model.ModelService;

/**
 * Tests the scripts of elements that see the inputs of their element as variables: the editor
 * offers the names the runtime compiles the scripts against, also for a script declared on a part
 * of the element, and a script declaring a parameter is a configuration error.
 */
public class TestElementInputScripts extends BasicTestCase {

	private static final String TABLE = "table";

	private static final String SWITCH = "switch";

	private static final String VISIBLE_IF = "visible-if";

	private static final String PROGRESS = "progress";

	private static final String SCROLL_LINK = "scroll-link";

	private static final String OBJECT_LIST = "object-list";

	private static final String ALERT = "alert";

	/**
	 * Tests the variables of the scripts of a table: its rows, a preset criterion, a criterion
	 * written in the form of a filter, and the download name of its export, which see the inputs of
	 * the table - not the (empty) inputs of the export command itself.
	 */
	public void testTableScripts() throws Exception {
		TableElement.Config table = (TableElement.Config) parse(TABLE,
			"<table inputs='a, b as second' rows='$a'>"
				+ "<presets><preset name='p'>"
				+ "<criterion column='x' expr='$second'/>"
				+ "<criterion column='y'><options selected='$a'/></criterion>"
				+ "</preset></presets>"
				+ "<export download-name='$second'/>"
				+ "</table>");
		List<String> inputs = List.of("a", "second");

		assertScope(inputs, table, TableElement.Config.ROWS);

		PresetConfig preset = table.getPresets().getPresets().get(0);
		CriterionConfig byValue = preset.getCriteria().get(0);
		assertScope(inputs, byValue, CriterionConfig.EXPR);

		FilterStateConfig.OptionsConfig byForm =
			(FilterStateConfig.OptionsConfig) preset.getCriteria().get(1).getStates().get(0);
		assertScope(inputs, byForm, FilterStateConfig.OptionsConfig.SELECTED);

		TableExportCommand.Config export = table.getExport();
		assertScope(inputs, export, TableExportCommand.Config.DOWNLOAD_NAME);
	}

	/**
	 * Tests that the scripts of an element with a single input see it by the channel name, or by
	 * the name given with {@code as}, also the test of a case declared within the element.
	 */
	public void testSingleInputScripts() throws Exception {
		SwitchElement.Config switchConfig = (SwitchElement.Config) parse(SWITCH,
			"<switch input='selectedDemo as demo'><case test='$demo != null'/></switch>");
		assertScope(List.of("demo"), switchConfig.getCases().get(0), SwitchElement.CaseConfig.TEST);

		VisibleIfElement.Config visibleIf =
			(VisibleIfElement.Config) parse(VISIBLE_IF, "<visible-if input='ticket' expr='$ticket != null'/>");
		assertScope(List.of("ticket"), visibleIf, VisibleIfElement.Config.EXPR);

		ScrollLinkElement.Config scrollLink =
			(ScrollLinkElement.Config) parse(SCROLL_LINK, "<scroll-link input='cited as comment' label='$comment'/>");
		assertScope(List.of("comment"), scrollLink, ScrollLinkElement.Config.LABEL);

		ProgressElement.Config withoutInput =
			(ProgressElement.Config) parse(PROGRESS, "<progress done='1' total='2'/>");
		assertScope(List.of(), withoutInput, ProgressElement.Config.DONE);
	}

	/**
	 * Tests the variables of the scripts of elements reading a list of inputs.
	 */
	public void testInputsScripts() throws Exception {
		ObjectListElement.Config list = (ObjectListElement.Config) parse(OBJECT_LIST,
			"<object-list inputs='catalogue, season as when' items='[$catalogue, $when]'>"
				+ "<item><text input='element'/></item></object-list>");
		assertScope(List.of("catalogue", "when"), list, ObjectListElement.Config.ITEMS);

		AlertElement.Config alert = (AlertElement.Config) parse(ALERT,
			"<alert inputs='state as s' visible-if='$s' message-expr='$s'/>");
		assertScope(List.of("s"), alert, AlertElement.Config.VISIBLE_IF);
		assertScope(List.of("s"), alert, AlertElement.Config.MESSAGE_EXPR);
	}

	/**
	 * Tests that the single input reads the {@code as} name.
	 */
	public void testInputRefFormat() throws Exception {
		InputRef renamed = InputRefFormat.INSTANCE.getValue("input", "selection as ticket");
		assertEquals("selection", renamed.getChannel().getChannelName());
		assertEquals("ticket", renamed.getName());

		InputRef plain = InputRefFormat.INSTANCE.getValue("input", "selection");
		assertEquals("selection", plain.getName());

		assertEquals("selection as ticket", InputRefFormat.INSTANCE.getSpecification(renamed));
	}

	/**
	 * Tests that an element script declaring a parameter is a configuration error.
	 */
	public void testLambdaHeaderIsAnError() throws Exception {
		assertInstantiationFails(VISIBLE_IF, "<visible-if input='state as s' expr='x -> $x'/>", "$s");
		assertInstantiationFails(SWITCH, "<switch input='state'><case test='x -> $x'/></switch>", "$state");
		assertInstantiationFails(ALERT, "<alert inputs='a' message-expr='x -> $x'/>", "$a");
	}

	/**
	 * Tests that a script of an element sees the declared names at runtime.
	 */
	public void testRuntimeCompilesAgainstTheNames() throws Exception {
		SwitchElement.Config switchConfig = (SwitchElement.Config) parse(SWITCH,
			"<switch input='state as s'><case test=\"$s == 'on'\"/></switch>");
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestElementInputScripts.class);
		instantiation.getInstance(switchConfig);
		instantiation.checkErrors();

		assertEquals(Boolean.TRUE, InputScript.compile(switchConfig.getCases().get(0), SwitchElement.CaseConfig.TEST)
			.execute("on"));
	}

	private static void assertScope(List<String> expected, ConfigurationItem config, String property) {
		assertEquals("Editor scope of '" + property + "'.", expected, ScriptContextVariablesResolver
			.resolve(new DefaultValueModel(config, config.descriptor().getProperty(property))));
		assertEquals("Runtime scope of '" + property + "'.", expected,
			InputScript.variables(config, config.descriptor().getProperty(property)));
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
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestElementInputScripts.class);
		ConfigurationReader reader =
			new ConfigurationReader(context, Collections.singletonMap(tag, descriptor(tag)));
		reader.setSource(CharacterContents.newContent(xml));
		ConfigurationItem result = reader.read();
		context.checkErrors();
		return result;
	}

	private static ConfigurationDescriptor descriptor(String tag) {
		switch (tag) {
			case TABLE:
				return TypedConfiguration.getConfigurationDescriptor(TableElement.Config.class);
			case SWITCH:
				return TypedConfiguration.getConfigurationDescriptor(SwitchElement.Config.class);
			case VISIBLE_IF:
				return TypedConfiguration.getConfigurationDescriptor(VisibleIfElement.Config.class);
			case PROGRESS:
				return TypedConfiguration.getConfigurationDescriptor(ProgressElement.Config.class);
			case SCROLL_LINK:
				return TypedConfiguration.getConfigurationDescriptor(ScrollLinkElement.Config.class);
			case OBJECT_LIST:
				return TypedConfiguration.getConfigurationDescriptor(ObjectListElement.Config.class);
			case ALERT:
				return TypedConfiguration.getConfigurationDescriptor(AlertElement.Config.class);
			default:
				throw new IllegalArgumentException("No tag: " + tag);
		}
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The scripts are compiled against the application model and the knowledge base.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestElementInputScripts.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
