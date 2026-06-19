/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.CommaSeparatedChannelRefs;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Responsive master-detail element that presents the same configuration differently depending on
 * the client's {@link com.top_logic.layout.responsive.DisplayClass display class}.
 *
 * <p>
 * The element holds a {@code <selector>} (the master, e.g. a table or tree that writes the
 * {@link #SELECTION selection} channel) and a {@code <detail>} (bound to the same channel) exactly
 * once. On a wide ({@code REGULAR}) viewport it renders both side by side in a draggable split; on a
 * narrow ({@code COMPACT}) viewport it shows the selector full-bleed and replaces it with the detail
 * once something is selected (with a back affordance to clear the selection).
 * </p>
 *
 * <p>
 * Because a {@code <detail>} may itself contain another {@code <adaptive-detail>}, multi-step
 * selection paths compose: cascading columns on wide screens, step-by-step drill-in on narrow ones -
 * all from a single configuration.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AdaptiveDetailElement implements UIElement {

	/**
	 * Configuration for {@link AdaptiveDetailElement}.
	 */
	@TagName("adaptive-detail")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getSelector()}. */
		String SELECTOR = "selector";

		/** Configuration name for {@link #getDetail()}. */
		String DETAIL = "detail";

		/** Configuration name for {@link #getResetOn()}. */
		String RESET_ON = "reset-on";

		@Override
		@ClassDefault(AdaptiveDetailElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The channel carrying the object selected in the {@link #getSelector() selector} and
		 * displayed by the {@link #getDetail() detail}.
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		@Mandatory
		ChannelRef getSelection();

		/**
		 * The master content (e.g. a {@code <table>} or {@code <tree>}) that writes the
		 * {@link #getSelection() selection} channel.
		 */
		@Name(SELECTOR)
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getSelector();

		/**
		 * The detail content bound to the {@link #getSelection() selection} channel.
		 */
		@Name(DETAIL)
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getDetail();

		/**
		 * Channels on which this element's {@link #getSelection() selection} depends; whenever one of
		 * them changes, the selection is reset to {@code null}.
		 *
		 * <p>
		 * Use this when the selectable rows derive from an upstream selection (the typical nested,
		 * cascading master-detail): e.g. a milestone selection is only meaningful within a project
		 * scope, so {@code reset-on="selectedScope"} clears the milestone when the scope changes -
		 * regardless of whether the milestone selector is currently displayed (in compact mode it is
		 * disposed once drilled past, so a value left in the channel would otherwise resurface under
		 * the new scope).
		 * </p>
		 */
		@Name(RESET_ON)
		@Format(CommaSeparatedChannelRefs.class)
		List<ChannelRef> getResetOn();
	}

	private final ChannelRef _selectionRef;

	private final List<UIElement> _selector;

	private final List<UIElement> _detail;

	private final List<ChannelRef> _resetOnRefs;

	/**
	 * Creates a new {@link AdaptiveDetailElement} from configuration.
	 */
	@CalledByReflection
	public AdaptiveDetailElement(InstantiationContext context, Config config) {
		_selectionRef = config.getSelection();
		_selector = config.getSelector().stream().map(context::getInstance).collect(Collectors.toList());
		_detail = config.getDetail().stream().map(context::getInstance).collect(Collectors.toList());
		_resetOnRefs = config.getResetOn();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel selectionChannel = context.resolveChannel(_selectionRef);
		List<ViewChannel> resetOn = _resetOnRefs.stream().map(context::resolveChannel).collect(Collectors.toList());
		return new ReactAdaptiveDetailControl(context, _selector, _detail, selectionChannel, resetOn);
	}

}
