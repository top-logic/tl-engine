/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ScrollLinkControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelChangeEvent.ChangeType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Declarative {@link UIElement} rendering a link that scrolls the view to the
 * {@link AnchorElement &lt;anchor&gt;} of the object on the {@link Config#getInput() input} channel.
 *
 * <p>
 * The link is hidden while the input is {@code null}, so it can display a "cited"/"reply-to"
 * reference only when set. The link text is the object's label, or the result of an optional
 * {@link Config#getLabel() label function}.
 * </p>
 *
 * <p>
 * The link is also hidden once its target object is deleted: its anchor is then no longer rendered,
 * so scrolling to it would go nowhere. This keeps a citation link consistent with the model when the
 * cited object is removed, even while the channel still holds the now-deleted object.
 * </p>
 */
@InApp
public class ScrollLinkElement implements UIElement {

	/**
	 * Configuration for {@link ScrollLinkElement}.
	 */
	@TagName("scroll-link")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		@Override
		@ClassDefault(ScrollLinkElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel providing the object to scroll to; the link is hidden while it is {@code null}.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * TL-Script function computing the link text from the target object: {@code target -> text}.
		 *
		 * <p>
		 * When unset, the target object's label is used.
		 * </p>
		 */
		@Name(LABEL)
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
		_labelExecutor = config.getLabel() == null ? null : QueryExecutor.compile(config.getLabel());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel channel = context.resolveChannel(_config.getInput());
		Object target = channel.get();

		ScrollLinkControl control = new ScrollLinkControl(context, target, label(target));

		ChannelListener listener = (sender, oldValue, newValue) -> control.setValue(newValue, label(newValue));
		channel.addListener(listener);
		control.addCleanupAction(() -> channel.removeListener(listener));

		ChannelObjectObserver observer =
			new ChannelObjectObserver(List.of(channel), Set.of(), event -> hideDeletedTarget(control, channel, event));
		control.addAttachListener(() -> observer.attach(context.getModelScope()));
		control.addDetachListener(observer::detach);
		return control;
	}

	/**
	 * Hides the link when its target object is deleted: the anchor to scroll to is then no longer
	 * rendered, so the link would go nowhere.
	 */
	private static void hideDeletedTarget(ScrollLinkControl control, ViewChannel channel, ModelChangeEvent event) {
		for (TLObject target : ChannelObjectObserver.objects(channel.get())) {
			if (event.getChange(target) == ChangeType.DELETED) {
				control.setValue(null, "");
			}
		}
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
