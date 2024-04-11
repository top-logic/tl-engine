/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.layout.create.CreateFormBuilder;
import com.top_logic.element.layout.meta.DefaultFormContextModificator;
import com.top_logic.element.layout.meta.FormContextModificator;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.component.EditAttributedComponent;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.Visibility;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.CommandHandlerUtil;

/**
 * Generic component to create a new object.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 * 
 * @deprecated Use {@link AbstractCreateComponent} with {@link CreateFormBuilder}
 */
@Deprecated
public abstract class CreateAttributedComponent extends AbstractCreateComponent implements DynamicTypeContext {

	/**
	 * Relevant information for creating an {@link Wrapper}.
	 */
	public interface Config extends AbstractCreateComponent.Config {

		/**
		 * The {@link FormContextModificator} is used to modify the {@link FormMember}s when a new
		 * object is created.
		 */
		@Name(EditAttributedComponent.XML_CONFIG_MODIFIER_CLASS)
		@ItemDefault
		@ImplementationClassDefault(DefaultFormContextModificator.class)
		PolymorphicConfiguration<FormContextModificator> getModifier();

	}

	/** Modifying instance for the form members during creation of an input row. */
	private final FormContextModificator _modifier;

	/** Create a {@link CreateAttributedComponent} from XML. */
	public CreateAttributedComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_modifier = context.getInstance(config.getModifier());
	}

    /**
	 * Create an AttributeFormContext based on a type.
	 */
    @Override
	public FormContext createFormContext() {
        AttributeFormContext theContext = new AttributeFormContext(this.getResPrefix());
        TLClass          theMeta    = getMetaElement();

        this.addAttributedConstraints(theMeta, theContext);
        this.addMoreAttributes(theMeta, theContext);

        return theContext;
    }

    /**
	 * Automatically create the {@link FormField} objects for the {@link TLClass} in the
	 * {@link FormContext}.
	 */
	protected void addAttributedConstraints(TLClass type, AttributeFormContext formContext) {
		if (_modifier.preModify(this, type, null)) {
			Object targetModel;
			/* The default command in a create component is the create command. */
			if (getDefaultCommand() == null) {
				targetModel = getModel();
			} else {
				Map<String, Object> commandArgs = Collections.emptyMap();
				targetModel = CommandHandlerUtil.getTargetModel(getDefaultCommand(), this, commandArgs);
			}
			TLObject createContext = targetModel instanceof TLObject ? (TLObject) targetModel : null;

			TLFormObject creation = formContext.createObject(type, null, createContext);
			for (TLStructuredTypePart attribute : type.getAllParts()) {
				this.addAnotherMetaAttribute(formContext, type, creation, attribute);
			}
			_modifier.postModify(this, type, null, formContext, formContext);
        }
    }

    /**
	 * Add an attribute update for the given meta attribute to the given form context.
	 * 
	 * @param formContext
	 *        The form context to append the update to, must not be <code>null</code>.
	 * @param creation
	 *        The object being created
	 * @param attribute
	 *        The meta attribute to be appended, must not be <code>null</code>.
	 */
	protected void addAnotherMetaAttribute(AttributeFormContext formContext, TLClass type,
			TLFormObject creation, TLStructuredTypePart attribute) {
		Visibility visibility = getAttributeVisibility(attribute);

		if (visibility != Visibility.HIDDEN && !DisplayAnnotations.isHiddenInCreate(attribute)) {
			String attributeName = attribute.getName();
			AttributeUpdate update = creation.newCreateUpdate(attribute);

			if (update != null) {
				update = this.modifyUpdateForAdd(attributeName, attribute, update);

				if (update != null) {
					FormMember formMember = formContext.addFormConstraintForUpdate(update);

					this.postProcessFormMember(attributeName, formMember, attribute, update, formContext);
					_modifier.modify(this, attributeName, formMember, attribute, type, null, update, formContext,
						formContext);
                }
            }
        }
    }

    /**
     * Hook for Subclasses to add extra attributes. 
     * 
     * @param aME        Meta element to add the constraints for, must not be <code>null</code>.
     * @param aContext   The form context, must not be <code>null</code>.
     */
    protected void addMoreAttributes(TLClass aME, AttributeFormContext aContext) {
        // empty here
    }

    /** 
     * Hook for subclass to modify the AttributeUpdate before the FormField is created.
     *  
     * @param    aName      The name of the meta attribute, must not be <code>null</code>.
     * @param    aMA        Meta attribute represented by the update, must not be <code>null</code>.
     * @param    anUpdate   The created update to be modified, must not be <code>null</code>.
     * @return   The modified update, may be <code>null</code>.
     */
    protected AttributeUpdate modifyUpdateForAdd(String aName, TLStructuredTypePart aMA, AttributeUpdate anUpdate) {
        return anUpdate;
    }

    /** 
     * Hook for sub classes to post process the created form member.
     */
    protected void postProcessFormMember(String aName, FormMember aMember, TLStructuredTypePart aMA, AttributeUpdate anUpdate, AttributeFormContext aContext) {
        // this is a hook only
    }

	/**
	 * Return how and if given attribute shall be used in FormContext.
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
		if (AttributeOperations.isReadOnly(attribute)) {
			return Visibility.HIDDEN;
		}
		if (excludeFromFormContext(attribute)) {
			return Visibility.HIDDEN;
		}
		return Visibility.EDITABLE;
	}

    /**
     * Return a set of meta attribute names to be excluded in automatic display.
     * 
     * @return    The set of attribute names to be excluded, never <code>null</code>.
     */
    public Set<String> getExcludeForUI() {
		Set<String> excludes = new HashSet<>();

		for (TLStructuredTypePart attribute : TLModelUtil.getMetaAttributes(getMetaElement())) {
			if (excludeFromFormContext(attribute)) {
				excludes.add(attribute.getName());
            }
        }

		return excludes;
    }
    
	/**
	 * @param attribute
	 *        The attribute to check.
	 * 
	 * @return Whether the given attribute must be excluded from the FormContext (and the JSP later
	 *         on).
	 */
	protected boolean excludeFromFormContext(TLStructuredTypePart attribute) {
		if (attribute.isMandatory()) {
			return false;
		}
		return DisplayAnnotations.isHiddenInCreate(attribute);
	}

    /** 
     * Small static helper for createHandlers.
     * 
     * @deprecated    Use the {@link AbstractCreateAttributedCommandHandler} for creating new attributed objects.
     */
    @Deprecated
	public static Object getMetaFieldValue(AttributeFormContext aContext, TLClass theMeta, String anAttr) throws NoSuchAttributeException {
        return CollectionUtil.getSingleValueFrom(aContext.getField(anAttr, theMeta).getValue()); 
    }

}

