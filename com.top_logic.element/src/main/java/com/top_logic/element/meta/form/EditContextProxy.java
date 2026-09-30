/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.AttributeUpdate.StoreAlgorithm;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.layout.form.FormMember;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * {@link EditContext} that delegates all calls to another {@link EditContext}.
 */
public class EditContextProxy implements EditContext {
	private final EditContext _base;

	/**
	 * Creates a {@link EditContextProxy}.
	 *
	 * @param base
	 *        The {@link EditContext} to delegate to.
	 */
	public EditContextProxy(EditContext base) {
		_base = base;
	}

	@Override
	public boolean isOrdered() {
		return _base.isOrdered();
	}

	@Override
	public boolean isMultiple() {
		return _base.isMultiple();
	}

	@Override
	public boolean isMandatory() {
		return _base.isMandatory();
	}

	@Override
	public boolean isDisabled() {
		return _base.isDisabled();
	}

	@Override
	public boolean isDerived() {
		return _base.isDerived();
	}

	@Override
	public boolean isComposition() {
		return _base.isComposition();
	}

	@Override
	public boolean inTableContext() {
		return _base.inTableContext();
	}

	@Override
	public boolean isBag() {
		return _base.isBag();
	}

	@Override
	public TLType getValueType() {
		return _base.getValueType();
	}

	@Override
	public TLStructuredType getType() {
		return _base.getType();
	}

	@Override
	public TLObject getObject() {
		return _base.getObject();
	}

	@Override
	public ResKey getLabelKey() {
		return _base.getLabelKey();
	}

	@Override
	public ResKey getDescriptionKey() {
		return _base.getDescriptionKey();
	}

	@Override
	public Object getCorrectValues() {
		return _base.getCorrectValues();
	}

	@Override
	public void setStoreAlgorithm(StoreAlgorithm storeAlgorithm) {
		_base.setStoreAlgorithm(storeAlgorithm);
	}

	@Override
	public TLFormObject getOverlay() {
		return _base.getOverlay();
	}

	@Override
	public Generator getOptions() {
		return _base.getOptions();
	}

	@Override
	public <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
		return _base.getAnnotation(annotationType);
	}

	@Override
	public String getConfigKey(FormMember field) {
		return _base.getConfigKey(field);
	}

	@Override
	public ResKey getTableTitleKey() {
		return _base.getTableTitleKey();
	}

	@Override
	public boolean isSearchUpdate() {
		return _base.isSearchUpdate();
	}
}