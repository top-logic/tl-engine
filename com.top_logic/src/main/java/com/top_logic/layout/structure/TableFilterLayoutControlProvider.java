/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.basic.ControlBasedTableDataProvider;
import com.top_logic.layout.basic.TableFilterOverviewControl;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlProvider}, that provides a {@link LayoutControl} for separate filter handling
 * of a table.
 * 
 * @see TableWithSidebar Simple layout for tables with a sidebar to the left.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableFilterLayoutControlProvider
		extends ConfiguredLayoutControlProvider<TableFilterLayoutControlProvider.Config> {

	private final ComponentName _maximizeRoot;

	private final boolean _initiallyMinimized;

	public interface Config
			extends PolymorphicConfiguration<TableFilterLayoutControlProvider>, ToolbarOptions, ExpandableConfig {

		/**
		 * @see #getMaximizeRoot()
		 */
		String MAXIMIZE_ROOT = "maximizeRoot";

		/**
		 * Name of a surrounding layout that should be maximized, if the maximize button in the
		 * table sidebar is pressed.
		 */
		@Name(MAXIMIZE_ROOT)
		ComponentName getMaximizeRoot();

	}

	/**
	 * Create a new {@link TableFilterLayoutControlProvider}.
	 */
	public TableFilterLayoutControlProvider(InstantiationContext context, Config config) {
		super(context, config);
		_maximizeRoot = config.getMaximizeRoot();
		_initiallyMinimized = config.isInitiallyMinimized();
	}

	@Override
	public LayoutControl createLayoutControl(Strategy strategy, final LayoutComponent component) {
		checkForCompatibility(component);

		Expandable model;
		boolean hasMaximizeRoot = _maximizeRoot != null;
		if (hasMaximizeRoot) {
			model = component.getMainLayout().getComponentByName(_maximizeRoot);
			if (model == null) {
				Logger.error(
					"Maximize root component '" + _maximizeRoot + "' not found for sidebar of '" + component.getName()
					+ "' at " + component.getLocation(), TableFilterLayoutControlProvider.class);

				model =
					new PersonalizingExpandable(component.getName() + ".toolbarCollapsedState", _initiallyMinimized);
			}
		} else {
			model = new PersonalizingExpandable(component.getName() + ".toolbarCollapsedState", _initiallyMinimized);
		}
		final CollapsibleControl collapsibleControl =
			new CollapsibleControl(I18NConstants.TABLE_FILTER_TITLE, model, false, getConfig());
		collapsibleControl.getToolbar().setShowMaximizeDefault(hasMaximizeRoot);

		final ControlBasedTableDataProvider tableDataProvider =
			new ControlBasedTableDataProvider((ControlRepresentable) component);
		TableFilterOverviewControl tableFilterOverviewControl =
			new TableFilterOverviewControl(collapsibleControl.getToolbar(), tableDataProvider);

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
		return collapsibleControl;
	}

	private void checkForCompatibility(LayoutComponent component) {
		if (!(component instanceof ControlRepresentable)) {
			throw new IllegalArgumentException("Component '" + component.getName() + "' must be '"
				+ ControlRepresentable.class.getName() + "' to provide filter sidebar.");
		}
	}
}
