/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.map.HashedMap;

import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.model.ModelService;

/**
 * {@link CommandApprovalService} with configured checks.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({
	PersistencyLayer.Module.class,
	ModelService.Module.class,
})
public class ConfiguredCommandApprovalService extends CommandApprovalService {

	/**
	 * Configuration options for {@link CommandApprovalService}.
	 */
	public interface Config<I extends CommandApprovalService> extends ServiceConfiguration<I> {

		/**
		 * @see #getChecks()
		 */
		String CHECKS = "checks";

		/**
		 * Configured additional checks to perform before commands are executed.
		 */
		@Name(CHECKS)
		@Key(Check.TYPE)
		@DefaultContainer
		List<Check> getChecks();

		/**
		 * Assignment of a {@link CommandApprovalRule} to a certain {@link TLStructuredType} for checking
		 * commands executed on instances of that type.
		 */
		@DisplayOrder({
			Check.TYPE,
			Check.RULES,
		})
		interface Check extends ConfigurationItem {

			/**
			 * @see #getType()
			 */
			String TYPE = "type";

			/**
			 * @see #getRules()
			 */
			String RULES = "rules";

			/**
			 * The type of command target models to which the {@link #getRules()} applies to.
			 */
			@Name(TYPE)
			@Mandatory
			TLModelPartRef getType();

			/**
			 * The {@link CommandApprovalRule} to apply to instances of {@link #getType()}.
			 */
			@Name(RULES)
			@DefaultContainer
			@ImplementationClassDefault(ConfiguredApprovalRule.class)
			@Options(fun = AllInAppImplementations.class)
			List<PolymorphicConfiguration<CommandApprovalRule>> getRules();
		}
	}

	private Map<ObjectKey, CommandApprovalRule> _configuredCheckers;

	private ConcurrentHashMap<ObjectKey, CommandApprovalRule> _checkers;

	/**
	 * Creates a {@link ConfiguredCommandApprovalService}.
	 */
	public ConfiguredCommandApprovalService(InstantiationContext context, Config<?> config)
			throws ConfigurationException {
		super(context, config);

		_configuredCheckers = new HashedMap<>();
		for (Config.Check typeCheck : config.getChecks()) {
			TLClass type = typeCheck.getType().resolveClass();

			CommandApprovalRule checker = null;
			for (PolymorphicConfiguration<CommandApprovalRule> checkerConfig : typeCheck.getRules()) {
				CommandApprovalRule part = context.getInstance(checkerConfig);
				checker = checker == null ? part : checker.combine(part);
			}
			if (checker != null) {
				_configuredCheckers.put(type.tId(), checker);
			}
		}

		_checkers = new ConcurrentHashMap<>(_configuredCheckers);
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent component, BoundCommandGroup commandGroup, String commandId,
			Object model, Map<String, Object> arguments) {
		if (model == null) {
			return ExecutableState.EXECUTABLE;
		}
		if (!(model instanceof TLObject)) {
			return ExecutableState.EXECUTABLE;
		}
		TLStructuredType type = ((TLObject) model).tType();
		if (type == null) {
			return ExecutableState.EXECUTABLE;
		}
		return checker(type).isExecutable(component, commandGroup, commandId, model, arguments);
	}

	private CommandApprovalRule checker(TLStructuredType type) {
		ObjectKey typeId = type.tId();
		CommandApprovalRule result = _checkers.get(typeId);
		if (result != null) {
			return result;
		}

		return MapUtil.putIfAbsent(_checkers, typeId, buildChecker(type));
	}

	private CommandApprovalRule buildChecker(TLStructuredType type) {
		CommandApprovalRule result = CommandApprovalRule.ALLOW;
		if (type.getModelKind() == ModelKind.CLASS) {
			List<TLClass> generalizations = ((TLClass) type).getGeneralizations();
			for (TLClass generalization : generalizations) {
				result = result.combine(checker(generalization));
			}
		}
		return result;
	}

}
