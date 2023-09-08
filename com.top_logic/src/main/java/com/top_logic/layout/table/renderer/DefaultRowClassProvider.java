/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import static com.top_logic.basic.StringServices.*;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.table.RowClassProvider;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.AbstractTreeTableNode;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.NodeFilterState;
import com.top_logic.util.css.CssUtil;

/**
 * {@link RowClassProvider} that creates striped tables using the following CSS class combinations:
 * 
 * <ul>
 * <li>{@link DefaultTableRenderer#TABLE_ROW_CSS_CLASS}</li>
 * <li>{@link #EVEN_CSS_CLASS}</li>
 * <li>{@link #SELECTED_CSS_CLASS}</li>
 * </ul>
 * 
 * Additionally, one of the following classes is set on the first and last row accordingly:
 * 
 * <ul>
 * <li>{@link #FIRST_ROW_CSS_CLASS}</li>
 * <li>{@link #LAST_ROW_CSS_CLASS}</li>
 * </ul>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultRowClassProvider<C extends DefaultRowClassProvider.Config<?>> extends AbstractConfiguredInstance<C>
		implements RowClassProvider {

	/**
	 * Additional CSS class for even table rows.
	 * 
	 * @see DefaultTableRenderer#TABLE_ROW_CSS_CLASS
	 */
	public static final String EVEN_CSS_CLASS = "tblEven";

	/**
	 * Additional CSS class for selected table rows.
	 * 
	 * @see DefaultTableRenderer#TABLE_ROW_CSS_CLASS
	 */
	public static final String SELECTED_CSS_CLASS = "tblSelected";

	/**
	 * Combination of CSS classes for even rows.
	 */
	public static final String TR_EVEN_CSS_CLASS =
		CssUtil.joinCssClasses(DefaultTableRenderer.TABLE_ROW_CSS_CLASS, EVEN_CSS_CLASS);

	/**
	 * Combination of CSS classes for selected rows.
	 */
	public static final String TR_SELECTED_CSS_CLASS =
		CssUtil.joinCssClasses(DefaultTableRenderer.TABLE_ROW_CSS_CLASS, SELECTED_CSS_CLASS);

	/**
	 * Combination of CSS classes for selected even rows.
	 */
	public static final String TR_SELECTED_EVEN_CSS_CLASS =
		CssUtil.joinCssClasses(DefaultTableRenderer.TABLE_ROW_CSS_CLASS + SELECTED_CSS_CLASS, EVEN_CSS_CLASS);

	/** CSS class added to the first displayed row of the table */
	public static final String FIRST_ROW_CSS_CLASS = "firstRow";

	/** CSS class added to the last displayed row of the table */
	public static final String LAST_ROW_CSS_CLASS = "lastRow";

	/** CSS class added to a non validated row of the table. */
	public static final String FILTER_NO_VALIDATED_MATCH_CSS_CLASS = "tblFilterNotValidated";

	/** CSS class added to a matching row of the filtered table. */
	public static final String FILTER_DIRECT_MATCH_CSS_CLASS = "tblFilterDirectMatch";

	/**
	 * CSS class added to a row of the filtered tree, when the row is not matched by the filter but
	 * displayed because one of its parents or children is matched.
	 */
	public static final String FILTER_INDIRECT_MATCH_CSS_CLASS = "tblFilterIndirectMatch";

	/**
	 * Singleton {@link DefaultRowClassProvider} instance.
	 */
	public static final DefaultRowClassProvider<?> INSTANCE = new DefaultRowClassProvider<>();

	/**
	 * Configuration options for {@link DefaultRowClassProvider}.
	 */
	public interface Config<I extends DefaultRowClassProvider<?>> extends PolymorphicConfiguration<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link DefaultRowClassProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultRowClassProvider(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * Creates a {@link DefaultRowClassProvider}.
	 */
	@SuppressWarnings("unchecked")
	protected DefaultRowClassProvider() {
		this(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			(C) TypedConfiguration.newConfigItem(Config.class));
	}

	@Override
	public String getTRClass(TableControl view, int rowOptions, int displayedRow, int row) {
		boolean isSelected = (rowOptions & TableRenderer.SELECTED) == TableRenderer.SELECTED;
		boolean even = (displayedRow % 2) == 0;
		StringBuilder cssClass = new StringBuilder();
		cssClass.append(nonNull(isSelected ? getTRClassSelected(view) : getTRClass(view, even)));
		if (displayedRow == 0) {
			appendCssClass(cssClass, FIRST_ROW_CSS_CLASS);
		}
		if ((rowOptions & TableRenderer.LAST) == TableRenderer.LAST) {
			appendCssClass(cssClass, LAST_ROW_CSS_CLASS);
		}
		TableViewModel viewModel = view.getViewModel();
		Object rowObject = viewModel.getRowObject(row);
		appendCssClass(cssClass, getFilterCssClass(rowObject, viewModel));

		return cssClass.toString();
	}

	/**
	 * null, if there is no css class.
	 */
	private String getFilterCssClass(Object rowObject, TableViewModel viewModel) {
		if (!viewModel.hasActiveFilters()) {
			return null;
		}

		TableModel applicationModel = viewModel.getApplicationModel();
		if (applicationModel instanceof ObjectTableModel) {
			return getTableFilterCssClass(rowObject, (ObjectTableModel) applicationModel);
		} else {
			return getTreeTableFilterCssClass(rowObject, viewModel);
		}
	}

	private String getTableFilterCssClass(Object rowObject, ObjectTableModel tableModel) {
		if (tableModel.isValidated(rowObject)) {
			return FILTER_DIRECT_MATCH_CSS_CLASS;
		} else {
			return FILTER_NO_VALIDATED_MATCH_CSS_CLASS;
		}
	}

	private String getTreeTableFilterCssClass(Object rowObject, TableViewModel viewModel) {
		if (!viewModel.hasFilterOptions()) {
			return null;
		}
		if (!(rowObject instanceof AbstractTreeTableNode)) {
			return null;
		}
		AbstractTreeTableNode<?> treeTableNode = (AbstractTreeTableNode<?>) rowObject;
		NodeFilterState filterState = treeTableNode.getFilterMatchState();
		if (filterState == NodeFilterState.NOT_VALIDATED) {
			return FILTER_NO_VALIDATED_MATCH_CSS_CLASS;
		} else if (filterState == NodeFilterState.DIRECT_MATCH) {
			return FILTER_DIRECT_MATCH_CSS_CLASS;
		} else {
			return FILTER_INDIRECT_MATCH_CSS_CLASS;
		}
	}

	/**
	 * Append another CSS class to a set of css classes.
	 * 
	 * @param cssClasses
	 *        Is not allowed to be null.
	 * @param additionalCssClass
	 *        null means: Don't append anything.
	 */
	private void appendCssClass(StringBuilder cssClasses, String additionalCssClass) {
		if (additionalCssClass != null) {
			if (cssClasses.length() > 0) {
				cssClasses.append(BLANK_CHAR);
			}
			cssClasses.append(additionalCssClass);
		}
	}

	/**
	 * Returns the name of the class for the currently selected row
	 */
	protected String getTRClassSelected(TableControl view) {
		return TR_SELECTED_CSS_CLASS;
	}

	/**
	 * @param even
	 *        returns a special class if <code>even == true</code>. <code>even = true</code>
	 *        indicates that the row is even.
	 * @return The name of the requested class.
	 */
	protected String getTRClass(TableControl view, boolean even) {
		if (even) {
			return TR_EVEN_CSS_CLASS;
		} else {
			return DefaultTableRenderer.TABLE_ROW_CSS_CLASS;
		}
	}

}
