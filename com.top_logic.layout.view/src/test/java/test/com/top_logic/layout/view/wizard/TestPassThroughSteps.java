/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.form.StateHandler;
import com.top_logic.layout.view.routing.ParamBindingParticipant;
import com.top_logic.layout.view.wizard.AdvanceStepArguments;
import com.top_logic.layout.view.wizard.GotoStepArguments;
import com.top_logic.layout.view.wizard.ReactWizardControl;
import com.top_logic.layout.view.wizard.WizardElement;
import com.top_logic.layout.view.wizard.WizardStep;

/**
 * Tests how a {@link ReactWizardControl} treats its {@link WizardStep#passThrough() pass-through
 * steps}: their time on every entry, the indicator offering no jump to them, and the address bar,
 * which never names one.
 */
public class TestPassThroughSteps extends TestCase {

	private static final String VIEW = "test-pass-through-steps.view.xml";

	/** Name of the channel holding the step of the wizard with pass-through steps in between. */
	private static final String CURRENT_STEP = "currentStep";

	/** Name of the channel holding the step of the wizard opening with a pass-through step. */
	private static final String INTRO_STEP = "introStep";

	/** Name of the route parameter the step channel is bound to. */
	private static final String STEP_PARAM = "step";

	/** Marks a reported URL that replaces the one shown rather than adding a history entry. */
	private static final String REPLACED = " (replace)";

	private DefaultViewChannel _currentStep;

	private DefaultViewChannel _introStep;

	private ViewContext _context;

	private RouteManager _routes;

	/** The URLs reported to the client, in order, each marked if it is a replacement. */
	private List<String> _reports;

	private List<WizardElement.Config> _wizards;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")));
		_routes = _context.getRouteManager();
		_reports = new ArrayList<>();
		_routes.setUrlChangeHandler((url, title, replace) -> _reports.add(replace ? url + REPLACED : url));

		_currentStep = new DefaultViewChannel(CURRENT_STEP);
		_introStep = new DefaultViewChannel(INTRO_STEP);
		_context.registerChannel(CURRENT_STEP, _currentStep);
		_context.registerChannel(INTRO_STEP, _introStep);

		_wizards = parseWizards();
	}

	/**
	 * Tests that the client is told which steps are pass-through steps.
	 */
	public void testTransitFlagPublished() throws Exception {
		ReactWizardControl wizard = control(_wizards.get(0));

		assertEquals(List.of(false, true, false, true, true, false), publishedTransit(wizard));
	}

	/**
	 * Tests that a pass-through step counts down however it is entered, a move back onto it
	 * included.
	 */
	public void testAutoAdvanceOnBackwardEntry() throws Exception {
		_currentStep.set("done");
		ReactWizardControl wizard = control(_wizards.get(0));
		assertNull(autoAdvance(wizard));

		wizard.getScope().goTo("checked");

		assertEquals(ReactWizardControl.BACKWARD, direction(wizard));
		assertEquals("A pass-through step entered going back counts down as well.", Long.valueOf(1000L),
			autoAdvance(wizard));

		wizard.getScope().goTo("ready");
		assertEquals(Long.valueOf(2000L), autoAdvance(wizard));
	}

	/**
	 * Tests that a pass-through step whose time is up moves the wizard on.
	 */
	public void testAdvanceFromPassThroughStep() throws Exception {
		_currentStep.set("ready");
		ReactWizardControl wizard = control(_wizards.get(0));

		wizard.executeCommand(ReactWizardControl.ADVANCE_STEP_COMMAND,
			Map.of(AdvanceStepArguments.STEP_ID, "ready"));

		assertEquals("role", _currentStep.get());
		assertEquals(Integer.valueOf(2), activeIndex(wizard));
		assertNull("The step reached is one the user leaves.", autoAdvance(wizard));
	}

	/**
	 * Tests that a move back from the step behind pass-through steps returns to the step before
	 * them, and that a jump from the indicator does not reach a pass-through step.
	 */
	public void testBackAndJumpSkipPassThroughSteps() throws Exception {
		_currentStep.set("done");
		ReactWizardControl wizard = control(_wizards.get(0));

		wizard.getScope().back();
		assertEquals("Both pass-through steps are skipped.", "role", _currentStep.get());

		wizard.getScope().back();
		assertEquals("profile", _currentStep.get());

		_currentStep.set("done");
		wizard.executeCommand(ReactWizardControl.GOTO_STEP_COMMAND, Map.of(GotoStepArguments.STEP_ID, "checked"));
		assertEquals("No jump leads onto a pass-through step.", "done", _currentStep.get());

		wizard.executeCommand(ReactWizardControl.GOTO_STEP_COMMAND, Map.of(GotoStepArguments.STEP_ID, "role"));
		assertEquals("A step the user leaves is one to jump to.", "role", _currentStep.get());
	}

	/**
	 * Tests that the address bar keeps naming the step the flow came from while a pass-through step
	 * is displayed, and names the step reached afterwards as a history entry of its own.
	 */
	public void testPassThroughStepIsNoAddress() throws Exception {
		_currentStep.set("profile");
		ReactWizardControl wizard = routedControl(_wizards.get(0), _currentStep);

		wizard.getScope().next();
		assertEquals("ready", _currentStep.get());
		assertEquals("The pass-through step reaches no address bar.", List.of(), _reports);

		wizard.executeCommand(ReactWizardControl.ADVANCE_STEP_COMMAND,
			Map.of(AdvanceStepArguments.STEP_ID, "ready"));
		assertEquals("role", _currentStep.get());
		assertEquals("The step reached follows the one the flow came from in the history.", List.of("role"),
			_reports);

		wizard.getScope().next();
		wizard.executeCommand(ReactWizardControl.ADVANCE_STEP_COMMAND,
			Map.of(AdvanceStepArguments.STEP_ID, "checking"));
		assertEquals("checked", _currentStep.get());
		assertEquals("A pass-through step following another one reaches no address bar either.", List.of("role"),
			_reports);

		wizard.executeCommand(ReactWizardControl.ADVANCE_STEP_COMMAND,
			Map.of(AdvanceStepArguments.STEP_ID, "checked"));
		assertEquals(List.of("role", "done"), _reports);

		wizard.getScope().back();
		assertEquals(List.of("role", "done", "role"), _reports);
	}

	/**
	 * Tests that a move onto a pass-through step from inside a navigation of its own reports
	 * nothing either when that navigation ends.
	 */
	public void testPassThroughStepInsideNavigation() throws Exception {
		_currentStep.set("profile");
		ReactWizardControl wizard = routedControl(_wizards.get(0), _currentStep);

		_routes.navigate(() -> wizard.getScope().next());

		assertEquals("ready", _currentStep.get());
		assertEquals(List.of(), _reports);

		wizard.getScope().next();
		assertEquals(List.of("role"), _reports);
	}

	/**
	 * Tests that a move onto a pass-through step that unsaved changes veto is retried as a move of
	 * the wizard, which keeps the address bar on the step the flow came from.
	 */
	public void testVetoedMoveOntoPassThroughStep() throws Exception {
		_currentStep.set("profile");
		ReactWizardControl wizard = routedControl(_wizards.get(0), _currentStep);
		UnsavedInput unsaved = new UnsavedInput();
		_currentStep.addVetoListener(
			(sender, oldValue, newValue) -> unsaved.isDirty() ? List.of(unsaved) : List.of());

		ChannelVetoException veto;
		try {
			wizard.getScope().next();
			fail("The unsaved input vetoes the move.");
			return;
		} catch (ChannelVetoException ex) {
			veto = ex;
		}
		assertEquals("profile", _currentStep.get());
		assertEquals("The veto names what objects to the move.", List.of(unsaved), veto.getDirtyHandlers());

		unsaved.executeDiscard();
		veto.getContinuation().run();

		assertEquals("ready", _currentStep.get());
		assertEquals("The retried move reaches no address bar either.", List.of(), _reports);

		wizard.executeCommand(ReactWizardControl.ADVANCE_STEP_COMMAND,
			Map.of(AdvanceStepArguments.STEP_ID, "ready"));
		assertEquals(List.of("role"), _reports);
	}

	/**
	 * Tests that a veto forwarded by a move keeps the rollback of the veto it forwards.
	 */
	public void testForwardedVetoKeepsRollback() {
		List<String> log = new ArrayList<>();
		ChannelVetoException veto = new ChannelVetoException(List.of(new UnsavedInput()), () -> log.add("retry"));
		veto.addRollback(() -> log.add("rollback"));

		ChannelVetoException forwarded = veto.withContinuation(() -> {
			log.add("around");
			veto.getContinuation().run();
		});

		assertEquals(veto.getDirtyHandlers(), forwarded.getDirtyHandlers());
		assertSame(veto, forwarded.getCause());
		forwarded.getContinuation().run();
		forwarded.getRollback().run();
		assertEquals(List.of("around", "retry", "rollback"), log);
	}

	/**
	 * Tests that a wizard disposed while it displays a pass-through step leaves the address bar
	 * free.
	 */
	public void testDisposalEndsPassage() throws Exception {
		_currentStep.set("profile");
		ReactWizardControl wizard = routedControl(_wizards.get(0), _currentStep);

		wizard.getScope().next();
		assertEquals(List.of(), _reports);

		wizard.cleanupTree();
		_currentStep.set("done");

		assertEquals("Nothing holds the address bar any more.", "done", _reports.get(_reports.size() - 1));
	}

	/**
	 * Tests that an address naming a pass-through step displays the step a move back from it
	 * reaches, and that the address bar is corrected to that step.
	 */
	public void testAddressOfPassThroughStepIsCorrected() throws Exception {
		_currentStep.set("done");
		ReactWizardControl wizard = routedControl(_wizards.get(0), _currentStep);

		_routes.adoptUrl("checked");
		_routes.resolvePending();
		_routes.finishAdoption();

		assertEquals("role", _currentStep.get());
		assertEquals(Integer.valueOf(2), activeIndex(wizard));
		assertNull(autoAdvance(wizard));
		assertEquals(List.of("role" + REPLACED), _reports);

		// The display is free: the corrected step is no pass-through step holding the address bar.
		wizard.getScope().back();
		assertEquals(List.of("role" + REPLACED, "profile"), _reports);
	}

	/**
	 * Tests that a wizard built for an address naming a pass-through step displays the step a move
	 * back from it reaches.
	 */
	public void testAddressOfPassThroughStepBeforeConstruction() throws Exception {
		_routes.adoptUrl("ready");
		_currentStep.set("ready");
		ReactWizardControl wizard = control(_wizards.get(0));
		_routes.finishAdoption();

		assertEquals("profile", _currentStep.get());
		assertEquals(Integer.valueOf(0), activeIndex(wizard));
	}

	/**
	 * Tests that an address naming a pass-through step that no step to return to precedes displays
	 * that step, which runs.
	 */
	public void testAddressOfLeadingPassThroughStep() throws Exception {
		_introStep.set("form");
		ReactWizardControl wizard = routedControl(_wizards.get(1), _introStep);

		_routes.adoptUrl("intro");
		_routes.resolvePending();
		_routes.finishAdoption();

		assertEquals("intro", _introStep.get());
		assertEquals(Integer.valueOf(0), activeIndex(wizard));
		assertEquals(Long.valueOf(2000L), autoAdvance(wizard));
		assertFalse("Nothing precedes to return to.", wizard.getScope().hasBack());

		wizard.executeCommand(ReactWizardControl.ADVANCE_STEP_COMMAND,
			Map.of(AdvanceStepArguments.STEP_ID, "intro"));
		assertEquals("form", _introStep.get());
		assertEquals(List.of("form"), _reports);
	}

	/**
	 * Unsaved input of a step, objecting to the wizard leaving it until it is discarded.
	 */
	private static class UnsavedInput implements StateHandler {

		private boolean _dirty = true;

		@Override
		public boolean isDirty() {
			return _dirty;
		}

		@Override
		public boolean hasErrors() {
			return false;
		}

		@Override
		public void executeSave() {
			_dirty = false;
		}

		@Override
		public void executeDiscard() {
			_dirty = false;
		}

		@Override
		public String getDescription() {
			return "unsaved input";
		}
	}

	/**
	 * Creates the control of the given wizard, attached and with its step channel bound to the
	 * address, the binding preceding the wizard among the listeners of the channel as in a view.
	 */
	private ReactWizardControl routedControl(WizardElement.Config config, DefaultViewChannel stepChannel)
			throws Exception {
		_routes.register(new ParamBindingParticipant(STEP_PARAM, stepChannel));
		ReactWizardControl wizard = control(config);
		wizard.attach();
		_reports.clear();
		return wizard;
	}

	private ReactWizardControl control(WizardElement.Config config) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestPassThroughSteps.class);
		WizardElement element = (WizardElement) context.getInstance(config);
		context.checkErrors();
		return (ReactWizardControl) element.createControl(_context);
	}

	/**
	 * The position of the displayed step, as the wizard publishes it to the client.
	 */
	private static Object activeIndex(ReactWizardControl wizard) {
		return wizard.scriptingScalarState().get(ReactWizardControl.ACTIVE_INDEX);
	}

	/**
	 * How long the wizard says the step displayed stays, {@code null} for one the user leaves.
	 */
	private static Object autoAdvance(ReactWizardControl wizard) {
		return wizard.scriptingScalarState().get(ReactWizardControl.AUTO_ADVANCE);
	}

	/**
	 * The way the wizard says the step displayed was reached.
	 */
	private static Object direction(ReactWizardControl wizard) {
		return wizard.scriptingScalarState().get(ReactWizardControl.DIRECTION);
	}

	/**
	 * Whether the wizard publishes each step as a pass-through step, in order.
	 */
	@SuppressWarnings("unchecked")
	private static List<Object> publishedTransit(ReactWizardControl wizard) {
		List<Map<String, Object>> steps =
			(List<Map<String, Object>>) wizard.scriptingScalarState().get(ReactWizardControl.STEPS);
		return steps.stream().map(step -> step.get(ReactWizardControl.STEP_TRANSIT)).toList();
	}

	private List<WizardElement.Config> parseWizards() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestPassThroughSteps.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestPassThroughSteps.class, VIEW);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config view = (ViewElement.Config) reader.read();
		context.checkErrors();

		PanelElement.Config panel = (PanelElement.Config) view.getContent();
		return panel.getChildren().stream()
			.filter(WizardElement.Config.class::isInstance)
			.map(WizardElement.Config.class::cast)
			.toList();
	}

	/**
	 * Test suite requiring the modules that configuration parsing and object labels need.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestPassThroughSteps.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE));
	}
}
