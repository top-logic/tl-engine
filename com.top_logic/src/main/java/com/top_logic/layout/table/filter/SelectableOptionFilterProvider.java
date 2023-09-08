/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.AbstractTableFilterProvider;
import com.top_logic.layout.table.component.TableFilterProvider;

/**
 * {@link TableFilterProvider} of {@link SelectionFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SelectableOptionFilterProvider extends AbstractTableFilterProvider {

	/** Static instance of {@link SelectableOptionFilterProvider} for single values */
	public static final SelectableOptionFilterProvider INSTANCE = new SelectableOptionFilterProvider(false, false);

	/** Static instance of {@link SelectableOptionFilterProvider} for collection based values */
	public static final SelectableOptionFilterProvider INSTANCE_MULTI = new SelectableOptionFilterProvider(true, false);

	@SuppressWarnings("javadoc")
	public interface Config extends AbstractTableFilterProvider.Config {

		/** Whether a table cell contains multiple values (e.g. collection), or not. */
		@Name("multiple-values")
		@BooleanDefault(false)
		boolean hasMultipleValues();

		/**
		 * true, if raw value from accessor shall be use, false, if string mapping by full text
		 * provider is requested
		 */
		@Name("use-raw-options")
		@BooleanDefault(false)
		boolean shouldUseRawOptions();
	}

	private boolean multiple;
	private boolean useRawOptions;

	/**
	 * Create a new {@link SelectableOptionFilterProvider}.
	 */
	@CalledByReflection
	public SelectableOptionFilterProvider(InstantiationContext context, Config config) {
		super(context, config);
		multiple = config.hasMultipleValues();
		useRawOptions = config.shouldUseRawOptions();
	}

	/**
	 * Create a new {@link SelectableOptionFilterProvider}.
	 */
	public SelectableOptionFilterProvider(boolean multiple, boolean useRawOptions) {
		this.multiple = multiple;
		this.useRawOptions = useRawOptions;
	}

	@Override
	protected List<ConfiguredFilter> createFilterList(TableViewModel tableViewModel, String filterPosition) {
		SelectionFilterConfiguration filterConfiguration =
			new SelectionFilterConfiguration(tableViewModel, filterPosition, IdentityTransformer.INSTANCE,
				multiple, showSeparateOptionEntries());
		filterConfiguration.setUseRawOptions(useRawOptions);
		SelectionFilter selectionFilter =
			new SelectionFilter(filterConfiguration,
				new SelectionFilterViewBuilder(getSeparateOptionDisplayThreshold()),
				showNonMatchingOptions());
		return TableFilterProviderUtil.includeNoValueOption(selectionFilter);
	}

	@Override
	protected TableFilter createPopup(List<ConfiguredFilter> someFilters, TableViewModel aTableModel) {
		boolean sameFilterGroup = false;
		PopupDialogModel theModel = this.createPopupDialogModel(this.createPopupTitle(), this.createPopupLayout());
		TableFilter theFilter = new TableFilter(new PopupFilterDialogBuilder(theModel), someFilters, sameFilterGroup);

		return theFilter;
	}
}
