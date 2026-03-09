/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.protocol.StateEvent;
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
 * Child {@link ReactControl}s embedded in the state are automatically initialized (ID assigned, SSE
 * registered) when they are serialized during the initial render. This is done by delegating to
 * {@link #writeAsChild(ReactContext, JsonWriter)} from {@link #writeJsonValue}. Each
 * control thus manages its own lifecycle, analogous to {@code child.write(context, out)} in
 * traditional controls.
 * </p>
 */
public class ReactControl implements HTMLFragment, IReactControl, ReactCommandTarget {

	/** State key for whether the control is hidden on the client. */
	private static final String HIDDEN = "hidden";

	private static final ConcurrentHashMap<Class<?>, ReactCommandMap> COMMAND_MAPS = new ConcurrentHashMap<>();

	private String _id;

	private ReactContext _reactContext;

	private final Object _model;

	private final String _reactModule;

	private Map<String, Object> _reactState;

	private SSEUpdateQueue _sseQueue;

	/**
	 * Creates a new {@link ReactControl}.
	 *
	 * @param model
	 *        The server-side model object.
	 * @param reactModule
	 *        The React module identifier to mount on the client (e.g. "TLTextInput").
	 */
	public ReactControl(Object model, String reactModule) {
		_model = model;
		_reactModule = reactModule;
		_reactState = new HashMap<>();
	}

	/**
	 * The <code>React</code> context.
	 */
	public ReactContext getReactContext() {
		return _reactContext;
	}

	@Override
	public String getID() {
		if (_id == null) {
			throw new IllegalStateException("Control has no ID. Call write() first.");
		}
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
	 * The current React state.
	 */
	protected final Map<String, Object> getReactState() {
		return _reactState;
	}

	/**
	 * The current {@link SSEUpdateQueue}, or {@code null} if not yet rendered.
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
		write(ReactContext.fromDisplayContext(context), out);
	}

	@Override
	public void write(ReactContext context, TagWriter out) throws IOException {
		_reactContext = context;
		_id = context.allocateId();
		SSEUpdateQueue queue = context.getSSEQueue();
		_sseQueue = queue;
		queue.registerControl(this);

		onBeforeWrite(context);
		try {
			String stateJson = toJsonString(context, _reactState);

			out.beginBeginTag(HTMLConstants.DIV);
			writeIdAttribute(out);
			writeControlClasses(out);
			out.writeAttribute("data-react-module", _reactModule);
			out.writeAttribute("data-react-state", stateJson);
			out.writeAttribute("data-window-name", context.getWindowName());
			out.writeAttribute("data-context-path", context.getContextPath());
			out.endBeginTag();
			out.endTag(HTMLConstants.DIV);
		} finally {
			onAfterWrite(context);
		}
	}

	/**
	 * Hook called before the control is rendered, after ID and SSE queue are assigned.
	 *
	 * <p>
	 * Subclasses override to perform initialization that must happen before rendering, such as
	 * registering model listeners or rebuilding state caches. Scoped resources installed here
	 * (e.g. on the {@link ReactContext}) can be cleaned up in {@link #onAfterWrite}.
	 * </p>
	 *
	 * @see #onAfterWrite(ReactContext)
	 */
	protected void onBeforeWrite(ReactContext context) {
		// Default: no-op.
	}

	/**
	 * Hook called after the control and all its children have been rendered.
	 *
	 * <p>
	 * Guaranteed to run even if rendering throws (called from a {@code finally} block). Subclasses
	 * override to restore scoped resources modified in {@link #onBeforeWrite}.
	 * </p>
	 *
	 * @see #onBeforeWrite(ReactContext)
	 */
	protected void onAfterWrite(ReactContext context) {
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
		if (_sseQueue != null) {
			patchReactState(java.util.Collections.singletonMap(key, value));
		} else {
			_reactState.put(key, value);
		}
	}

	/**
	 * Replaces the full React state and sends a {@link StateEvent} via the configured SSE queue.
	 *
	 * @param newState
	 *        The new state.
	 * @throws IllegalStateException
	 *         if no {@link SSEUpdateQueue} has been configured.
	 */
	public void setReactState(Map<String, Object> newState) {
		setReactState(requireSSEQueue(), newState);
	}

	/**
	 * Applies a partial patch to the React state and sends a {@link PatchEvent} via the configured
	 * SSE queue.
	 *
	 * @param patch
	 *        The partial state update.
	 * @throws IllegalStateException
	 *         if no {@link SSEUpdateQueue} has been configured.
	 */
	public void patchReactState(Map<String, Object> patch) {
		patchReactState(requireSSEQueue(), patch);
	}

	private SSEUpdateQueue requireSSEQueue() {
		if (_sseQueue == null) {
			throw new IllegalStateException("No SSEUpdateQueue configured on this ReactControl.");
		}
		return _sseQueue;
	}

	/**
	 * Replaces the full React state and sends a {@link StateEvent} via SSE.
	 *
	 * @param queue
	 *        The SSE queue to deliver the update to.
	 * @param newState
	 *        The new state.
	 */
	public void setReactState(SSEUpdateQueue queue, Map<String, Object> newState) {
		_reactState = new HashMap<>(newState);

		StateEvent event = StateEvent.create()
			.setControlId(getID())
			.setState(stateAsJSON());
		queue.enqueue(event);
	}

	/**
	 * Applies a partial patch to the React state and sends a {@link PatchEvent} via SSE.
	 *
	 * <p>
	 * Any child {@link ReactControl}s in the patch are automatically initialized (ID assigned, SSE
	 * registered) during serialization if a {@link ReactContext} is available on this control.
	 * </p>
	 *
	 * @param queue
	 *        The SSE queue to deliver the update to.
	 * @param patch
	 *        The partial state update.
	 */
	public void patchReactState(SSEUpdateQueue queue, Map<String, Object> patch) {
		_reactState.putAll(patch);

		PatchEvent event = PatchEvent.create()
			.setControlId(getID())
			.setPatch(toJsonString(_reactContext, patch));
		queue.enqueue(event);
	}

	/**
	 * Writes the <code>React</code> state to a JSON string.
	 */
	public final String stateAsJSON() {
		assert _reactContext != null : "Control not yet attached.";

		StringW buffer = new StringW();
		try {
			writeState(_reactContext, new JsonWriter(buffer));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return buffer.toString();
	}

	/**
	 * Writes this control as a child descriptor to JSON, ensuring it is properly initialized (ID
	 * assigned, SSE registered) before serialization.
	 *
	 * <p>
	 * Composite controls override this to prepare lazy children before serialization (e.g.,
	 * {@link com.top_logic.layout.react.control.layout.ReactDeckPaneControl} creates its active
	 * child).
	 * </p>
	 * 
	 * @param context
	 *        The view display context for ID allocation and SSE registration, or {@code null} if
	 *        controls are already initialized.
	 * @param writer
	 *        The JSON writer to write to.
	 */
	protected void writeAsChild(ReactContext context, JsonWriter writer)
			throws IOException {
		if (context != null && _id == null) {
			_reactContext = context;
			_id = context.allocateId();
			SSEUpdateQueue queue = context.getSSEQueue();
			if (_sseQueue == null) {
				_sseQueue = queue;
				queue.registerControl(this);
			}
		}

		writer.beginObject();
		writer.name("controlId");
		writer.value(getID());
		writer.name("module");
		writer.value(_reactModule);
		writer.name("state");
		writeState(context, writer);
		writer.endObject();
	}

	private void writeState(ReactContext context, JsonWriter writer) throws IOException {
		writeJsonMap(context, writer, getReactState());
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
		cleanupChildren();
		onCleanup();
		SSEUpdateQueue queue = _sseQueue;
		if (queue != null) {
			queue.unregisterControl(this);
			_sseQueue = null;
		}
		_reactContext = null;
	}

	/**
	 * Registers a dynamically created child {@link ReactControl} with this control's SSE queue so
	 * that it can receive state updates and dispatch commands.
	 *
	 * <p>
	 * If this control has no SSE queue yet (not rendered), the method is a no-op; the child will
	 * be registered when this control is written.
	 * </p>
	 *
	 * @param child
	 *        The child control to register.
	 */
	protected void registerChildControl(ReactControl child) {
		SSEUpdateQueue queue = _sseQueue;
		if (queue != null && child._sseQueue == null) {
			if (_reactContext != null) {
				child._reactContext = _reactContext;
				child._id = _reactContext.allocateId();
			}
			child._sseQueue = queue;
			queue.registerControl(child);
		}
	}

	/**
	 * Unregisters a dynamically created child {@link ReactControl} from this control's SSE queue.
	 *
	 * @param child
	 *        The child control to unregister.
	 */
	protected void unregisterChildControl(ReactControl child) {
		SSEUpdateQueue queue = _sseQueue;
		if (queue != null) {
			queue.unregisterControl(child);
			child._sseQueue = null;
		}
	}

	// -- Context-aware serialization methods --

	/**
	 * Serializes a map to a JSON string with context for child initialization.
	 * 
	 * @param context
	 *        The view display context for ID allocation and SSE registration, or {@code null}.
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
			((ReactControl) value).writeAsChild(context, writer);
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
