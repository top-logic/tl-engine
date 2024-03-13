/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.element.layout.meta.FormContextModificator;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.UpdateFactory;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.gui.DynamicTypeContext;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.compare.CompareAlgorithm;
import com.top_logic.layout.compare.CompareAlgorithmHolder;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.declarative.WithShowNoModel;
import com.top_logic.layout.form.decorator.CompareService;
import com.top_logic.layout.form.decorator.DecorateService;
import com.top_logic.layout.form.model.FolderField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.Visibility;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContext;

/**
 * Edit component for {@link com.top_logic.knowledge.wrap.Wrapper} objects.
 *
 * This component will take care of the form context, when there are changes
 * to the meta element representing this {@link com.top_logic.knowledge.wrap.Wrapper}.
 *
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class EditAttributedComponent extends EditComponent implements FormContextModificator,
		DynamicTypeContext, CompareAlgorithmHolder {

	/**
	 * Configuration options for {@link EditAttributedComponent}.
	 */
	public interface Config extends EditComponent.Config, WithShowNoModel {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Name of {@link #getReadOnlyAttributes()}. */
		String READ_ONLY_ATTRIBUTES_NAME = "readOnlyAttributes";

		/** Name of {@link #getExcludeFromAdditionalAttributes()}. */
		String EXCLUDE_FROM_ADDITIONAL_ATTRIBUTES_NAME = "excludeFromAdditionalAttributes";

		/** Name of {@link #getExcludeAttributes()}. */
		String EXCLUDE_ATTRIBUTES_NAME = "excludeAttributes";

		/** Name of the {@link #getShowCompareCommand()}. */
		String SHOW_COMPARE_COMMAND_NAME = "show-compare-command";

		/** name of the configuration property {@link #getExcludeSubtypes()}. */
		String EXCLUDE_SUBTYPES_NAME = "exclude-subtypes";

		/** Modifying instance for the {@link FormMember}s during creation of an input row. */
		@Name(EditAttributedComponent.XML_CONFIG_MODIFIER_CLASS)
		PolymorphicConfiguration<FormContextModificator> getModifier();

		/**
		 * Whether this component should only support direct instances of
		 * {@link EditAttributedComponent#getMetaElementName() the component's intrinsic type} and
		 * not instances of any sub-types of it.
		 * 
		 * @see EditAttributedComponent#getMetaElement()
		 */
		@Name(EXCLUDE_SUBTYPES_NAME)
		@BooleanDefault(false)
		boolean getExcludeSubtypes();

		/**
		 * Whether the {@link AttributedCompareCommandHandler#COMMAND_ID compare command} should be
		 * shown.
		 */
		@Name(SHOW_COMPARE_COMMAND_NAME)
		Boolean getShowCompareCommand();

		/**
		 * Name of the attributes not to include in the {@link FormContext}.
		 * 
		 * @see EditAttributedComponent#getExcludeList()
		 */
		@Format(CommaSeparatedStrings.class)
		@Name(EXCLUDE_ATTRIBUTES_NAME)
		List<String> getExcludeAttributes();

		/**
		 * Name of the attributes to include in the {@link FormContext}, can be referenced in the
		 * JSP but are not included into the "additional attributes".
		 * 
		 * @see EditAttributedComponent#getExcludeListForUI()
		 */
		@Format(CommaSeparatedStrings.class)
		@Name(EXCLUDE_FROM_ADDITIONAL_ATTRIBUTES_NAME)
		List<String> getExcludeFromAdditionalAttributes();

		/**
		 * Name of the attributes to display "read-only" in the {@link FormContext}.
		 * 
		 * @see EditAttributedComponent#getReadOnlyList()
		 */
		@Format(CommaSeparatedStrings.class)
		@Name(READ_ONLY_ATTRIBUTES_NAME)
		List<String> getReadOnlyAttributes();

		@Override
		@StringDefault(DefaultApplyAttributedCommandHandler.COMMAND_ID)
		String getApplyCommand();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			Boolean showCompareCommand = getShowCompareCommand();
			if (showCompareCommand == null) {
				GlobalConfig globalConfig = ApplicationConfig.getInstance().getConfig(GlobalConfig.class);
				showCompareCommand = Boolean.valueOf(globalConfig.getShowCompareCommand());
			}
			if (showCompareCommand.booleanValue()) {
				registry.registerButton(AttributedCompareCommandHandler.COMMAND_ID);
			}
			EditComponent.Config.super.modifyIntrinsicCommands(registry);

		}

	}

	/**
	 * Global configuration options for all {@link EditAttributedComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface GlobalConfig extends ConfigurationItem {

		/**
		 * @see Config#getShowCompareCommand()
		 */
		@Name(Config.SHOW_COMPARE_COMMAND_NAME)
		boolean getShowCompareCommand();

	}

    /** A class for modifying the form members during creation of an input row. */
    public static final String XML_CONFIG_MODIFIER_CLASS = "modifier";

    /**
     * whether the build {@link FormContext} contains {@link FolderField}.
     */
	private boolean hasFolderFields;

	private final FormContextModificator modifier;

	/** @see Config#getExcludeSubtypes() */
	private final boolean _excludeSubTypes;

	private CompareAlgorithm _compareAlgorithm;

	/** Creates an {@link EditAttributedComponent}. */
	public EditAttributedComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		modifier = createModifier(context, config.getModifier());
		_excludeSubTypes = config.getExcludeSubtypes();
	}

	private FormContextModificator createModifier(InstantiationContext context,
			PolymorphicConfiguration<FormContextModificator> modifierConfig) {
		FormContextModificator result = context.getInstance(modifierConfig);
		return result != null ? result : this;
	}

    /**
     * The name of the supported meta element, must not be <code>null</code>.
     */
    protected abstract String getMetaElementName();

    @Override
	protected final boolean supportsInternalModel(Object anObject) {
		if (anObject == null) {
			return supportsInternalModelNull();
		} else {
			return supportsInternalModelNonNull(anObject);
		}
	}

	/**
	 * Whether the component should display even for <code>null</code> as model.
	 * 
	 * @see Config#getShowNoModel()
	 */
	protected boolean supportsInternalModelNull() {
		return ((Config) getConfig()).getShowNoModel();
	}

	/**
	 * Implementation of {@link #supportsInternalModel(Object)} for non-<code>null</code> objects.
	 * 
	 * @see Config#getShowNoModel()
	 */
	protected boolean supportsInternalModelNonNull(Object anObject) {
		if (!(anObject instanceof Wrapper)) {
			return false;
		}
		if (!hasCorrectType(anObject)) {
			return false;
		}
		return super.supportsInternalModel(anObject);
	}

	private boolean hasCorrectType(Object o) {
		String typeName = getMetaElementName();
		int nameSeparator = typeName.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		String localTypeName;
		String moduleName;
		if (nameSeparator == -1) {
			moduleName = null;
			localTypeName = typeName;
		} else {
			moduleName = typeName.substring(0, nameSeparator);
			localTypeName = typeName.substring(nameSeparator + 1);
		}

		TLClass objectType = (TLClass) ((Wrapper) o).tType();
		if (objectType == null) {
			return false;
		}
		boolean hasCorrectType;
		if (_excludeSubTypes) {
			// Check correct type name
			hasCorrectType = objectType.getName().equals(localTypeName);
			if (hasCorrectType && moduleName != null) {
				// Check correct module name
				hasCorrectType = objectType.getModule().getName().equals(moduleName);
			}
		} else {
			hasCorrectType = MetaElementUtil.isSubtype(moduleName, localTypeName, objectType);
		}
		return hasCorrectType;
	}

	@Override
	public boolean unCachedAllow(BoundCommandGroup aCmdGroup, BoundObject anObject) {
		SecurityConfiguration theMAAB = this.getSecurityConfiguration(anObject, aCmdGroup);
		
		if (theMAAB == null) {
			return super.unCachedAllow(aCmdGroup, anObject);
		}
		
		// If a attribute is given we will check the attribute security
		if (theMAAB.metaAttribute != null) {	// Check attribute security
			if (!TLContext.isAdmin()) {
				if (AttributeOperations.isClassified(theMAAB.metaAttribute)) {
					TLContext theContext = TLContext.getContext();
			        Set<BoundRole>         theRoles  = AccessManager.getInstance().getRoles(theContext == null ? null : theContext.getCurrentPersonWrapper(), theMAAB.boundObject);
			        Set<BoundCommandGroup> theAccess = AttributeOperations.getAccess(theMAAB.metaAttribute, theRoles);
	
			        if (!theAccess.contains(aCmdGroup)) {
			        	return false;
			        }
				}
			}
		}
		
		// If no BoundObject is given we will use the input parameter
		if (theMAAB.boundObject == null) {
			theMAAB.boundObject = anObject;
		}
		
		// If a different BoundChecker is given we will dispatch the security check to it
    	if (theMAAB.boundChecker != null && theMAAB.boundChecker != this) {
    		return theMAAB.boundChecker.allow(aCmdGroup, anObject);
    	}
    	
    	// If no BoundChecher is given we will let the default checker check the security
    	if (theMAAB.boundChecker == null) {
			return BoundHelper.getInstance().getDefaultChecker(getMainLayout(), theMAAB.boundObject, aCmdGroup) != null;
    	}

    	// Otherwise we use the normal security on the given BoundObject
        return super.unCachedAllow(aCmdGroup, theMAAB.boundObject);
	}
	
	/**
	 * Get the configuration for the security check in {@link #unCachedAllow(BoundCommandGroup, BoundObject)}
	 * 
	 * @param anObject	the BoundObject
	 * @param aBCG		the BoundCommandGroup
	 * @return the security configuration
	 */
	protected SecurityConfiguration getSecurityConfiguration(BoundObject anObject, BoundCommandGroup aBCG) {
		return null;
	}
	
	/**
	 * Security configuration for
	 * {@link EditAttributedComponent#unCachedAllow(BoundCommandGroup, BoundObject)} consisting of -
	 * an optional attribute (no fallback) - an optional BoundObject (fallback is usually the given
	 * BoundObject in the checking method) - an optional BoundChecker (fallback is usually the
	 * default checker for the BoudnObject in the checking method)
	 * 
	 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
	 */
	public static class SecurityConfiguration {
		public TLStructuredTypePart metaAttribute;
		public BoundObject   boundObject;
		public BoundChecker  boundChecker;
		public SecurityConfiguration(TLStructuredTypePart aMetaAttribute, BoundObject aBoundObject, BoundChecker aBoundChecker) {
			this.metaAttribute = aMetaAttribute;
			this.boundObject   = aBoundObject;
			this.boundChecker  = aBoundChecker;
		}
	}

	@Override
	protected Set<? extends TLStructuredType> getTypesToObserve() {
		return Set.of(
			TlModelFactory.getTLStructuredTypePartType(),
			WebFolder.getWebFolderType(),
			Document.getDocumentType(),
			DocumentVersion.getDocumentVersionType());
	}

    /**
     * If the created model is a {@link TLStructuredTypePart} and the matching {@link TLClass}
     * is used by this model, the form context will be resetted.
     *
     * @see com.top_logic.mig.html.layout.LayoutComponent#receiveModelCreatedEvent(java.lang.Object, java.lang.Object)
     */
    @Override
	protected boolean receiveModelCreatedEvent(Object aModel, Object someChangedBy) {
        boolean theResult = false;

        updateWebFolders(aModel);
        
        if (aModel instanceof TLStructuredTypePart) {
            TLClass theME    = AttributeOperations.getMetaElement(((TLStructuredTypePart) aModel));
            Object      theModel = this.getModel();

            if (theModel instanceof Wrapper) {
				theResult = TLModelUtil.isGeneralization(theME, ((Wrapper) theModel).tType());

                if (theResult) {
					this.removeFormContext();
                }
            }
        }

        return (theResult || super.receiveModelCreatedEvent(aModel, someChangedBy));
    }

	/**
	 * updates the {@link FolderField}s in the {@link FormContext} with the
	 * given model.
	 * 
	 * @param aModel
	 *        the changed model.
	 */
	private void updateWebFolders(Object aModel) {
		if (hasFolderFields) {
    		WebFolderUtils.updateWebfolder(this, aModel);
    	}
	}

    /**
     * If the deleted model is a {@link TLStructuredTypePart}, this component will forget its form context.
     *
     * @see com.top_logic.mig.html.layout.LayoutComponent#receiveModelDeletedEvent(Set, java.lang.Object)
     */
    @Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object someChangedBy) {
		boolean theResult = models.stream().anyMatch(TLStructuredTypePart.class::isInstance);

		models.forEach(this::updateWebFolders);
        
        if (theResult) {
			this.removeFormContext();
        }

		return (theResult | super.receiveModelDeletedEvent(models, someChangedBy));
    }

    @Override
    protected boolean receiveModelChangedEvent(Object aModel, Object changedBy) {
    	updateWebFolders(aModel);
    	return super.receiveModelChangedEvent(aModel, changedBy);
    }
    
    @Override
	protected void writeJSTags(String aPath, TagWriter anOut, HttpServletRequest aRequest) throws IOException {
        super.writeJSTags(aPath, anOut, aRequest);

        if (this.isInEditMode()) {
			HTMLUtil.writeJavascriptRef(anOut, aPath, "/script/popUpSupport.js");
			HTMLUtil.writeJavascriptRef(anOut, aPath, "/script/nonNullCheckbox.js");
			HTMLUtil.writeJavascriptRef(anOut, aPath, "/script/tristate.js");
        }
    }

    @Override
	public FormContext createFormContext() {
        return (this.createFormContext(/* newMode */ false));
    }

	/**
	 * Automatically create a FormContext based on {@link TLObject}/{@link TLClass}.
	 * 
	 * You may wish to override this to add some fixed attributes.
	 * 
	 * @param newMode
	 *        Used when creating a new Object
	 * @return The requested form context.
	 */
	public FormContext createFormContext(boolean newMode) {
		Wrapper theAttributed = (Wrapper) this.getModel();
		CompareAlgorithm compareAlgorithm = getCompareAlgorithm();
		FormContext theContext = this.createFormContext(newMode, theAttributed);

		if (!newMode && compareAlgorithm != null) {
			updateFormContextByCompare(theContext, theAttributed, compareAlgorithm);
		}

		return theContext;
	}

    /**
	 * Automatically create a FormContext based on {@link TLObject}/{@link TLClass}.
	 *
	 * You may wish to override this to add some fixed attributes.
	 *
	 * @param newMode
	 *        Used when creating a new Object
	 * @return The requested form context.
	 */
    public FormContext createFormContext(boolean newMode, Wrapper theAttributed) {
		AttributeFormContext theContext = new AttributeFormContext(this.getResPrefix());

        if (!newMode) {
            this.addAttributedConstraints(theAttributed, theContext);
        }

        this.addMoreAttributes(theAttributed, theContext, newMode);

        return theContext;
    }

    /**
	 * Automatically add Constraints for a {@link TLObject}/{@link TLClass} to theContext.
	 *
	 * @param theAttributed
	 *        the object
	 * @param formContext
	 *        the form context
	 */
	protected void addAttributedConstraints(Wrapper theAttributed, AttributeFormContext formContext) {
        TLClass theME = this.getMetaElement(theAttributed);
        // List theExcludeList = this.getExcludeList(); // Handled via getAttributeAddMode

        this.hasFolderFields = false;
        if (theME != null) {
			if (this.modifier.preModify(this, theME, theAttributed)) {
				UpdateFactory overlay = formContext.editObject(theAttributed);

				for (TLStructuredTypePart theMA : TLModelUtil.getMetaAttributes(theME)) {
                    String          theName    = theMA.getName();
					Visibility visibility = getAttributeVisibility(theMA);
    
					if (visibility != Visibility.HIDDEN && !DisplayAnnotations.isHidden(theMA)) {
						boolean isDisabled = visibility == Visibility.READ_ONLY;
						AttributeUpdate theUpdate = overlay.newEditUpdateDefault(theMA, isDisabled);
						if (theUpdate != null) {
                            theUpdate = this.modifyUpdateForAdd(theName, theMA, theAttributed, theUpdate);
    
                            if (theUpdate != null) {
								FormMember theMember = formContext.addFormConstraintForUpdate(theUpdate);
    
                                if (theMember instanceof FolderField) {
                                	this.hasFolderFields = true;
                                }
								else if (theMember instanceof SelectField) {
									SelectField selectField = (SelectField) theMember;
									makeConfigurable(selectField, theName);
								}
    
								this.modifier.modify(this, theName, theMember, theMA, theME, theAttributed, theUpdate,
									formContext, formContext);
                            }
                        }
                    }
                }

				this.modifier.postModify(this, theME, theAttributed, formContext, formContext);
            }
        }
    }

    /**
     * Hook for sub classes to modify the update object when created.
     *
     * This can be used to disable the field for the form context or remove it from that.
     *
     * @param    aName           The name of the meta attribute, must not be <code>null</code>.
     * @param    aMA             The meta attribute to get the value for, must not be <code>null</code>.
     * @param    anAttributed    The matching attributed, must not be <code>null</code>.
     * @param    anUpdate        The update to be added, must not be <code>null</code>.
     * @return   The update to be added, may be <code>null</code>, which will not add the constraint to the context.
     * @see      #addAttributedConstraints(Wrapper, AttributeFormContext)
     */
    protected AttributeUpdate modifyUpdateForAdd(String aName, TLStructuredTypePart aMA, Wrapper anAttributed, AttributeUpdate anUpdate) {
        return (anUpdate);
    }

	/**
	 * Get the mode how the attribute can be rendered.
	 * 
	 * <p>
	 * A return value {@link Visibility#HIDDEN} means not to include the attribute in the
	 * {@link FormContext}.
	 * </p>
	 *
	 * @param attribute
	 *        the attribute
	 * @return The {@link Visibility}
	 */
	protected Visibility getAttributeVisibility(TLStructuredTypePart attribute) {
		String attributeName = attribute.getName();
		if (getExcludeList().contains(attributeName)) {
			return Visibility.HIDDEN;
		}

		if (getReadOnlyList().contains(attributeName)) {
			return Visibility.READ_ONLY;
		}

		return Visibility.EDITABLE;
	}

	private Config config() {
		return (Config) getConfig();
	}

    /**
	 * Return the list of attribute names to be excluded from the FormContext (and the JSP later
	 * on).
	 *
	 * @return The unmodifiable List of attribute names to be excluded.
	 */
    public List<String> getExcludeList() {
		return new ArrayList<>(config().getExcludeAttributes());
    }

    /**
     * Return the list of values to be hidden in the UI.
     *
     * This method will be called by the UI for expanding the list afterwards.
     *
     * @return    The list of values to be excluded from the UI.
     */
    public List<String> getExcludeListForUI() {
		return new ArrayList<>(config().getExcludeFromAdditionalAttributes());
    }

	/**
	 * Do not edit some MetaAttributes
	 *
	 * @return the list of attribute names that are read-only
	 */
	public List<String> getReadOnlyList() {
		return new ArrayList<>(config().getReadOnlyAttributes());
	}

    /**
     * Hook for sub classes to post process the created form member.
     */
    protected void postProcessFormMember(String aName, FormMember aMember, TLStructuredTypePart aMA, TLObject anAttributed, AttributeUpdate anUpdate, AttributeFormContext aContext) {
        // Nothing to do here, this is a hook
    }

    /**
	 * Return the meta element for the given attributed.
	 *
	 * If the given attributed is <code>null</code>, the method will use the
	 * {@link #getMetaElementName()} method. If that also returns <code>null</code>, this method
	 * will return <code>null</code>.
	 *
	 * @param anAttributed
	 *        The attributed to get the meta element for, may be <code>null</code>.
	 * @return The requested element, may be <code>null</code>.
	 * @throws IllegalStateException
	 *         If the method is not able to get the type.
	 */
    protected TLClass getMetaElement(Wrapper anAttributed) throws IllegalStateException {
        if (anAttributed != null) {
			return (TLClass) (anAttributed.tType());
		} else {
            return getMetaElement();
        }
    }

	@Override
	public TLClass getMetaElement() {
		// null handling
		String theName = this.getMetaElementName();

		if (theName != null) {
			return (MetaElementFactory.getInstance().getGlobalMetaElement(theName));
		}
		else {
			throw new IllegalStateException("No attributed element found and no type name defined.");
        }
    }

    /**
     * Hook for Subclasses to add extra attributes.
     *
     * @param someObject    the object to add the constraints for (may also be a collection)
     * @param someContext   the form context
     * @param create        if true the object will be created, i.e. is <code>null</code> normally.
     */
    protected void addMoreAttributes(Object someObject, AttributeFormContext someContext, boolean create) {
        if (!create && (someObject instanceof Wrapper)) {
            List<Wrapper> theValue = CollectionUtil.intoListNotNull((Wrapper)someObject);
            SelectField      theField = FormFactory.newSelectField(WrapperAccessor.SELF, theValue, false, theValue, true);

            someContext.addMember(theField);
        }
    }

	public FormField getField(FormContext aContext, Wrapper aProject, String anAttributeName) {
	    try {
	        String fieldName = MetaAttributeGUIHelper.getAttributeID(anAttributeName, aProject);

	        return (FormField) aContext.getFirstMemberRecursively(fieldName);
	    }
	    catch (Exception e) {
	        Logger.error("Could not get the field name of the attribute ('" + anAttributeName + "') for the attributed ('" + ((Wrapper)aProject).getName() + "')", e, this);
	        return null;
	    }

	}

	/**
	 * Return the model as {@link Wrapper}
	 *
	 * @see #getModel()
	 *
	 * @return the current model
	 */
	protected Wrapper getAttributed() {
	    return (Wrapper) this.getModel();
	}

    @Override
	public boolean preModify(LayoutComponent component, TLClass type, TLObject anAttributed) {
        return true;
    }

    @Override
	public void modify(LayoutComponent component, String aName, FormMember aMember, TLStructuredTypePart aMA, TLClass type, TLObject anAttributed, AttributeUpdate anUpdate, AttributeFormContext aContext, FormContainer currentGroup) {
        this.postProcessFormMember(aName, aMember, aMA, anAttributed, anUpdate, aContext);
    }

    @Override
	public void postModify(LayoutComponent component, TLClass type, TLObject anAttributed, AttributeFormContext aContext, FormContainer currentGroup) {
    	// No modification.
    }
    
    @Override
	public void clear(LayoutComponent component, TLClass type,
    		TLObject anAttributed, AttributeUpdateContainer aContainer,
    		FormContainer currentGroup) {
    	// Nothing to clear.
    }
    
	/**
	 * @see #setCompareAlgorithm(CompareAlgorithm)
	 */
	@Override
	public CompareAlgorithm getCompareAlgorithm() {
		return _compareAlgorithm;
	}

	/**
	 * Set the object to be used for compare data in this component.
	 * 
	 * <p>
	 * When the given object is <code>null</code>, the comparing will be deactivated.
	 * </p>
	 * 
	 * @param compareAlgorithm
	 *        The requested compare object, may be <code>null</code>.
	 */
	@Override
	public void setCompareAlgorithm(CompareAlgorithm compareAlgorithm) {
		if (Utils.equals(_compareAlgorithm, compareAlgorithm)) {
			return;
		}
		_compareAlgorithm = compareAlgorithm;
		removeFormContext();
		invalidate();
	}

	/**
	 * Update the given form context by using the {@link DecorateService} for the given compare
	 * object.
	 * 
	 * <p>
	 * When the given object is <code>null</code>, the comparing will be deactivated.
	 * </p>
	 * 
	 * @param aContext
	 *        The context to be extended.
	 * @param anAttributed
	 *        The attributed object the given context represents.
	 * @param algorithm
	 *        The object to be used for compare.
	 * @see DecorateService#annotate(FormContext, DecorateService)
	 */
	protected void updateFormContextByCompare(FormContext aContext, Wrapper anAttributed, CompareAlgorithm algorithm) {
		if (!isInViewMode()) {
			return;
		}
		DecorateService.annotate(aContext, this.getDecorateService(anAttributed, algorithm));
	}
 		 
	/**
	 * Create a matching {@link DecorateService} for the given attributed and compare object.
	 * 
	 * <p>
	 * When the given object is <code>null</code>, the comparing must be deactivated (method will
	 * return <code>null</code> in that cases).
	 * </p>
	 * 
	 * @param anAttributed
	 *        The attributed object the given context represents.
	 * @param algorithm
	 *        The object to be used for compare.
	 */
	protected DecorateService<?> getDecorateService(Wrapper anAttributed, CompareAlgorithm algorithm) {
		if (algorithm == null) {
			return null;
		}

		Object compareObject = algorithm.getCompareObject(this, anAttributed);
		if (!(compareObject instanceof Wrapper)) {
			return null;
		}
		Wrapper compareAttributed = (Wrapper) compareObject;
		return new CompareService<>(createFormContext(false, compareAttributed), algorithm);
	}

}
