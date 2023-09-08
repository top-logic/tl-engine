/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.query.kbbased.StructuredElementAttributeFilter;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Factory for AttributeFilters based on KBBasedMetaAttributes.
 *
 * TODO KBU cache Filter CTors
 *
 * @author    <a href="mailto:fma@top-logic.com"></a>
 */
public class CollectionFilterFactory {

    /**
     * Paramaters for aCTor using a Map;
     */
    protected static final Class[] MAP_PARAMETERS = new Class[] {Map.class};

    /** The key for finding the class to be used as factory. */
    private static final String FACTORY_KEY = "factory";

	public static CollectionFilterProvider LIST_OBJECT_FILTER_PROVIDER = new CollectionFilterProvider() {
		@Override
		public CollectionFilter getFilter(TLStructuredTypePart anAttribute, Collection<? extends TLObject> value,
				boolean doNegate, boolean searchForEmptyValues, String anAccessPath) {
			return new WrapperValuedAttributeFilter(anAttribute, value, doNegate, true, anAccessPath);
		}
	};

	/**
	 * Default CTor
	 */
	protected CollectionFilterFactory() {
		super();
	}

	/** Singleton pattern. */
	private static CollectionFilterFactory singleton;

	/**
	 * Get the single instance
	 *
	 * @return the single instance
	 */
	public static synchronized CollectionFilterFactory getInstance () {
        if (singleton == null) {
            setupInstance();
        }

        return (singleton);
    }

    /**
     * Statically synhronized actual implementation of getInstance().
     */
    protected static synchronized void setupInstance() {
        if (singleton == null) {
            try {
                String theName  =
                    Configuration.getConfiguration(CollectionFilterFactory.class)
                                 .getValue(FACTORY_KEY);
                Class  theClass = Class.forName(theName);

                singleton = (CollectionFilterFactory) theClass.newInstance();
            }
            catch (Exception ex) {
                Logger.info("No CollectionFilterFactory configured, using default",
                            ex, CollectionFilterFactory.class);
            }
        }
    }

    /**
     * Get a suitable filter for the given update.
     *
     * @param anAttributeUpdate the update
     * @param doNegate          if true create a negating filter
     * @param isRelevant        the relevant flag
     * @return a suitable filter
     * @throws IllegalArgumentException if no update is given
     */
    public CollectionFilter getFilter(AttributeUpdate anAttributeUpdate, boolean doNegate, boolean isRelevant, boolean searchForEmptyValues) {
        // Check params
        if(anAttributeUpdate==null){
            // No attribute update
            throw new IllegalArgumentException("AttributeUpdate must not be null");
        }

        return this.getFilter(anAttributeUpdate.getAttribute(), anAttributeUpdate.getCorrectValues(), doNegate, searchForEmptyValues, anAttributeUpdate.getDomain());
    }

    /**
     * Get a suitable filter for the given update.
     *
     * @param anAttributeUpdate the update
     * @param doNegate          if true create a negating filter
     * @param isRelevant        the relevant flag
     * @return a suitable filter
     * @throws IllegalArgumentException if no update is given
     */
    public CollectionFilter getFilter(AttributeUpdate anAttributeUpdate, boolean doNegate, boolean isRelevant, boolean addEmpty, String anAccessPath) {
		// Check params
		if(anAttributeUpdate==null){
		    // No attribute update
			throw new IllegalArgumentException("AttributeUpdate must not be null");
		}

		return this.getFilter(anAttributeUpdate.getAttribute(), anAttributeUpdate.getCorrectValues(), doNegate, addEmpty, anAccessPath);
    }

    /**
	 * Returns an AttributeFilter for the given attribute initialized with the given value. If the
	 * value is <code>null</code> or an empty map <code>null</code> will be.
	 *
	 * @param anAttribute
	 *        the attribute
	 * @param aValue
	 *        the value to be searched for
	 * @param doNegate
	 *        if true the element not matching the condition will be returned
	 * @param isRelevant
	 *        the relevant flag
	 * @param addEmpty
	 *        also add relevant(!) empty filters
	 * @return the AttributeFilter or <code>null</code>.
	 */
    public CollectionFilter getFilter(TLStructuredTypePart anAttribute, Object aValue, boolean doNegate, boolean isRelevant, boolean addEmpty) {
        return this.getFilter(anAttribute, aValue, doNegate, addEmpty, null);
    }

    /**
	 * Returns an AttributeFilter for the given attribute initialized with the given value. If the
	 * value is <code>null</code> or an empty map <code>null</code> will be.
	 *
	 * @param anAttribute
	 *        the attribute
	 * @param aValue
	 *        the value to be searched for
	 * @param doNegate
	 *        if true the element not matching the condition will be returned
	 * @param searchForEmptyValues
	 *        also add relevant(!) empty filters
	 * @return the AttributeFilter or <code>null</code>.
	 */
    public final CollectionFilter getFilter(TLStructuredTypePart anAttribute, Object aValue, boolean doNegate, boolean searchForEmptyValues, String anAccessPath) {
		// Check params
		if (anAttribute==null){
		    // No attribute
			throw new IllegalArgumentException("Attribute must not be null");
		}
		
		if (AttributeOperations.allowsSearchRange(anAttribute)) {
			// we need a search range
			boolean isList = aValue instanceof List;
			if (!isList) {
				// No search range -> ignore
				return null;
			}
			
			List listValue = (List) aValue;
			if (listValue.size() != 2) {
				// Wrong search range -> ignore
				return null;
			}

			if (!searchForEmptyValues) {
				Object fromValue = listValue.get(0);
				Object toValue = listValue.get(1);
				if ((fromValue == null) && (toValue == null)) {
					// both values are null -> ignore
					return null;
				}
			}
		} else {
			if (!searchForEmptyValues) {
				if (aValue==null) {
					// No search value -> ignore
					return null;
				}
				
				if (aValue instanceof String && StringServices.isEmpty((String) aValue)) {
					// Not search text -> ignore
					return null;
				}
				
				if (aValue instanceof Collection<?> && ((Collection<?>) aValue).isEmpty()) {
					// Not search text -> ignore
					return null;
				}
			}
		}

		// Create filter
		return createFilter(anAttribute, aValue, doNegate, searchForEmptyValues, anAccessPath);
	}

	protected CollectionFilter createFilter(TLStructuredTypePart anAttribute,
			Object aValue, boolean doNegate, boolean searchForEmptyValues,
			String anAccessPath) {
		boolean isRelevant = true;
		switch (AttributeOperations.getMetaAttributeType(anAttribute)) {
			case LegacyTypeCodes.TYPE_STRING:
				return new StringAttributeFilter(anAttribute, (String) aValue, doNegate, isRelevant, anAccessPath);
			case LegacyTypeCodes.TYPE_DATE:
				return new DateAttributeFilter(anAttribute, toRangeList(aValue), doNegate, isRelevant, anAccessPath);
			case LegacyTypeCodes.TYPE_FLOAT:
			case LegacyTypeCodes.TYPE_LONG:
				return new NumberAttributeFilter(anAttribute, toRangeList(aValue), doNegate, isRelevant, anAccessPath);
			case LegacyTypeCodes.TYPE_BOOLEAN:
				return new BooleanAttributeFilter(anAttribute, (Boolean) aValue, doNegate, isRelevant, anAccessPath);
			case LegacyTypeCodes.TYPE_SINGLE_STRUCTURE:
			case LegacyTypeCodes.TYPE_STRUCTURE: {
				List<?> theVal = asListValue(aValue, searchForEmptyValues);
				if (theVal == null) {
					return null;
				}
				Set setVal = new HashSet<Object>(theVal);
				if (AttributeOperations.isReadOnly(anAttribute)) {
					return new WrapperValuedAttributeFilter(anAttribute, setVal, doNegate, true, anAccessPath);
				} else {
					return new StructuredElementAttributeFilter(anAttribute, setVal, true, doNegate, isRelevant,
						anAccessPath, StructuredElementAttributeFilter.START_LEVEL_ELEMENT);
				}
			}
			case LegacyTypeCodes.TYPE_CLASSIFICATION:
			case LegacyTypeCodes.TYPE_WRAPPER:
			case LegacyTypeCodes.TYPE_LIST:
			case LegacyTypeCodes.TYPE_SINGLE_REFERENCE:
			case LegacyTypeCodes.TYPE_SINGLEWRAPPER:
			case LegacyTypeCodes.TYPE_TYPEDSET: {
				List theVal = asListValue(aValue, searchForEmptyValues);
				if (theVal == null) {
					return null;
				}
				
			    return LIST_OBJECT_FILTER_PROVIDER.getFilter(anAttribute, theVal, doNegate, searchForEmptyValues, anAccessPath);
			}
			case LegacyTypeCodes.TYPE_STRING_SET: {
				List theVal = asListValue(aValue, searchForEmptyValues);
				if (theVal == null) {
					return null;
				}
				
			    return new StringSetFilter(anAttribute, theVal, doNegate, isRelevant, anAccessPath);
			}
			case LegacyTypeCodes.TYPE_COMPLEX: {
				List theVal = asListValue(aValue, searchForEmptyValues);
				if (theVal == null) {
					return null;
				}
				
			    return new ComplexAttributeFilter(anAttribute, theVal, doNegate, isRelevant, anAccessPath);
			}
			default: {
				CollectionFilterProviderAnnotation annotation =
					anAttribute.getAnnotation(CollectionFilterProviderAnnotation.class);
				if (annotation == null) {
					throw new IllegalArgumentException("No AttributeFilter available for attribute " + anAttribute);
				}
				CollectionFilterProvider filterProvider = TypedConfigUtil.createInstance(annotation.getImpl());
				
				List theVal = asListValue(aValue, searchForEmptyValues);
				if (theVal == null) {
					return null;
				}
				
				return filterProvider.getFilter(anAttribute, theVal, doNegate, searchForEmptyValues, anAccessPath);
			}
		}
	}

	private List toRangeList(Object aValue) {
		List startEndList;
		if (aValue instanceof List) {
			startEndList = (List) aValue;
		} else {
			startEndList = new ArrayList();
			Object searchRangeStart = aValue;
			startEndList.add(searchRangeStart);
			Object searchRangeEnd = aValue;
			startEndList.add(searchRangeEnd);
		}
		return startEndList;
	}

	/**
	 * Converts the given value to a {@link List}.
	 * 
	 * @param aValue
	 *        {@link List}, <code>null</code>, or any other object.
	 * @param searchForEmptyValues
	 *        Whether empty results should not be converted to <code>null</code>.
	 * @return The given list, an empty list or <code>null</code> for <code>null</code> (depending
	 *         on searchForEmptyValues), or a singleton list containing the given object.
	 */
	protected static List<?> asListValue(Object aValue, boolean searchForEmptyValues) {
		List<?> listValue;
		if (aValue instanceof List) {
			listValue = (List<?>) aValue;
		} else if (aValue == null) {
			listValue = Collections.EMPTY_LIST;
		} else {
			listValue = Collections.singletonList(aValue);
		}

		if (!searchForEmptyValues && listValue.isEmpty()) {
			// Error like empty input???
			return null;
		}
		
		return listValue;
	}

	/**
	 * Get an AttributeFilter that filters objects that match
	 * the given filter for at least one value of the
	 * given WrapperSetMetaAttribute.
	 *
	 * @param anAttribute		the WrapperSetMetaAttribute
	 * @param anInnerFilter		the filter on the values of the WrapperSetMetaAttribute
	 * @param doNegate			if true invert the filter condition of the resulting filter
	 * @param isRelevant 		the relevant flag
	 * @return the filter
	 */
	public CollectionFilter getWrapperSetFilter (TLStructuredTypePart anAttribute, CollectionFilter anInnerFilter, boolean doNegate, boolean isRelevant, String anAccessPath) {
		return new WrapperSetFilter (anAttribute, anInnerFilter, doNegate, isRelevant, anAccessPath);
	}

	/**
	 * Get a full text filter
	 *
	 * @param aSearchString	the search string for the full text search
	 * @param aMode			if true AND search will be used for the words in the
	 * 						search string, OR search otherwise
	 * @param doNegate 		the negation flag
	 * @param isRelevant 	the relevant flag
	 * @return the filter or <code>null</code> if the searchString is empty or <code>null</code>.
	 */
	public CollectionFilter getFulltextFilter(String[] koTypesForLucene, String aSearchString, boolean aMode, boolean exactMatch, boolean doNegate, boolean isRelevant) {
		if(! StringServices.isEmpty(aSearchString)){
			return new FulltextFilter(koTypesForLucene, aSearchString, aMode, exactMatch,doNegate, isRelevant);
		}
		return null;
	}

}
