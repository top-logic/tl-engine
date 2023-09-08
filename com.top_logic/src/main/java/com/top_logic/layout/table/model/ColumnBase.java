/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.top_logic.basic.col.AbstractFreezable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.util.Utils;
import com.top_logic.util.css.CssUtil;

/**
 * Configuration that is common for columns and column groups.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ColumnBase extends AbstractFreezable {

	/**
	 * Pseudo attribute that makes the parser de-facto ignore the name attribute of the column,
	 * because this attribute is handled explicitly during parsing.
	 */
	private static final String INTERNAL_ATTRIBUTE_NAME = ColumnConfig.NAME_ATTRIBUTE;

	/**
	 * Whether the column header is shown.
	 */
	public abstract boolean isShowHeader();

	/**
	 * @see #isShowHeader()
	 */
	public abstract void setShowHeader(boolean aShowHeader);

	/**
	 * @see #isShowHeader()
	 */
	protected abstract void copyShowHeader(boolean aShowHeader);

	/**
	 * This method returns the style which is special for the "TH" element of the column described
	 * by this {@link ColumnConfiguration}.
	 * 
	 * @return may be <code>null</code>
	 */
	public abstract String getHeadStyle();

	/**
	 * This method sets the style which is special for the "TH" element of the column described by
	 * this {@link ColumnConfiguration}.
	 * 
	 * @param headStyle
	 *        must be <code>null</code> or usable as HTML 'style'- value.
	 */
	public final void setHeadStyle(String headStyle) {
		String terminatedStyleDefinition = CssUtil.terminateStyleDefinition(headStyle);
		internalSetHeadStyle(terminatedStyleDefinition);
	}

	/**
	 * @see #getHeadStyle()
	 */
	protected abstract void copyHeadStyle(String terminatedStyleDefinition);

	/**
	 * Store the already normalized style definition.
	 */
	protected abstract void internalSetHeadStyle(String terminatedStyleDefinition);

	/**
	 * The {@link ControlProvider} that creates the {@link Control} to be rendered in the header
	 * area.
	 */
	public abstract ControlProvider getHeadControlProvider();

	/**
	 * @see #getHeadControlProvider()
	 */
	public abstract void setHeadControlProvider(ControlProvider aHeadControlProvider);

	/**
	 * @see #getHeadControlProvider()
	 */
	protected abstract void copyHeadControlProvider(ControlProvider aHeadControlProvider);

	/**
	 * Completely internationalized column label.
	 * 
	 * <p>
	 * This option takes precedence over {@link #getColumnLabelKey()}.
	 * </p>
	 */
	public abstract String getColumnLabel();

	/**
	 * Setter for {@link #getColumnLabel()}.
	 */
	public abstract void setColumnLabel(String value);

	/**
	 * @see #getColumnLabel()
	 */
	protected abstract void copyColumnLabel(String value);

	/**
	 * Fully qualified resource key to produce the column label.
	 */
	public abstract ResKey getColumnLabelKey();

	/**
	 * Setter for {@link #getColumnLabelKey()}.
	 */
	public abstract void setColumnLabelKey(ResKey value);

	/**
	 * @see #getColumnLabelKey()
	 */
	public abstract void copyColumnLabelKey(ResKey value);

	@SuppressWarnings({ "rawtypes", "javadoc" })
	protected final void updateFrom(PropertyUpdater updater, Iterator<Entry<String, Object>> values) {
		while (values.hasNext()) {
			Entry<String, Object> entry = values.next();

			String configName = entry.getKey();
			AbstractProperty property = getProperties().get(configName);

			Object value = entry.getValue();
			updater.update(this, property, value);
		}
	}

	/**
	 * Extracts all settings of this {@link ColumnConfiguration} to a map.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Iterator<Entry<String, Object>> getSettings() {
		class SettingsIterator implements Iterator<Entry<String, Object>>, Entry<String, Object> {

			private final Iterator<Entry<String, AbstractProperty>> _base;

			private Entry<String, AbstractProperty> _current;

			private String _propertyName;

			private Object _value;

			private SettingsIterator(Iterator<Entry<String, AbstractProperty>> base) {
				_base = base;
			}

			@Override
			public boolean hasNext() {
				while (_base.hasNext()) {
					_current = _base.next();
					if (_current.getValue().isCopyableProperty(ColumnBase.this)) {
						return true;
					}
				}
				_current = null;
				return false;
			}

			@Override
			public Entry<String, Object> next() {
				if (_current == null) {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}
				}
				_propertyName = _current.getKey();
				AbstractProperty property = _current.getValue();
				_value = property.get(ColumnBase.this);

				return this;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getKey() {
				return _propertyName;
			}

			@Override
			public Object getValue() {
				return _value;
			}

			@Override
			public Object setValue(Object value) {
				throw new UnsupportedOperationException();
			}
		}

		return new SettingsIterator(getProperties().entrySet().iterator());
	}

	/**
	 * The {@link AbstractProperty properties} supported by this configuration.
	 */
	@SuppressWarnings("rawtypes")
	protected Map<String, AbstractProperty> getProperties() {
		return BASE_PROPERTIES;
	}

	static abstract class BaseProperty extends AbstractProperty<ColumnBase> {
		protected BaseProperty(String configName) {
			super(configName);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	static Map<String, AbstractProperty> BASE_PROPERTIES = TableUtil.index(
		new ColumnConfiguration.Property(INTERNAL_ATTRIBUTE_NAME) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				if (!Utils.equals(value, self.getName())) {
					throw new IllegalArgumentException("Column name cannot be changed.");
				}
			}

			@Override
			public boolean isCopyableProperty(ColumnConfiguration self) {
				// Property is internal and should not be copied or adapted.
				return false;
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getName();
			}
		},
		new BaseProperty(ColumnBaseConfig.SHOW_HEADER) {
			@Override
			public void set(ColumnBase self, Object value) {
				self.setShowHeader((Boolean) value);
			}

			@Override
			public void copy(ColumnBase self, Object value) {
				self.copyShowHeader((Boolean) value);
			}

			@Override
			public Object get(ColumnBase self) {
				return self.isShowHeader();
			}
		},
		new BaseProperty(ColumnBaseConfig.HEAD_STYLE) {
			@Override
			public void set(ColumnBase self, Object value) {
				self.setHeadStyle((String) value);
			}

			@Override
			public void copy(ColumnBase self, Object value) {
				self.copyHeadStyle((String) value);
			}

			@Override
			public Object get(ColumnBase self) {
				return self.getHeadStyle();
			}
		},
		new BaseProperty(ColumnBaseConfig.HEAD_CONTROL_PROVIDER) {
			@Override
			public void set(ColumnBase self, Object value) {
				self.setHeadControlProvider((ControlProvider) value);
			}

			@Override
			public void copy(ColumnBase self, Object value) {
				self.copyHeadControlProvider((ControlProvider) value);
			}

			@Override
			public Object get(ColumnBase self) {
				return self.getHeadControlProvider();
			}
		},
		new BaseProperty(ColumnBaseConfig.COLUMN_LABEL) {
			@Override
			public void set(ColumnBase self, Object value) {
				self.setColumnLabel((String) value);
			}

			@Override
			public void copy(ColumnBase self, Object value) {
				self.copyColumnLabel((String) value);
			}

			@Override
			public Object get(ColumnBase self) {
				return self.getColumnLabel();
			}
		},
		new BaseProperty(ColumnBaseConfig.COLUMN_LABEL_KEY) {
			@Override
			public void set(ColumnBase self, Object value) {
				self.setColumnLabelKey((ResKey) value);
			}

			@Override
			public void copy(ColumnBase self, Object value) {
				self.copyColumnLabelKey((ResKey) value);
			}

			@Override
			public Object get(ColumnBase self) {
				return self.getColumnLabelKey();
			}
		});

	private interface PropertyUpdater {
		void update(ColumnBase source, AbstractProperty<?> target, Object value);
	}

	@SuppressWarnings("javadoc")
	protected static class PropertySetter implements PropertyUpdater {

		public static final PropertyUpdater INSTANCE = new PropertySetter();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void update(ColumnBase source, AbstractProperty target, Object value) {
			target.set(source, value);
		}
	}

	@SuppressWarnings("javadoc")
	protected static class PropertyCopier implements PropertyUpdater {

		public static final PropertyUpdater INSTANCE = new PropertyCopier();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void update(ColumnBase source, AbstractProperty target, Object value) {
			target.copy(source, value);
		}
	}

}
