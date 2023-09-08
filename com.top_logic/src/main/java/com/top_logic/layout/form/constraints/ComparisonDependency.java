/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * {@link Constraint} implementation that establishes a dependency between two
 * {@link FormField}s.
 * 
 * <p>
 * This {@link Constraint} ensures that the value in the {@link FormField}, to
 * which this {@link Constraint} is added, compares to the value in some other
 * {@link FormField} in a way that can be specified in the constructor of this
 * class.
 * </p>
 * 
 * <p>
 * If one of the two {@link FormField}s that are compared by this
 * {@link Constraint} have an empty input value, then this dependency is assumed
 * to be fulfilled (The {@link #check(Object)} method returns <code>true</code>.
 * This means that this dependency does not make its dependent fields mandatory.
 * If this is required, add an additional {@link GenericMandatoryConstraint} to
 * each of the fields.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComparisonDependency implements Constraint {
	private FormField dependency;
	private Comparator<Object> valueComparator;

	private ResKey customErrorMessageKey;

	private boolean lowerThanIsValid;
	private boolean equalIsValid;
	private boolean greaterThanIsValid;

	/**
	 * A {@link ComparisonDependency} of type {@link #LOWER_TYPE} ensures that
	 * the value in the field, to which this constraint is added, is lower than
	 * the value in the other field.
	 */
	public static final int LOWER_TYPE = 0;

	/**
	 * A {@link ComparisonDependency} of type {@link #LOWER_OR_EQUALS_TYPE}
	 * ensures that the value in the field, to which this constraint is added,
	 * is lower than or equals to the value in the other field.
	 */
	public static final int LOWER_OR_EQUALS_TYPE = 1;

	/**
	 * A {@link ComparisonDependency} of type {@link #NOT_EQUALS_TYPE} ensures
	 * that the value in the field, to which this constraint is added, is not
	 * equal to the value in the other field.
	 */
	public static final int NOT_EQUALS_TYPE = 2;

	/**
	 * A {@link ComparisonDependency} of type {@link #GREATER_OR_EQUALS_TYPE}
	 * ensures that the value in the field, to which this constraint is added,
	 * is greater than or equals to the value in the other field.
	 */
	public static final int GREATER_OR_EQUALS_TYPE = 3;
	
	
	/**
	 * A {@link ComparisonDependency} of type {@link #GREATER_TYPE} ensures that
	 * the value in the field, to which this constraint is added, is greater
	 * than the value in the other field.
	 */
	public static final int GREATER_TYPE = 4;

	/**
	 * Same as {@link #ComparisonDependency(int, FormField, ResKey)} with <code>null</code> error
	 * message key.
	 */
	public ComparisonDependency(int type, FormField dependency) {
		this(type, dependency, null);
	}

	/**
	 * Construct a new {@link ComparisonDependency} of the given type.
	 * 
	 * <p>
	 * The comparing is performed between the field to which this dependency
	 * is attached and the other field passed to this constructor and is based on
	 * values, which implements the comparable interface.
	 * </p>
	 * 
	 * <p>
	 * If the relation between the values of both fields does not match the
	 * specified type, an error message is generated. If the given custom error
	 * message key is non-<code>null</code>, this key is used to look up an
	 * error message string from the application's resources. Otherwise, a
	 * generic error message is constructed.
	 * </p>
	 */
	public ComparisonDependency(int type, FormField dependency, ResKey customErrorMessageKey) {
		this(type, dependency, null, customErrorMessageKey);
	}
	
	/**
	 * Construct a new {@link ComparisonDependency} of the given type.
	 * 
	 * <p>
	 * The comparing is performed between the field to which this dependency
	 * is attached and the other field passed to this constructor.
	 * </p>
	 * 
	 * <p>
	 * If the relation between the values of both fields does not match the
	 * specified type, an error message is generated. If the given custom error
	 * message key is non-<code>null</code>, this key is used to look up an
	 * error message string from the application's resources. Otherwise, a
	 * generic error message is constructed.
	 * </p>
	 */
	public ComparisonDependency(int type, FormField dependency, Comparator<Object> valueComparator,
			ResKey customErrorMessageKey) {
		assert dependency != null;
		
		this.dependency = dependency;
		this.valueComparator = (valueComparator != null) ? valueComparator : ComparableComparator.INSTANCE;
		this.customErrorMessageKey = customErrorMessageKey;
		
		switch (type) {
		case LOWER_TYPE: {
			lowerThanIsValid = true;
			break;
		}
		case LOWER_OR_EQUALS_TYPE: {
			lowerThanIsValid = equalIsValid = true;
			break;
		}
		case NOT_EQUALS_TYPE: {
			lowerThanIsValid = greaterThanIsValid = true;
			break;
		}
		case GREATER_OR_EQUALS_TYPE: {
			greaterThanIsValid = equalIsValid = true;
			break;
		}
		case GREATER_TYPE: {
			greaterThanIsValid = true;
			break;
		}
		default: {
			throw new IllegalArgumentException("unknown type");
		}
		}
	}
	
	@Override
	public boolean check(Object value) throws CheckException {
		if (! dependency.hasValue()) {
			// Cannot check due to illegal input in dependency field. Defer the
			// check.
			return false;
		}
		
		Object otherValue = dependency.getValue();
		
		if (isEmpty(value) || isEmpty(otherValue)) {
			// The following is *wrong*:
			// 
			// // Cannot check due to missing input. Defer the check.
			// //
			// // return false;
			//
			// The reason is the following: Checking may only be deferred, if and
			// only if one of the dependency fields has *no* value, which means
			// that the user has not yet touched the field and should therefore
			// not be bothered with error messages, which he has not provoked
			// himself.
			//
			// The only option in the case of empty input in one of the compared
			// fields is either to accept this as valid input, or to throw an
			// exception that explains that both fields are required.
			// 
			// The better choice is to return true and accept this situation,
			// because if the fields are mandatory, the application would have
			// assigned an additional constraint to the fields that checks that
			// they may not contain empty input.
			
			return true;
		}
		
		int comparisionResult = compareObjects(value, otherValue);
		
		if ((! lowerThanIsValid) && (comparisionResult < 0)) {
			throw new CheckException(getErrorMessage(value, otherValue));
		}

		if ((! equalIsValid) && (comparisionResult == 0)) {
			throw new CheckException(getErrorMessage(value, otherValue));
		}

		if ((! greaterThanIsValid) && (comparisionResult > 0)) {
			throw new CheckException(getErrorMessage(value, otherValue));
		}
		
		// The value is valid.
		return true;
	}

	private boolean isEmpty(Object value) {
		return value == FormField.EMPTY_INPUT || (value instanceof Collection && ((Collection<?>) value).isEmpty());  
	}

	protected int compareObjects(Object value, Object otherValue) {
	    return valueComparator.compare(value, otherValue);
	}

	protected Object formatForMessage(Object value) {
	    return value;
	}

	private String getErrorMessage(Object value, Object bound) {
	    value = formatForMessage(value);
	    bound = formatForMessage(bound);
		if (customErrorMessageKey != null) {
			return Resources.getInstance().getMessage(customErrorMessageKey, value, bound);
		}
		
		if (equalIsValid && greaterThanIsValid) {
			return Resources.getInstance().getString(I18NConstants.VALUE_TO_SMALL__VALUE_MINIMUM.fill(value, bound));
		} 

		if (equalIsValid && lowerThanIsValid) {
			return Resources.getInstance().getString(I18NConstants.VALUE_TO_BIG__VALUE_MAXIMUM.fill(value, bound));
		} 

		if (lowerThanIsValid && greaterThanIsValid) {
			return Resources.getInstance().getString(I18NConstants.VALUE_MIGHT_NOT_BE_EQUAL_TO__VALUE_TEST.fill(value, bound));
		}

		if (lowerThanIsValid) {
			return Resources.getInstance().getString(I18NConstants.VALUE_TO_BIG_NOT_EQUALS__VALUE_BOUND.fill(value, bound));
		} 

		if (greaterThanIsValid) {
			return Resources.getInstance().getString(I18NConstants.VALUE_TO_SMALL_NOT_EQUALS__VALUE_BOUND.fill(value, bound));
		}
		
		// This might not happen, since the constructor makes sure that only the
		// combinations tested above are possible.
		throw new AssertionError("spurious dependency");
	}

	@Override
	public Collection<FormField> reportDependencies() {
		return Collections.singletonList(dependency);
	}

	/** 
	 * Binds two form fields with a comparison dependency, whereby the values of the fields
	 * must implement the comparable interface.
	 * start field < date > end field
	 * 
	 * @param    aStartField    The start field (which has the lower value).
	 * @param    anEndField     The end field (which has the higher value).
	 */
	public static void buildStartEndDependency(FormField aStartField, FormField anEndField) {
        aStartField.addConstraint(new ComparisonDependency(ComparisonDependency.LOWER_TYPE, anEndField));
        anEndField.addConstraint(new ComparisonDependency(ComparisonDependency.GREATER_TYPE, aStartField));
	}
	
	/** 
	 * Binds two form fields with a comparison dependency, whereby the values of the fields
	 * must be comparable by the given comparator.
	 * start field < date > end field
	 * 
	 * @param    aStartField    The start field (which has the lower value).
	 * @param    anEndField     The end field (which has the higher value).
	 * @param	 valueComparator - the comparator, which can handle the form field values
	 */
	public static void buildStartEndDependency(FormField aStartField, FormField anEndField,
											   Comparator<Object> valueComparator) {
		aStartField.addConstraint(new ComparisonDependency(ComparisonDependency.LOWER_TYPE, anEndField,
														   valueComparator, null));
		anEndField.addConstraint(new ComparisonDependency(ComparisonDependency.GREATER_TYPE, aStartField,
														  valueComparator, null));
	}
	
	/** 
	 * Binds two form fields with a comparison dependency, whereby the values.
	 * start field <= date => end field
	 * 
	 * @param    aStartField    The start field (which has the lower or equal value).
	 * @param    anEndField     The end field (which has the higher or equal value).
	 */
	public static void buildStartEndWithEqualEqualDependency(FormField aStartField, FormField anEndField) {
        aStartField.addConstraint(new ComparisonDependency(ComparisonDependency.LOWER_OR_EQUALS_TYPE, anEndField));
        anEndField.addConstraint(new ComparisonDependency(ComparisonDependency.GREATER_OR_EQUALS_TYPE, aStartField));
	}
	
	/** 
	 * Binds two form fields with a comparison dependency, whereby the values.
	 * start field <= date => end field
	 * 
	 * @param    aStartField    The start field (which has the lower or equal value).
	 * @param    anEndField     The end field (which has the higher or equal value).
	 * @param	 valueComparator - the comparator, which can handle the form field values
	 */
	public static void buildStartEndWithEqualEqualDependency(FormField aStartField, FormField anEndField,
															 Comparator<Object> valueComparator) {
		aStartField.addConstraint(new ComparisonDependency(ComparisonDependency.LOWER_OR_EQUALS_TYPE, anEndField,
														   valueComparator, null));
		anEndField.addConstraint(new ComparisonDependency(ComparisonDependency.GREATER_OR_EQUALS_TYPE, aStartField,
														  valueComparator, null));
	}
}
