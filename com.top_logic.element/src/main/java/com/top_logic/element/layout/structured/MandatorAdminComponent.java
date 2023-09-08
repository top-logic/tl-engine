/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.core.CreateElementException;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.gui.AbstractCreateAttributedCommandHandler;
import com.top_logic.element.meta.gui.CreateAttributedComponent;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.Visibility;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;


/**
 * Component for managing mandators.
 * 
 * Here you can create new mandators, remove old one and change some attributes.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class MandatorAdminComponent extends AdminElementComponent {

    public interface Config extends AdminElementComponent.Config {
		@Name(MIN_ID_LENGTH)
		@IntDefault(3)
		int getMinIDLength();

		@Name(MAX_ID_LENGTH)
		@IntDefault(4)
		int getMaxIDLength();

		@Override
		@StringDefault(MandatorDeleteCommandhandler.COMMAND_ID)
		String getDeleteCommand();
	}

	public static final String MAX_ID_LENGTH = "maxIDLength";
    public static final String MIN_ID_LENGTH = "minIDLength";

    private int maxIDLength;
    private int minIDLength;

    public MandatorAdminComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);

        this.minIDLength = someAttrs.getMinIDLength();
        this.maxIDLength = someAttrs.getMaxIDLength();
    }
    
    @Override
	protected Map<Tuple, Boolean> createAllowCache() {
		return new HashMap<>(64);
    }
    
    @Override
	public List<String> getExcludeList() {
    	List<String> theExcludeList = new ArrayList<>();
    	theExcludeList.add(Mandator.DESCRIPTION);
    	theExcludeList.add(Mandator.CONTACT_PERSONS);
    	theExcludeList.add(Mandator.CONTRACT_GUIDELINES);
    	return theExcludeList;
    }

    @Override
	public FormContext createFormContext(boolean newMode) {
        FormContext theContext = super.createFormContext(newMode);
        
		Mandator mandator = (Mandator) this.getModel();
		TLClass type = mandator.tType();
		TLStructuredTypePart attribute = type.getPart(Mandator.NUMBER_HANDLER_ID);
		if (attribute != null) {
			String theID = MetaAttributeGUIHelper.getAttributeID(attribute, mandator);
			FormField theField = theContext.getField(theID);
			theField.addConstraint(new StringLengthConstraint(this.minIDLength, this.maxIDLength));
        }
        
        return theContext;
    }

    @Override
	public List<String> getExcludeListForUI() {
        return this.getExcludeList();
    }
    
    public static class MandatorDeleteCommandhandler extends StructuredElementRemoveHandler {
        
        public static final String COMMAND_ID = "deleteMandator";
        
        public MandatorDeleteCommandhandler(InstantiationContext context, Config config) {
            super(context, config);
        }
        
        @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
            return CombinedExecutabilityRule.combine(super.createExecutabilityRule(), MandatorDeleteRule.INSTANCE);
        }
    }
    
    public static class MandatorDeleteRule implements ExecutabilityRule {
        
        public static final ExecutabilityRule INSTANCE = new MandatorDeleteRule();
        
        /**
         * Allow delete when the mandator has no contracts and no contacts.
         */
        @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
			if (model == null) {
				return ExecutableState.NO_EXEC_NO_MODEL;
			}
			Mandator theMandator = (Mandator) model;

            if(theMandator.hasObjectsOfAnyType()) {
            	return ExecutableState.createDisabledState(I18NConstants.ERROR_MANDATOR_IS_REFERENCED);
            }
            else if (theMandator.hasChildren(null)) {
            	return ExecutableState.createDisabledState(I18NConstants.ERROR_MANDATOR_HAS_CHILDREN);
            }
            else {
            	return ExecutableState.EXECUTABLE;
            }
        }
    }
    
    public static class NewMandatorComponent extends CreateAttributedComponent {
        
        /** Name of the mandator constraint in the form context. */
        public static final String PARENT_MANDATOR = "parentMandator";
        
		/**
		 * Configuration for the {@link NewMandatorComponent}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends CreateAttributedComponent.Config {

			@StringDefault(NewMandatorCommandHandler.COMMAND)
			@Override
			String getCreateHandler();

		}

        public NewMandatorComponent(InstantiationContext context, Config someAttr) throws ConfigurationException {
            super(context, someAttr);
        }
        
        @Override
		public TLClass getMetaElement() {
			Mandator parent = (Mandator) getModel();
			return parent.tType();
        }

		@Override
		protected Visibility getAttributeVisibility(TLStructuredTypePart attribute) {
			if (attribute.isMandatory()) {
				return Visibility.EDITABLE;
			}
			return Visibility.HIDDEN;
		}

        @Override
		protected void postProcessFormMember(String aName, FormMember aMember, TLStructuredTypePart aMA, AttributeUpdate aAnUpdate, AttributeFormContext aContext) {
            
            if (Mandator.NUMBER_HANDLER_ID.equals(aName)) {
                LayoutComponent theComp    = this.getDialogParent();
                Constraint      theConst;
                if (theComp instanceof MandatorAdminComponent) {
                    theConst = new StringLengthConstraint(((MandatorAdminComponent) theComp).minIDLength, ((MandatorAdminComponent) theComp).maxIDLength);
                }
                else {
                    theConst = new StringLengthConstraint(3,4);
                }
                ((StringField) aMember).addConstraint(theConst);
            }
            else {
                super.postProcessFormMember(aName, aMember, aMA, aAnUpdate, aContext);
            }
        }
        
        @Override
		protected AttributeUpdate modifyUpdateForAdd(String aName, TLStructuredTypePart aMA, AttributeUpdate aAnUpdate) {
            return super.modifyUpdateForAdd(aName, aMA, aAnUpdate);
        }
        
        @Override
		protected void addMoreAttributes(TLClass aME, AttributeFormContext aContext) {
            super.addMoreAttributes(aME, aContext);
            HiddenField     theHidden  = FormFactory.newHiddenField(NewMandatorComponent.PARENT_MANDATOR);
            theHidden.setValue(this.getModel());
            aContext.addMember(theHidden);
        }
        
        @Override
		protected boolean supportsInternalModel(Object aAnObject) {
            return aAnObject instanceof Mandator;
        }
    }
  
    public static class NewMandatorCommandHandler extends AbstractCreateAttributedCommandHandler {
        // Constants

        /** ID of this command handler. */
        public static final String COMMAND = "newMandator";

        // Constructors

        
        
        public NewMandatorCommandHandler(InstantiationContext context, Config config) {
            super(context, config);
        }
        
        @Override
		protected Map<String,Object> extractValues(FormContainer formContainer, Wrapper aAnAttributed) {
            Map<String,Object> theMap = super.extractValues(formContainer, aAnAttributed);
            
            theMap.put(NewMandatorComponent.PARENT_MANDATOR, formContainer.getField(NewMandatorComponent.PARENT_MANDATOR).getValue());
            
            return theMap;
        }
        
        @Override
		public Wrapper createNewObject(Map<String, Object> aSomeValues, Wrapper aModel) {
            Mandator theParent      = (Mandator) aSomeValues.get(NewMandatorComponent.PARENT_MANDATOR);
            String   theName        = (String)   aSomeValues.get(Mandator.NAME_ATTRIBUTE);
            String   theNHID        = (String)   aSomeValues.get(Mandator.NUMBER_HANDLER_ID);
            if (StringServices.isEmpty(theNHID)) {
                theNHID = "---";
            }
            
			Collection<? extends StructuredElement> children = theParent.getChildren();
			Iterator theIter = children.iterator();
            boolean  exists         = false;

            while(theIter.hasNext()){
                Mandator theMandator = (Mandator)theIter.next();
                String theChilDNHID = (String)theMandator.getValue(Mandator.NUMBER_HANDLER_ID);
                if(theMandator.getName().equalsIgnoreCase(theName) &&
                        theChilDNHID != null && theChilDNHID.equalsIgnoreCase(theNHID)){
                   exists = true; 
                }
            }
            if(!exists){
                return this.createMandator(theParent, theName, theNHID);
            }
            else{
                return null;
            }
        }
        
        /**
         * @param    aParent           The parent of the new mandator, must not be <code>null</code>..
         * @param    aName             The name for the mandator, must not be <code>null</code> or empty.
         * @param    aNumberHandlerID  The number handler ID
         * @return   The created child, never <code>null</code>.
         * @throws   IllegalArgumentException    If one the params is <code>null</code>
         *                                       or empty.
         * @throws   CreateElementException      If creation fails for reasons like: 
         *                                       is leaf node, no access rights, 
         *                                       failure in the system (db)...
         */
        public Mandator createMandator(Mandator aParent, String aName, String aNumberHandlerID) throws IllegalArgumentException, CreateElementException {
            Mandator theChild = (Mandator) aParent.createChild(aName, Mandator.MANDATOR_TYPE);

            theChild.setValue(Mandator.NUMBER_HANDLER_ID, aNumberHandlerID);

            return (theChild);
        }
        
    }
}
