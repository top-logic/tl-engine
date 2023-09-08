/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.element.meta.query.BasicCollectionFilter;
import com.top_logic.element.meta.query.CollectionFilter;
import com.top_logic.element.meta.query.CollectionFilterFactory;
import com.top_logic.element.meta.query.FulltextFilter;
import com.top_logic.element.meta.query.MetaAttributeFilter;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Support class for search filters.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SearchFilterSupport {

	/** Only instance of the support. */
    public static final SearchFilterSupport INSTANCE = new SearchFilterSupport();
    
	/** ID prefix for the relevant and negate form group. */
    public static final String RELEVANTANDNEGATE_CONSTRAINT_NAME = "isNegative";

    /**
	 * Get the filters for the given AttributeFormContext
	 *
	 * @param formContext
	 *        The form context to get the filters from, must not be <code>null</code>.
	 * @param extendedSearch
	 *        Flag, if negative and relevant information has to be used.
	 * @return The list of collected filters, never <code>null</code>.
	 */
	@SuppressWarnings("rawtypes")
	public List<CollectionFilter> getFilters(AttributeFormContext formContext, boolean extendedSearch) {
        List<CollectionFilter>   theFilters   = new ArrayList<>();
        CollectionFilterFactory  theFac       = CollectionFilterFactory.getInstance();
		AttributeUpdateContainer theContainer = formContext.getAttributeUpdateContainer();
        BasicCollectionFilter<?> theFilter;

		for (AttributeUpdate theUpdate : theContainer.getAllUpdates()) {
            boolean searchForEmptyValues;
            boolean positiveSearch;
            if (extendedSearch) {
                String relevantFieldName = SearchFilterSupport.getRelevantAndNegateMemberName(theUpdate.getAttribute(), theUpdate.getDomain());
				boolean extendedSearchForAttribute = formContext.hasMember(relevantFieldName);
				if (extendedSearchForAttribute) {
					Boolean relevantState = (Boolean) formContext.getField(relevantFieldName).getValue();
					if (relevantState == null) {
						// Attribute is excluded from search.
						continue;
					}
					
					searchForEmptyValues = true;
					positiveSearch = relevantState.booleanValue();
				} else {
					positiveSearch = true;
					searchForEmptyValues = false;
				}
            } else {
				positiveSearch = true;
				searchForEmptyValues = false;
            }

            boolean isRelevant =true;
            
            boolean doNegate = ! positiveSearch;
			theFilter = (BasicCollectionFilter<?>) theFac.getFilter(theUpdate, doNegate, isRelevant, searchForEmptyValues);

            if (theFilter != null) {
                theFilter.setIsRelevant(isRelevant);
                theFilter.setNegate    (doNegate);

                theFilters.add(theFilter);
            }
        }

        // Add filter for full text search (if requested).
		if (formContext.hasMember(SearchFieldSupport.FULL_TEXT)) {
			String theFulltext = (String) formContext.getField(SearchFieldSupport.FULL_TEXT).getValue();
			Boolean theFulltextMode = (Boolean) formContext.getField(SearchFieldSupport.FULLTEXT_PARAM_MODE).getValue();
			Boolean exactMatch =
				(Boolean) formContext.getField(SearchFieldSupport.FULLTEXT_PARAM_EXACT_MATCH).getValue();

        	boolean doNegate   = false;
        	boolean isRelevant = true;

        	if (extendedSearch) {
				Boolean theFlag = SearchFilterSupport.getRelevantAndNegate(formContext, SearchFieldSupport.FULL_TEXT);

        		isRelevant = (theFlag != null);
        		doNegate   = (theFlag != null) && !theFlag.booleanValue();
        	}

        	boolean theMode = theFulltextMode != null && theFulltextMode.booleanValue();
        	if (!StringServices.isEmpty(theFulltext) || (extendedSearch && isRelevant)) {
        	    theFilters.add(getFullTextFilter(null, theFulltext, theMode, exactMatch, doNegate, isRelevant));
        	}
        }

        return theFilters;
    }

    /**
	 * Return the full text filter for the given parameters.
	 * 
	 * @return The requested full text filter, never <code>null</code>.
	 */
    protected FulltextFilter getFullTextFilter(String[] koTypesForLucene, String aFulltext, boolean isAND, boolean exactMatch, boolean doNegate, boolean isRelevant) {
        return new FulltextFilter(koTypesForLucene, aFulltext, isAND, exactMatch, doNegate, isRelevant);
    }

    /**
	 * Fill the context with the values defined in the given stored query.
	 *
	 * @param formContext
	 *        The form context to be updated, must not be <code>null</code>.
	 * @param aQuery
	 *        The query to be used for updating the context, may be <code>null</code>.
	 */
	public void fillFormContext(AttributeFormContext formContext, StoredQuery aQuery) {
		Collection<?> theFilters = null;

        if (aQuery != null) {
            // Get the filters defined in the query
			theFilters = aQuery.getFilters();
        }

        try {
			for (AttributeUpdate theUpdate : formContext.getAttributeUpdateContainer().getAllUpdates()) {
                String              theAccessPath = theUpdate.getDomain();
                TLStructuredTypePart       theMA         = theUpdate.getAttribute();
				String theName = MetaAttributeGUIHelper.getAttributeIDCreate(theMA, theAccessPath);
                MetaAttributeFilter theFilter     = (theFilters != null) ? StoredQuery.getFilterFor(theFilters, theMA, theAccessPath) : null;

				if (formContext.hasMember(theName)) {
					FormMember theMember = formContext.getMember(theName);
                    String     theCheckName = getRelevantAndNegateMemberName(theMA, theAccessPath);

                    if (theMember instanceof FormField) {
                        FormField theField = (FormField) theMember;

                        if (theFilter != null) {
							AttributeUpdate theNewUpdate = theFilter.getSearchValuesAsUpdate(
								formContext.getAttributeUpdateContainer(), theUpdate.getType(), null);

                            theField.setValue(theNewUpdate.getCorrectValues());
                        }
                        else {
                        	// if aQuery is null all fields should be set to null and not the default value, 
                        	// because this might be the value of a previously selected stored query 
                        	theField.setValue((aQuery != null) ? theField.getDefaultValue() : null); 
                        }
                    }
                    else if (theMember instanceof FormContainer) {
                        FormContainer theCont = (FormContainer) theMember;
                        FormField     theFrom = (FormField) theCont.getMember(AttributeFormFactory.SEARCH_FROM_FIELDNAME);
                        FormField     theTo   = (FormField) theCont.getMember(AttributeFormFactory.SEARCH_TO_FIELDNAME);

                        if (theFilter != null) {
							AttributeUpdate theNewUpdate = theFilter.getSearchValuesAsUpdate(
								formContext.getAttributeUpdateContainer(), theUpdate.getType(), null);
							List<?> theValues = (List<?>) theNewUpdate.getCorrectValues();

                            theFrom.setValue(theValues.get(0));
                            theTo.setValue(theValues.get(1));
                        }
                        else {
                            theFrom.setValue((aQuery != null) ? theFrom.getDefaultValue() : null); 
                            theTo.setValue  ((aQuery != null) ? theTo.getDefaultValue()   : null);                         
                        }
                    }

					if (formContext.hasMember(theCheckName) && theFilter != null) {
						BooleanField theCheck = (BooleanField) formContext.getField(theCheckName);
                        boolean      isRelevant = theFilter.isRelevant();
                        boolean      theNegate  = theFilter.getNegate();

                        theCheck.setValue(!isRelevant ? null : Boolean.valueOf(!theNegate));
                    }
                }
            }

			if (aQuery != null && formContext.hasMember(SearchFieldSupport.FULL_TEXT)) {
                FulltextFilter theFilter = (FulltextFilter) StoredQuery.getFilterFor(theFilters, FulltextFilter.class);
                if (theFilter != null) {
					FormField theField = formContext.getField(SearchFieldSupport.FULL_TEXT);
					FormField theRelevantField =
						formContext.getField(getRelevantAndNegateMemberName(SearchFieldSupport.FULL_TEXT));
                    boolean isRelevant = theFilter.isRelevant();
                    boolean isNegative = theFilter.getNegate();
                    
                    theRelevantField.setValue(isRelevant ? Boolean.valueOf(isNegative) : null);
                    theField.setValue(theFilter.getPattern());
                } 
        	}
        }
        catch (Exception ex) {
            Logger.error("Unable to fill form context with stored query " + aQuery, ex, SearchFilterSupport.class);
        }

		SelectField theField = SearchFieldSupport.getTableColumnsFields(formContext);
		if (theField != null) {
            if(aQuery == null) {
                theField.setValue(theField.getDefaultValue());
            }
            else {
				List<String> theResultColumns = aQuery.getResultColumns();
	            if (!theResultColumns.isEmpty()) {
					(theField).setValue(theResultColumns);
                }
            }
        }

        // Add filter for full text search (if requested).
		if (formContext.hasMember(SearchFieldSupport.FULL_TEXT)) {
			FormField theFulltext = formContext.getField(SearchFieldSupport.FULL_TEXT);
			FormField theFulltextMode = formContext.getField(SearchFieldSupport.FULLTEXT_PARAM_MODE);
			FormField exactMatchField = formContext.getField(SearchFieldSupport.FULLTEXT_PARAM_EXACT_MATCH);
			FormField theFulltextRel =
				formContext.getField(getRelevantAndNegateMemberName(SearchFieldSupport.FULL_TEXT));

        	if (aQuery != null) {
        		FulltextFilter theFilter = (FulltextFilter) StoredQuery.getFilterFor(theFilters, FulltextFilter.class);
        		if (theFilter != null) {
        			String  theText = theFilter.getPattern();
        			boolean theMode = theFilter.getMode();
        			boolean theNeg  = theFilter.getNegate();
        			boolean theRel  = theFilter.isRelevant();
        			boolean exactMatch = theFilter.getExactMatch();
        			
        			Boolean theNegateTristate = theRel ? Boolean.valueOf(!theNeg) : null;
        			theFulltext.setValue(theText);
        			theFulltextRel.setValue(theNegateTristate);
        			theFulltextMode.setValue(Boolean.valueOf(theMode));
        			exactMatchField.setValue(Boolean.valueOf(exactMatch));
        		}
        		else {
        			theFulltext.setValue("");
        			theFulltextMode.setValue(Boolean.valueOf(FulltextFilter.MODE_AND));
        			// dont know what to set here since default is configurable in component
//        			exactMatchField.setValue(???);
        			theFulltextRel.setValue(null);
        		}
        	}
        	else {
        		theFulltext.setValue("");
        		theFulltextMode.setValue(Boolean.valueOf(FulltextFilter.MODE_AND));
                // dont know what to set here since default is configurable in component
//              exactMatchField.setValue(???);
        		theFulltextRel.setValue(null);
        	}
        }
    }

    /**
     * Return the "negate flag" field name for the given meta attribute.
     *
     * @param    anMA    The meta attribute to get the field name for, must not be <code>null</code>.
     * @return   The requested field name, never <code>null</code>.
     */
    public static String getRelevantAndNegateMemberName(TLStructuredTypePart anMA, String aDomain) {
        return MetaAttributeGUIHelper.internalID(SearchFilterSupport.RELEVANTANDNEGATE_CONSTRAINT_NAME, anMA, null, aDomain);
    }

	/**
	 * Return the value for the requested field (relevant or negate) from the given form context.
	 * 
	 * @param aFormCtx
	 *        Form context containing the requested field.
	 * @param aBasicID
	 *        ID of the requested field.
	 * @return The value from the relevant and negate group in the form context for the given ID.
	 */
    protected static Boolean getRelevantAndNegate(AttributeFormContext aFormCtx, String aBasicID) {
		String theFieldName = SearchFilterSupport.getRelevantAndNegateMemberName(aBasicID);
		if (!aFormCtx.hasMember(theFieldName)) {
			return null;
		}

		return (Boolean) aFormCtx.getField(theFieldName).getValue();

	}

	/**
	 * The name for the relevant and negate group in the form context.
	 */
	public static String getRelevantAndNegateMemberName(String aBasicID) {
		return RELEVANTANDNEGATE_CONSTRAINT_NAME+aBasicID;
	}
}

