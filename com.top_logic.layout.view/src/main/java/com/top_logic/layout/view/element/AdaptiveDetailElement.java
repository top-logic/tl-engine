/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl.ChildConstraint;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.CommaSeparatedChannelRefs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.util.Resources;

/**
 * Responsive master-detail element that presents the same configuration differently depending on
 * the client's {@link com.top_logic.layout.responsive.DisplayClass display class}.
 *
 * <p>
 * The element holds a {@code <selector>} (the master, e.g. a table or tree that writes the
 * {@link Config#SELECTION selection} channel) and a {@code <detail>} (bound to the same channel) exactly
 * once, and presents the two in one of three ways:
 * </p>
 * <ul>
 * <li>On a wide ({@code REGULAR}) viewport with a {@link DetailDisplay#SPLIT} display, both stand
 * side by side in a draggable split.</li>
 * <li>On a wide viewport with a {@link DetailDisplay#DRAWER} display, the selector keeps the full
 * width and the detail overlays it from the right edge, in a panel as wide as
 * {@value Config#DRAWER_SIZE} says, appearing with the selection and dismissed by clearing it.</li>
 * <li>On a narrow ({@code COMPACT}) viewport, whichever display is configured, the selector is shown
 * full-bleed and is replaced by the detail once something is selected (with a back affordance to
 * clear the selection).</li>
 * </ul>
 *
 * <p>
 * Because a {@code <detail>} may itself contain another {@code <adaptive-detail>}, multi-step
 * selection paths compose: cascading columns on wide screens, step-by-step drill-in on narrow ones -
 * all from a single configuration.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
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

		/** Configuration name for {@link #getHomeLabel()}. */
		String HOME_LABEL = "home-label";

		/** Configuration name for {@link #getDetailDisplay()}. */
		String DETAIL_DISPLAY = "detail-display";

		/** Configuration name for {@link #getDrawerSize()}. */
		String DRAWER_SIZE = "drawer-size";

		/** Default value of {@link #getDrawerSize()}. */
		int DEFAULT_DRAWER_SIZE = 420;

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
		 * The master side: its content (e.g. a {@code <table>} or {@code <tree>}) that writes the
		 * {@link #getSelection() selection} channel, and its share of the width when it stands
		 * beside the {@link #getDetail() detail} in a {@link DetailDisplay#SPLIT split}.
		 */
		@Name(SELECTOR)
		@TreeProperty
		@ItemDefault
		SelectorConfig getSelector();

		/**
		 * The detail side: its content bound to the {@link #getSelection() selection} channel, and
		 * its share of the width when it stands beside the {@link #getSelector() selector} in a
		 * {@link DetailDisplay#SPLIT split}.
		 */
		@Name(DETAIL)
		@TreeProperty
		@ItemDefault
		DetailConfig getDetail();

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

		/**
		 * Label of the breadcrumb's home crumb (the one that returns to the empty selector).
		 *
		 * <p>
		 * Only meaningful on the outermost {@code <adaptive-detail>}: in compact mode it renders a
		 * single breadcrumb spanning all nested levels ({@code home > selected-master >
		 * selected-detail ...}); tapping a crumb clears the selections from that level down. Nested
		 * elements contribute their selected object's label and do not render a breadcrumb of their
		 * own.
		 * </p>
		 */
		@Name(HOME_LABEL)
		ResKey getHomeLabel();

		/**
		 * How the detail is presented beside the selector on a wide viewport: dividing the width
		 * with it in a {@link DetailDisplay#SPLIT split}, or overlaying it in a
		 * {@link DetailDisplay#DRAWER drawer} that leaves the selector at full width.
		 *
		 * <p>
		 * On a narrow viewport the detail replaces the selector either way.
		 * </p>
		 */
		@Name(DETAIL_DISPLAY)
		DetailDisplay getDetailDisplay();

		/**
		 * The width in pixels of the {@link DetailDisplay#DRAWER drawer} the detail is displayed
		 * in.
		 *
		 * <p>
		 * The drawer never grows wider than the element it overlays, so a value exceeding the
		 * available width yields a drawer covering the selector completely. Without a
		 * {@link #getDetailDisplay() drawer display} the value has no effect.
		 * </p>
		 */
		@Name(DRAWER_SIZE)
		@IntDefault(DEFAULT_DRAWER_SIZE)
		int getDrawerSize();
	}

	/**
	 * One side of an {@link AdaptiveDetailElement}: its content and its share of the width in a
	 * {@link DetailDisplay#SPLIT split}.
	 *
	 * <p>
	 * When only one of the two sides gives its {@link #getSize() size} and both sides measure in
	 * percent, the other side takes the rest of the 100 percent, so that
	 * {@code <selector size="55">} alone yields a detail of 45 percent. Sizes given on both sides,
	 * or in pixels, are taken as given.
	 * </p>
	 */
	public interface SideConfig extends PaneSizeConfig {

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/**
		 * The content elements of this side.
		 */
		@Name(CHILDREN)
		@DefaultContainer
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends UIElement>> getChildren();
	}

	/**
	 * The {@link Config#getSelector() selector} side of an {@link AdaptiveDetailElement}.
	 */
	public interface SelectorConfig extends SideConfig {

		/** Default value of {@link #getSize()}. */
		float DEFAULT_SIZE = 32f;

		/** Default value of {@link #getMinSize()}. */
		int DEFAULT_MIN_SIZE = 240;

		@Override
		@FloatDefault(DEFAULT_SIZE)
		float getSize();

		@Override
		@IntDefault(DEFAULT_MIN_SIZE)
		int getMinSize();
	}

	/**
	 * The {@link Config#getDetail() detail} side of an {@link AdaptiveDetailElement}.
	 */
	public interface DetailConfig extends SideConfig {

		/** Default value of {@link #getSize()}. */
		float DEFAULT_SIZE = 68f;

		/** Default value of {@link #getMinSize()}. */
		int DEFAULT_MIN_SIZE = 320;

		@Override
		@FloatDefault(DEFAULT_SIZE)
		float getSize();

		@Override
		@IntDefault(DEFAULT_MIN_SIZE)
		int getMinSize();
	}

	/**
	 * The size one side of an {@link AdaptiveDetailElement} takes in a {@link DetailDisplay#SPLIT
	 * split}.
	 *
	 * @param size
	 *        The share of the width, measured in {@code unit}.
	 * @param unit
	 *        The unit of {@code size}.
	 * @param minSize
	 *        The minimum width in pixels.
	 */
	public record SideSize(float size, DisplayUnit unit, int minSize) {

		/**
		 * The constraint a split panel lays this side out with.
		 */
		public ChildConstraint toConstraint() {
			return new ChildConstraint(size, unit, minSize, Scrolling.AUTO);
		}
	}

	private final ChannelRef _selectionRef;

	private final List<UIElement> _selector;

	private final SideSize _selectorSize;

	private final List<UIElement> _detail;

	private final SideSize _detailSize;

	private final List<ChannelRef> _resetOnRefs;

	private final ResKey _homeLabel;

	private final DetailDisplay _detailDisplay;

	private final int _drawerSize;

	/**
	 * Whether this element is nested inside another {@code <adaptive-detail>}'s detail. Set by the
	 * enclosing element; a nested element suppresses its own breadcrumb (the outermost element
	 * renders one spanning all levels).
	 */
	private boolean _nested;

	private final String _cssClass;

	/**
	 * Creates a new {@link AdaptiveDetailElement} from configuration.
	 */
	@CalledByReflection
	public AdaptiveDetailElement(InstantiationContext context, Config config) {
		_selectionRef = config.getSelection();
		SideConfig selectorConfig = config.getSelector();
		SideConfig detailConfig = config.getDetail();
		_selector = selectorConfig.getChildren().stream().map(context::getInstance).collect(Collectors.toList());
		_detail = detailConfig.getChildren().stream().map(context::getInstance).collect(Collectors.toList());
		_selectorSize = resolveSideSize(selectorConfig, detailConfig);
		_detailSize = resolveSideSize(detailConfig, selectorConfig);
		_resetOnRefs = config.getResetOn();
		_homeLabel = config.getHomeLabel();
		_detailDisplay = config.getDetailDisplay();
		_drawerSize = config.getDrawerSize();

		AdaptiveDetailElement nested = firstNestedAdaptiveDetail();
		if (nested != null) {
			nested._nested = true;
		}
		_cssClass = config.getCssClass();
	}

	/**
	 * The size a side of an {@link AdaptiveDetailElement} takes in a split beside the other side.
	 *
	 * <p>
	 * When the other side gives its size explicitly, this side does not, and both measure in
	 * percent, this side takes the rest of the 100 percent. Otherwise this side takes the size it
	 * is configured with (explicitly or by default).
	 * </p>
	 *
	 * @param side
	 *        The side to resolve the size of.
	 * @param other
	 *        The other side of the same element.
	 * @return The size {@code side} takes in the split.
	 */
	public static SideSize resolveSideSize(SideConfig side, SideConfig other) {
		DisplayUnit unit = side.getUnit();
		float size = side.getSize();
		if (!isSizeSet(side) && isSizeSet(other)
			&& unit == DisplayUnit.PERCENT && other.getUnit() == DisplayUnit.PERCENT) {
			size = Math.max(0f, 100f - other.getSize());
		}
		return new SideSize(size, unit, side.getMinSize());
	}

	private static boolean isSizeSet(SideConfig side) {
		return side.valueSet(side.descriptor().getProperty(PaneSizeConfig.SIZE));
	}

	/**
	 * The nearest nested {@link AdaptiveDetailElement} directly within this element's detail, or
	 * {@code null}.
	 */
	private AdaptiveDetailElement firstNestedAdaptiveDetail() {
		for (UIElement element : _detail) {
			if (element instanceof AdaptiveDetailElement nested) {
				return nested;
			}
		}
		return null;
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(
			ChildGroup.keyed(Config.SELECTOR, _selector),
			ChildGroup.keyed(Config.DETAIL, _detail));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel selectionChannel = context.resolveChannel(_selectionRef);
		List<ViewChannel> resetOn = _resetOnRefs.stream().map(context::resolveChannel).collect(Collectors.toList());

		boolean coordinator = !_nested;
		List<ViewChannel> chain;
		String homeLabel;
		if (coordinator) {
			chain = new ArrayList<>();
			for (AdaptiveDetailElement level = this; level != null; level = level.firstNestedAdaptiveDetail()) {
				chain.add(context.resolveChannel(level._selectionRef));
			}
			homeLabel = _homeLabel == null ? null : Resources.getInstance().getString(_homeLabel);
		} else {
			chain = null;
			homeLabel = null;
		}

		ReactAdaptiveDetailControl result = new ReactAdaptiveDetailControl(context, this, _selector, _detail,
			selectionChannel, resetOn, coordinator, chain, homeLabel, _detailDisplay, _drawerSize,
			_selectorSize.toConstraint(), _detailSize.toConstraint());
		result.setCssClass(_cssClass);
		return result;
	}

}
