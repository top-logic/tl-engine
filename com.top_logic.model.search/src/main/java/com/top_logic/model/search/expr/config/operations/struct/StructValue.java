/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.struct;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.util.Utils;

/**
 * Memory-optimized immutable {@link Map} associating property "names" with values assuming there
 * are many instances of the same {@link StructType}.
 * 
 * <p>
 * In constrast to e.g. JSON objects, property names are not restricted to strings.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructValue extends AbstractMap<Object, Object> {

	private StructType _type;

	private Object[] _values;

	/**
	 * Creates a {@link StructValue}.
	 *
	 * @param type
	 *        The {@link StructType} describing defined properties.
	 * @param values
	 *        The values of the properties described in the given {@link StructType}.
	 */
	public StructValue(StructType type, Object[] values) {
		assert values.length == type.size() : "Invalid number of arguments";
		_type = type;
		_values = values;
	}

	/**
	 * The type of this {@link StructValue}.
	 */
	public StructType getType() {
		return _type;
	}

	@Override
	public int size() {
		return _type.size();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return _type.hasKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return Arrays.asList(_values).contains(value);
	}

	@Override
	public Object get(Object key) {
		Integer indexOrNull = _type.indexOrNull(key);
		if (indexOrNull == null) {
			return null;
		}
		return internalValue(indexOrNull.intValue());
	}

	final Object internalValue(int index) {
		return _values[index];
	}

	@Override
	public Set<Object> keySet() {
		return _type.getKeySet();
	}

	@Override
	public Collection<Object> values() {
		return Arrays.asList(_values);
	}

	@Override
	public Set<Entry<Object, Object>> entrySet() {
		return new AbstractSet<>() {
			@Override
			public int size() {
				return StructValue.this.size();
			}

			@Override
			public boolean isEmpty() {
				return StructValue.this.isEmpty();
			}

			@Override
			public boolean contains(Object o) {
				if (!(o instanceof Entry)) {
					return false;
				}
				Entry<?, ?> other = (Entry<?, ?>) o;
				if (!containsKey(other.getKey())) {
					return false;
				}

				return Utils.equals(get(other.getKey()), other.getValue());
			}

			@Override
			public Iterator<Entry<Object, Object>> iterator() {
				return new Iterator<>() {
					int _pos = 0;

					int _size = size();

					@Override
					public boolean hasNext() {
						return _pos < _size;
					}

					@Override
					public Entry<Object, Object> next() {
						int pos = _pos++;

						return new Entry<>() {
							@Override
							public Object getKey() {
								return getType().get(pos);
							}

							@Override
							public Object getValue() {
								return internalValue(pos);
							}

							@Override
							public Object setValue(Object value) {
								throw new UnsupportedOperationException();
							}

							@Override
							public int hashCode() {
								return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
							}

							@Override
							public boolean equals(Object obj) {
								if (obj == this) {
									return true;
								} else if (!(obj instanceof Entry)) {
									return false;
								}

								Entry<Object, Object> e1 = this;
								Entry<?, ?> e2 = (Entry<?, ?>) obj;
								return (Objects.equals(e1.getKey(), e2.getKey())
									&& Objects.equals(e1.getValue(), e2.getValue()));
							}

							public final String toString() {
								return getKey() + "=" + getValue();
							}
						};
					}
				};
			}
		};
	}

}
