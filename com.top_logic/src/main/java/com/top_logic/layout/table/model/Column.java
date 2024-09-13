/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.beans.Visibility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.renderer.IDColumnTableCellRenderer;
import com.top_logic.layout.tree.renderer.NoResourceProvider;
import com.top_logic.layout.tree.renderer.TreeCellRenderer;
import com.top_logic.util.Resources;
import com.top_logic.util.css.CssUtil;

/**
 * Column or column group of the table {@link Header}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Column {

	private final Header _header;

	private final Column _parent;

	private final ColumnConfiguration _config;

	private final List<Column> _parts;

	/**
	 * @see #getFirstColumnClass()
	 */
	private String _firstColumnClass;

	/**
	 * @see #getLastColumnClass()
	 */
	private String _lastColumnClass;

	/**
	 * @see #getCssClasses()
	 */
	private String _cssClasses;

	private int _size;

	private int _index;

	private int _span;

	private int _depth;

	private boolean _visible;

	private boolean _excluded;

	private final String _name;

	private final CellRenderer _renderer;

	/**
	 * Creates a {@link Column} for the given {@link ColumnConfiguration}.
	 */
	public Column(Header header, String name, ColumnConfiguration config) {
		this(header, null, name, config);
	}

	private Column(Header header, Column parent, String name, ColumnConfiguration config) {
		_header = header;
		_parent = parent;
		_name = name;
		_config = config;
		_parts = mkParts(config.getDeclaredColumns());
		_size = mkSize(_parts);
		_visible = config.isVisible();
		_renderer = createCellRenderer(header, name, config);
	}

	private CellRenderer createCellRenderer(Header header, String name, ColumnConfiguration config) {
		TableConfiguration tableConfiguration = header.getTableConfiguration();

		CellRenderer cellRenderer = config.finalCellRenderer();
		if (name.equals(tableConfiguration.getIDColumn())) {
			return toIdColumn(cellRenderer, tableConfiguration);
		} else {
			return cellRenderer;
		}
	}

	/**
	 * Upgrades a regular table column to an ID column with type image display and toggle buttons in
	 * case of a tree table.
	 */
	public static CellRenderer toIdColumn(CellRenderer cellRenderer, TableConfiguration tableConfig) {
		CellRenderer idCellRenderer = new IDColumnTableCellRenderer(cellRenderer, tableConfig.getRowObjectResourceProvider());

		if (tableConfig.isTree()) {
			return toTreeColumn(idCellRenderer);
		}

		return idCellRenderer;
	}

	private static CellRenderer toTreeColumn(CellRenderer cellRenderer) {
		return new TreeCellRenderer(NoResourceProvider.INSTANCE, cellRenderer,
			TreeCellRenderer.DEFAULT_INDENT_CHARS);
	}

	private static int mkSize(List<Column> parts) {
		if (parts.isEmpty()) {
			return 1;
		}

		int size = 0;
		for (Column part : parts) {
			size += part.getSize();
		}
		return size;
	}

	/**
	 * The column group, this {@link Column} is part of.
	 */
	public Column getParent() {
		return _parent;
	}

	/**
	 * The configuration of this {@link Column}.
	 */
	public ColumnConfiguration getConfig() {
		return _config;
	}

	/**
	 * The index in {@link Header#getColumns()}, <code>-1</code> for {@link #isVisible() invisible}
	 * columns.
	 */
	public int getIndex() {
		return _index;
	}

	/**
	 * The value {@link CellRenderer} for this column.
	 */
	public CellRenderer getValueRenderer() {
		return _renderer;
	}

	/**
	 * The columns of this column group.
	 * 
	 * <p>
	 * Empty for elementary columns.
	 * </p>
	 */
	public List<Column> getParts() {
		return _parts;
	}

	/**
	 * The complete number of elementary descendant columns in this column group.
	 * 
	 * <p>
	 * <code>1</code> for elementary columns.
	 * </p>
	 */
	public int getSize() {
		return _size;
	}

	/**
	 * The number of {@link #isVisible() visible} elementary columns in this column group.
	 * 
	 * <p>
	 * <code>1</code> for visible elementary columns.
	 * </p>
	 * 
	 * <p>
	 * <code>0</code> for column groups with only invisible elementary columns, and for invisible
	 * columns and column groups.
	 * </p>
	 */
	public int getSpan() {
		return _span;
	}

	/**
	 * The maximum grouping level.
	 * 
	 * <p>
	 * <code>1</code> for elementary columns.
	 * </p>
	 */
	public int getDepth() {
		return _depth;
	}

	/**
	 * The space-separated CSS classes to assign to (header and content) cells of this column
	 * (group).
	 */
	public String getCssClasses() {
		return _cssClasses;
	}

	/**
	 * The CSS class to use on those subgroups and columns that appear as the first group or column
	 * within this column group.
	 */
	private String getFirstColumnClass() {
		return _firstColumnClass;
	}

	/**
	 * The CSS class to use on those subgroups and columns that appear as the last group or column
	 * within this column group.
	 */
	private String getLastColumnClass() {
		return _lastColumnClass;
	}

	private List<Column> mkParts(Collection<? extends ColumnConfiguration> columns) {
		if (columns.isEmpty()) {
			return Collections.emptyList();
		}

		ArrayList<Column> result = new ArrayList<>(columns.size());
		for (ColumnConfiguration config : columns) {
			Column subCell = new Column(_header, this, config.getName(), config);
			result.add(subCell);
		}
		return result;
	}

	/**
	 * The name of the {@link Column}.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Whether this {@link Column} is being displayed.
	 */
	public boolean isVisible() {
		return _visible && !_excluded;
	}

	/**
	 * @see #isVisible()
	 */
	public void setVisible(boolean value) {
		_visible = value;
		_header.invalidate();
	}

	/**
	 * Whether this column is completely hidden from the user.
	 * 
	 * <p>
	 * It cannot even be made visible using the column configuration dialog.
	 * </p>
	 * 
	 * <p>
	 * This is the dynamic equivalent of {@link DisplayMode#excluded} in the
	 * {@link ColumnConfiguration}.
	 * </p>
	 */
	public boolean isExcluded() {
		return _excluded;
	}

	/**
	 * If value is <code>true</code>, this {@link Column} behaves as if its
	 * {@link ColumnConfiguration} had {@link ColumnConfiguration#getVisibility() visibility}
	 * {@link DisplayMode#excluded}. The difference is that calling this method with
	 * <code>false</code> switches this {@link Column} to visibility before call of this method.
	 * 
	 * @see #isExcluded()
	 */
	public void setExcluded(boolean value) {
		_excluded = value;
		_header.invalidate();
	}

	/**
	 * Updates {@link #isVisible()} and {@link #isExcluded()} from the given {@link Visibility}.
	 */
	public void applyVisibility(DisplayMode visibility) {
		setVisible(visibility.isDisplayed());
		setExcluded(visibility == DisplayMode.excluded);
	}

	/**
	 * Update {@link #getSpan()} and {@link #getDepth()} and {@link #isVisible()} on column groups
	 * according to the visibility of elementary column contents.
	 * 
	 * @param index
	 *        The first index to assign.
	 * @return The next free index.
	 */
	public int update(int index) {
		updateStyles();

		if (_parts.isEmpty()) {
			if (isVisible()) {
				_index = index++;
				_span = 1;
				_depth = 1;
			} else {
				_index = -1;
				_span = 0;
				_depth = 0;
			}
		} else {
			int span = 0;
			int partDepth = 0;
			for (Column part : _parts) {
				index = part.update(index);

				span += part.getSpan();
				partDepth = Math.max(partDepth, part.getDepth());
			}

			boolean visible = span > 0;

			_span = span;
			if (visible) {
				_index = index;
				_depth = partDepth + 1;
			} else {
				_index = -1;
				_depth = 0;
			}
			setVisible(visible);
		}

		return index;
	}

	private void updateStyles() {
		Column parent = getParent();
		ColumnConfiguration config = getConfig();
		String ownClass = config.getCssClass();
		if (parent == null) {
			_firstColumnClass = config.getCssClassGroupFirst();
			_lastColumnClass = config.getCssClassGroupLast();

			_cssClasses =
				CssUtil.joinCssClasses(ownClass, _firstColumnClass, _lastColumnClass);
		} else {
			List<Column> parentParts = parent.getParts();
			
			boolean firstChild = this == parentParts.get(0);
			if (firstChild) {
				_firstColumnClass =
					CssUtil.joinCssClasses(config.getCssClassGroupFirst(), parent.getFirstColumnClass());
			} else {
				_firstColumnClass = null;
			}
			
			boolean lastChild = this == parentParts.get(parentParts.size() - 1);
			if (lastChild) {
				_lastColumnClass =
					CssUtil.joinCssClasses(config.getCssClassGroupLast(), parent.getLastColumnClass());
			} else {
				_lastColumnClass = null;
			}

			_cssClasses = CssUtil.joinCssClasses(ownClass, CssUtil.joinCssClasses(_firstColumnClass, _lastColumnClass));
		}
	}

	/**
	 * The top-level group of the {@link Column}.
	 */
	public Column getRoot() {
		if (_parent == null) {
			return this;
		}
		return _parent.getRoot();
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Sorts {@link #getParts()} according to the given {@link Comparator}.
	 */
	public void sort(Comparator<Column> columnComparator) {
		for (Column part : _parts) {
			part.sort(columnComparator);
		}
		Collections.sort(_parts, columnComparator);
	}

	/**
	 * The label of this column as displayed e.g. in the column selector.
	 * 
	 * @param table
	 *        The context table.
	 * @return The internationalized column label.
	 */
	public String getLabel(TableData table) {
		TableConfiguration tableConfiguration = table.getTableModel().getTableConfiguration();
		return getLabel(tableConfiguration);
	}

	/**
	 * The label of this column as displayed e.g. in the column selector.
	 * 
	 * @param tableConfiguration
	 *        The context table.
	 * @return The internationalized column label.
	 */
	public String getLabel(TableConfiguration tableConfiguration) {
		return getColumnLabel(tableConfiguration, getConfig(), getName());
	}

	/**
	 * Algorithm to compute a column label.
	 */
	public static String getColumnLabel(TableConfiguration tableConfiguration, ColumnConfiguration columnConfig,
			String columnName) {
		String label = columnConfig.getColumnLabel();
		if (!StringServices.isEmpty(label)) {
			return label;
		}
	
		ResKey labelKey = columnConfig.getColumnLabelKey();
		if (labelKey != null) {
			// Note: the key is an absolute resource name and must not be
			// passed to the getColumnHeaderText() processing.
			return Resources.getInstance().getString(labelKey);
		}
	
		return tableConfiguration.getResPrefix().getStringResource(columnName);
	}

	/**
	 * The tool-tip to use for the {@link #getLabel(TableData)}.
	 */
	public String getTooltip(TableData table) {
		TableConfiguration tableConfiguration = table.getTableModel().getTableConfiguration();
		return getTooltip(tableConfiguration);
	}

	/**
	 * The tool-tip to use for the {@link #getLabel(TableConfiguration)}.
	 */
	public String getTooltip(TableConfiguration tableConfiguration) {
		ColumnConfiguration columnConfig = getConfig();

		ResKey labelKey = columnConfig.getColumnLabelKey();
		if (labelKey != null) {
			// Note: the key is an absolute resource name and must not be
			// passed to the getColumnHeaderText() processing.
			String result = Resources.getInstance().getString(labelKey.tooltipOptional());
			if (result != null) {
				return result;
			}
		}

		ResourceView resPrefix = tableConfiguration.getResPrefix();
		String result = resPrefix.getStringResource(getName() + ResKey.TOOLTIP, null);
		if (result != null) {
			return result;
		}

		// By default use the label also as tooltip to make the column label readable, if the colum
		// width is to small.
		return getLabel(tableConfiguration);
	}

}
