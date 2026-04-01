/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.function.TriConsumer;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AccessRightsConfig;
import com.top_logic.model.annotate.security.AccessGrant;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.model.config.TLModelPartMapping;
import com.top_logic.model.util.AllClasses;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.model.ModelService;

/**
 * Service class implementation of {@link ModelAccessRights}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	ModelService.Module.class,
	CommandGroupRegistry.Module.class
})
public class SecurityConfigurationService extends ConfiguredManagedClass<SecurityConfigurationService.Config>
		implements ModelAccessRights {

	/**
	 * Typed configuration interface definition for {@link SecurityConfigurationService}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<SecurityConfigurationService> {

		/**
		 * The explicit access right rules, keyed by model element name (e.g.
		 * {@code "my.module:MyClass"}).
		 */
		@Key(ModelAccessRights.NAME_ATTRIBUTE)
		Map<String, ModelAccessRights> getSecurityConfig();

	}

	/**
	 * Base configuration for access rights on a named model element.
	 */
	@Abstract
	public interface ModelAccessRights extends NamedConfigMandatory, AccessRightsConfig {
		// marker interface
	}

	/**
	 * Access rights configuration for a {@link TLClass}, identified by its qualified name.
	 */
	@TagName("class")
	public static interface TLClassAccessRights extends ModelAccessRights {

		@Options(fun = AllClasses.class, mapping = TLModelPartMapping.class)
		@ControlProvider(SelectionControlProvider.class)
		@Override
		String getName();

	}

	/**
	 * Access rights configuration for a module singleton, identified by its qualified name.
	 */
	@TagName("singleton")
	public static interface TLSingletonAccessRights extends ModelAccessRights {
		// marker interface
	}

	/**
	 * Access rights configuration for a {@link TLStructuredTypePart}, identified by its qualified name.
	 */
	@TagName("part")
	public static interface TLPartAccessRights extends ModelAccessRights {
		// marker interface
	}

	/**
	 * Access rights configuration for all classes in a {@link TLModule}, identified by the module name.
	 */
	@TagName("module")
	public static interface TLModuleAccessRights extends ModelAccessRights {

		@Options(fun = TLModelPartRef.AllModules.class, mapping = TLModelPartMapping.class)
		@ControlProvider(SelectionControlProvider.class)
		@Override
		String getName();

	}

	private Map<TLObject, Map<BoundCommandGroup, Set<BoundedRole>>> _singletonRights = new HashMap<>();

	private Map<TLStructuredTypePart, Map<BoundCommandGroup, Set<BoundedRole>>> _typePartRights = new HashMap<>();

	private Map<TLClass, Map<BoundCommandGroup, Set<BoundedRole>>> _classRights = new HashMap<>();

	private Map<TLClass, Map<BoundCommandGroup, Set<BoundedRole>>> _expandedClassRights = new HashMap<>();

	private CommandGroupRegistry _commandGroups;

	private TLModel _applicationModel;

	/**
	 * Create a {@link SecurityConfigurationService}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SecurityConfigurationService(InstantiationContext context, Config config) {
		super(context, config);
		_commandGroups = CommandGroupRegistry.getInstance();
		_applicationModel = ModelService.getApplicationModel();

		for (ModelAccessRights modelConf : config.getSecurityConfig().values()) {
			TLObject modelPart = TLModelUtil.resolveQualifiedName(_applicationModel, modelConf.getName());
			if (modelConf instanceof TLClassAccessRights conf) {
				handleTLClass(context, modelPart, conf);
			} else if (modelConf instanceof TLSingletonAccessRights conf) {
				handleTLSingleton(context, modelPart, conf);
			} else if (modelConf instanceof TLPartAccessRights conf) {
				handleTLPart(context, modelPart, conf);
			} else if (modelConf instanceof TLModuleAccessRights conf) {
				handleTLModule(context, modelPart, conf);
			}
		}

	}

	private void handleTLModule(InstantiationContext context, TLObject part, TLModuleAccessRights config) {
		if (!(part instanceof TLModule)) {
			context.error("The configured part " + part + " is not a module.");
			return;
		}
		TLModule module = (TLModule) part;
		Collection<TLClass> classes = module.getClasses();
		processRights(context, config, (group, roles, inherit) -> {
			for (TLClass clazz : classes) {
				storeClassRights(clazz, group, roles, inherit);
			}
		});
	}

	private void handleTLPart(InstantiationContext context, TLObject part, TLPartAccessRights config) {
		if (!(part instanceof TLStructuredTypePart)) {
			context.error("The configured part " + part + " is not a structured type part.");
			return;
		}
		TLStructuredTypePart typePart = ((TLStructuredTypePart) part).getDefinition();
		processRights(context, config, (group, roles, inherit) -> {
			_typePartRights
				.computeIfAbsent(typePart, unused -> new HashMap<>())
				.put(group, roles);
		});
	}

	private void handleTLSingleton(InstantiationContext context, TLObject singleton, TLSingletonAccessRights config) {
		// Ensure it is really a singleton.
		Collection<TLModuleSingleton> singletons = singleton.tType().getModule().getSingletons();
		if (!singletons.stream().map(TLModuleSingleton::getSingleton).anyMatch(singleton::equals)) {
			context.error("The configured singleton " + singleton + " is not a singleton.");
			return;
		}
		processRights(context, config, (group, roles, inherit) -> {
			_singletonRights
				.computeIfAbsent(singleton, unused -> new HashMap<>())
				.put(group, roles);
		});
	}

	private void handleTLClass(InstantiationContext context, TLObject part, TLClassAccessRights config) {
		if (!(part instanceof TLClass)) {
			context.error("The configured part " + part + " is not a TLClass.");
			return;
		}
		TLClass clazz = (TLClass) part;
		processRights(context, config, (group, roles, inherit) -> storeClassRights(clazz, group, roles, inherit));
	}

	private void processRights(InstantiationContext context, ModelAccessRights config,
			TriConsumer<BoundCommandGroup, Set<BoundedRole>, Boolean> sink) {
		KnowledgeBase kb = _applicationModel.tKnowledgeBase();
		for (AccessGrant grant : config.getGrants().values()) {
			BoundCommandGroup operation = grant.getOperation().resolve(_commandGroups);
			if (operation == null) {
				context.error("The command group " + grant.getOperation().id() + " in configuration for "
						+ config.getName() + " does not exist.");
				continue;
			}
			Set<BoundedRole> roles = new HashSet<>();
			for (RoleConfig roleConf : grant.getRoles()) {
				BoundedRole role = BoundedRole.getRoleByName(kb, roleConf.getName());
				if (role == null) {
					context.error("The role " + roleConf.getName() + " in configuration for " + config.getName()
							+ " does not exist.");
					continue;
				}
				roles.add(role);
			}
			sink.accept(operation, roles, grant.isInherit());
		}

	}

	/**
	 * Stores the given roles for the given type and operation in both the direct and expanded rights
	 * maps, and optionally propagates them to all specializations when {@code inherit} is
	 * {@code true}.
	 */
	private void storeClassRights(TLClass type, BoundCommandGroup operation, Set<BoundedRole> roles, boolean inherit) {
		_classRights
			.computeIfAbsent(type, unused -> new HashMap<>())
			.put(operation, roles);
		_expandedClassRights
			.computeIfAbsent(type, unused -> new HashMap<>())
			.computeIfAbsent(operation, unused -> new HashSet<>())
			.addAll(roles);
		if (inherit) {
			inheritToSpecializations(type, operation, roles);
		}
	}

	/**
	 * Recursively adds the given roles for the given operation to all direct and indirect
	 * specializations of the given type.
	 */
	private void inheritToSpecializations(TLClass type, BoundCommandGroup operation, Collection<BoundedRole> roles) {
		for (TLClass specialization : type.getSpecializations()) {
			_expandedClassRights
				.computeIfAbsent(type, unused -> new HashMap<>())
				.computeIfAbsent(operation, unused -> new HashSet<>())
				.addAll(roles);
			inheritToSpecializations(specialization, operation, roles);
		}
	}

	private static AccessManager accessManager() {
		return AccessManager.getInstance();
	}

	/**
	 * Returns the roles required to perform the given command group on the given object, taking
	 * singleton-specific rules into account before falling back to type-level rules.
	 */
	private Set<? extends BoundRole> getRoles(TLObject object, BoundCommandGroup group) {
		Map<BoundCommandGroup, Set<BoundedRole>> singletonRights =
			_singletonRights.getOrDefault(object, Collections.emptyMap());
		if (!singletonRights.isEmpty()) {
			// Singleton!
			return singletonRights.getOrDefault(group, Collections.emptySet());
		}
		TLStructuredType tType = object.tType();
		return getAllowedRoles((TLClass) tType, group);
	}

	@Override
	public Set<BoundedRole> getAllowedRoles(TLClass type, BoundCommandGroup commandGroup) {
		return _expandedClassRights
			.getOrDefault(type, Collections.emptyMap())
			.getOrDefault(commandGroup, Collections.emptySet());
	}

	@Override
	public Set<BoundedRole> getAllowedRoles(TLStructuredTypePart attribute, BoundCommandGroup commandGroup) {
		// TODO dbu: implement getAllowedRoles!
		if (true)
			throw new UnsupportedOperationException("Generated stub");
		return null;
	}

	@Override
	public boolean isAllowed(Person person, TLObject instance, BoundCommandGroup commandGroup) {
		if (!(instance instanceof BoundObject)) {
			return true;
		}
		Boolean allowedBypass = BoundChecker.isAllowedBypass(person, commandGroup);
		if (allowedBypass != null) {
			return allowedBypass;
		}
		Set<? extends BoundRole> roles = getRoles(instance, commandGroup);
		return accessManager().hasRole(person, (BoundObject) instance, roles);
	}

	@Override
	public boolean isAllowed(Person person, TLObject instance, TLStructuredTypePart attribute,
			BoundCommandGroup commandGroup) {
		if (!(instance instanceof BoundObject)) {
			return true;
		}
		Boolean allowedBypass = BoundChecker.isAllowedBypass(person, commandGroup);
		if (allowedBypass != null) {
			return allowedBypass;
		}
		Set<? extends BoundRole> roles = getRoles(instance, commandGroup);
		boolean allowedOnInstance = accessManager().hasRole(person, (BoundObject) instance, roles);
		if (!allowedOnInstance) {
			return false;
		}
		Set<BoundedRole> requiredPartRoles = _typePartRights
			.getOrDefault(attribute.getDefinition(), Collections.emptyMap())
			.get(commandGroup);

		if (requiredPartRoles.isEmpty()) {
			return true;
		}
		return accessManager().hasRole(person, (BoundObject) instance, requiredPartRoles);
	}

	@Override
	public boolean isAllowedCreate(Person person, TLObject parent, TLStructuredTypePart compositionAttribute) {
		return isAllowed(person, parent, compositionAttribute, SimpleBoundCommandGroup.WRITE);
	}

	@Override
	public Set<TLClass> getAccessibleTypes(Person person, BoundCommandGroup commandGroup) {
		// TODO dbu: implement getAccessibleTypes!
		if (true)
			throw new UnsupportedOperationException("Generated stub");
		return null;
	}

	/**
	 * Module for {@link SecurityConfigurationService}.
	 */
	public static final class Module extends TypedRuntimeModule<SecurityConfigurationService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<SecurityConfigurationService> getImplementation() {
			return SecurityConfigurationService.class;
		}
	}

}
