/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.protocol.StateEvent;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * A server-side control that mounts a React component on the client.
 *
 * <p>
 * The control renders a mount-point {@code <div>} and a bootstrap script that calls
 * {@code TLReact.mount()} on the client. State updates are delivered via SSE.
 * </p>
 *
 * <p>
 * Child {@link ReactControl}s embedded in the state are automatically initialized (ID assigned, SSE
 * registered) when they are serialized during the initial render. This is done by delegating to
 * {@link #writeAsChild(JsonWriter, FrameScope, SSEUpdateQueue)} from {@link #writeJsonValue}. Each
 * control thus manages its own lifecycle, analogous to {@code child.write(context, out)} in
 * traditional controls.
 * </p>
 */
public class ReactControl extends AbstractVisibleControl {

	/** State key for whether the control is hidden on the client. */
	private static final String HIDDEN = "hidden";

	private final Object _model;

	private final String _reactModule;

	private Map<String, Object> _reactState;

	private SSEUpdateQueue _sseQueue;

	private FrameScope _frameScope;

	/**
	 * Creates a new {@link ReactControl}.
	 *
	 * @param model
	 *        The server-side model object.
	 * @param reactModule
	 *        The React module identifier to mount on the client (e.g. "TLTextInput").
	 * @param commands
	 *        The commands this control supports.
	 */
	public ReactControl(Object model, String reactModule, Map<String, ControlCommand> commands) {
		super(commands);
		_model = model;
		_reactModule = reactModule;
		_reactState = new HashMap<>();
	}

	/**
	 * Creates a new {@link ReactControl} with no commands.
	 */
	public ReactControl(Object model, String reactModule) {
		this(model, reactModule, Collections.emptyMap());
	}

	@Override
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
			patchReactState(Collections.singletonMap(key, value));
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
	 * registered) during serialization if a {@link FrameScope} and {@link SSEUpdateQueue} are
	 * available on this control.
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
			.setPatch(toJsonString(patch, _frameScope, queue));
		queue.enqueue(event);
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		FrameScope frameScope = getScope().getFrameScope();
		SSEUpdateQueue queue = SSEUpdateQueue.forSession(context.asRequest().getSession());

		// Initialize THIS control (the root). Children auto-initialize during serialization.
		_frameScope = frameScope;
		fetchID(frameScope);
		_sseQueue = queue;
		queue.registerControl(this);

		out.beginBeginTag(HTMLConstants.DIV);
		writeControlAttributes(context, out);
		out.writeAttribute("data-react-module", _reactModule);
		out.writeAttribute("data-react-state", toJsonString(_reactState, frameScope, queue));
		out.endBeginTag();
		out.endTag(HTMLConstants.DIV);

		// Resolve the window name so the client can send it back with command requests.
		String windowName = context.getLayoutContext().getWindowId().getWindowName();
		String contextPath = context.getContextPath();

		HTMLUtil.beginScriptAfterRendering(out);
		out.append("TLReact.mount('");
		out.append(getID());
		out.append("', '");
		out.append(_reactModule);
		out.append("', ");
		writeJsonLiteral(out, _reactState, frameScope, queue);
		out.append(", '");
		out.append(windowName);
		out.append("', '");
		out.append(contextPath);
		out.append("');");
		HTMLUtil.endScriptAfterRendering(out);
	}

	@Override
	protected void internalDetach() {
		cleanupTree();
		super.internalDetach();
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Updates are delivered via SSE, not through the standard revalidation path.
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
	 * @param frameScope
	 *        The frame scope for ID assignment, or {@code null} if controls are already
	 *        initialized.
	 * @param queue
	 *        The SSE queue for registration, or {@code null} if controls are already registered.
	 */
	protected void writeAsChild(JsonWriter writer, FrameScope frameScope, SSEUpdateQueue queue)
			throws IOException {
		if (frameScope != null) {
			_frameScope = frameScope;
			fetchID(frameScope);
		}
		if (queue != null && _sseQueue == null) {
			_sseQueue = queue;
			queue.registerControl(this);
		}

		writer.beginObject();
		writer.name("controlId");
		writer.value(getID());
		writer.name("module");
		writer.value(_reactModule);
		writer.name("state");
		writeJsonMap(writer, getReactState(), frameScope, queue);
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
	 * Unregisters this control and all its children from SSE. Called during
	 * {@link #internalDetach()} and when dynamically removing a child.
	 */
	public final void cleanupTree() {
		cleanupChildren();
		SSEUpdateQueue queue = _sseQueue;
		if (queue != null) {
			queue.unregisterControl(this);
			_sseQueue = null;
		}
		_frameScope = null;
	}

	// -- Context-aware serialization methods --

	/**
	 * Serializes a map to a JSON string with context for child initialization.
	 *
	 * @param map
	 *        The map to serialize.
	 * @param frameScope
	 *        The frame scope for ID assignment, or {@code null}.
	 * @param queue
	 *        The SSE queue for registration, or {@code null}.
	 */
	public static String toJsonString(Map<String, Object> map, FrameScope frameScope, SSEUpdateQueue queue) {
		try {
			StringW sw = new StringW();
			try (JsonWriter writer = new JsonWriter(sw)) {
				writeJsonMap(writer, map, frameScope, queue);
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
	public static void writeJsonLiteral(TagWriter out, Map<String, Object> map, FrameScope frameScope,
			SSEUpdateQueue queue) throws IOException {
		out.append(toJsonString(map, frameScope, queue));
	}

	static void writeJsonMap(JsonWriter writer, Map<String, Object> map, FrameScope frameScope,
			SSEUpdateQueue queue) throws IOException {
		writer.beginObject();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			writer.name(entry.getKey());
			writeJsonValue(writer, entry.getValue(), frameScope, queue);
		}
		writer.endObject();
	}

	@SuppressWarnings("unchecked")
	static void writeJsonValue(JsonWriter writer, Object value, FrameScope frameScope,
			SSEUpdateQueue queue) throws IOException {
		if (value == null) {
			writer.nullValue();
		} else if (value instanceof String) {
			writer.value((String) value);
		} else if (value instanceof Number) {
			writer.value(((Number) value).doubleValue());
		} else if (value instanceof Boolean) {
			writer.value((Boolean) value);
		} else if (value instanceof Map) {
			writeJsonMap(writer, (Map<String, Object>) value, frameScope, queue);
		} else if (value instanceof ReactControl) {
			((ReactControl) value).writeAsChild(writer, frameScope, queue);
		} else if (value instanceof List) {
			writer.beginArray();
			for (Object element : (List<?>) value) {
				writeJsonValue(writer, element, frameScope, queue);
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
		return toJsonString(map, null, null);
	}

	/**
	 * Writes a JSON map literal directly to a {@link TagWriter}.
	 */
	public static void writeJsonLiteral(TagWriter out, Map<String, Object> map) throws IOException {
		writeJsonLiteral(out, map, null, null);
	}

	/**
	 * Writes a single value to JSON.
	 */
	@SuppressWarnings("unchecked")
	public static void writeJsonValue(JsonWriter writer, Object value) throws IOException {
		writeJsonValue(writer, value, null, null);
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
			if (_frameScope != null) {
				child._frameScope = _frameScope;
				child.fetchID(_frameScope);
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
