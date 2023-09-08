/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import static com.top_logic.layout.table.filter.LabelFilterProvider.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.AbstractTableFilterProvider;
import com.top_logic.layout.table.component.TableFilterProvider;

/**
 * {@link TableFilterProvider} that creates filters for {@link Collection} valued columns based on
 * rendered labels.
 * 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CollectionLabelFilterProvider extends AbstractTableFilterProvider {

	/** Singleton instance of {@link CollectionLabelFilterProvider}*/
	public static final TableFilterProvider INSTANCE = new CollectionLabelFilterProvider(false);

	@SuppressWarnings("javadoc")
	public interface Config extends AbstractTableFilterProvider.Config {
		@Name(MANDATORY_PROPERTY)
		@BooleanDefault(false)
		boolean isMandatory();
	}

	private static final String MANDATORY_PROPERTY = "mandatory";

	/**
	 * Whether the column displays a mandatory attribute.
	 * 
	 * <p>
	 * In that case, the filter does not display the "no value" option.
	 * </p>
	 */
	private boolean _mandatory;

	/**
	 * Creates a {@link CollectionLabelFilterProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CollectionLabelFilterProvider(InstantiationContext context, Config config) {
		super(context, config);
		_mandatory = config.isMandatory();
	}

	/**
	 * Creates a {@link CollectionLabelFilterProvider}.
	 * 
	 * @param mandatory
	 *        Whether the column value cannot be empty.
	 */
	public CollectionLabelFilterProvider(boolean mandatory) {
		_mandatory = mandatory;
    }

	@Override
	protected final List<ConfiguredFilter> createFilterList(TableViewModel tableModel, String filterPosition) {
		LabelProvider labelProvider = getLabelProvider(tableModel, filterPosition);
		assertValidLabelProvider(labelProvider);
		ConfiguredFilter theFilter =
			new TextFilter(
				new TextFilterConfiguration(tableModel, filterPosition, labelProvider, true,
					showSeparateOptionEntries()),
				new TextFilterViewBuilder(getSeparateOptionDisplayThreshold()),
				labelProvider, Collections.<Class<?>> singletonList(Collection.class),
				showNonMatchingOptions());

		if (_mandatory) {
			return Collections.singletonList(theFilter);
		} else {
			return TableFilterProviderUtil.includeNoValueOption(theFilter);
		}
	}

	private LabelProvider getLabelProvider(TableViewModel tableModel, String filterPosition) {
		return tableModel.getColumnDescription(filterPosition).getFullTextProvider();
	}
}

