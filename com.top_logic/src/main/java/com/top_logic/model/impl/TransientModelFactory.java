/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.util.TLCharacteristicsCopier;
import com.top_logic.util.error.TopLogicException;

/**
 * Factory methods to create transient model overlays on top of any model.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TransientModelFactory {

	/**
	 * Creates a transient {@link TLClass} implementation as overlay to the given model.
	 * 
	 * <p>
	 * The resulting class may refere to model elements of the given model, but is not reported to
	 * be part of the given model.
	 * </p>
	 */
	public static TLClass createTransientClass(TLModel model, String name) {
		return new TLClassImpl(model, name);
	}

	/**
	 * Creates a property in a transient class created with
	 * {@link #createTransientClass(TLModel, String)}.
	 */
	public static TLClassProperty addClassProperty(TLClass tlClass, String name, TLType valueType) {
		TLClassProperty newProperty = new TLClassPropertyImpl(tlClass.getModel(), name);
		newProperty.setType(valueType);
	
		TLClassPart part = (TLClassPart) tlClass.getPart(name);
		if (part != null) {
			if (part.getOwner() == tlClass) {
				throw new TopLogicException(
					com.top_logic.model.I18NConstants.DUPLICATE_ATTRIBUTE__NAME_CLASS.fill(name, tlClass));
			}
			TLCharacteristicsCopier.copyOverrideCharacteristics(part, newProperty);
		}
	
		tlClass.getLocalClassParts().add(newProperty);
		newProperty.updateDefinition();
		return newProperty;
	}

	/**
	 * Creates a transient object as instance of some type.
	 */
	public static TLObject createTransientObject(TLClass type) {
		return TransientObjectFactory.INSTANCE.createObject(type);
	}

}
