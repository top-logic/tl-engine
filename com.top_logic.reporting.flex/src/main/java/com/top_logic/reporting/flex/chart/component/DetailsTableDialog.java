/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.meta.search.AttributedSearchResultComponent;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;

/**
 * Dynamic table-component for detail-tables that uses the {@link TLClass} from the
 * {@link AttributedSearchResultSet}.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class DetailsTableDialog extends AttributedSearchResultComponent {

	/**
	 * <code>GLOBAL_DIALOG_NAME</code>: Name of the global dialog used to display
	 * chart-detail-tables
	 */
	public static final ComponentName GLOBAL_DIALOG_NAME =
		ComponentName.newName("reporting/flex/chart/chartDetailsDialog.layout.xml", "ChartDetailsTable");

	/**
	 * Config-constructor for {@link DetailsTableDialog}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public DetailsTableDialog(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}
	
	public void setSearchResult(final AttributedSearchResultSet resultSet) {
		final Set<? extends TLClass> me = resultSet.getTypes();
		setTypes(me);
		this.setModel(resultSet);
	}

	/**
	 * the detail-dialog to display the chart-detail-table
	 */
	public static DetailsTableDialog getDialog(LayoutComponent component) {
		return (DetailsTableDialog) component.getMainLayout().getComponentByName(GLOBAL_DIALOG_NAME);
	}

}