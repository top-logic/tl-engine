/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.expression.NewExpressionCommand;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.Expandable;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Create component for stored queries.
 * 
 * For creating a new stored query one has to define the name of the new query.
 * 
 * NOTE: This code has been taken over from AbstractStoredQueryComponent.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class NewQueryComponent extends AbstractCreateComponent implements Expandable{

	/**
	 * Configuration for the {@link NewQueryComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCreateComponent.Config {

		@StringDefault(NewQueryCommandHandler.COMMAND_ID)
		@Override
		String getCreateHandler();

	}

	/**
	 * Creates a {@link NewQueryComponent}.
	 */
    public NewQueryComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject == null || anObject instanceof StoredQuery;
    }

    /**
     * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
        StringLengthConstraint nameLengthConstraint = new StringLengthConstraint(1, NewQueryCommandHandler.NAME_LENGTH);
		FormField theName =
			FormFactory.newStringField(NewQueryCommandHandler.NAME_ATTRIBUTE, StringServices.EMPTY_STRING, false,
				false, nameLengthConstraint);
		theName.setMandatory(true);
        FormField       theValues = FormFactory.newBooleanField(NewQueryCommandHandler.VALUES_ATTRIBUTE, Boolean.TRUE, false);
        LayoutComponent theParent = this.getDialogParent();
        FormField       theRel    = null;

        if (theParent != null) {
            Boolean theRelVal = Boolean.valueOf(((AttributedSearchComponent) theParent).getRelevantAndNegate());

            theRel = FormFactory.newHiddenField(SearchFilterSupport.RELEVANTANDNEGATE_CONSTRAINT_NAME, theRelVal);
        }
        
        FormContext theContext = new FormContext("default", this.getResPrefix(), new FormField[] {theName, theValues});

        if (theRel != null) {
            theContext.addMember(theRel);
        }

        return (theContext);
    }

    /**
     * Create handler for stored queries.
     * 
     * For creating a new stored query one has to define the name of the new query.
     * 
     * @author    <a href="mailto:mga@top-logic.com">mga</a>
     */
	public static class NewQueryCommandHandler extends NewExpressionCommand {

        // Constants

        /** The ID of this command handler. */
        public static final String COMMAND_ID = "newAttributedQuery";

        /** The name to be used for creating the new StoredQuery. */
        public static final String NAME_ATTRIBUTE = "name";

        /** The flag to be used for direct storing values to new StoredQuery. */
        public static final String VALUES_ATTRIBUTE = "useValues";

        /** The flag to be used for direct storing values to new StoredQuery. */
        public static final String CONTEXT_ATTRIBUTE = "outerContext";

        /** The name to be used for creating the new StoredQuery. */
        public static final int NAME_LENGTH = 80;

        /** 
         * Creates a {@link NewQueryCommandHandler}.
         */
        public NewQueryCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }
        
		@Override
		public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
				Map<String, Object> arguments) {
			StoredQuery query;
			try {
				query = createNewQuery(formContainer, component);
			} catch (Exception ex) {
				String errorKey = "element.search.query.create.failed";
				Logger.error("Unable to create a new query", ex, this);
				throw new TopLogicException(NewQueryCommandHandler.class, errorKey, ex);
			}
			if (query != null) {
				return query;
			} else {
				String errorKey = "element.search.query.create.input.check";
				throw new TopLogicException(NewQueryCommandHandler.class, errorKey);
			}
		}

        @Override
		@Deprecated
		public ResKey getDefaultI18NKey() {
			return I18NConstants.CREATE_QUERY;
        }

        /**
         * @see com.top_logic.layout.form.component.AbstractCreateCommandHandler#createObject(LayoutComponent, java.lang.Object, FormContainer, Map)
         */
        protected StoredQuery createNewQuery(FormContainer aContext, LayoutComponent aComponent) throws Exception {
        	AttributedSearchComponent theDialogParent = (AttributedSearchComponent)aComponent.getDialogParent();
			if (!theDialogParent.hasFormContext()) {
				return null;
			}
			FormContext theOuter = theDialogParent.getFormContext();
            if (!theOuter.checkAll()) {
                return null; // Outer FormContext is invalid Stored Query will be broken ...
            }

            String      theName    = (String) aContext.getField(NewQueryCommandHandler.NAME_ATTRIBUTE).getValue();
            Boolean     theFlag    = (Boolean) aContext.getField(NewQueryCommandHandler.VALUES_ATTRIBUTE).getValue();
            Boolean     theRel     = (Boolean) aContext.getField(SearchFilterSupport.RELEVANTANDNEGATE_CONSTRAINT_NAME).getValue();
            Person      thePerson  = TLContext.getContext().getCurrentPersonWrapper();
            StoredQuery theQuery   = this.createStoredQuery(theName, thePerson, null);
            Boolean     isExtended = Boolean.valueOf((theDialogParent).getRelevantAndNegate());
            

            if ((theFlag != null) && theFlag.booleanValue()) {
                if (theOuter instanceof AttributeFormContext) {
                    this.fillStoredQuery(theDialogParent, theQuery, (AttributeFormContext) theOuter, theRel.booleanValue());
                }
            }
            theQuery.setValue(QueryUtils.IS_EXTENDED, isExtended);

            return (theQuery);
        }

        /** 
         * Fill the stored query with information provided by the attributed form context from 
         * the dialog parent component.
         * 
         * @param    aQuery      The stored query to be filled, must not be <code>null</code>.
         * @param    aContext    The form context to be used for filling, must not be <code>null</code>.
         * @param    isRel       Flag, if relevant flags have to be included.
         */
        protected boolean fillStoredQuery(AttributedSearchComponent theParent, StoredQuery aQuery, AttributeFormContext aContext, boolean isRel) {
            if (aContext.checkAll()) {
                List theFilters = theParent.getSearchFilterSupport().getFilters(aContext, isRel);

                aQuery.setFilters(theFilters);

                if (aContext.hasMember(AttributedSearchComponent.CURRENT_META_ELEMENT)) {
                    TLClass theME = (TLClass) aContext.getField(AttributedSearchComponent.CURRENT_META_ELEMENT).getValue();

                    aQuery.setQueryMetaElement(theME);
                }
                
				SelectField tableColumnsField = SearchFieldSupport.getTableColumnsFields(aContext);
				if (tableColumnsField != null) {
					List theResultColumns = tableColumnsField.getSelection();
                    
                    aQuery.setResultColumns(theResultColumns);
                }
                return true;
            }
            return false;
        }

        // Public methods

        /**
         * Create a new stored query.
         * 
         * If the knowledgebase is <code>null</code>, the method will take the same knowledgebase, 
         * the given person is located.
         * 
         * @param    aName      The name of the new stored query, must not be <code>null</code> or empty.
         * @param    anOwner    The owner of the query, must not be <code>null</code>.
         * @param    aKB        The knowledgebase to store the query, may be <code>null</code>.
         * @return   The new created query, never <code>null</code>.
         * @throws   Exception    If one parameter is invalid or the creation fails.
         */
        public StoredQuery createStoredQuery(String aName, Person anOwner, KnowledgeBase aKB) throws Exception {
            int theLength = aName.length();

            if (theLength > NewQueryCommandHandler.NAME_LENGTH) {
                throw new IllegalArgumentException("Name of new query is too long (" + theLength + " > " + NewQueryCommandHandler.NAME_LENGTH + ")!");
            }
            else if (aKB == null) {
                aKB = anOwner.getKnowledgeBase();
            }

            return StoredQuery.newStoredQuery(aName, anOwner, aKB);
        }
    }
    
    @Override
	public boolean isExpanded() {
        return false;
    }
}

