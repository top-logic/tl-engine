/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.form.boxes.model.Icons;

/**
 * Default mutable {@link GroupSettings} implementation.
 * 
 * @param <S> The self-type returned by setters.
 */
public abstract class AbstractGroupSettings<S extends AbstractGroupSettings<S>> implements GroupSettings {

	private boolean _preventCollapse = false;

	private int _columns;

	private int _rows;

	private String _cssClass;

	private String _style;

	private String _width;

	private Boolean _labelAbove = null;

	private Boolean _hasBorder = null;

	private boolean _wholeLine = true;

	private boolean _hasLegend = true;

	private boolean _isDraggable = false;

	private String _dataId = null;

	private Menu _commands = null;

	private ThemeImage _commandButton = Icons.BURGER_MENU;

	@Override
	public int getColumns() {
		return _columns;
	}

	/**
	 * @see #getColumns()
	 */
	public S setColumns(int columns) {
		_columns = columns;
		return self();
	}

	@Override
	public String getCssClass() {
		return _cssClass;
	}

	/**
	 * @see #getCssClass()
	 */
	public S setCssClass(String cssClass) {
		_cssClass = cssClass;
		return self();
	}

	@Override
	public String getStyle() {
		return _style;
	}

	/**
	 * @see #getStyle()
	 */
	public S setStyle(String style) {
		_style = style;
		return self();
	}

	@Override
	public boolean isWholeLine() {
		return _wholeLine;
	}

	/**
	 * @see #isWholeLine()
	 */
	public S setWholeLine(boolean wholeLine) {
		_wholeLine = wholeLine;
		return self();
	}

	@Override
	public boolean getLabelAbove() {
		return _labelAbove == null ? com.top_logic.layout.form.boxes.reactive_tag.Icons.LABEL_ABOVE.get()
			: _labelAbove.booleanValue();
	}

	/**
	 * @see #getLabelAbove()
	 */
	public S setLabelAbove(boolean labelAbove) {
		_labelAbove = labelAbove;
		return self();
	}

	@Override
	public boolean hasBorder() {
		return _hasBorder == null ? com.top_logic.layout.Icons.GROUP_BORDER.get() : _hasBorder.booleanValue();
	}

	/**
	 * @see #hasBorder()
	 */
	public S setHasBorder(Boolean hasBorder) {
		_hasBorder = hasBorder;
		return self();
	}

	@Override
	public boolean hasLegend() {
		return _hasLegend;
	}

	/**
	 * @see #hasLegend()
	 */
	public S setHasLegend(boolean legend) {
		_hasLegend = legend;
		return self();
	}

	@Override
	public boolean isDraggable() {
		return _isDraggable;
	}

	/**
	 * @see #isDraggable()
	 */
	public S setIsDraggable(boolean isDraggable) {
		_isDraggable = isDraggable;
		return self();
	}

	@Override
	public String getDataId() {
		return _dataId;
	}

	/**
	 * @see #getDataId()
	 */
	public S setDataId(String dataId) {
		_dataId = dataId;
		return self();
	}

	@Override
	public boolean isCollapsible() {
		return !_preventCollapse;
	}

	/**
	 * @see #isCollapsible()
	 */
	public S setPreventCollapse(boolean preventCollapse) {
		_preventCollapse = preventCollapse;
		return self();
	}

	@Override
	public int getRows() {
		return _rows;
	}

	/**
	 * @see #getRows()
	 */
	public S setRows(int rows) {
		_rows = rows;
		return self();
	}

	@Override
	public String getWidth() {
		return _width;
	}

	/**
	 * @see #getWidth()
	 */
	public S setWidth(String width) {
		_width = width;
		return self();
	}

	@Override
	public Menu getMenu() {
		return _commands;
	}

	/**
	 * @see #getMenu()
	 */
	public S setMenu(Menu commands) {
		_commands = commands;
		return self();
	}

	@Override
	public ThemeImage getCommandButton() {
		return _commandButton;
	}

	/**
	 * @see #getCommandButton()
	 */
	public S setCommandButton(ThemeImage commandButton) {
		_commandButton = commandButton;
		return self();
	}

	/**
	 * The settings instance with concrete type.
	 */
	protected abstract S self();
	
}