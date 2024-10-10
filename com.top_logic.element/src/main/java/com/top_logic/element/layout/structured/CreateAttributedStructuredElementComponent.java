/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.core.util.ElementEventUtil;
import com.top_logic.element.layout.create.CreateFormBuilder;
import com.top_logic.element.meta.PersistentClass;
import com.top_logic.element.meta.gui.AbstractCreateAttributedCommandHandler;
import com.top_logic.element.meta.gui.CreateAttributedComponent;
import com.top_logic.element.meta.kbbased.AttributedWrapper;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * Create component for attributed structured elements.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 * 
 * @deprecated Use {@link AbstractCreateComponent} with {@link CreateFormBuilder}
 */
@Deprecated
public class CreateAttributedStructuredElementComponent extends CreateAttributedComponent implements ValueListener {

    public interface Config extends CreateAttributedComponent.Config {
		@Name(CreateAttributedStructuredElementComponent.XML_CONFIG_STRUCTURE_NAME)
		@Mandatory
		String getStructureName();

		@StringDefault(CreateAttributedStructuredElementCommandHandler.COMMAND_ID)
		@Override
		String getCreateHandler();

	}

	/** Type of the new element. */
    public static final String PARAMETER_TYPE = "_parameter_type";

    /** The config entry for the structure to be used. */
    private static final String XML_CONFIG_STRUCTURE_NAME = "structureName";

    /** The SE structure name handled by this instance. */
    protected final String structureName;

    /** The meta element name for the currently selected type of child. */
    private TLClass metaElement;

    /** 
     * Creates a {@link CreateAttributedStructuredElementComponent}.
     */
    public CreateAttributedStructuredElementComponent(InstantiationContext context, Config someAttr) throws ConfigurationException {
		this(context, someAttr, someAttr.getStructureName());
    }

    /** 
     * Creates a {@link CreateAttributedStructuredElementComponent}.
     */
    protected CreateAttributedStructuredElementComponent(InstantiationContext context, Config someAttr, String aStructure) throws ConfigurationException {
        super(context, someAttr);

        this.structureName = aStructure;
    }

    /**
     * @see com.top_logic.element.meta.gui.CreateAttributedComponent#getMetaElement()
     */
    @Override
	public TLClass getMetaElement() {
        return this.metaElement;
    }

    /**
     * @see com.top_logic.element.meta.gui.CreateAttributedComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
        StructuredElement theElement = getStructuredElementModel();
        FormField         theType    = this.getTypeField(theElement, this.getResPrefix(), null);

        return this.createFormContext(null, theType);
    }

	/**
	 * Type-safe variant of {@link #getModel()}.
	 * 
	 * @return This {@link StructuredElement} model.
	 */
	protected final StructuredElement getStructuredElementModel() {
		return (StructuredElement) this.getModel();
	}

    /**
     * When the type of structured element to be created has changed, the method
     * has to validate, that the information displayed is up to date.
     * 
     * The method will then change the selected {@link #metaElement} and may change the
     * form context afterwards.
     * 
     * @see com.top_logic.layout.form.ValueListener#valueChanged(com.top_logic.layout.form.FormField, java.lang.Object, java.lang.Object)
     */
    @Override
	public void valueChanged(FormField aField, Object anOldValue, Object aNewValue) {
        TLClass theME     = this.metaElement;
		TLClass newType = (TLClass) CollectionUtil.getSingleValueFrom(aNewValue);

		this.setStructureType(newType);

        if (!theME.equals(this.metaElement)) {
			FormField theField = this.getTypeField(getStructuredElementModel(), this.getResPrefix(), newType);
            FormContext theContext = this.getFormContext();

			installFormContext(this.createFormContext(theContext, theField));
            this.invalidate();
        }
    }

    /** 
     * Create a form context (with reflecting information from given parameters).
     * 
     * @param    anOld      An old form context to transfer the values from, may be <code>null</code>.
     * @param    aSEType    The field for selecting the structured element type, must not be <code>null</code>.
     * @return   The new created form context, never <code>null</code>.
     */
    protected FormContext createFormContext(FormContext anOld, FormField aSEType) {
        FormContext theContext = super.createFormContext();

        theContext.addMember(aSEType);

        if (anOld != null) {
            for (Iterator theIt = anOld.getFieldNames(); theIt.hasNext();) {
                String theKey = (String) theIt.next();

                if (!PARAMETER_TYPE.equals(theKey) && theContext.hasMember(theKey)) {
                    FormField theOldField = anOld.getField(theKey);
                    FormField theNewField = theContext.getField(theKey);

                    theNewField.setValue(theOldField.getValue()); 
                }
            }
        }

        return theContext;
    }

    /**
	 * Called if SE is changed to update type.
	 * 
	 * @param type
	 *        the SE type
	 */
	protected void setStructureType(TLClass type) {
		metaElement = (PersistentClass) type;
    }
    
	@Override
	protected void handleNewModel(Object newModel) {
		super.handleNewModel(newModel);
		StructuredElement newSEModel = (StructuredElement) newModel;
		Set<TLClass> childrenTypes = newSEModel.getChildrenTypes();
		if (childrenTypes.size() > 0) {
			setStructureType(CollectionUtil.getFirst(childrenTypes));
		}
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject instanceof StructuredElement;
	}

    protected StructuredElement getRoot() {
        return this.getFactory().getRoot();
    }

    protected StructuredElementFactory getFactory() {
        return StructuredElementFactory.getInstanceForStructure(this.structureName);
    }

    /** 
     * Return a selection for the type of the new created element.
     * 
     * @param    anElement    The current element to create a child for, must not be <code>null</code>.
     * @param    resPrefix      The resource prefix, must not be <code>null</code>.
     * @return   The requested form field, never <code>null</code>.
     */
	protected FormField getTypeField(StructuredElement anElement, ResPrefix resPrefix, TLClass aDefault) {
		List<TLClass> types = list(getTypeOptions(anElement));
		LabelComparator comparator = LabelComparator.newCachingInstance();
		Collections.sort(types, comparator);
		SelectField theField =
			FormFactory.newSelectField(CreateAttributedStructuredElementComponent.PARAMETER_TYPE, types, false,
				types.size() == 1);
		theField.setOptionComparator(comparator);

		if (types.size() > 0) {
			TLClass type = (aDefault == null) ? chooseInitialType(types) : aDefault;

			theField.setAsSingleSelection(type);
			this.setStructureType(type);
        }

        theField.setMandatory(true);
        theField.addValueListener(this);

        return (theField);
    }

	/**
	 * The options for the {@link #getTypeField(StructuredElement, ResPrefix, TLClass)}.
	 * 
	 * @return Is not allowed to be null or empty.
	 */
	protected Collection<TLClass> getTypeOptions(StructuredElement parent) {
		return parent.getChildrenTypes();
	}

	/**
	 * Hook for subclasses to choose the initial type.
	 * <p>
	 * This method is called only when there are multiple types and when there is no "default" type
	 * given to {@link #getTypeField(StructuredElement, ResPrefix, TLClass)}.
	 * </p>
	 * 
	 * @param types
	 *        The {@link TLClass}es to choose from. Never null or empty.
	 * @return Has to be an element of the given {@link List} of {@link TLClass}, or null.
	 */
	protected TLClass chooseInitialType(List<TLClass> types) {
		return types.get(0);
	}

    /**
	 * Create a new attributed structured element.
	 * 
	 * @author <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
	 */
    public static class CreateAttributedStructuredElementCommandHandler extends AbstractCreateAttributedCommandHandler {

        // Constants

        /** Unique ID of this command handler. */
        public static final String COMMAND_ID = "createElement";

        // Constructors
        
        /**
		 * Creates a {@link CreateAttributedStructuredElementCommandHandler} for attributed
		 * structured elements.
		 */
        public CreateAttributedStructuredElementCommandHandler(InstantiationContext context, Config config) {
            super(context, config);
        }

        // Overridden methods from AbstractCreateAttributedCommandHandler

        /**
         * Extend the map of extracted values by the type of the structured element to be created.
         * 
         * @see com.top_logic.element.meta.gui.AbstractCreateAttributedCommandHandler#extractValues(FormContainer, TLObject)
         */
        @Override
		protected Map<String,Object> extractValues(FormContainer formContainer, TLObject anAttributed) {
            Map<String,Object> theValues = super.extractValues(formContainer, anAttributed);

            theValues.put(CreateAttributedStructuredElementComponent.PARAMETER_TYPE, this.getType(formContainer));

            return theValues;
        }

        /**
         * @see com.top_logic.element.meta.gui.AbstractCreateAttributedCommandHandler#createNewObject(Map, com.top_logic.knowledge.wrap.Wrapper)
         */
        @Override
		public Wrapper createNewObject(Map<String, Object> someValues, Wrapper aModel) {
            StructuredElement theElement = ((StructuredElement) aModel);
            String            theName    = (String) someValues.get(AttributedWrapper.NAME_ATTRIBUTE);
			TLClass theType = (TLClass) someValues.get(CreateAttributedStructuredElementComponent.PARAMETER_TYPE);
            return (Wrapper) callFactory(theElement, theName, theType);
        }

		/**
		 * The call to the factory that will create the new object.
		 * 
		 * @return Must not return null.
		 */
		protected TLObject callFactory(StructuredElement parent, String name, TLClass type) {
			return parent.createChild(name, type);
		}

		@Override
		public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
				Map arguments) {
			Object newElement = super.createObject(component, createContext, formContainer, arguments);
			if (newElement instanceof StructuredElement) {
				ElementEventUtil.sendEvent((StructuredElement) newElement, MonitorEvent.CREATED);
			}
			return newElement;
		}

        // Protected methods

        /** 
         * Extract the structured element type value from the given form context.
         * 
         * @param    formContainer    The context to get the type from, must not be <code>null</code>.
         * @return   The structured element type, never <code>null</code> or empty.
         */
		protected TLClass getType(FormContainer formContainer) {
            SelectField theSelect = (SelectField) formContainer.getField(CreateAttributedStructuredElementComponent.PARAMETER_TYPE);

			return (TLClass) theSelect.getSingleSelection();
        }
    }
}

