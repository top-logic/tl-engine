/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLProperty;

/**
 * Persistent implementation of {@link TLProperty}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class PersistentProperty extends ConfiguredAttributeImpl implements TLProperty {

	/**
	 * Creates a {@link PersistentProperty}.
	 */
	@CalledByReflection
	public PersistentProperty(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public String getName() {
		return tGetDataString(NAME_ATTRIBUTE);
	}

	@Override
	public void setName(String value) {
		tSetDataString(NAME_ATTRIBUTE, value);
	}

	@Override
	public boolean isMandatory() {
		return tGetDataBooleanValue(MANDATORY_ATTR);
	}

	@Override
	public void setMandatory(boolean value) {
		tSetDataBoolean(MANDATORY_ATTR, value);
	}

	@Override
	public boolean isMultiple() {
		return tGetDataBooleanValue(PersistentReference.MULTIPLE_ATTR);
	}

	@Override
	public void setMultiple(boolean value) {
		tSetDataBoolean(PersistentReference.MULTIPLE_ATTR, value);
	}

	@Override
	public boolean isBag() {
		return tGetDataBooleanValue(PersistentReference.BAG_ATTR);
	}

	@Override
	public void setBag(boolean value) {
		tSetDataBoolean(PersistentReference.BAG_ATTR, value);
	}

	@Override
	public boolean isOrdered() {
		return tGetDataBooleanValue(PersistentReference.ORDERED_ATTR);
	}

	@Override
	public void setOrdered(boolean value) {
		tSetDataBoolean(PersistentReference.ORDERED_ATTR, value);
	}

	@Override
	public boolean isAbstract() {
		return tGetDataBooleanValue(ABSTRACT_ATTR);
	}

	@Override
	public void setAbstract(boolean value) {
		tSetDataBoolean(ABSTRACT_ATTR, value);
	}

}
