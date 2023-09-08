/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.JSFunctionCall;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * Common functionality for dispatching focus requests to the client.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FocusHandling {

	private static final Property<Focusable> FOCUSED_MODEL = TypedAnnotatable.property(Focusable.class, "focusedModel");

	/**
	 * Store a focus request to the given {@link DisplayContext}.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param model
	 *        The {@link Focusable} model to request focus for.
	 * @return Whether the given model was newly focused. <code>false</code>, if the given model
	 *         already has requested focus.
	 */
	public static boolean focus(DisplayContext context, Focusable model) {
		Object before = DefaultDisplayContext.getDisplayContext().set(FOCUSED_MODEL, model);
		return before != model;
	}

	/**
	 * Whether the given model has a pending focus request.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param model
	 *        The {@link Focusable} model to test.
	 * @return Whether there is a pending focus request for the given model.
	 */
	public static boolean shouldFocus(DisplayContext context, Focusable model) {
		Object fieldWithFocus = context.get(FOCUSED_MODEL);
		return fieldWithFocus == model;
	}

	/**
	 * Creates a {@link JSFunctionCall} to request focus for the first input element found in the
	 * element with the given ID.
	 * 
	 * @param id
	 *        The client-side ID to request focus for.
	 * @return The {@link ClientAction} requesting focus.
	 */
	public static ClientAction focusRequest(String id) {
		return new JSFunctionCall(id, "BAL", "focusFirst");
	}

	/**
	 * Writes a focus request during rendering.
	 * 
	 * @param out
	 *        The current {@link TagWriter}.
	 * @param id
	 *        The client-side ID to request focus for.
	 */
	public static void writeFocusRequest(TagWriter out, String id) throws IOException {
		out.beginScript();
		out.writeScript("BAL.focusFirstId(");
		out.writeJsString(id);
		out.writeScript(");");
		out.endScript();
	}

}
