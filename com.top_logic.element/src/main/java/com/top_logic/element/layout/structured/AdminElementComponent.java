/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.component.EditAttributedComponent;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.SwitchEditCommandHandler;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Edit component for the {@link com.top_logic.element.structured.StructuredElement}.
 * 
 * This component is able to create, modify and delete a {@link com.top_logic.element.structured.StructuredElement}.
 * It will only refer the interface, not the implementation behind.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class AdminElementComponent extends EditAttributedComponent {

    public interface Config extends EditAttributedComponent.Config {
		@Name(AdminElementComponent.EDIT_ROOT)
		@BooleanDefault(false)
		boolean getEditRoot();

		@Name(AdminElementComponent.ELEMENT_STRUCTURE_NAME)
		String getStructure();

		@Override
		@StringDefault(StructuredElementApplyHandler.COMMAND)
		String getApplyCommand();

		@Override
		@StringDefault(StructuredElementRemoveHandler.COMMAND)
		String getDeleteCommand();

		@Override
		@StringDefault(StructuredElementSwitchEditCommandHandler.COMMAND_ID)
		String getEditCommand();

	}

	/** FormConstraint/input name for attribute name. */
    public static final String ELEMENT_NAME                 = "elementName";

    /** FormConstraint/input name for attribute type. */
    public static final String ELEMENT_TYPE                 = "elementType";

    /** FormConstraint/input name for attribute order. */
    public static final String ELEMENT_ORDER 				= "elementOrder";

    /** Constant for the configuration for defining the name of the structure to be displayed. */
    protected static final String ELEMENT_STRUCTURE_NAME 	= "structure";

    /** Layout xml definition attribute to set if the root node should be editable. */ 
    public static final String EDIT_ROOT = "editRoot";
    
    /** The name of the used structure (needed for finding the root node). */
    private transient String structureName;
    
    /** If true root node may be edited. */
    private transient boolean editRoot;
    
    /**
     * sets a boolean flag to indicate whether editing of attributes
     * on the root element is possible or not  
     */
    protected void setEditRoot(boolean flag){
    	this.editRoot = flag;
    }
    
    /**
     * a boolean flag to indicate whether editing of attributes
     * on the root element is possible or not
     */
    protected boolean allowEditRoot(){
    	return this.editRoot;
    }
    /**
     * Standard constructor when loading the class via the layout framework.
     * 
     * @param    someAttrs       Configuration values.
     */
    public AdminElementComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);
        
        this.alwaysReloadButtons = true;
        
        this.editRoot      = someAttrs.getEditRoot();
        // TODO MGA/KBU/KHA this should be mandatory 
        this.structureName = StringServices.nonEmpty(someAttrs.getStructure());
    }

    @Override
	protected boolean supportsInternalModelNonNull(Object anObject) {
		if (!(anObject instanceof StructuredElement)) {
            return false;
        }
        return (this.structureName == null) || this.structureName.equals(((StructuredElement) anObject).getStructureName());
    }

    /**
	 * Automatically create a FormContext based on {@link TLObject} / {@link TLClass}.
	 * 
	 * You may wish to override this to add some fixed attributes.
	 * 
	 * @param newMode
	 *        Used when creating a new Object
	 * @return The requested form context.
	 */
    @Override
	public FormContext createFormContext(boolean newMode) {
        Object theModel = this.getModel();

        if (theModel instanceof Wrapper) {
			return super.createFormContext(newMode);
        }
        else if (theModel instanceof StructuredElement) {
            StructuredElement theElement = (StructuredElement) theModel;
            FormContext       theContext = new FormContext("structured", this.getResPrefix());
            StringField       theName    = FormFactory.newStringField(ELEMENT_NAME, theElement.getName(), false);
            StringField       theType    = FormFactory.newStringField(ELEMENT_TYPE, theElement.getElementType(), true);

            theName.setMandatory(true);

            theContext.addMember(theName);
            theContext.addMember(theType);

            return (theContext);
        }
        else {
            return (new FormContext("empty", this.getResPrefix()));
        }
    }

    /**
     * The name of the supported meta element, must not be <code>null</code>.
     */
    @Override
	protected String getMetaElementName() {
        return (null);
    }

	/**
	 * This component has no {@link #getMetaElementName()}, therefore the {@link #getMetaElement()}
	 * is <code>null</code>.
	 */
	@Override
	public TLClass getMetaElement() {
		return null;
	}

    /**
     * the component name of the dialog component which is responsible for creating new elements.
     */
    protected String getCreateDialogComponentName() {
        return "createStructuredElement";
    }

    /** 
     * Overriden to add "Name", "Type" and "Order" fields.
     * 
     * @see com.top_logic.element.meta.form.component.EditAttributedComponent#addMoreAttributes(java.lang.Object, com.top_logic.element.meta.form.AttributeFormContext, boolean)
     */
    @Override
	protected void addMoreAttributes(Object aModel, AttributeFormContext aContext, boolean create) {
        if (aModel != null) {
            StructuredElement theElement = (StructuredElement) aModel;
			ResPrefix thePrefix = this.getResPrefix();
    
            aContext.addMember(this.createNameField(theElement, thePrefix));
            aContext.addMember(this.createTypeField(theElement, thePrefix, create));
            aContext.addMember(this.createOrderField(theElement, thePrefix, create));
        }
        super.addMoreAttributes(aModel, aContext, create);
    }

    /**
	 * Create a "elementName" StringField with size constraints
	 * @return the StringField
	 */
	protected FormMember createNameField(StructuredElement anElement, ResPrefix resPrefix) {
        StringField theField = FormFactory.newStringField(AdminElementComponent.ELEMENT_NAME, /* mandatory */ true, /* immutable */ false, new StringLengthConstraint(1, 255));
        String theName = anElement.getName();
		theField.initializeField(theName);
        return (theField);
    }

    /**
	 * Create a type StringField with a special label provider
	 * 
	 * @return the field
	 */
	protected FormMember createTypeField(StructuredElement anElement, ResPrefix resPrefix, boolean create) {
        StringField theField = FormFactory.newStringField(AdminElementComponent.ELEMENT_TYPE, true);

		theField
			.setValue(Resources.getInstance().getString(TLModelNamingConvention.getTypeLabelKey(anElement.tType())));

        return (theField);
    }

    /**
	 * Create an order SelectField for change element sort order
	 * 
	 * @return the field
	 */
	protected FormMember createOrderField(StructuredElement anElement, ResPrefix resPrefix, boolean create) {
        StructuredElement[] theSiblings = (create) ? new StructuredElement[0] : StructuredElementFactory.getSiblings(anElement);
        StructuredElement   theNext     = StructuredElementFactory.findNextSibling(anElement);
        List                theList     = Arrays.asList(theSiblings);
        SelectField         theField    = FormFactory.newSelectField(AdminElementComponent.ELEMENT_ORDER, theList, false, (theSiblings.length == 0));
        theField.setOptionLabelProvider(new LabelProvider() {

            @Override
			public String getLabel(Object anObject) {
                if (anObject instanceof StructuredElement) {
                    return (((StructuredElement) anObject).getName());
                }
                else {
					return (Resources.getInstance().getString(getResPrefix().key("atEnd")));
                }
            }
        });
        theField.setAsSingleSelection(theNext);

        return (theField);
    }

    /**
     * Return the factory for  {@link com.top_logic.element.structured.StructuredElement elements}.
     * 
     * @return    The factory to be used for creating elements, must not be <code>null</code>.
     */
    protected StructuredElementFactory getStructuredElementFactory() {
        return StructuredElementFactory.getInstanceForStructure(this.structureName);
    }

    /**
     * Return the configuration value of {@link AdminElementComponent#ELEMENT_STRUCTURE_NAME}
     */
    protected String getStructureName() {
        return (structureName);
    }

    public static class StructuredElementSwitchEditCommandHandler extends SwitchEditCommandHandler {
    	
        public static final String COMMAND_ID = "ElementSwitchEditCommand";
        
    	public StructuredElementSwitchEditCommandHandler(InstantiationContext context, Config config) {
    		super(context, config);
		}

    	@Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
			return CombinedExecutabilityRule.combine(super.createExecutabilityRule(), EditRootExecutable.INSTANCE);
    	}
    }
    
    private static class EditRootExecutable implements ExecutabilityRule {
    	
        private static final ExecutableState EXEC_STATE_DISABLED 
			= ExecutableState.createDisabledState(I18NConstants.ERROR_ROOT_CANNOT_BE_EDITED);
        
    	public static final ExecutabilityRule INSTANCE = new EditRootExecutable();
    	
    	@Override
		public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
    		AdminElementComponent theComp    = (AdminElementComponent) component;
			StructuredElement theElement = (StructuredElement) model;
    		if (theComp.editRoot || (theElement != null && !theElement.isRoot())) {
    			return ExecutableState.EXECUTABLE;
    		}
    		return EditRootExecutable.EXEC_STATE_DISABLED;
    	}
    }
}
