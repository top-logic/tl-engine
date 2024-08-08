/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Comparator;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragmentProvider;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.model.export.EmptyPreloadContribution;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.tool.export.pdf.PDFRenderer;

/**
 * Configuration options that are common among columns and column groups.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DisplayOrder({
	ColumnBaseConfig.SHOW_HEADER,
	ColumnBaseConfig.SORTABLE,
	ColumnBaseConfig.SELECTABLE,
	ColumnBaseConfig.VISIBLE,
	ColumnBaseConfig.MANDATORY,
	ColumnBaseConfig.EXCLUDE_FILTER_FROM_SIDEBAR,
	ColumnBaseConfig.VISIBILITY,
	ColumnBaseConfig.COLUMN_WIDTH,
	ColumnBaseConfig.CELL_STYLE,
	ColumnBaseConfig.CSS_CLASS,
	ColumnBaseConfig.CSS_CLASS_GROUP_FIRST,
	ColumnBaseConfig.CSS_CLASS_GROUP_LAST,
	ColumnBaseConfig.HEAD_STYLE,
	ColumnBaseConfig.COLUMN_LABEL,
	ColumnBaseConfig.COLUMN_LABEL_KEY,

	// Makes no sense to display these settings in the UI.

//	ColumnBaseConfig.CLASSIFIER,
//	ColumnBaseConfig.COMMAND_GROUP,
//	ColumnBaseConfig.COMPARATOR,
//	ColumnBaseConfig.DESCENDING_COMPARATOR,
//	ColumnBaseConfig.SORT_KEY_PROVIDER,
//	ColumnBaseConfig.HEAD_CONTROL_PROVIDER,
//	ColumnBaseConfig.ADDITIONAL_HEADERS,
//	ColumnBaseConfig.CELL_EXISTENCE_TESTER,
//	ColumnBaseConfig.CSS_CLASS_PROVIDER,
//	ColumnBaseConfig.ACCESSOR,
//	ColumnBaseConfig.FIELD_PROVIDER,
//	ColumnBaseConfig.CONTROL_PROVIDER,
//	ColumnBaseConfig.CELL_RENDERER,
//	ColumnBaseConfig.FULL_TEXT_PROVIDER,
//	ColumnBaseConfig.LABEL_PROVIDER,
//	ColumnBaseConfig.RESOURCE_PROVIDER,
//	ColumnBaseConfig.RENDERER,
//	ColumnBaseConfig.EDIT_CONTROL_PROVIDER,
//	ColumnBaseConfig.PRELOAD_CONTRIBUTION,
//	ColumnBaseConfig.FILTER_PROVIDER,
//	ColumnBaseConfig.EXCEL_RENDERER,

	// This must be set to get the attributes-based internationalized label as column name.
	ColumnBaseConfig.CONFIGURATORS,
})
public interface ColumnBaseConfig extends ConfigurationItem {

	/**
	 * Configuration option for {@link ColumnBase#isShowHeader()}.
	 */
	String SHOW_HEADER = "showHeader";

	/**
	 * Configuration option for {@link ColumnBase#getHeadStyle()}.
	 */
	String HEAD_STYLE = "headStyle";

	/**
	 * Configuration option for {@link ColumnBase#getColumnLabel()}
	 */
	String COLUMN_LABEL = "columnLabel";

	/**
	 * Configuration option for {@link ColumnBase#getColumnLabelKey()}.
	 */
	String COLUMN_LABEL_KEY = "columnLabelKey";

	/**
	 * Configuration option for {@link ColumnBase#getHeadControlProvider()}.
	 */
	String HEAD_CONTROL_PROVIDER = "headControlProvider";

	/** Property name of {@link #getAdditionalHeaders()}. */
	String ADDITIONAL_HEADERS = "additionalHeaders";

	/**
	 * @see #getAccessor()
	 */
	String ACCESSOR = "accessor";

	/**
	 * @see #getFieldProvider()
	 */
	String FIELD_PROVIDER = "fieldProvider";

	/**
	 * @see #getControlProvider()
	 */
	String CONTROL_PROVIDER = "controlProvider";

	/**
	 * @see #getCellRenderer()
	 */
	String CELL_RENDERER = "cellRenderer";

	/**
	 * @see #getFullTextProvider()
	 */
	String FULL_TEXT_PROVIDER = "fullTextProvider";

	/**
	 * @see #getLabelProvider()
	 */
	String LABEL_PROVIDER = "labelProvider";

	/**
	 * @see #getResourceProvider()
	 */
	String RESOURCE_PROVIDER = "resourceProvider";

	/**
	 * @see #getRenderer()
	 */
	String RENDERER = "renderer";

	/**
	 * @see #getComparator()
	 */
	String COMPARATOR = "comparator";

	/**
	 * @see #getDescendingComparator()
	 */
	String DESCENDING_COMPARATOR = "descendingComparator";

	/**
	 * @see #getSortKeyProvider()
	 */
	String SORT_KEY_PROVIDER = "sortKeyProvider";

	/**
	 * @see #getEditControlProvider()
	 */
	String EDIT_CONTROL_PROVIDER = "editControlProvider";

	/**
	 * @see #getPreloadContribution()
	 */
	String PRELOAD_CONTRIBUTION = "preloadContribution";

	/**
	 * @see #getFilterProvider()
	 */
	String FILTER_PROVIDER = "filterProvider";

	/**
	 * @see #getCellExistenceTester()
	 */
	String CELL_EXISTENCE_TESTER = "cellExistenceTester";

	/**
	 * @see #hasExcludedFilterFromSidebar()
	 */
	String EXCLUDE_FILTER_FROM_SIDEBAR = "excludeFilterFromSidebar";

	/**
	 * @see ColumnConfig#getExcelRenderer()
	 */
	String EXCEL_RENDERER = "excelRenderer";

	/**
	 * @see #getColumnWidth()
	 */
	String COLUMN_WIDTH = "columnWidth";

	/**
	 * @see #getCellStyle()
	 */
	String CELL_STYLE = "cellStyle";

	/**
	 * @see #isSortable()
	 */
	String SORTABLE = "sortable";

	/**
	 * @see #isSelectable()
	 */
	String SELECTABLE = "selectable";

	/**
	 * @see #getVisibility()
	 */
	String VISIBILITY = "visibility";

	/**
	 * @see #isVisible()
	 */
	String VISIBLE = "visible";

	/**
	 * @see #isMandatory()
	 */
	String MANDATORY = "mandatory";

	/**
	 * @see #getCommandGroup()
	 */
	String COMMAND_GROUP = "commandGroup";

	/**
	 * @see #getCssClass()
	 */
	String CSS_CLASS = "cssClass";

	/**
	 * @see #getCssHeaderClass()
	 */
	String CSS_HEADER_CLASS = "cssHeaderClass";

	/**
	 * @see #getCssClassGroupFirst()
	 */
	String CSS_CLASS_GROUP_FIRST = "cssClassGroupFirst";

	/**
	 * @see #getCssClassGroupFirst()
	 */
	String CSS_CLASS_GROUP_LAST = "cssClassGroupLast";

	/**
	 * @see #getCssClassProvider()
	 */
	String CSS_CLASS_PROVIDER = "cssClassProvider";

	/**
	 * @see #getClassifier()
	 */
	String CLASSIFIER = "classifier";

	/**
	 * Special {@link #getClassifier() classifier} to mark a column from being excluded in exports.
	 */
	String CLASSIFIER_NO_EXPORT = "suppressExport";

	/** @see #getConfigurators() */
	String CONFIGURATORS = "configurators";

	/** @see #getPDFRenderer() */
	String PDF_RENDERER = "pdfRenderer";

	/**
	 * whether the header of the column should be shown.
	 * 
	 * @see ColumnConfiguration#isShowHeader()
	 */
	@Name(SHOW_HEADER)
	@BooleanDefault(true)
	boolean isShowHeader();

	/**
	 * @see #isShowHeader()
	 */
	void setShowHeader(boolean value);

	/**
	 * The CSS style to annotate at the header of the column.
	 * 
	 * @see ColumnConfiguration#getHeadStyle()
	 */
	@Name(HEAD_STYLE)
	@Nullable
	String getHeadStyle();

	/**
	 * @see #getHeadStyle()
	 */
	void setHeadStyle(String value);

	/**
	 * The {@link ControlProvider} to produce the control rendering the head of the column,
	 *         or <code>null</code> when none configured.
	 * 
	 * @see ColumnConfiguration#getHeadControlProvider()
	 */
	@Name(HEAD_CONTROL_PROVIDER)
	PolymorphicConfiguration<? extends ControlProvider> getHeadControlProvider();

	/**
	 * @see #getHeadControlProvider()
	 */
	void setHeadControlProvider(PolymorphicConfiguration<? extends ControlProvider> value);

	/**
	 * Internationalised label for this column, or empty string if none configured.
	 * 
	 * @see ColumnConfiguration#getColumnLabel()
	 */
	@Name(COLUMN_LABEL)
	String getColumnLabel();

	/**
	 * @see #getColumnLabel()
	 */
	void setColumnLabel(String value);

	/**
	 * I18N key to produce label for this column, or empty string if none configured.
	 * 
	 * @see ColumnConfiguration#getColumnLabelKey()
	 */
	@Name(COLUMN_LABEL_KEY)
	@InstanceFormat
	ResKey getLabelKey();

	/**
	 * @see #getLabelKey()
	 */
	void setLabelKey(ResKey value);

	/**
	 * {@link Accessor} to access part of the row value that is displayed within the column,
	 *         or <code>null</code> if none configured.
	 * 
	 * @see ColumnConfiguration#getAccessor()
	 */
	@Name(ColumnConfig.ACCESSOR)
	PolymorphicConfiguration<? extends Accessor<?>> getAccessor();

	/**
	 * @see #getAccessor()
	 */
	void setAccessor(PolymorphicConfiguration<? extends Accessor<?>> value);

	/**
	 * {@link CellRenderer} to render cell decorations, <code>null</code> for default rendering.
	 * 
	 * @see ColumnConfiguration#getCellRenderer()
	 */
	@Name(ColumnConfig.CELL_RENDERER)
	PolymorphicConfiguration<? extends CellRenderer> getCellRenderer();

	/**
	 * @see #getCellRenderer()
	 */
	void setCellRenderer(PolymorphicConfiguration<? extends CellRenderer> value);

	/**
	 * {@link Renderer} for cell values, <code>null</code> to use default rendering.
	 * 
	 * @see ColumnConfig#getCellRenderer()
	 * @see ColumnConfiguration#setRenderer(Renderer)
	 */
	@Name(ColumnConfig.RENDERER)
	PolymorphicConfiguration<? extends Renderer<?>> getRenderer();

	/**
	 * @see #getRenderer()
	 */
	void setRenderer(PolymorphicConfiguration<? extends Renderer<?>> value);

	/**
	 * {@link ResourceProvider} to create a {@link CellRenderer} from, or <code>null</code>
	 *         if none configured.
	 * 
	 * @see ColumnConfig#getCellRenderer()
	 * @see ColumnConfiguration#setResourceProvider(ResourceProvider)
	 */
	@Name(ColumnConfig.RESOURCE_PROVIDER)
	PolymorphicConfiguration<? extends ResourceProvider> getResourceProvider();

	/**
	 * @see #getResourceProvider()
	 */
	void setResourceProvider(PolymorphicConfiguration<? extends ResourceProvider> value);

	/**
	 * {@link LabelProvider} to create a {@link CellRenderer} from, or <code>null</code> if
	 *         none configured.
	 * 
	 * @see ColumnConfig#getCellRenderer()
	 * @see ColumnConfiguration#setLabelProvider(LabelProvider)
	 */
	@Name(ColumnConfig.LABEL_PROVIDER)
	PolymorphicConfiguration<? extends LabelProvider> getLabelProvider();

	/**
	 * @see #getLabelProvider()
	 */
	void setLabelProvider(PolymorphicConfiguration<? extends LabelProvider> value);

	/**
	 * {@link LabelProvider} to provide the column value for full-text search.
	 * 
	 * <p>
	 * If not configured, it defaults to either {@link #getLabelProvider()}, or
	 * {@link #getResourceProvider()}, if one of those properties are set.
	 * </p>
	 * 
	 * <p>
	 * If neither {@link #getLabelProvider()} nor {@link #getResourceProvider()} is set, but the
	 * column has an {@link #getAccessor()}, the default for the full-text provider is
	 * {@link MetaLabelProvider}.
	 * </p>
	 * 
	 * @see ColumnConfiguration#setFullTextProvider(LabelProvider)
	 */
	@Name(ColumnConfig.FULL_TEXT_PROVIDER)
	PolymorphicConfiguration<? extends LabelProvider> getFullTextProvider();

	/**
	 * @see #getFullTextProvider()
	 */
	void setFullTextProvider(PolymorphicConfiguration<? extends LabelProvider> value);

	/**
	 * {@link FieldProvider} to produce {@link FormField} for the value of the column, or
	 *         <code>null</code> if none configured.
	 * 
	 * @see ColumnConfiguration#getFieldProvider()
	 */
	@Name(ColumnConfig.FIELD_PROVIDER)
	PolymorphicConfiguration<? extends FieldProvider> getFieldProvider();

	/**
	 * @see #getFieldProvider()
	 */
	void setFieldProvider(PolymorphicConfiguration<? extends FieldProvider> value);

	/**
	 * {@link ControlProvider} to produce a {@link CellRenderer} from, or <code>null</code>
	 *         if none configured.
	 * 
	 * @see ColumnConfig#getCellRenderer()
	 * @see ColumnConfiguration#setControlProvider(ControlProvider)
	 */
	@Name(ColumnConfig.CONTROL_PROVIDER)
	PolymorphicConfiguration<? extends ControlProvider> getControlProvider();

	/**
	 * @see #getControlProvider()
	 */
	void setControlProvider(PolymorphicConfiguration<? extends ControlProvider> value);

	/**
	 * Classifiers are used to mark the columns in an arbitrary way; classifiers are comma separated
	 * strings.
	 * 
	 * @return List of arbitrary markers for this column, or empty list if none are configured.
	 * 
	 * @see ColumnConfiguration#getClassifiers()
	 */
	@Format(CommaSeparatedStrings.class)
	@Name(ColumnConfig.CLASSIFIER)
	List<String> getClassifier();

	/**
	 * @see #getClassifier()
	 */
	void setClassifier(List<String> value);

	/**
	 * Whether the table can be sorted by the content of this column.
	 * 
	 * @see ColumnConfiguration#isSortable()
	 */
	@Name(ColumnConfig.SORTABLE)
	boolean isSortable();

	/**
	 * @see #isSortable()
	 */
	void setSortable(boolean value);

	/**
	 * Whether a click to this column selects the row.
	 * 
	 * @see ColumnConfiguration#isSelectable()
	 */
	@Name(ColumnConfig.SELECTABLE)
	@BooleanDefault(true)
	boolean isSelectable();

	/**
	 * @see #isSelectable()
	 */
	void setSelectable(boolean value);

	/**
	 * Visibility mode of this column.
	 * 
	 * @see ColumnConfiguration#getVisibility()
	 */
	@Name(ColumnConfig.VISIBILITY)
	DisplayMode getVisibility();

	/**
	 * @see #getVisibility()
	 */
	void setVisibility(DisplayMode value);

	/**
	 * Whether the column is visible. Shortcut for {@link #getVisibility()}.
	 * 
	 * @see ColumnConfiguration#isVisible()
	 * @see ColumnConfig#getVisibility()
	 */
	@Name(ColumnConfig.VISIBLE)
	boolean isVisible();

	/**
	 * @see #isVisible()
	 */
	void setVisible(boolean value);

	/**
	 * Whether the column can not be removed from view. Shortcut for
	 *         {@link #getVisibility()}
	 * 
	 * @see ColumnConfiguration#isMandatory()
	 * @see ColumnConfig#getVisibility()
	 */
	@Name(ColumnConfig.MANDATORY)
	boolean isMandatory();

	/**
	 * @see #isMandatory()
	 */
	void setMandatory(boolean value);

	/**
	 * @see ColumnConfiguration#getCommandGroup()
	 */
	@Name(ColumnConfig.COMMAND_GROUP)
	@Nullable
	String getCommandGroup();

	/**
	 * @see #getCommandGroup()
	 */
	void setCommandGroup(String value);

	/**
	 * {@link Comparator} to compare content of this column (ascending), or
	 *         <code>null</code> if not configured.
	 * 
	 * @see ColumnConfiguration#getComparator()
	 * @see ColumnConfig#getDescendingComparator()
	 */
	@Name(ColumnConfig.COMPARATOR)
	@ItemDefault(ComparableComparator.class)
	PolymorphicConfiguration<? extends Comparator<?>> getComparator();

	/**
	 * @see #getComparator()
	 */
	void setComparator(PolymorphicConfiguration<? extends Comparator<?>> value);

	/**
	 * {@link Comparator} to compare content of this column (descending), or
	 *         <code>null</code> if not configured.
	 * 
	 * @see ColumnConfiguration#getDescendingComparator()
	 * @see ColumnConfig#getComparator()
	 */
	@Name(ColumnConfig.DESCENDING_COMPARATOR)
	PolymorphicConfiguration<? extends Comparator<?>> getDescendingComparator();

	/**
	 * @see #getDescendingComparator()
	 */
	void setDescendingComparator(PolymorphicConfiguration<? extends Comparator<?>> value);

	/**
	 * CSS class for content cells of this column.
	 * 
	 * @see ColumnConfiguration#getCssClass()
	 */
	@Name(ColumnConfig.CSS_CLASS)
	@Nullable
	String getCssClass();

	/**
	 * @see #getCssClass()
	 */
	void setCssClass(String value);

	/**
	 * CSS class for this column's header.
	 * 
	 * @see ColumnConfiguration#getCssHeaderClass()
	 */
	@Name(ColumnConfig.CSS_HEADER_CLASS)
	@Nullable
	String getCssHeaderClass();

	/**
	 * @see #getCssHeaderClass()
	 */
	void setCssHeaderClass(String value);

	/**
	 * CSS class for this column if it is the first column in a column group.
	 * 
	 * @see ColumnConfiguration#getCssClassGroupFirst()
	 */
	@Name(ColumnConfig.CSS_CLASS_GROUP_FIRST)
	@Nullable
	String getCssClassGroupFirst();

	/**
	 * @see #getCssClassGroupFirst()
	 */
	void setCssClassGroupFirst(String value);

	/**
	 * CSS class for this column if it is the last column in a column group.
	 * 
	 * @see ColumnConfiguration#getCssClassGroupLast()
	 */
	@Name(ColumnConfig.CSS_CLASS_GROUP_LAST)
	@Nullable
	String getCssClassGroupLast();

	/**
	 * @see #getCssClassGroupLast()
	 */
	void setCssClassGroupLast(String value);

	/**
	 * CSS class for this column, or empty string if none configured.
	 * 
	 * @see ColumnConfiguration#getCssClass()
	 */
	@Name(ColumnConfig.CSS_CLASS_PROVIDER)
	PolymorphicConfiguration<? extends CellClassProvider> getCssClassProvider();

	/**
	 * @see #getCssClassProvider()
	 */
	void setCssClassProvider(PolymorphicConfiguration<? extends CellClassProvider> value);

	/**
	 * true, if filter of this columns shall be not available in filter sidebar, false
	 *         otherwise.
	 * 
	 * @see ColumnConfiguration#hasExcludedFilterFromSidebar()
	 */
	@Name(ColumnConfig.EXCLUDE_FILTER_FROM_SIDEBAR)
	boolean hasExcludedFilterFromSidebar();

	/**
	 * @see #hasExcludedFilterFromSidebar()
	 */
	void setExcludedFilterFromSidebar(boolean excluded);

	/**
	 * {@link TableFilterProvider} to provide {@link TableFilter} for this column, or
	 *         <code>null</code> if none configured.
	 * 
	 * @see ColumnConfiguration#getFilterProvider()
	 */
	@Name(ColumnConfig.FILTER_PROVIDER)
	PolymorphicConfiguration<? extends TableFilterProvider> getFilterProvider();

	/**
	 * @see #getFilterProvider()
	 */
	void setFilterProvider(PolymorphicConfiguration<? extends TableFilterProvider> value);

	/**
	 * {@link CellExistenceTester} for this column, or <code>null</code> if none configured.
	 * 
	 * @see ColumnConfiguration#getCellExistenceTester()
	 */
	@Name(ColumnConfig.CELL_EXISTENCE_TESTER)
	PolymorphicConfiguration<? extends CellExistenceTester> getCellExistenceTester();

	/**
	 * @see #getCellExistenceTester()
	 */
	void setCellExistenceTester(PolymorphicConfiguration<? extends CellExistenceTester> value);

	/**
	 * {@link ExcelCellRenderer} that is used when the value for this column is exported to
	 *         excel, or <code>null</code> if none configured.
	 * 
	 * @see ColumnConfiguration#getExcelRenderer()
	 */
	@Name(ColumnConfig.EXCEL_RENDERER)
	PolymorphicConfiguration<? extends ExcelCellRenderer> getExcelRenderer();

	/**
	 * @see ColumnConfig#getExcelRenderer()
	 */
	void setExcelRenderer(PolymorphicConfiguration<? extends ExcelCellRenderer> value);

	/**
	 * {@link Mapping} that maps value of the column to something that can be compared by
	 *         the {@link Comparator}, or <code>null</code> if none configured.
	 * 
	 * @see ColumnConfiguration#getSortKeyProvider()
	 * @see ColumnConfig#getComparator()
	 */
	@InstanceFormat
	@Name(ColumnConfig.SORT_KEY_PROVIDER)
	@ComplexDefault(Mappings.IdentityMappingDefaultProvider.class)
	Mapping<?, ?> getSortKeyProvider();

	/**
	 * @see #getSortKeyProvider()
	 */
	void setSortKeyProvider(Mapping<?, ?> value);

	/**
	 * @see ColumnConfiguration#getEditControlProvider()
	 */
	@Name(ColumnConfig.EDIT_CONTROL_PROVIDER)
	PolymorphicConfiguration<? extends ControlProvider> getEditControlProvider();

	/**
	 * @see #getEditControlProvider()
	 */
	void setEditControlProvider(PolymorphicConfiguration<? extends ControlProvider> value);

	/**
	 * @see ColumnConfiguration#getPreloadContribution()
	 */
	@InstanceFormat
	@Name(ColumnConfig.PRELOAD_CONTRIBUTION)
	@ComplexDefault(EmptyPreloadContribution.EmptyDefault.class)
	PreloadContribution getPreloadContribution();

	/**
	 * @see #getPreloadContribution()
	 */
	void setPreloadContribution(PreloadContribution value);

	/**
	 * The width of the column, or empty string when not configured.
	 * 
	 * @see ColumnConfiguration#getDefaultColumnWidth()
	 */
	@Name(ColumnConfig.COLUMN_WIDTH)
	@Nullable
	String getColumnWidth();

	/**
	 * @see #getColumnWidth()
	 */
	void setColumnWidth(String value);

	/**
	 * CSS style for the content of this column, or empty string when not configured.
	 * 
	 * @see ColumnConfiguration#getCellStyle()
	 */
	@Name(ColumnConfig.CELL_STYLE)
	@Nullable
	String getCellStyle();

	/**
	 * @see #getCellStyle()
	 */
	void setCellStyle(String value);

	/**
	 * The {@link HTMLFragmentProvider}s for this column's additional headers.
	 * <p>
	 * Each provider represents one additional header. The first entry represents the uppermost
	 * additional column. If columns have different numbers of additional headers, they are filled
	 * top to bottom and the lowest additional columns are left empty.
	 * </p>
	 * <p>
	 * If this {@link List} is empty, or the {@link HTMLFragmentProvider} returns null, the cell is
	 * empty.
	 * </p>
	 * <p>
	 * An {@link AdditionalHeaderControlModel} is passed to
	 * {@link HTMLFragmentProvider#createFragment(Object, String) the create fragment method} as the
	 * first (model) parameter.
	 * </p>
	 */
	@Name(ADDITIONAL_HEADERS)
	List<PolymorphicConfiguration<HTMLFragmentProvider>> getAdditionalHeaders();

	/** Setter for {@link #getAdditionalHeaders()}. */
	void setAdditionalHeaders(List<PolymorphicConfiguration<ControlProvider>> value);

	/**
	 * Additional code to be executed when a {@link ColumnConfig} is applied to a
	 * {@link ColumnConfiguration}.
	 */
	@Name(CONFIGURATORS)
	List<PolymorphicConfiguration<ColumnConfigurator>> getConfigurators();

	/**
	 * The {@link PDFRenderer} producing the output when exporting value of the column to PDF, or
	 * <code>null</code> when none configured.
	 * 
	 * @see ColumnConfiguration#getHeadControlProvider()
	 */
	@Name(PDF_RENDERER)
	PolymorphicConfiguration<? extends PDFRenderer> getPDFRenderer();

	/**
	 * @see #getPDFRenderer()
	 */
	void setPDFRenderer(PolymorphicConfiguration<? extends PDFRenderer> value);

}
