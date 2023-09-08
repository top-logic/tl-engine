/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import static java.util.Arrays.*;

import java.util.List;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.iterator.AppendIterator;
import com.top_logic.basic.col.iterator.IteratorUtil;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.tools.NameBuilder;

/**
 * The "null-ability" aspect of a {@link PropertyDescriptor}.
 * 
 * @see Nullable
 * @see NullDefault
 * @see NonNullable
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NullableAspect {

	private final PropertyDescriptorImpl _property;

	private final boolean _nullable;

	NullableAspect(PropertyDescriptorImpl property, Protocol protocol) {
		if (property == null) {
			throw new NullPointerException("Property is not allowed to be null.");
		}
		_property = property;
		_nullable = calcNullable();
		checkConsistency(protocol);
		adaptValueProvider();
	}

	private boolean calcNullable() {
		if (getProperty().getType().isPrimitive()) {
			return false;
		}
		if (isAnnotatedAsNullable()) {
			return true;
		}
		if (isAnnotatedAsNonNullable()) {
			return false;
		}
		if (hasParents(getProperty())) {
			// All parents agree, or an error will be reported in checkConsistency().
			// Therefore, take the value from _any_ parent.
			return getAnyParent(getProperty()).isNullable();
		}
		switch (getProperty().kind()) {
			case PLAIN: {
				return getProperty().getValueProvider().isLegalValue(null);
			}
			case COMPLEX: {
				return getProperty().getValueBinding().isLegalValue(null);
			}
			case ITEM: {
				return true;
			}
			case ARRAY: {
				return false;
			}
			case LIST: {
				return false;
			}
			case MAP: {
				return false;
			}
			case REF: {
				return true;
			}
			case DERIVED: {
				return true;
			}
			default: {
				throw new UnreachableAssertion("Unknown PropertyKind: " + getProperty().kind());
			}
		}
	}

	private static PropertyDescriptor getAnyParent(PropertyDescriptor property) {
		return property.getSuperProperties()[0];
	}

	private void checkConsistency(Protocol protocol) {
		if (isAnnotatedAsNonNullable() && isAnnotatedAsNullable()) {
			error(protocol, "Property is annotated as nullable and non-nullable at the same time."
				+ " That makes no sense and is forbidden.");
			return;
		}
		if (!hasLocalAnnotation()) {
			for (PropertyDescriptor superProperty : getAllParents()) {
				if (superProperty.isNullable() != isNullable()) {
					error(protocol, "At least one of the super-properties is nullable"
						+ " and at least one is none-nullable. That is a conflict."
						+ " It is possible to solve that with a local non-nullable annotation,"
						+ " if there is no inherited setter.");
					return;
				}
			}
			return;
		}
		if (getProperty().getType().isPrimitive() && isAnnotatedAsNullable()) {
			error(protocol, "A property storing a Java primitive cannot be declared as nullable.");
			return;
		}
		if (isNullable()) {
			PropertyKind kind = getProperty().kind();
			if ((kind == PropertyKind.LIST) || (kind == PropertyKind.MAP) || (kind == PropertyKind.ARRAY)) {
				error(protocol, "Properties of kind 'LIST', 'MAP', and 'ARRAY' cannot be nullable.");
				return;
			}
			if (!hasParents(getProperty())) {
				return;
			}
			if (isAnyParentNonNullable()) {
				error(protocol, "A property cannot be made nullable, if it has an non-nullable parent."
					+ " That would break the contract of the inherited getter:"
					+ " It's callers don't expect it to return null.");
				return;
			}
		}
	}

	/**
	 * Retrieves all parents recursively in no guaranteed order.
	 * <p>
	 * The {@link #getProperty()} itself is not contained in the returned {@link List}.
	 * </p>
	 */
	private List<PropertyDescriptorImpl> getAllParents() {
		AppendIterator<PropertyDescriptorImpl> iterator = IteratorUtil.createAppendIterator();
		iterator.appendAll(asList(getProperty().getSuperProperties()));
		while (iterator.hasNext()) {
			iterator.appendAll(asList(iterator.next().getSuperProperties()));
		}
		return iterator.copyUnderlyingCollection();
	}

	private boolean isAnyParentNonNullable() {
		for (PropertyDescriptor superProperty : getAllParents()) {
			if (!superProperty.isNullable()) {
				return true;
			}
		}
		return false;
	}

	private void error(Protocol protocol, String message) {
		String fullMessage = message + " Property '" + getProperty().toString() + "'.";
		protocol.error(fullMessage);
	}

	private static boolean hasParents(PropertyDescriptor property) {
		return property.getSuperProperties().length > 0;
	}

	private void adaptValueProvider() {
		if (isNullable() && isValueProvider(StringValueProvider.INSTANCE.getClass())) {
			getProperty().setValueProvider(NullableString.INSTANCE);
		} else if (isNonNullable() && isValueProvider(NullableString.class)) {
			getProperty().setValueProvider(StringValueProvider.INSTANCE);
		}
	}

	private boolean isValueProvider(Class<?> valueProviderType) {
		ConfigurationValueProvider<?> valueProvider = getProperty().getValueProvider();
		if (valueProvider == null) {
			return false;
		}
		/* Check for exact match, not just if it is "the value provider or a subclass of it", as the
		 * subclass is a different value provider as it behaves differently. */
		return valueProviderType.equals(valueProvider.getClass());
	}

	/**
	 * The {@link PropertyDescriptor} to which this belongs.
	 */
	private PropertyDescriptorImpl getProperty() {
		return _property;
	}

	/**
	 * Is there a local (i.e. non-inherited) annotation specifying whether the property is nullable
	 * or not?
	 */
	public boolean hasLocalAnnotation() {
		return isAnnotatedAsNullable() || isAnnotatedAsNonNullable();
	}

	private boolean isAnnotatedAsNonNullable() {
		return getProperty().hasLocalAnnotation(NonNullable.class);
	}

	private boolean isAnnotatedAsNullable() {
		return getProperty().hasLocalAnnotation(Nullable.class) || getProperty().hasLocalAnnotation(NullDefault.class);
	}

	/**
	 * Has the user specified _explicit_ whether the property is nullable?
	 * <p>
	 * Equivalent to: Is there an (inherited) annotation specifying whether the property is
	 * nullable? If not, "nullable or not" has been derived from the {@link PropertyKind}, the
	 * {@link PropertyDescriptor#getValueProvider() value provider} and the
	 * {@link PropertyDescriptor#getValueBinding() value binding}.
	 * </p>
	 */
	public boolean isExplicit() {
		if (hasLocalAnnotation()) {
			return true;
		}
		for (PropertyDescriptorImpl superProperty : getProperty().getSuperProperties()) {
			if (superProperty.getNullableAspect().isExplicit()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see #isExplicit()
	 */
	public boolean isImplicit() {
		return !isExplicit();
	}

	/**
	 * Is the
	 */
	public boolean isNullable() {
		return _nullable;
	}

	/**
	 * @see #isNullable()
	 */
	public boolean isNonNullable() {
		return !_nullable;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("nullable", Boolean.toString(_nullable))
			.add("explicit", Boolean.toString(isExplicit()))
			.add("localAnnotation", Boolean.toString(hasLocalAnnotation()))
			.add("property", _property.toString())
			.buildName();
	}

}
