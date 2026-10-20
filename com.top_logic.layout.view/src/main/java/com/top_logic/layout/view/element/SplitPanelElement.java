/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl.ChildConstraint;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that wraps {@link ReactSplitPanelControl}.
 *
 * <p>
 * Renders a split panel with draggable splitters between child panes. Each {@code <pane>} defines
 * its size, unit, and content elements.
 * </p>
 */
@InApp
public class SplitPanelElement implements UIElement {

	/**
	 * Configuration for {@link SplitPanelElement}.
	 */
	@TagName("split-panel")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(SplitPanelElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getOrientation()}. */
		String ORIENTATION = "orientation";

		/** Configuration name for {@link #getResizable()}. */
		String RESIZABLE = "resizable";

		/** Configuration name for {@link #getPanes()}. */
		String PANES = "panes";

		/**
		 * The layout orientation.
		 */
		@Name(ORIENTATION)
		Orientation getOrientation();

		/**
		 * Whether draggable splitters are shown between panes.
		 */
		@Name(RESIZABLE)
		@BooleanDefault(true)
		boolean getResizable();

		/**
		 * The child pane definitions.
		 *
		 * @implNote Keyed by {@link PaneConfig#ID} so that a same-path overlay can address a pane.
		 */
		@Name(PANES)
		@Key(PaneConfig.ID)
		@TreeProperty
		List<PaneConfig> getPanes();
	}

	/**
	 * Configuration for a single pane in the split panel.
	 */
	@TagName("pane")
	public interface PaneConfig extends PaneSizeConfig {

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/**
		 * The name an overlay of the view addresses this pane by.
		 *
		 * <p>
		 * A pane without id is anonymous: an overlay adds panes beside it, but cannot address the
		 * pane itself.
		 * </p>
		 */
		@Name(ID)
		String getId();

		/**
		 * The content elements in this pane.
		 *
		 * @implNote Keyed by {@link UIElement.Config#ID} so that a same-path overlay can address
		 *           an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(CHILDREN)
		@DefaultContainer
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getChildren();
	}

	/**
	 * Segment appended to the personalization key of the context to form the key the
	 * {@link SplitSizePersonalization} of the split is stored under.
	 */
	private static final String KEY_SEGMENT = ".split-panel";

	private final Orientation _orientation;

	private final boolean _resizable;

	private final List<PaneEntry> _panes;

	private final String _cssClass;

	/**
	 * Creates a new {@link SplitPanelElement} from configuration.
	 */
	@CalledByReflection
	public SplitPanelElement(InstantiationContext context, Config config) {
		_orientation = config.getOrientation();
		_resizable = config.getResizable();
		_panes = config.getPanes().stream()
			.map(paneConfig -> {
				List<UIElement> children = paneConfig.getChildren().stream()
					.map(context::getInstance)
					.collect(Collectors.toList());
				return new PaneEntry(new ChildConstraint(paneConfig.getSize(), paneConfig.getUnit(),
					paneConfig.getMinSize(), Scrolling.AUTO), children);
			})
			.collect(Collectors.toList());
		_cssClass = config.getCssClass();
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return _panes.stream()
			.map(pane -> ChildGroup.elements(pane._children()))
			.collect(Collectors.toList());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ChildConstraint> configured = _panes.stream()
			.map(PaneEntry::_constraint)
			.collect(Collectors.toList());
		SplitSizePersonalization personalization =
			new SplitSizePersonalization(context.getPersonalizationKey() + KEY_SEGMENT, configured);

		ReactSplitPanelControl splitPanel = personalization.createSplitPanel(context, _orientation, _resizable);
		splitPanel.setCssClass(_cssClass);

		for (int i = 0; i < _panes.size(); i++) {
			// Descend the personalization path per pane so that nested split panels get distinct
			// personalization keys instead of all sharing the parent's key.
			ViewContext paneContext = context.childContext(String.valueOf(i));
			personalization.addPane(splitPanel, i, createContent(_panes.get(i)._children(), paneContext));
		}

		return splitPanel;
	}

	private static ReactControl createContent(List<UIElement> elements, ViewContext context) {
		return ContentControls.toControl(elements, context);
	}

	private record PaneEntry(ChildConstraint _constraint, List<UIElement> _children) {
	}
}
