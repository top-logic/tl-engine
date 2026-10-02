/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactAlertControl;
import com.top_logic.layout.react.control.overlay.DismissArguments;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl.Variant;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.AlertElement;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.model.ModelService;

/**
 * Tests what an {@code <alert>} shows and when: its message over the input channels, its condition,
 * and the dismissal by the user together with the rule that brings a dismissed message back.
 *
 * <p>
 * A new evaluation without a new channel value - what the change of an input object causes - is
 * caused here by displaying the alert again after it was off screen: the observation of the input
 * objects then evaluates anew, exactly as on an object change. The value on the channel is a map
 * the test changes in place, standing in for an object whose attribute was edited.
 * </p>
 */
public class TestAlertElement extends BasicTestCase {

	/** Name of the channel the functions of the alert are evaluated over. */
	private static final String STATE_CHANNEL = "state";

	/** Name of a second input channel. */
	private static final String OTHER_CHANNEL = "other";

	/** Name of the channel the dismiss actions write their input to. */
	private static final String DISMISSED_CHANNEL = "dismissed";

	/** Key of the entry of the state map the message is read from. */
	private static final String TEXT = "text";

	/** Key of the entry of the state map the condition is read from. */
	private static final String SHOW = "show";

	private ViewChannel _state;

	private ViewChannel _other;

	private ViewChannel _dismissed;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_state = new DefaultViewChannel(STATE_CHANNEL);
		_other = new DefaultViewChannel(OTHER_CHANNEL);
		_dismissed = new DefaultViewChannel(DISMISSED_CHANNEL);
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_context.registerChannel(STATE_CHANNEL, _state);
		_context.registerChannel(OTHER_CHANNEL, _other);
		_context.registerChannel(DISMISSED_CHANNEL, _dismissed);
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_dismissed = null;
		_other = null;
		_state = null;

		super.tearDown();
	}

	/**
	 * Tests that a fixed message is shown with the configured severity and title, and without input.
	 */
	public void testFixedMessage() throws ConfigurationException {
		ReactAlertControl alert = alert(
			"<alert severity='warning'>"
				+ "<title><en>Attention</en></title>"
				+ "<message><en>Something to know.</en></message>"
				+ "</alert>");

		assertFalse("A message without condition is shown.", alert.isHidden());
		assertEquals(Variant.WARNING, alert.getVariant());
		assertEquals("Attention", alert.getTitle());
		assertEquals("Something to know.", alert.getMessage());
		assertFalse("An alert is not closable unless configured.", alert.isClosable());
	}

	/**
	 * Tests that the severity defaults to an information and the title to none.
	 */
	public void testDefaults() throws ConfigurationException {
		ReactAlertControl alert = alert("<alert><message><en>Hint</en></message></alert>");

		assertEquals(Variant.INFO, alert.getVariant());
		assertNull("Without a title, the message has none.", alert.getTitle());
	}

	/**
	 * Tests that computed title and message receive all input channels and follow a new value of
	 * either of them.
	 */
	public void testFunctionsFollowTheInputChannels() throws ConfigurationException {
		_state.set("A");
		_other.set("B");
		ReactAlertControl alert = alert(
			"<alert inputs='state, other'"
				+ " title-expr='s -> o -> $s'"
				+ " message-expr='s -> o -> $s + \"/\" + $o'/>");

		assertEquals("A", alert.getTitle());
		assertEquals("A/B", alert.getMessage());

		_state.set("C");
		assertEquals("C", alert.getTitle());
		assertEquals("C/B", alert.getMessage());

		_other.set("D");
		assertEquals("C/D", alert.getMessage());
	}

	/**
	 * Tests that the condition hides and shows the message as the input changes.
	 */
	public void testConditionHidesAndShows() throws ConfigurationException {
		_state.set("on");
		ReactAlertControl alert = alert(
			"<alert inputs='state' visible-if=\"s -> $s == 'on'\" message-expr='s -> $s'/>");

		assertFalse(alert.isHidden());

		_state.set("off");
		assertTrue("The message is hidden while the condition does not hold.", alert.isHidden());

		_state.set("on");
		assertFalse("The message is shown again when the condition holds again.", alert.isHidden());
	}

	/**
	 * Tests that the user dismissing the message hides it and runs the dismiss actions with the value
	 * of the first input channel.
	 */
	public void testDismissRunsTheActionsWithTheFirstInput() throws ConfigurationException {
		_state.set("first");
		_other.set("second");
		ReactAlertControl alert = closableAlert();

		dismiss(alert);

		assertTrue("A dismissed message is hidden.", alert.isHidden());
		assertEquals("The dismiss actions receive the value of the first input channel.", "first",
			_dismissed.get());
	}

	/**
	 * Tests that a dismissed message stays closed while an evaluation yields what was dismissed, and
	 * is shown again when an evaluation yields something else.
	 */
	public void testDismissedMessageReturnsOnChangedContent() throws ConfigurationException {
		Map<String, Object> state = state("one", true);
		_state.set(state);
		ReactAlertControl alert = alert(
			"<alert inputs='state' closable='true'"
				+ " visible-if=\"s -> $s['" + SHOW + "']\""
				+ " message-expr=\"s -> $s['" + TEXT + "']\"/>");
		alert.attach();

		dismiss(alert);
		assertTrue(alert.isHidden());

		reevaluate(alert);
		assertTrue("An evaluation yielding the dismissed message keeps it closed.", alert.isHidden());

		state.put(TEXT, "two");
		reevaluate(alert);
		assertFalse("An evaluation yielding another message shows it again.", alert.isHidden());
		assertEquals("two", alert.getMessage());
	}

	/**
	 * Tests that a dismissed message whose condition stops and starts holding again is shown again.
	 */
	public void testDismissedMessageReturnsWhenTheConditionHoldsAgain() throws ConfigurationException {
		Map<String, Object> state = state("one", true);
		_state.set(state);
		ReactAlertControl alert = alert(
			"<alert inputs='state' closable='true'"
				+ " visible-if=\"s -> $s['" + SHOW + "']\""
				+ " message-expr=\"s -> $s['" + TEXT + "']\"/>");
		alert.attach();

		dismiss(alert);

		state.put(SHOW, Boolean.FALSE);
		reevaluate(alert);
		assertTrue("The message is hidden while the condition does not hold.", alert.isHidden());

		state.put(SHOW, Boolean.TRUE);
		reevaluate(alert);
		assertFalse("The condition holding again shows the message again.", alert.isHidden());
	}

	/**
	 * Tests that a new value of an input channel shows a dismissed message again, even when the
	 * message stays the same.
	 */
	public void testDismissedMessageReturnsOnNewChannelValue() throws ConfigurationException {
		_state.set("first");
		_other.set("second");
		ReactAlertControl alert = closableAlert();

		dismiss(alert);
		assertTrue(alert.isHidden());

		_other.set("third");
		assertFalse("A new value of an input channel shows a dismissed message again.", alert.isHidden());
		assertEquals("Fixed", alert.getMessage());
	}

	/**
	 * Tests that the commands of an alert are offered as its buttons.
	 */
	public void testCommandsBecomeButtons() throws ConfigurationException {
		ReactAlertControl alert = alert(
			"<alert>"
				+ "<message><en>Hint</en></message>"
				+ "<generic-command><label><en>Fix</en></label></generic-command>"
				+ "</alert>");

		List<ReactControl> actions = alert.actions();
		assertEquals(1, actions.size());
		assertTrue(actions.get(0) instanceof ReactButtonControl);
	}

	/**
	 * Tests that a message given both fixed and as a function is reported as configuration error.
	 */
	public void testFixedAndComputedMessageIsAnError() {
		assertConfigurationError("<alert message-expr=\"'x'\"><message><en>x</en></message></alert>");
	}

	/**
	 * Tests that an alert without message is reported as configuration error.
	 */
	public void testMissingMessageIsAnError() {
		assertConfigurationError("<alert/>");
	}

	/**
	 * Tests that a title given both fixed and as a function is reported as configuration error.
	 */
	public void testFixedAndComputedTitleIsAnError() {
		assertConfigurationError(
			"<alert title-expr=\"'x'\" message-expr=\"'x'\"><title><en>x</en></title></alert>");
	}

	/**
	 * A closable alert over both input channels with a fixed message, whose dismiss actions write
	 * their input to {@link #DISMISSED_CHANNEL}.
	 */
	private ReactAlertControl closableAlert() throws ConfigurationException {
		return alert(
			"<alert inputs='state, other' closable='true'>"
				+ "<message><en>Fixed</en></message>"
				+ "<on-dismiss><write-channel name='" + DISMISSED_CHANNEL + "'/></on-dismiss>"
				+ "</alert>");
	}

	private void assertConfigurationError(String xml) {
		try {
			element(xml);
			fail("Configuration error expected: " + xml);
		} catch (ConfigurationException | AbortExecutionException ex) {
			// Expected: the element reports the error to its instantiation context.
		}
	}

	private ReactAlertControl alert(String xml) throws ConfigurationException {
		return (ReactAlertControl) element(xml).createControl(_context);
	}

	private static UIElement element(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestAlertElement.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"alert", TypedConfiguration.getConfigurationDescriptor(AlertElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml, "test-alert.view.xml"));
		AlertElement.Config config = (AlertElement.Config) reader.read();
		context.checkErrors();

		UIElement result = context.getInstance(config);
		context.checkErrors();
		return result;
	}

	private static Map<String, Object> state(String text, boolean show) {
		Map<String, Object> result = new HashMap<>();
		result.put(TEXT, text);
		result.put(SHOW, Boolean.valueOf(show));
		return result;
	}

	/**
	 * Lets the alert evaluate anew without a new channel value, as the change of an input object
	 * does.
	 */
	private static void reevaluate(ReactAlertControl alert) {
		alert.detach();
		alert.attach();
	}

	private static void dismiss(ReactAlertControl alert) {
		alert.executeCommand(ReactAlertControl.DISMISS_COMMAND,
			Map.of(DismissArguments.GENERATION, Integer.valueOf(alert.getGeneration())));
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The functions are TL-Script expressions, whose compilation and evaluation need the
	 *           application model and the {@link com.top_logic.knowledge.service.KnowledgeBase} they
	 *           are executed against; the alert takes its icons from the theme.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestAlertElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
