/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.expression.SaveExpressionCommand;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SaveQueryCommandHandler extends SaveExpressionCommand {

    public static final String COMMAND_ID = "saveAttributedQuery";

    public SaveQueryCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        AttributedSearchComponent theComp    = (AttributedSearchComponent) aComponent;
        AttributeFormContext      theContext = (AttributeFormContext) theComp.getFormContext();
		ResKey theError = null;

        if (theContext.checkAll()) {
            try {
                StoredQuery theSQ = this.storeQuery(theComp, theContext, someArguments);
                if (theSQ != null) {
					Boolean isExtended = Boolean.valueOf(theComp.getRelevantAndNegate());
					theSQ.setValue(QueryUtils.IS_EXTENDED, isExtended);
                    if (this.commit(theSQ)) {
                        this.notifyComponent(theComp, theSQ);
                    }
                    else {
						theError = I18NConstants.ERROR_QUERY_FAILED;
                    }
                }
                else {
					theError = I18NConstants.ERROR_NO_QUERY_SELECTED;
                }
            }
            catch (Exception ex) {
				theError = I18NConstants.ERROR_QUERY_FAILED;

                Logger.error("Unable to store query in " + theComp, ex, this);
           }
        }
        else {
			HandlerResult theResult = new HandlerResult();
			AbstractApplyCommandHandler.fillHandlerResultWithErrors(null, theContext, theResult);
			return theResult;
        }
        
        if (theError != null) {
        	HandlerResult theResult = new HandlerResult();
        	theResult.addErrorMessage(theError);
        	return theResult;
        }
        else {
            return HandlerResult.DEFAULT_RESULT;
        }
    }

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.SAVE_QUERY;
    }

    /** 
     * Store the information from the filters to the stored query.
     * 
     * @param    aComp       The component providing the needed methods, must not be <code>null</code>.
     * @param    aContext    The form context holding the filter information, must not be <code>null</code>.
     * @return   The requested stored query, may be <code>null</code>.
     */
    public StoredQuery storeQuery(AttributedSearchComponent aComp, FormContext aContext, Map someArguments) {
        List        theFilters = aComp.getFilters(aContext, someArguments);
        StoredQuery theSQ      = aComp.getStoredQuery();

        if (theSQ != null) {
			List theColumns = (List) SearchFieldSupport.getTableColumnsFields(aContext).getValue();

            theSQ.setFilters(theFilters);
            theSQ.setQueryMetaElement(aComp.getSearchMetaElement());
            theSQ.setResultColumns(theColumns);
        }

        return theSQ;
    }

    /** 
     * Commit the given query.
     * 
     * @param    aStoredQuery    The query to be commited, must not be <code>null</code>.
     */
    protected boolean commit(StoredQuery aStoredQuery) {
        return aStoredQuery.getKnowledgeBase().commit();
    }

    /** 
     * Hand over the committed stored query to the component calling.
     * 
     * @param    aComp           The component to be notified, must not be <code>null</code>.
     * @param    aStoredQuery    The query commited, must not be <code>null</code>.
     */
    protected void notifyComponent(AttributedSearchComponent aComp, StoredQuery aStoredQuery) {
        AttributeFormContext theContext = (AttributeFormContext) aComp.getFormContext();

        if (theContext.hasMember(SearchFieldSupport.STORED_QUERY)) {
            SelectField theField = (SelectField) theContext.getField(SearchFieldSupport.STORED_QUERY);

            theField.setDisabled(false);
            theField.setAsSingleSelection(aStoredQuery);
        }
    }
    
    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
    	return QueryUtils.SAVE_DEL_RULE;
    }
}

