/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.InvisibleView;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.component.configuration.ViewConfiguration;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.util.ToBeValidated;

/**
 * {@link LayoutControlProvider} that displays a single {@link View}.
 * 
 * <p>
 * In contrast to {@link LayoutViewProvider}, this display only reserves space, if the displayed
 * {@link View} is {@link View#isVisible() visible}. If the visibility of the {@link View} changes
 * dynamically, the {@link View} must allow observing its visibility state using a
 * {@link VisibilityListener}.
 * </p>
 * 
 * @see LayoutViewProvider
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConditionalViewLayout extends ConfiguredLayoutControlProvider<ConditionalViewLayout.Config> {

	private ViewConfiguration _viewFactory;

	/**
	 * Configuration options for {@link ConditionalViewLayout}
	 */
	public interface Config extends PolymorphicConfiguration<LayoutControlProvider> {
		/**
		 * CSS class used for the top-level layout control.
		 */
		String getCssClass();

		/**
		 * The individual size of this layout, if the {@link #getView()} is active.
		 * 
		 * <p>
		 * The interpretation depends on the {@link #isHorizontal()} property.
		 * </p>
		 */
		DisplayDimension getSize();

		/**
		 * The view to display.
		 */
		PolymorphicConfiguration<? extends ViewConfiguration> getView();

		/**
		 * Whether the active view may have scroll bars.
		 */
		boolean isScrolleable();

		/**
		 * Whether this layout is used in horizontal context.
		 * 
		 * <p>
		 * In horizontal context, all siblings form a row, where each one has identical height. In
		 * vertical context, all siblings have identical width.
		 * </p>
		 */
		boolean isHorizontal();
	}

	/**
	 * Creates a {@link ConditionalViewLayout} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConditionalViewLayout(InstantiationContext context, Config config) {
		super(context, config);
		_viewFactory = context.getInstance(config.getView());
	}

	@Override
	public LayoutControl createLayoutControl(Strategy strategy, LayoutComponent component) {
		final HTMLFragment view = createView(component);

		final LayoutControlAdapter layout = new LayoutControlAdapter(view);
		if (view instanceof PropertyObservable) {
			((PropertyObservable) view).addListener(VisibilityModel.VISIBLE_PROPERTY, new VisibilityListener() {
				@Override
				public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
					updateConstraint(layout, newVisibility);
					return Bubble.BUBBLE;
				}
			});
		}
		layout.setCssClass(getConfig().getCssClass());

		// The initially assigned layout constraint is overridden be the LayoutControlFactory.
		// Ensure that the initial size is a dynamically computed size and not the statically
		// assigned size from the layout XML by scheduling a validation.
		DefaultDisplayContext.getDisplayContext().getLayoutContext().notifyInvalid(new ToBeValidated() {
			@Override
			public void validate(DisplayContext context) {
				boolean visible = Fragments.isVisible(view);
				updateConstraint(layout, visible);
			}
		});

		return layout;
	}

	private HTMLFragment createView(LayoutComponent component) {
		final HTMLFragment view = _viewFactory == null ? null : _viewFactory.createView(component);
		return view == null ? InvisibleView.INSTANCE : view;
	}

	void updateConstraint(final LayoutControlAdapter layout, boolean visible) {
		if (visible) {
			DisplayDimension width =
				getConfig().isHorizontal() ? getConfig().getSize() : DisplayDimension.HUNDERED_PERCENT;
			DisplayDimension height =
				getConfig().isHorizontal() ? DisplayDimension.HUNDERED_PERCENT : getConfig().getSize();
			layout.setConstraint(new DefaultLayoutData(width, 100, height, 100,
				getConfig().isScrolleable() ? Scrolling.AUTO : Scrolling.NO));
		} else {
			layout.setConstraint(new DefaultLayoutData(DisplayDimension.ZERO, 100, DisplayDimension.ZERO, 100,
				Scrolling.NO));
		}
	}

}
