/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.Objects;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.AttributeUpdate.StoreAlgorithm;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.DynamicLabel;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link EditContext} based on a given {@link TLStructuredTypePart} and edit object.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SimpleEditContext implements EditContext {

	/**
	 * Create a {@link SimpleEditContext} for the given attribute without an object to edit.
	 */
	public static SimpleEditContext createContext(TLStructuredTypePart attribute) {
		return new SimpleEditContext(attribute);
	}

	/**
	 * Create a {@link SimpleEditContext} for the given attribute and the given edited object.
	 */
	public static SimpleEditContext createContext(TLObject editObject, TLStructuredTypePart attribute) {
		if (editObject == null) {
			return createContext(attribute);
		}
		return new SimpleEditContext(attribute) {

			@Override
			public TLObject getObject() {
				return editObject;
			}

		};
	}

	private TLStructuredTypePart _targetAttribute;

	private boolean _inTableContext = false;

	/**
	 * Creates a {@link SimpleEditContext}.
	 *
	 * @param attribute
	 *        The edited {@link TLStructuredTypePart}.
	 */
	protected SimpleEditContext(TLStructuredTypePart attribute) {
		assert Objects.nonNull(attribute);
		_targetAttribute = attribute;
	}

	/**
	 * The attribute whose value is edited.
	 */
	public final TLStructuredTypePart getAttribute() {
		return _targetAttribute;
	}

	@Override
	public ResKey getLabelKey() {
		return getRelevantKey(false);
	}

	@Override
	public ResKey getTableTitleKey() {
		return getRelevantKey(true);
	}

	/**
	 * Resolves the appropriate {@link ResKey} based on context.
	 * 
	 * <p>
	 * Determines the label resource key for an attribute, with support for dynamic labels through
	 * annotations. For table titles, uses the table title key; otherwise uses the standard I18N
	 * key.
	 * </p>
	 * 
	 * @param isTableTitle
	 *        Whether to return a table title key (<code>true</code>) or a standard label key
	 *        (<code>false</code>)
	 * 
	 * @return The resource key for the attribute, potentially modified by {@link DynamicLabel}
	 *         annotations if an object context exists
	 */
	private ResKey getRelevantKey(boolean isTableTitle) {
		TLStructuredTypePart attribute = getAttribute();
		ResKey defaultLabelKey =
			isTableTitle ? TLModelI18N.getTableTitleKey(attribute) : TLModelI18N.getI18NKey(attribute);

		if (getObject() != null) {
			DynamicLabel annotation = getAnnotation(DynamicLabel.class);
			if (annotation != null) {
				Object dynamicLabel = annotation.getLabel().impl().apply(getObject(), defaultLabelKey, attribute);
				if (dynamicLabel instanceof ResKey) {
					return (ResKey) dynamicLabel;
				}
				String labelString;
				if (dynamicLabel instanceof String) {
					labelString = (String) dynamicLabel;
				} else {
					labelString = MetaLabelProvider.INSTANCE.getLabel(dynamicLabel);
				}
				return ResKey.text(labelString);
			}
		}
		return defaultLabelKey;
	}

	@Override
	public TLType getValueType() {
		return getAttribute().getType();
	}

	@Override
	public <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
		return TLAnnotations.getAnnotation(getAttribute(), annotationType);
	}

	@Override
	public boolean isMultiple() {
		return getAttribute().isMultiple();
	}

	@Override
	public boolean isOrdered() {
		return getAttribute().isOrdered();
	}

	@Override
	public boolean isBag() {
		return getAttribute().isBag();
	}

	@Override
	public boolean isDerived() {
		return AttributeOperations.isReadOnly(getAttribute());
	}

	@Override
	public boolean isComposition() {
		return AttributeOperations.isComposition(getAttribute());
	}

	@Override
	public boolean inTableContext() {
		return _inTableContext;
	}

	/** @see #inTableContext() */
	public void setInTableContext(boolean inTableContext) {
		_inTableContext = inTableContext;
	}

	@Override
	public Generator getOptions() {
		return AttributeOperations.getOptions(getAttribute());
	}

	@Override
	public AttributedValueFilter getFilter() {
		return AttributeOperations.getConstraint(getAttribute());
	}

	@Override
	public String getConfigKey(FormMember field) {
		return TLModelUtil.qualifiedName(getAttribute());
	}

	@Override
	public ResKey getDescriptionKey() {
		if (getObject() == null) {
			return I18NConstants.EDIT_LOCATION__ATTR.fill(getAttribute());
		}
		return I18NConstants.EDIT_LOCATION__ATTR_OBJ.fill(getAttribute(), getObject());
	}

	@Override
	public TLStructuredType getType() {
		if (getObject() == null) {
			return null;
		}
		return getObject().tType();
	}

	@Override
	public TLObject getObject() {
		return null;
	}

	@Override
	public TLFormObject getOverlay() {
		return null;
	}

	@Override
	public Object getCorrectValues() {
		return null;
	}

	@Override
	public boolean isMandatory() {
		return false;
	}

	@Override
	public boolean isDisabled() {
		return false;
	}

	@Override
	public void setStoreAlgorithm(StoreAlgorithm storeAlgorithm) {
		// Ignore.
	}

}
