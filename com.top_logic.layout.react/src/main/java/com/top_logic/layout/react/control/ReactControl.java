/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.protocol.StateEvent;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.routing.RoutingParticipant;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * A server-side control that mounts a {@code React} component on the client.
 *
 * <p>
 * The control renders a mount-point {@code <div>} and a bootstrap script that calls
 * {@code TLReact.mount()} on the client. State updates are delivered via SSE.
 * </p>
 *
 * <p>
 * Subclasses handle client-side commands by declaring methods annotated with
 * {@link ReactCommandHandler @ReactCommandHandler}. See the annotation's documentation for the supported method
 * signatures.
 * </p>
 *
 * <p>
 * The {@link ReactContext} is provided at construction time, so ID allocation and SSE registration
 * happen immediately. Child {@link ReactControl}s embedded in the state are automatically
 * serialized during the initial render by delegating to {@link #writeAsChild(JsonWriter)} from
 * {@link #writeJsonValue}. Each control thus manages its own lifecycle, analogous to
 * {@code child.write(context, out)} in traditional controls.
 * </p>
 */
public class ReactControl implements HTMLFragment, IReactControl, AgentControl {

	/** State key for whether the control is hidden on the client. */
	private static final String HIDDEN = "hidden";

	private static final ConcurrentHashMap<Class<?>, ReactCommandMap> COMMAND_MAPS = new ConcurrentHashMap<>();

	private final String _id;

	private final ReactContext _reactContext;

	private final Object _model;

	private final String _reactModule;

	private Map<String, Object> _reactState;

	private SSEUpdateQueue _sseQueue;

	/**
	 * Whether this control has been rendered (written to HTML or serialized as a child). Before
	 * rendering, {@link #putState} writes directly to the pre-render state map. After rendering,
	 * it sends SSE patch events to the client.
	 */
	private boolean _rendered;

	/**
	 * Whether this control is currently attached to the displayed UI tree.
	 */
	private boolean _attached;

	/**
	 * Whether this control has been disposed via {@link #cleanupTree()}.
	 *
	 * <p>
	 * A disposed control may still be reached within the running interaction: the very handler
	 * that triggered the disposal can continue on a stale reference (e.g. a selection handler
	 * whose channel write replaced the presentation containing its own control). The client has
	 * already unmounted the control at that point, so trailing state updates are dropped instead
	 * of failing.
	 * </p>
	 */
	private boolean _disposed;

	private final List<Runnable> _attachListeners = new CopyOnWriteArrayList<>();

	private final List<Runnable> _detachListeners = new CopyOnWriteArrayList<>();

	/**
	 * Pending batch of state changes accumulated between {@link #beginUpdate()} and
	 * {@link #commitUpdate(Object)}. {@code null} when not batching. Also serves as the opaque
	 * token returned by {@link #beginUpdate()} for the outermost transaction.
	 */
	private Map<String, Object> _pendingPatch;

	/**
	 * Whether the currently executing command was dispatched by the browser client (via
	 * {@link #executeClientCommand(String, Map)}), as opposed to programmatically (headless agent,
	 * script replay, server-side code).
	 */
	private boolean _clientDispatch;

	/**
	 * While {@code true}, {@link #putState(String, Object)} records state changes server-side
	 * without sending a patch event.
	 *
	 * @see #updateStateSilently(Runnable)
	 */
	private boolean _silentUpdates;

	/**
	 * Whether the state was changed without sending an event since the client last received the
	 * full state (render or state resend). Set by silent state writes; a programmatic command
	 * dispatch that leaves silent changes behind triggers a state resend, see
	 * {@link #executeCommand(String, Map)}.
	 */
	private boolean _silentChanges;

	private List<Runnable> _cleanupActions;

	private List<Runnable> _beforeWriteActions;

	/**
	 * Creates a new {@link ReactControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The server-side model object.
	 * @param reactModule
	 *        The React module identifier to mount on the client (e.g. "TLTextInput").
	 */
	public ReactControl(ReactContext context, Object model, String reactModule) {
		_reactContext = context;
		_model = model;
		_reactModule = reactModule;
		_reactState = new HashMap<>();
		_id = context.allocateId();
		_sseQueue = context.getSSEQueue();
		_sseQueue.registerControl(this);
	}

	/**
	 * The <code>React</code> context.
	 */
	public ReactContext getReactContext() {
		return _reactContext;
	}

	@Override
	public String getID() {
		return _id;
	}

	/**
	 * The server-side model object.
	 */
	public Object getModel() {
		return _model;
	}

	/**
	 * The React module identifier.
	 */
	public String getReactModule() {
		return _reactModule;
	}

	/**
	 * Returns the value of a single state key.
	 *
	 * @param key
	 *        The state key.
	 * @return The current value, or {@code null} if the key is not set.
	 */
	protected Object getState(String key) {
		return _reactState.get(key);
	}

	/**
	 * The current {@link SSEUpdateQueue}, or {@code null} if cleaned up.
	 */
	protected SSEUpdateQueue getSSEQueue() {
		return _sseQueue;
	}

	/**
	 * Whether this control is attached to an SSE queue (i.e., has been rendered).
	 *
	 * <p>
	 * Subclasses use this to decide whether state changes should produce a patch event or just
	 * update the pre-render state map.
	 * </p>
	 */
	protected boolean isSSEAttached() {
		return _sseQueue != null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Command handlers may record state changes silently on the assumption that the browser
	 * already shows them (see {@link #updateStateSilently(Runnable)}). That assumption only holds
	 * for {@link #executeClientCommand(String, Map) client dispatch}; here — where the browser has
	 * anticipated nothing — any silent changes the handler leaves behind are corrected by resending
	 * this control's full state after the command. Handlers therefore need no dispatch-origin
	 * logic of their own.
	 * </p>
	 */
	@Override
	public HandlerResult executeCommand(String commandName, Map<String, Object> arguments) {
		ReactCommandMap commandMap = COMMAND_MAPS.computeIfAbsent(getClass(), ReactCommandMap::forClass);
		ReactCommandInvoker invoker = commandMap.get(commandName);
		if (invoker == null) {
			throw new IllegalArgumentException(
				"No @ReactCommandHandler(\"" + commandName + "\") on " + getClass().getName());
		}
		HandlerResult result = invoker.invoke(this, _reactContext, arguments);
		if (!_clientDispatch && _silentChanges && _rendered && isSSEAttached()) {
			sendCurrentState();
		}
		return result;
	}

	@Override
	public final HandlerResult executeClientCommand(String commandName, Map<String, Object> arguments) {
		boolean before = _clientDispatch;
		_clientDispatch = true;
		try {
			return executeCommand(commandName, arguments);
		} finally {
			_clientDispatch = before;
		}
	}

	/**
	 * Sends this control's complete current state to the client, superseding any state the client
	 * holds.
	 */
	private void sendCurrentState() {
		_silentChanges = false;
		if (_disposed) {
			// The client has already unmounted this control; drop the trailing update.
			return;
		}
		StateEvent event = StateEvent.create()
			.setControlId(getID())
			.setState(stateAsJSON());
		requireSSEQueue().enqueue(event);
	}

	@Override
	public Set<String> commandNames() {
		return COMMAND_MAPS.computeIfAbsent(getClass(), ReactCommandMap::forClass).commandIds();
	}

	@Override
	public boolean agentTransparent() {
		return false;
	}

	@Override
	public String agentName() {
		return null;
	}

	@Override
	public String agentRole() {
		return null;
	}

	@Override
	public String agentChildSlot(ReactControl child) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implNote Walks the state map the same way {@link #writeJsonValue} serializes it, but stops at
	 *           each embedded control rather than descending into it. Entries are visited in
	 *           state-key order (the backing map is unordered) so the child order is stable across
	 *           calls.
	 */
	@Override
	public List<ReactControl> agentChildren() {
		List<ReactControl> result = new ArrayList<>();
		_reactState.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.forEach(entry -> collectChildControls(entry.getValue(), result));
		return result;
	}

	private static void collectChildControls(Object value, List<ReactControl> out) {
		if (value instanceof ReactControl child) {
			out.add(child);
		} else if (value instanceof Map<?, ?> map) {
			for (Object element : map.values()) {
				collectChildControls(element, out);
			}
		} else if (value instanceof List<?> list) {
			for (Object element : list) {
				collectChildControls(element, out);
			}
		}
	}

	@Override
	public Map<String, Object> agentScalarState() {
		Set<String> presentation = agentPresentationKeys();
		Map<String, Object> result = new LinkedHashMap<>();
		_reactState.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.forEach(entry -> {
				Object value = entry.getValue();
				if (value == null) {
					// No information for an agent.
					return;
				}
				if (presentation.contains(entry.getKey())) {
					// Rendering-only (e.g. padding, variant, css class).
					return;
				}
				if (containsControl(value)) {
					// Child controls are projected as separate addressable nodes.
					return;
				}
				result.put(entry.getKey(), value);
			});
		return result;
	}

	/**
	 * The names of state keys this control sets for rendering only, which the headless agent
	 * projection omits from {@link #agentScalarState()}.
	 *
	 * <p>
	 * Presentation properties (padding, variant, size, css class, …) carry no task-level meaning for
	 * an agent and only bloat the observation. A control declares its own such keys here; the
	 * projection stays generic and never enumerates keys per control type. {@code null}-valued entries
	 * are dropped automatically and need not be listed.
	 * </p>
	 *
	 * @return The rendering-only state keys; empty by default.
	 */
	protected Set<String> agentPresentationKeys() {
		return Set.of();
	}

	/**
	 * The names of commands this control handles that are UI chrome rather than meaningful agent
	 * actions (e.g. {@code toggleCollapse}, {@code reportDisplayClass}), and which the headless
	 * projection omits from a node's advertised actions.
	 *
	 * <p>
	 * Like {@link #agentPresentationKeys()}, each control declares its own; the projector subtracts
	 * these from the {@link #commandNames() command set} without switching on control types.
	 * </p>
	 *
	 * @return The chrome command names; empty by default.
	 */
	protected Set<String> agentHiddenCommands() {
		return Set.of();
	}

	/**
	 * Commands the script recorder must not capture because they are incidental UI adjustments
	 * (scrolling, column resizing) or chrome, not meaningful user intent worth replaying.
	 *
	 * <p>
	 * Defaults to the {@link #agentHiddenCommands() chrome commands} — what is not advertised to an
	 * agent is also not recorded. A control with transient view-only commands adds them (typically
	 * unioning with {@code super.nonRecordableCommands()}).
	 * </p>
	 *
	 * @return The non-recordable command names.
	 */
	public Set<String> nonRecordableCommands() {
		return effectiveChromeCommands();
	}

	/**
	 * The commands omitted from the agent action space and never recorded: the union of the
	 * {@link ReactCommandHandler#technical() technical}-flagged commands (co-located on the handler) and the
	 * manually declared {@link #agentHiddenCommands() chrome commands}.
	 */
	private Set<String> effectiveChromeCommands() {
		Set<String> technical = COMMAND_MAPS.computeIfAbsent(getClass(), ReactCommandMap::forClass).technicalCommands();
		Set<String> manual = agentHiddenCommands();
		if (technical.isEmpty()) {
			return manual;
		}
		if (manual.isEmpty()) {
			return technical;
		}
		Set<String> result = new LinkedHashSet<>(technical);
		result.addAll(manual);
		return result;
	}

	/**
	 * Whether the script recorder should capture the given command on this control.
	 *
	 * @param command
	 *        The dispatched command id.
	 * @return {@code false} for {@link #nonRecordableCommands() view-only/chrome} commands.
	 */
	public boolean isRecordable(String command) {
		return !nonRecordableCommands().contains(command);
	}

	@Override
	public Set<String> agentCommands() {
		Set<String> hidden = effectiveChromeCommands();
		if (hidden.isEmpty()) {
			return commandNames();
		}
		Set<String> result = new LinkedHashSet<>(commandNames());
		result.removeAll(hidden);
		return result;
	}

	@Override
	public ReactParam[] agentCommandParams(String command) {
		return COMMAND_MAPS.computeIfAbsent(getClass(), ReactCommandMap::forClass).paramsFor(command);
	}

	@Override
	public ConfigurationDescriptor agentCommandArgsType(String command) {
		return COMMAND_MAPS.computeIfAbsent(getClass(), ReactCommandMap::forClass).argTypeFor(command);
	}

	/**
	 * Binds the (client JSON) command arguments into a {@link ConfigurationItem} of the given
	 * descriptor, by re-serializing the map and reading it through {@link JsonConfigurationReader} so
	 * binding is by the JSON property names the schema advertises.
	 *
	 * @param descriptor
	 *        The target argument descriptor.
	 * @param arguments
	 *        The raw client arguments (may be {@code null}).
	 * @return The bound configuration item.
	 */
	static ConfigurationItem bindArguments(ConfigurationDescriptor descriptor, Map<String, Object> arguments)
			throws ConfigurationException {
		String json = JSON.toString(arguments != null ? arguments : Map.of());
		DefaultInstantiationContext context = new DefaultInstantiationContext(ReactControl.class);
		JsonConfigurationReader reader = new JsonConfigurationReader(context, descriptor);
		reader.setSource(CharacterContents.newContent(json));
		ConfigurationItem result = reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * Translates a just-dispatched command into the form the script recorder should capture.
	 *
	 * <p>
	 * The default records the command and arguments verbatim, as the {@link #commandItem(String, Map)
	 * typed item}. A control whose live arguments are session-bound — e.g. a select that sends
	 * allocated option ids — overrides this to emit a replay-stable form (business keys instead of
	 * ids, possibly a different command), so a recorded step resolves in a later session. Called
	 * before the command runs, with the same arguments.
	 * </p>
	 *
	 * @param command
	 *        The dispatched command id.
	 * @param arguments
	 *        The dispatched arguments.
	 * @return The command to record.
	 */
	public RecordedCommand recordCommand(String command, Map<String, Object> arguments) {
		return new RecordedCommand(commandItem(command, arguments));
	}

	/**
	 * The typed {@link ReactCommand} item representing a dispatch of the given command on this
	 * control: the arguments bound into the command's {@link #agentCommandArgsType(String) argument
	 * interface} (a bare {@link ReactCommand} for argument-less commands), with the {@link
	 * ReactCommand#getName() command name} set.
	 *
	 * @param command
	 *        The command id.
	 * @param arguments
	 *        The dispatched arguments (may be {@code null}).
	 * @return The command item, without an {@link ReactCommand#getAddress() address} (the recording
	 *         servlet fills it in).
	 */
	public final ReactCommand commandItem(String command, Map<String, Object> arguments) {
		ConfigurationDescriptor argType = agentCommandArgsType(command);
		ReactCommand item;
		if (argType != null) {
			try {
				item = (ReactCommand) bindArguments(argType, arguments);
			} catch (ConfigurationException ex) {
				throw new IllegalArgumentException("Arguments of command '" + command + "' on "
					+ getClass().getName() + " do not bind to "
					+ argType.getConfigurationInterface().getName() + ".", ex);
			}
		} else {
			item = TypedConfiguration.newConfigItem(ReactCommand.class);
		}
		item.setName(command);
		return item;
	}

	private static boolean containsControl(Object value) {
		if (value instanceof ReactControl) {
			return true;
		}
		if (value instanceof Map<?, ?> map) {
			for (Object element : map.values()) {
				if (containsControl(element)) {
					return true;
				}
			}
			return false;
		}
		if (value instanceof List<?> list) {
			for (Object element : list) {
				if (containsControl(element)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		write(out);
	}

	@Override
	public void write(TagWriter out) throws IOException {
		boolean silentBefore = _silentUpdates;
		// State written while producing the rendered output (e.g. lazily created children in an
		// onBeforeWrite hook) reaches the client as part of that output — no events for it.
		_silentUpdates = true;
		try {
			onBeforeWrite();
			_rendered = true;
			// The full state is serialized below; nothing is pending on the client side anymore.
			_silentChanges = false;
			String stateJson = toJsonString(_reactContext, _reactState);

			out.beginBeginTag(HTMLConstants.DIV);
			writeIdAttribute(out);
			writeControlClasses(out);
			out.writeAttribute("data-react-module", _reactModule);
			out.writeAttribute("data-react-state", stateJson);
			out.writeAttribute("data-window-name", _reactContext.getWindowName());
			out.writeAttribute("data-context-path", _reactContext.getContextPath());
			out.endBeginTag();
			out.endTag(HTMLConstants.DIV);
		} finally {
			_silentUpdates = silentBefore;
			onAfterWrite();
		}
	}

	/**
	 * Hook called before the control is rendered.
	 *
	 * <p>
	 * Subclasses override to perform initialization that must happen before rendering, such as
	 * registering model listeners or rebuilding state caches. Scoped resources installed here can be
	 * cleaned up in {@link #onAfterWrite()}.
	 * </p>
	 *
	 * @see #onAfterWrite()
	 */
	protected void onBeforeWrite() {
		if (_beforeWriteActions != null) {
			_beforeWriteActions.forEach(Runnable::run);
			_beforeWriteActions = null;
		}
	}

	/**
	 * Hook called after the control and all its children have been rendered.
	 *
	 * <p>
	 * Guaranteed to run even if rendering throws (called from a {@code finally} block). Subclasses
	 * override to restore scoped resources modified in {@link #onBeforeWrite()}.
	 * </p>
	 *
	 * @see #onBeforeWrite()
	 */
	protected void onAfterWrite() {
		// Default: no-op.
	}

	/**
	 * Called when this control is removed from its SSE queue.
	 *
	 * <p>
	 * Subclasses override to release model listeners or other resources.
	 * </p>
	 */
	protected void onCleanup() {
		// Default: no-op. Subclasses override.
	}

	/**
	 * Registers an action to run when this control is cleaned up.
	 *
	 * <p>
	 * Use this to register external resource cleanup (e.g. detaching a model from a channel) that
	 * cannot be handled by a subclass override of {@link #onCleanup()}.
	 * </p>
	 *
	 * @param action
	 *        The cleanup action to register.
	 */
	public void addCleanupAction(Runnable action) {
		if (_cleanupActions == null) {
			_cleanupActions = new ArrayList<>();
		}
		_cleanupActions.add(action);
	}

	/**
	 * Registers an action to run once before this control is first rendered.
	 *
	 * <p>
	 * Use this to defer resource-intensive setup (e.g. registering model listeners) until the
	 * control is actually displayed. Actions run during {@link #onBeforeWrite()} and are
	 * discarded afterwards.
	 * </p>
	 *
	 * @param action
	 *        The action to run before first render.
	 */
	public void addBeforeWriteAction(Runnable action) {
		if (_beforeWriteActions == null) {
			_beforeWriteActions = new ArrayList<>();
		}
		_beforeWriteActions.add(action);
	}

	/**
	 * Writes the control ID as an HTML attribute.
	 */
	protected void writeIdAttribute(TagWriter out) throws IOException {
		out.writeAttribute("id", _id);
	}

	/**
	 * Writes CSS classes for this control.
	 */
	protected void writeControlClasses(TagWriter out) throws IOException {
		out.beginCssClasses();
		out.append("is-control");
		out.endCssClasses();
	}

	/**
	 * Sets whether this control is hidden on the client.
	 *
	 * <p>
	 * When hidden, the mount-point element gets {@code display:none} applied by the bridge,
	 * preserving the React component tree and its local state.
	 * </p>
	 *
	 * @param hidden
	 *        {@code true} to hide, {@code false} to show.
	 */
	public void setHidden(boolean hidden) {
		putState(HIDDEN, Boolean.valueOf(hidden));
	}

	/**
	 * Sets a single value in the React state.
	 *
	 * <p>
	 * If this control is already attached to an SSE queue (i.e. rendered), a {@link PatchEvent} is
	 * sent to the client. Before rendering, the value simply becomes part of the initial render.
	 * Within {@link #updateStateSilently(Runnable)} — and during state serialization, where the
	 * written state reaches the client as part of the rendered output — the change is recorded
	 * without an event.
	 * </p>
	 *
	 * @param key
	 *        The state key.
	 * @param value
	 *        The state value.
	 */
	protected void putState(String key, Object value) {
		_reactState.put(key, value);
		if (_silentUpdates) {
			_silentChanges = true;
			return;
		}
		if (_rendered) {
			if (_pendingPatch != null) {
				_pendingPatch.put(key, value);
			} else {
				sendPatch(java.util.Collections.singletonMap(key, value));
			}
		}
	}

	/**
	 * Runs the given state update without sending events: changes are recorded in the server-side
	 * state (so later renders and agent observations see them) but no patch event is sent.
	 *
	 * <p>
	 * Use this when the client already holds the resulting state because the change being
	 * processed originates from the client itself — e.g. to run the regular model-change reaction
	 * after applying a value the client sent. Echoing such state back would be redundant, and for
	 * an input being typed into harmful (a late echo overwrites newer keystrokes).
	 * </p>
	 *
	 * <p>
	 * The client-already-holds assumption is only valid for commands the browser itself
	 * dispatched. When the same handler runs programmatically (script replay, headless agent), the
	 * framework corrects the omission by resending the control's state after the command — see
	 * {@link #executeCommand(String, Map)}. Handlers need not distinguish the two cases.
	 * </p>
	 *
	 * @param update
	 *        The state update to run silently.
	 */
	protected final void updateStateSilently(Runnable update) {
		boolean before = _silentUpdates;
		_silentUpdates = true;
		try {
			update.run();
		} finally {
			_silentUpdates = before;
		}
	}

	/**
	 * Begins a batched state update.
	 *
	 * <p>
	 * All {@link #putState} calls between {@code beginUpdate()} and {@link #commitUpdate(Object)}
	 * are collected and sent as a single SSE {@link PatchEvent}. This avoids sending multiple
	 * events when several state properties need to change atomically.
	 * </p>
	 *
	 * <p>
	 * Calls may be nested: only the outermost {@code beginUpdate}/{@code commitUpdate} pair
	 * triggers the SSE event. Inner pairs are no-ops, so methods that batch internally can be
	 * called from within an outer batch without double-sending.
	 * </p>
	 *
	 * @return An opaque token that must be passed to {@link #commitUpdate(Object)}.
	 *
	 * @see #commitUpdate(Object)
	 */
	protected Object beginUpdate() {
		if (_pendingPatch != null) {
			// Nested: return null so commitUpdate knows this is an inner call.
			return null;
		}
		_pendingPatch = new HashMap<>();
		return _pendingPatch;
	}

	/**
	 * Commits a batched state update started by {@link #beginUpdate()}.
	 *
	 * <p>
	 * Sends all accumulated state changes as a single {@link PatchEvent} to the client. Only the
	 * outermost commit (matching the outermost {@link #beginUpdate()}) actually sends; inner
	 * commits are no-ops.
	 * </p>
	 *
	 * @param token
	 *        The token returned by the corresponding {@link #beginUpdate()} call.
	 *
	 * @see #beginUpdate()
	 */
	protected void commitUpdate(Object token) {
		if (token != _pendingPatch) {
			// Inner (nested) commit: nothing to do.
			return;
		}
		Map<String, Object> patch = _pendingPatch;
		_pendingPatch = null;
		if (!patch.isEmpty() && _rendered) {
			sendPatch(patch);
		}
	}

	private void sendPatch(Map<String, Object> patch) {
		if (_disposed) {
			// The client has already unmounted this control; drop the trailing update.
			return;
		}
		PatchEvent event = PatchEvent.create()
			.setControlId(getID())
			.setPatch(toJsonString(_reactContext, patch));
		requireSSEQueue().enqueue(event);
	}

	private SSEUpdateQueue requireSSEQueue() {
		if (_sseQueue == null) {
			throw new IllegalStateException("No SSEUpdateQueue configured on this ReactControl.");
		}
		return _sseQueue;
	}

	/**
	 * Writes the <code>React</code> state to a JSON string.
	 */
	public final String stateAsJSON() {
		StringW buffer = new StringW();
		try {
			writeState(new JsonWriter(buffer));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return buffer.toString();
	}

	/**
	 * Writes this control as a child descriptor to JSON.
	 *
	 * <p>
	 * Since the control is fully initialized at construction time (ID assigned, SSE registered),
	 * this method simply serializes the current state. Composite controls that create children
	 * lazily do so in an {@link #onBeforeWrite()} hook (e.g.
	 * {@link com.top_logic.layout.react.control.layout.ReactDeckPaneControl} creates its active
	 * child).
	 * </p>
	 *
	 * @param writer
	 *        The JSON writer to write to.
	 */
	protected final void writeAsChild(JsonWriter writer) throws IOException {
		boolean silentBefore = _silentUpdates;
		// State written while producing the rendered output (e.g. lazily created children in an
		// onBeforeWrite hook) reaches the client as part of that output — no events for it.
		_silentUpdates = true;
		try {
			onBeforeWrite();
			_rendered = true;
			// The full state is serialized below; nothing is pending on the client side anymore.
			_silentChanges = false;
			writer.beginObject();
			writer.name("controlId");
			writer.value(getID());
			writer.name("module");
			writer.value(_reactModule);
			writer.name("state");
			writeState(writer);
			writer.endObject();
		} finally {
			_silentUpdates = silentBefore;
		}
	}

	private void writeState(JsonWriter writer) throws IOException {
		writeJsonMap(_reactContext, writer, _reactState);
	}

	/**
	 * Whether this control is currently attached to the displayed UI tree.
	 *
	 * <p>
	 * A control is attached between calls to {@link #attach()} and {@link #detach()}. It is
	 * detached while it exists in memory but is not currently displayed (e.g. the inactive child
	 * of a one-of-N container like a sidebar, tab-bar or deck-pane). Final disposal is signaled
	 * separately by {@link #cleanupTree()}.
	 * </p>
	 */
	public final boolean isAttached() {
		return _attached;
	}

	/**
	 * Marks this control as attached (part of the displayed tree) and fires
	 * {@link #addAttachListener(Runnable) attach listeners}.
	 *
	 * <p>
	 * Idempotent: if already attached, this call is a no-op.
	 * </p>
	 */
	public final void attach() {
		if (_attached) {
			return;
		}
		_attached = true;
		onAttach();
		for (Runnable l : _attachListeners) {
			l.run();
		}
		propagateAttach();
	}

	/**
	 * Marks this control as detached (still in memory but not displayed) and fires
	 * {@link #addDetachListener(Runnable) detach listeners}.
	 *
	 * <p>
	 * Idempotent: if not attached, this call is a no-op.
	 * </p>
	 */
	public final void detach() {
		if (!_attached) {
			return;
		}
		propagateDetach();
		_attached = false;
		onDetach();
		for (Runnable l : _detachListeners) {
			l.run();
		}
	}

	/**
	 * Hook for subclasses to react to this control becoming attached (displayed).
	 *
	 * <p>
	 * Called after the attached flag is set but before attach listeners fire. If this control
	 * implements {@link RoutingParticipant}, it is automatically registered with the
	 * {@link RouteManager}.
	 * </p>
	 */
	protected void onAttach() {
		if (this instanceof RoutingParticipant rp) {
			RouteManager rm = _reactContext.getRouteManager();
			if (rm != null) {
				rm.register(rp);
			}
		}
	}

	/**
	 * Hook for subclasses to react to this control becoming detached (no longer displayed).
	 *
	 * <p>
	 * Called after the attached flag is cleared but before detach listeners fire. If this control
	 * implements {@link RoutingParticipant}, it is automatically unregistered from the
	 * {@link RouteManager}.
	 * </p>
	 */
	protected void onDetach() {
		if (this instanceof RoutingParticipant rp) {
			RouteManager rm = _reactContext.getRouteManager();
			if (rm != null) {
				rm.unregister(rp);
			}
		}
	}

	/**
	 * Registers a listener that fires whenever this control becomes attached.
	 *
	 * <p>
	 * If this control is already attached at registration time, the listener is invoked
	 * immediately to simplify late registrations.
	 * </p>
	 */
	public final void addAttachListener(Runnable listener) {
		_attachListeners.add(listener);
		if (_attached) {
			listener.run();
		}
	}

	/**
	 * Unregisters a previously added attach listener.
	 */
	public final void removeAttachListener(Runnable listener) {
		_attachListeners.remove(listener);
	}

	/**
	 * Registers a listener that fires whenever this control becomes detached.
	 */
	public final void addDetachListener(Runnable listener) {
		_detachListeners.add(listener);
	}

	/**
	 * Unregisters a previously added detach listener.
	 */
	public final void removeDetachListener(Runnable listener) {
		_detachListeners.remove(listener);
	}

	/**
	 * Hook for subclasses to propagate an {@link #attach()} call to their currently displayed
	 * children. The default does nothing.
	 */
	protected void propagateAttach() {
		// Default: no children to propagate to.
	}

	/**
	 * Hook for subclasses to propagate a {@link #detach()} call to their currently displayed
	 * children. The default does nothing.
	 */
	protected void propagateDetach() {
		// Default: no children to propagate to.
	}

	/**
	 * Hook for composite controls to clean up their children during detach.
	 *
	 * <p>
	 * Composite controls override this to call {@link #cleanupTree()} on each of their children.
	 * The default implementation does nothing (leaf controls have no children).
	 * </p>
	 */
	protected void cleanupChildren() {
		// Leaf controls have no children.
	}

	/**
	 * Disposes this control and all its children: detaches the tree, runs cleanup actions, and
	 * unregisters from SSE. Called during cleanup and when dynamically removing a child.
	 *
	 * <p>
	 * A disposed control tolerates trailing state updates: {@link #putState(String, Object)} and
	 * state resends become no-ops instead of failing. This is required because stale references can
	 * legitimately reach the control within the running interaction — the very handler that
	 * triggered the disposal may continue on its own control (e.g. a selection handler whose
	 * channel write replaced the presentation containing it), and observer notifications iterating
	 * a listener snapshot may still deliver one event after the disposal.
	 * </p>
	 *
	 * <p>
	 * Callers replacing a child subtree from <em>inside</em> an observer notification must not call
	 * this method synchronously if the old subtree observes the same source; they defer the call
	 * until the notification has unwound (view channels provide
	 * {@code ChannelNotificationScope.current().afterNotification(old::cleanupTree)} for this).
	 * </p>
	 */
	public final void cleanupTree() {
		_disposed = true;
		detach();
		cleanupChildren();
		onCleanup();
		if (_cleanupActions != null) {
			_cleanupActions.forEach(Runnable::run);
			_cleanupActions = null;
		}
		SSEUpdateQueue queue = _sseQueue;
		if (queue != null) {
			queue.unregisterControl(this);
			_sseQueue = null;
		}
	}

	/**
	 * Registers a dynamically created child {@link ReactControl} with this control's SSE queue so
	 * that it can receive state updates and dispatch commands.
	 *
	 * <p>
	 * Since the child already has its context and ID from construction, this method only ensures
	 * the child is registered with the parent's SSE queue if it is not already.
	 * </p>
	 *
	 * @param child
	 *        The child control to register.
	 */
	protected void registerChildControl(ReactControl child) {
		SSEUpdateQueue queue = _sseQueue;
		if (queue != null && child._sseQueue == null) {
			child._sseQueue = queue;
			queue.registerControl(child);
		}
	}


	/**
	 * Serializes a map to a JSON string with context for child initialization.
	 *
	 * @param context
	 *        The view display context, or {@code null}.
	 * @param map
	 *        The map to serialize.
	 */
	public static String toJsonString(ReactContext context, Map<String, Object> map) {
		try {
			StringW sw = new StringW();
			try (JsonWriter writer = new JsonWriter(sw)) {
				writeJsonMap(context, writer, map);
			}
			return sw.toString();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Writes a JSON map literal directly to a {@link TagWriter} with context for child
	 * initialization.
	 */
	public static void writeJsonLiteral(ReactContext context, TagWriter out,
			Map<String, Object> map) throws IOException {
		out.append(toJsonString(context, map));
	}

	static void writeJsonMap(ReactContext context, JsonWriter writer,
			Map<String, Object> map) throws IOException {
		writer.beginObject();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			writer.name(entry.getKey());
			writeJsonValue(context, writer, entry.getValue());
		}
		writer.endObject();
	}

	@SuppressWarnings("unchecked")
	static void writeJsonValue(ReactContext context, JsonWriter writer,
			Object value) throws IOException {
		if (value == null) {
			writer.nullValue();
		} else if (value instanceof String) {
			writer.value((String) value);
		} else if (value instanceof Number) {
			writer.value(((Number) value).doubleValue());
		} else if (value instanceof Boolean) {
			writer.value((Boolean) value);
		} else if (value instanceof Map) {
			writeJsonMap(context, writer, (Map<String, Object>) value);
		} else if (value instanceof ReactControl) {
			((ReactControl) value).writeAsChild(writer);
		} else if (value instanceof List) {
			writer.beginArray();
			for (Object element : (List<?>) value) {
				writeJsonValue(context, writer, element);
			}
			writer.endArray();
		} else {
			writer.value(value.toString());
		}
	}

}
