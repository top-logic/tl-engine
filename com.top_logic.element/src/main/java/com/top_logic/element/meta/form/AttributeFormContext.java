/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.overlay.ObjectConstructor;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.Media;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * FormContext for MetaAttributes and their values.
 *
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class AttributeFormContext extends FormContext {

	private static final Property<TLFormObject> OVERLAY = TypedAnnotatable.property(TLFormObject.class, "overlay");

	/** Glue Object that ties FormContext back to the underlying object. */
	private final AttributeUpdateContainer _updateContainer;

	private final Media _media;

	/**
	 * Creates a {@link AttributeFormContext}.
	 *
	 * @see FormContext#FormContext(String, ResPrefix)
	 */
	public AttributeFormContext(String name, ResPrefix resPrefix) {
		this(name, resPrefix, Media.BROWSER);
	}
	
	/**
	 * Creates a {@link AttributeFormContext}.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param resPrefix
	 *        See {@link #getResources()}
	 * @param media
	 *        See {@link #getOutputMedia()}
	 */
	public AttributeFormContext(String name, ResPrefix resPrefix, Media media) {
		super(name, resPrefix);
		_updateContainer = new AttributeUpdateContainer(this);
		_media = media;
	}
	
	/**
	 * Creates an {@link AttributeFormContext} with a default name.
	 *
	 * @param resPrefix
	 *        See {@link FormContext#FormContext(String, ResPrefix)}.
	 */
	public AttributeFormContext(ResPrefix resPrefix) {
		this(resPrefix, Media.BROWSER);
	}
	
	/**
	 * Creates an {@link AttributeFormContext} with a default name.
	 *
	 * @param resPrefix
	 *        See {@link FormContext#FormContext(String, ResPrefix)}.
	 * @param media
	 *        The output media.
	 */
	public AttributeFormContext(ResPrefix resPrefix, Media media) {
		this("attributeform", resPrefix, media);
	}

	/**
	 * The {@link AttributeUpdateContainer} with {@link AttributeUpdate}s for attributes displayed
	 * in this form.
	 */
	public final AttributeUpdateContainer getAttributeUpdateContainer() {
		return _updateContainer;
	}

	/**
	 * The current output media.
	 */
	public Media getOutputMedia() {
		return _media;
	}

	/**
	 * @see AttributeUpdateContainer#editObject(TLObject)
	 */
	public TLFormObject editObject(TLObject object) {
		return _updateContainer.editObject(object);
	}

	/**
	 * @see AttributeUpdateContainer#createObject(TLStructuredType, String)
	 */
	public TLFormObject createObject(TLStructuredType type, String domain) {
		return _updateContainer.createObject(type, domain);
	}

	/**
	 * @see AttributeUpdateContainer#createObject(TLStructuredType, String, TLObject)
	 */
	public TLFormObject createObject(TLStructuredType type, String domain, TLObject container) {
		return _updateContainer.createObject(type, domain, container);
	}

	/**
	 * @see AttributeUpdateContainer#createObject(TLStructuredType, String, TLObject,
	 *      ObjectConstructor)
	 */
	public TLFormObject createObject(TLStructuredType type, String domain, TLObject container,
			ObjectConstructor constructor) {
		return _updateContainer.createObject(type, domain, container, constructor);
	}

    /**
	 * Transfers all values in this {@link FormContext} to the underlying
	 * {@link #getAttributeUpdateContainer()}.
	 * 
	 * @see AttributeUpdateContainer#store()
	 */
	public void store() {
		_updateContainer.store();
	}

    /**
	 * @param attributeName
	 *        The name of the meta attribute to get the constraint for.
	 * @param object
	 *        The attributed to identify the constraint.
	 * @return The requested FormField.
	 */
	public FormField getField(String attributeName, TLObject object) throws NoSuchElementException {
		TLStructuredType type = object.tType();
		TLStructuredTypePart attribute = type.getPart(attributeName);
		if (attribute == null) {
			throw new NoSuchElementException("No such attribute '" + attributeName + "' in '" + type + "'.");
		}
		return getField(object, attribute);
	}

	private FormField getField(TLObject object, TLStructuredTypePart attribute) throws NoSuchElementException {
		try {
			return getField(getAttributeID(object, attribute));
		} catch (NoSuchElementException ex) {
			throw (NoSuchElementException) new NoSuchElementException("No field for attribute '" + attribute + "' "
				+ (object == null ? "" : "of object '" + object + "' ") + "in this form, existing members: "
				+ names(getMembers())).initCause(ex);
		}
	}

	private String names(Iterator<? extends FormMember> members) {
		StringBuilder result = new StringBuilder();
		while (members.hasNext()) {
			if (result.length() > 0) {
				result.append(", ");
			}
			FormMember member = members.next();
			AttributeUpdate update = AttributeFormFactory.getAttributeUpdate(member);
			if (update == null) {
				result.append("'");
				result.append(member.getName());
				result.append("'");
			} else {
				result.append(update.getAttribute());
				if (update.getObject() != null) {
					result.append(" of ");
					result.append(update.getObject());
				}
			}
		}
		return result.toString();
	}

	private String getAttributeID(TLObject attributed, String attributeName) {
		return getAttributeID(attributed, attributed.tType().getPart(attributeName));
	}

	private String getAttributeID(TLClass type, String attributeName) {
		return getAttributeID(null, type.getPart(attributeName));
	}

	private String getAttributeID(TLObject attributed, TLStructuredTypePart part) {
		if (part == null) {
			return null;
		}
		return MetaAttributeGUIHelper.getAttributeID(part, attributed);
	}

    /**
	 * Check if a field for the given attribute name exists in this context.
	 * 
	 * @param attributeName
	 *        name of an attribute
	 * @param object
	 *        an {@link TLObject}
	 */
	public boolean hasField(String attributeName, TLObject object) throws NoSuchAttributeException {
		String attributeID = getAttributeID(object, attributeName);
		if (attributeID != null) {
			return this.hasMember(attributeID);
		} else {
            return false;
        }
    }
    
    /**
     * Check if a field for the given attribute name exists in this context.
     * 
     * @param attributeName
     *      name of an attribute
     * @param meta
     *      an {@link TLClass}
     */
    public boolean hasField(String attributeName, TLClass meta) throws NoSuchAttributeException {
		String attributeID = getAttributeID(meta, attributeName);
		if (attributeID != null) {
			return this.hasMember(attributeID);
		} else {
			return false;
		}
    }
    
	/**
	 * Looks up the {@link FormMember} that holds input for the given {@link AttributeUpdate}.
	 */
	public FormField getField(AttributeUpdate update) {
		FormMember member = getMember(update);
		if (member instanceof FormField) {
			return (FormField) member;
		}
		return null;
	}

	/**
	 * Looks up the {@link FormMember} that holds input for the given {@link AttributeUpdate}.
	 */
	public FormMember getMember(AttributeUpdate update) {
		return update.getField();
	}

    /**
	 * Variant of {@link #getField(String, TLObject)} if the object is <code>null</code> (to be
	 * created).
	 *
	 * @param attributeName
	 *        The name of the attribute to get the field for.
	 * @param type
	 *        The type to identify the field.
	 * @return The requested {@link FormField}.
	 */
	public FormField getField(String attributeName, TLClass type) throws NoSuchElementException {
		TLStructuredTypePart attribute = type.getPart(attributeName);
		if (attribute == null) {
			throw new NoSuchElementException("No such attribute '" + attributeName + "' in '" + type + "'.");
		}
		return getField((TLObject) null, attribute);
    }

    /**
	 * @param attributeName
	 *        The name of the attribute to get the field for.
	 * @param object
	 *        The object to identify the field.
	 * @return The requested {@link FormField}.
	 *
	 * @see FormContainer#getFirstMemberRecursively(String)
	 */
	public FormMember getFirstMemberRecursively(String attributeName, TLObject object) throws NoSuchAttributeException {
		return this.getFirstMemberRecursively(attributeName, object, object.tType());
    }

	public FormMember getFirstMemberRecursively(String aKey, TLObject anAttr, TLStructuredType type)
			throws NoSuchAttributeException {
		TLStructuredTypePart attribute = type.getPart(aKey);
		if (attribute == null) {
			Logger.info("No such attribute '" + aKey + "' in '" + type + "'.", this);
			return null;
		}
		return getFirstMemberRecursively(getAttributeID(anAttr, attribute));
    }

    /**
     * @param    aKey     The name of the meta attribute to get the constraint for.
     * @param    anAttr   The attributed to identify the constraint.
     * @return   The two element sized constraints.
     */
	public FormContainer getRangeContainer(String aKey, TLObject anAttr) throws NoSuchAttributeException {
    	return (FormContainer) getField(aKey, anAttr);
    }

    /**
	 * Get the AttributeUpdate/FormConstraint(s) Map
	 *
	 * @return the AttributeUpdate/FormConstraint(s) Map
	 */
	protected Map getAttributeConstraintMap() {
		TreeMap result = new TreeMap(); // To sort the attributes according to attribute sort order

		for (AttributeUpdate update : _updateContainer.getAllUpdates()) {
			String name = MetaAttributeGUIHelper.getAttributeID(update.getAttribute(), update.getObject());
			if (hasMember(name)) {
				result.put(update, getMember(name));
			}
		}

		return result;
	}

	/**
	 * Get all FormConstraints for updates
	 *
	 * @return all FormConstraints for updates
	 */
	public Collection getAllUpdatesWithFormConstraints() {
	    return this.getAttributeConstraintMap().keySet();
	}

	public FormMember addFormConstraintForUpdate(AttributeUpdate update) {
		FormMember theMember = this.createFormMemberForUpdate(update);
		if (theMember != null) {
            this.addMember(theMember);
		}
		return theMember;
	}

	/**
	 * Allocates the {@link FormMember} displaying and editing the model value represented by the
	 * given {@link AttributeUpdate}.
	 * 
	 * <p>
	 * Note: The resulting member is <b>not automatically added</b> to this {@link FormContext}. Use
	 * {@link #addFormConstraintForUpdate(AttributeUpdate)} to automatically add the new field
	 * directly to this {@link FormContext}. Alternatively, you can add the resulting field to a
	 * custom {@link FormContainer} created in this {@link FormContext}, or use
	 * {@link #addFormContainerForOverlay(TLFormObject)} to allocate one for an edited or created
	 * object and add the resulting field to this one.
	 * </p>
	 *
	 * @param update
	 *        The model value to edit.
	 * @return The field model to display at the UI, or <code>null</code>, if no field can be
	 *         allocated to display the given model value.
	 * 
	 * @see #addFormConstraintForUpdate(AttributeUpdate)
	 */
	public FormMember createFormMemberForUpdate(AttributeUpdate update) {
		// Check for null update
		if (update == null) {
			return null;
		}

		return AttributeFormFactory.getInstance().toFormMember(update, _updateContainer);
	}

	/**
	 * Creates a new {@link FormContainer} for the given {@link TLFormObject} and adds it directly
	 * to this {@link FormContext}.
	 * 
	 * @see #createFormContainerForOverlay(TLFormObject)
	 */
	public FormContainer addFormContainerForOverlay(TLFormObject overlay) {
		FormContainer result = createFormContainerForOverlay(overlay);
		addMember(result);
		return result;
	}

	/**
	 * Creates a {@link FormContainer} for displaying fields for the given overlay object.
	 * 
	 * <p>
	 * Note: The resulting {@link FormContainer} is not automatically added to this
	 * {@link FormContext}. Alternatively, {@link #addFormContainerForOverlay(TLFormObject)} can be
	 * used.
	 * </p>
	 *
	 * <p>
	 * Note: The resulting {@link FormContainer} is not automatically populated with fields for the
	 * given overlay, use {@link #createFormMemberForUpdate(AttributeUpdate)}.
	 * </p>
	 *
	 * @param overlay
	 *        The {@link TLFormObject} to be displayed.
	 * @return A new {@link FormContainer} for the given {@link TLFormObject}.
	 */
	public FormContainer createFormContainerForOverlay(TLFormObject overlay) {
		FormGroup result = new FormGroup(MetaAttributeGUIHelper.getOverlayId(overlay), getResources());
		overlay.initContainer(result);
		result.set(OVERLAY, overlay);
		return result;
	}

	/**
	 * Finds the {@link FormContainer} associated with the given {@link TLFormObject}.
	 * 
	 * @see #createFormContainerForOverlay(TLFormObject)
	 */
	public FormContainer getFormContainerForOverlay(TLFormObject overlay) {
		return overlay.getFormContainer();
	}

	/**
	 * The {@link TLFormObject} the given {@link FormContainer} was
	 * {@link #createFormContainerForOverlay(TLFormObject) created} for.
	 * 
	 * @see #createFormContainerForOverlay(TLFormObject)
	 */
	public TLFormObject getOverlay(FormContainer container) {
		return container.get(OVERLAY);
	}

	@Override
	public boolean checkAll() {
		if (!locallyCheckAll()) {
			return false;
		}

		_updateContainer.update();

		return _updateContainer.check();
	}

	/**
	 * Checks that all inputs in the fields are valid.
	 * 
	 * @return Whether all UI constraints are fulfilled.
	 */
	protected final boolean locallyCheckAll() {
		return super.checkAll();
	}

}