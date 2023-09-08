/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.AttributeUpdate.StoreAlgorithm;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.util.TLTypeContext;

/**
 * {@link EditContext} that is independent of an attribute being edited.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CustomEditContext implements EditContext {

	private ResKey _description = ResKey.NONE;

	private ResKey _label = ResKey.NONE;

	private final TLTypeContext _valueType;

	private Object _value = null;

	private boolean _mandatory = false;

	private boolean _ordered = false;

	private boolean _bag = false;

	private boolean _multiple = false;

	private boolean _readonly = false;

	private boolean _composition = false;

	private Generator _options = null;

	/**
	 * Creates a {@link CustomEditContext}.
	 */
	public CustomEditContext(TLTypeContext valueType) {
		_valueType = valueType;
		_multiple = valueType.isMultiple();
		_mandatory = valueType.isMandatory();
	}

	@Override
	public ResKey getLabelKey() {
		return _label;
	}

	/**
	 * @see #getLabelKey()
	 */
	public CustomEditContext setLabel(ResKey label) {
		_label = label;
		return this;
	}

	@Override
	public TLStructuredType getType() {
		return null;
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
	public TLType getValueType() {
		return _valueType.getType();
	}

	@Override
	public <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
		return _valueType.getAnnotation(annotationType);
	}

	@Override
	public Object getCorrectValues() {
		return _value;
	}

	/**
	 * @see #getCorrectValues()
	 */
	public CustomEditContext setValue(Object value) {
		_value = value;
		return this;
	}

	@Override
	public boolean isMandatory() {
		return _mandatory;
	}

	/**
	 * @see #isMandatory()
	 */
	public CustomEditContext setMandatory(boolean mandatory) {
		_mandatory = mandatory;
		return this;
	}

	@Override
	public boolean isMultiple() {
		return _multiple;
	}

	/**
	 * @see #isMultiple()
	 */
	public CustomEditContext setMultiple(boolean multiple) {
		_multiple = multiple;
		return this;
	}

	@Override
	public boolean isOrdered() {
		return _ordered;
	}

	/**
	 * @see #isOrdered()
	 */
	public CustomEditContext setOrdered(boolean ordered) {
		_ordered = ordered;
		return this;
	}

	@Override
	public boolean isBag() {
		return _bag;
	}

	/**
	 * @see #isBag()
	 */
	public CustomEditContext setBag(boolean bag) {
		_bag = bag;
		return this;
	}

	@Override
	public boolean isDisabled() {
		return _readonly;
	}

	@Override
	public boolean isDerived() {
		return _readonly;
	}

	/**
	 * @see #isDisabled()
	 * @see #isDerived()
	 */
	public CustomEditContext setReadonly(boolean readonly) {
		_readonly = readonly;
		return this;
	}

	@Override
	public boolean isComposition() {
		return _composition;
	}

	/**
	 * @see #isComposition()
	 */
	public CustomEditContext setComposition(boolean composition) {
		_composition = composition;
		return this;
	}

	@Override
	public Generator getOptions() {
		return _options;
	}

	/**
	 * @see #getOptions()
	 */
	public CustomEditContext setOptions(Generator options) {
		_options = options;
		return this;
	}

	@Override
	public void setStoreAlgorithm(StoreAlgorithm storeAlgorithm) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResKey getDescriptionKey() {
		return _description;
	}

	/**
	 * @see #getDescriptionKey()
	 */
	public CustomEditContext setDescription(ResKey description) {
		_description = description;
		return this;
	}
}
