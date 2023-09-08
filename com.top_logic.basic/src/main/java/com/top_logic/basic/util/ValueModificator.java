/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * The class {@link ValueModificator} describes modifications to apply to a {@link Long} larger or
 * equal to a certain one. E.g. if the modification for 5 is -3 and for 8 is 9 then all values less
 * than 5 must not be modified, values greater or equal to 5 but less then 8 must be decreased by 3
 * and all values greater or equal to 8 must be increased by 9
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ValueModificator {

	/**
	 * Rule how to modify values which are larger or equal to {@link ModificationRule#value()}.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private final static class ModificationRule {

		/**
		 * first revision (and all larger) to modify with {@link #modification()}
		 */
		private final long _value;

		/**
		 * this value must be added to all revisions &gt;= {@link #value()}
		 */
		private final long _modification;

		ModificationRule(long value, long modification) {
			_value = value;
			_modification = modification;
		}

		@Override
		public String toString() {
			return "VAL:" + value() + ",MOD:" + modification();
		}

		long value() {
			return _value;
		}

		long modification() {
			return _modification;
		}

	}

	/**
	 * Comparator to find a modification rule in {@link #_modificationsStartValues} which matches a
	 * given {@link Long}, i.e. it can compare {@link Long} and {@link ModificationRule}, by
	 * interpreting a {@link ModificationRule} as its {@link ModificationRule#value() revision}
	 */
	protected static Comparator<Object> MOD_RULE_COMPARATOR = new Comparator<>() {

		@Override
		public int compare(Object o1, Object o2) {
			long val1 = valueOf(o1);
			long val2 = valueOf(o2);
			return Long.compare(val1, val2);
		}

		private long valueOf(Object obj) {
			if (obj instanceof ModificationRule) {
				return ((ModificationRule) obj).value();
			} else {
				assert obj instanceof Long;
				return ((Long) obj).longValue();
			}
		}
	};

	/**
	 * List of {@link ModificationRule}s. The sequence of corresponding
	 * {@link ModificationRule#value() values} is strictly increasing.
	 * <p>
	 * The intended meaning is the following: If some value is lower than the
	 * {@link ModificationRule#value() value} of {@link ModificationRule} at index <code>0</code>,
	 * then no modification is necessary. If <code>i</code> is the greatest index such that the
	 * {@link ModificationRule#value() value} of {@link ModificationRule} at index <code>i</code> is
	 * lower or equal to a value <code>r</code>, then the corresponding
	 * {@link ModificationRule#modification()} must be applied to <code>r</code>.
	 * </p>
	 */
	private ArrayList<ModificationRule> _modificationsStartValues = new ArrayList<>();

	/**
	 * Finds the modification which must be applied to the given value
	 * 
	 * @param value
	 *        the value to find modification
	 * 
	 * @return the modification for that value
	 */
	public long getModificationValue(final Long value) {
		final int entry =
			Collections.binarySearch(_modificationsStartValues, value, ValueModificator.MOD_RULE_COMPARATOR);
		long modification;
		if (entry >= 0) {
			modification = _modificationsStartValues.get(entry).modification();
		} else {
			int modIndex = -(entry + 1);
			if (modIndex == 0) {
				modification = 0;
			} else {
				modification = _modificationsStartValues.get(modIndex - 1).modification();
			}
		}
		return modification;
	}

	/**
	 * Adds a new modification rule.
	 * 
	 * @param value
	 *        the start point from which on the given modification is valid. It must be a
	 *        modification start point added before.
	 * @param modification
	 *        the modification which must be applied to all values larger or equal to the given one.
	 * 
	 * @throws IllegalArgumentException
	 *         iff the given value was formerly added as modification value
	 */
	public void addModification(final long value, final long modification) throws IllegalArgumentException {
		final ModificationRule newRule = new ModificationRule(value, modification);
		if (_modificationsStartValues.isEmpty()
			|| _modificationsStartValues.get(_modificationsStartValues.size() - 1).value() < value) {
			_modificationsStartValues.add(newRule);
		} else {
			final int index = Collections.binarySearch(_modificationsStartValues, newRule, MOD_RULE_COMPARATOR);
			if (index >= 0) {
				final ModificationRule oldRule = _modificationsStartValues.get(index);
				throw new IllegalArgumentException("There is already a modification for value " + value + " Rule:"
					+ oldRule);
			} else {
				_modificationsStartValues.add(-index - 1, newRule);
			}

		}
	}
	
	/**
	 * Drops all modification of the given value and all values greater that the given value.
	 */
	public void clearFrom(long value) {
		while (_modificationsStartValues.size() > 0) {
			int last = _modificationsStartValues.size() - 1;
			if (_modificationsStartValues.get(last).value() >= value) {
				_modificationsStartValues.remove(last);
			} else {
				break;
			}
		}
	}

	@Override
	public String toString() {
		return _modificationsStartValues.toString();
	}

}
