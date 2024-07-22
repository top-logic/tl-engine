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
import com.top_logic.model.fallback.UpdateFallbackDisplay;

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
		if (!editContext.isSearchUpdate() && member instanceof FormField field) {
			TLStorage storage = editContext.getAnnotation(TLStorage.class);
			if (storage != null) {
				PolymorphicConfiguration<? extends StorageImplementation> implementation = storage.getImplementation();
				if (implementation instanceof AttributeWithFallbackStorage.Config<?> fallbackConfig) {
					String storageAttribute = fallbackConfig.getStorageAttribute();
					Object explicitValue = editContext.getOverlay().tValueByName(storageAttribute);

					if (editContext.isDisabled()) {
						AttributeFormFactory.initFieldValue(editContext, (FormField) member);
					} else {
						String fallbackAttribute = fallbackConfig.getFallbackAttribute();
						
						Object fallbackValue = AttributeFormFactory.toFieldValue(editContext, field,
							editContext.getOverlay().tValueByName(fallbackAttribute));
						field.setPlaceholder(fallbackValue);
						
						Object fieldValue = AttributeFormFactory.toFieldValue(editContext, field, explicitValue);
						field.initializeField(fieldValue);
					}

					member.addCssClass(UpdateFallbackDisplay.CSS_WITH_FALLBACK);
					field.addValueListener(UpdateFallbackDisplay.INSTANCE);
					field.addListener(FormField.ACTIVE_PROPERTY, UpdateFallbackDisplay.INSTANCE);

					// Set initial values.
					UpdateFallbackDisplay.INSTANCE.valueChanged(field, null, explicitValue);

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

}
