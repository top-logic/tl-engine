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
import com.top_logic.layout.react.headless.AgentTreeProjector;
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

		_session = AgentSession.forRoot(_form);
	}

	@Override
	protected void tearDown() {
		AgentTreeProjector.resetModelNaming();
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

	/**
	 * Model-derived addressing: a control bound to a business object (no {@link AgentNode}, no label
	 * state) is addressed by that object's label — the mechanism that gives table rows and list items
	 * stable, meaningful addresses.
	 */
	public void testModelDerivedAddressing() {
		Object rowModel = new Object();
		AgentTreeProjector.setModelNaming(model -> model == rowModel ? "Project Apollo" : null);

		SSEUpdateQueue queue = new SSEUpdateQueue();
		DemoModelControl row = new DemoModelControl(new TestReactContext(queue), rowModel);
		AgentSession session = AgentSession.forRoot(row);

		AgentNodeView rowView = single(session.observe().children());
		assertEquals("cell", rowView.role());
		assertEquals("Project Apollo", rowView.name());
		assertEquals("/cell[Project_Apollo]", rowView.address());

		// The model-derived address resolves and drives the control.
		session.act(rowView.address(), "open", Map.of());
		assertTrue(row.opened());
	}

	/**
	 * Structural pruning: a control that declares itself {@link ReactControl#agentTransparent()
	 * transparent} is elided from the projection and its children are lifted in its place — so the
	 * address skips layout scaffolding. The decision is polymorphic (per control), with no type
	 * switch in the projector.
	 */
	public void testStructuralWrapperIsElided() {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		TestReactContext context = new TestReactContext(queue);
		DemoButtonControl button = new DemoButtonControl(context, "Go");
		// Two nested transparent wrappers around the button.
		DemoWrapperControl wrapper = new DemoWrapperControl(context,
			new DemoWrapperControl(context, button));
		AgentSession session = AgentSession.forRoot(wrapper);

		AgentNodeView root = session.observe();
		AgentNodeView buttonView = single(root.children());
		assertEquals("button", buttonView.role());
		// Both wrappers elided: no "/stack/stack" prefix.
		assertEquals("/button[Go]", buttonView.address());

		// The address resolves and acts through the elided wrappers.
		session.act("/button[Go]", "click", Map.of());
		assertEquals(1, button.clicks());
	}

	/**
	 * Noise stripping: rendering-only state ({@code variant}), {@code null} state, and chrome
	 * commands ({@code toggleStyle}) are omitted from the projection, leaving only what an agent acts
	 * on. The control declares its own presentation keys / chrome commands; the projector is generic.
	 */
	public void testNoiseStripping() {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		DemoModelControl row = new DemoModelControl(new TestReactContext(queue), new Object());

		AgentNodeView view = single(AgentSession.forRoot(row).observe().children());

		// "variant" (presentation) and "note" (null) stripped; "value" (semantic) kept.
		assertEquals(java.util.Set.of("value"), view.state().keySet());

		// "toggleStyle" (chrome) hidden; "open" (semantic) advertised.
		assertEquals(List.of("open"),
			view.actions().stream().map(AgentAction::command).toList());
	}

	/**
	 * The affordance-first view: a flat list of only the actionable nodes (field + button), excluding
	 * the non-actionable form container and the synthetic root, and with no nested children.
	 */
	public void testActionsViewIsFlatAndActionableOnly() {
		List<AgentNodeView> nodes = _session.observe().actionableNodes();

		List<String> addresses = nodes.stream().map(AgentNodeView::address).sorted().toList();
		assertEquals(List.of("/form/button[Submit]", "/form/field[username]"), addresses);

		// Flat: serialized entries carry no children.
		for (AgentNodeView node : nodes) {
			assertFalse(node.toMap(false).containsKey("children"));
		}

		// The actions JSON wraps the flat entries and is acceptable to act() by the same addresses.
		System.out.println("=== Headless actions view ===");
		System.out.println(_session.observeActionsJson());
		_session.act("/form/button[Submit]", "click", Map.of());
		assertEquals(1, _submit.clicks());
	}

	/**
	 * A model whose label is only a default {@code toString()} ({@code Class@hashcode}) must not
	 * become a name: it is unstable (the hashcode varies per run) and meaningless as an address. The
	 * node falls back to a role-only address.
	 */
	public void testDefaultToStringModelLabelRejected() {
		Object model = new Object();
		AgentTreeProjector.setModelNaming(
			m -> m == model ? "com.top_logic.layout.view.form.AttributeSelectFieldModel@2c24205d" : null);

		SSEUpdateQueue queue = new SSEUpdateQueue();
		DemoPlainControl node = new DemoPlainControl(new TestReactContext(queue), model);

		AgentNodeView view = single(AgentSession.forRoot(node).observe().children());
		assertNull("Default toString must not be used as a name.", view.name());
		assertEquals("/select", view.address());

		// A genuine label containing '@' (e.g. an email) is still accepted.
		AgentTreeProjector.setModelNaming(m -> "user@example.com");
		AgentNodeView view2 = single(AgentSession.forRoot(
			new DemoPlainControl(new TestReactContext(new SSEUpdateQueue()), model)).observe().children());
		assertEquals("user@example.com", view2.name());
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
	 * A control bound to a business-object model, carrying neither {@link AgentNode} metadata nor
	 * label state, so its name must come from the model.
	 */
	private static final class DemoModelControl extends ReactControl {

		private boolean _opened;

		DemoModelControl(ReactContext context, Object model) {
			super(context, model, "TLCell");
			putStateSilent("value", "v1");
			putStateSilent("variant", "outlined");
			putStateSilent("note", null);
		}

		@ReactCommand("open")
		void open() {
			_opened = true;
		}

		@ReactCommand("toggleStyle")
		void toggleStyle() {
			// Chrome-only command.
		}

		@Override
		protected java.util.Set<String> agentPresentationKeys() {
			return java.util.Set.of("variant");
		}

		@Override
		protected java.util.Set<String> agentHiddenCommands() {
			return java.util.Set.of("toggleStyle");
		}

		boolean opened() {
			return _opened;
		}
	}

	/**
	 * A plain control bound to a model, with no {@link AgentNode} metadata and no label state, so its
	 * name (if any) comes solely from the model-naming strategy.
	 */
	private static final class DemoPlainControl extends ReactControl {

		DemoPlainControl(ReactContext context, Object model) {
			super(context, model, "TLSelect");
		}
	}

	/**
	 * A structural-only wrapper (like a layout stack) that elides itself from the agent projection.
	 */
	private static final class DemoWrapperControl extends ReactCompositeControl {

		DemoWrapperControl(ReactContext context, ReactControl child) {
			super(context, null, "TLStack", List.of(child));
		}

		@Override
		public boolean agentTransparent() {
			return true;
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
