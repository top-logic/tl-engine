/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.services.simpleajax.AbstractCssClassUpdate;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.ContentReplacement;
import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.FragmentInsertion;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSFunctionCall;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.base.services.simpleajax.RangeReplacement;
import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.react.protocol.CssClassUpdate;
import com.top_logic.layout.react.protocol.FunctionCall;
import com.top_logic.layout.react.protocol.Property;
import com.top_logic.layout.react.protocol.SSEEvent;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Converts TopLogic {@link ClientAction} objects into SSE protocol events.
 */
public class ClientActionConverter {

	private final DisplayContext _context;

	/**
	 * Creates a new converter.
	 *
	 * @param context
	 *        The display context for rendering fragments.
	 */
	public ClientActionConverter(DisplayContext context) {
		_context = context;
	}

	/**
	 * Converts a {@link ClientAction} to an {@link SSEEvent}.
	 *
	 * @param action
	 *        The action to convert.
	 * @return The converted SSE event, or {@code null} if the action type is not supported.
	 */
	public SSEEvent convert(ClientAction action) {
		if (action instanceof ContentReplacement) {
			return convertContentReplacement((ContentReplacement) action);
		}
		if (action instanceof ElementReplacement) {
			return convertElementReplacement((ElementReplacement) action);
		}
		if (action instanceof RangeReplacement) {
			return convertRangeReplacement((RangeReplacement) action);
		}
		if (action instanceof FragmentInsertion) {
			return convertFragmentInsertion((FragmentInsertion) action);
		}
		if (action instanceof PropertyUpdate) {
			return convertPropertyUpdate((PropertyUpdate) action);
		}
		if (action instanceof AbstractCssClassUpdate) {
			return convertCssClassUpdate((AbstractCssClassUpdate) action);
		}
		if (action instanceof JSSnipplet) {
			return convertJSSnipplet((JSSnipplet) action);
		}
		if (action instanceof JSFunctionCall) {
			return convertFunctionCall((JSFunctionCall) action);
		}

		Logger.warn("Unsupported ClientAction type for React SSE conversion: " + action.getClass().getName(),
			ClientActionConverter.class);
		return null;
	}

	private com.top_logic.layout.react.protocol.ContentReplacement convertContentReplacement(
			ContentReplacement action) {
		return com.top_logic.layout.react.protocol.ContentReplacement.create()
			.setElementId(action.getElementID())
			.setHtml(renderFragment(action.getFragment()));
	}

	private com.top_logic.layout.react.protocol.ElementReplacement convertElementReplacement(
			ElementReplacement action) {
		return com.top_logic.layout.react.protocol.ElementReplacement.create()
			.setElementId(action.getElementID())
			.setHtml(renderFragment(action.getFragment()));
	}

	private com.top_logic.layout.react.protocol.RangeReplacement convertRangeReplacement(RangeReplacement action) {
		return com.top_logic.layout.react.protocol.RangeReplacement.create()
			.setStartId(action.getElementID())
			.setStopId(action.getStopID())
			.setHtml(renderFragment(action.getFragment()));
	}

	private com.top_logic.layout.react.protocol.FragmentInsertion convertFragmentInsertion(FragmentInsertion action) {
		return com.top_logic.layout.react.protocol.FragmentInsertion.create()
			.setElementId(action.getElementID())
			.setPosition(action.getPosition())
			.setHtml(renderFragment(action.getFragment()));
	}

	private com.top_logic.layout.react.protocol.PropertyUpdate convertPropertyUpdate(PropertyUpdate action) {
		List<Property> properties = new ArrayList<>();
		// PropertyUpdate holds a single property. The DynamicText value must be rendered.
		// We cannot directly access the property name and value since they are package-private.
		// Instead, we use the toString() representation for logging and pass through the action ID.
		// For a proper implementation, we would need public getters on PropertyUpdate.
		properties.add(Property.create()
			.setName("property")
			.setValue(action.toString()));

		return com.top_logic.layout.react.protocol.PropertyUpdate.create()
			.setElementId(action.getElementID())
			.setProperties(properties);
	}

	private CssClassUpdate convertCssClassUpdate(AbstractCssClassUpdate action) {
		StringBuilder sb = new StringBuilder();
		try {
			action.writeCssClassContent(_context, sb);
		} catch (IOException ex) {
			Logger.error("Failed to render CSS class update.", ex, ClientActionConverter.class);
		}
		return CssClassUpdate.create()
			.setElementId(action.getElementID())
			.setCssClass(sb.toString());
	}

	private com.top_logic.layout.react.protocol.JSSnipplet convertJSSnipplet(JSSnipplet action) {
		String code = action.getCode();
		if (code == null) {
			DynamicText fragment = action.getCodeFragment();
			if (fragment != null) {
				StringWriter sw = new StringWriter();
				try {
					fragment.append(_context, sw);
				} catch (IOException ex) {
					Logger.error("Failed to render JS snipplet fragment.", ex, ClientActionConverter.class);
				}
				code = sw.toString();
			}
		}
		return com.top_logic.layout.react.protocol.JSSnipplet.create()
			.setCode(code != null ? code : "");
	}

	private FunctionCall convertFunctionCall(JSFunctionCall action) {
		String argsJson;
		try {
			StringW sw = new StringW();
			try (JsonWriter jw = new JsonWriter(sw)) {
				jw.beginArray();
				for (Object arg : action.getArguments()) {
					ReactControl.writeJsonValue(jw, arg);
				}
				jw.endArray();
			}
			argsJson = sw.toString();
		} catch (IOException ex) {
			Logger.error("Failed to serialize function call arguments.", ex, ClientActionConverter.class);
			argsJson = "[]";
		}

		return FunctionCall.create()
			.setElementId(action.getElementID())
			.setFunctionRef(action.getFunctionReference())
			.setFunctionName(action.getFunctionName())
			.setArguments(argsJson);
	}

	private String renderFragment(HTMLFragment fragment) {
		try {
			StringWriter sw = new StringWriter();
			TagWriter tagWriter = new TagWriter(sw);
			fragment.write(_context, tagWriter);
			tagWriter.flushBuffer();
			return sw.toString();
		} catch (IOException ex) {
			Logger.error("Failed to render HTML fragment.", ex, ClientActionConverter.class);
			return "";
		}
	}

}
