/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;

/**
 * Reference to another {@link LayoutComponent}.
 * 
 * <p>
 * This configuration is not instantiated, but the {@link LayoutComponent} defined in
 * {@link LayoutReference#getResource()} is used instead.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName(LayoutReference.TAG_NAME)
@DisplayOrder(value = { LayoutComponent.Config.LAYOUT_INFO })
@DisplayInherited(DisplayStrategy.IGNORE)
public interface LayoutReference extends LayoutComponent.Config, ResourceDeclaration {

	/** Tag to use a {@link LayoutReference}. */
	String TAG_NAME = "layout-reference";

	/**
	 * Path to the layout file containing the referenced component.
	 */
	@Override
	@Mandatory
	@ReadOnly
	String getResource();

	@Override
	@DynamicMode(fun = FirstLayoutListParentIsLayout.class, args = @Ref(LayoutComponent.Config.NAME))
	LayoutInfo getLayoutInfo();

	@Override
	@ClassDefault(LayoutReferenceComponent.class)
	Class<? extends LayoutComponent> getImplementationClass();

	@Override
	@Derived(fun = ComponentNameByLocalName.class, args = @Ref(ResourceDeclaration.RESOURCE_ATTRIBUTE))
	ComponentName getName();

	/**
	 * Label of the target {@link LayoutComponent component} given by this {@link LayoutReference
	 * reference}.
	 */
	@Derived(fun = TargetLabelByReferenceResource.class, args = @Ref(ResourceDeclaration.RESOURCE_ATTRIBUTE))
	String getTargetLabel();

	/**
	 * Workaround component to avoid unnecessary "config:interface" annotation when serialising a
	 * {@link LayoutReference}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Deprecated
	public static final class LayoutReferenceComponent extends LayoutComponent {

		/**
		 * Call throws an {@link UnsupportedOperationException}.
		 */
		public LayoutReferenceComponent(InstantiationContext context, LayoutReference atts)
				throws ConfigurationException {
			super(context, atts);
			throw new UnsupportedOperationException("Dummy component which must not be created.");
		}

	}

}

