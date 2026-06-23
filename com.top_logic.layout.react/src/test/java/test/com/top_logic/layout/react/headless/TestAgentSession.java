/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.headless;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactCompositeControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.headless.AgentAction;
import com.top_logic.layout.react.headless.AgentNode;
import com.top_logic.layout.react.headless.AgentNodeView;
import com.top_logic.layout.react.headless.AgentParam;
import com.top_logic.layout.react.headless.AgentSession;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.listen.ModelScope;

/**
 * Demonstration of the headless agent interface: build a control tree, observe it as an addressable
 * state tree, act on it by semantic address, and observe the result.
 *
 * <p>
 * The scenario is a small login-style form ({@link DemoFormControl}) holding a text {@link
 * DemoFieldControl} (added as a composite child) and a {@link DemoButtonControl} (embedded under a
 * custom {@code "submit"} state key, to prove addressing is by semantic role/name, not by the
 * internal state key). The whole flow runs against a lightweight {@link TestReactContext} with no
 * application services, exactly like the browser's command path but with no browser.
 * </p>
 */
public class TestAgentSession extends TestCase {

	private SSEUpdateQueue _queue;

	private DemoFormControl _form;

	private DemoFieldControl _username;

	private DemoButtonControl _submit;

	private AgentSession _session;

	@Override
	protected void setUp() {
		_queue = new SSEUpdateQueue();
		TestReactContext context = new TestReactContext(_queue);

		_username = new DemoFieldControl(context, "username", "");
		_submit = new DemoButtonControl(context, "Submit");
		_form = new DemoFormControl(context, List.of(_username), _submit);

		_session = AgentSession.over(_queue);
	}

	/**
	 * The agent's first primitive: observe the screen as an addressable tree.
	 */
	public void testObserveProducesAddressedTree() {
		AgentNodeView root = _session.observe();

		// Print the observation so the demonstration is visible in the test log.
		System.out.println("=== Headless observation (initial) ===");
		System.out.println(root.toJson());

		assertEquals(AgentSession.ROOT, root.address());
		assertEquals("app", root.role());

		AgentNodeView form = single(root.children());
		assertEquals("/form", form.address());
		assertEquals("form", form.role());

		// Children are addressed by semantic role+name, independent of their internal state key
		// ("username" sits in the composite "children" list; "Submit" under a custom "submit" key).
		AgentNodeView field = childByAddress(form, "/form/field[username]");
		assertEquals("field", field.role());
		assertEquals("username", field.name());

		AgentNodeView button = childByAddress(form, "/form/button[Submit]");
		assertEquals("button", button.role());
		assertEquals("Submit", button.name());

		// The action space is advertised with an argument schema for the field's change command.
		AgentAction change = single(field.actions());
		assertEquals("change", change.command());
		assertEquals("value", single(change.params()).name());
		assertTrue(single(change.params()).required());
	}

	/**
	 * The full loop: observe, act by address, observe again and see the effect — using only semantic
	 * addresses, never the opaque control IDs.
	 */
	public void testActByAddressUpdatesObservableState() {
		// Type into the field.
		_session.act("/form/field[username]", "change", Map.of("value", "alice"));
		assertEquals("alice", _username.currentValue());

		// Click the button twice.
		_session.act("/form/button[Submit]", "click", Map.of());
		_session.act("/form/button[Submit]", "click", Map.of());
		assertEquals(2, _submit.clicks());

		// Re-observe: the new values are visible in the projected state.
		AgentNodeView root = _session.observe();
		System.out.println("=== Headless observation (after actions) ===");
		System.out.println(root.toJson());

		AgentNodeView form = single(root.children());
		assertEquals("alice", childByAddress(form, "/form/field[username]").state().get("value"));
		assertEquals(2.0, ((Number) childByAddress(form, "/form/button[Submit]").state().get("clicks"))
			.doubleValue(), 0.0);
	}

	/**
	 * The recorder angle: a captured user interaction is just a list of (address, command, arguments)
	 * steps, and replaying that list reproduces the outcome — the same substrate serves recording,
	 * replay (tests / data setup) and agent control.
	 */
	public void testRecordedStepsReplay() {
		List<Step> recording = List.of(
			new Step("/form/field[username]", "change", Map.of("value", "bob")),
			new Step("/form/button[Submit]", "click", Map.of()));

		for (Step step : recording) {
			_session.act(step.address(), step.command(), step.arguments());
		}

		assertEquals("bob", _username.currentValue());
		assertEquals(1, _submit.clicks());
	}

	/**
	 * Addressing an unknown node fails loudly with a helpful message rather than silently
	 * mis-targeting.
	 */
	public void testUnknownAddressFails() {
		try {
			_session.act("/form/button[Cancel]", "click", Map.of());
			fail("Expected failure for unknown address.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("button[Cancel]"));
		}
	}

	private static AgentNodeView childByAddress(AgentNodeView parent, String address) {
		for (AgentNodeView child : parent.children()) {
			if (child.address().equals(address)) {
				return child;
			}
		}
		throw new AssertionError("No child at " + address + " under " + parent.address());
	}

	private static <T> T single(List<T> list) {
		assertEquals("Expected exactly one element, got " + list, 1, list.size());
		return list.get(0);
	}

	/**
	 * A recorded interaction step.
	 *
	 * @param address
	 *        The semantic address of the target control.
	 * @param command
	 *        The command ID to invoke.
	 * @param arguments
	 *        The command arguments.
	 */
	private record Step(String address, String command, Map<String, Object> arguments) {
		// Pure data.
	}

	/**
	 * A minimal text field control advertising a {@code change} action with a typed argument.
	 */
	private static final class DemoFieldControl extends ReactControl implements AgentNode {

		private final String _fieldName;

		DemoFieldControl(ReactContext context, String fieldName, String initialValue) {
			super(context, fieldName, "TLTextInput");
			_fieldName = fieldName;
			putStateSilent("value", initialValue);
		}

		@Override
		public String agentRole() {
			return "field";
		}

		@Override
		public String agentName() {
			return _fieldName;
		}

		@Override
		public List<AgentAction> agentActions() {
			return List.of(new AgentAction("change", "Set the field value",
				List.of(AgentParam.requiredString("value", "The new text value."))));
		}

		@ReactCommand("change")
		void change(Map<String, Object> arguments) {
			putState("value", String.valueOf(arguments.get("value")));
		}

		String currentValue() {
			return (String) getState("value");
		}
	}

	/**
	 * A minimal button control advertising a {@code click} action and counting invocations.
	 */
	private static final class DemoButtonControl extends ReactControl implements AgentNode {

		private final String _label;

		private int _clicks;

		DemoButtonControl(ReactContext context, String label) {
			super(context, label, "TLButton");
			_label = label;
			putStateSilent("label", label);
			putStateSilent("clicks", Integer.valueOf(0));
		}

		@Override
		public String agentRole() {
			return "button";
		}

		@Override
		public String agentName() {
			return _label;
		}

		@Override
		public List<AgentAction> agentActions() {
			return List.of(AgentAction.of("click"));
		}

		@ReactCommand("click")
		void click() {
			_clicks++;
			putState("clicks", Integer.valueOf(_clicks));
		}

		int clicks() {
			return _clicks;
		}
	}

	/**
	 * A composite form holding its field as a composite child and its button under a custom state
	 * key, to demonstrate that addressing does not depend on the embedding key.
	 */
	private static final class DemoFormControl extends ReactCompositeControl {

		DemoFormControl(ReactContext context, List<? extends ReactControl> fields, ReactControl submit) {
			super(context, null, "TLForm", fields);
			putStateSilent("submit", submit);
		}
	}

	/**
	 * A minimal {@link ReactContext} backed only by an {@link SSEUpdateQueue}, with no application
	 * services.
	 */
	private static final class TestReactContext implements ReactContext {

		private final SSEUpdateQueue _sseQueue;

		TestReactContext(SSEUpdateQueue queue) {
			_sseQueue = queue;
		}

		@Override
		public String allocateId() {
			return _sseQueue.allocateId();
		}

		@Override
		public String getWindowName() {
			return "testWindow";
		}

		@Override
		public String getContextPath() {
			return "";
		}

		@Override
		public SSEUpdateQueue getSSEQueue() {
			return _sseQueue;
		}

		@Override
		public ReactWindowRegistry getWindowRegistry() {
			return null;
		}

		@Override
		public ModelScope getModelScope() {
			return null;
		}
	}
}
