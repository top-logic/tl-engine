/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CollectionStorage} that stores a set of strings.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringSetStorage<C extends StringSetStorage.Config<?>> extends CollectionStorage<C> {

	/**
	 * Configuration options for {@link StringSetStorage}.
	 */
	@TagName("string-set-storage")
	public interface Config<I extends StringSetStorage<?>> extends CollectionStorage.Config<I> {

		/** @see #getResultLocator() */
		String RESULT_PROPERTY = "result";

		/**
		 * TODO: What's this?
		 */
		@Name(RESULT_PROPERTY)
		AttributeValueLocator getResultLocator();

	}

	/** Separate attribute values in the stored combined value. */
	protected static final char VALUE_SEPARATOR = ',';

	private AttributeValueLocator _resultLocator;

	/**
	 * Creates a {@link StringSetStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StringSetStorage(InstantiationContext context, C config) {
		super(context, config);
		_resultLocator = config.getResultLocator();
	}

	/**
	 * @see Config#getResultLocator()
	 */
	public AttributeValueLocator getResultLocator() {
		return _resultLocator;
	}

	@Override
	public void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException,
			AttributeException {
		if (!(aValue instanceof Collection)) {
			throw new IllegalArgumentException("Given value is no Collection");
		}
		Collection theValue = (Collection) aValue;

		// Check attribute
		AttributeUtil.checkHasAttribute(object, attribute);

		// Check params
		if (theValue == null) {
			throw new IllegalArgumentException("Values must not be null for attribute " + attribute);
		}
		if (attribute.isMandatory() && theValue.isEmpty()) {
			throw new IllegalArgumentException("Empty collection not allowed for mandatory attibute " + attribute);
		}

		// Set values
		int theSize = theValue.size();
		if (theSize == 0) { // Empty
			object.tSetData(attribute.getName(), null);
		}

		StringBuffer theValS = new StringBuffer(theSize * 10);
		Set theValues = (theValue instanceof Set) ? (Set) theValue : new HashSet(theValue); // Avoid
																							// duplicates
		Iterator theVals = theValues.iterator();
		Set theCurrVal = new HashSet((Collection) getAttributeValue(object, attribute));
		boolean hasChanged = false;
		int theCount = 1;
		while (theVals.hasNext()) {
			Object theVal = theVals.next();

			// Check value
			checkBasicValue(theVal, object);

			String theRealVal = getBasicValue(theVal);
			if (theCurrVal.contains(theRealVal)) {
				theCurrVal.remove(theRealVal);
			}
			else {
				hasChanged = true;
			}
			theValS.append(theRealVal);
			if (theCount < theSize) {
				theValS.append(VALUE_SEPARATOR);
			}

			theCount++;
		}
		hasChanged |= !theCurrVal.isEmpty();

		if (hasChanged) {
			object.tSetData(attribute.getName(), theValS.toString());
			AttributeOperations.touch(object, attribute);
		}
	}

	/**
	 * Check if the given value fulfills basic constraints.
	 * 
	 * @param aValue
	 *        the value to be added
	 * @param object
	 *        the object. May be <code>null</code>
	 * @throws IllegalArgumentException
	 *         if the value does not match constraints
	 */
	protected void checkBasicValue(Object aValue, TLObject object) throws IllegalArgumentException {
		getBasicValue(aValue); // Will do the checks...
	}

	/**
	 * Get the value if there is a DataObject given.
	 * 
	 * @param aValue
	 *        the value to be added
	 * @throws IllegalArgumentException
	 *         if the value does not match constraints
	 */
	public String getBasicValue(Object aValue) throws IllegalArgumentException {
		if (aValue == null) {
			throw new IllegalArgumentException("Given value is null.");
		}
		if (aValue instanceof String) {
			return (String) aValue;
		}
		if (_resultLocator != null) {
			try {
				Object theResult = _resultLocator.locateAttributeValue(aValue);
				if (theResult instanceof String) {
					return (String) theResult;
				} else {
					throw new IllegalArgumentException("The resource locator did not translate to a String. Input: "
						+ aValue + ", Output: " + theResult);
				}
			} catch (Exception ex) {
				throw new IllegalArgumentException("Value could not be located in: " + aValue);
			}

		}
		throw new IllegalArgumentException("Given value is not a Stringand there is no resource locator to transalte: "
			+ aValue);
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) {
		// Check attribute
//		checkHasAttribute(aMetaAttributed);

		// Get values
		String theVals = (String) object.tGetData(attribute.getName());
		if (StringServices.isEmpty(theVals)) {
			return Collections.EMPTY_SET;
		}

		return StringServices.toList(theVals, VALUE_SEPARATOR);
	}

	@Override
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException,
			AttributeException {
		// Check
		checkAddValue(object, attribute, aValue);

		String theOld = (String) object.tGetData(attribute.getName());
		String theNew = (theOld == null) ? (String) aValue : (theOld + VALUE_SEPARATOR + aValue);
		object.tSetData(attribute.getName(), theNew);
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, AttributeException {
		// Check and calculate
		checkRemoveValue(object, attribute, aValue);

		Collection<?> theValues = (Collection<?>) getAttributeValue(object, attribute);
		theValues.remove(aValue);

		setAttributeValue(object, attribute, theValues);
	}

	@Override
	protected void checkAddValue(TLObject object, TLStructuredTypePart attribute, Object aValue) throws TopLogicException {
		// Check attribute
		AttributeUtil.checkHasAttribute(object, attribute);

		// Check values
		checkBasicValue(aValue, object);
		// Check duplicate
		boolean isDuplicate = ((Collection<?>) getAttributeValue(object, attribute)).contains(aValue);

		if (isDuplicate) {
			throw new IllegalArgumentException("Given value is already used and cannot be added again to " + attribute);
		}
	}

	@Override
	protected void checkRemoveValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws TopLogicException {
		// Check attribute
		AttributeUtil.checkHasAttribute(object, attribute);

		// Check values
		checkBasicValue(aValue, object);
		// Check if the value exists in the current collection
		Collection<?> theValues = (Collection<?>) getAttributeValue(object, attribute);
		if (!theValues.contains(aValue)) {
			throw new IllegalArgumentException("Given value is not used and therefore cannot be removed from "
				+ attribute);
		}

		checkMandatoryRemove(attribute, theValues);
	}

	@Override
	protected void checkSetValues(TLObject object, TLStructuredTypePart attribute, Collection aValues)
			throws TopLogicException {
		if (object != null) {
			// Check attribute
			AttributeUtil.checkHasAttribute(object, attribute);

			if (aValues != null) {
				// Check for duplicates
				Set<Object> valueSet = CollectionUtil.newSet(aValues.size());
				for (Object value : aValues) {
					if (!valueSet.add(value)) {
						throw new TopLogicException(
							I18NConstants.ERROR_DUPLICATE_VALUES_NOT_ALLOWED__ATTRIBUE_VALUE.fill(
								attribute.getName(), MetaLabelProvider.INSTANCE.getLabel(value)));
					}
				}

				// Check the single values
				Iterator<?> theVals = aValues.iterator();
				while (theVals.hasNext()) {
					Object theValue = theVals.next();
					checkBasicValue(theValue, object);
				}
			}
		}
	}

}
