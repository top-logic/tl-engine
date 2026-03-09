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
import java.util.function.Consumer;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.react.ReactDisplayContext;
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
 * {@link #writeAsChild(JsonWriter, ReactDisplayContext)} from {@link #writeJsonValue}. Each
 * control thus manages its own lifecycle, analogous to {@code child.write(context, out)} in
 * traditional controls.
 * </p>
 */
public class ReactControl implements HTMLFragment, IReactControl, ReactCommandTarget {

	/** State key for whether the control is hidden on the client. */
	private static final String HIDDEN = "hidden";

	private static final ConcurrentHashMap<Class<?>, ReactCommandMap> COMMAND_MAPS = new ConcurrentHashMap<>();

	private String _id;

	private final Object _model;

	private final String _reactModule;

	private Map<String, Object> _reactState;

	private SSEUpdateQueue _sseQueue;

	private ReactDisplayContext _viewContext;

	private ErrorSink _errorSink;

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
	public Map<String, Object> getReactState() {
		return _reactState;
	}

	/**
	 * The {@link ErrorSink} for reporting user-visible errors, or {@code null} in legacy mode.
	 */
	public ErrorSink getErrorSink() {
		return _errorSink;
	}

	/**
	 * The current {@link ReactDisplayContext}, or {@code null} if not yet rendered.
	 */
	protected ReactDisplayContext getViewContext() {
		return _viewContext;
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
		return invoker.invoke(this, _viewContext, arguments);
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		write(ReactDisplayContext.fromDisplayContext(context), out);
	}

	@Override
	public void write(ReactDisplayContext context, TagWriter out) throws IOException {
		_viewContext = context;
		_errorSink = context.getErrorSink();
		_id = context.allocateId();
		SSEUpdateQueue queue = context.getSSEQueue();
		_sseQueue = queue;
		queue.registerControl(this);

		onBeforeWrite(context);
		try {
			String stateJson = toJsonString(_reactState, context);

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
	 * (e.g. on the {@link ReactDisplayContext}) can be cleaned up in {@link #onAfterWrite}.
	 * </p>
	 *
	 * @see #onAfterWrite(ReactDisplayContext)
	 */
	protected void onBeforeWrite(ReactDisplayContext context) {
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
	 * @see #onBeforeWrite(ReactDisplayContext)
	 */
	protected void onAfterWrite(ReactDisplayContext context) {
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
			.setState(toJsonString(_reactState));
		queue.enqueue(event);
	}

	/**
	 * Applies a partial patch to the React state and sends a {@link PatchEvent} via SSE.
	 *
	 * <p>
	 * Any child {@link ReactControl}s in the patch are automatically initialized (ID assigned, SSE
	 * registered) during serialization if a {@link ReactDisplayContext} is available on this control.
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
			.setPatch(toJsonString(patch, _viewContext));
		queue.enqueue(event);
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
	 * @param writer
	 *        The JSON writer to write to.
	 * @param viewContext
	 *        The view display context for ID allocation and SSE registration, or {@code null} if
	 *        controls are already initialized.
	 */
	protected void writeAsChild(JsonWriter writer, ReactDisplayContext viewContext)
			throws IOException {
		if (viewContext != null && _id == null) {
			_viewContext = viewContext;
			_errorSink = viewContext.getErrorSink();
			_id = viewContext.allocateId();
			SSEUpdateQueue queue = viewContext.getSSEQueue();
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
		writeJsonMap(writer, getReactState(), viewContext);
		writer.endObject();
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
		_viewContext = null;
		_errorSink = null;
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
			if (_viewContext != null) {
				child._viewContext = _viewContext;
				child._errorSink = _viewContext.getErrorSink();
				child._id = _viewContext.allocateId();
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
	 * @param map
	 *        The map to serialize.
	 * @param viewContext
	 *        The view display context for ID allocation and SSE registration, or {@code null}.
	 */
	public static String toJsonString(Map<String, Object> map, ReactDisplayContext viewContext) {
		try {
			StringW sw = new StringW();
			try (JsonWriter writer = new JsonWriter(sw)) {
				writeJsonMap(writer, map, viewContext);
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
	public static void writeJsonLiteral(TagWriter out, Map<String, Object> map,
			ReactDisplayContext viewContext) throws IOException {
		out.append(toJsonString(map, viewContext));
	}

	static void writeJsonMap(JsonWriter writer, Map<String, Object> map,
			ReactDisplayContext viewContext) throws IOException {
		writer.beginObject();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			writer.name(entry.getKey());
			writeJsonValue(writer, entry.getValue(), viewContext);
		}
		writer.endObject();
	}

	@SuppressWarnings("unchecked")
	static void writeJsonValue(JsonWriter writer, Object value,
			ReactDisplayContext viewContext) throws IOException {
		if (value == null) {
			writer.nullValue();
		} else if (value instanceof String) {
			writer.value((String) value);
		} else if (value instanceof Number) {
			writer.value(((Number) value).doubleValue());
		} else if (value instanceof Boolean) {
			writer.value((Boolean) value);
		} else if (value instanceof Map) {
			writeJsonMap(writer, (Map<String, Object>) value, viewContext);
		} else if (value instanceof ReactControl) {
			((ReactControl) value).writeAsChild(writer, viewContext);
		} else if (value instanceof List) {
			writer.beginArray();
			for (Object element : (List<?>) value) {
				writeJsonValue(writer, element, viewContext);
			}
			writer.endArray();
		} else {
			writer.value(value.toString());
		}
	}

	// -- Backward-compatible no-context methods (used for patches/reconnection where controls are
	// already initialized) --

	/**
	 * Serializes a map to a JSON string.
	 */
	public static String toJsonString(Map<String, Object> map) {
		return toJsonString(map, (ReactDisplayContext) null);
	}

	/**
	 * Writes a JSON map literal directly to a {@link TagWriter}.
	 */
	public static void writeJsonLiteral(TagWriter out, Map<String, Object> map) throws IOException {
		writeJsonLiteral(out, map, null);
	}

	/**
	 * Writes a single value to JSON.
	 */
	@SuppressWarnings("unchecked")
	public static void writeJsonValue(JsonWriter writer, Object value) throws IOException {
		writeJsonValue(writer, value, null);
	}

	/**
	 * Recursively walks the given value and applies the action to every {@link ReactControl} found
	 * (as direct value, List element, or Map value).
	 */
	@SuppressWarnings("unchecked")
	protected static void forEachChildControl(Object value, Consumer<ReactControl> action) {
		if (value instanceof ReactControl) {
			action.accept((ReactControl) value);
		} else if (value instanceof Map) {
			for (Object entry : ((Map<?, ?>) value).values()) {
				forEachChildControl(entry, action);
			}
		} else if (value instanceof List) {
			for (Object element : (List<?>) value) {
				forEachChildControl(element, action);
			}
		}
	}

}
