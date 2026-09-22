/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.ServiceExtensionPoint;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.mode.HideActiveIf;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AccessRightsConfig;
import com.top_logic.model.annotate.security.AccessRevoke;
import com.top_logic.model.annotate.security.AccessRule;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.model.config.SingletonMapping;
import com.top_logic.model.config.TLModelPartMapping;
import com.top_logic.model.util.AllAttributes;
import com.top_logic.model.util.AllClasses;
import com.top_logic.model.util.AllSingletons;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.model.ModelService;

/**
 * Provides the configured access rights for model elements.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	ModelService.Module.class,
	CommandGroupRegistry.Module.class,
	InitialRolesManager.Module.class
})
@ServiceExtensionPoint(ModelService.Module.class)
@Label("Model access rights")
public class SecurityConfigurationService extends ConfiguredManagedClass<SecurityConfigurationService.Config>
		implements ModelAccessRights {

	/**
	 * Typed configuration interface definition for {@link SecurityConfigurationService}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<SecurityConfigurationService> {

		/**
		 * The explicit access right rules, indexed by model element name (e.g.
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
	 * Base configuration for access rights on all objects of a {@link TLClass}, or of all
	 * {@link TLClass}es of a {@link TLModule}.
	 */
	@Abstract
	public interface TypeBasedAccessRights extends ModelAccessRights {

		/** Configuration name for {@link #isWithoutSecurity()}. */
		String WITHOUT_SECURITY = "without-security";

		/** Configuration name for {@link #isInternal()}. */
		String INTERNAL = "internal";

		/**
		 * Whether the configured types are excluded from access control.
		 *
		 * <p>
		 * An object of a type without security is not access controlled: Every user may access the
		 * object and its attribute values, no matter which roles the user has on it, and every user
		 * may create such an object. Only a restricted user, who must not perform the operation at
		 * all, stays excluded.
		 * </p>
		 *
		 * <p>
		 * A specialization of a type without security is without security, too. Declaring a
		 * {@link TLModule} without security therefore excludes every class of that module and all
		 * their specializations from access control.
		 * </p>
		 */
		@Name(WITHOUT_SECURITY)
		boolean isWithoutSecurity();

		/**
		 * Whether objects of the configured types are used by the application's own code only.
		 *
		 * <p>
		 * An internal type is never accessed on behalf of a user: its objects are transient, or
		 * they are read and written in a context that bypasses the access check. No user is
		 * expected to hold a role on them, so an internal type needs neither a grant, nor a role
		 * rule, nor a security parent, and a check of the access definition does not report the
		 * missing ones. The access check itself is not changed: a user asking for such an object is
		 * denied, since no role is granted.
		 * </p>
		 *
		 * <p>
		 * A specialization of an internal type is internal, too. Declaring a {@link TLModule}
		 * internal therefore marks every class of that module and all their specializations.
		 * </p>
		 */
		@Name(INTERNAL)
		boolean isInternal();

		/**
		 * @see #isWithoutSecurity() The grants of a type without security are not displayed, since
		 *      access to its objects is not controlled.
		 */
		@Override
		@DynamicMode(fun = HideActiveIf.class, args = @Ref(WITHOUT_SECURITY))
		List<AccessRule> getGrants();

	}

	/**
	 * Configuration of access rights on objects of a {@link TLClass}. The {@link TLClass} is
	 * identified by its qualified name.
	 */
	@TagName("class")
	@Label("Class based access rights")
	public static interface TLClassAccessRights extends TypeBasedAccessRights {

		@Options(fun = AllClasses.class, mapping = TLModelPartMapping.class)
		@ControlProvider(SelectionControlProvider.class)
		@Override
		String getName();

	}

	/**
	 * Configuration of access rights on a module singleton, identified by its qualified name.
	 */
	@TagName("singleton")
	@Label("Singleton access rights")
	public static interface TLSingletonAccessRights extends ModelAccessRights {

		@Options(fun = AllSingletons.class, mapping = SingletonMapping.class)
		@ControlProvider(SelectionControlProvider.class)
		@Override
		String getName();

	}

	/**
	 * Configuration of access rights to values for a {@link TLStructuredTypePart}. The part is
	 * identified by its qualified name.
	 */
	@TagName("part")
	@Label("Attribute value access rights")
	public static interface TLPartAccessRights extends ModelAccessRights {

		@Options(fun = AllAttributes.class, mapping = TLModelPartMapping.class)
		@ControlProvider(SelectionControlProvider.class)
		@Override
		String getName();

	}

	/**
	 * Configuration of access rights that are applied to all classes in a {@link TLModule}. The
	 * module is identified by the module name.
	 */
	@TagName("module")
	@Label("Module based access rights")
	public static interface TLModuleAccessRights extends TypeBasedAccessRights {

		@Options(fun = TLModelPartRef.AllModules.class, mapping = TLModelPartMapping.class)
		@ControlProvider(SelectionControlProvider.class)
		@Override
		String getName();

	}

	private Map<TLObject, Map<BoundCommandGroup, Set<BoundedRole>>> _singletonRights = new HashMap<>();

	private Map<TLStructuredTypePart, Map<BoundCommandGroup, Set<BoundedRole>>> _typePartRights = new HashMap<>();

	private Map<TLClass, Map<BoundCommandGroup, Set<BoundedRole>>> _expandedClassRights = new HashMap<>();

	private Set<TLClass> _typesWithoutSecurity = new HashSet<>();

	private Set<TLClass> _internalTypes = new HashSet<>();

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

		Map<TLClass, List<ResolvedRule>> classRules = new HashMap<>();
		Map<TLModule, List<ResolvedRule>> moduleRules = new HashMap<>();
		for (ModelAccessRights modelConf : config.getSecurityConfig().values()) {
			TLObject modelPart = TLModelUtil.resolveQualifiedName(_applicationModel, modelConf.getName());
			if (modelConf instanceof TLClassAccessRights conf) {
				handleTLClass(context, modelPart, conf, classRules);
			} else if (modelConf instanceof TLSingletonAccessRights conf) {
				handleTLSingleton(context, modelPart, conf);
			} else if (modelConf instanceof TLPartAccessRights conf) {
				handleTLPart(context, modelPart, conf);
			} else if (modelConf instanceof TLModuleAccessRights conf) {
				handleTLModule(context, modelPart, conf, moduleRules);
			}
		}

		computeClassRights(classRules, moduleRules);
	}

	private void handleTLModule(InstantiationContext context, TLObject part, TLModuleAccessRights config,
			Map<TLModule, List<ResolvedRule>> moduleRules) {
		if (!(part instanceof TLModule module)) {
			context.error("The configured part " + part + " is not a module.");
			return;
		}
		if (config.isWithoutSecurity()) {
			module.getClasses().forEach(this::markWithoutSecurity);
		}
		if (config.isInternal()) {
			module.getClasses().forEach(this::markInternal);
		}
		moduleRules.computeIfAbsent(module, unused -> new ArrayList<>()).addAll(resolveRules(context, config));
	}

	private void handleTLPart(InstantiationContext context, TLObject part, TLPartAccessRights config) {
		if (!(part instanceof TLStructuredTypePart typePart)) {
			context.error("The configured part " + part + " is not a structured type part.");
			return;
		}
		applyRules(resolveRules(context, config),
			_typePartRights.computeIfAbsent(typePart.getDefinition(), unused -> new HashMap<>()));
	}

	private void handleTLSingleton(InstantiationContext context, TLObject singleton, TLSingletonAccessRights config) {
		// Ensure it is really a singleton.
		Collection<TLModuleSingleton> singletons = singleton.tType().getModule().getSingletons();
		if (!singletons.stream().map(TLModuleSingleton::getSingleton).anyMatch(singleton::equals)) {
			context.error("The configured singleton " + singleton + " is not a singleton.");
			return;
		}
		applyRules(resolveRules(context, config),
			_singletonRights.computeIfAbsent(singleton, unused -> new HashMap<>()));
	}

	private void handleTLClass(InstantiationContext context, TLObject part, TLClassAccessRights config,
			Map<TLClass, List<ResolvedRule>> classRules) {
		if (!(part instanceof TLClass clazz)) {
			context.error("The configured part " + part + " is not a TLClass.");
			return;
		}
		if (config.isWithoutSecurity()) {
			markWithoutSecurity(clazz);
		}
		if (config.isInternal()) {
			markInternal(clazz);
		}
		classRules.computeIfAbsent(clazz, unused -> new ArrayList<>()).addAll(resolveRules(context, config));
	}

	/**
	 * Excludes the given type and all its specializations from access control.
	 *
	 * @see TypeBasedAccessRights#isWithoutSecurity()
	 */
	private void markWithoutSecurity(TLClass type) {
		if (_typesWithoutSecurity.add(type)) {
			for (TLClass specialization : type.getSpecializations()) {
				markWithoutSecurity(specialization);
			}
		}
	}

	/**
	 * Marks the given type and all its specializations as used by the application's code only.
	 *
	 * @see TypeBasedAccessRights#isInternal()
	 */
	private void markInternal(TLClass type) {
		if (_internalTypes.add(type)) {
			for (TLClass specialization : type.getSpecializations()) {
				markInternal(specialization);
			}
		}
	}

	@Override
	public boolean isInternal(TLClass type) {
		return _internalTypes.contains(type);
	}

	/**
	 * Whether the given object is an instance of a type without security.
	 *
	 * @param instance
	 *        The object to access. May be <code>null</code>, which is access controlled.
	 *
	 * @see TypeBasedAccessRights#isWithoutSecurity()
	 */
	private boolean isWithoutSecurity(TLObject instance) {
		return instance != null && instance.tType() instanceof TLClass type && isWithoutSecurity(type);
	}

	/**
	 * @see TypeBasedAccessRights#isWithoutSecurity() The configuration excluding a type from access
	 *      control.
	 */
	@Override
	public boolean isWithoutSecurity(TLClass type) {
		return _typesWithoutSecurity.contains(type);
	}

	/**
	 * Resolves the operation and the roles of each configured rule against the application,
	 * reporting a configuration error for an operation or role that does not exist.
	 */
	private List<ResolvedRule> resolveRules(InstantiationContext context, ModelAccessRights config) {
		KnowledgeBase kb = _applicationModel.tKnowledgeBase();
		List<ResolvedRule> result = new ArrayList<>();
		for (AccessRule rule : config.getGrants()) {
			BoundCommandGroup operation = rule.getOperation().resolve(_commandGroups);
			if (operation == null) {
				context.error("The command group " + rule.getOperation().id() + " in configuration for "
						+ config.getName() + " does not exist.");
				continue;
			}
			Set<BoundedRole> roles = new HashSet<>();
			for (RoleConfig roleConf : rule.getRoles()) {
				BoundedRole role = BoundedRole.getRoleByName(kb, roleConf.getName());
				if (role == null) {
					context.error("The role " + roleConf.getName() + " in configuration for " + config.getName()
							+ " does not exist.");
					continue;
				}
				roles.add(role);
			}
			result.add(new ResolvedRule(operation, roles, rule.isInherit(), rule instanceof AccessRevoke));
		}
		return result;
	}

	/**
	 * Applies the given rules in order to the given rights of a single model element.
	 */
	private static void applyRules(List<ResolvedRule> rules, Map<BoundCommandGroup, Set<BoundedRole>> rights) {
		for (ResolvedRule rule : rules) {
			applyRule(rule, rights);
		}
	}

	/**
	 * Applies the given rules in order to the rights of a type, where a rule declared to be
	 * inherited also adjusts the rights that are passed on to the specializations of that type.
	 */
	private static void applyRules(List<ResolvedRule> rules, Map<BoundCommandGroup, Set<BoundedRole>> effective,
			Map<BoundCommandGroup, Set<BoundedRole>> passedOn) {
		for (ResolvedRule rule : rules) {
			applyRule(rule, effective);
			if (rule.inherit()) {
				applyRule(rule, passedOn);
			}
		}
	}

	/**
	 * Adds the roles of a grant to the operation's role set, or removes the roles of a revocation
	 * from it. A revocation without roles drops the operation's entry.
	 */
	private static void applyRule(ResolvedRule rule, Map<BoundCommandGroup, Set<BoundedRole>> rights) {
		BoundCommandGroup operation = rule.operation();
		if (!rule.revoke()) {
			rights.computeIfAbsent(operation, unused -> new HashSet<>()).addAll(rule.roles());
			return;
		}
		if (rule.roles().isEmpty()) {
			rights.remove(operation);
			return;
		}
		Set<BoundedRole> allowed = rights.get(operation);
		if (allowed != null) {
			allowed.removeAll(rule.roles());
		}
	}

	/**
	 * Computes the rights of all types, processing each type after its generalizations, so that a
	 * type sees the rights its generalizations pass on.
	 */
	private void computeClassRights(Map<TLClass, List<ResolvedRule>> classRules,
			Map<TLModule, List<ResolvedRule>> moduleRules) {
		Map<TLClass, Map<BoundCommandGroup, Set<BoundedRole>>> passedOn = new HashMap<>();
		for (TLClass type : TLModelUtil.getAllGlobalClasses(_applicationModel)) {
			computeClassRights(type, classRules, moduleRules, passedOn);
		}
		for (TLClass type : classRules.keySet()) {
			computeClassRights(type, classRules, moduleRules, passedOn);
		}
	}

	/**
	 * Computes the rights of the given type and returns the rights it passes on to its
	 * specializations.
	 *
	 * @implNote The rights a type requires start out as the union of the rights all its
	 *           generalizations pass on. On top of that, the rules of the type's module are applied
	 *           as an additive baseline, followed by the rules configured for the type itself. A
	 *           rule marked as inherited adjusts the passed on rights as well.
	 */
	private Map<BoundCommandGroup, Set<BoundedRole>> computeClassRights(TLClass type,
			Map<TLClass, List<ResolvedRule>> classRules, Map<TLModule, List<ResolvedRule>> moduleRules,
			Map<TLClass, Map<BoundCommandGroup, Set<BoundedRole>>> passedOn) {
		Map<BoundCommandGroup, Set<BoundedRole>> inheritable = passedOn.get(type);
		if (inheritable != null) {
			return inheritable;
		}

		Map<BoundCommandGroup, Set<BoundedRole>> effective = new HashMap<>();
		inheritable = new HashMap<>();
		for (TLClass generalization : type.getGeneralizations()) {
			Map<BoundCommandGroup, Set<BoundedRole>> inherited =
				computeClassRights(generalization, classRules, moduleRules, passedOn);
			addRights(effective, inherited);
			addRights(inheritable, inherited);
		}
		applyRules(moduleRules.getOrDefault(type.getModule(), Collections.emptyList()), effective, inheritable);
		applyRules(classRules.getOrDefault(type, Collections.emptyList()), effective, inheritable);

		if (!effective.isEmpty()) {
			_expandedClassRights.put(type, effective);
		}
		passedOn.put(type, inheritable);
		return inheritable;
	}

	/**
	 * Adds the roles of the given rights to the roles required for the same operations.
	 */
	private static void addRights(Map<BoundCommandGroup, Set<BoundedRole>> rights,
			Map<BoundCommandGroup, Set<BoundedRole>> added) {
		for (Map.Entry<BoundCommandGroup, Set<BoundedRole>> entry : added.entrySet()) {
			rights.computeIfAbsent(entry.getKey(), unused -> new HashSet<>()).addAll(entry.getValue());
		}
	}

	/**
	 * A configured {@link AccessRule} with its operation and roles resolved against the
	 * application.
	 *
	 * @param operation
	 *        The command group the rule applies to.
	 * @param roles
	 *        The roles the rule adds or removes.
	 * @param inherit
	 *        Whether the rule also applies to the specializations of the configured type.
	 * @param revoke
	 *        Whether the rule removes the {@link #roles()} instead of adding them.
	 */
	private record ResolvedRule(BoundCommandGroup operation, Set<BoundedRole> roles, boolean inherit,
			boolean revoke) {
		// Pure value type without additional behavior.
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
		if (!(tType instanceof TLClass clazz)) {
			// Only TLClass instances carry configured class rights. Objects with a non-class type
			// (e.g. a TLClassifier, whose type is a TLEnumeration) have no class rights.
			return Collections.emptySet();
		}
		return getAllowedRoles(clazz, group);
	}

	/**
	 * @implNote For a type {@link #isWithoutSecurity(TLClass) without security}, the result is the
	 *           configured (typically empty) set of roles, which must not be mistaken for a denial.
	 */
	@Override
	public Set<BoundedRole> getAllowedRoles(TLClass type, BoundCommandGroup commandGroup) {
		return _expandedClassRights
			.getOrDefault(type, Collections.emptyMap())
			.getOrDefault(commandGroup, Collections.emptySet());
	}

	@Override
	public Set<BoundedRole> getAllowedRoles(TLStructuredTypePart attribute, BoundCommandGroup commandGroup) {
		return _typePartRights
			.getOrDefault(attribute.getDefinition(), Collections.emptyMap())
			.getOrDefault(commandGroup, Collections.emptySet());
	}

	@Override
	public boolean isAllowed(Person person, TLObject instance, BoundCommandGroup commandGroup) {
		if (!(instance instanceof BoundObject)) {
			return true;
		}
		if (!isCommitted(instance)) {
			// Roles are computed at commit time; an object created in the current transaction has no
			// computed roles yet, so an instance-level check against it is not meaningful.
			return true;
		}
		Boolean allowedBypass = isAllowedBypass(person, commandGroup);
		if (allowedBypass != null) {
			return allowedBypass;
		}
		if (isWithoutSecurity(instance)) {
			// An object of a type without security is not access controlled.
			return true;
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
		if (!isCommitted(instance)) {
			// See isAllowed(Person, TLObject, BoundCommandGroup): no check on not-yet-committed objects.
			return true;
		}
		Boolean allowedBypass = isAllowedBypass(person, commandGroup);
		if (allowedBypass != null) {
			return allowedBypass;
		}
		if (isWithoutSecurity(instance)) {
			// An object of a type without security is not access controlled, neither on the object,
			// nor on its attribute values.
			return true;
		}
		Set<? extends BoundRole> roles = getRoles(instance, commandGroup);
		boolean allowedOnInstance = accessManager().hasRole(person, (BoundObject) instance, roles);
		if (!allowedOnInstance) {
			return false;
		}
		Map<BoundCommandGroup, Set<BoundedRole>> partRights =
			_typePartRights.getOrDefault(attribute.getDefinition(), Collections.emptyMap());
		if (!partRights.containsKey(commandGroup)) {
			// No attribute-level grant for this operation: the attribute inherits the (passed)
			// class-level decision.
			return true;
		}
		Set<BoundedRole> requiredPartRoles = partRights.get(commandGroup);
		if (requiredPartRoles.isEmpty()) {
			// An attribute-level grant is present but lists no roles: the operation is denied for
			// every role. A role-based user can never satisfy it (only a bypassing super-user, which
			// is already handled above, is unaffected).
			return false;
		}
		return accessManager().hasRole(person, (BoundObject) instance, requiredPartRoles);
	}

	private static Boolean isAllowedBypass(Person person, BoundCommandGroup commandGroup) {
		if (ThreadContext.isSystemContext()) {
			// The current code deliberately runs in a system context (see
			// ThreadContext.inSystemContext), i.e. it explicitly opted out of a user identity. Such
			// code acts on behalf of the system and is granted full access. Note: a system context
			// is only established when no user interaction is active; code running within a
			// logged-in user's interaction is unaffected and stays subject to the regular checks.
			return Boolean.TRUE;
		}

		return BoundChecker.isAllowedBypass(person, commandGroup);
	}

	@Override
	public boolean isAllowedCreate(Person person, TLObject parent, TLStructuredTypePart compositionAttribute) {
		// Condition 1: CREATE right on the created type (the reference's target type) in the parent
		// context.
		TLType targetType = compositionAttribute.getType();
		if (targetType instanceof TLClass && !isAllowedCreate(person, (TLClass) targetType, parent)) {
			return false;
		}
		// Condition 2: WRITE right on the composition reference of the parent.
		return isAllowed(person, parent, compositionAttribute, SimpleBoundCommandGroup.WRITE);
	}

	@Override
	public boolean isAllowedCreate(Person person, TLClass type, TLObject context) {
		Boolean allowedBypass = isAllowedBypass(person, SimpleBoundCommandGroup.CREATE);
		if (allowedBypass != null) {
			return allowedBypass.booleanValue();
		}
		if (isWithoutSecurity(type)) {
			// Objects of a type without security are not access controlled, so everybody may create
			// them.
			return true;
		}
		if (context != null && !isCommitted(context)) {
			// The context is being built in the current transaction (no computed roles yet); its own
			// creation was already authorized, so creating into it is not checked here.
			return true;
		}
		Set<BoundedRole> roles = getAllowedRoles(type, SimpleBoundCommandGroup.CREATE);
		return accessManager().hasRole(person, createContext(context), roles);
	}

	/**
	 * Whether instance-level access checks are meaningful for the given object.
	 *
	 * <p>
	 * Role assignments (in particular rule-derived roles) are computed at commit time. An object
	 * that was created in the current, not-yet-committed transaction therefore has no computed
	 * roles; evaluating instance-level security against it would spuriously deny. Such objects (and
	 * transient objects) are exempt from instance-level checks -- their creation was already gated
	 * by the CREATE check against a committed context.
	 * </p>
	 */
	private static boolean isCommitted(TLObject object) {
		if (object == null || object.tTransient()) {
			return false;
		}
		return object.tHandle().getState() == KnowledgeItem.State.PERSISTENT;
	}

	/**
	 * The {@link BoundObject} on which the CREATE right is checked: the given context if it is a
	 * {@link BoundObject}, otherwise the global security root.
	 */
	private static BoundObject createContext(TLObject context) {
		if (context instanceof BoundObject) {
			return (BoundObject) context;
		}
		return BoundHelper.getInstance().getDefaultObject();
	}

	@Override
	public Set<TLClass> getAccessibleTypes(Person person, BoundCommandGroup commandGroup) {
		Boolean allowedBypass = isAllowedBypass(person, commandGroup);
		if (allowedBypass != null) {
			// The decision does not depend on per-type grants: an administrator may act on every
			// object, so all types of the system are accessible; a restricted user gets none. In
			// particular, types without any configured rights must be included for the "allow all"
			// case, so iterating the configured rights would be wrong here.
			return allowedBypass.booleanValue()
				? TLModelUtil.getAllGlobalClasses(_applicationModel)
				: Collections.emptySet();
		}
		BoundObject securityRoot = BoundHelper.getInstance().getDefaultObject();
		Set<TLClass> result = new HashSet<>();
		for (Map.Entry<TLClass, Map<BoundCommandGroup, Set<BoundedRole>>> entry : _expandedClassRights.entrySet()) {
			Set<BoundedRole> roles = entry.getValue().getOrDefault(commandGroup, Collections.emptySet());
			if (roles.isEmpty()) {
				// Deny-by-default: a type without a grant for the command group is never accessible.
				continue;
			}
			if (accessManager().hasRole(person, securityRoot, roles)) {
				result.add(entry.getKey());
			}
		}
		// A type without security is accessible without any role, no matter whether rights are
		// configured for it.
		result.addAll(_typesWithoutSecurity);
		return result;
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
