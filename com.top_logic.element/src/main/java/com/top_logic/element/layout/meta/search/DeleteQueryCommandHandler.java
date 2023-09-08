/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.query.StoredFlexWrapper;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.util.error.TopLogicException;

/**
 * Delete a {@link StoredQuery} from an {@link AttributedSearchComponent}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class DeleteQueryCommandHandler extends AJAXCommandHandler {

    public static final String COMMAND_ID = "deleteAttributedQuery";

	/**
	 * Configuration for {@link DeleteQueryCommandHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AJAXCommandHandler.Config {

		@FormattedDefault(QueryUtils.OWNER_WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

    /** 
     * Creates a {@link DeleteQueryCommandHandler}.
     */
    public DeleteQueryCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> aArguments) {   	
    	AttributedSearchComponent theComp   = (AttributedSearchComponent) aComponent;
        StoredQuery               theSQ     = theComp.getStoredQuery();
        HandlerResult             theResult = HandlerResult.DEFAULT_RESULT;

        if (theSQ != null) {
            try {
                KnowledgeBase theKB = this.deleteStoredWrapper(theSQ);

                if (theKB.commit()) {              
                    // Not needed as updateComponent will do this via the SelectField.
                    // theComp.resetStoredQuery();  
                    this.updateComponent(theComp, theSQ);
                    theComp.invalidate();
                }
                else {
                    theResult = new HandlerResult();
					theResult.addErrorMessage(com.top_logic.layout.form.component.I18NConstants.ERROR_COMMIT_FAILED);

                	Logger.error("Failed to commit deleting stored query " + theSQ, this);
                }
            }
            catch (Exception ex) {
                theResult = new HandlerResult();
            	theResult.setException(new TopLogicException(this.getClass(),"element.search.query.delete.failed", ex ));
            }
        }   

        return theResult;
    }

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.DELETE_QUERY;
    }

    /** 
     * Delete aStoredWrapper from the {@link KnowledgeBase}.
     * 
     * @param    aStoredQuery    The stored query to be deleted, must not be <code>null</code>.
     * @return   The knowledge base, which has deleted the query, never <code>null</code>.
     * @throws   Exception      If deleting the object failed.
     */
    public KnowledgeBase deleteStoredWrapper(StoredFlexWrapper aStoredQuery) throws Exception {
        KnowledgeBase kb = aStoredQuery.getKnowledgeBase();
        aStoredQuery.tDelete();
        return kb;
    }

    /** 
     * Update the search component after deletion of stored query succeeds.
     * 
     * @param    aComp     The component to be updated, must not be <code>null</code>.
     * @param    aQuery    The query which has been deleted, must not be <code>null</code>.
     */
    protected void updateComponent(AttributedSearchComponent aComp, StoredQuery aQuery) {
        if (aComp.getQuerySupport() && aComp.hasFormContext()) {
            SelectField theField   = (SelectField) aComp.getFormContext().getField(SearchFieldSupport.STORED_QUERY);
            updateField(theField, aQuery);
            
        }
    }
    
    protected void updateField(SelectField aField, StoredFlexWrapper aWrapper) {
    	List        theOptions = new ArrayList(aField.getOptions());
        int         thePos     = theOptions.indexOf(aWrapper);

        if (thePos >= 0) {
            theOptions.remove(thePos);
            thePos--;
        }

        // Must set default Value as well as setOptions will reset to this value
        aField.setDefaultValue(Collections.emptyList());
        aField.setValue       (Collections.emptyList());
        aField.setOptions(theOptions);
    }
    
    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
    	return QueryUtils.SAVE_DEL_RULE;
    }
}

