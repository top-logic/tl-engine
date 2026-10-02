/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

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
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.command.ActionScript;
import com.top_logic.layout.view.command.CommandCliques;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.IfAction;
import com.top_logic.layout.view.command.SwitchAction;
import com.top_logic.layout.view.command.SwitchAction.SwitchCase;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActions;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests that the tone of a command follows what the command does: an author says what a command
 * is - it deletes, it sits in the clique of deleting commands - and never which color it has.
 */
public class TestActionTone extends TestCase {

	/** An ordinary action, standing in for writing a channel or running a script. */
	static final ViewAction ORDINARY = (context, input) -> input;

	/** A condition that always holds. */
	static final ActionScript TRUE = (context, input) -> Boolean.TRUE;

	/** Deleting is destructive; an action saying nothing is ordinary. */
	public void testDeletingIsDestructive() throws Exception {
		assertEquals(ButtonTone.DANGER, instantiate(parseCommand("<delete-object/>")).getTone());
		assertEquals(ButtonTone.DEFAULT, ORDINARY.getTone());
	}

	/** A chain is destructive as soon as one of its actions is, wherever it stands in the chain. */
	public void testAChainIsDestructiveAsSoonAsOneOfItsActionsIs() {
		assertEquals(ButtonTone.DANGER, ViewActions.tone(List.of(ORDINARY, new Destroying(), ORDINARY)));
		assertEquals(ButtonTone.DEFAULT, ViewActions.tone(List.of(ORDINARY, ORDINARY)));
		assertEquals(ButtonTone.DEFAULT, ViewActions.tone(List.of()));
	}

	/**
	 * An action made of actions carries their tone: a deletion wrapped in a transaction, or placed
	 * in any branch of a condition - the branch not taken decides the button as well, as it decides
	 * whether the command applies the form.
	 */
	public void testCompositesCarryTheToneOfTheActionsTheyRun() throws Exception {
		assertEquals(ButtonTone.DANGER,
			instantiate(parseCommand("<with-transaction><delete-object/></with-transaction>")).getTone());
		assertEquals(ButtonTone.DEFAULT, instantiate(parseCommand("<with-transaction/>")).getTone());

		assertEquals(ButtonTone.DANGER, new IfAction(TRUE, List.of(ORDINARY), List.of(new Destroying())).getTone());
		assertEquals(ButtonTone.DANGER, new IfAction(TRUE, List.of(new Destroying()), List.of()).getTone());
		assertEquals(ButtonTone.DEFAULT, new IfAction(TRUE, List.of(ORDINARY), List.of(ORDINARY)).getTone());

		assertEquals(ButtonTone.DANGER, new SwitchAction(TRUE,
			List.of(new SwitchCase(value -> true, List.of(new Destroying()))), List.of()).getTone());
		assertEquals(ButtonTone.DANGER, new SwitchAction(TRUE, List.of(), List.of(new Destroying())).getTone());
		assertEquals(ButtonTone.DEFAULT, new SwitchAction(TRUE,
			List.of(new SwitchCase(value -> true, List.of(ORDINARY))), List.of(ORDINARY)).getTone());
	}

	/** The model of a command offers the command's tone to every UI element showing it. */
	public void testTheModelTakesTheToneOfTheChain() throws Exception {
		ViewCommand delete = instantiate(parseCommand("<delete-object/>"));

		assertEquals(ButtonTone.DANGER, model(delete, null).getTone());
	}

	/**
	 * A command stating its meaning only by its clique - a chain without a destructive action of
	 * its own, placed among the deleting commands - is destructive as well; any other clique is not.
	 */
	public void testTheDeleteCliqueIsTheSecondSource() {
		ViewCommand ordinary = (context, input) -> HandlerResult.DEFAULT_RESULT;

		assertEquals(ButtonTone.DANGER, model(ordinary, CommandCliques.DELETE).getTone());
		assertEquals(ButtonTone.DEFAULT, model(ordinary, CommandCliques.EDIT).getTone());
		assertEquals(ButtonTone.DEFAULT, model(ordinary, null).getTone());
	}

	private static ViewCommandModel model(ViewCommand command, String clique) {
		ViewCommand.Config config = TypedConfiguration.newConfigItem(ViewCommand.Config.class);
		if (clique != null) {
			config.update(config.descriptor().getProperty(ViewCommand.Config.CLIQUE), clique);
		}
		return new ViewCommandModel(command, config, null, ViewExecutabilityRule.ALWAYS_EXECUTABLE);
	}

	/** The command a view declares with the given actions, as an author writes it. */
	static GenericViewCommand.Config parseCommand(String actionsXml) throws Exception {
		String view = """
				<view>
					<panel>
						<commands>
							<generic-command name="tested">
								%s
							</generic-command>
						</commands>
					</panel>
				</view>
				""".formatted(actionsXml);

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestActionTone.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(view, "test-tone.view.xml"));
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		PanelElement.Config panel = (PanelElement.Config) config.getContent();
		return (GenericViewCommand.Config) panel.getCommands().get(0);
	}

	/** The command built from the given configuration; nothing of it is executed. */
	static ViewCommand instantiate(GenericViewCommand.Config config) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestActionTone.class);
		ViewCommand command = context.getInstance(config);
		context.checkErrors();
		return command;
	}

	/** Destructive without touching any storage: stands in for deleting in a chain that runs. */
	static final class Destroying implements ViewAction {

		@Override
		public Object execute(ReactContext context, Object input) {
			return input;
		}

		@Override
		public ButtonTone getTone() {
			return ButtonTone.DANGER;
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, the resources labels are resolved from,
	 * and the {@link ThemeFactory} a parsed view resolves its images with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestActionTone.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE));
	}
}
