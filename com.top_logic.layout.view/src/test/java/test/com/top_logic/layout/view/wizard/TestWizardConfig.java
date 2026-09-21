/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.wizard.StaticStepSource;
import com.top_logic.layout.view.wizard.WizardBackCommand;
import com.top_logic.layout.view.wizard.WizardElement;
import com.top_logic.layout.view.wizard.WizardGotoCommand;
import com.top_logic.layout.view.wizard.WizardNextCommand;
import com.top_logic.layout.view.wizard.WizardScope;
import com.top_logic.layout.view.wizard.WizardStep;
import com.top_logic.layout.view.wizard.WizardStepSource;

/**
 * Tests that the tags of the wizard resolve: the element with its steps, and the commands moving
 * through it with the rules guarding them.
 */
public class TestWizardConfig extends TestCase {

	private static final String VIEW = "test-wizard.view.xml";

	/**
	 * Tests that the steps written out inside a {@code <wizard>} become its step sources, in
	 * configuration order.
	 */
	public void testStepsAreContent() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestWizardConfig.class);
		WizardElement.Config config = wizard(parse(context));

		List<String> ids = config.getSteps().stream()
			.map(step -> ((StaticStepSource.Config) step).getId())
			.toList();
		assertEquals(List.of("contact", "payment", "summary"), ids);
		assertTrue("The step list is switched on in the view.", config.getStepList());
		assertTrue("The counter is on by default.", config.getCounter());
		assertTrue("The progress bar is on by default.", config.getProgress());
	}

	/**
	 * Tests that the instantiated element addresses the content of each step by that step's id.
	 */
	public void testStepsAreAddressedById() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestWizardConfig.class);
		UIElement element = context.getInstance(wizard(parse(context)));
		context.checkErrors();

		List<String> keys = element.getChildGroups().stream().map(ChildGroup::key).toList();
		assertEquals(List.of("contact", "payment", "summary"), keys);
	}

	/**
	 * Tests that the tags of the moves resolve to the commands, and that a move inside a chain
	 * resolves to the action of the same tag.
	 */
	public void testCommandTags() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestWizardConfig.class);
		Map<String, ViewCommand> commands = commands(context, firstStepContent(parse(context)));

		assertTrue(commands.get("nextCommand") instanceof WizardNextCommand);
		assertTrue(commands.get("backCommand") instanceof WizardBackCommand);
		assertTrue(commands.get("gotoCommand") instanceof WizardGotoCommand);
		assertTrue("A <wizard-goto> in a chain is an action of a generic command.",
			commands.get("finish") instanceof GenericViewCommand);
	}

	/**
	 * Tests that a step written out with a duration carries it as the time after which the wizard
	 * moves on by itself.
	 */
	public void testAutoAdvance() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestWizardConfig.class);
		WizardElement.Config wizard = wizard(parse(context));

		assertEquals("2s is two thousand milliseconds.", Long.valueOf(2000L),
			((StaticStepSource.Config) wizard.getSteps().get(1)).getAutoAdvance());
		assertNull("A step the user leaves has no time of its own.",
			((StaticStepSource.Config) wizard.getSteps().get(0)).getAutoAdvance());

		WizardElement element = (WizardElement) context.getInstance(wizard);
		context.checkErrors();
		List<WizardStep> steps = new ArrayList<>();
		for (WizardStepSource source : element.getSources()) {
			steps.addAll(source.steps(new DefaultViewContext(null)));
		}
		assertEquals("The step carries the duration to the wizard.", Long.valueOf(2000L),
			steps.get(1).autoAdvanceMillis());
		assertNull(steps.get(0).autoAdvanceMillis());
	}

	/**
	 * Tests that the rules guarding the moves follow the wizard: a Back button is absent on the
	 * first step and appears once the wizard has moved on, without anything writing the command's
	 * own input.
	 */
	public void testMoveRulesFollowTheWizard() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestWizardConfig.class);
		PanelElement.Config panel = firstStepContent(parse(context));

		ViewChannel step = new DefaultViewChannel("currentStep");
		WizardScope scope = new WizardScope(step, List.of(step("contact"), step("payment"), step("summary")));
		ViewContext stepContext = new DefaultViewContext(null).withScope(WizardScope.class, scope);

		ViewCommandModel back = model(context, stepContext, panel, "backCommand");
		ViewCommandModel next = model(context, stepContext, panel, "nextCommand");
		back.attach(null);
		next.attach(null);

		assertFalse("Nothing precedes the first step, so no Back is offered.", back.isVisible());
		assertTrue("A step follows the first one, so Next is offered.", next.isVisible());

		scope.next();

		assertTrue("The move is reported, so Back appears.", back.isVisible());
		assertTrue(next.isVisible());

		scope.goTo("summary");

		assertTrue(back.isVisible());
		assertFalse("Nothing follows the last step, so no Next is offered.", next.isVisible());

		back.detach();
		next.detach();
		scope.goTo("contact");

		assertTrue("A detached command hears nothing and keeps the state it had.", back.isVisible());
	}

	/**
	 * The {@link ViewCommandModel} of the named command of the given panel, built in the given
	 * context exactly as a hosting element builds it.
	 */
	private ViewCommandModel model(DefaultInstantiationContext context, ViewContext stepContext,
			PanelElement.Config panel, String name) throws Exception {
		for (PolymorphicConfiguration<? extends ViewCommand> config : panel.getCommands()) {
			ViewCommand.Config commandConfig = (ViewCommand.Config) config;
			if (name.equals(commandConfig.getName())) {
				ViewCommand command = context.getInstance(config);
				context.checkErrors();
				return ViewCommandModel.forCommand(stepContext, command, commandConfig);
			}
		}
		throw new AssertionError("The step holds a command named '" + name + "'.");
	}

	/**
	 * A step of the sequence the rules are tested over; its content is never built.
	 */
	private static WizardStep step(String id) {
		return new WizardStep(id, null, null, context -> null, null);
	}

	private WizardElement.Config wizard(ViewElement.Config view) {
		PanelElement.Config panel = (PanelElement.Config) view.getContent();
		return panel.getChildren().stream()
			.filter(WizardElement.Config.class::isInstance)
			.map(WizardElement.Config.class::cast)
			.findFirst()
			.orElseThrow(() -> new AssertionError("The panel contains a wizard."));
	}

	private ViewElement.Config parse(DefaultInstantiationContext context) throws Exception {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestWizardConfig.class, VIEW);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();
		return config;
	}

	/**
	 * The panel inside the first step, which carries the commands that move the wizard.
	 *
	 * <p>
	 * A move finds its wizard through the scope the wizard installs into the context of a step's
	 * content, so a command moving the wizard sits inside a step, not beside the wizard.
	 * </p>
	 */
	private PanelElement.Config firstStepContent(ViewElement.Config view) {
		StaticStepSource.Config step = (StaticStepSource.Config) wizard(view).getSteps().get(0);
		return (PanelElement.Config) step.getChildren().get(0);
	}

	private Map<String, ViewCommand> commands(DefaultInstantiationContext context, PanelElement.Config panel)
			throws Exception {
		Map<String, ViewCommand> result = new HashMap<>();
		for (PolymorphicConfiguration<? extends ViewCommand> config : panel.getCommands()) {
			result.put(((ViewCommand.Config) config).getName(), context.getInstance(config));
		}
		context.checkErrors();
		return result;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestWizardConfig.class, TypeIndex.Module.INSTANCE);
	}
}
