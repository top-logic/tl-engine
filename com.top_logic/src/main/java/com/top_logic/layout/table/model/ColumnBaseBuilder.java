/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.control.TableDescriptionPart;

/**
 * Builder for {@link ColumnBase} configurations.
 * 
 * @param <C>
 *        The concrete column builder type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ColumnBaseBuilder<C extends ColumnBaseBuilder<C>> extends TableDescriptionPart {

	private final Map<String, Object> _data;

	private final C _columnDefaults;

	private final String _name;

	/**
	 * Creates a {@link ColumnBaseBuilder}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param data
	 *        See {@link #getSettings()}.
	 * @param defaultColumn
	 *        See {@link #getDefaults()}.
	 */
	protected ColumnBaseBuilder(String name, Map<String, Object> data, C defaultColumn) {
		_name = name;
		_data = data;
		_columnDefaults = defaultColumn;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	protected Iterator<Entry<String, Object>> getSettings() {
		if (_data == null) {
			return Collections.<String, Object> emptyMap().entrySet().iterator();
		}
		return Collections.unmodifiableMap(_data).entrySet().iterator();
	}

	/**
	 * The fallback configuration.
	 */
	protected C getDefaults() {
		return _columnDefaults;
	}

	@Override
	public boolean isShowHeader() {
		return asBoolean(ColumnBaseConfig.SHOW_HEADER, DEFAULT_SHOW_HEADER);
	}

	@Override
	public void setShowHeader(boolean aShowHeader) {
		set(ColumnBaseConfig.SHOW_HEADER, aShowHeader);
	}

	@Override
	public String getHeadStyle() {
		return asString(ColumnBaseConfig.HEAD_STYLE);
	}

	@Override
	protected void internalSetHeadStyle(String terminatedStyleDefinition) {
		set(ColumnBaseConfig.HEAD_STYLE, terminatedStyleDefinition);
	}

	@Override
	public ControlProvider getHeadControlProvider() {
		return (ControlProvider) get(ColumnBaseConfig.HEAD_CONTROL_PROVIDER);
	}

	@Override
	public void setHeadControlProvider(ControlProvider aHeadControlProvider) {
		set(ColumnBaseConfig.HEAD_CONTROL_PROVIDER, aHeadControlProvider);
	}

	@Override
	public String getColumnLabel() {
		return asString(ColumnBaseConfig.COLUMN_LABEL);
	}

	@Override
	public void setColumnLabel(String value) {
		set(ColumnBaseConfig.COLUMN_LABEL, value);
	}

	@Override
	public ResKey getColumnLabelKey() {
		return asKey(ColumnBaseConfig.COLUMN_LABEL_KEY);
	}

	@Override
	public void setColumnLabelKey(ResKey value) {
		set(ColumnBaseConfig.COLUMN_LABEL_KEY, value);
	}

	/**
	 * The value of the given {@link String}-valued property.
	 */
	protected final String asString(String property) {
		return (String) get(property);
	}

	/**
	 * The value of the given {@link ResKey}-valued property.
	 */
	protected final ResKey asKey(String property) {
		return (ResKey) get(property);
	}

	/**
	 * The value of the given {@link Boolean}-valued property.
	 */
	protected final boolean asBoolean(String property, boolean defaultValue) {
		Boolean value = (Boolean) get(property);
		return value == null ? defaultValue : value;
	}

	/**
	 * The value of the given property.
	 */
	protected final Object get(String property) {
		return get(property, null);
	}

	/**
	 * The value of the given property, or the given default value, if the property is not set.
	 */
	protected final Object get(String property, Object defaultValue) {
		Object directResult = _data.get(property);
		if (directResult == null && !_data.containsKey(property)) {
			if (_columnDefaults == null) {
				return defaultValue;
			} else {
				Object inheritedResult = _columnDefaults.get(property, defaultValue);
				return inheritedResult;
			}
		}
		return directResult;
	}

	/**
	 * Sets the given property to the given value.
	 */
	protected final void set(String property, Object value) {
		checkFrozen();
		_data.put(property, value);
	}

	@Override
	public void freeze() {
		super.freeze();
		_columnDefaults.freeze();
	}

}
