/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.equal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.equal.ArrayEqualitySpecification;
import com.top_logic.basic.col.equal.EqualitySpecification;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.config.PropertyKind;

/**
 * Compares {@link ConfigurationItem}s by their values, with control how different
 * {@link PropertyKind kinds of properties} should be compared: {@link CompareMode}
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ConfigEquality extends EqualitySpecification<ConfigurationItem> {

	/**
	 * The hascode for a property that is mandatory but not set.
	 * <p>
	 * The actual value doesn't matter, but should not collide with other common values such as
	 * null, 0, -1, the empty string, etc. The value is therefore an uncommon random number.
	 * </p>
	 */
	private static final int HASHCODE_MANDATORY_UNSET_PROPERTY = -12345;

	private static final int HASH_START_PRIME = 65537; // 2**16 + 1.

	private static final int HASH_MIXIN_PRIME = 37;

	/**
	 * When a loop (infinite recursion) is detected during hash computation, the loop is broken by
	 * using this value instead of the "actual" hash code.
	 */
	private static final int HASH_LOOP = 257;

	/** How should {@link ConfigurationItem}s be compared? */
	@SuppressWarnings("synthetic-access")
	public enum CompareMode {

		/** The {@link ConfigurationItem}s are compared by identity. */
		IDENTITY {

			@Override
			boolean equals(Object left, Object right, ConfigEquality equalsSpec, Set<Set<ConfigurationItem>> stack) {
				return left == right;
			}

			@Override
			int hashCode(Object object, ConfigEquality equalsSpec, Set<ConfigurationItem> stack) {
				return System.identityHashCode(object);
			}
		},

		/** The {@link ConfigurationItem}s are compared by the values of their properties. */
		VALUE {

			@Override
			boolean equals(Object left, Object right, ConfigEquality equalsSpec, Set<Set<ConfigurationItem>> stack) {
				if (CollectionUtil.equals(left, right)) {
					return true;
				}
				if (left instanceof ConfiguredInstance<?> && right instanceof ConfiguredInstance<?>) {
					left = ((ConfiguredInstance<?>) left).getConfig();
					right = ((ConfiguredInstance<?>) right).getConfig();
				}
				if ((left instanceof ConfigurationItem) && (right instanceof ConfigurationItem)) {
					ConfigurationItem itemLeft = (ConfigurationItem) left;
					ConfigurationItem itemRight = (ConfigurationItem) right;

					HashSet<ConfigurationItem> stackFrame = new HashSet<>();
					stackFrame.add(itemLeft);
					stackFrame.add(itemRight);
					if (stack.contains(stackFrame)) {
						// Loop detected. This comparison is already being evaluated.
						// 'true' is correct, as no difference has been found till now,
						// and it allows the comparison to continue;
						return true;
					}
					stack.add(stackFrame);
					boolean result = equalsSpec.equals(itemLeft, itemRight, stack);
					stack.remove(stackFrame);
					return result;
				}
				// Comparison above returned 'false'.
				return false;
			}

			@Override
			int hashCode(Object object, ConfigEquality equalsSpec, Set<ConfigurationItem> stack) {
				if (!(object instanceof ConfigurationItem)) {
					return object.hashCode();
				}
				ConfigurationItem configItem = (ConfigurationItem) object;

				if (stack.contains(configItem)) {
					// Loop detected. This hashCode is already being evaluated.
					return HASH_LOOP;
				}
				stack.add(configItem);
				int result = equalsSpec.hashCode(configItem, stack);
				stack.remove(configItem);
				return result;
			}
		},

		/** The {@link ConfigurationItem}s don't matter for the comparison. */
		IGNORE {

			@Override
			boolean equals(Object left, Object right, ConfigEquality equalsSpec, Set<Set<ConfigurationItem>> stack) {
				return true;
			}

			@Override
			int hashCode(Object object, ConfigEquality equalsSpec, Set<ConfigurationItem> stack) {
				return 0;
			}
		};

		abstract boolean equals(Object left, Object right, ConfigEquality equalsSpec, Set<Set<ConfigurationItem>> stack);

		abstract int hashCode(Object object, ConfigEquality equalsSpec, Set<ConfigurationItem> stack);

	}

	/**
	 * Compares the {@link ConfigurationItem}s by value, but ignores {@link PropertyKind#DERIVED}
	 * properties.
	 * <p>
	 * Is the most useful comparison mode.
	 * </p>
	 * 
	 * @see EqualityByValue
	 */
	public static final ConfigEquality INSTANCE_ALL_BUT_DERIVED =
		new ConfigEquality(CompareMode.VALUE, CompareMode.VALUE, CompareMode.IGNORE, CompareMode.IGNORE);

	private final CompareMode _itemProperties;

	private final CompareMode _referenceProperties;

	private final CompareMode _derivedProperties;

	private final CompareMode _containerProperties;

	/**
	 * Creates a {@link ConfigEquality}.
	 * 
	 * @param itemCompareMode
	 *        How should properties of kind {@link PropertyKind#ITEM}, {@link PropertyKind#ARRAY},
	 *        {@link PropertyKind#LIST} and {@link PropertyKind#MAP} be compared?
	 * @param referenceCompareMode
	 *        How should properties of kind {@link PropertyKind#REF} be compared?
	 * @param derivedProperties
	 *        How should properties of kind {@link PropertyKind#DERIVED} be compared?
	 * @param containerCompareMode
	 *        How should properties that are {@link PropertyDescriptor#hasContainerAnnotation()} be
	 *        compared?
	 */
	public ConfigEquality(CompareMode itemCompareMode, CompareMode referenceCompareMode, CompareMode derivedProperties,
			CompareMode containerCompareMode) {

		_itemProperties = itemCompareMode;
		_referenceProperties = referenceCompareMode;
		_derivedProperties = derivedProperties;
		_containerProperties = containerCompareMode;
	}

	@Override
	protected boolean equalsInternal(ConfigurationItem left, ConfigurationItem right) {
		return equals(left, right, new HashSet<>());
	}

	private boolean equals(ConfigurationItem left, ConfigurationItem right, Set<Set<ConfigurationItem>> stack) {
		if (!equalsTypes(left, right)) {
			return false;
		}
		for (PropertyDescriptor property : left.descriptor().getProperties()) {
			if (!equalsProperty(left, right, property, stack)) {
				return false;
			}
		}

		return true;
	}

	private boolean equalsTypes(ConfigurationItem left, ConfigurationItem right) {
		return getType(left).equals(getType(right));
	}

	private Class<?> getType(ConfigurationItem configItem) {
		return configItem.descriptor().getConfigurationInterface();
	}

	private boolean equalsProperty(ConfigurationItem left, ConfigurationItem right, PropertyDescriptor property,
			Set<Set<ConfigurationItem>> stack) {
		if (property.isMandatory()) {
			boolean isLeftSet = left.valueSet(property);
			boolean isRightSet = right.valueSet(property);
			if ((!isLeftSet) && !isRightSet) {
				return true;
			}
			if ((!isLeftSet) || !isRightSet) {
				/* Both properties are mandatory. If one is set and the other is not set, they are
				 * not equal. */
				return false;
			}
		}

		Object valueLeft = left.value(property);
		Object valueRight = right.value(property);
		
		return equalsPropertyValue(valueLeft, valueRight, property, stack);
	}

	/**
	 * Checks whether the left value and the right value are equal.
	 * 
	 * @param valueLeft
	 *        The value for the given {@link PropertyDescriptor} in the left item. May be
	 *        <code>null</code>.
	 * @param valueRight
	 *        The value for the given {@link PropertyDescriptor} in the right item. May be
	 *        <code>null</code>.
	 * @param property
	 *        The {@link PropertyDescriptor} for which the values are given.
	 * @return Whether the left and right value are equal.
	 */
	protected boolean equalsPropertyValue(Object valueLeft, Object valueRight, PropertyDescriptor property,
			Set<Set<ConfigurationItem>> stack) {
		switch (property.kind()) {
			case REF: {
				return _referenceProperties.equals(valueLeft, valueRight, this, stack);
			}
			case DERIVED: {
				if (property.hasContainerAnnotation()) {
					return _containerProperties.equals(valueLeft, valueRight, this, stack);
				}
				return _derivedProperties.equals(valueLeft, valueRight, this, stack);
			}
			case ITEM: {
				return _itemProperties.equals(valueLeft, valueRight, this, stack);
			}
			case ARRAY: {
				Collection<?> collectionLeft = PropertyDescriptorImpl.arrayAsList(valueLeft);
				Collection<?> collectionRight = PropertyDescriptorImpl.arrayAsList(valueRight);
				return equalsList(collectionLeft, collectionRight, stack);
			}
			case LIST: {
				Collection<?> collectionLeft = (Collection<?>) valueLeft;
				Collection<?> collectionRight = (Collection<?>) valueRight;
				return equalsList(collectionLeft, collectionRight, stack);
			}
			case MAP: {
				Map<?, ?> mapLeft = (Map<?, ?>) valueLeft;
				Map<?, ?> mapRight = (Map<?, ?>) valueRight;

				if (!equalsUnsorted(mapLeft.keySet(), mapRight.keySet(), stack)) {
					return false;
				}
				if (!equalsUnsorted(mapLeft.values(), mapRight.values(), stack)) {
					return false;
				}
				return true;
			}
			default: {
				if (valueLeft instanceof Pattern) {
					// Pattern have no equal. Compare by string representation.
					if (!(valueRight instanceof Pattern)) {
						return false;
					} else {
						Pattern p1 = (Pattern) valueLeft;
						Pattern p2 = (Pattern) valueRight;
						return p1.flags() == p2.flags() && p1.pattern().equals(p2.pattern());
					}
				}
				return ArrayEqualitySpecification.INSTANCE.equals(valueLeft, valueRight);
			}
		}
	}

	private boolean equalsList(Collection<?> left, Collection<?> right, Set<Set<ConfigurationItem>> stack) {
		if (left == right) {
			return true;
		}
		if (left.size() != right.size()) {
			return false;
		}
		Iterator<?> iteratorLeft = left.iterator();
		Iterator<?> iteratorRight = right.iterator();
		while (iteratorLeft.hasNext()) {
			if (!_itemProperties.equals(iteratorLeft.next(), iteratorRight.next(), this, stack)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The current implementation needs O(left*right/4) comparisons, which is O(n²). <br/>
	 * A more efficient implementation would be pre-compute a hash code for each entry in both
	 * collections and use those to prevent most of the comparisons. That should reduce the number
	 * of comparisons to O(n).
	 */
	private boolean equalsUnsorted(Collection<?> left, Collection<?> right, Set<Set<ConfigurationItem>> stack) {
		if (left == right) {
			return true;
		}
		// The comparison has to be done manually to pass the stack along.
		// Otherwise, cycles could not be detected and would cause an infinite recursion.
		if (left.size() != right.size()) {
			return false;
		}
		// Both Collections can contain multiple entries that are 'equal' to each other,
		// even if they are Sets.
		// ('equal' according to this ConfigEquality, not 'equal' according to 'Object.equal'.)
		// That makes comparison more complex, as the following comparison rules are _not enough_:
		// "Both have the same size and right contains everything in left and left contains everything in right."
		// Problem: (a, b, a) = (b, a, b)
		// The rules above would consider them equal, but they are not.
		// Therefore, the comparison process has to 'count' how many of each item are in both
		// collections. To do this, every entry in the right collection which has been 'matched'
		// by an entry from the left collection is being removed.
		List<Object> copyRight = new ArrayList<>(right);
		for (Object valueLeft : left) {
			Iterator<Object> iteratorRight = copyRight.iterator();
			boolean success = false;
			while (iteratorRight.hasNext()) {
				if (_itemProperties.equals(valueLeft, iteratorRight.next(), this, stack)) {
					iteratorRight.remove();
					success = true;
					break;
				}
			}
			if (!success) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected int hashCodeInternal(ConfigurationItem configItem) {
		return hashCode(configItem, new HashSet<>());
	}

	private int hashCode(ConfigurationItem configItem, Set<ConfigurationItem> stack) {
		int result = HASH_START_PRIME;
		for (PropertyDescriptor property : configItem.descriptor().getPropertiesOrdered()) {
			result = HASH_MIXIN_PRIME * (result + hashCodeProperty(configItem, property, stack));
		}
		return result;
	}

	private int hashCodeProperty(ConfigurationItem configItem, PropertyDescriptor property, Set<ConfigurationItem> stack) {
		if (property.isMandatory() && !configItem.valueSet(property)) {
			return HASHCODE_MANDATORY_UNSET_PROPERTY;
		}
		Object value = configItem.value(property);

		return hashCodePropertyValue(value, property, stack);
	}

	/**
	 * Determines the hash code for the given property value.
	 * 
	 * @param value
	 *        The value for the given {@link PropertyDescriptor}. May be <code>null</code>.
	 * @param property
	 *        The {@link PropertyDescriptor} for which the value is given.
	 * 
	 * @return The hash code for the given property value.
	 */
	protected int hashCodePropertyValue(Object value, PropertyDescriptor property, Set<ConfigurationItem> stack) {
		if (value == null) {
			return 0;
		}

		switch (property.kind()) {
			case REF: {
				return _referenceProperties.hashCode(value, this, stack);
			}
			case DERIVED: {
				if (property.hasContainerAnnotation()) {
					return _containerProperties.hashCode(value, this, stack);
				}
				return _derivedProperties.hashCode(value, this, stack);
			}
			case ITEM: {
				return _itemProperties.hashCode(value, this, stack);
			}
			case ARRAY: {
				return hashCodeList(PropertyDescriptorImpl.arrayAsList(value), stack);
			}
			case LIST: {
				return hashCodeList((Collection<?>) value, stack);
			}
			case MAP: {
				int result = HASH_START_PRIME;
				Map<?, ?> map = (Map<?, ?>) value;
				result = HASH_MIXIN_PRIME * (result + hashCodeUnsorted(map.keySet(), stack));
				result = HASH_MIXIN_PRIME * (result + hashCodeUnsorted(map.values(), stack));
				return result;
			}
			default: {
				if (value instanceof Pattern) {
					// Pattern have no hashCode. Hash of string representation.
					Pattern p = (Pattern) value;
					return (p.pattern().hashCode() * 98179 + p.flags()) * 96553;
				}
				return ArrayEqualitySpecification.INSTANCE.hashCode(value);
			}
		}
	}

	private int hashCodeList(Collection<?> collection, Set<ConfigurationItem> stack) {
		int result = HASH_START_PRIME;
		for (Object entry : collection) {
			result = HASH_MIXIN_PRIME * (result + _itemProperties.hashCode(entry, this, stack));
		}
		return result;
	}

	/**
	 * In contrast to {@link #hashCodeList(Collection, Set)} the result of this method is stable,
	 * even if the entries in the given {@link Collection} change their order.
	 */
	private int hashCodeUnsorted(Collection<?> collection, Set<ConfigurationItem> stack) {
		int result = HASH_START_PRIME;
		for (Object entry : collection) {
			// No multiplication here, as that would make order relevant.
			// But this method is for collections where the order doesn't matter.
			result += _itemProperties.hashCode(entry, this, stack);
		}
		return result;
	}

}
