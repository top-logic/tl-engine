/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import de.haumacher.msgbuf.data.DataObject;
import de.haumacher.msgbuf.data.ReflectiveDataObject;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * The JSON answer of a command (or an upload) the React client sent.
 *
 * <p>
 * Every answer carries {@link #FIELD_SUCCESS}. A processed command whose
 * {@link ReactCommandHandler} returned a reply - a msgbuf message - carries the fields of that
 * message next to it: <code>{"success": true, "id": "..."}</code>. On the client, the promise the
 * command function of <code>useTLCommand()</code> returns resolves with that answer. A command that
 * failed carries {@link #FIELD_ERROR} with the message the user was shown, and a request the
 * server could not process at all - or one whose failure asks the client for more than showing
 * the message - a machine-readable {@link #FIELD_ERROR_CODE} as well.
 * </p>
 *
 * <p>
 * The names {@link #FIELD_SUCCESS}, {@link #FIELD_ERROR} and {@link #FIELD_ERROR_CODE} belong to
 * the answer itself: a reply message must not have properties of these names, see
 * {@link #checkReplyType(Class)}.
 * </p>
 *
 * <p>
 * The reply is the outcome for the caller only: the script recorder does not record it, and a
 * replayed command produces it again.
 * </p>
 */
public final class ReactCommandAnswer {

	/**
	 * Name of the answer field telling whether the request was processed.
	 *
	 * <p>
	 * Must match the {@code success} field the client reads in {@code command-channel.ts}.
	 * </p>
	 */
	public static final String FIELD_SUCCESS = "success";

	/**
	 * Name of the answer field holding the message of a request that failed.
	 */
	public static final String FIELD_ERROR = "error";

	/**
	 * Name of the answer field holding the machine-readable code of a request the server could not
	 * process.
	 *
	 * <p>
	 * Must match the {@code errorCode} field the client reads in {@code command-channel.ts}.
	 * </p>
	 */
	public static final String FIELD_ERROR_CODE = "errorCode";

	/**
	 * Value of {@link #FIELD_ERROR_CODE} of a command refused because of input the user has to
	 * correct first, see {@link CommandErrors#invalidInput(com.top_logic.basic.util.ResKey, java.util.List)}.
	 *
	 * <p>
	 * The client moves the focus to the first field showing an error in the control the command was
	 * sent to. Must match {@code ERROR_CODE_INVALID_INPUT} in {@code command-channel.ts}.
	 * </p>
	 */
	public static final String ERROR_CODE_INVALID_INPUT = "invalid-input";

	/**
	 * The names of the fields the answer itself consists of, which a reply must not use.
	 */
	private static final Set<String> RESERVED_FIELDS = Set.of(FIELD_SUCCESS, FIELD_ERROR, FIELD_ERROR_CODE);

	/**
	 * Suffix of the constants a msgbuf message class declares for the names of its properties.
	 */
	private static final String PROPERTY_CONSTANT_SUFFIX = "__PROP";

	private ReactCommandAnswer() {
		// Static utility.
	}

	/**
	 * Whether the given name belongs to the answer itself and is therefore refused as name of a
	 * property of a reply.
	 */
	public static boolean isReserved(String name) {
		return RESERVED_FIELDS.contains(name);
	}

	/**
	 * Checks that the given msgbuf message type may be a reply: none of its properties has a
	 * {@link #isReserved(String) reserved} name.
	 *
	 * @param replyType
	 *        The message type a command handler returns.
	 * @throws IllegalStateException
	 *         If a property of the type has a reserved name.
	 */
	public static void checkReplyType(Class<?> replyType) {
		for (Field field : replyType.getFields()) {
			int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers) || field.getType() != String.class
				|| !field.getName().endsWith(PROPERTY_CONSTANT_SUFFIX)) {
				continue;
			}
			Object name;
			try {
				name = field.get(null);
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException("Cannot read property name " + field + ".", ex);
			}
			if (isReserved((String) name)) {
				throw new IllegalStateException("The reply type " + replyType.getName() + " has the property '"
					+ name + "', which is reserved for the answer itself, reserved are: " + RESERVED_FIELDS);
			}
		}
	}

	/**
	 * The answer of a processed request: {@link #FIELD_SUCCESS} and the fields of the given reply.
	 *
	 * @param reply
	 *        The reply to the request, <code>null</code> for none.
	 */
	public static String success(DataObject reply) {
		String head = object(json -> {
			json.name(FIELD_SUCCESS);
			json.value(true);
		});
		if (reply == null) {
			return head;
		}
		String fields = replyFields(reply);
		if (fields.isEmpty()) {
			return head;
		}
		return head.substring(0, head.length() - 1) + "," + fields + "}";
	}

	/**
	 * The answer of a request that failed.
	 *
	 * @param errorCode
	 *        The machine-readable code identifying the error condition, e.g.
	 *        {@link #ERROR_CODE_INVALID_INPUT}, <code>null</code> for a failure the client only
	 *        reports.
	 * @param message
	 *        The message describing the failure.
	 */
	public static String failure(String errorCode, String message) {
		return object(json -> {
			json.name(FIELD_SUCCESS);
			json.value(false);
			if (errorCode != null) {
				json.name(FIELD_ERROR_CODE);
				json.value(errorCode);
			}
			json.name(FIELD_ERROR);
			json.value(message);
		});
	}

	/**
	 * The given reply as a JSON object of its fields.
	 */
	public static String toJson(DataObject reply) {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			// Its fields alone, without the type tag a message of a type hierarchy is written with.
			reply.writeContent(json);
		} catch (IOException ex) {
			throw new IllegalStateException("Writing to a string buffer failed.", ex);
		}
		return buffer.toString();
	}

	/**
	 * The members of the JSON object of the given reply, without the enclosing braces.
	 */
	private static String replyFields(DataObject reply) {
		if (reply instanceof ReflectiveDataObject reflective) {
			for (String property : reflective.properties()) {
				if (isReserved(property)) {
					throw new IllegalStateException("The reply " + reply.getClass().getName()
						+ " has the property '" + property + "', which is reserved for the answer itself.");
				}
			}
		}
		String json = toJson(reply).trim();
		return json.substring(1, json.length() - 1).trim();
	}

	private static String object(JsonContent content) {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginObject();
			content.write(json);
			json.endObject();
		} catch (IOException ex) {
			throw new IllegalStateException("Writing to a string buffer failed.", ex);
		}
		return buffer.toString();
	}

	/**
	 * Members written into a JSON object.
	 */
	private interface JsonContent {

		/**
		 * Writes the members.
		 */
		void write(JsonWriter json) throws IOException;
	}

}
