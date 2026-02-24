/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
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
 */
public class ReactControl extends AbstractVisibleControl {

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
	 * @param queue
	 *        The SSE queue to deliver the update to.
	 * @param patch
	 *        The partial state update.
	 */
	public void patchReactState(SSEUpdateQueue queue, Map<String, Object> patch) {
		_reactState.putAll(patch);

		PatchEvent event = PatchEvent.create()
			.setControlId(getID())
			.setPatch(toJsonString(patch));
		queue.enqueue(event);
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(HTMLConstants.DIV);
		writeControlAttributes(context, out);
		out.writeAttribute("data-react-module", _reactModule);
		out.writeAttribute("data-react-state", toJsonString(_reactState));
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
		writeJsonLiteral(out, _reactState);
		out.append(", '");
		out.append(windowName);
		out.append("', '");
		out.append(contextPath);
		out.append("');");
		HTMLUtil.endScriptAfterRendering(out);

		// Retrieve the SSE queue from the session and register this control for command dispatch.
		SSEUpdateQueue queue = SSEUpdateQueue.forSession(context.asRequest().getSession());
		_sseQueue = queue;
		queue.registerControl(this);
	}

	@Override
	protected void detachInvalidated() {
		SSEUpdateQueue queue = _sseQueue;
		if (queue != null) {
			queue.unregisterControl(this);
			_sseQueue = null;
		}
		super.detachInvalidated();
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Updates are delivered via SSE, not through the standard revalidation path.
	}

	/**
	 * Serializes a map to a JSON string.
	 */
	static String toJsonString(Map<String, Object> map) {
		try {
			StringW sw = new StringW();
			try (JsonWriter writer = new JsonWriter(sw)) {
				writeJsonMap(writer, map);
			}
			return sw.toString();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Writes a JSON map literal directly to a {@link TagWriter}.
	 */
	static void writeJsonLiteral(TagWriter out, Map<String, Object> map) throws IOException {
		out.append(toJsonString(map));
	}

	@SuppressWarnings("unchecked")
	private static void writeJsonMap(JsonWriter writer, Map<String, Object> map) throws IOException {
		writer.beginObject();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			writer.name(entry.getKey());
			writeJsonValue(writer, entry.getValue());
		}
		writer.endObject();
	}

	@SuppressWarnings("unchecked")
	static void writeJsonValue(JsonWriter writer, Object value) throws IOException {
		if (value == null) {
			writer.nullValue();
		} else if (value instanceof String) {
			writer.value((String) value);
		} else if (value instanceof Number) {
			writer.value(((Number) value).doubleValue());
		} else if (value instanceof Boolean) {
			writer.value((Boolean) value);
		} else if (value instanceof Map) {
			writeJsonMap(writer, (Map<String, Object>) value);
		} else {
			writer.value(value.toString());
		}
	}

}
