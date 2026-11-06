/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.refusal;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal;
import com.top_logic.tl.ai.core.tools.refusal.messages.Reason;
import com.top_logic.util.Resources;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * The answer of a tool whose operation the application refused: an {@link ApplicationRefusal}.
 *
 * <p>
 * A tool's own text to the model is English. The refusals and validation messages of the
 * application it relays are text about the user, meant to reach the user through the model, so
 * they are resolved in the language of the run's user. The two never meet in one sentence: the
 * application's messages go into the {@link Reason#getText() text} fields of a structured
 * result, and what the result means is said once, in English, in the tool's description - see
 * {@link #DESCRIPTION}.
 * </p>
 */
public final class ApplicationRefusals {

	/**
	 * How a tool's description explains an {@link ApplicationRefusal} to the model.
	 */
	public static final String DESCRIPTION = "If the application refuses, the answer is a JSON object"
		+ " {\"refused\": true, ..., \"reasons\": [{\"attribute\": ..., \"text\": ...}]}: nothing took effect,"
		+ " and each text is the application's own, in the user's language - relay it, do not translate it.";

	private ApplicationRefusals() {
		// Static utility.
	}

	/**
	 * A new, empty refusal.
	 */
	public static ApplicationRefusal refusal() {
		return ApplicationRefusal.create().setRefused(true);
	}

	/**
	 * A reason given by the application's message, resolved in the language of the run's user.
	 */
	public static Reason reason(ResKey message) {
		return Reason.create().setText(Resources.getInstance().getString(message));
	}

	/**
	 * A reason given by a text the application or the database already gave, in the language of the
	 * run's user where it was translated.
	 */
	public static Reason reason(String text) {
		return Reason.create().setText(text);
	}

	/**
	 * The given refusal as the JSON text a tool answers with.
	 */
	public static String toJson(ApplicationRefusal refusal) {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			refusal.writeTo(json);
		} catch (IOException ex) {
			throw new IllegalStateException("Writing to a string cannot fail.", ex);
		}
		return buffer.toString();
	}
}
