/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.PopupFilterDialogBuilder;

/**
 * Abstract filter provider for plugging some {@link ConfiguredFilter} into a table column filter.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractTableFilterProvider
		implements TableFilterProvider, ConfiguredInstance<PolymorphicConfiguration<AbstractTableFilterProvider>> {

	/**
	 * Common configuration of table filters.
	 */
	public interface Config extends PolymorphicConfiguration<AbstractTableFilterProvider> {

		/**
		 * Configuration name of property of a threshold, below which options shall be displayed
		 * separately
		 */
		public static final String SEPARATE_OPTION_DISPLAY_THRESHOLD = "separate-option-display-threshold";

		/** Configuration name of property, that decides about display of non-matching options */
		public static final String SHOW_NON_MATCHING_OPTIONS = "show-non-matching-options";

		/** Configuration name of property, that decides about option entry display */
		public static final String SHOW_OPTION_ENTRIES = "show-option-entries";

		/**
		 * threshold, below that every option will be displayed as separate filter entry. A
		 *         value of -1 means infinity.
		 */
		@Name(SEPARATE_OPTION_DISPLAY_THRESHOLD)
		int getSeparateOptionDisplayThreshold();
	
		/**
		 * true, when options, that do not match filter criteria, shall be displayed, false
		 *         otherwise
		 */
		@Name(SHOW_NON_MATCHING_OPTIONS)
		boolean shouldShowNonMatchingOptions();

		/**
		 * true, if option entries shall be displayed separately at all, false otherwise
		 */
		@Name(SHOW_OPTION_ENTRIES)
		boolean shouldShowOptionEntries();
	}
	
	private Config _config;

	/**
	 * Creates a {@link AbstractTableFilterProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractTableFilterProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	/**
	 * Create a new {@link AbstractTableFilterProvider} with default configuration.
	 */
	public AbstractTableFilterProvider() {
		this(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, TypedConfiguration.newConfigItem(Config.class));
	}

	/**
	 * Hook to create the list of filters to be plugged into the {@link TableFilter}.
	 * 
	 * @param tableViewModel
	 *        See {@link #createTableFilter(TableViewModel, String)}.
	 * @param filterPosition
	 *        See {@link #createTableFilter(TableViewModel, String)}.
	 * 
	 * @return The list of single filters to be used in the pop up later on, must not be
	 *         <code>null</code>.
	 */
	protected abstract List<ConfiguredFilter> createFilterList(TableViewModel tableViewModel, String filterPosition);

	@Override
	public TableFilter createTableFilter(TableViewModel aTableModel, String filterPosition) {
		return createPopup(createFilterList(aTableModel, filterPosition), aTableModel);
	}

	/** 
     * Initialize the pop up for the given list of filters.
     * 
     * @param    someFilters    The filters to be used in the pop up, must not be <code>null</code>.
     * @param    aTableModel    The table model to adapt the filters to, must not be <code>null</code>.
     * @see      #createPopupTitle()
     * @see      #createPopupLayout()
     * @see      #createPopupDialogModel(ResourceText, LayoutData)
     */
	protected TableFilter createPopup(List<ConfiguredFilter> someFilters, TableViewModel aTableModel) {
        PopupDialogModel theModel  = this.createPopupDialogModel(this.createPopupTitle(), this.createPopupLayout());
        TableFilter      theFilter = new TableFilter(new PopupFilterDialogBuilder(theModel), someFilters, true);

		return theFilter;
    }

    /** 
     * Create the pop up dialog model for displaying the filters later on.
     * 
     * @param    aTitle     The title of the pop up, must not be <code>null</code>.
     * @param    aLayout    The layout information for the pop up, must not be <code>null</code>.
     * @return   The requested dialog pop up model, never <code>null</code>.
     */
    protected DefaultPopupDialogModel createPopupDialogModel(ResourceText aTitle, LayoutData aLayout) {
        return new DefaultPopupDialogModel(aTitle, aLayout, 1);
    }

    /** 
     * Create the layout description for the filter pop up.
     * 
     * @return    The requested layout information, never <code>null</code>.
     */
	protected LayoutData createPopupLayout() {
		return new DefaultLayoutData(
			DisplayDimension.dim(ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.FILTER_DIALOG_WIDTH),
				DisplayUnit.PIXEL),
			100, DisplayDimension.dim(0,
			DisplayUnit.PIXEL), 100,
			Scrolling.NO);
    }

    /** 
     * Create the title of the filter pop up.
     * 
     * @return    The requested title of the pop up, never <code>null</code>.
     */
    protected ResourceText createPopupTitle() {
        return new ResourceText(PopupFilterDialogBuilder.STANDARD_TITLE);
    }

	/**
	 * @see Config#shouldShowOptionEntries()
	 */
	protected boolean showSeparateOptionEntries() {
		return _config.shouldShowOptionEntries();
	}

	/**
	 * @see Config#shouldShowNonMatchingOptions()
	 */
	protected boolean showNonMatchingOptions() {
		return _config.shouldShowNonMatchingOptions();
	}

	/**
	 * @see Config#getSeparateOptionDisplayThreshold()
	 */
	protected int getSeparateOptionDisplayThreshold() {
		return _config.getSeparateOptionDisplayThreshold();
	}

	@Override
	public PolymorphicConfiguration<AbstractTableFilterProvider> getConfig() {
		return _config;
	}
}

