/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import com.top_logic.basic.Log;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.element.layout.formeditor.builder.ConfiguredDynamicFormBuilder;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.DerivedComponentChannel;
import com.top_logic.layout.channel.linking.impl.AbstractChannelLinking;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.channel.linking.ref.ComponentRef;
import com.top_logic.layout.channel.linking.ref.ComponentRelation;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;

/**
 * {@link ChannelLinking} accessing the current form data of a {@link FormHandler}.
 */
@InApp
public class FormData extends AbstractChannelLinking<FormData.Config> {

	/**
	 * The form object of a form component.
	 */
	@TagName("form")
	public interface Config extends ModelSpec {

		@Override
		@ClassDefault(FormData.class)
		Class<? extends FormData> getImplementationClass();

		/**
		 * @see #getComponentRef()
		 */
		String COMPONENT_REF = "component-ref";

		/**
		 * Specification of the component to access the form data.
		 */
		@Name(COMPONENT_REF)
		@DefaultContainer
		@ImplementationClassDefault(ComponentRelation.class)
		ComponentRef getComponentRef();

		/**
		 * @see #getComponentRef()
		 */
		void setComponentRef(ComponentRef ref);

	}

	/**
	 * Creates a {@link FormData}.
	 */
	public FormData(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public ComponentChannel resolveChannel(Log log, LayoutComponent contextComponent) {
		LayoutComponent component = ComponentRef.resolveComponent(getConfig().getComponentRef(), contextComponent);
		return new DerivedComponentChannel(component, Provider.INSTANCE);
	}

	@Override
	public Object eval(LayoutComponent component) {
		return Provider.INSTANCE.getBusinessModel(component);
	}

	@Override
	public void appendTo(StringBuilder result) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@link ModelProvider} retrieving the form data from a {@link FormHandler}.
	 */
	public static class Provider implements ModelProvider {
	
		/**
		 * Singleton {@link Provider} instance.
		 */
		public static final Provider INSTANCE = new Provider();
	
		private Provider() {
			// Singleton constructor.
		}
	
		@Override
		public Object getBusinessModel(LayoutComponent businessComponent) {
			if (businessComponent instanceof FormHandler form) {
				AttributeFormContext fc = (AttributeFormContext) form.getFormContext();
	
				AttributeUpdateContainer updateContainer = fc.getAttributeUpdateContainer();
				TLFormObject formData;
				if (fc.isSet(ConfiguredDynamicFormBuilder.TOP_LEVEL_OBJECT)) {
					formData =
						updateContainer.getOverlay(fc.get(ConfiguredDynamicFormBuilder.TOP_LEVEL_OBJECT), null);
				} else {
					// Try to fetch create.
					formData = updateContainer.getOverlay(null, null);
					if (formData == null) {
						// Try to fetch edited model.
						formData = updateContainer.getOverlay((TLObject) businessComponent.getModel(), null);
						if (formData == null) {
							// Use just the first one.
							formData = updateContainer.getAllOverlays().iterator().next();
						}
					}
				}
	
				return formData;
			}
			return null;
		}
	
	}
}