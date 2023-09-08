/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Objects;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * A {@link FormContextModificator} that sets a configured {@link ControlProvider} at the field for
 * the specified attribute.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ControlProviderModificator<C extends ControlProviderModificator.Config>
		extends DefaultFormContextModificator implements ConfiguredInstance<C> {

	/** {@link ConfigurationItem} for the {@link ControlProviderModificator}. */
	public interface Config extends PolymorphicConfiguration<ControlProviderModificator<?>> {

		/** Property name of {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Property name of {@link #getControlProvider()}. */
		String CONTROL_PROVIDER = "controlProvider";

		/** The name of the attribute whose {@link ControlProvider} should be set. */
		@Mandatory
		@Name(ATTRIBUTE)
		String getAttribute();

		/**
		 * The {@link ControlProvider} to set at the {@link FormField}.
		 * <p>
		 * The {@link ControlProvider} is created just once per {@link FormContextModificator}
		 * instance.
		 * </p>
		 */
		@Mandatory
		@Name(CONTROL_PROVIDER)
		PolymorphicConfiguration<ControlProvider> getControlProvider();

	}

	private final C _config;

	private final ControlProvider _controlProvider;

	/** {@link TypedConfiguration} constructor for {@link ControlProviderModificator}. */
	public ControlProviderModificator(InstantiationContext context, C config) {
		_config = config;
		_controlProvider = context.getInstance(config.getControlProvider());
	}

	@Override
	public void modify(LayoutComponent component, String attributeName, FormMember formMember,
			TLStructuredTypePart attribute, TLClass type, TLObject tlObject, AttributeUpdate attributeUpdate,
			AttributeFormContext formContext, FormContainer currentGroup) {
		super.modify(component, attributeName, formMember, attribute, type, tlObject, attributeUpdate, formContext,
			currentGroup);
		if (Objects.equals(attributeName, getAttributeName())) {
			formMember.setControlProvider(_controlProvider);
		}
	}

	private String getAttributeName() {
		return getConfig().getAttribute();
	}

	@Override
	public C getConfig() {
		return _config;
	}

}
