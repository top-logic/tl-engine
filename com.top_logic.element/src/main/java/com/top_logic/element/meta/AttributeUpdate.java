/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.dob.data.DOList;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AnnotationContainer;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Utils;

/**
 * Container for a pending edit/input operation for a {@link TLStructuredTypePart} of an
 * {@link TLObject}.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class AttributeUpdate extends SimpleEditContext implements Comparable<AttributeUpdate> {

	/**
	 * Algorithm that persists the value of an {@link AttributeUpdate}.
	 */
	public interface StoreAlgorithm {

		/**
		 * Persists the value of the given {@link AttributeUpdate}.
		 * 
		 * @param update
		 *        The container for the value to persist.
		 */
		void store(AttributeUpdate update);

	}

	/**
	 * {@link StoreAlgorithm} simply transferring the {@link AttributeUpdate} to the underlying
	 * {@link StorageImplementation} of the edited attribute.
	 */
	public static class DefaultStorageAlgorithm implements StoreAlgorithm {
		@Override
		public void store(AttributeUpdate update) {
			TLObject object = update.getObject();
			TLStructuredTypePart attribute = update.getAttribute();
			AttributeOperations.checkAlive(object, attribute);

			StorageImplementation storage = AttributeOperations.getStorageImplementation(attribute);
			storage.update(update);
		}
	}

	/**
	 * Checks that the value of an {@link AttributeUpdate} is correct.
	 */
	public interface UpdateCheck {

		/**
		 * Checks the value of the given {@link AttributeUpdate}.
		 * 
		 * @param update
		 *        The container for the value to check.
		 */
		void checkUpdate(AttributeUpdate update) throws I18NRuntimeException;

	}

	/** Default {@link StoreAlgorithm} for almost all {@link AttributeUpdate}. */
	public static final StoreAlgorithm DEFAULT_STORE_ALGORITHM = new DefaultStorageAlgorithm();

	/** Default {@link UpdateCheck} for almost all {@link AttributeUpdate}. */
	public static final UpdateCheck DEFAULT_UPDATE_CHECK = new UpdateCheck() {
		@Override
		public void checkUpdate(AttributeUpdate update) {
			AttributeOperations.checkUpdate(update);
		}
	};

	/**
	 * Classification of {@link AttributeUpdate}s.
	 * 
	 * @see AttributeUpdate#getUpdateType()
	 */
	public enum UpdateType {
		/** Type: set a simple value. */
		TYPE_SET_SIMPLE(false),

		/** Type: set a collection value. */
		TYPE_SET_COLLECTION(false),

		/** Type: a collection of search values. */
		TYPE_SEARCH_COLLECTION(true),
		/** Type:search range (from and to value). */

		TYPE_SEARCH_RANGE(true),

		/** Type: simple search value. */
		TYPE_SEARCH_SIMPLE(true);

		private boolean _search;

		/**
		 * Creates a {@link UpdateType}.
		 */
		UpdateType(boolean search) {
			_search = search;
		}

		/**
		 * Whether this is an {@link AttributeUpdate} for a search UI.
		 */
		boolean isSearch() {
			return _search;
		}
	}

	private TLFormObject _overlay;

	private UpdateType _updateType;

	private Object _value;

	private Object _toValue;

	private boolean _disabled;

	private boolean _mandatory;

	private boolean _touched;

	private boolean _changed;

	private boolean _forCreate;

	private AnnotationLookup _localAnnotations = AnnotationContainer.EMPTY;

	private StoreAlgorithm _storeAlgorithm = DEFAULT_STORE_ALGORITHM;

	private UpdateCheck _updateCheck = DEFAULT_UPDATE_CHECK;

	private FormMember _field;

	/**
	 * Creates a {@link AttributeUpdate}.
	 *
	 * @param overlay
	 *        The transient respresentative of the edited or created object, this update belongs to.
	 * @param attribute
	 *        the attribute. Must not be <code>null</code>.
	 * 
	 * @see AttributeFormContext#editObject(TLObject)
	 */
	@FrameworkInternal
	public AttributeUpdate(TLFormObject overlay, TLStructuredTypePart attribute) {
		super(attribute);
		assert Objects.nonNull(overlay);

		_overlay = overlay;
	}

	@Override
	public TLFormObject getOverlay() {
		return _overlay;
	}

	@Override
	public TLStructuredType getType() {
		return _overlay.getType();
	}

	/**
	 * The {@link FormMember} that was built for this {@link AttributeUpdate}, or <code>null</code>
	 * if an field was built yet.
	 */
	public FormMember getField() {
		return _field;
	}

	/**
	 * Creates an identifier for a form field to be displayed in the user interface.
	 */
	public String createFieldName() {
		if (isUpdateForCreate()) {
			return MetaAttributeGUIHelper.getAttributeIDCreate(getAttribute(), getDomain());
		} else {
			return MetaAttributeGUIHelper.getAttributeID(getAttribute(), getObject());
		}
	}

	/**
	 * Remembers the {@link FormMember} that was built to display this {@link AttributeUpdate}.
	 */
	@FrameworkInternal
	public void initField(FormMember field) {
		assert _field == null : "Must not create multiple fields for the same update.";
		_field = field;
	}

    @Override
	public String toString() {
		TLObject object = getObject();
		String attributeName = TLModelUtil.qualifiedName(getAttribute());
		if (object == null) {
			return attributeName;
		} else {
			return attributeName + "' of '" + object;
		}
    }

	/**
	 * @see #isUpdateForCreate()
	 */
	protected void initCreate(boolean forCreate) {
		_forCreate = forCreate;
	}

	/**
	 * Whether this update was build to create a new {@link TLObject}.
	 */
	public boolean isUpdateForCreate() {
		return _forCreate;
	}

	@Override
	public boolean isDisabled() {
		return _disabled;
	}

	/**
	 * Set the disabled flag
	 *
	 * @param disabled
	 *        the disabled flag
	 */
	public void setDisabled(boolean disabled) {
		_disabled = disabled;
		_mandatory = _mandatory && !_disabled;
	}

	@Override
	public boolean isMandatory() {
		return _mandatory;
	}

	/**
	 * Set the mandatory flag
	 *
	 * @param mandatory
	 *        the mandatory flag
	 */
	public void setMandatory(boolean mandatory) {
		_mandatory = mandatory && !_disabled;
	}

	@Override
	public <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
		T localAnnotation = _localAnnotations.getAnnotation(annotationType);
		if (localAnnotation != null) {
			return localAnnotation;
		}
		return super.getAnnotation(annotationType);
	}

	/**
	 * Sets overrides to the annotations of the {@link #getAttribute()}.
	 */
	public void setLocalAnnotations(AnnotationLookup localAnnotations) {
		_localAnnotations = localAnnotations;
	}

	@Override
	public TLObject getObject() {
		return _overlay.getEditedObject();
	}

	/**
	 * Set the update type. Has to be a legal type.
	 *
	 * @param aType
	 *        the type
	 * @throws IllegalArgumentException
	 *         if the type is illegal
	 */
	protected void setType(UpdateType aType) throws IllegalArgumentException {
		_updateType = aType;
	}

	@Override
	public boolean isSearchUpdate () {
		return getUpdateType().isSearch();
	}

	/**
	 * Get the update type
	 *
	 * @return the update type
	 */
	public UpdateType getUpdateType() {
		return _updateType;
	}

	/**
	 * Get the update value for a SimpleMetaAttribute
	 *
	 * @return the value
	 * @throws RuntimeException if the update type
	 * 			does not correspond to this method
	 */
	public Object getSimpleSetUpdate () throws RuntimeException {
		if (UpdateType.TYPE_SET_SIMPLE != getUpdateType()) {
			throw new RuntimeException("Call to getSimpleSetUpdate not allowed for type " + getUpdateType());
		}

		return _value;
	}

	/**
	 * Get the values for a CollectionMetaAttribute
	 *
	 * @return the values
	 * @throws RuntimeException if the update type
	 * 			does not correspond to this method
	 */
	public Collection<?> getCollectionSetUpdate() throws RuntimeException {
		if (UpdateType.TYPE_SET_COLLECTION != getUpdateType()) {
			throw new RuntimeException("Call to getCollectionSetUpdate not allowed for type " + getUpdateType());
		}

		return (Collection<?>) _value;
	}

	/**
	 * Get the search value for an attribute
	 *
	 * @return the value
	 * @throws RuntimeException
	 *         if the update type does not correspond to this method
	 */
	public Object getSimpleSearchUpdate () throws RuntimeException {
		if (UpdateType.TYPE_SEARCH_SIMPLE != getUpdateType()) {
			throw new RuntimeException("Call to getSimpleSearchUpdate not allowed for type " + getUpdateType());
		}

		return _value;
	}

	/**
	 * Get the 'from' value for a range search of an attribute
	 *
	 * @return the value
	 * @throws RuntimeException
	 *         if the update type does not correspond to this method
	 */
	public Object getFromSearchUpdate () throws RuntimeException {
		if (UpdateType.TYPE_SEARCH_RANGE != getUpdateType()) {
			throw new RuntimeException("Call to getFromSearchUpdate not allowed for type " + getUpdateType());
		}

		return _value;
	}

	/**
	 * Get the 'to' value for a range search of an attribute
	 *
	 * @return the value
	 * @throws RuntimeException
	 *         if the update type does not correspond to this method
	 */
	public Object getToSearchUpdate () throws RuntimeException {
		if (UpdateType.TYPE_SEARCH_RANGE != getUpdateType()) {
			throw new RuntimeException("Call to getToSearchUpdate not allowed for type " + getUpdateType());
		}

		return _toValue;
	}

	/**
	 * Get the values for a multiple value search of an attribute
	 *
	 * @return the values
	 * @throws RuntimeException
	 *         if the update type does not correspond to this method
	 */
	public Collection<?> getCollectionSearchUpdate() throws RuntimeException {
		if (UpdateType.TYPE_SEARCH_COLLECTION != getUpdateType()) {
			throw new RuntimeException("Call to getCollectionSearchUpdate not allowed for type " + getUpdateType());
		}

		return (Collection<?>) _value;
	}

	@Override
	public Object getCorrectValues() {
		switch (getUpdateType()) {
			case TYPE_SEARCH_RANGE:
				ArrayList<Object> pair = new ArrayList<>(2);
				pair.add(getFromSearchUpdate());
				pair.add(getToSearchUpdate());
				return pair;
			case TYPE_SEARCH_SIMPLE:
				return getSimpleSearchUpdate();
			case TYPE_SEARCH_COLLECTION:
				return getCollectionSearchUpdate();
			case TYPE_SET_SIMPLE:
				return getSimpleSetUpdate();
			case TYPE_SET_COLLECTION:
				return getCollectionSetUpdate();
			default:
				Logger.warn("Unknown update type: " + getUpdateType(), this);
				return null;
		}
	}

	/**
	 * Set the correct values according to the update type.
	 * 
	 * @param formValue
	 *        The value from the form that was editing the value.
	 * 
	 * @see #setValues(Object, Object) for setting correct value in case type
	 *      {@link UpdateType#TYPE_SEARCH_RANGE}
	 */
	public final void setValue(Object formValue) {
		_value = convertValue(formValue);
	}

	/**
	 * Converts the value from an input field to a value that is forwarded to the model.
	 *
	 * @param formValue
	 *        The value from the input field.
	 * @return The value for the model.
	 */
	public final Object convertValue(Object formValue) {
		switch (getUpdateType()) {
			case TYPE_SEARCH_COLLECTION:
				return toCollection(formValue);
			case TYPE_SET_SIMPLE:
				return formValue;
			case TYPE_SET_COLLECTION:
				return toCollection(formValue);
			default:
				return formValue;
		}
	}

	/**
	 * Sets the search range values.
	 * 
	 * @param fromValue
	 *        The start of the search range for type {@link UpdateType#TYPE_SEARCH_RANGE}.
	 * @param toValue
	 *        The end of the search range for type {@link UpdateType#TYPE_SEARCH_RANGE}.
	 * 
	 * @see #setValue(Object) for setting the value for other types.
	 */
	public void setValues(Object fromValue, Object toValue) {
		setValue(fromValue);
		switch (getUpdateType()) {
			case TYPE_SEARCH_RANGE:
				_toValue = toValue;
				break;
			default:
				// Ignore.
		}
	}
	
	private static Collection<?> toCollection(Object aValue1) {
		if ((aValue1 != null) && !(aValue1 instanceof Collection)) {
			return Collections.singletonList(aValue1);
		} else {
			return (Collection<?>) aValue1;
		}
	}

	/**
	 * Sets the {@link #getCorrectValues() value} of this update to the given value and marks this
	 * update as {@link #isChanged() changed}, when value has been changed.
	 * 
	 * @param newValue
	 *        the new value of this update
	 * 
	 * @see #updateValues(Object, Object) for updating value in case type
	 *      {@link UpdateType#TYPE_SEARCH_RANGE}
	 */
	public void updateValue(Object newValue) {
		updateValues(newValue, null);
	}

	/**
	 * Sets the {@link #getCorrectValues() value} of this update to the given value and marks this
	 * update as {@link #isChanged() changed}, when value has been changed.
	 * 
	 * @param newValue
	 *        the new value of this update
	 * @param newOptionalToValue
	 *        the new to value for type {@link UpdateType#TYPE_SEARCH_RANGE}
	 * 
	 * @see #updateValues(Object, Object) for updating value in case not type
	 *      {@link UpdateType#TYPE_SEARCH_RANGE}
	 */
	public void updateValues(Object newValue, Object newOptionalToValue) {
		Object previousValues = getCorrectValues();
		setValues(newValue, newOptionalToValue);
		if (!Utils.equals(previousValues, getCorrectValues())) {
			_changed = true;
		}
	}
	
	/**
	 * true iff this {@link AttributeUpdate} was updated and the value has changed.
	 */
	public boolean isChanged() {
		return _changed;
	}

	/**
	 * Marks this update as changed.
	 * 
	 * <p>
	 * Only changed values are stored upon save.
	 * </p>
	 */
	public void touch() {
		_changed = true;
	}

    /**
	 * Use attribute for comparison
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
    @Override
	public int compareTo(AttributeUpdate other) {
        // Compare MetaAttributes
		int comp = DisplayAnnotations.PART_ORDER.compare(getAttribute(), (other).getAttribute());
        if (comp != 0) {
            return comp;
        }
        
		String thisDomanin = getDomain();
		String otherDomanin = other.getDomain();
        if (thisDomanin == null) {
        	if (otherDomanin != null) {
        		return -1;
        	}
        } else {
        	if (otherDomanin == null) {
        		return 1;
        	} else {
                int domainComparision = thisDomanin.compareTo(otherDomanin);
                if (domainComparision != 0) {
                	return domainComparision;
                }
        	}
        }

        // Compare Attributes if available
		TLObject theAtt = (other).getObject();
		TLObject thisAtt = getObject();

		TLID id1 = theAtt != null ? KBUtils.getWrappedObjectName(theAtt) : null;
		TLID id2 = thisAtt != null ? KBUtils.getWrappedObjectName(thisAtt) : null;

		return ArrayUtil.compareObjects(id1, id2);
    }

    public boolean isTouched() {
		return (_touched);
    }

    public void setTouched(boolean touch) {
		_touched = touch;
    }

	/**
	 * Additional identifier for {@link AttributeUpdate}s representing search field inputs, when a
	 * search UI is build, where query values are collected for more than on object, e.g. when
	 * searching for properties of depending objects (the name of the project leader of the project
	 * being searched). The value is <code>null</code> for all other cases.
	 */
    public String getDomain() {
		return _overlay.getDomain();
    }

	@Override
	public void setStoreAlgorithm(StoreAlgorithm algorithm) {
		_storeAlgorithm = Objects.requireNonNull(algorithm);
	}

	/**
	 * Forces this update to persist its value.
	 */
	@FrameworkInternal
	public void store() {
		_storeAlgorithm.store(this);
	}

	/**
	 * Sets the method to check whether the value is appropriate for {@link #getAttribute()} and
	 * {@link #getObject()}.
	 */
	public void setUpdateCheck(UpdateCheck check) {
		_updateCheck = Objects.requireNonNull(check);
	}

	/**
	 * Forces this update to check its value.
	 */
	@FrameworkInternal
	public void checkUpdate() throws I18NRuntimeException {
		if (isSearchUpdate()) {
			return;
		}

		if (isDisabled()) {
			return;
		}

		_updateCheck.checkUpdate(this);
	}

	/**
	 * Initializes this {@link AttributeUpdate} for a search mask.
	 * 
	 * @param fromValue
	 *        The initial start of the search range.
	 * @param toValue
	 *        The initial end of the search range.
	 * 
	 * @return This instance of call chaining.
	 */
	public AttributeUpdate searchUpdate(Object fromValue, Object toValue) {
		TLStructuredTypePart attribute = getAttribute();
		if (!AttributeOperations.isCollectionValued(attribute)) {
			if (AttributeOperations.allowsSearchRange(attribute)) {
				setType(UpdateType.TYPE_SEARCH_RANGE);
				setValues(fromValue, toValue);
			}
			else {
				setType(UpdateType.TYPE_SEARCH_SIMPLE);
				setValue(fromValue);
			}
		}
		else { // Collection
			setType(UpdateType.TYPE_SEARCH_COLLECTION);
			setValue(fromValue);
		}

		initSearchVisibility();
		return this;
	}

	void initEdit(Object presetValue) {
		TLStructuredTypePart attribute = getAttribute();
		if (!AttributeOperations.isCollectionValued(attribute)) {
			setType(UpdateType.TYPE_SET_SIMPLE);
			setValue(presetValue);
		} else {
			setType(UpdateType.TYPE_SET_COLLECTION);
		    if (!AttributeUpdateFactory.isStringSetType(attribute) || !AttributeUpdateFactory.isRestricted(attribute)) {
				setValue(presetValue);
			} else {
				Collection<?> collectionValue = (Collection<?>) presetValue;
				if (collectionValue instanceof DOList || collectionValue == null || collectionValue.isEmpty()) {
					setValue(collectionValue);
				} else {
					OptionModel<?> options = AttributeOperations.allOptions(this);
					List<Object> result = new ArrayList<>();
					if (options != null) {
			    		boolean stop = false;
						Iterator<?> optionIt = options.iterator();
						while (!stop && optionIt.hasNext()) {
							Object option = optionIt.next();
							try {
								if (collectionValue.contains(option)) {
									setValue(collectionValue);
									stop = true;
								} else {
									// Check Strings (init)...
									String dapParam =
										(String) AttributeUpdateFactory.getResultLocator(attribute).locateAttributeValue(option);
									if (collectionValue.contains(dapParam)) {
										result.add(option);
									}
								}
							}
					    	catch (Exception ex) {
								StringBuilder message = new StringBuilder();
								message.append("Failed to get DAP parameters in attribute ");
								message.append(attribute.getName());
								Logger.warn(message.toString(), ex, AttributeUpdateFactory.class);
					    	}
						}
	
			    		if (!stop) {
							setValue(result);
			    		}
			    	}
		    	}
		    }
		}
	}

	void initSearchVisibility() {
		setDisabled(false);
		setMandatory(false);
	}

	void initCreateVisibility() {
		setDisabled(derived() || annotatedCreateDiabled());
		setMandatory(defaultMandatory());
	}

	void initDefaultEditVisibility(boolean externalDisabled) {
		setDisabled(externalDisabled || derived() || annotatedEditDisabled());
		setMandatory(defaultMandatory());
	}

	void initCustomEditVisibility(boolean externalDisabled, boolean mandatory) {
		setDisabled(externalDisabled || derived());
		setMandatory(mandatory || defaultMandatory());
	}

	private boolean annotatedCreateDiabled() {
		return !DisplayAnnotations.isEditableInCreate(getAttribute());
	}

	private boolean annotatedEditDisabled() {
		return !DisplayAnnotations.isEditable(getAttribute());
	}

	private boolean derived() {
		return TLModelUtil.isDerived(getAttribute());
	}

	private boolean defaultMandatory() {
		return getAttribute().isMandatory();
	}

	/**
	 * Initializes this {@link AttributeUpdate} for an object being created.
	 * 
	 * @return This instance of call chaining.
	 */
	public AttributeUpdate createUpdate() {
		initCreate(true);
		initCreateVisibility();
		initEdit(null);
		if (!isDerived()) {
			DefaultProvider defaultProvider = DisplayAnnotations.getDefaultProvider(getAttribute());
			if (defaultProvider != null) {
				setValue(defaultProvider.createDefault(getOverlay().tContainer(), getAttribute(), true));
			}
		}
		return this;
	}

	/**
	 * Initializes this {@link AttributeUpdate} for an object being edited.
	 * 
	 * @param externalDisabled
	 *        Whether to explicitly disable the input element.
	 * 
	 * @return This instance of call chaining.
	 */
	public AttributeUpdate editUpdateDefault(boolean externalDisabled) {
		initPersistentValue();
		initDefaultEditVisibility(externalDisabled);
		return this;
	}

	/**
	 * Initializes this {@link AttributeUpdate} for an object being edited.
	 * 
	 * @param disabled
	 *        Whether to explicitly disable the input element.
	 * @param mandatory
	 *        Whether to explicitly set the input element to mandatory (if not disabled).
	 * 
	 * @return This instance of call chaining.
	 */
	public AttributeUpdate editUpdateCustom(boolean disabled, boolean mandatory) {
		initPersistentValue();
		initCustomEditVisibility(disabled, mandatory);
		return this;
	}

	private void initPersistentValue() {
		initEdit(getObject().tValue(getAttribute()));
	}

	/**
	 * Load values from associated form field.
	 */
	void update() {
		if (isDisabled()) {
			return;
		}
	
		FormMember member = getField();
		if (member == null) {
			// No field was created for this update, but the update was added
			// programmatically.
			return;
		}
	
		final TLStructuredTypePart attribute = getAttribute();
		if (isSearchUpdate() && AttributeOperations.allowsSearchRange(attribute)) {
			FormContainer group = (FormContainer) member;
			FormField from = group.getField(AttributeFormFactory.SEARCH_FROM_FIELDNAME);
			FormField to = group.getField(AttributeFormFactory.SEARCH_TO_FIELDNAME);
	
			if (!(from.isValid() && to.isValid())) {
				return;
			}
	
			updateValues(from.getValue(), to.getValue());
		} else if (member instanceof FormField) {
			FormField field = (FormField) member;
	
			if (!field.isValid()) {
				return;
			}
	
			if (!isUpdateRequired()) {
				return;
			}
	
			updateValue(fieldToAttributeValue(field));
		}
	}

	/**
	 * Service method to convert the value of the field to be a potential attribute value.
	 * 
	 * @param field
	 *        The {@link FormField} to get value form. It is expected that the field has a value
	 *        ({@link FormField#hasValue()}).
	 * @return The value that can be used as value for this {@link AttributeUpdate}.
	 */
	@FrameworkInternal
	public final Object fieldToAttributeValue(FormField field) {
		Object fieldValue = field.getValue();
		if (field instanceof StringField) {
			if (StringServices.isEmpty(fieldValue)) {
				// The field value represents "null".
				if (getObject() != null && getAttribute() != null) {
					String storedValue = (String) getObject().tValue(getAttribute());
					if (StringServices.isEmpty(storedValue)) {
						// The current value represents "null".
						// Use the stored value instead of the empty string. This
						// prevents changing from null to "" by typing a value and
						// deleting it afterwards.
						fieldValue = storedValue;
					}
				}
			}
		} else if (field instanceof SelectField) {
			if (!((SelectField)field).isMultiple()) {
				fieldValue = ((SelectField) field).getSingleSelection();
			}
		} else if (field instanceof DataField) {
			if (!((DataField) field).isMultiple()) {
				fieldValue = ((DataField) field).getDataItem();
			}
		}
		return fieldValue;
	}

	/**
	 * Whether field values must be transfered.
	 */
	private boolean isUpdateRequired() {
		/* Update is to create a new object. Therefore the AttributeUpdate must be updated with the
		 * value of the form field. */
		if (isUpdateForCreate()) {
			return true;
		}
		/* Update was changed before, therefore the update must changed again. This is necessary as
		 * the field may be re-changed to its old value, therefore the update must also be
		 * re-changed. (See #10966) */
		if (isChanged()) {
			return true;
		}

		/* AttributeUpdate must only be changed when field is changed. (See #9118) */
		return getField().isChanged();
	}

	/**
	 * @deprecated Use {@link #setDisabled(boolean)}
	 */
	@Deprecated
	public void setIsDisabled(boolean disabled) {
		setDisabled(disabled);
	}

	/**
	 * @deprecated Use {@link #getAttribute()}
	 */
	@Deprecated
	public TLStructuredTypePart getMetaAttribute() {
		return getAttribute();
	}

	/**
	 * @deprecated Use {@link #getObject()}
	 */
	@Deprecated
	public Wrapper getAttributed() {
		return (Wrapper) getObject();
	}

}
