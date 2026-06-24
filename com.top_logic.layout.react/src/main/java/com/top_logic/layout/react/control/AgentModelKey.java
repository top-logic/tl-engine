/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.io.StringWriter;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;

/**
 * Builds a stable, locale- and session-independent identity for a business object, for use as a
 * headless agent address/argument key.
 *
 * <p>
 * The identity is a {@link ModelName} — the same object-naming the script recorder uses to refer to
 * a model object across runs — serialized as JSON via {@link JsonConfigurationWriter}. Unlike a
 * positional index or a session-allocated id, this key designates the actual business object, so it
 * survives sorting, filtering, re-rendering and replay in a different session.
 * </p>
 */
public final class AgentModelKey {

	private AgentModelKey() {
		// Static utility.
	}

	/**
	 * The JSON-serialized {@link ModelName} of the given model object, or {@code null} if the object
	 * cannot be named (e.g. has no applicable naming scheme).
	 *
	 * <p>
	 * Best-effort: any failure to name or serialize yields {@code null} rather than an error, so the
	 * projection degrades to no key for that node.
	 * </p>
	 *
	 * @param model
	 *        The business object to identify.
	 * @return The key JSON, or {@code null}.
	 */
	public static String toJson(Object model) {
		if (model == null) {
			return null;
		}
		Maybe<? extends ModelName> name;
		try {
			name = ModelResolver.buildModelNameIfAvailable(model);
		} catch (Throwable ex) {
			return null;
		}
		if (!name.hasValue()) {
			return null;
		}
		try {
			StringWriter buffer = new StringWriter();
			JsonWriter json = new JsonWriter(buffer);
			new JsonConfigurationWriter(json).write(name.get());
			json.flush();
			return buffer.toString();
		} catch (Throwable ex) {
			return null;
		}
	}
}
