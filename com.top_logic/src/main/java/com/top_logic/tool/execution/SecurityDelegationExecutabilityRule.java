/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * {@link ExecutabilityRule} that explicitly checks for permission.
 * 
 * <p>
 * For regular commands, this check is done implicitly: A component on which a command is executed
 * needs to give permission for the command's {@link BoundCommandGroup} and the command's base
 * model. Only if a command explicitly delegates permission to another component, a
 * {@link SecurityDelegationExecutabilityRule} is required.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SecurityDelegationExecutabilityRule<C extends SecurityDelegationExecutabilityRule.Config<?>>
		extends AbstractConfiguredInstance<C> implements ExecutabilityRule {

	/**
	 * Configuration options for {@link SecurityDelegationExecutabilityRule}.
	 */
	@TagName("security-check")
	public interface Config<I extends SecurityDelegationExecutabilityRule<?>> extends PolymorphicConfiguration<I> {

		/**
		 * The {@link BoundCommandGroup} to check access for.
		 * 
		 * <p>
		 * Normally, this must be the command group of the command in which context this
		 * {@link ExecutabilityRule} rule is used. For technical reasons, a redundant configuration
		 * is necessary.
		 * </p>
		 */
		@Mandatory
		BoundCommandGroup getGroup();

		/**
		 * Name of the {@link LayoutComponent} that is responsible for checking access.
		 * 
		 * <p>
		 * The configured component must be a {@link BoundChecker}.
		 * </p>
		 * 
		 * <p>
		 * This rule allows execution, if the referenced component
		 * {@link BoundChecker#allow(com.top_logic.tool.boundsec.BoundCommandGroup, BoundObject)
		 * permits access} to the command's base model using the configured {@link #getGroup()
		 * command group}.
		 * </p>
		 * 
		 * @see BoundChecker#allow(com.top_logic.tool.boundsec.BoundCommandGroup, BoundObject)
		 */
		@Mandatory
		ComponentName getSecurityComponent();
	}

	private final BoundCommandGroup _group;

	private BoundChecker _checker;

	/**
	 * Creates a {@link SecurityDelegationExecutabilityRule} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SecurityDelegationExecutabilityRule(InstantiationContext context, C config) {
		super(context, config);

		_group = config.getGroup();

		context.resolveReference(config.getSecurityComponent(), LayoutComponent.class,
			c -> _checker = asChecker(c));
	}

	static BoundChecker asChecker(LayoutComponent component) {
		if (!(component instanceof BoundChecker)) {
			throw new ConfigurationError(
				I18NConstants.ERROR_NOT_A_CHECKER__COMPONENT_LOCATION.fill(
					component.getName(), component.getLocation()));
		}
		return (BoundChecker) component;
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (model instanceof BoundObject) {
			boolean allow = _checker.allow(_group, (BoundObject) model);
			if (!allow) {
				return ExecutableState.NO_EXEC_PERMISSION;
			}
		}
		return ExecutableState.EXECUTABLE;
	}

}
