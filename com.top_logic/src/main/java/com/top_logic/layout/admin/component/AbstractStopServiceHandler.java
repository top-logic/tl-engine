/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.provider.label.I18NClassNameProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.AbstractConfirmationHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.NullModelDisabled;

/**
 * {@link AbstractCommandHandler} asking for confirmation if a service with dependents should be
 * stopped.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class AbstractStopServiceHandler extends AbstractConfirmationHandler {

	/**
	 * Configuration of an {@link AbstractStopServiceHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractConfirmationHandler.Config {

		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	/**
	 * Creates a {@link AbstractStopServiceHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AbstractStopServiceHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> args) {
		BasicRuntimeModule<?> module = (BasicRuntimeModule<?>) model;

		if (!getAllDependentModules(module).isEmpty()) {
			return super.handleCommand(context, component, model, args);
		}

		return handleCommandInternal(context, component, model, args);
	}

	@Override
	protected HTMLFragment getMessage(Object model) {
		return createMessageFragment((BasicRuntimeModule<?>) model);
	}

	private HTMLFragment createMessageFragment(BasicRuntimeModule<?> module) {
		Collection<? extends BasicRuntimeModule<?>> dependencies = getAllDependentModules(module);

		HTMLFragment dependencyFragment = createDependencyFragment(dependencies);

		return createMessageFragment(module, dependencyFragment);
	}

	private Collection<BasicRuntimeModule<?>> getAllDependentModules(BasicRuntimeModule<?> module) {
		return ModuleUtil.INSTANCE.getAllDependents(module, false);
	}

	private HTMLFragment createMessageFragment(BasicRuntimeModule<?> module, HTMLFragment dependencies) {
		HTMLFragment message = createIntroMessage(module);
		HTMLFragment confirmationMessage = Fragments.message(I18NConstants.CONFIRMATION_MESSAGE);

		return Fragments.concat(message, dependencies, confirmationMessage);
	}

	private HTMLFragment createIntroMessage(BasicRuntimeModule<?> module) {
		ResKey intro = I18NConstants.SERVICE_DEPENDENT_MODULES_MESSAGE__MODULE.fill(moduleLabel(module));

		return Fragments.message(intro);
	}

	private HTMLFragment createDependencyFragment(Collection<? extends BasicRuntimeModule<?>> dependencies) {
		List<HTMLFragment> dependencyFragments = createDependencies(dependencies);

		return Fragments.ul(dependencyFragments.toArray(new HTMLFragment[dependencyFragments.size()]));
	}

	private List<HTMLFragment> createDependencies(Collection<? extends BasicRuntimeModule<?>> dependencies) {
		return dependencies.stream()
			.map(runtimeModule -> Fragments.li(Fragments.text(moduleLabel(runtimeModule))))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	static String moduleLabel(BasicRuntimeModule<?> module) {
		return I18NClassNameProvider.INSTANCE.getLabel(module.getImplementation());
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(),
			NullModelDisabled.INSTANCE,
			isActiveRule());
	}

	private ExecutabilityRule isActiveRule() {
		return new ExecutabilityRule() {

			@Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model,
					Map<String, Object> someValues) {
				BasicRuntimeModule<?> module = (BasicRuntimeModule<?>) model;
				if (!module.isActive()) {
					return ExecutableState
						.createDisabledState(I18NConstants.SERVICE_NOT_STARTED__SERVICE.fill(moduleLabel(module)));
				}
				return ExecutableState.EXECUTABLE;
			}
		};
	}
}
