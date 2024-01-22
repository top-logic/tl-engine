/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.IfTrue;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.form.values.edit.annotation.DynamicMandatory;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.tool.boundsec.BoundLayout;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;

/**
 * Configuration fragment allowing to specify separate security for the component.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface WithSecurityConfiguration extends ConfigurationItem {

	/** Configuration name for {@link #getWithSecurityConfiguration()}. */
	String WITH_SECURITY_CONFIGURATION = "with-security-configuration";

	/** Configuration name for {@link #getComponentClass()}. */
	String COMPONENT_CLASS = "component-class";

	/** Configuration name for {@link #getSecurityLabel()}. */
	String SECURITY_LABEL = "security-label";

	/**
	 * Allows to configure special security configuration for the component.
	 */
	@Name(WITH_SECURITY_CONFIGURATION)
	boolean getWithSecurityConfiguration();

	/**
	 * The implementation class that is used for the component.
	 */
	@Hidden
	@Derived(fun = ComponentClassComputation.class, args = @Ref(WITH_SECURITY_CONFIGURATION))
	@Name(COMPONENT_CLASS)
	Class<? extends LayoutComponent> getComponentClass();

	/**
	 * The label that is used to identify this component in the security configuration.
	 */
	@DynamicMandatory(fun = IfTrue.class, args = @Ref(WITH_SECURITY_CONFIGURATION))
	@DynamicMode(fun = CommandHandler.ConfirmConfig.VisibleIf.class, args = @Ref(WITH_SECURITY_CONFIGURATION))
	@Label("Label in security configuration")
	ResKey getSecurityLabel();

	/**
	 * {@link Function1} to compute value {@link WithSecurityConfiguration#getComponentClass()}.
	 * 
	 * @see WithSecurityConfiguration#getComponentClass()
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	class ComponentClassComputation extends Function1<Class<? extends LayoutComponent>, Boolean> {

		@Override
		public Class<? extends LayoutComponent> apply(Boolean arg) {
			if (Utils.isTrue(arg)) {
				return CompoundSecurityLayout.class;
			}
			return BoundLayout.class;
		}

	}

}
