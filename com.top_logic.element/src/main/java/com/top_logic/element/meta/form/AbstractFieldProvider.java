/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeWithFallbackStorage;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;

/**
 * Common base class for {@link FieldProvider} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFieldProvider implements FieldProvider {

	@Override
	public final FormMember getFormField(EditContext editContext, String fieldName) {
		FormMember field = createFormField(editContext, fieldName);
		initValue(editContext, field);
		return field;
	}

	/**
	 * Sets the given field's value to the value from the {@link EditContext}.
	 */
	protected void initValue(EditContext editContext, FormMember member) {
		if (member instanceof FormField) {
			TLStorage storage = editContext.getAnnotation(TLStorage.class);
			if (storage != null) {
				PolymorphicConfiguration<? extends StorageImplementation> implementation = storage.getImplementation();
				if (implementation instanceof AttributeWithFallbackStorage.Config<?> fallbackConfig) {
					String storageAttribute = fallbackConfig.getStorageAttribute();
					String fallbackAttribute = fallbackConfig.getFallbackAttribute();

					Object explicitValue = editContext.getOverlay().tValueByName(storageAttribute);
					Object fallbackValue = editContext.getOverlay().tValueByName(fallbackAttribute);

					FormField field = (FormField) member;
					Object fieldValue = AttributeFormFactory.toFieldValue(editContext, field, explicitValue);
					field.initializeField(fieldValue);

					field.setPlaceholder(fallbackValue);
					return;
				}
			}

			AttributeFormFactory.initFieldValue(editContext, (FormField) member);
		}
	}

	/**
	 * Creates a new {@link FormField} with the given name suitable to represent a value from the
	 * given {@link EditContext}.
	 */
	protected abstract FormMember createFormField(EditContext editContext, String fieldName);

	@Override
	public final Unimplementable unimplementable() {
		return null;
	}

}
