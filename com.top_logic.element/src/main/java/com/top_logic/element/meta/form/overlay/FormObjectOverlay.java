/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.overlay;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;
import static com.top_logic.model.util.TLModelUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.ChangeAware;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.Media;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TransientObject;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.util.TLModelUtil;

/**
 * Base class for transient {@link TLObject} backed up by values from a {@link FormContext form}
 * currently being edited.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public abstract class FormObjectOverlay extends TransientObject implements TLFormObject {

	private final AttributeUpdateContainer _scope;

	private final TLStructuredType _type;

	private Map<TLStructuredTypePart, AttributeUpdate> _updates = new HashMap<>();

	private Map<TLStructuredTypePart, Consumer<AttributeUpdate>> _updateFutures;

	private FormContainer _formContainer;

	private String _id;

	/**
	 * Creates a {@link FormObjectOverlay}.
	 * 
	 * @param scope
	 *        The {@link AttributeUpdateContainer} with all current edit operations.
	 * @param type
	 *        The type of the transient object.
	 */
	public FormObjectOverlay(AttributeUpdateContainer scope, TLStructuredType type, String id) {
		_scope = scope;
		_type = type;
		_id = id;
	}

	@Override
	public AttributeUpdateContainer getScope() {
		return _scope;
	}

	@Override
	public TLStructuredType getType() {
		return _type;
	}

	@Override
	public Media getOutputMedia() {
		return _scope.getOutputMedia();
	}

	@Override
	public FormContainer getFormContainer() {
		return _formContainer == null ? _scope.getFormContext() : _formContainer;
	}

	@Override
	public void initContainer(FormContainer formContainer) {
		if (_formContainer != null) {
			throw new IllegalStateException("Overlay has already a form container.");
		}
		_formContainer = formContainer;
	}

	@Override
	public final Iterable<AttributeUpdate> getUpdates() {
		return _updates.values();
	}

	@Override
	public final AttributeUpdate getUpdate(TLStructuredTypePart part) {
		return _updates.get(part);
	}

	@Override
	public void withUpdate(TLStructuredTypePart attribute, Consumer<AttributeUpdate> callback) {
		AttributeUpdate update = getUpdate(attribute);
		if (update == null) {
			Map<TLStructuredTypePart, Consumer<AttributeUpdate>> futures = mkUpdateFutures();
			Consumer<AttributeUpdate> clash = futures.put(attribute, callback);
			if (clash != null) {
				futures.put(attribute, clash.andThen(callback));
			}
		} else {
			callback.accept(update);
		}
	}

	private Map<TLStructuredTypePart, Consumer<AttributeUpdate>> mkUpdateFutures() {
		if (_updateFutures == null) {
			_updateFutures = new HashMap<>();
		}
		return _updateFutures;
	}

	/**
	 * Adds the given {@link AttributeUpdate} to this object.
	 */
	private final void addUpdate(AttributeUpdate update) {
		TLStructuredTypePart attribute = update.getAttribute();
		_updates.put(attribute, update);
		if (_updateFutures != null) {
			Consumer<AttributeUpdate> future = _updateFutures.remove(attribute);
			if (future != null) {
				future.accept(update);
			}
		}
	}

	@Override
	public final AttributeUpdate removeUpdate(TLStructuredTypePart attribute) {
		return _updates.remove(attribute);
	}

	@Override
	public abstract TLObject getEditedObject();

	@Override
	public String getFormId() {
		return _id;
	}

	@Override
	public abstract String getDomain();

	@Override
	public TLStructuredType tType() {
		return _type;
	}

	@Override
	public Object tValue(TLStructuredTypePart part) {
		if (part.isDerived() && (part.getModelKind() != ModelKind.REFERENCE || !((TLReference) part).isBackwards())) {
			if (part.getName().equals(PersistentObject.T_TYPE_ATTR)) {
				return tType();
			} else {
				return part.getStorageImplementation().getAttributeValue(this, part);
			}
		}

		AttributeUpdate update = getUpdate(part);
		if (update == null) {
			return defaultValue(part);
		}

		FormMember member = update.getField();
		if (member == null || !(member instanceof FormField)) {
			return update.getCorrectValues();
		}

		FormField field = (FormField) member;
		if (!field.hasValue()) {
			return null;
		}

		return fromUIToDBValue(update, part, field);
	}

	/**
	 * Normalize value in a way as it has been retrieved from the persistency layer.
	 * <p>
	 * Without this code, attributes of type tl.core:Integer that have been edited in the UI return
	 * de-facto values of type java.lang.Long. This is a problem, if those values are used in
	 * set-contains comparisons.
	 * </p>
	 */
	private Object fromUIToDBValue(AttributeUpdate update, TLStructuredTypePart part, FormField field) {
		Object uiValue = update.convertValue(update.fieldToAttributeValue(field));
		TLType type = part.getType();
		if (type.getModelKind() == ModelKind.DATATYPE) {
			StorageMapping<?> storageMapping = ((TLPrimitive) type).getStorageMapping();
			if (part.isMultiple()) {
				return toDBValues(part, storageMapping, uiValue);
			} else {
				return toDBValue(storageMapping, uiValue);
			}
		}
		return uiValue;
	}

	/** Create a {@link Collection} for the values of the given {@link TLStructuredTypePart}. */
	protected Collection<Object> createCollection(TLStructuredTypePart attribute) {
		if (!attribute.isMultiple()) {
			throw new IllegalArgumentException("Attribute is not multiple: " + qualifiedName(attribute));
		}
		if (attribute.isBag()) {
			return new ArrayList<>();
		} else {
			// Values must not appear multiple times, cannot use List.
			if (attribute.isOrdered()) {
				return new LinkedHashSet<>();
			} else {
				return new HashSet<>();
			}
		}
	}

	private Object toDBValues(TLStructuredTypePart part, StorageMapping<?> storageMapping, Object uiValues) {
		Collection<Object> dbValues = createCollection(part);
		for (Object uiValue : nonNull((Collection<?>) uiValues)) {
			Object dbValue = toDBValue(storageMapping, uiValue);
			dbValues.add(dbValue);
		}
		return dbValues;
	}

	private Object toDBValue(StorageMapping<?> storageMapping, Object uiValue) {
		return storageMapping.getBusinessObject(storageMapping.getStorageObject(uiValue));
	}

	@Override
	public Set<? extends TLObject> tReferers(TLReference ref) {
		TLStructuredType owningType = ref.getDefinition().getOwner();
		
		Set<TLObject> result = new HashSet<>();
		for (TLFormObject object : getScope().getAllOverlays()) {
			if (TLModelUtil.isCompatibleType(owningType, object.tType())) {
				Object value = object.tValue(ref);
				if (value instanceof Collection<?>) {
					if (((Collection<?>) value).contains(this)) {
						result.add(object);
					}
				} else if (value == this) {
					result.add(object);
				}
			}
		}

		// For edited objects, consider references from the outside of the current edit scope.
		TLObject editedObject = getEditedObject();
		if (editedObject != null) {
			for (TLObject persistentObject : editedObject.tReferers(ref)) {
				TLFormObject overlay = getScope().getOverlay(persistentObject, null);
				if (overlay == null) {
					result.add(persistentObject);
				} else {
					result.add(overlay);
				}
			}
		}

		return result;
	}

	/**
	 * Computes the value to use, if the form has no field for the given
	 * {@link TLStructuredTypePart} of this {@link TLObject}.
	 */
	protected abstract Object defaultValue(TLStructuredTypePart part);

	@Override
	public void tUpdate(TLStructuredTypePart part, Object value) {
		AttributeUpdate update = getUpdate(part);
		if (update == null) {
			update = newCreateUpdate(part);
		}

		update.setValue(value);

		FormMember member = update.getField();
		if (member instanceof FormField) {
			FormField field = (FormField) member;
			AttributeFormFactory.initFieldValue(update, field);
		}
		
		update.touch();
	}

	/**
	 * Stores all updates to attributes of this overlay object.
	 * 
	 * @param updateContainer
	 *        All updates of the current transaction.
	 */
	public void store(AttributeUpdateContainer updateContainer) {
		Iterable<AttributeUpdate> updates = getUpdates();
		for (AttributeUpdate update : updates) {
			update.checkUpdate();
		}
		for (AttributeUpdate update : updates) {
			if (this instanceof ChangeAware) {
				TLStructuredTypePart attribute = update.getAttribute();
				((ChangeAware) this).notifyPreChange(attribute.getName(), update.getCorrectValues());
			}
		
			update.store();
		}
		if (this instanceof ChangeAware) {
			((ChangeAware) this).updateValues(updateContainer);
		}
	}

	@Override
	public AttributeUpdate newEditUpdateCustom(TLStructuredTypePart attribute, boolean disabled, boolean mandatory) {
		AttributeUpdate result = newUpdate(attribute);
		if (result == null) {
			return null;
		}
		return result.editUpdateCustom(disabled, mandatory);
	}

	@Override
	public AttributeUpdate newEditUpdateCustom(TLStructuredTypePart attribute, boolean disabled, boolean mandatory,
			AnnotationLookup annotations) {
		AttributeUpdate update = newEditUpdateCustom(attribute, disabled, mandatory);
		if (update == null) {
			return null;
		}
		update.setLocalAnnotations(annotations);
		return update;
	}

	@Override
	public AttributeUpdate newEditUpdateDefault(TLStructuredTypePart attribute, boolean disabled) {
		AttributeUpdate result = newUpdate(attribute);
		if (result == null) {
			return null;
		}
		return result.editUpdateDefault(disabled);
	}

	@Override
	public AttributeUpdate newCreateUpdate(TLStructuredTypePart attribute) {
		AttributeUpdate result = newUpdate(attribute);
		if (result == null) {
			return null;
		}
		return result.createUpdate();
	}

	@Override
	public AttributeUpdate newSearchUpdate(TLStructuredTypePart attribute, Object presetValue, Object presetValue2) {
		AttributeUpdate result = newUpdate(attribute);
		if (result == null) {
			return null;
		}
		return result.searchUpdate(presetValue, presetValue2);
	}

	private AttributeUpdate newUpdate(TLStructuredTypePart attribute) {
		TLStructuredTypePart specializedAttribute = AttributeOperations.getAttributeOverride(this, attribute);
		if (specializedAttribute == null) {
			return null;
		}

		AttributeUpdate existing = _updates.get(specializedAttribute);
		if (existing != null) {
			// May happen due to legacy code forcing an update creation while the update has already
			// been created before.
			return existing;
		}

		AttributeUpdate result = new AttributeUpdate(this, specializedAttribute);
		addUpdate(result);
		return result;
	}

}
