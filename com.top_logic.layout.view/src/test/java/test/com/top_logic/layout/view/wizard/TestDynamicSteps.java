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
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.wizard.AdvanceStepArguments;
import com.top_logic.layout.view.wizard.DynamicStepsSource;
import com.top_logic.layout.view.wizard.ReactWizardControl;
import com.top_logic.layout.view.wizard.StaticStepSource;
import com.top_logic.layout.view.wizard.WizardElement;
import com.top_logic.layout.view.wizard.WizardScope;
import com.top_logic.layout.view.wizard.WizardStep;
import com.top_logic.layout.view.wizard.WizardStepSource;
import com.top_logic.util.Resources;

/**
 * Tests the {@code <dynamic-steps>} source: the steps it contributes for what its channel holds,
 * where they sit among the steps written out beside them, and how a wizard follows that channel
 * while it is displayed.
 */
public class TestDynamicSteps extends TestCase {

	private static final String VIEW = "test-dynamic-steps.view.xml";

	/** Name of the channel holding the elements one step each is contributed for. */
	private static final String QUESTIONS = "questions";

	/** Name of the channel holding the step the mixed wizard displays. */
	private static final String CURRENT_STEP = "currentStep";

	/** Name of the channel holding the step the wizard ending in dynamic steps displays. */
	private static final String CONCIERGE_STEP = "conciergeStep";

	/** Name of the channel holding the step the wizard with scripted names displays. */
	private static final String SCRIPTED_STEP = "scriptedStep";

	private DefaultViewChannel _questions;

	private DefaultViewChannel _currentStep;

	private DefaultViewChannel _conciergeStep;

	private DefaultViewChannel _scriptedStep;

	private ViewContext _context;

	private List<WizardElement.Config> _wizards;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")));

		_questions = new DefaultViewChannel(QUESTIONS);
		_currentStep = new DefaultViewChannel(CURRENT_STEP);
		_conciergeStep = new DefaultViewChannel(CONCIERGE_STEP);
		_scriptedStep = new DefaultViewChannel(SCRIPTED_STEP);
		_context.registerChannel(QUESTIONS, _questions);
		_context.registerChannel(CURRENT_STEP, _currentStep);
		_context.registerChannel(CONCIERGE_STEP, _conciergeStep);
		_context.registerChannel(SCRIPTED_STEP, _scriptedStep);

		_wizards = parseWizards();
	}

	/**
	 * Tests that a {@code <dynamic-steps>} between two steps written out parses as a source of its
	 * own, with its channel and its content template.
	 */
	public void testParse() throws Exception {
		List<Object> sourceTypes = _wizards.get(0).getSteps().stream()
			.map(source -> (Object) source.getImplementationClass())
			.toList();
		assertEquals(List.of(StaticStepSource.class, DynamicStepsSource.class, StaticStepSource.class),
			sourceTypes);

		DynamicStepsSource.Config config = dynamicSource(_wizards.get(0));
		assertEquals(QUESTIONS, config.getSteps().getChannelName());
		assertEquals("The element channel defaults to the usual name.", "element", config.getElementChannel());
		assertNull("No function names the steps.", config.getLabel());
		assertNull("No function chooses their icon.", config.getIcon());
		assertEquals("The content written out is the template of one step.", 1, config.getContent().size());
	}

	/**
	 * Tests that the label and icon functions parse, are compiled when the source is instantiated,
	 * and that the element channel can be named.
	 */
	public void testScriptedSource() throws Exception {
		DynamicStepsSource.Config config = dynamicSource(_wizards.get(2));
		assertNotNull("A function names the steps.", config.getLabel());
		assertNotNull("A function chooses their icon.", config.getIcon());
		assertNotNull("A function says how long a step stays.", config.getAutoAdvance());
		assertEquals("question", config.getElementChannel());

		WizardStepSource source = sources(_wizards.get(2)).get(0);
		assertTrue("The functions are compiled with the source.", source instanceof DynamicStepsSource);
		assertEquals(List.of(_questions), source.observedChannels(_context));
	}

	/**
	 * Tests that the elements of the channel become steps between the steps written out, in the
	 * order of the list, and that the element itself is the key of its step.
	 */
	public void testElementsAreStepsInOrder() throws Exception {
		_questions.set(List.of("a", "b"));

		assertEquals(List.of("welcome", "a", "b", "summary"), keys(expand(_wizards.get(0))));
	}

	/**
	 * Tests that a channel holding nothing contributes no step, and one holding a single object
	 * contributes one.
	 */
	public void testChannelWithoutList() throws Exception {
		assertEquals("A channel holding nothing contributes no step.",
			List.of("welcome", "summary"), keys(expand(_wizards.get(0))));

		_questions.set("a");
		assertEquals("A channel holding one object contributes one step.",
			List.of("welcome", "a", "summary"), keys(expand(_wizards.get(0))));
	}

	/**
	 * Tests that without a label function a step is named after its element, the way the element is
	 * named everywhere else, and that it carries no icon.
	 */
	public void testDefaultLabel() throws Exception {
		_questions.set(List.of("a", "b"));

		List<WizardStep> steps = expand(_wizards.get(0));
		assertEquals("Without a label function, the element names its step.", List.of("a", "b"),
			labels(steps).subList(1, 3));
		assertNull("Without an icon function, a step carries no icon.", steps.get(1).icon());
	}

	/**
	 * Tests that the source names the channel it decides by, so that the wizard can follow it.
	 */
	public void testObservedChannels() throws Exception {
		WizardStepSource source = sources(_wizards.get(0)).get(1);

		assertEquals(List.of(_questions), source.observedChannels(_context));
		assertEquals("A step written out decides by nothing.", List.of(),
			sources(_wizards.get(0)).get(0).observedChannels(_context));
	}

	/**
	 * Tests that a wizard expands its sequence anew when the list channel is written, and that the
	 * step displayed keeps its content while its key is still in the sequence.
	 */
	public void testAppendedElementBecomesStep() throws Exception {
		_questions.set(new ArrayList<>(List.of("a", "b")));
		_currentStep.set("b");
		ReactWizardControl wizard = control(_wizards.get(0));

		assertEquals(List.of("welcome", "a", "b", "summary"), keys(wizard.steps()));
		assertEquals(Integer.valueOf(2), activeIndex(wizard));
		ReactControl content = activeContent(wizard);

		_questions.set(List.of("a0", "a", "b"));

		assertEquals("The sequence follows the channel.", List.of("welcome", "a0", "a", "b", "summary"),
			keys(wizard.steps()));
		assertEquals("The step displayed moved along, the display followed it.", Integer.valueOf(3),
			activeIndex(wizard));
		assertSame("A step that keeps its key keeps its content.", content, activeContent(wizard));
		assertEquals("The client sees the sequence as it is now.",
			List.of("welcome", "a0", "a", "b", "summary"), publishedKeys(wizard));
	}

	/**
	 * Tests that appending an element makes a move on available on what was the last step, which is
	 * what lets one command append a step and go there.
	 */
	public void testAppendOffersNextStep() throws Exception {
		_questions.set(new ArrayList<>(List.of("a")));
		_conciergeStep.set("a");
		ReactWizardControl wizard = control(_wizards.get(1));
		WizardScope scope = wizard.getScope();

		assertFalse("Nothing follows the last step yet.", scope.hasNext());

		_questions.set(List.of("a", "b"));

		assertTrue("The appended element is a step to move on to.", scope.hasNext());
		scope.next();
		assertEquals("b", _conciergeStep.get());
		assertEquals(Integer.valueOf(2), activeIndex(wizard));
	}

	/**
	 * Tests that a wizard whose displayed step is removed falls back to its first step.
	 */
	public void testRemovedStepFallsBackToFirst() throws Exception {
		_questions.set(new ArrayList<>(List.of("a", "b")));
		_currentStep.set("b");
		ReactWizardControl wizard = control(_wizards.get(0));
		ReactControl content = activeContent(wizard);

		_questions.set(List.of("a"));

		assertEquals(List.of("welcome", "a", "summary"), keys(wizard.steps()));
		assertEquals("The step the channel names has gone, so the first one is displayed.",
			Integer.valueOf(0), activeIndex(wizard));
		assertEquals(0, wizard.getScope().currentIndex());
		assertNotSame("The content of the step that has gone was dropped.", content, activeContent(wizard));
	}

	/**
	 * Tests that a step whose time is up moves the wizard on, and that a timer outliving its step
	 * does not: the user may have moved on themselves while it ran.
	 */
	public void testAdvanceStepOnlyFromTheStepDisplayed() throws Exception {
		_questions.set(new ArrayList<>(List.of("a", "b")));
		_currentStep.set("a");
		ReactWizardControl wizard = control(_wizards.get(0));

		assertEquals(Integer.valueOf(1), activeIndex(wizard));

		wizard.executeCommand(ReactWizardControl.ADVANCE_STEP_COMMAND,
			Map.of(AdvanceStepArguments.STEP_ID, "b"));

		assertEquals("The timer of a step that is not displayed moves nothing.",
			Integer.valueOf(1), activeIndex(wizard));
		assertEquals("a", _currentStep.get());

		wizard.executeCommand(ReactWizardControl.ADVANCE_STEP_COMMAND,
			Map.of(AdvanceStepArguments.STEP_ID, "a"));

		assertEquals("The step displayed ran out of time, so the wizard moved on.", "b",
			_currentStep.get());
		assertEquals(Integer.valueOf(2), activeIndex(wizard));
	}

	/**
	 * Tests that the wizard publishes how the step displayed was reached.
	 */
	public void testPublishedDirection() throws Exception {
		_questions.set(new ArrayList<>(List.of("a", "b")));
		ReactWizardControl wizard = control(_wizards.get(0));

		assertEquals(ReactWizardControl.FORWARD, direction(wizard));

		wizard.getScope().goTo("b");
		assertEquals(ReactWizardControl.FORWARD, direction(wizard));

		wizard.getScope().back();
		assertEquals("A move towards the beginning is reported as such.", ReactWizardControl.BACKWARD,
			direction(wizard));
	}

	/**
	 * Tests that a step's own time runs while the flow leads through it: it is published for a step
	 * entered going forward, and not for one the user came back to.
	 */
	public void testAutoAdvanceOnlyForwards() throws Exception {
		_questions.set(new ArrayList<>(List.of("a", "b")));
		ReactWizardControl wizard = control(_wizards.get(0));

		assertEquals("A wizard opening on a timed step counts it down.", Long.valueOf(2000L),
			autoAdvance(wizard));

		wizard.getScope().next();
		assertNull("The step moved to has no time of its own.", autoAdvance(wizard));

		wizard.getScope().goTo("summary");
		assertEquals("A timed step moved on to counts down.", Long.valueOf(2000L), autoAdvance(wizard));

		wizard.getScope().back();
		assertEquals(ReactWizardControl.BACKWARD, direction(wizard));
		assertNull(autoAdvance(wizard));

		wizard.getScope().goTo("welcome");
		assertEquals("Coming back is a move backwards.", ReactWizardControl.BACKWARD, direction(wizard));
		assertNull("A timed step the user came back to waits for them.", autoAdvance(wizard));
	}

	/**
	 * Tests that a re-expansion carrying the displayed step along leaves its time alone, so a step
	 * already counting down keeps counting rather than starting over.
	 */
	public void testAutoAdvanceSurvivesReExpansion() throws Exception {
		_questions.set(new ArrayList<>(List.of("a")));
		_currentStep.set("summary");
		ReactWizardControl wizard = control(_wizards.get(0));

		assertEquals(Integer.valueOf(2), activeIndex(wizard));
		assertEquals(Long.valueOf(2000L), autoAdvance(wizard));

		_questions.set(List.of("a", "b"));

		assertEquals("The step was carried along.", Integer.valueOf(3), activeIndex(wizard));
		assertEquals("Its time was not restarted.", Long.valueOf(2000L), autoAdvance(wizard));
	}

	/**
	 * Tests that a step reached again is published under the identifier it had, so that an
	 * identifier the client holds keeps naming the same step.
	 */
	public void testIdentifiersStayAssigned() throws Exception {
		_questions.set(new ArrayList<>(List.of("a", "b")));
		ReactWizardControl wizard = control(_wizards.get(0));
		List<Object> before = publishedKeys(wizard);

		_questions.set(List.of("b"));
		_questions.set(List.of("a", "b"));

		assertEquals("The steps are published under the identifiers they had.", before, publishedKeys(wizard));
	}

	/**
	 * The one {@code <dynamic-steps>} of the given wizard.
	 */
	private static DynamicStepsSource.Config dynamicSource(WizardElement.Config wizard) {
		return wizard.getSteps().stream()
			.filter(DynamicStepsSource.Config.class::isInstance)
			.map(DynamicStepsSource.Config.class::cast)
			.findFirst()
			.orElseThrow(() -> new AssertionError("The wizard holds a <dynamic-steps>."));
	}

	private List<WizardStep> expand(WizardElement.Config config) throws Exception {
		List<WizardStep> result = new ArrayList<>();
		for (WizardStepSource source : sources(config)) {
			result.addAll(source.steps(_context));
		}
		return result;
	}

	private List<WizardStepSource> sources(WizardElement.Config config) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestDynamicSteps.class);
		WizardElement element = (WizardElement) context.getInstance(config);
		context.checkErrors();
		return element.getSources();
	}

	private ReactWizardControl control(WizardElement.Config config) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestDynamicSteps.class);
		WizardElement element = (WizardElement) context.getInstance(config);
		context.checkErrors();
		return (ReactWizardControl) element.createControl(_context);
	}

	/**
	 * The keys of the given steps, in order.
	 */
	private static List<Object> keys(List<WizardStep> steps) {
		return steps.stream().map(WizardStep::key).toList();
	}

	/**
	 * The names of the given steps, in order.
	 */
	private static List<String> labels(List<WizardStep> steps) {
		List<String> result = new ArrayList<>(steps.size());
		for (WizardStep step : steps) {
			result.add(label(step.label()));
		}
		return result;
	}

	private static String label(ResKey key) {
		return key == null ? null : Resources.getInstance().getString(key);
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
	 * The step identifiers the wizard publishes to the client, in order.
	 */
	@SuppressWarnings("unchecked")
	private static List<Object> publishedKeys(ReactWizardControl wizard) {
		List<Map<String, Object>> steps =
			(List<Map<String, Object>>) wizard.scriptingScalarState().get(ReactWizardControl.STEPS);
		return steps.stream().map(step -> step.get(ReactWizardControl.STEP_KEY)).toList();
	}

	/**
	 * The control displaying the content of the step the wizard shows.
	 *
	 * <p>
	 * The wizards under test are configured without a progress bar, so the content is the only
	 * control the wizard displays.
	 * </p>
	 */
	private static ReactControl activeContent(ReactWizardControl wizard) {
		List<ReactControl> children = wizard.displayedChildren();
		assertEquals("The wizard displays the content of one step.", 1, children.size());
		return children.get(0);
	}

	private List<WizardElement.Config> parseWizards() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestDynamicSteps.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestDynamicSteps.class, VIEW);
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
			ServiceTestSetup.createSetup(TestDynamicSteps.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE));
	}
}
