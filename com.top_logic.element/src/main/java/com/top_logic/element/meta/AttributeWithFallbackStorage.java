/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.Step;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.func.Function3;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.layout.meta.TLStructuredTypePartFormBuilder.EditModel;
import com.top_logic.element.layout.meta.TLStructuredTypePartFormBuilder.PartModel;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.fallback.StorageWithFallback;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link StorageImplementation} that creates an overlay of storage attribute with a fallback
 * attribute providing a value, if no explicit value has been set to the storage attribute.
 */
@InApp
public class AttributeWithFallbackStorage extends AbstractStorageBase<AttributeWithFallbackStorage.Config<?>>
		implements StorageWithFallback {

	/**
	 * Configuration options for {@link AttributeWithFallbackStorage}.
	 */
	@TagName("fallback-storage")
	public interface Config<I extends AttributeWithFallbackStorage> extends AbstractStorageBase.Config<I>, ConfigPart {

		/**
		 * @see #getStorageAnnotation()
		 */
		public static final String STORAGE_ANNOTATION = "storage-annotation";

		/**
		 * The {@link TLStorage} annotation that uses this implementation.
		 */
		@Name(STORAGE_ANNOTATION)
		@Hidden
		@Container
		TLStorage getStorageAnnotation();

		/**
		 * The name of the attribute to store explicitly set values.
		 * 
		 * <p>
		 * The attribute must be defined in the same type as the attribute with fallback.
		 * </p>
		 */
		@Options(fun = CompatibleAttributes.class, mapping = LocalPartName.class, args = {
			@Ref(steps = {
				@Step(STORAGE_ANNOTATION),
				@Step(TLAttributeAnnotation.ANNOTATED),
				@Step(value = PartModel.EDITING, type = PartModel.class) }),
			@Ref(steps = {
				@Step(STORAGE_ANNOTATION),
				@Step(TLAttributeAnnotation.ANNOTATED),
				@Step(value = PartModel.EDIT_MODEL, type = PartModel.class),
				@Step(value = EditModel.CONTEXT_TYPE, type = EditModel.class) }),
			@Ref(steps = {
				@Step(STORAGE_ANNOTATION),
				@Step(TLAttributeAnnotation.ANNOTATED),
				@Step(value = PartModel.RESOLVED_TYPE, type = PartModel.class) }),
		})
		String getStorageAttribute();

		/**
		 * The attribute to take fallback values from, if no value was explicitly set.
		 * 
		 * <p>
		 * The attribute must be defined in the same type as the attribute with fallback.
		 * </p>
		 */
		@Options(fun = CompatibleAttributes.class, mapping = LocalPartName.class, args = {
			@Ref(steps = {
				@Step(STORAGE_ANNOTATION),
				@Step(TLAttributeAnnotation.ANNOTATED),
				@Step(value = PartModel.EDITING, type = PartModel.class) }),
			@Ref(steps = {
				@Step(STORAGE_ANNOTATION),
				@Step(TLAttributeAnnotation.ANNOTATED),
				@Step(value = PartModel.EDIT_MODEL, type = PartModel.class),
				@Step(value = EditModel.CONTEXT_TYPE, type = EditModel.class) }),
			@Ref(steps = {
				@Step(STORAGE_ANNOTATION),
				@Step(TLAttributeAnnotation.ANNOTATED),
				@Step(value = PartModel.RESOLVED_TYPE, type = PartModel.class) }),
		})
		String getFallbackAttribute();

		/**
		 * CSS class to set on values that have an explicit value assigned.
		 * 
		 * <p>
		 * There are a number of pre-defined CSS classes for highlighting:
		 * </p>
		 * 
		 * <ul>
		 * <li><code>tl-info</code></li>
		 * <li><code>tl-success</code></li>
		 * <li><code>tl-warning</code></li>
		 * <li><code>tl-danger</code></li>
		 * <li><code>tl-debug</code></li>
		 * <li><code>tl-accent-1</code></li>
		 * <li><code>tl-accent-2</code></li>
		 * <li><code>tl-accent-3</code></li>
		 * </ul>
		 * 
		 * <p>
		 * All these classes can be combined with the class <code>tl-lighter</code> to make the
		 * highlighting less prominent.
		 * </p>
		 */
		@Nullable
		@StringDefault("tl-info")
		String getCssExplicit();

		/**
		 * CSS class to set on locations where the fallback value is used.
		 * 
		 * <p>
		 * There are a number of pre-defined CSS classes for highlighting:
		 * </p>
		 * 
		 * <ul>
		 * <li><code>tl-info</code></li>
		 * <li><code>tl-success</code></li>
		 * <li><code>tl-warning</code></li>
		 * <li><code>tl-danger</code></li>
		 * <li><code>tl-debug</code></li>
		 * <li><code>tl-accent-1</code></li>
		 * <li><code>tl-accent-2</code></li>
		 * <li><code>tl-accent-3</code></li>
		 * </ul>
		 * 
		 * <p>
		 * All these classes can be combined with the class <code>tl-lighter</code> to make the
		 * highlighting less prominent.
		 * </p>
		 */
		@Nullable
		@StringDefault("tl-debug")
		String getCssFallback();

		/**
		 * {@link OptionMapping} of a {@link TLTypePart} to its name.
		 */
		class LocalPartName implements OptionMapping {
			@Override
			public Object toSelection(Object option) {
				if (option instanceof TLStructuredTypePart part) {
					return part.getName();
				}
				return null;
			}
		}

		/**
		 * Options for {@link Config#getStorageAttribute()} and
		 * {@link Config#getFallbackAttribute()}.
		 */
		class CompatibleAttributes extends
				Function3<List<? extends TLStructuredTypePart>, TLStructuredTypePart, TLStructuredType, TLType> {

			@Override
			public List<? extends TLStructuredTypePart> apply(TLStructuredTypePart self, TLStructuredType contextType,
					TLType attributeType) {
				if (contextType == null || attributeType == null) {
					return Collections.emptyList();
				}

				return contextType.getAllParts().stream()
					.filter(p -> p != self)
					.filter(p -> p.getType() == attributeType)
					.filter(p -> p.isMultiple() == self.isMultiple())
					.toList();
			}
		}
	}

	private TLStructuredTypePart _storageAttr;

	private TLStructuredTypePart _fallbackAttr;

	private final String _cssExplicit;

	private final String _cssFallback;

	/**
	 * Creates a {@link AttributeWithFallbackStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeWithFallbackStorage(InstantiationContext context, Config<?> config) {
		super(context, config);

		_cssExplicit = config.getCssExplicit();
		_cssFallback = config.getCssFallback();
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);

		_storageAttr = attribute.getOwner().getPart(getConfig().getStorageAttribute()).getDefinition();
		_fallbackAttr = attribute.getOwner().getPart(getConfig().getFallbackAttribute()).getDefinition();

		assert _storageAttr != attribute.getDefinition() : "A fallback attribute '"
			+ TLModelUtil.qualifiedName(attribute) + "' cannot be defined with itself as storage.";
	}

	/**
	 * The attribute storing explicitly set values.
	 */
	public TLStructuredTypePart getStorageAttr() {
		return _storageAttr;
	}

	/**
	 * The attribute that provides fallback values if no value has been explicitly set.
	 */
	public TLStructuredTypePart getFallbackAttr() {
		return _fallbackAttr;
	}

	@Override
	public String getCssExplicit() {
		return _cssExplicit;
	}

	@Override
	public String getCssFallback() {
		return _cssFallback;
	}

	@Override
	public void checkUpdate(AttributeUpdate update) throws TopLogicException {
		storage().checkUpdate(update);
	}

	@Override
	public void initUpdate(TLObject object, TLStructuredTypePart attribute, AttributeUpdate update) {
		update.setValue(object.tValue(_storageAttr));
	}

	@Override
	public void update(AttributeUpdate update) throws AttributeException {
		if (update.isChanged()) {
			storage().setAttributeValue(update.getObject(), _storageAttr, update.getEditedValue());
		}
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) {
		Object explicitValue = getExplicitValue(object, attribute);
		if (!Utils.isEmpty(explicitValue)) {
			return explicitValue;
		}

		return getFallbackValue(object, attribute);
	}

	@Override
	public Object getFallbackValue(TLObject object, TLStructuredTypePart attribute) {
		return object.tValue(_fallbackAttr);
	}

	@Override
	public Object getExplicitValue(TLObject object, TLStructuredTypePart attribute) {
		return object.tValue(_storageAttr);
	}

	@Override
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object newEntry)
			throws NoSuchAttributeException, IllegalArgumentException {
		storage().addAttributeValue(object, attribute, newEntry);
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object oldEntry)
			throws NoSuchAttributeException {
		storage().removeAttributeValue(object, attribute, oldEntry);
	}

	@Override
	protected void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object newValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		storage().setAttributeValue(object, attribute, newValue);
	}

	private StorageImplementation storage() {
		return AttributeOperations.getStorageImplementation(_storageAttr);
	}

}
