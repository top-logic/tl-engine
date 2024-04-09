/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.ControlBasedTableDataProvider;
import com.top_logic.layout.basic.TableFilterOverviewControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.mig.html.layout.Layout.LayoutResizeMode;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlProvider}, that provides a {@link LayoutControl} for a table with a filter
 * sidebar.
 * 
 * @see TableFilterLayoutControlProvider Separately layouting the table sidebar.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@IntroducesToolbar
public class TableWithSidebar extends ConfiguredLayoutControlProvider<TableWithSidebar.Config> {

	private final DisplayDimension _filterWidth;

	private final boolean _initiallyMinimized;

	/**
	 * Configuration options for {@link TableWithSidebar}.
	 */
	public interface Config extends PolymorphicConfiguration<TableWithSidebar>, ToolbarOptions, ExpandableConfig {

		/**
		 * @see #getFilterWidth()
		 */
		String FILTER_WIDTH = "filterWidth";

		/**
		 * Default value of {@link #getFilterWidth()}.
		 */
		String FILTER_WIDTH_DEFAULT = "200px";

		/**
		 * Width of the sidebar.
		 */
		@FormattedDefault(FILTER_WIDTH_DEFAULT)
		@Name(FILTER_WIDTH)
		DisplayDimension getFilterWidth();
	}

	/**
	 * Creates a {@link TableWithSidebar} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TableWithSidebar(InstantiationContext context, Config config) {
		super(context, config);
		_filterWidth = config.getFilterWidth();
		_initiallyMinimized = config.isInitiallyMinimized();
	}

	@Override
	public LayoutControl createLayoutControl(Strategy strategy, final LayoutComponent component) {
		checkForCompatibility(component);

		Expandable model =
			new PersonalizingExpandable(component.getName() + ".toolbarCollapsedState", _initiallyMinimized);

		final MaximizableControl container = new MaximizableControl(component);
		FlexibleFlowLayoutControl layout = new FlexibleFlowLayoutControl(LayoutResizeMode.INSTANT, Orientation.HORIZONTAL);
		container.setChildControl(layout);

		final CollapsibleControl collapsibleControl =
			new CollapsibleControl(I18NConstants.TABLE_FILTER_TITLE, model, false, getConfig());
		collapsibleControl.getToolbar().setShowMaximizeDefault(false);

		final ControlBasedTableDataProvider tableControlProvider =
			new ControlBasedTableDataProvider((ControlRepresentable) component);
		TableFilterOverviewControl tableFilterOverviewControl =
			new TableFilterOverviewControl(collapsibleControl.getToolbar(), tableControlProvider);

		LayoutControlAdapter layoutControlAdapter = new LayoutControlAdapter(tableFilterOverviewControl) {

			@Override
			protected void internalAttach() {
				super.internalAttach();
				component.addInvalidationListener(this);
			}

			@Override
			protected void internalDetach() {
				component.removeInvalidationListener(this);
				super.internalDetach();
			}
		};
		collapsibleControl.setChildControl(layoutControlAdapter);
		collapsibleControl.setConstraint(
			new DefaultLayoutData(
				_filterWidth, 100, DisplayDimension.HUNDERED_PERCENT, 100, Scrolling.AUTO));
		layout.addChild(collapsibleControl);

		LayoutControl table = strategy.createDefaultLayout(component);
		if (table instanceof CollapsibleControl) {
			CollapsibleControl tableToolbar = (CollapsibleControl) table;
			tableToolbar.setCanMaximize(false);
			tableToolbar.getToolbar().setShowMinimizeDefault(false);
		}
		layout.addChild(table);

		return container;
	}

	private void checkForCompatibility(LayoutComponent component) {
		if (!(component instanceof ControlRepresentable)) {
			throw new IllegalArgumentException("Component '" + component.getName() + "' must be '"
				+ ControlRepresentable.class.getName() + "' to provide filter sidebar.");
		}
	}
}
