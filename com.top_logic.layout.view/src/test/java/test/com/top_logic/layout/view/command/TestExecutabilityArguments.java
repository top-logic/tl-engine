/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import static test.com.top_logic.layout.view.command.TestConfiguredActionArguments.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.navigation.TestDisplayTargetService;
import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.values.edit.DefaultValueModel;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.CommandArguments;
import com.top_logic.layout.view.command.DisabledIf;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.InputExecutabilityConfig;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewCommands;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.command.ViewExecutabilityRules;
import com.top_logic.layout.view.command.VisibleIf;
import com.top_logic.layout.view.dnd.DropConfig;
import com.top_logic.layout.view.element.FormElement;
import com.top_logic.layout.view.element.HtmlElement;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.navigation.DisplayTargetService;
import com.top_logic.layout.view.navigation.ShowStep;
import com.top_logic.layout.view.navigation.ShowViewAction;
import com.top_logic.layout.view.table.TableDragConfig;
import com.top_logic.model.search.ui.ScriptContextVariablesResolver;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests that executability rules decide over named arguments: the rules of a command over all of
 * its arguments, re-evaluated whenever one of its inputs changes, a rule of an element over the
 * value the element supplies, and that the script editor offers the names the runtime compiles the
 * rule scripts and the scripts of a display against.
 */
public class TestExecutabilityArguments extends AbstractModelAccessTest {

	/** Name of the value a trigger supplies in these tests. */
	private static final String TRIGGER = "row";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ArgumentsProbe.reset();
	}

	@Override
	protected Person initialUser() {
		return _root;
	}

	@Override
	protected void tearDown() throws Exception {
		ArgumentsProbe.reset();
		super.tearDown();
	}

	/**
	 * Tests that a rule deciding by the second input of a command is re-evaluated when the channel
	 * of that input changes, and sees its value.
	 */
	public void testSecondInputDrivesLiveState() throws Exception {
		DefaultViewContext context = context(Map.of(SELECTION, "s"));
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestExecutabilityArguments.class);
		GenericViewCommand.Config config = TestConfiguredActionArguments.parse(instantiation,
			"<generic-command inputs='" + SELECTION + ", " + OTHER + "'>"
				+ "<executability><visible-if expr=\"$" + OTHER + " == 'o'\"/></executability>"
				+ PROBE
				+ "</generic-command>");
		GenericViewCommand command = (GenericViewCommand) instantiation.getInstance(config);
		instantiation.checkErrors();
		ViewCommandModel model = ViewCommandModel.forCommand(context, command, config);
		model.attach(null);
		int[] changes = { 0 };
		model.addStateChangeListener(() -> changes[0]++);

		assertFalse("The second input does not hold the value.", model.isVisible());

		context.resolveChannel(new ChannelRef(OTHER)).set("o");
		assertTrue("A change of the second input re-evaluates the rule.", model.isVisible());
		assertEquals(1, changes[0]);

		model.detach();
	}

	/**
	 * Tests that the state of a command run by a trigger is computed from the same arguments the
	 * per-row check and the run use: the inputs as they are, the trigger's value absent before the
	 * trigger supplies it.
	 */
	public void testLiveStateAndRowCheckAgree() throws Exception {
		DefaultViewContext context = context(Map.of(SELECTION, "s"));
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestExecutabilityArguments.class);
		GenericViewCommand.Config config = TestConfiguredActionArguments.parse(instantiation,
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<executability>"
				+ "<visible-if expr=\"$" + SELECTION + " == 's'\"/>"
				+ "<disabled-if expr=\"$" + TRIGGER + " == 'locked'\"/>"
				+ "</executability>"
				+ PROBE
				+ "</generic-command>");
		ViewCommand command = ViewCommands.instantiate(instantiation, config, TRIGGER);
		instantiation.checkErrors();
		ViewCommandModel model = ViewCommandModel.forCommand(context, command, config, TRIGGER);
		model.attach(null);

		assertTrue("The trigger supplied no value yet.", model.getExecutableState().isExecutable());
		assertTrue(model.executability("free").isExecutable());
		ExecutableState locked = model.executability("locked");
		assertTrue(locked.isVisible());
		assertFalse("The row is the trigger's argument.", locked.isExecutable());

		context.resolveChannel(new ChannelRef(SELECTION)).set("x");
		assertFalse("The live state follows the input.", model.isVisible());
		assertFalse("The row check sees the same input.", model.executability("free").isVisible());

		context.resolveChannel(new ChannelRef(SELECTION)).set("s");
		model.execute(context, "free");
		ActionArguments seen = ArgumentsProbe.SEEN.get(0);
		assertEquals(List.of(SELECTION, TRIGGER), seen.names());
		assertEquals("free", seen.get(TRIGGER));
		model.detach();
	}

	/**
	 * Tests that the script editor offers the rules of a command the names of the command's
	 * arguments, exactly those the runtime compiles the rules against.
	 */
	public void testEditorScopeOfCommandRules() throws Exception {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestExecutabilityArguments.class);
		GenericViewCommand.Config config = TestConfiguredActionArguments.parse(instantiation,
			"<generic-command inputs='" + SELECTION + " as ticket, " + OTHER + "'>"
				+ "<executability><visible-if expr=\"$ticket != $" + OTHER + "\"/></executability>"
				+ "</generic-command>");
		ConfigurationItem rule = config.getExecutability().get(0);

		List<String> runtime = CommandArguments.of(config, (String) null).scope().names();
		assertEquals(List.of("ticket", OTHER), runtime);
		assertEquals(runtime, editorVariables(rule, VisibleIf.Config.EXPR));

		instantiation.getInstance(config);
		instantiation.checkErrors();
		assertRuleError("<generic-command inputs='" + SELECTION + "'>"
			+ "<executability><visible-if expr='$unknown'/></executability>"
			+ "</generic-command>");
		assertRuleError("<generic-command inputs='" + SELECTION + "'>"
			+ "<executability><visible-if expr='x -> $x'/></executability>"
			+ "</generic-command>");
	}

	/**
	 * Tests that the rules of a command run by a trigger see the trigger's value under its name, in
	 * the script editor as at runtime.
	 */
	public void testEditorScopeOfTriggeredCommandRules() throws Exception {
		DefaultViewContext context = context(Map.of(OTHER, "o"));
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestExecutabilityArguments.class);
		HtmlElement.Config config = read(instantiation, "html", HtmlElement.Config.class,
			"<html input='" + OTHER + "' " + HtmlElement.Config.LINK_AS + "='target'>"
				+ "<on-link class='" + GenericViewCommand.class.getName() + "' inputs='" + OTHER + "'>"
				+ "<executability><disabled-if expr=\"$target == $" + OTHER + "\"/></executability>"
				+ "</on-link>"
				+ "</html>");
		ViewCommand.Config commandConfig = (ViewCommand.Config) config.getOnLink();

		assertEquals(List.of(OTHER, "target"),
			editorVariables(commandConfig.getExecutability().get(0), DisabledIf.Config.EXPR));

		ViewCommand command = ViewCommands.instantiate(instantiation, commandConfig, config.getOnLinkAs());
		instantiation.checkErrors();
		ViewCommandModel model = ViewCommandModel.forCommand(context, command, commandConfig, config.getOnLinkAs());
		assertFalse(model.executability("o").isExecutable());
		assertTrue(model.executability("p").isExecutable());
	}

	/**
	 * Tests that the rules of a drag see the dragged row under the name the table gives it, and the
	 * rules of the drag as a whole the value of its input, in the script editor as at runtime.
	 */
	public void testDragRuleScope() throws Exception {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestExecutabilityArguments.class);
		TableElement.Config table = read(instantiation, "table", TableElement.Config.class,
			"<table row-as='ticket'>"
				+ "<drag input='" + SELECTION + " as allowed'>"
				+ "<executability><visible-if expr='$allowed'/></executability>"
				+ "<row-executability><disabled-if expr=\"$ticket == 'locked'\"/></row-executability>"
				+ "</drag>"
				+ "<drop target='row' target-as='goal'>"
				+ "<target-executability><disabled-if expr=\"$goal == 'locked'\"/></target-executability>"
				+ "</drop>"
				+ "<rows>[]</rows>"
				+ "</table>");
		TableDragConfig drag = table.getDrag();

		ActionScope rowScope = ViewExecutabilityRules.scope(drag, TableDragConfig.ROW_EXECUTABILITY);
		assertEquals(List.of("ticket"), rowScope.names());
		assertEquals(rowScope.names(), editorVariables(drag.getRowExecutability().get(0), DisabledIf.Config.EXPR));

		List<String> dragNames = InputExecutabilityConfig.arguments(drag).scope().names();
		assertEquals(List.of("allowed"), dragNames);
		assertEquals(dragNames, editorVariables(drag.getExecutability().get(0), VisibleIf.Config.EXPR));

		ViewExecutabilityRule rowRule =
			ViewExecutabilityRules.build(drag, TableDragConfig.ROW_EXECUTABILITY, new DefaultViewContext(null));
		assertFalse(rowRule.isExecutableFor(rowScope.arguments("locked")).isExecutable());
		assertTrue(rowRule.isExecutableFor(rowScope.arguments("free")).isExecutable());

		DropConfig drop = table.getDrops().get(0);
		ActionScope targetScope = ViewExecutabilityRules.scope(drop, DropConfig.TARGET_EXECUTABILITY);
		assertEquals(List.of("goal"), targetScope.names());
		assertEquals(targetScope.names(),
			editorVariables(drop.getTargetExecutability().get(0), DisabledIf.Config.EXPR));
		ViewExecutabilityRule targetRule =
			ViewExecutabilityRules.build(drop, DropConfig.TARGET_EXECUTABILITY, new DefaultViewContext(null));
		assertFalse(targetRule.isExecutable("locked").isExecutable());
	}

	/**
	 * Tests that the edit rules of a form see the displayed object under the name of the form's
	 * input channel, in the script editor as at runtime.
	 */
	public void testFormEditRuleScope() throws Exception {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestExecutabilityArguments.class);
		FormElement.Config form = read(instantiation, "form", FormElement.Config.class,
			"<form input='account'>"
				+ "<edit-executability><visible-if expr='$account != null'/></edit-executability>"
				+ "</form>");

		ActionScope scope = FormElement.editScope(form);
		assertEquals(List.of("account"), scope.names());
		assertEquals(scope.names(), editorVariables(form.getEditExecutability().get(0), VisibleIf.Config.EXPR));

		ViewExecutabilityRule rule =
			ViewExecutabilityRules.build(form.getEditExecutability(), new DefaultViewContext(null), scope);
		assertFalse(rule.isExecutable((Object) null).isVisible());
		assertTrue(rule.isExecutable("a").isVisible());
	}

	/**
	 * Tests that the bindings and the label of a {@code <show-view>} are scripts of the chain: the
	 * editor offers the arguments of the chain, and the runtime compiles them against those.
	 */
	public void testShowViewScope() throws Exception {
		String xml = "<generic-command inputs='" + SELECTION + " as ticket'>"
			+ "<show-view view='shown.view.xml' label-expr='$ticket'>"
			+ "<bind channel='target' expr='$ticket'/>"
			+ "</show-view>"
			+ "</generic-command>";
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestExecutabilityArguments.class);
		GenericViewCommand.Config config = TestConfiguredActionArguments.parse(instantiation, xml);
		ShowViewAction.Config show = (ShowViewAction.Config) config.getActions().get(0);

		assertEquals(List.of("ticket"), editorVariables(show, ShowViewAction.Config.LABEL_EXPR));
		assertEquals(List.of("ticket"),
			editorVariables(show.getBindings().get(0), DisplayTargetService.BindConfig.EXPR));

		instantiation.getInstance(config);
		instantiation.checkErrors();
		assertInstantiationError(xml.replace("expr='$ticket'", "expr='$unknown'"));
		assertInstantiationError(xml.replace("expr='$ticket'", "expr='t -> $t'"));
	}

	/**
	 * Tests that the scripts of a display target see the object being displayed as
	 * {@value DisplayTargetService#OBJECT}, in the script editor as at runtime.
	 */
	public void testDisplayTargetScripts() throws Exception {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestExecutabilityArguments.class);
		ConfigurationReader reader = new ConfigurationReader(instantiation, Collections.singletonMap("config",
			TypedConfiguration.getConfigurationDescriptor(DisplayTargetService.Config.class)));
		reader.setSource(new ClassRelativeBinaryContent(TestDisplayTargetService.class, "display-targets.config.xml"));
		DisplayTargetService.Config config = (DisplayTargetService.Config) reader.read();
		instantiation.checkErrors();
		List<DisplayTargetService.ShowConfig> shows = config.getTargets().get(0).getShows();

		List<String> object = List.of(DisplayTargetService.OBJECT);
		assertEquals(object, editorVariables(shows.get(0), DisplayTargetService.ShowConfig.LABEL_EXPR));
		assertEquals(object, editorVariables(shows.get(1).getBindings().get(0), DisplayTargetService.BindConfig.EXPR));
		assertEquals(object, DisplayTargetService.OBJECT_SCOPE.names());

		ActionArguments shown = ActionArguments.of(DisplayTargetService.OBJECT, "Shown");
		assertEquals(ResKey.text("Shown"),
			ShowStep.fromConfig(shows.get(0), DisplayTargetService.OBJECT_SCOPE).labelFor(shown));
		assertEquals("Shown",
			ShowStep.fromConfig(shows.get(1), DisplayTargetService.OBJECT_SCOPE).bindings().get(0).evaluate(shown));
	}

	private static void assertRuleError(String xml) throws ConfigurationException {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestExecutabilityArguments.class);
		GenericViewCommand.Config config = TestConfiguredActionArguments.parse(instantiation, xml);
		try {
			ViewExecutabilityRules.build(config, (String) null, new DefaultViewContext(null));
			fail("The rule is refused: " + xml);
		} catch (ConfigurationError ex) {
			// Expected.
		}
	}

	private static <C extends ConfigurationItem> C read(DefaultInstantiationContext instantiation, String tag,
			Class<C> type, String xml) throws ConfigurationException {
		ConfigurationReader reader = new ConfigurationReader(instantiation,
			Collections.singletonMap(tag, TypedConfiguration.getConfigurationDescriptor(type)));
		reader.setSource(CharacterContents.newContent(xml));
		@SuppressWarnings("unchecked")
		C config = (C) reader.read();
		instantiation.checkErrors();
		return config;
	}

	private static List<String> editorVariables(ConfigurationItem config, String property) {
		return ScriptContextVariablesResolver
			.resolve(new DefaultValueModel(config, config.descriptor().getProperty(property)));
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestExecutabilityArguments.class);
	}
}
