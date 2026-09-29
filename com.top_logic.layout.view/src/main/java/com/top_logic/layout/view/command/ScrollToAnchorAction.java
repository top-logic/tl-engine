/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.common.AnchorControl;
import com.top_logic.layout.react.protocol.FunctionCall;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ViewAction} that scrolls the browser to an {@link com.top_logic.layout.view.element.AnchorElement
 * &lt;anchor&gt;}, computing the target dynamically.
 *
 * <p>
 * The target is a model object - scrolled to by its {@link AnchorControl#anchorId(Object) anchor id}
 * - or a string naming a fixed anchor. Placed in a command chain, e.g. a reply command that brings
 * the new-element editor into view:
 * </p>
 *
 * <pre>
 * &lt;generic-command&gt;
 *   ... prepare the reply ...
 *   &lt;scroll-to-anchor target="'composer'"/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 *
 * @implNote Emits a {@link FunctionCall} directed event on the command's response, invoking the
 *           client {@code TLReact.scrollToAnchor} helper - the same primitive a
 *           {@link com.top_logic.layout.view.element.ScrollLinkElement &lt;scroll-link&gt;} uses.
 */
public class ScrollToAnchorAction implements ViewAction {

	/** Client-side global holding the scroll helper. */
	private static final String CLIENT_NAMESPACE = "TLReact";

	/** Client-side helper scrolling an anchor into view. */
	private static final String CLIENT_FUNCTION = "scrollToAnchor";

	/**
	 * Configuration for {@link ScrollToAnchorAction}.
	 */
	@TagName("scroll-to-anchor")
	public interface Config extends PolymorphicConfiguration<ScrollToAnchorAction> {

		/** Configuration name for {@link #getTarget()}. */
		String TARGET = "target";

		@Override
		@ClassDefault(ScrollToAnchorAction.class)
		Class<? extends ScrollToAnchorAction> getImplementationClass();

		/**
		 * TL-Script function computing the scroll target from the chain's current input value:
		 * {@code input -> target}.
		 *
		 * <p>
		 * The result is a model object (scrolled to by its anchor id) or a string (a literal anchor
		 * name). When unset, the chain's input value is used as the target directly.
		 * </p>
		 */
		@Name(TARGET)
		Expr getTarget();
	}

	private final QueryExecutor _target;

	/**
	 * Creates a new {@link ScrollToAnchorAction}.
	 */
	@CalledByReflection
	public ScrollToAnchorAction(InstantiationContext context, Config config) {
		_target = config.getTarget() == null ? null : QueryExecutor.compile(config.getTarget());
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		Object target = _target == null ? input : _target.execute(input);
		String key = anchorKey(target);
		if (key != null) {
			SSEUpdateQueue queue = context.getSSEQueue();
			if (queue != null) {
				queue.enqueue(FunctionCall.create()
					.setFunctionRef(CLIENT_NAMESPACE)
					.setFunctionName(CLIENT_FUNCTION)
					.setArguments(jsonArguments(key)));
			}
		}
		return input;
	}

	private static String anchorKey(Object target) {
		if (target == null) {
			return null;
		}
		if (target instanceof TLObject) {
			return AnchorControl.anchorId(target);
		}
		return target.toString();
	}

	/**
	 * The JSON argument array {@code ["<key>"]} for the client {@link #CLIENT_FUNCTION} call.
	 */
	private static String jsonArguments(String key) {
		StringBuilder buffer = new StringBuilder("[\"");
		for (int i = 0; i < key.length(); i++) {
			char c = key.charAt(i);
			switch (c) {
				case '"':
					buffer.append("\\\"");
					break;
				case '\\':
					buffer.append("\\\\");
					break;
				case '\n':
					buffer.append("\\n");
					break;
				case '\r':
					buffer.append("\\r");
					break;
				case '\t':
					buffer.append("\\t");
					break;
				default:
					buffer.append(c);
			}
		}
		return buffer.append("\"]").toString();
	}

}
