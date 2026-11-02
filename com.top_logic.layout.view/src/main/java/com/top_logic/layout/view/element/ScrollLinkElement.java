/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ScrollLinkControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputRefFormat;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.SingleInput;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * Declarative {@link UIElement} rendering a link that scrolls the view to the
 * {@link AnchorElement &lt;anchor&gt;} of the object on the {@link Config#getInput() input} channel.
 *
 * <p>
 * The link is hidden while the input is {@code null}, so it can display a "cited"/"reply-to"
 * reference only when set. The link text is the object's label, or the result of an optional
 * {@link Config#getLabel() label expression}.
 * </p>
 *
 * <p>
 * The link is also hidden once its target object is deleted: its anchor is then no longer rendered,
 * so scrolling to it would go nowhere. A channel drops a deleted object it holds (see
 * {@link com.top_logic.layout.view.channel.DefaultViewChannel}), and the link hides itself on that
 * {@code null} value like on any other.
 * </p>
 */
@InApp
public class ScrollLinkElement implements UIElement {

	/**
	 * Configuration for {@link ScrollLinkElement}.
	 */
	@TagName("scroll-link")
	@TypeIcon("css:bi bi-arrow-down-circle")
	public interface Config extends UIElement.Config, SingleInput {

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		@Override
		@ClassDefault(ScrollLinkElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel providing the object to scroll to; the link is hidden while it is {@code null}.
		 *
		 * <p>
		 * The {@link #getLabel() label} sees the object as a variable named after the channel, or by
		 * the name given with {@code as}: {@code input="replyTo as comment"}.
		 * </p>
		 */
		@Override
		@Name(INPUT)
		@Mandatory
		@Format(InputRefFormat.class)
		InputRef getInput();

		/**
		 * TL-Script expression computing the link text from the target object.
		 *
		 * <p>
		 * The expression sees the target object as a variable named after the
		 * {@link #getInput() input} channel, or by its {@code as} name, e.g.
		 * {@code input="replyTo as comment"} with {@code label="$comment.get(`my.module:Comment#title`)"}.
		 * When unset, the target object's label is used.
		 * </p>
		 */
		@Name(LABEL)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getLabel();
	}

	private final Config _config;

	private final QueryExecutor _labelExecutor;

	/**
	 * Creates a new {@link ScrollLinkElement} from configuration.
	 */
	@CalledByReflection
	public ScrollLinkElement(InstantiationContext context, Config config) {
		_config = config;
		_labelExecutor = InputScript.compile(config, Config.LABEL);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel channel = context.resolveChannel(_config.getInput().getChannel());
		Object target = channel.get();

		ScrollLinkControl control = new ScrollLinkControl(context, target, label(target));
		control.setCssClass(_config.getCssClass());

		ChannelListener listener = (sender, oldValue, newValue) -> control.setValue(newValue, label(newValue));
		Registration registration = channel.addListener(listener);
		control.addCleanupAction(registration::dispose);
		return control;
	}

	private String label(Object target) {
		if (target == null) {
			return "";
		}
		if (_labelExecutor != null) {
			Object result = _labelExecutor.execute(target);
			return result == null ? "" : result.toString();
		}
		return MetaLabelProvider.INSTANCE.getLabel(target);
	}

}
