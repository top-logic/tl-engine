/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action.assertion;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.scripting.check.ValueCheck;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.runtime.action.assertion.ValueAssertionOp;
import com.top_logic.util.Utils;

/**
 * A parameterized assertion about an application value.
 * 
 * @see ValueAssertionOp
 * @see #getComparision()
 * @see CheckAction
 * @see ValueCheck
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface ValueAssertion extends GuiAssertion {

	/**
	 * The mode how the {@link #getActualValue()} is compared to the {@link #getExpectedValue()}.
	 */
	Comparision getComparision();

	/**
	 * Setter for {@link #getComparision()}
	 */
	void setComparision(Comparision comparision);

	/**
	 * Whether the {@link #getComparision()} must evaluate to <code>false</code>.
	 */
	boolean isInverted();

	/**
	 * Setter for {@link #isInverted()}
	 */
	void setInverted(boolean inverted);

	/**
	 * The expected value for the comparison.
	 */
	ModelName getExpectedValue();

	/**
	 * @see #getExpectedValue()
	 */
	void setExpectedValue(ModelName value);

	/**
	 * The actual value retrieved from the application.
	 */
	ModelName getActualValue();

	/**
	 * @see #getActualValue()
	 */
	void setActualValue(ModelName value);

	/**
	 * Comparison mode of {@link ValueAssertion#getActualValue()} and
	 * {@link ValueAssertion#getExpectedValue()}.
	 */
	enum Comparision implements ExternallyNamed {
	
		/**
		 * It is asserted that {@link ValueAssertion#getActualValue()} equals
		 * {@link ValueAssertion#getExpectedValue()}.
		 */
		EQUALS("equals", "matches") {
			@Override
			public boolean compare(ConfigurationItem context, Object actual, Object expected) {
				if (actual instanceof Number && expected instanceof Number) {
					return ((Number) expected).doubleValue() == ((Number) actual).doubleValue();
				}
				return Utils.equals(actual, expected);
			}
		},
	
		/**
		 * It is asserted that the collection {@link ValueAssertion#getActualValue()} equals the
		 * collection {@link ValueAssertion#getExpectedValue()} if both are converted to sets.
		 */
		SET_EQUALS("set-equals", "has the same elements as") {
			@Override
			public boolean compare(ConfigurationItem context, Object actual, Object expected) {
				return CollectionUtil.toSet(asCollectionActual(context, actual)).equals(
					CollectionUtil.toSet(asCollectionExpected(context, expected)));
			}
		},

		/**
		 * It is asserted that the collection {@link ValueAssertion#getActualValue()} equals the
		 * collection {@link ValueAssertion#getExpectedValue()} if both are converted to multi sets.
		 */
		MULTI_SET_EQUALS("multi-set-equals", "has the same elements with same multiplicity as") {
			@Override
			public boolean compare(ConfigurationItem context, Object actual, Object expected) {
				return CollectionUtil.toMultiSet(asCollectionActual(context, actual)).equals(
					CollectionUtil.toMultiSet(asCollectionExpected(context, expected)));
			}
		},

		/**
		 * It is asserted that {@link ValueAssertion#getActualValue()} is a collection and that it
		 * contains the value {@link ValueAssertion#getExpectedValue()}.
		 */
		CONTAINS("contains", "contains") {
			@Override
			public boolean compare(ConfigurationItem context, Object actual, Object expected) {
				return asCollectionActual(context, actual).contains(expected);
			}
		},
	
		/**
		 * It is asserted that {@link ValueAssertion#getActualValue()} is a collection and that it
		 * contains all values of the {@link ValueAssertion#getExpectedValue()} collection.
		 */
		CONTAINS_ALL("contains-all", "contains all of") {
			@Override
			public boolean compare(ConfigurationItem context, Object actual, Object expected) {
				return asCollectionActual(context, actual).containsAll(asCollectionExpected(context, expected));
			}
		},
	
		/**
		 * It is asserted that {@link ValueAssertion#getActualValue()} is a collection and that it
		 * contains at least one value of the {@link ValueAssertion#getExpectedValue()} collection.
		 */
		CONTAINS_SOME("contains-some", "contains some of") {
			@Override
			public boolean compare(ConfigurationItem context, Object actual, Object expected) {
				Collection<?> actualCollection = asCollectionActual(context, actual);
				for (Object expectedEntry : asCollectionExpected(context, expected)) {
					if (actualCollection.contains(expectedEntry)) {
						return true;
					}
				}
				return false;
			}
		},
	
		/**
		 * It is asserted that {@link ValueAssertion#getActualValue()} is a collection and that it
		 * does not contain any of the {@link ValueAssertion#getExpectedValue()} collection.
		 */
		CONTAINS_NONE("contains-none", "contains none of") {
			@Override
			public boolean compare(ConfigurationItem context, Object actual, Object expected) {
				return !CONTAINS_SOME.compare(context, actual, expected);
			}
		},

		/**
		 * It is asserted that {@link ValueAssertion#getActualValue()} is contained in the
		 * {@link ValueAssertion#getExpectedValue()} collection.
		 */
		CONTAINED_IN("contained-in", "is contained in") {
			@Override
			public boolean compare(ConfigurationItem context, Object actual, Object expected) {
				return asCollectionExpected(context, expected).contains(actual);
			}
		},

		/**
		 * It is asserted that {@link ValueAssertion#getActualValue()} is a subset of the
		 * {@link ValueAssertion#getExpectedValue()} collection.
		 */
		ALL_CONTAINED_IN("all-contained-in", "is a subset of") {
			@Override
			public boolean compare(ConfigurationItem context, Object actual, Object expected) {
				return asCollectionExpected(context, expected).containsAll(asCollectionActual(context, actual));
			}
		},

		/**
		 * It is asserted that {@link ValueAssertion#getActualValue()} is lower than the
		 * {@link ValueAssertion#getExpectedValue()}.
		 * 
		 * <p>
		 * Both, the actual and expected value are required to implement {@link Comparable} with a
		 * compatible bound.
		 * </p>
		 */
		LOWER_THAN("lower-than", "is lower than") {
			@Override
			public boolean compare(ConfigurationItem context, Object actual, Object expected) {
				return comare(context, actual, expected) < 0;
			}
		},

		/**
		 * It is asserted that {@link ValueAssertion#getActualValue()} is lower or equal than the
		 * {@link ValueAssertion#getExpectedValue()}.
		 * 
		 * <p>
		 * Both, the actual and expected value are required to implement {@link Comparable} with a
		 * compatible bound.
		 * </p>
		 */
		LOWER_OR_EQUAL("lower-or-equal", "is lower than or equal to") {
			@Override
			public boolean compare(ConfigurationItem context, Object actual, Object expected) {
				return comare(context, actual, expected) <= 0;
			}
		},

		/**
		 * It is asserted that {@link ValueAssertion#getActualValue()} is greater than the
		 * {@link ValueAssertion#getExpectedValue()}.
		 * 
		 * <p>
		 * Both, the actual and expected value are required to implement {@link Comparable} with a
		 * compatible bound.
		 * </p>
		 */
		GREATER_THAN("greater-than", "is greater than") {
			@Override
			public boolean compare(ConfigurationItem context, Object actual, Object expected) {
				return comare(context, actual, expected) > 0;
			}
		},

		/**
		 * It is asserted that {@link ValueAssertion#getActualValue()} is greater or equal than the
		 * {@link ValueAssertion#getExpectedValue()}.
		 * 
		 * <p>
		 * Both, the actual and expected value are required to implement {@link Comparable} with a
		 * compatible bound.
		 * </p>
		 */
		GREATER_OR_EQUAL("greater-or-equal", "is greater than or equal to") {
			@Override
			public boolean compare(ConfigurationItem context, Object actual, Object expected) {
				return comare(context, actual, expected) >= 0;
			}
		};

		protected final Collection<?> asCollectionExpected(ConfigurationItem context, Object expected) {
			if (!(expected instanceof Collection<?>)) {
				ApplicationAssertions.fail(context,
					"The expected value must be a collection, is " + StringServices.debug(expected));
			}
			return asCollection(expected);
		}
	
		protected int comare(ConfigurationItem context, Object actual, Object expected) {
			if (actual instanceof Number && expected instanceof Number) {
				return Double.compare(((Number) expected).doubleValue(), ((Number) actual).doubleValue());
			}

			return asComparable(context, actual).compareTo(asComparable(context, expected));
		}

		private Comparable<Object> asComparable(ConfigurationItem context, Object value) {
			if (value == null) {
				ApplicationAssertions.fail(context, "Null cannot be compared.");
			}

			if (!(value instanceof Comparable<?>)) {
				ApplicationAssertions.fail(context, "Value not comparable: " + StringServices.debug(value));
			}

			@SuppressWarnings("unchecked")
			Comparable<Object> comparable = (Comparable<Object>) value;
			return comparable;
		}

		protected final Collection<?> asCollectionActual(ConfigurationItem context, Object actual) {
			if (actual == null) {
				return Collections.emptyList();
			}

			if (!(actual instanceof Collection<?>)) {
				ApplicationAssertions.fail(context,
					"The actual value must be a collection, is " + StringServices.debug(actual));
			}
			return asCollection(actual);
		}

		private Collection<?> asCollection(Object value) {
			return (Collection<?>) value;
		}

		private String _name;

		private final String _description;
	
		private Comparision(String name, String description) {
			_name = name;
			_description = description;
		}
	
		public abstract boolean compare(ConfigurationItem context, Object actual, Object expected);
	
		@Override
		public String getExternalName() {
			return _name;
		}

		public String getDescription() {
			return _description;
		}

	}

}
