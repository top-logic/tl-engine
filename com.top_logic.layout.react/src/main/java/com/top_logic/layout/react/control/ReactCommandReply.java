/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.tool.boundsec.DefaultHandlerResult;
import com.top_logic.tool.boundsec.HandlerResult;

import de.haumacher.msgbuf.data.DataObject;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * The fields a {@link ReactCommandHandler} answers its command with, beyond the fact that it was
 * processed.
 *
 * <p>
 * Most commands report their outcome through the state of the controls: the client renders what
 * the server publishes. A caller that has to know the outcome of its very command before it goes
 * on - an identifier the command created, the reason it refused to act, how much it changed -
 * gets it from the reply instead. The handler attaches the reply to the {@link HandlerResult} it
 * returns:
 * </p>
 *
 * <pre>
 * &#64;ReactCommandHandler(CMD_CREATE)
 * HandlerResult handleCreate(Map&lt;String, Object&gt; arguments) {
 * 	Item item = createItem(arguments);
 * 	return ReactCommandReply.create().put(FIELD_ID, item.getId()).toResult();
 * }
 * </pre>
 *
 * <p>
 * The servlet writes the reply's fields into the JSON answer of the command, next to
 * {@link #FIELD_SUCCESS}: <code>{"success": true, "id": "..."}</code>. On the client, the promise
 * the command function of <code>useTLCommand()</code> returns resolves with that answer. The same
 * holds for an upload, whose {@link com.top_logic.layout.react.UploadHandler} result may carry a
 * reply as well.
 * </p>
 *
 * <p>
 * A field value is a JSON value: <code>null</code>, a {@link String}, a {@link Number}, a
 * {@link Boolean}, a {@link Map} with {@link String} keys, an {@link Iterable} of such values, or a
 * msgbuf {@link DataObject}, which is written as its own JSON serialization. The names
 * {@link #FIELD_SUCCESS}, {@link #FIELD_ERROR} and {@link #FIELD_ERROR_CODE} belong to the answer
 * itself and are refused as field names.
 * </p>
 *
 * <p>
 * A reply is written whether or not the result {@link HandlerResult#isSuccess() succeeded}: a
 * failed command is reported to the user in the window (see {@link CommandErrors}) and its request
 * is answered like that of a successful one, so a handler that attached a reply to a failed result
 * still tells its caller what it attached. A command answered with an error status (an unknown
 * control, a refused upload) carries no reply.
 * </p>
 *
 * <p>
 * The reply is the outcome for the caller only: the script recorder does not record it, and a
 * replayed command produces it again.
 * </p>
 */
public final class ReactCommandReply {

	/**
	 * Name of the answer field telling whether the request was processed.
	 *
	 * <p>
	 * Must match the {@code success} field the client reads in {@code command-channel.ts}.
	 * </p>
	 */
	public static final String FIELD_SUCCESS = "success";

	/**
	 * Name of the answer field holding the message of a request answered with an error status.
	 */
	public static final String FIELD_ERROR = "error";

	/**
	 * Name of the answer field holding the machine-readable code of a request answered with an
	 * error status.
	 *
	 * <p>
	 * Must match the {@code errorCode} field the client reads in {@code command-channel.ts}.
	 * </p>
	 */
	public static final String FIELD_ERROR_CODE = "errorCode";

	/**
	 * The names of the fields the answer itself consists of, which a reply must not use.
	 */
	private static final Set<String> RESERVED_FIELDS = Set.of(FIELD_SUCCESS, FIELD_ERROR, FIELD_ERROR_CODE);

	/**
	 * The {@link HandlerResult} annotation carrying the reply.
	 */
	private static final Property<ReactCommandReply> REPLY =
		TypedAnnotatable.property(ReactCommandReply.class, "reactCommandReply");

	private final Map<String, Object> _fields = new LinkedHashMap<>();

	private ReactCommandReply() {
		// See create().
	}

	/**
	 * Creates an empty reply, to be filled with {@link #put(String, Object)}.
	 */
	public static ReactCommandReply create() {
		return new ReactCommandReply();
	}

	/**
	 * The reply attached to the given result, <code>null</code> if it carries none.
	 */
	public static ReactCommandReply of(HandlerResult result) {
		if (result == null) {
			return null;
		}
		return result.get(REPLY);
	}

	/**
	 * Whether the given name belongs to the answer itself and is therefore refused as name of a
	 * reply field.
	 */
	public static boolean isReserved(String name) {
		return RESERVED_FIELDS.contains(name);
	}

	/**
	 * Adds a field to this reply, replacing a field of the same name.
	 *
	 * @param name
	 *        The name of the field in the answer. Must not be a {@link #isReserved(String) reserved}
	 *        name.
	 * @param value
	 *        The JSON value of the field, see the class documentation.
	 * @return This reply, for chaining.
	 * @throws IllegalArgumentException
	 *         If the name is reserved, or the value is no JSON value.
	 */
	public ReactCommandReply put(String name, Object value) {
		if (name == null) {
			throw new IllegalArgumentException("A reply field needs a name.");
		}
		if (isReserved(name)) {
			throw new IllegalArgumentException(
				"The reply field '" + name + "' is reserved for the answer itself, reserved are: " + RESERVED_FIELDS);
		}
		checkValue(name, value);
		_fields.put(name, value);
		return this;
	}

	/**
	 * The fields of this reply, in the order they were {@link #put(String, Object) added}.
	 */
	public Map<String, Object> getFields() {
		return Collections.unmodifiableMap(_fields);
	}

	/**
	 * Whether this reply has no fields.
	 */
	public boolean isEmpty() {
		return _fields.isEmpty();
	}

	/**
	 * A successful {@link HandlerResult} carrying this reply.
	 */
	public HandlerResult toResult() {
		return attachTo(new HandlerResult());
	}

	/**
	 * Attaches this reply to the given result.
	 *
	 * @param result
	 *        The result of the command. The shared {@link HandlerResult#DEFAULT_RESULT} cannot be
	 *        annotated and is replaced by a fresh successful result.
	 * @return The result carrying this reply.
	 */
	public HandlerResult attachTo(HandlerResult result) {
		HandlerResult target = result instanceof DefaultHandlerResult ? new HandlerResult() : result;
		target.set(REPLY, this);
		return target;
	}

	/**
	 * Writes the fields of this reply as name-value pairs into the JSON object the given writer is
	 * currently writing.
	 */
	public void writeFields(JsonWriter json) throws IOException {
		for (Map.Entry<String, Object> field : _fields.entrySet()) {
			json.name(field.getKey());
			writeValue(json, field.getValue());
		}
	}

	/**
	 * This reply as a JSON object of its fields.
	 */
	public String toJson() {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginObject();
			writeFields(json);
			json.endObject();
		} catch (IOException ex) {
			throw new IllegalStateException("Writing to a string buffer failed.", ex);
		}
		return buffer.toString();
	}

	/**
	 * The JSON answer of a processed command whose handler returned the given result:
	 * {@link #FIELD_SUCCESS} and the fields of the {@link #of(HandlerResult) reply} it carries.
	 */
	public static String answerJson(HandlerResult result) {
		return answerJson(of(result));
	}

	/**
	 * The JSON answer of a processed request: {@link #FIELD_SUCCESS} and the fields of the given
	 * reply.
	 *
	 * @param reply
	 *        The reply to the request, <code>null</code> for none.
	 */
	public static String answerJson(ReactCommandReply reply) {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginObject();
			json.name(FIELD_SUCCESS);
			json.value(true);
			if (reply != null) {
				reply.writeFields(json);
			}
			json.endObject();
		} catch (IOException ex) {
			throw new IllegalStateException("Writing to a string buffer failed.", ex);
		}
		return buffer.toString();
	}

	private static void checkValue(String path, Object value) {
		if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean
			|| value instanceof DataObject) {
			return;
		}
		if (value instanceof Map<?, ?> map) {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if (!(entry.getKey() instanceof String key)) {
					throw new IllegalArgumentException(
						"The reply field '" + path + "' holds a map with the non-string key '" + entry.getKey() + "'.");
				}
				checkValue(path + "." + key, entry.getValue());
			}
			return;
		}
		if (value instanceof Iterable<?> elements) {
			int index = 0;
			for (Object element : elements) {
				checkValue(path + "[" + index++ + "]", element);
			}
			return;
		}
		throw new IllegalArgumentException(
			"The reply field '" + path + "' holds no JSON value but a " + value.getClass().getName() + ".");
	}

	private static void writeValue(JsonWriter json, Object value) throws IOException {
		if (value == null) {
			json.nullValue();
		} else if (value instanceof String string) {
			json.value(string);
		} else if (value instanceof Number number) {
			json.value(number);
		} else if (value instanceof Boolean bool) {
			json.value(bool.booleanValue());
		} else if (value instanceof DataObject data) {
			data.writeTo(json);
		} else if (value instanceof Map<?, ?> map) {
			json.beginObject();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				json.name((String) entry.getKey());
				writeValue(json, entry.getValue());
			}
			json.endObject();
		} else {
			json.beginArray();
			for (Object element : (Iterable<?>) value) {
				writeValue(json, element);
			}
			json.endArray();
		}
	}

}
