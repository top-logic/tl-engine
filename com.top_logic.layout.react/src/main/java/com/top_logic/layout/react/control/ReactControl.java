/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.protocol.PatchEvent;
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
 * {@link ReactCommand @ReactCommand}. See the annotation's documentation for the supported method
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
public class ReactControl implements HTMLFragment, IReactControl, ReactCommandTarget {

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

	private final List<Runnable> _attachListeners = new CopyOnWriteArrayList<>();

	private final List<Runnable> _detachListeners = new CopyOnWriteArrayList<>();

	/**
	 * Pending batch of state changes accumulated between {@link #beginUpdate()} and
	 * {@link #commitUpdate(Object)}. {@code null} when not batching. Also serves as the opaque
	 * token returned by {@link #beginUpdate()} for the outermost transaction.
	 */
	private Map<String, Object> _pendingPatch;

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
	 * Updates a state key on the server without sending an SSE event.
	 *
	 * <p>
	 * Use this for updates where the client already knows the new value (e.g. in command handlers
	 * that process client-initiated changes), or for deferred pre-render state updates that will be
	 * included in the initial render.
	 * </p>
	 *
	 * @param key
	 *        The state key.
	 * @param value
	 *        The new value.
	 */
	protected void putStateSilent(String key, Object value) {
		_reactState.put(key, value);
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

	@Override
	public HandlerResult executeCommand(String commandName, Map<String, Object> arguments) {
		ReactCommandMap commandMap = COMMAND_MAPS.computeIfAbsent(getClass(), ReactCommandMap::forClass);
		ReactCommandInvoker invoker = commandMap.get(commandName);
		if (invoker == null) {
			throw new IllegalArgumentException(
				"No @ReactCommand(\"" + commandName + "\") on " + getClass().getName());
		}
		return invoker.invoke(this, _reactContext, arguments);
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		write(out);
	}

	@Override
	public void write(TagWriter out) throws IOException {
		onBeforeWrite();
		_rendered = true;
		try {
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
	 * sent to the client. Otherwise the value is stored for inclusion in the initial render.
	 * </p>
	 *
	 * @param key
	 *        The state key.
	 * @param value
	 *        The state value.
	 */
	protected void putState(String key, Object value) {
		_reactState.put(key, value);
		if (_rendered) {
			if (_pendingPatch != null) {
				_pendingPatch.put(key, value);
			} else {
				sendPatch(java.util.Collections.singletonMap(key, value));
			}
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
	 * this method simply serializes the current state.
	 * </p>
	 *
	 * <p>
	 * Composite controls override this to prepare lazy children before serialization (e.g.,
	 * {@link com.top_logic.layout.react.control.layout.ReactDeckPaneControl} creates its active
	 * child).
	 * </p>
	 *
	 * @param writer
	 *        The JSON writer to write to.
	 */
	protected void writeAsChild(JsonWriter writer) throws IOException {
		onBeforeWrite();
		_rendered = true;
		writer.beginObject();
		writer.name("controlId");
		writer.value(getID());
		writer.name("module");
		writer.value(_reactModule);
		writer.name("state");
		writeState(writer);
		writer.endObject();
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
	 * Called after the attached flag is set but before attach listeners fire. Default does nothing.
	 * </p>
	 */
	protected void onAttach() {
		// Default: no-op.
	}

	/**
	 * Hook for subclasses to react to this control becoming detached (no longer displayed).
	 *
	 * <p>
	 * Called after the attached flag is cleared but before detach listeners fire. Default does
	 * nothing.
	 * </p>
	 */
	protected void onDetach() {
		// Default: no-op.
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
	 * Unregisters this control and all its children from SSE. Called during cleanup and when
	 * dynamically removing a child.
	 */
	public final void cleanupTree() {
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


	// -- Context-aware serialization methods --

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
