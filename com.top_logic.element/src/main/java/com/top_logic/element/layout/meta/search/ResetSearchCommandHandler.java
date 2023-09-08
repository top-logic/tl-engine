/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Utils;

/**
 * Reset the search input area to the state defined by a stored query.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ResetSearchCommandHandler extends AJAXCommandHandler {

    public static final String COMMAND_ID = "resetAttributedQuery";

    /** 
     * Creates a {@link ResetSearchCommandHandler}.
     */
    public ResetSearchCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> aArguments) {
        AttributedSearchComponent theComp  = (AttributedSearchComponent) aComponent;
        StoredQuery               theSQ    = theComp.getStoredQuery();

        if(theSQ != null && !theSQ.tValid()) {
        	HandlerResult theResult = new HandlerResult();
			theResult.addErrorMessage(com.top_logic.layout.component.I18NConstants.ERROR_SELECTED_OBJECT_DELETED);
            return theResult;
        }
        
        // Set the search scope to the one saved in the query
        if (theSQ != null) {
        	boolean theCompMode = theComp.isExtendedSearch();
        	Boolean isExtended  = (Boolean) theSQ.getValue(QueryUtils.IS_EXTENDED);

        	if (theCompMode != Utils.isTrue(isExtended)) {
        		theComp.getSwitchSearchScopeCommand().switchSearchScope(theComp);
        	}
        }
        if (theComp.hasFormContext()) {
        	AttributeFormContext theContext = (AttributeFormContext) theComp.getFormContext();
        	SelectField          theField   = (SelectField) theContext.getField(SearchFieldSupport.STORED_QUERY);

        	theComp.getSearchFilterSupport().fillFormContext(theContext, theSQ);
            
            theField.setDisabled(false);
        }

        return HandlerResult.DEFAULT_RESULT;

    }

    @Override
	public boolean needsConfirm() {
        return true;
    }

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.RESET_SEARCH;
    }

    /** 
     * Reset the form context of the given search component to the initial state.
     * 
     * @param    aComponent    The component to get the form context from, must not be <code>null</code>. 
     * @param    aQuery        The stored query currently selected, may be <code>null</code>.
     */
    public void resetStoredQuery(AttributedSearchComponent aComponent, StoredQuery aQuery) {
        AttributeFormContext theContext = (AttributeFormContext) aComponent.getFormContext();

        if (theContext.hasMember(SearchFieldSupport.STORED_QUERY)) {
            SelectField theField = (SelectField) theContext.getField(SearchFieldSupport.STORED_QUERY);

            theField.setDisabled(false);
            theField.setAsSingleSelection(aQuery);
        }
    }
}
