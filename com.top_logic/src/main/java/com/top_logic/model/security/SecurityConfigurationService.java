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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NotBothTrue;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.ServiceExtensionPoint;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.AccessControlCategory;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.mode.HideActiveIf;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AccessRightsConfig;
import com.top_logic.model.annotate.security.AccessRevoke;
import com.top_logic.model.annotate.security.AccessRule;
import com.top_logic.model.annotate.security.OperationDeclaration;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.model.config.SingletonMapping;
import com.top_logic.model.form.definition.NamedPartNames;
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
import com.top_logic.tool.boundsec.CommandGroupReference;
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
@ServiceCategory.Of(AccessControlCategory.class)
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
	 * Base configuration of an entry declaring the operations that apply to its objects.
	 *
	 * @see ApplicableOperations
	 */
	@Abstract
	public interface WithOperations extends ConfigurationItem {

		/** Configuration name for {@link #getOperations()}. */
		String OPERATIONS = "operations";

		/**
		 * The operations that apply to the objects of the configured element, in addition to the
		 * {@link ApplicableOperations#BUILT_IN built-in operations}.
		 *
		 * <p>
		 * An operation declared for a {@link TLClass} applies to the class and all its
		 * specializations; one declared for a {@link TLModule} applies to every class of the module
		 * and their specializations; one declared for a module singleton applies to that singleton.
		 * Only an operation that applies may be granted. The declarations of all configuration
		 * layers are appended.
		 * </p>
		 */
		@Name(OPERATIONS)
		@EntryTag(OperationDeclaration.TAG_NAME)
		List<OperationDeclaration> getOperations();

	}

	/**
	 * Base configuration for access rights on all objects of a {@link TLClass}, or of all
	 * {@link TLClass}es of a {@link TLModule}.
	 */
	@Abstract
	public interface TypeBasedAccessRights extends ModelAccessRights, WithOperations {

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
		 * <b>Warning:</b> This option is dangerous: whoever has an object of such a type in reach may
		 * read, modify, create and delete it, no matter which roles the user holds. It is meant as a
		 * temporary measure only, e.g. to keep an application usable after an upgrade, until the
		 * access rights of the type are configured properly. A type that is used by the application
		 * code alone is {@link #isInternal() internal}, not without security.
		 * </p>
		 *
		 * <p>
		 * A specialization of a type without security is without security, too. Declaring a
		 * {@link TLModule} without security therefore excludes every class of that module and all
		 * their specializations from access control.
		 * </p>
		 *
		 * <p>
		 * A type without security is not {@link #isInternal() internal} at the same time: the one
		 * mark opens its objects to every user, the other says no user needs them, so a configuration
		 * setting both is rejected.
		 * </p>
		 */
		@Name(WITHOUT_SECURITY)
		@Constraint(value = NotBothTrue.class, args = { @Ref(INTERNAL) })
		boolean isWithoutSecurity();

		/**
		 * Setter for {@link #isWithoutSecurity()}.
		 */
		void setWithoutSecurity(boolean value);

		/**
		 * Whether objects of the configured types are used by the application's own code only.
		 *
		 * <p>
		 * An internal type is never accessed on behalf of a user: its objects are transient, or
		 * they are read and written in a context that bypasses the access check. No user is
		 * expected to hold a role on them, so an internal type needs neither a grant, nor a role
		 * rule, nor a role parent, and a check of the access definition does not report the
		 * missing ones. An internal type does not delegate to an access parent either. The access check itself is not changed: a user asking for such an object is
		 * denied, since no role is granted.
		 * </p>
		 *
		 * <p>
		 * A specialization of an internal type is internal, too. Declaring a {@link TLModule}
		 * internal therefore marks every class of that module and all their specializations.
		 * </p>
		 */
		@Name(INTERNAL)
		@Constraint(value = NotBothTrue.class, args = { @Ref(WITHOUT_SECURITY) })
		boolean isInternal();

		/**
		 * Setter for {@link #isInternal()}.
		 */
		void setInternal(boolean value);

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
	@DisplayOrder({
		TLClassAccessRights.NAME_ATTRIBUTE,
		TLClassAccessRights.WITHOUT_SECURITY,
		TLClassAccessRights.INTERNAL,
		TLClassAccessRights.ACCESS_PARENT,
		TLClassAccessRights.DISCOVER_ATTRIBUTES,
		TLClassAccessRights.OPERATIONS,
		TLClassAccessRights.GRANTS
	})
	@Label("Class based access rights")
	public static interface TLClassAccessRights extends TypeBasedAccessRights {

		/** Configuration name for {@link #getAccessParent()}. */
		String ACCESS_PARENT = "access-parent";

		/** Configuration name for {@link #getDiscoverAttributes()}. */
		String DISCOVER_ATTRIBUTES = "discover-attributes";

		@Options(fun = AllClasses.class, mapping = TLModelPartMapping.class)
		@Override
		String getName();

		/**
		 * The attributes of the class that a person may read who may discover the object.
		 *
		 * <p>
		 * A discoverable object shows its label only. The discover attributes show additional
		 * properties of it, e.g. in an option list that presents the objects to choose from in a
		 * table. Only reading is extended: writing such an attribute still requires the write right.
		 * When a discover attribute is a reference, a target the person may not discover is still
		 * hidden.
		 * </p>
		 *
		 * <p>
		 * An attribute with a read grant of its own (see {@link TLPartAccessRights}) is decided by
		 * that grant, not by the discover attributes.
		 * </p>
		 *
		 * <p>
		 * The setting applies to the class and its specializations. A specialization with a setting
		 * of its own replaces the setting of its generalizations, an empty setting withdraws all
		 * discover attributes. Different settings of two independent generalizations of a class are
		 * a configuration error, unless the class has a setting of its own.
		 * </p>
		 *
		 * @see SimpleBoundCommandGroup#DISCOVER
		 * @see SimpleBoundCommandGroup#WRITE
		 */
		@Name(DISCOVER_ATTRIBUTES)
		@Format(CommaSeparatedStrings.class)
		@Options(fun = ClassAttributes.class, args = @Ref(NAME_ATTRIBUTE), mapping = NamedPartNames.class)
		List<String> getDiscoverAttributes();

		/**
		 * Setter for {@link #getDiscoverAttributes()}.
		 */
		void setDiscoverAttributes(List<String> value);

		/**
		 * The access parent of the type: the object whose access definition decides access to an
		 * object of the type.
		 * <p>
		 * A type with a delegating access parent has no marks of its own. For every operation the
		 * access parent decides, whether a user may perform it on one of its objects is whether the
		 * user may do the same to the access parent, where creating or deleting one of its objects
		 * is writing the access parent; an object whose relation leads nowhere is denied the
		 * delegated operations, unless the own grants of the type imply them. Without a restriction
		 * to some operations, the access parent decides all of them, and the type has no grants and
		 * no roles of its own. Restricted to some operations, the access parent decides these and
		 * Discover, and the grants of the type together with the roles held on the object itself
		 * decide all other operations.
		 * </p>
		 * <p>
		 * Without a setting the type inherits the setting of its generalizations; without one, a
		 * composition part without a role rule, without a role parent rule and without marks
		 * delegates to its container by default. The specializations of the type inherit the
		 * setting unless they have one of their own.
		 * </p>
		 *
		 * @see ContainerAccessParent
		 * @see TargetAccessParent
		 * @see SelfAccessParent
		 */
		@Name(ACCESS_PARENT)
		@Constraint(value = AccessParentStandsAlone.class, args = { @Ref(GRANTS), @Ref(WITHOUT_SECURITY), @Ref(INTERNAL) })
		@DynamicMode(fun = HideBesideOtherDefinition.class,
			args = { @Ref(ACCESS_PARENT), @Ref(GRANTS), @Ref(WITHOUT_SECURITY), @Ref(INTERNAL) })
		AccessParentConfig getAccessParent();

		/**
		 * Setter for {@link #getAccessParent()}.
		 */
		void setAccessParent(AccessParentConfig value);

		/**
		 * @see HideBesideOtherDefinition The grants are not offered while the type delegates to an
		 *      access parent.
		 */
		@Override
		@DynamicMode(fun = HideBesideOtherDefinition.ForGrants.class,
			args = { @Ref(WITHOUT_SECURITY), @Ref(GRANTS), @Ref({ ACCESS_PARENT, AccessParentConfig.DEFINITION }) })
		List<AccessRule> getGrants();

		/**
		 * @see HideBesideOtherDefinition The mark is not offered while the type delegates to an
		 *      access parent.
		 */
		@Override
		@DynamicMode(fun = HideBesideOtherDefinition.class,
			args = { @Ref(WITHOUT_SECURITY), @Ref({ ACCESS_PARENT, AccessParentConfig.DEFINITION }) })
		boolean isWithoutSecurity();

		/**
		 * @see HideBesideOtherDefinition The mark is not offered while the type delegates to an
		 *      access parent.
		 */
		@Override
		@DynamicMode(fun = HideBesideOtherDefinition.class,
			args = { @Ref(INTERNAL), @Ref({ ACCESS_PARENT, AccessParentConfig.DEFINITION }) })
		boolean isInternal();

	}

	/**
	 * Options for {@link TLClassAccessRights#getDiscoverAttributes()}: the attributes of the class
	 * with the given qualified name, including the inherited ones.
	 *
	 * <p>
	 * A name that does not denote a class - it may still be incomplete while it is edited - has no
	 * attributes.
	 * </p>
	 */
	public static class ClassAttributes extends Function1<Collection<? extends TLStructuredTypePart>, String> {

		@Override
		public Collection<? extends TLStructuredTypePart> apply(String className) {
			if (className == null || className.isEmpty()) {
				return Collections.emptyList();
			}
			TLObject type;
			try {
				type = TLModelUtil.resolveQualifiedName(className);
			} catch (I18NRuntimeException ex) {
				return Collections.emptyList();
			}
			if (!(type instanceof TLClass clazz)) {
				return Collections.emptyList();
			}
			return clazz.getAllParts();
		}

	}

	/**
	 * Configuration of access rights on a module singleton, identified by its qualified name.
	 */
	@TagName("singleton")
	@DisplayOrder({
		TLSingletonAccessRights.NAME_ATTRIBUTE,
		TLSingletonAccessRights.OPERATIONS,
		TLSingletonAccessRights.GRANTS
	})
	@Label("Singleton access rights")
	public static interface TLSingletonAccessRights extends ModelAccessRights, WithOperations {

		@Options(fun = AllSingletons.class, mapping = SingletonMapping.class)
		@Override
		String getName();

	}

	/**
	 * Configuration of access rights to values for a {@link TLStructuredTypePart}. The part is
	 * identified by its qualified name.
	 *
	 * <p>
	 * The rights apply to the attribute of the named type and of all its specializations, no matter
	 * whether the named type defines, overrides or inherits the attribute. For an object, the most
	 * specific entry on the way from its type to its generalizations decides. Entries of two
	 * independent generalizations of a type that grant different rights for the same attribute
	 * are a configuration error, unless the type has an entry of its own for the attribute.
	 * </p>
	 */
	@TagName("part")
	@DisplayOrder({
		TLPartAccessRights.NAME_ATTRIBUTE,
		TLPartAccessRights.GRANTS
	})
	@Label("Attribute value access rights")
	public static interface TLPartAccessRights extends ModelAccessRights {

		@Options(fun = AllAttributes.class, mapping = TLModelPartMapping.class)
		@Override
		String getName();

	}

	/**
	 * Configuration of access rights that are applied to all classes in a {@link TLModule}. The
	 * module is identified by the module name.
	 */
	@TagName("module")
	@DisplayOrder({
		TLModuleAccessRights.NAME_ATTRIBUTE,
		TLModuleAccessRights.WITHOUT_SECURITY,
		TLModuleAccessRights.INTERNAL,
		TLModuleAccessRights.OPERATIONS,
		TLModuleAccessRights.GRANTS
	})
	@Label("Module based access rights")
	public static interface TLModuleAccessRights extends TypeBasedAccessRights {

		@Options(fun = TLModelPartRef.AllModules.class, mapping = TLModelPartMapping.class)
		@Override
		String getName();

	}

	private Map<TLObject, Map<BoundCommandGroup, Set<BoundedRole>>> _singletonRights = new HashMap<>();

	/**
	 * The attribute rights of the {@link TLPartAccessRights part entries}, by the type named in the
	 * entry and the attribute name.
	 *
	 * <p>
	 * The type named in an entry is the type the rights are configured for, no matter whether it
	 * defines, overrides or inherits the attribute.
	 * </p>
	 *
	 * @see TypeEntry#_partRights
	 */
	private final Map<TLStructuredType, Map<String, Map<BoundCommandGroup, Set<BoundedRole>>>> _declaredPartRights =
		new HashMap<>();

	/**
	 * The {@link TLClassAccessRights#getDiscoverAttributes() discover attributes} of the class
	 * entries that set them, by class. An entry setting no attribute withdraws the discover
	 * attributes of the generalizations of its class.
	 *
	 * @see TypeEntry#_discoverAttributes
	 */
	private final Map<TLClass, Set<String>> _declaredDiscoverAttributes = new HashMap<>();

	/**
	 * The rules of the {@link TLModuleAccessRights module entries}, by module.
	 */
	private final Map<TLModule, List<ResolvedRule>> _moduleRules = new HashMap<>();

	/**
	 * The types and modules whose definition excludes them from access control, collected while
	 * the configuration is read, see {@link #markOrigin(TLClass, TypeEntry, Mark)}.
	 */
	private final Set<TLModelPart> _declaredWithoutSecurity = new HashSet<>();

	/**
	 * The types and modules whose definition declares them internal, collected while the
	 * configuration is read, see {@link #markOrigin(TLClass, TypeEntry, Mark)}.
	 */
	private final Set<TLModelPart> _declaredInternal = new HashSet<>();

	/**
	 * The access control settings of the classes indexed at startup: the global classes and the
	 * classes named in the configuration.
	 *
	 * @see #entry(TLClass)
	 */
	private final Map<TLClass, TypeEntry> _globalEntries = new HashMap<>();

	/**
	 * The access control settings of the classes not indexed at startup, computed on first access.
	 *
	 * <p>
	 * Such a class is a class declared locally in an object, which comes into existence with the
	 * object, see {@link #entry(TLClass)}. The keys are held weakly, so that the settings of a
	 * deleted local class are dropped with it. The settings depend on nothing but the configuration
	 * of this service and the model, so they live as long as this service.
	 * </p>
	 */
	private final Map<TLClass, TypeEntry> _localEntries = Collections.synchronizedMap(new WeakHashMap<>());

	/**
	 * Whether the settings of the classes indexed at startup are complete, so that the settings of
	 * another class may be computed from them.
	 */
	private boolean _started;

	/**
	 * The operations applying to the singletons with declarations of their own.
	 *
	 * @see #getObjectOperations(TLObject)
	 */
	private final Map<TLObject, ApplicableOperations> _singletonOperations = new HashMap<>();

	/**
	 * The operations declared for a module, each mapped to the operations declared to imply it.
	 */
	private final Map<TLModule, Map<BoundCommandGroup, Set<BoundCommandGroup>>> _moduleDeclarations =
		new HashMap<>();

	/**
	 * The roles a singleton with operations of its own holds an operation by, because they are
	 * granted an operation implying it, see {@link #getRoles(TLObject, BoundCommandGroup)}.
	 */
	private final Map<TLObject, Map<BoundCommandGroup, Set<BoundedRole>>> _singletonImpliedRights = new HashMap<>();

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

		for (TLClass type : TLModelUtil.getAllGlobalClasses(_applicationModel)) {
			_globalEntries.put(type, new TypeEntry());
		}
		indexContainerTypes();

		Map<ModelAccessRights, TLObject> modelParts = new LinkedHashMap<>();
		Map<TLClass, Map<BoundCommandGroup, Set<BoundCommandGroup>>> classDeclarations = new HashMap<>();
		Map<TLObject, Map<BoundCommandGroup, Set<BoundCommandGroup>>> singletonDeclarations = new HashMap<>();
		for (ModelAccessRights modelConf : config.getSecurityConfig().values()) {
			TLObject modelPart = TLModelUtil.resolveQualifiedName(_applicationModel, modelConf.getName());
			modelParts.put(modelConf, modelPart);
			if (modelConf instanceof TLClassAccessRights conf && modelPart instanceof TLClass clazz) {
				indexEntry(clazz);
				declareOperations(context, conf, classDeclarations.computeIfAbsent(clazz, unused -> new HashMap<>()));
			} else if (modelConf instanceof TLModuleAccessRights conf && modelPart instanceof TLModule module) {
				declareOperations(context, conf, _moduleDeclarations.computeIfAbsent(module, unused -> new HashMap<>()));
			} else if (modelConf instanceof TLSingletonAccessRights conf && modelPart != null) {
				declareOperations(context, conf,
					singletonDeclarations.computeIfAbsent(modelPart, unused -> new HashMap<>()));
			}
		}
		computeOperations(classDeclarations, singletonDeclarations);
		checkImplications(context, classDeclarations, singletonDeclarations);

		Map<TLClass, List<ResolvedRule>> classRules = new HashMap<>();
		Map<TLClass, AccessParentFunction> explicitParents = new HashMap<>();
		for (Map.Entry<ModelAccessRights, TLObject> entry : modelParts.entrySet()) {
			ModelAccessRights modelConf = entry.getKey();
			TLObject modelPart = entry.getValue();
			if (modelConf instanceof TLClassAccessRights conf) {
				handleTLClass(context, modelPart, conf, classRules, explicitParents);
			} else if (modelConf instanceof TLSingletonAccessRights conf) {
				handleTLSingleton(context, modelPart, conf);
			} else if (modelConf instanceof TLPartAccessRights conf) {
				handleTLPart(context, modelPart, conf);
			} else if (modelConf instanceof TLModuleAccessRights conf) {
				handleTLModule(context, modelPart, conf);
			}
		}

		Set<TLClass> types = startupTypes();
		for (TLClass type : types) {
			TypeEntry entry = entry(type);
			markOrigin(type, entry, Mark.WITHOUT_SECURITY);
			markOrigin(type, entry, Mark.INTERNAL);
		}
		for (TLClass type : types) {
			inheritAccessParent(type, entry(type), explicitParents);
		}
		checkInheritedRestriction(context, classRules, explicitParents);
		for (TLClass type : types) {
			computeClassRights(type, entry(type), classRules);
		}
		for (TLClass type : types) {
			computePartRights(type, entry(type), context::error);
		}
		for (TLClass type : types) {
			computeDiscoverAttributes(type, entry(type), context::error);
		}
		for (TLClass type : types) {
			computeDiscoverRoles(type, entry(type));
		}
		for (TLClass type : types) {
			applyImplications(entry(type));
		}
		applySingletonImplications();
		_started = true;
	}

	/**
	 * Indexes the given class at startup, see {@link #_globalEntries}.
	 */
	private void indexEntry(TLClass type) {
		_globalEntries.computeIfAbsent(type, unused -> new TypeEntry());
	}

	/**
	 * The classes indexed at startup, the global classes first.
	 */
	private Set<TLClass> startupTypes() {
		Set<TLClass> result = new LinkedHashSet<>(TLModelUtil.getAllGlobalClasses(_applicationModel));
		result.addAll(_globalEntries.keySet());
		return result;
	}

	/**
	 * The access control settings of the given class.
	 *
	 * <p>
	 * This is the one place resolving a class to its settings. A class indexed at startup has the
	 * settings computed there. A class declared locally in an object has no entry of its own in the
	 * configuration, since it has no qualified name to be named by. It is treated as a global
	 * specialization of its generalizations without a configuration of its own: its settings are
	 * computed on first access by the same computation as the settings of such a class at startup,
	 * and kept in {@link #_localEntries}. A global class created after startup has no settings,
	 * until this service is started again.
	 * </p>
	 */
	private TypeEntry entry(TLClass type) {
		TypeEntry result = _globalEntries.get(type);
		if (result != null) {
			return result;
		}
		result = _localEntries.get(type);
		if (result != null) {
			return result;
		}
		if (!_started) {
			// The settings of a local class are computed from the complete settings of the classes
			// indexed at startup.
			return TypeEntry.none();
		}
		// Computed without holding a lock, since the computation resolves the generalizations of the
		// class, which may be local classes as well. Two threads computing the settings of the same
		// class compute the same settings.
		TypeEntry computed = TLModelUtil.isGlobal(type) ? TypeEntry.none() : computeLocalEntry(type);
		result = _localEntries.putIfAbsent(type, computed);
		return result != null ? result : computed;
	}

	/**
	 * Computes the settings of a class not indexed at startup, see {@link #entry(TLClass)}.
	 *
	 * <p>
	 * The steps are those of the startup in the same order, applied to the given class alone. The
	 * class has no configuration of its own: no operations, no grants, no attribute rights, no marks
	 * and no access parent are declared for it. A composition holding one of its generalizations
	 * holds it as well, so its containers are the containers of its generalizations.
	 * </p>
	 */
	private TypeEntry computeLocalEntry(TLClass type) {
		TypeEntry entry = new TypeEntry();
		mergeOperations(type, entry, Collections.emptyMap());
		markOrigin(type, entry, Mark.WITHOUT_SECURITY);
		markOrigin(type, entry, Mark.INTERNAL);
		inheritAccessParent(type, entry, Collections.emptyMap());
		computeClassRights(type, entry, Collections.emptyMap());
		computePartRights(type, entry, message -> Logger.error(message, SecurityConfigurationService.class));
		computeDiscoverAttributes(type, entry,
			message -> Logger.error(message, SecurityConfigurationService.class));
		computeDiscoverRoles(type, entry);
		applyImplications(entry);
		for (TLClass generalization : type.getGeneralizations()) {
			entry._containerTypes.addAll(entry(generalization)._containerTypes);
		}
		return entry;
	}

	/**
	 * Resolves the operations the given entry declares and enters them into the given
	 * declarations, reporting an operation that does not exist or is
	 * {@link ApplicableOperations#BUILT_IN built-in}.
	 *
	 * @param declarations
	 *        The declarations of the configured element, each operation mapped to the operations
	 *        declared to imply it.
	 */
	private void declareOperations(InstantiationContext context, WithOperations config,
			Map<BoundCommandGroup, Set<BoundCommandGroup>> declarations) {
		String name = ((ModelAccessRights) config).getName();
		for (OperationDeclaration declaration : config.getOperations()) {
			BoundCommandGroup operation = declaration.getName().resolve(_commandGroups);
			if (operation == null) {
				context.error("The command group " + declaration.getName().id() + " declared as operation for "
					+ name + " does not exist.");
				continue;
			}
			if (ApplicableOperations.isBuiltIn(operation)) {
				context.error("The operation " + operation.getID() + " declared for " + name
					+ " is built-in: it applies to every type and cannot be declared.");
				continue;
			}
			Set<BoundCommandGroup> implying = declarations.computeIfAbsent(operation, unused -> new HashSet<>());
			if (declaration.getImpliedBy() != null) {
				BoundCommandGroup impliedBy = declaration.getImpliedBy().resolve(_commandGroups);
				if (impliedBy == null) {
					context.error("The command group " + declaration.getImpliedBy().id() + " implying the operation "
						+ operation.getID() + " for " + name + " does not exist.");
					continue;
				}
				implying.add(impliedBy);
			}
		}
	}

	/**
	 * Computes the operations applying to every class indexed at startup and to every singleton with
	 * declarations of its own.
	 */
	private void computeOperations(Map<TLClass, Map<BoundCommandGroup, Set<BoundCommandGroup>>> classDeclarations,
			Map<TLObject, Map<BoundCommandGroup, Set<BoundCommandGroup>>> singletonDeclarations) {
		for (TLClass type : startupTypes()) {
			mergeOperations(type, entry(type), classDeclarations);
		}
		for (Map.Entry<TLObject, Map<BoundCommandGroup, Set<BoundCommandGroup>>> entry : singletonDeclarations
			.entrySet()) {
			TLObject singleton = entry.getKey();
			Map<BoundCommandGroup, Set<BoundCommandGroup>> declared = new HashMap<>();
			if (singleton.tType() instanceof TLClass type) {
				addDeclarations(declared, mergeOperations(type, entry(type), classDeclarations));
			}
			addDeclarations(declared, entry.getValue());
			_singletonOperations.put(singleton, new ApplicableOperations(declared));
		}
	}

	/**
	 * The operations declared for the given type, its generalizations and their modules.
	 *
	 * <p>
	 * Enters the result and the {@link ApplicableOperations} built from it into the given entry of
	 * the type.
	 * </p>
	 */
	private Map<BoundCommandGroup, Set<BoundCommandGroup>> mergeOperations(TLClass type, TypeEntry entry,
			Map<TLClass, Map<BoundCommandGroup, Set<BoundCommandGroup>>> classDeclarations) {
		Map<BoundCommandGroup, Set<BoundCommandGroup>> result = entry._declaredOperations;
		if (result != null) {
			return result;
		}
		result = new HashMap<>();
		// Guards against a cyclic generalization hierarchy.
		entry._declaredOperations = result;
		for (TLClass generalization : type.getGeneralizations()) {
			addDeclarations(result, mergeOperations(generalization, entry(generalization), classDeclarations));
		}
		addDeclarations(result, _moduleDeclarations.getOrDefault(type.getModule(), Collections.emptyMap()));
		addDeclarations(result, classDeclarations.getOrDefault(type, Collections.emptyMap()));
		if (!result.isEmpty()) {
			entry._operations = new ApplicableOperations(result);
		}
		return result;
	}

	private static void addDeclarations(Map<BoundCommandGroup, Set<BoundCommandGroup>> declarations,
			Map<BoundCommandGroup, Set<BoundCommandGroup>> added) {
		for (Map.Entry<BoundCommandGroup, Set<BoundCommandGroup>> entry : added.entrySet()) {
			declarations.computeIfAbsent(entry.getKey(), unused -> new HashSet<>()).addAll(entry.getValue());
		}
	}

	/**
	 * Reports an operation implying a declared one that does not apply where the declaration is
	 * made, and implications forming a cycle.
	 */
	private void checkImplications(InstantiationContext context,
			Map<TLClass, Map<BoundCommandGroup, Set<BoundCommandGroup>>> classDeclarations,
			Map<TLObject, Map<BoundCommandGroup, Set<BoundCommandGroup>>> singletonDeclarations) {
		for (Map.Entry<TLClass, Map<BoundCommandGroup, Set<BoundCommandGroup>>> entry : classDeclarations.entrySet()) {
			checkImplyingApply(context, TLModelUtil.qualifiedName(entry.getKey()), entry.getValue(),
				getOperations(entry.getKey())::appliesTo);
		}
		for (Map.Entry<TLModule, Map<BoundCommandGroup, Set<BoundCommandGroup>>> entry : _moduleDeclarations
			.entrySet()) {
			checkImplyingApply(context, TLModelUtil.qualifiedName(entry.getKey()), entry.getValue(),
				moduleOperations(entry.getKey()));
		}
		for (Map.Entry<TLObject, Map<BoundCommandGroup, Set<BoundCommandGroup>>> entry : singletonDeclarations
			.entrySet()) {
			checkImplyingApply(context, String.valueOf(entry.getKey()), entry.getValue(),
				getObjectOperations(entry.getKey())::appliesTo);
		}

		Set<BoundCommandGroup> reported = new HashSet<>();
		Map<TLClass, ApplicableOperations> classOperations = new LinkedHashMap<>();
		for (TLClass type : startupTypes()) {
			classOperations.put(type, getOperations(type));
		}
		checkCycles(context, classOperations, reported);
		checkCycles(context, _singletonOperations, reported);
	}

	private static void checkImplyingApply(InstantiationContext context, String name,
			Map<BoundCommandGroup, Set<BoundCommandGroup>> declarations, Predicate<BoundCommandGroup> applies) {
		for (Map.Entry<BoundCommandGroup, Set<BoundCommandGroup>> entry : declarations.entrySet()) {
			for (BoundCommandGroup implying : entry.getValue()) {
				if (!applies.test(implying)) {
					context.error("The operation " + implying.getID() + " implying the operation "
						+ entry.getKey().getID() + " for " + name + " does not apply to " + name + ".");
				}
			}
		}
	}

	private static void checkCycles(InstantiationContext context, Map<?, ApplicableOperations> operations,
			Set<BoundCommandGroup> reported) {
		for (Map.Entry<?, ApplicableOperations> entry : operations.entrySet()) {
			ApplicableOperations applicable = entry.getValue();
			for (BoundCommandGroup operation : applicable.getOperations()) {
				if (applicable.isCyclic(operation) && reported.add(operation)) {
					context.error("The operation " + operation.getID() + " is implied by itself on " + label(entry.getKey())
						+ ": its implied-by declarations form a cycle.");
				}
			}
		}
	}

	private static String label(Object modelPart) {
		return modelPart instanceof TLModelPart part ? TLModelUtil.qualifiedName(part) : String.valueOf(modelPart);
	}

	/**
	 * The operations a grant of a {@link TLModuleAccessRights module entry} may name: the
	 * {@link ApplicableOperations#BUILT_IN built-in operations} and those declared for the module.
	 */
	private Predicate<BoundCommandGroup> moduleOperations(TLModule module) {
		Map<BoundCommandGroup, Set<BoundCommandGroup>> declared =
			_moduleDeclarations.getOrDefault(module, Collections.emptyMap());
		return operation -> ApplicableOperations.isBuiltIn(operation) || declared.containsKey(operation);
	}

	/**
	 * Adds to the rights of the given type the roles holding an operation because they are granted
	 * an operation implying it.
	 */
	private static void applyImplications(TypeEntry entry) {
		Map<BoundCommandGroup, Set<BoundedRole>> implied =
			impliedRights(entry._operations, operation -> entry.roles(operation));
		if (!implied.isEmpty()) {
			Map<BoundCommandGroup, Set<BoundedRole>> rights = new HashMap<>(entry._rights);
			addRights(rights, implied);
			entry._rights = rights;
		}
	}

	/**
	 * Adds to the rights of every singleton with operations of its own the roles holding an
	 * operation because they are granted an operation implying it.
	 */
	private void applySingletonImplications() {
		for (Map.Entry<TLObject, ApplicableOperations> entry : _singletonOperations.entrySet()) {
			TLObject singleton = entry.getKey();
			Map<BoundCommandGroup, Set<BoundedRole>> implied =
				impliedRights(entry.getValue(), operation -> getGrantedRoles(singleton, operation));
			if (!implied.isEmpty()) {
				_singletonImpliedRights.put(singleton, implied);
			}
		}
	}

	/**
	 * The roles holding an operation because they are granted an operation implying it.
	 *
	 * @param roles
	 *        The roles granted an operation, without implications.
	 */
	private static Map<BoundCommandGroup, Set<BoundedRole>> impliedRights(ApplicableOperations operations,
			Function<BoundCommandGroup, Set<BoundedRole>> roles) {
		Map<BoundCommandGroup, Set<BoundedRole>> result = new HashMap<>();
		for (BoundCommandGroup operation : operations.getOperations()) {
			for (BoundCommandGroup implying : operations.getImplying(operation)) {
				if (implying == operation) {
					continue;
				}
				Set<BoundedRole> granted = roles.apply(implying);
				if (!granted.isEmpty()) {
					result.computeIfAbsent(operation, unused -> new HashSet<>()).addAll(granted);
				}
			}
		}
		return result;
	}

	/**
	 * Computes the roles holding {@link SimpleBoundCommandGroup#DISCOVER} on the instances of the
	 * given class: the union of all roles of its class rights, of the rights of its attributes, and
	 * of the {@link SimpleBoundCommandGroup#CREATE} rights of the types creatable in its
	 * compositions.
	 *
	 * <p>
	 * Of the types creatable in a composition, the classes indexed at startup count. A class
	 * declared locally in an object holds the {@link SimpleBoundCommandGroup#CREATE} rights it
	 * inherits, which enter the union through its concrete global generalizations.
	 * </p>
	 *
	 * <p>
	 * For a type whose access parent is restricted to some operations, the rights of its attributes
	 * for a delegated operation do not count: they are checked on the access parent (see
	 * {@link #getRoleHolders(TLObject, BoundCommandGroup)}), so that holding one of their roles on the
	 * object itself admits nothing. Discover is delegated together with these operations.
	 * </p>
	 */
	private void computeDiscoverRoles(TLClass type, TypeEntry entry) {
		AccessParentFunction restriction =
			entry._accessParent != null && !entry._accessParent.delegatesAll() ? entry._accessParent : null;
		Set<BoundedRole> roles = new HashSet<>();
		for (Map<BoundCommandGroup, Set<BoundedRole>> partRights : entry._partRights.values()) {
			for (Map.Entry<BoundCommandGroup, Set<BoundedRole>> right : partRights.entrySet()) {
				if (restriction != null && restriction.delegates(right.getKey())) {
					continue;
				}
				roles.addAll(right.getValue());
			}
		}
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (part instanceof TLReference reference && reference.isComposite()
				&& reference.getType() instanceof TLClass target) {
				for (TLClass created : TLModelUtil.getConcreteSpecializations(target)) {
					TypeEntry createdEntry = _globalEntries.get(created);
					if (createdEntry != null) {
						roles.addAll(createdEntry.roles(SimpleBoundCommandGroup.CREATE));
					}
				}
			}
		}
		if (!roles.isEmpty()) {
			entry._discoverRolesWithoutClassRights = new HashSet<>(roles);
		}
		entry._rights.values().forEach(roles::addAll);
		if (!roles.isEmpty()) {
			entry._discoverRoles = roles;
		}
	}

	/**
	 * Indexes every class indexed at startup whose objects a composition holds to the global classes
	 * owning such a composition.
	 * <p>
	 * A composition holding a type holds its specializations as well, so each of them is indexed.
	 * </p>
	 */
	private void indexContainerTypes() {
		for (TLClass owner : TLModelUtil.getAllGlobalClasses(_applicationModel)) {
			for (TLStructuredTypePart part : owner.getLocalParts()) {
				if (part instanceof TLReference reference && reference.isComposite()
					&& reference.getType() instanceof TLClass target) {
					for (TLClass contained : TLModelUtil.getReflexiveTransitiveSpecializations(target)) {
						TypeEntry entry = _globalEntries.get(contained);
						if (entry != null) {
							entry._containerTypes.add(owner);
						}
					}
				}
			}
		}
	}

	/**
	 * Resolves the access parent of the given type into its entry: the one configured for it, or
	 * else the one its generalizations pass on.
	 * 
	 * @return Whether the type or one of its generalizations has a setting.
	 */
	private boolean inheritAccessParent(TLClass type, TypeEntry entry,
			Map<TLClass, AccessParentFunction> explicitParents) {
		if (entry._accessParentResolved) {
			return entry._hasAccessParent;
		}
		entry._accessParentResolved = true;
		if (explicitParents.containsKey(type)) {
			entry._hasAccessParent = true;
			entry._accessParent = explicitParents.get(type);
			return true;
		}
		for (TLClass generalization : type.getGeneralizations()) {
			TypeEntry generalizationEntry = entry(generalization);
			if (inheritAccessParent(generalization, generalizationEntry, explicitParents)) {
				entry._hasAccessParent = true;
				entry._accessParent = generalizationEntry._accessParent;
				return true;
			}
		}
		return false;
	}

	/**
	 * Reports a grant of a type for an operation that the access parent it inherits from a
	 * generalization decides.
	 *
	 * <p>
	 * A type with an access parent of its own is checked when its definition is read, see
	 * {@link #checkOwnDefinition(InstantiationContext, TLClassAccessRights, AccessParentFunction)}.
	 * A grant inherited from a generalization is no definition of the type and is dropped silently,
	 * see {@link #computeClassRights(TLClass, TypeEntry, Map)}.
	 * </p>
	 */
	private void checkInheritedRestriction(InstantiationContext context, Map<TLClass, List<ResolvedRule>> classRules,
			Map<TLClass, AccessParentFunction> explicitParents) {
		for (Map.Entry<TLClass, List<ResolvedRule>> rules : classRules.entrySet()) {
			TLClass type = rules.getKey();
			if (explicitParents.containsKey(type)) {
				continue;
			}
			AccessParentFunction parent = entry(type)._accessParent;
			if (parent == null || parent.delegatesAll()) {
				continue;
			}
			for (ResolvedRule rule : rules.getValue()) {
				if (parent.delegates(rule.operation())) {
					context.error("The type " + TLModelUtil.qualifiedName(type) + " delegates the operation "
						+ rule.operation().getID()
						+ " to the access parent it inherits and therefore must neither grant nor revoke it.");
				}
			}
		}
	}

	private void handleTLModule(InstantiationContext context, TLObject part, TLModuleAccessRights config) {
		if (!(part instanceof TLModule module)) {
			context.error("The configured part " + part + " is not a module.");
			return;
		}
		if (config.isWithoutSecurity()) {
			_declaredWithoutSecurity.add(module);
		}
		if (config.isInternal()) {
			_declaredInternal.add(module);
		}
		_moduleRules.computeIfAbsent(module, unused -> new ArrayList<>())
			.addAll(resolveRules(context, config, moduleOperations(module)));
	}

	/**
	 * Enters the rights of a {@link TLPartAccessRights part entry} into {@link #_declaredPartRights}
	 * under the type named in the entry, which may define, override or only inherit the attribute.
	 */
	private void handleTLPart(InstantiationContext context, TLObject part, TLPartAccessRights config) {
		if (!(part instanceof TLStructuredTypePart typePart)) {
			context.error("The configured part " + part + " is not a structured type part.");
			return;
		}
		String qualifiedName = config.getName();
		String typeName =
			qualifiedName.substring(0, qualifiedName.lastIndexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR));
		// The part was resolved in this type, which is therefore a structured type.
		TLStructuredType type = (TLStructuredType) TLModelUtil.findType(_applicationModel, typeName);
		Predicate<BoundCommandGroup> applies = type instanceof TLClass clazz
			? getOperations(clazz)::appliesTo
			: ApplicableOperations::isBuiltIn;
		applyRules(resolveRules(context, config, applies),
			_declaredPartRights.computeIfAbsent(type, unused -> new HashMap<>())
				.computeIfAbsent(typePart.getName(), unused -> new HashMap<>()));
	}

	/**
	 * Computes the attribute rights in effect for the given class, see {@link TypeEntry#_partRights}.
	 *
	 * <p>
	 * Attribute rights that contradict each other, because two independent generalizations of a
	 * class pass on different rights for the same attribute and the class has no rights of its own
	 * for it, are reported as configuration error. The attribute then has no attribute rights.
	 * </p>
	 *
	 * @param error
	 *        Reports a configuration error.
	 */
	private void computePartRights(TLClass type, TypeEntry entry, Consumer<String> error) {
		if (_declaredPartRights.isEmpty()) {
			return;
		}
		Map<String, Map<BoundCommandGroup, Set<BoundedRole>>> rights = new HashMap<>();
		for (Map.Entry<String, Set<TLClass>> origin : partRightOrigins(type, entry).entrySet()) {
			String name = origin.getKey();
			Set<TLClass> sources = origin.getValue();
			if (contradicts(sources, name)) {
				if (!inheritsContradiction(type, name)) {
					error.accept("The attribute rights of " + TLModelUtil.qualifiedName(type)
						+ TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + name
						+ " are ambiguous: its generalizations "
						+ sources.stream().map(TLModelUtil::qualifiedName).sorted().toList()
						+ " define different rights for the attribute. Configure the attribute rights for "
						+ TLModelUtil.qualifiedName(type) + " itself.");
				}
				continue;
			}
			rights.put(name, _declaredPartRights.get(sources.iterator().next()).get(name));
		}
		entry._partRights = rights;
	}

	/**
	 * Whether the given types configure different attribute rights for the attribute with the given
	 * name.
	 */
	private boolean contradicts(Set<TLClass> sources, String name) {
		Map<BoundCommandGroup, Set<BoundedRole>> rights = null;
		for (TLClass source : sources) {
			Map<BoundCommandGroup, Set<BoundedRole>> sourceRights = _declaredPartRights.get(source).get(name);
			if (rights == null) {
				rights = sourceRights;
			} else if (!rights.equals(sourceRights)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether a generalization of the given type already has contradicting attribute rights for
	 * the given attribute, so that the contradiction is reported there.
	 */
	private boolean inheritsContradiction(TLClass type, String name) {
		for (TLClass generalization : type.getGeneralizations()) {
			TypeEntry generalizationEntry = entry(generalization);
			Set<TLClass> sources = partRightOrigins(generalization, generalizationEntry).get(name);
			if (sources != null && contradicts(sources, name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The types whose {@link #_declaredPartRights} are in effect for the given type, by attribute
	 * name, entered into the given entry of the type.
	 *
	 * <p>
	 * For an attribute the type has rights for itself, this is the type alone. Otherwise, the types
	 * the generalizations of the type pass on, without those that are a generalization of another
	 * one of them, since the rights of the more specific type win. More than one type remains only
	 * if independent generalizations configure rights for the attribute.
	 * </p>
	 */
	private Map<String, Set<TLClass>> partRightOrigins(TLClass type, TypeEntry typeEntry) {
		Map<String, Set<TLClass>> result = typeEntry._partRightOrigins;
		if (result != null) {
			return result;
		}
		result = new HashMap<>();
		for (TLClass generalization : type.getGeneralizations()) {
			for (Map.Entry<String, Set<TLClass>> entry : partRightOrigins(generalization, entry(generalization))
				.entrySet()) {
				result.computeIfAbsent(entry.getKey(), unused -> new LinkedHashSet<>()).addAll(entry.getValue());
			}
		}
		for (Set<TLClass> sources : result.values()) {
			if (sources.size() > 1) {
				List<TLClass> candidates = new ArrayList<>(sources);
				sources.removeIf(source -> candidates.stream()
					.anyMatch(other -> other != source && TLModelUtil.isGeneralization(source, other)));
			}
		}
		for (String name : _declaredPartRights.getOrDefault(type, Collections.emptyMap()).keySet()) {
			result.put(name, Collections.singleton(type));
		}
		typeEntry._partRightOrigins = result;
		return result;
	}

	/**
	 * The attribute rights in effect for the attribute with the given name on objects of the given
	 * type, see {@link TypeEntry#_partRights}.
	 *
	 * @return The roles granted each operation; empty when no attribute rights apply.
	 */
	private Map<BoundCommandGroup, Set<BoundedRole>> partRights(TLStructuredType type, String name) {
		Map<String, Map<BoundCommandGroup, Set<BoundedRole>>> rights = type instanceof TLClass clazz
			? entry(clazz)._partRights
			: _declaredPartRights.get(type);
		if (rights == null) {
			return Collections.emptyMap();
		}
		return rights.getOrDefault(name, Collections.emptyMap());
	}

	private void handleTLSingleton(InstantiationContext context, TLObject singleton, TLSingletonAccessRights config) {
		// Ensure it is really a singleton.
		Collection<TLModuleSingleton> singletons = singleton.tType().getModule().getSingletons();
		if (!singletons.stream().map(TLModuleSingleton::getSingleton).anyMatch(singleton::equals)) {
			context.error("The configured singleton " + singleton + " is not a singleton.");
			return;
		}
		applyRules(resolveRules(context, config, getObjectOperations(singleton)::appliesTo),
			_singletonRights.computeIfAbsent(singleton, unused -> new HashMap<>()));
	}

	private void handleTLClass(InstantiationContext context, TLObject part, TLClassAccessRights config,
			Map<TLClass, List<ResolvedRule>> classRules, Map<TLClass, AccessParentFunction> explicitParents) {
		if (!(part instanceof TLClass clazz)) {
			context.error("The configured part " + part + " is not a TLClass.");
			return;
		}
		if (config.isWithoutSecurity()) {
			_declaredWithoutSecurity.add(clazz);
		}
		if (config.isInternal()) {
			_declaredInternal.add(clazz);
		}
		AccessParentConfig accessParent = config.getAccessParent();
		if (accessParent != null) {
			AccessParentFunction function = resolveAccessParent(context, clazz, accessParent, explicitParents);
			if (AccessParentConfig.delegates(accessParent)) {
				checkOwnDefinition(context, config, function);
			}
		}
		if (config.valueSet(config.descriptor().getProperty(TLClassAccessRights.DISCOVER_ATTRIBUTES))) {
			Set<String> discoverAttributes = new HashSet<>();
			for (String name : config.getDiscoverAttributes()) {
				if (clazz.getPart(name) == null) {
					context.error("The class " + config.getName() + " has no attribute " + name
						+ " declared as discover attribute.");
					continue;
				}
				discoverAttributes.add(name);
			}
			_declaredDiscoverAttributes.put(clazz, Collections.unmodifiableSet(discoverAttributes));
		}
		classRules.computeIfAbsent(clazz, unused -> new ArrayList<>())
			.addAll(resolveRules(context, config, getOperations(clazz)::appliesTo));
	}

	/**
	 * Computes the discover attributes in effect for the given class, see
	 * {@link TypeEntry#_discoverAttributes}.
	 *
	 * <p>
	 * Discover attributes that contradict each other, because two independent generalizations of a
	 * class pass on different settings and the class has no setting of its own, are reported as
	 * configuration error. The class then has no discover attributes.
	 * </p>
	 *
	 * @param error
	 *        Reports a configuration error.
	 */
	private void computeDiscoverAttributes(TLClass type, TypeEntry entry, Consumer<String> error) {
		if (_declaredDiscoverAttributes.isEmpty()) {
			return;
		}
		Set<TLClass> sources = discoverAttributeOrigins(type, entry);
		if (sources.isEmpty()) {
			return;
		}
		if (contradictingDiscoverAttributes(sources)) {
			if (!inheritsDiscoverAttributeContradiction(type)) {
				error.accept("The discover attributes of " + TLModelUtil.qualifiedName(type)
					+ " are ambiguous: its generalizations "
					+ sources.stream().map(TLModelUtil::qualifiedName).sorted().toList()
					+ " define different discover attributes. Configure "
					+ TLClassAccessRights.DISCOVER_ATTRIBUTES + " for " + TLModelUtil.qualifiedName(type)
					+ " itself.");
			}
			return;
		}
		entry._discoverAttributes = _declaredDiscoverAttributes.get(sources.iterator().next());
	}

	/**
	 * Whether the given types declare different discover attributes.
	 */
	private boolean contradictingDiscoverAttributes(Set<TLClass> sources) {
		return sources.stream().map(_declaredDiscoverAttributes::get).distinct().count() > 1;
	}

	/**
	 * Whether a generalization of the given type already has contradicting discover attributes, so
	 * that the contradiction is reported there.
	 */
	private boolean inheritsDiscoverAttributeContradiction(TLClass type) {
		for (TLClass generalization : type.getGeneralizations()) {
			if (contradictingDiscoverAttributes(discoverAttributeOrigins(generalization, entry(generalization)))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The types whose {@link #_declaredDiscoverAttributes} are in effect for the given type,
	 * entered into the given entry of the type.
	 *
	 * <p>
	 * For a type with a setting of its own, this is the type alone. Otherwise, the types the
	 * generalizations of the type pass on, without those that are a generalization of another one
	 * of them, since the setting of the more specific type wins. More than one type remains only
	 * if independent generalizations have a setting.
	 * </p>
	 */
	private Set<TLClass> discoverAttributeOrigins(TLClass type, TypeEntry typeEntry) {
		Set<TLClass> result = typeEntry._discoverAttributeOrigins;
		if (result != null) {
			return result;
		}
		if (_declaredDiscoverAttributes.containsKey(type)) {
			result = Collections.singleton(type);
		} else {
			result = new LinkedHashSet<>();
			for (TLClass generalization : type.getGeneralizations()) {
				result.addAll(discoverAttributeOrigins(generalization, entry(generalization)));
			}
			if (result.size() > 1) {
				List<TLClass> candidates = new ArrayList<>(result);
				result.removeIf(source -> candidates.stream()
					.anyMatch(other -> other != source && TLModelUtil.isGeneralization(source, other)));
			}
		}
		typeEntry._discoverAttributeOrigins = result;
		return result;
	}

	@Override
	public Set<String> getDiscoverAttributes(TLClass type) {
		return entry(type)._discoverAttributes;
	}

	/**
	 * Resolves the configured access parent of the given type and enters it into the given map.
	 * <p>
	 * A type {@link SelfAccessParent deciding for itself} is entered as <code>null</code>, which also
	 * switches off the default of a composition part and an access parent inherited from a
	 * generalization. A definition restricted to some operations is entered as
	 * {@link RestrictedRelation}. An unusable setting is reported and not entered.
	 * </p>
	 *
	 * @return The entered relation, <code>null</code> for a type deciding for itself and for an
	 *         unusable setting.
	 */
	private AccessParentFunction resolveAccessParent(InstantiationContext context, TLClass type,
			AccessParentConfig config, Map<TLClass, AccessParentFunction> explicitParents) {
		if (!AccessParentConfig.delegates(config)) {
			explicitParents.put(type, null);
			return null;
		}
		AccessParentFunction function = resolveAccessParentDefinition(context, type, config.getDefinition());
		if (function == null) {
			return null;
		}
		explicitParents.put(type, function);
		return function;
	}

	/**
	 * Resolves the given access parent definition of the given type, the definition configured for
	 * the type as well as a candidate of a combining definition like {@link AnyOfAccessParent}.
	 *
	 * @return The relation of the definition, a {@link RestrictedRelation} when it is restricted to
	 *         some operations; <code>null</code> for an unusable definition, which is reported,
	 *         {@link SelfAccessParent} as candidate included.
	 *
	 * @see AccessParentDefinition.CandidateResolver
	 */
	private AccessParentFunction resolveAccessParentDefinition(InstantiationContext context, TLClass type,
			PolymorphicConfiguration<? extends AccessParentDefinition> config) {
		if (config instanceof SelfAccessParent.Config) {
			// The setting of a type deciding for itself is handled before, so this is a candidate of a
			// combining definition.
			context.error("The access parent of " + TLModelUtil.qualifiedName(type)
				+ " names <self/> as a candidate: a type deciding for itself has no candidates.");
			return null;
		}
		AccessParentDefinition definition = context.getInstance(config);
		if (definition == null) {
			return null;
		}
		AccessParentFunction function = definition.resolveWith(context, type, this::resolveAccessParentDefinition);
		if (function == null) {
			return null;
		}
		if (config instanceof OperationRestriction restriction && !restriction.getOperations().isEmpty()) {
			Set<BoundCommandGroup> operations = resolveDelegatedOperations(context, type, restriction);
			if (operations == null) {
				return null;
			}
			for (BoundCommandGroup operation : operations) {
				if (!function.delegates(operation)) {
					// E.g. Create for a referer: nothing refers to an object being created.
					context.error("The access parent of " + TLModelUtil.qualifiedName(type) + " lists the operation "
						+ operation.getID() + ", which it never decides: the grants of the type decide it.");
					return null;
				}
			}
			function = new RestrictedRelation(function, Set.copyOf(operations));
		}
		return function;
	}

	/**
	 * Resolves the operations an access parent is restricted to, reporting a configuration error for
	 * an operation that does not exist or does not apply to the type.
	 *
	 * @return The delegated operations, <code>null</code> when one of them is unusable.
	 */
	private Set<BoundCommandGroup> resolveDelegatedOperations(InstantiationContext context, TLClass type,
			OperationRestriction restriction) {
		String typeName = TLModelUtil.qualifiedName(type);
		ApplicableOperations applicable = getOperations(type);
		Set<BoundCommandGroup> result = new HashSet<>();
		boolean valid = true;
		for (CommandGroupReference reference : restriction.getOperations()) {
			BoundCommandGroup operation = reference.resolve(_commandGroups);
			if (operation == null) {
				context.error("The command group " + reference.id() + " in the access parent of " + typeName
					+ " does not exist.");
				valid = false;
				continue;
			}
			if (!applicable.appliesTo(operation)) {
				context.error("The operation " + operation.getID() + " in the access parent of " + typeName
					+ " does not apply there: declare it with <" + OperationDeclaration.TAG_NAME + " "
					+ OperationDeclaration.NAME + "=\"" + operation.getID() + "\"/> in the <"
					+ WithOperations.OPERATIONS + "> of the type, of a generalization or of the module.");
				valid = false;
				continue;
			}
			result.add(operation);
		}
		return valid ? result : null;
	}

	/**
	 * Reports the own definition of a type with a delegating access parent that the access parent
	 * takes over: a mark, and a grant of an operation the access parent decides.
	 *
	 * @param function
	 *        The resolved access parent, <code>null</code> when the setting is unusable, which is
	 *        reported already.
	 */
	private void checkOwnDefinition(InstantiationContext context, TLClassAccessRights config,
			AccessParentFunction function) {
		if (!AccessParentConfig.isRestricted(config.getAccessParent())) {
			if (!config.getGrants().isEmpty() || config.isWithoutSecurity() || config.isInternal()) {
				context.error("The type " + config.getName()
					+ " has an access parent and therefore must have neither grants nor marks of its own.");
			}
			return;
		}
		if (config.isWithoutSecurity() || config.isInternal()) {
			context.error("The type " + config.getName()
				+ " has an access parent and therefore must not be marked internal or without security.");
		}
		if (function == null) {
			return;
		}
		for (AccessRule rule : config.getGrants()) {
			BoundCommandGroup operation = rule.getOperation().resolve(_commandGroups);
			if (operation != null && function.delegates(operation)) {
				context.error("The type " + config.getName() + " delegates the operation " + operation.getID()
					+ " to its access parent and therefore must neither grant nor revoke it.");
			}
		}
	}

	/**
	 * The part whose definition declares the given mark of the given type, entered into the given
	 * entry of the type.
	 *
	 * <p>
	 * A type is marked when its own definition declares the mark, when one of its generalizations
	 * is marked, or when the definition of its module declares it. The part reported is decided in
	 * this order, so that it does not depend on the order of the configuration: the type itself,
	 * whose mark is the one to drop for the type alone; the part a marked generalization reports,
	 * the generalizations taken in their declared order; the module of the type.
	 * </p>
	 *
	 * @see TypeBasedAccessRights#isWithoutSecurity()
	 * @see TypeBasedAccessRights#isInternal()
	 */
	private Optional<TLModelPart> markOrigin(TLClass type, TypeEntry entry, Mark mark) {
		Optional<TLModelPart> known = mark.get(entry);
		if (known != null) {
			return known;
		}
		Set<TLModelPart> declared = mark == Mark.WITHOUT_SECURITY ? _declaredWithoutSecurity : _declaredInternal;
		Optional<TLModelPart> result = Optional.empty();
		if (declared.contains(type)) {
			result = Optional.of(type);
		} else {
			for (TLClass generalization : type.getGeneralizations()) {
				result = markOrigin(generalization, entry(generalization), mark);
				if (result.isPresent()) {
					break;
				}
			}
			if (result.isEmpty() && declared.contains(type.getModule())) {
				result = Optional.of(type.getModule());
			}
		}
		mark.set(entry, result);
		return result;
	}

	@Override
	public boolean isInternal(TLClass type) {
		return entry(type)._internal.isPresent();
	}

	@Override
	public TLModelPart getInternalOrigin(TLClass type) {
		return entry(type)._internal.orElse(null);
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
		return entry(type)._withoutSecurity.isPresent();
	}

	@Override
	public TLModelPart getWithoutSecurityOrigin(TLClass type) {
		return entry(type)._withoutSecurity.orElse(null);
	}

	/**
	 * @implNote The configured access parents are resolved once at startup, the default of a
	 *           composition part is decided per call: whether a rule delivers a role on the type is
	 *           answered by the {@link AccessManager}, which is loaded after this service and may be
	 *           reloaded independently of it.
	 */
	@Override
	public AccessParentFunction getAccessParent(TLClass type) {
		TypeEntry entry = entry(type);
		if (entry._hasAccessParent) {
			return entry._accessParent;
		}
		if (!entry._containerTypes.isEmpty() && entry._withoutSecurity.isEmpty() && entry._internal.isEmpty()
			&& !accessManager().hasRoleSource(type)) {
			return ContainerRelation.DEFAULT;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implNote The container, whichever composition holds the object, is followed to the types
	 *           owning a composition of the type, which this service indexes. For an
	 *           {@link AnyOfRelation}, the types of all its candidates are joined, see
	 *           {@link AnyOfRelation#getParentTypes(Function)}, a container
	 *           candidate contributing the indexed types as well.
	 */
	@Override
	public Set<TLClass> getAccessParentTypes(TLClass type) {
		AccessParentFunction parent = getAccessParent(type);
		if (parent == null) {
			return Collections.emptySet();
		}
		if (parent instanceof AnyOfRelation anyOf) {
			return anyOf.getParentTypes(candidate -> parentTypes(type, candidate));
		}
		return parentTypes(type, parent);
	}

	/**
	 * The types the given relation may lead to from an object of the given type.
	 *
	 * @return <code>null</code> when they are not known statically.
	 */
	private Set<TLClass> parentTypes(TLClass type, AccessParentFunction relation) {
		if (relation.getRelation() instanceof ContainerRelation container && container.composition() == null) {
			return Collections.unmodifiableSet(entry(type)._containerTypes);
		}
		return relation.getParentTypes(type);
	}

	/**
	 * Resolves the operation and the roles of each configured rule against the application,
	 * reporting a configuration error for an operation or role that does not exist, and for an
	 * operation that does not apply to the configured element.
	 *
	 * @param applies
	 *        Whether an operation applies to the configured element, see
	 *        {@link #getOperations(TLClass)}.
	 */
	private List<ResolvedRule> resolveRules(InstantiationContext context, ModelAccessRights config,
			Predicate<BoundCommandGroup> applies) {
		KnowledgeBase kb = _applicationModel.tKnowledgeBase();
		List<ResolvedRule> result = new ArrayList<>();
		for (AccessRule rule : config.getGrants()) {
			BoundCommandGroup operation = rule.getOperation().resolve(_commandGroups);
			if (operation == null) {
				context.error("The command group " + rule.getOperation().id() + " in configuration for "
						+ config.getName() + " does not exist.");
				continue;
			}
			if (!applies.test(operation)) {
				context.error("The operation " + operation.getID() + " in configuration for " + config.getName()
					+ " does not apply there: declare it with <" + OperationDeclaration.TAG_NAME + " "
					+ OperationDeclaration.NAME + "=\"" + operation.getID() + "\"/> in the <"
					+ WithOperations.OPERATIONS + "> of the type, of a generalization or of the module.");
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
	 * Computes the rights of the given type into its entry and returns the rights it passes on to
	 * its specializations. The rights of its generalizations are computed first.
	 *
	 * @implNote The rights a type requires start out as the union of the rights all its
	 *           generalizations pass on. On top of that, the rules of the type's module are applied
	 *           as an additive baseline, followed by the rules configured for the type itself. A
	 *           rule marked as inherited adjusts the passed on rights as well. A type whose access
	 *           parent is {@link AccessParentFunction#getOperations() restricted} to some operations
	 *           keeps only the rights of the other operations, the rights it passes on are not
	 *           restricted. The access parent of the type must therefore be resolved before.
	 */
	private Map<BoundCommandGroup, Set<BoundedRole>> computeClassRights(TLClass type, TypeEntry entry,
			Map<TLClass, List<ResolvedRule>> classRules) {
		Map<BoundCommandGroup, Set<BoundedRole>> inheritable = entry._passedOnRights;
		if (inheritable != null) {
			return inheritable;
		}

		Map<BoundCommandGroup, Set<BoundedRole>> effective = new HashMap<>();
		inheritable = new HashMap<>();
		for (TLClass generalization : type.getGeneralizations()) {
			Map<BoundCommandGroup, Set<BoundedRole>> inherited =
				computeClassRights(generalization, entry(generalization), classRules);
			addRights(effective, inherited);
			addRights(inheritable, inherited);
		}
		applyRules(_moduleRules.getOrDefault(type.getModule(), Collections.emptyList()), effective, inheritable);
		Map<BoundCommandGroup, Set<BoundedRole>> inherited = new HashMap<>();
		addRights(inherited, effective);
		applyRules(classRules.getOrDefault(type, Collections.emptyList()), effective, inheritable);

		AccessParentFunction parent = entry._accessParent;
		if (parent != null && !parent.delegatesAll()) {
			// The access parent decides the delegated operations, the type the others.
			effective.keySet().removeIf(parent::delegates);
			inherited.keySet().removeIf(parent::delegates);
		}
		entry._inheritedRights = inherited;
		entry._rights = effective;
		entry._passedOnRights = inheritable;
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
	 *
	 * <p>
	 * A singleton with operations of its own additionally admits the roles granted an operation
	 * implying the given one.
	 * </p>
	 */
	private Set<BoundedRole> getRoles(TLObject object, BoundCommandGroup group) {
		Set<BoundedRole> implied = _singletonImpliedRights.getOrDefault(object, Collections.emptyMap()).get(group);
		if (implied == null) {
			return getGrantedRoles(object, group);
		}
		Set<BoundedRole> result = new HashSet<>(getGrantedRoles(object, group));
		result.addAll(implied);
		return result;
	}

	/**
	 * The roles granted the given command group on the given object, without the implications of
	 * the operations of a singleton.
	 */
	private Set<BoundedRole> getGrantedRoles(TLObject object, BoundCommandGroup group) {
		Map<BoundCommandGroup, Set<BoundedRole>> singletonRights =
			_singletonRights.getOrDefault(object, Collections.emptyMap());
		if (!singletonRights.isEmpty()) {
			// Singleton!
			if (SimpleBoundCommandGroup.DISCOVER.equals(group)) {
				// The rights of the singleton replace the class rights, but not the rights on its
				// attributes and compositions, which imply Discover as well.
				Set<BoundedRole> all = new HashSet<>();
				singletonRights.values().forEach(all::addAll);
				if (object.tType() instanceof TLClass clazz) {
					all.addAll(entry(clazz)._discoverRolesWithoutClassRights);
				}
				return all;
			}
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

	@Override
	public ApplicableOperations getOperations(TLClass type) {
		return entry(type)._operations;
	}

	@Override
	public ApplicableOperations getObjectOperations(TLObject object) {
		ApplicableOperations singletonOperations = _singletonOperations.get(object);
		if (singletonOperations != null) {
			return singletonOperations;
		}
		return object.tType() instanceof TLClass type ? getOperations(type) : ApplicableOperations.BUILT_IN_ONLY;
	}

	/**
	 * @implNote For a type {@link #isWithoutSecurity(TLClass) without security}, the result is the
	 *           configured (typically empty) set of roles, which must not be mistaken for a denial.
	 *           The roles granted an operation implying the given one on the type are included.
	 */
	@Override
	public Set<BoundedRole> getAllowedRoles(TLClass type, BoundCommandGroup commandGroup) {
		return entry(type).roles(commandGroup);
	}

	@Override
	public Set<BoundedRole> getInheritedRoles(TLClass type, BoundCommandGroup commandGroup) {
		return entry(type)._inheritedRights.getOrDefault(commandGroup, Collections.emptySet());
	}

	@Override
	public Set<BoundedRole> getAllowedRoles(TLStructuredTypePart attribute, BoundCommandGroup commandGroup) {
		return partRights(attribute.getOwner(), attribute.getName()).getOrDefault(commandGroup, Collections.emptySet());
	}

	@Override
	public Set<BoundedRole> getAllowedRoles(TLClass type, TLStructuredTypePart attribute,
			BoundCommandGroup commandGroup) {
		return partRights(type, attribute.getName()).getOrDefault(commandGroup, Collections.emptySet());
	}

	@Override
	public boolean hasGrant(TLClass type, TLStructuredTypePart attribute, BoundCommandGroup commandGroup) {
		return partRights(type, attribute.getName()).containsKey(commandGroup);
	}

	@Override
	public boolean hasSingletonRights(TLClass type) {
		for (Map.Entry<TLObject, Map<BoundCommandGroup, Set<BoundedRole>>> entry : _singletonRights.entrySet()) {
			if (entry.getValue().isEmpty()) {
				// Without rights, the singleton is decided by its class rights, see getRoles().
				continue;
			}
			if (entry.getKey().tType() instanceof TLClass singletonType
				&& TLModelUtil.isGeneralization(type, singletonType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasGrant(TLStructuredTypePart attribute, BoundCommandGroup commandGroup) {
		TLStructuredType owner = attribute.getOwner();
		if (!(owner instanceof TLClass ownerClass)) {
			return partRights(owner, attribute.getName()).containsKey(commandGroup);
		}
		for (TLClass type : TLModelUtil.getReflexiveTransitiveSpecializations(ownerClass)) {
			if (hasGrant(type, attribute, commandGroup)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isAllowedInitial(Person person, TLObject draft, TLStructuredTypePart attribute,
			BoundCommandGroup commandGroup) {
		if (!(draft.tType() instanceof TLClass type) || isWithoutSecurity(type)) {
			// Objects of a type without security are not access controlled, neither on the object,
			// nor on its attribute values.
			return true;
		}
		Set<BoundedRole> requiredPartRoles = partRights(type, attribute.getName()).get(commandGroup);
		if (requiredPartRoles == null) {
			// No attribute-level grant: the attribute is not restricted beyond the object. The right
			// to create the object covers its initial values, independent of the person (a transient
			// input object can be used without a person at all).
			return true;
		}
		Boolean allowedBypass = BoundChecker.isAllowedBypass(person, commandGroup);
		if (allowedBypass != null) {
			return allowedBypass.booleanValue();
		}
		if (requiredPartRoles.isEmpty()) {
			// A grant without roles denies the operation in every context.
			return false;
		}
		return isAllowedInitial(person, draft, commandGroup, requiredPartRoles, new CyclicDecisions<>(),
			new CyclicDecisions<>());
	}

	/**
	 * Whether the given person holds one of the given roles, granted the given operation on an
	 * attribute, for an object not committed yet.
	 *
	 * <p>
	 * The access parents of an object not committed yet are followed up to the committed objects
	 * deciding the operation, a transient object leading to its
	 * {@link AccessParentFunction#resolveDraft(TLObject, BoundCommandGroup) draft parents}. The
	 * roles the person holds there are checked, as for an attribute of a persistent object
	 * delegating its access decision. One of the parents admitting the person suffices.
	 * </p>
	 *
	 * @param initial
	 *        The decisions on objects not committed yet made so far in this check; an object
	 *        reached again while it is decided closes a cycle, see {@link CyclicDecisions}.
	 * @param holds
	 *        The decisions on committed objects made so far in this check, see
	 *        {@link #holdsRole(Person, TLObject, BoundCommandGroup, Set, CyclicDecisions)}.
	 */
	private boolean isAllowedInitial(Person person, TLObject current, BoundCommandGroup operation,
			Set<BoundedRole> requiredPartRoles, CyclicDecisions<ObjectOperation> initial,
			CyclicDecisions<ObjectOperation> holds) {
		if (isCommitted(current)) {
			return holdsRole(person, current, operation, requiredPartRoles, holds);
		}
		AccessParentFunction parent = accessParentOf(current);
		if (parent == null || !parent.delegates(operation)) {
			// An object deciding the operation by its own roles: the roles it will hold are
			// computed only once it exists.
			return true;
		}
		return initial.decide(new ObjectOperation(current, operation), key -> {
			Iterable<TLObject> parents =
				current.tTransient() ? parent.resolveDraft(current, operation) : parent.resolveAll(current, operation);
			boolean leadsAnywhere = false;
			for (TLObject next : parents) {
				leadsAnywhere = true;
				if (isAllowedInitial(person, TLFormObjectBase.editedObject(next), operationOnParent(operation),
					requiredPartRoles, initial, holds)) {
					return true;
				}
			}
			// A free-standing object is not accessible until it is put into a container, which is a
			// write of the container checked in its own right.
			return !leadsAnywhere;
		}, () -> logCycle(current));
	}

	@Override
	public boolean isAllowed(Person person, TLObject instance, BoundCommandGroup commandGroup) {
		if (!isPersistent(instance)) {
			return true;
		}
		if (isDeleted(instance)) {
			// A deleted object is accessible for nobody, and its type must not be read.
			return false;
		}
		if (instance instanceof TLModelPart && isMetaDataAccess(commandGroup)) {
			// A model element is meta-data, not a business object, see
			// ModelAccessRights#isDiscoverAllowed(Person, TLObject).
			return true;
		}
		if (!isCommitted(instance)) {
			// Roles are computed at commit time; an object created in the current transaction has no
			// computed roles yet, so an instance-level check against it is not meaningful.
			return true;
		}
		Boolean allowedBypass = BoundChecker.isAllowedBypass(person, commandGroup);
		if (allowedBypass != null) {
			return allowedBypass;
		}
		return decide(decisionCache(), person, instance, commandGroup);
	}

	/**
	 * Whether the given object is a persistent object, the object a {@link KnowledgeItem} stands
	 * for.
	 *
	 * <p>
	 * A transient object, a form object editing a persistent object, and <code>null</code> are no
	 * persistent objects, they are not access controlled.
	 * </p>
	 */
	private static boolean isPersistent(TLObject object) {
		return object != null && !object.tTransient() && object.tHandle().getWrapper() == object;
	}

	/**
	 * Whether the given operation only reads a {@link TLModelPart model element}, which every person
	 * may do.
	 *
	 * <p>
	 * Modifying, creating and deleting a model element is checked like any other operation.
	 * </p>
	 */
	private static boolean isMetaDataAccess(BoundCommandGroup commandGroup) {
		return SimpleBoundCommandGroup.DISCOVER.equals(commandGroup)
			|| SimpleBoundCommandGroup.READ.equals(commandGroup);
	}

	/**
	 * The memo of decisions the current interaction has made for the current revision of the
	 * knowledge base.
	 */
	private AccessDecisionCache decisionCache() {
		return AccessDecisionCache.current(_applicationModel.tKnowledgeBase().getHistoryManager().getLastRevision());
	}

	/**
	 * Decides whether the given person may perform the given operation on the given committed
	 * object, the bypasses being settled already.
	 * <p>
	 * The decision is answered from the memo where the interaction has made it before. Otherwise an
	 * object of a type without security is accessible, an object whose access parent delegates the
	 * operation (or an operation implying it) asks its parent, and any other object requires the
	 * person to hold a role granted the operation on its type. An object whose access parent is
	 * restricted to some operations is also admitted by the roles held on itself, see
	 * {@link #compute(AccessDecisionCache, Person, TLObject, BoundCommandGroup)}.
	 * </p>
	 * <p>
	 * An object whose access parents lead back to itself is denied along that path: reaching a
	 * decision under way again means that the chain of access parents has a cycle. A denial relying
	 * on such a decision is kept provisionally until that decision is made, and discarded if it
	 * allows the operation, since the decision may still allow the operation through another access
	 * parent, see {@link CyclicDecisions}.
	 * </p>
	 */
	private boolean decide(AccessDecisionCache cache, Person person, TLObject instance,
			BoundCommandGroup commandGroup) {
		if (isWithoutSecurity(instance)) {
			// An object of a type without security is not access controlled.
			return true;
		}
		return cache.decide(AccessDecisionCache.key(person, instance, commandGroup),
			key -> compute(cache, person, instance, commandGroup),
			() -> logCycle(instance));
	}

	/**
	 * Logs that the access parents of the given object lead back to it.
	 *
	 * <p>
	 * Where the access parent of the object offers alternatives, a cycle is a matter of the data,
	 * and another candidate may still decide. Otherwise the object is denied by the cycle.
	 * </p>
	 */
	private void logCycle(TLObject instance) {
		AccessParentFunction parent = accessParentOf(instance);
		if (parent != null && parent.getRelation() instanceof AnyOfRelation anyOf && anyOf.candidates().size() > 1) {
			Logger.info("The access parents of " + instance + " form a cycle, the path through it is denied.",
				SecurityConfigurationService.class);
		} else {
			Logger.error("The access parents of " + instance + " form a cycle, access is denied.",
				SecurityConfigurationService.class);
		}
	}

	/**
	 * Computes the decision that {@link #decide(AccessDecisionCache, Person, TLObject, BoundCommandGroup)}
	 * stores.
	 *
	 * <p>
	 * The operation and each operation implying it on the object, as far as the access parent
	 * delegates it, is checked on the access parents, mapped by
	 * {@link #operationOnParent(BoundCommandGroup)}; one of them
	 * {@link AccessParentFunction#allows(TLObject, BoundCommandGroup, ParentCheck) allowed} by the
	 * access parents allows the operation. An access parent delegating every operation decides
	 * alone. For an access parent restricted to some operations, the roles the person holds on the
	 * object itself, granted the operation on its type, allow it as well: the type holds a
	 * delegated operation only where an operation it does not delegate implies it, so that this
	 * decides the operations not delegated, and the delegated ones (Discover in particular) for a
	 * person holding an own grant implying them.
	 * </p>
	 */
	private boolean compute(AccessDecisionCache cache, Person person, TLObject instance,
			BoundCommandGroup commandGroup) {
		AccessParentFunction parent = accessParentOf(instance);
		if (parent != null) {
			// The requested operation and the operations implying it on the object, as far as they
			// are delegated, are checked on the parent.
			Set<BoundCommandGroup> operations = new LinkedHashSet<>();
			operations.add(commandGroup);
			operations.addAll(getObjectOperations(instance).getImplying(commandGroup));
			ParentCheck check = (parentObject, operation) -> {
				if (!CheckedObjects.isStored(parentObject)) {
					// The parent is not access controlled, or it is being built in the current
					// transaction and has no computed roles yet (see isAllowed(Person, TLObject,
					// BoundCommandGroup)): its decision is not meaningful, and the object follows it.
					return true;
				}
				return decide(cache, person, parentObject, operationOnParent(operation));
			};
			for (BoundCommandGroup operation : operations) {
				if (parent.delegates(operation) && parent.allows(instance, operation, check)) {
					return true;
				}
			}
			if (parent.delegatesAll()) {
				// Every operation is delegated: the object holds no rights of its own.
				return false;
			}
		}
		if (!(instance instanceof BoundObject holder)) {
			// A persistent object that cannot hold roles grants no role to anybody.
			return false;
		}
		Set<BoundedRole> roles = getRoles(instance, commandGroup);
		return accessManager().hasRole(person, holder, roles);
	}

	/**
	 * The access parent relation of the type of the given object.
	 * 
	 * @return <code>null</code> when the object decides for itself.
	 */
	private AccessParentFunction accessParentOf(TLObject instance) {
		return instance.tType() instanceof TLClass type ? getAccessParent(type) : null;
	}

	/**
	 * The operation checked on the access parent in place of the given operation on the delegating
	 * object.
	 * <p>
	 * Creating or deleting a part is a modification of the whole, so both require the write right on
	 * the parent. Any other operation is checked on the parent as it is.
	 * </p>
	 */
	private static BoundCommandGroup operationOnParent(BoundCommandGroup operation) {
		if (operation == SimpleBoundCommandGroup.CREATE || operation == SimpleBoundCommandGroup.DELETE) {
			return SimpleBoundCommandGroup.WRITE;
		}
		return operation;
	}

	@Override
	public Set<TLObject> getRoleHolders(TLObject object, BoundCommandGroup operation) {
		Set<TLObject> result = new LinkedHashSet<>();
		collectRoleHolders(object, operation, new HashSet<>(), result);
		return result;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implNote Maps the operation as {@link #isAllowed(Person, TLObject, BoundCommandGroup)} does
	 *           when the access parent of the object delegates it. The mapping does not change the
	 *           operation any further along the chains of access parents, so that the first step
	 *           decides it for every chain.
	 */
	@Override
	public BoundCommandGroup getOperationOnRoleHolder(TLObject object, BoundCommandGroup operation) {
		return AccessParentFunction.isDelegated(accessParentOf(object), operation) ? operationOnParent(operation)
			: operation;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implNote Follows the delegation of
	 *           {@link #isAllowed(Person, TLObject, BoundCommandGroup)}: an object with an access
	 *           parent is admitted by the roles admitting the operation checked on the parent in its
	 *           place, or an operation implying it on the object, as far as these are delegated. An
	 *           object whose access parent is restricted to some operations is admitted by the roles
	 *           granted the operation on itself as well.
	 */
	@Override
	public Set<BoundedRole> getAdmittingRoles(TLObject object, BoundCommandGroup operation) {
		Set<BoundedRole> result = new HashSet<>();
		for (Set<BoundedRole> roles : getAdmittingRolesByHolder(object, operation).values()) {
			result.addAll(roles);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implNote Follows the delegation of {@link #isAllowed(Person, TLObject, BoundCommandGroup)}
	 *           as {@link #getAdmittingRoles(TLObject, BoundCommandGroup)} does, keeping the roles
	 *           apart by the object they are granted on.
	 */
	@Override
	public Map<TLObject, Set<BoundedRole>> getAdmittingRolesByHolder(TLObject object, BoundCommandGroup operation) {
		Map<TLObject, Set<BoundedRole>> result = new LinkedHashMap<>();
		collectAdmittingRoles(object, operation, new HashSet<>(), result);
		return result;
	}

	/**
	 * Adds the roles admitting the given operation on the given object, by the object holding them.
	 *
	 * @param visited
	 *        The steps along the chains of access parents taken so far. A step taken before adds
	 *        nothing new, on the path to itself (a cycle, which admits nobody) as well as on another
	 *        path.
	 */
	private void collectAdmittingRoles(TLObject instance, BoundCommandGroup operation,
			Set<ObjectOperation> visited, Map<TLObject, Set<BoundedRole>> result) {
		AccessParentFunction parent = accessParentOf(instance);
		if (parent == null) {
			addAdmittingRoles(result, instance, getRoles(instance, operation));
			return;
		}
		if (!visited.add(new ObjectOperation(instance, operation))) {
			return;
		}
		Set<BoundCommandGroup> operations = new LinkedHashSet<>();
		operations.add(operation);
		operations.addAll(getObjectOperations(instance).getImplying(operation));
		for (BoundCommandGroup delegated : operations) {
			for (TLObject parentObject : parent.resolveAll(instance, delegated)) {
				collectAdmittingRoles(parentObject, operationOnParent(delegated), visited, result);
			}
		}
		if (!parent.delegatesAll()) {
			// The object decides the operations not delegated by its own roles, see compute().
			addAdmittingRoles(result, instance, getRoles(instance, operation));
		}
	}

	private static void addAdmittingRoles(Map<TLObject, Set<BoundedRole>> result, TLObject holder,
			Set<BoundedRole> roles) {
		if (!roles.isEmpty()) {
			result.computeIfAbsent(holder, x -> new HashSet<>()).addAll(roles);
		}
	}

	/**
	 * An operation on an object: a step along the chains of access parents.
	 */
	private record ObjectOperation(TLObject object, BoundCommandGroup operation) {
		// Pure value.
	}

	/**
	 * Adds the objects whose roles decide the given operation on the given object: the first
	 * objects of its chains of access parents that do not delegate the operation (mapped by
	 * {@link #operationOnParent(BoundCommandGroup)} at every step), or the object itself when it
	 * does not delegate the operation. A chain leading nowhere or running in a cycle adds nothing.
	 *
	 * @param visited
	 *        The steps along the chains of access parents taken so far. A step taken before adds
	 *        nothing new, on the path to itself (a cycle) as well as on another path.
	 */
	private void collectRoleHolders(TLObject instance, BoundCommandGroup operation, Set<ObjectOperation> visited,
			Set<TLObject> result) {
		AccessParentFunction parent = accessParentOf(instance);
		if (parent == null || !parent.delegates(operation)) {
			result.add(instance);
			return;
		}
		if (!visited.add(new ObjectOperation(instance, operation))) {
			return;
		}
		for (TLObject parentObject : parent.resolveAll(instance, operation)) {
			collectRoleHolders(parentObject, operationOnParent(operation), visited, result);
		}
	}

	/**
	 * Whether the given person holds one of the given roles on an object deciding the given
	 * operation on the given object.
	 *
	 * <p>
	 * An object deciding the operation for itself is checked by the roles held on itself. For an
	 * object delegating the operation, the roles held on its access parents are checked instead,
	 * combined as the access parent {@link AccessParentFunction#allows(TLObject, BoundCommandGroup,
	 * ParentCheck) combines its decisions}; for the usual combination by OR, a role held on one of
	 * the {@link #getRoleHolders(TLObject, BoundCommandGroup) role holders} suffices.
	 * </p>
	 *
	 * @param decisions
	 *        The decisions made so far in this check, for the given person and roles. An object
	 *        reached again while it is decided closes a cycle, which admits nobody along that path,
	 *        see {@link CyclicDecisions}.
	 */
	private boolean holdsRole(Person person, TLObject instance, BoundCommandGroup operation,
			Set<BoundedRole> roles, CyclicDecisions<ObjectOperation> decisions) {
		AccessParentFunction parent = accessParentOf(instance);
		if (parent == null || !parent.delegates(operation)) {
			return instance instanceof BoundObject holder && accessManager().hasRole(person, holder, roles);
		}
		return decisions.decide(new ObjectOperation(instance, operation),
			key -> parent.allows(instance, operation,
				(parentObject, delegated) -> holdsRole(person, parentObject, operationOnParent(delegated), roles,
					decisions)),
			() -> {
				// The cycle is logged by the decision of the object itself.
			});
	}

	@Override
	public boolean isAllowed(Person person, TLObject instance, TLStructuredTypePart attribute,
			BoundCommandGroup commandGroup) {
		if (!isPersistent(instance)) {
			return true;
		}
		if (isDeleted(instance)) {
			// A deleted object is accessible for nobody, and its type must not be read.
			return false;
		}
		if (instance instanceof TLModelPart && isMetaDataAccess(commandGroup)) {
			// The attribute values of a model element are meta-data like the element itself, see
			// isAllowed(Person, TLObject, BoundCommandGroup).
			return true;
		}
		if (!isCommitted(instance)) {
			// See isAllowed(Person, TLObject, BoundCommandGroup): no check on not-yet-committed objects.
			return true;
		}
		Boolean allowedBypass = BoundChecker.isAllowedBypass(person, commandGroup);
		if (allowedBypass != null) {
			return allowedBypass;
		}
		if (isWithoutSecurity(instance)) {
			// An object of a type without security is not access controlled, neither on the object,
			// nor on its attribute values.
			return true;
		}
		Map<BoundCommandGroup, Set<BoundedRole>> partRights = partRights(instance.tType(), attribute.getName());
		if (!partRights.containsKey(commandGroup)) {
			if (SimpleBoundCommandGroup.READ.equals(commandGroup) && instance.tType() instanceof TLClass type
				&& getDiscoverAttributes(type).contains(attribute.getName())) {
				// A discover attribute without a read grant of its own is readable for everybody
				// who may discover the object.
				return decide(decisionCache(), person, instance, SimpleBoundCommandGroup.DISCOVER);
			}
			// No grant of the attribute itself: the class decision applies.
			return decide(decisionCache(), person, instance, commandGroup);
		}
		// The grants of the attribute replace the class grant for this attribute.
		Set<BoundedRole> requiredPartRoles = partRights.get(commandGroup);
		if (requiredPartRoles.isEmpty()) {
			// An attribute-level grant is present but lists no roles: the operation is denied for
			// every role (only a bypassing super-user, handled above, is unaffected).
			return false;
		}
		// An object delegating the operation to an access parent holds no roles for it: the roles
		// the person holds on the objects deciding the operation for it are checked instead.
		return holdsRole(person, instance, commandGroup, requiredPartRoles, new CyclicDecisions<>());
	}

	@Override
	public boolean isAllowedCreate(Person person, TLObject parent, TLStructuredTypePart compositionAttribute,
			TLClass type) {
		TLType targetType = compositionAttribute.getType();
		TLType createdType;
		if (type == null) {
			createdType = targetType;
		} else {
			if (!TLModelUtil.isCompatibleType(targetType, type)) {
				throw new IllegalArgumentException("Type '" + TLModelUtil.qualifiedName(type)
					+ "' is not compatible with the type '" + TLModelUtil.qualifiedName(targetType)
					+ "' of attribute '" + TLModelUtil.qualifiedName(compositionAttribute) + "'.");
			}
			createdType = type;
		}
		// Condition 1: CREATE right on the created type in the parent context.
		if (createdType instanceof TLClass && !isAllowedCreate(person, (TLClass) createdType, parent)) {
			return false;
		}
		// Condition 2: WRITE right on the composition reference of the parent.
		return isAllowed(person, parent, compositionAttribute, SimpleBoundCommandGroup.WRITE);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implNote A type whose access parent {@link AccessParentFunction#delegatesCreation()
	 *           delegates the creation} is decided by {@link SimpleBoundCommandGroup#WRITE} on the
	 *           context; any other type, also one whose access parent is restricted to operations
	 *           not including {@link SimpleBoundCommandGroup#CREATE}, by its own create grants,
	 *           checked on the context.
	 */
	@Override
	public boolean isAllowedCreate(Person person, TLClass type, TLObject context) {
		if (context != null && isDeleted(context)) {
			// Nobody may create an object in a deleted context.
			return false;
		}
		Boolean allowedBypass = BoundChecker.isAllowedBypass(person, SimpleBoundCommandGroup.CREATE);
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
		AccessParentFunction accessParent = getAccessParent(type);
		if (accessParent != null && accessParent.delegatesCreation()) {
			// Creating an object that delegates the creation to its access parent is writing the
			// object it is created in, which is its access parent by construction for a composition
			// part.
			// Without a context the object is free-standing: nobody can access it until it is put
			// into a container, and that is a write of the container checked in its own right, so
			// the creation itself is not restricted.
			if (!(context instanceof BoundObject)) {
				return true;
			}
			return decide(decisionCache(), person, context, SimpleBoundCommandGroup.WRITE);
		}
		// A type deciding for itself, or not delegating the creation, grants it by its own rights.
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
	 *
	 * <p>
	 * An object deleted in a committed transaction still counts as committed here; the access
	 * checks deny it before, see {@link #isDeleted(TLObject)}.
	 * </p>
	 *
	 * <p>
	 * Unlike {@link CheckedObjects#isStored(TLObject)}, an object that is no {@link BoundObject}
	 * counts as committed: walking the access parents of an object to be created stops at the first
	 * committed object, access controlled or not.
	 * </p>
	 */
	static boolean isCommitted(TLObject object) {
		if (object == null || object.tTransient()) {
			return false;
		}
		return object.tHandle().getState() == KnowledgeItem.State.PERSISTENT;
	}

	/**
	 * Whether the given object has been deleted.
	 *
	 * <p>
	 * A deleted object is accessible for nobody, not even for a person bypassing the model
	 * security, and its type must not be read.
	 * </p>
	 */
	private static boolean isDeleted(TLObject object) {
		return !object.tValid();
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
		Boolean allowedBypass = BoundChecker.isAllowedBypass(person, commandGroup);
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
		Map<TLClass, Map<BoundCommandGroup, Boolean>> memo = new HashMap<>();
		Set<TLClass> result = new HashSet<>();
		for (TLClass type : TLModelUtil.getAllGlobalClasses(_applicationModel)) {
			if (isAccessibleType(type, person, commandGroup, securityRoot, memo)) {
				result.add(type);
			}
		}
		return result;
	}

	/**
	 * Whether the given type is accessible on the type level, see
	 * {@link #getAccessibleTypes(Person, BoundCommandGroup)}.
	 * <p>
	 * A type without security is accessible without any role. A type with an access parent is
	 * accessible when one of the types it may delegate to is. Any other type is accessible when the
	 * person holds a role on the security root that is granted the operation on the type;
	 * deny-by-default, a type without such a grant is never accessible.
	 * </p>
	 * 
	 * @param memo
	 *        The decisions made so far, with a decision being computed stored as
	 *        <code>null</code>: a chain of access parents reaching it again runs in a cycle and is
	 *        not accessible.
	 */
	private boolean isAccessibleType(TLClass type, Person person, BoundCommandGroup commandGroup,
			BoundObject securityRoot, Map<TLClass, Map<BoundCommandGroup, Boolean>> memo) {
		if (isWithoutSecurity(type)) {
			return true;
		}
		Map<BoundCommandGroup, Boolean> decisions = memo.computeIfAbsent(type, unused -> new HashMap<>());
		if (decisions.containsKey(commandGroup)) {
			Boolean decision = decisions.get(commandGroup);
			return decision != null && decision.booleanValue();
		}
		decisions.put(commandGroup, null);
		boolean result;
		AccessParentFunction accessParent = getAccessParent(type);
		if (accessParent != null) {
			Set<TLClass> parentTypes = getAccessParentTypes(type);
			result = false;
			// The operation itself or one implying it on the type: a delegated one is checked on
			// the parent types.
			Set<BoundCommandGroup> operations = new HashSet<>(getOperations(type).getImplying(commandGroup));
			operations.add(commandGroup);
			for (BoundCommandGroup operation : operations) {
				if (!accessParent.delegates(operation)) {
					continue;
				}
				if (parentTypes == null) {
					// Unknown parent types (a computed access parent): the type-level check is a
					// pre-filter only, the check of each object follows the relation.
					result = true;
					break;
				}
				BoundCommandGroup parentOperation = operationOnParent(operation);
				if (parentTypes.stream().anyMatch(
					parentType -> isAccessibleType(parentType, person, parentOperation, securityRoot, memo))) {
					result = true;
					break;
				}
			}
			if (!result && !accessParent.delegatesAll()) {
				// An access parent not deciding every operation: the grants of the type itself
				// decide the operations not delegated.
				result = holdsOnRoot(person, securityRoot, getAllowedRoles(type, commandGroup));
			}
		} else {
			result = holdsOnRoot(person, securityRoot, getAllowedRoles(type, commandGroup));
		}
		decisions.put(commandGroup, Boolean.valueOf(result));
		return result;
	}

	private boolean holdsOnRoot(Person person, BoundObject securityRoot, Set<BoundedRole> roles) {
		return !roles.isEmpty() && accessManager().hasRole(person, securityRoot, roles);
	}

	/**
	 * The access control settings of a class, computed from the configuration of this service.
	 *
	 * <p>
	 * The settings are written while they are computed, see {@link SecurityConfigurationService#entry(TLClass)},
	 * and not changed afterwards. A setting computed from the settings of the generalizations is
	 * <code>null</code> until it is computed.
	 * </p>
	 */
	private static final class TypeEntry {

		/**
		 * The operations declared for the class, its generalizations and their modules, each mapped
		 * to the operations declared to imply it.
		 */
		Map<BoundCommandGroup, Set<BoundCommandGroup>> _declaredOperations;

		/**
		 * The operations applying to the class.
		 *
		 * @see SecurityConfigurationService#getOperations(TLClass)
		 */
		ApplicableOperations _operations = ApplicableOperations.BUILT_IN_ONLY;

		/**
		 * The rights the class passes on to its specializations.
		 */
		Map<BoundCommandGroup, Set<BoundedRole>> _passedOnRights;

		/**
		 * The rights of the class before the rules of its own entry are applied.
		 *
		 * @see SecurityConfigurationService#getInheritedRoles(TLClass, BoundCommandGroup)
		 */
		Map<BoundCommandGroup, Set<BoundedRole>> _inheritedRights = Collections.emptyMap();

		/**
		 * The rights of the class, including the roles granted an operation implying another one.
		 */
		Map<BoundCommandGroup, Set<BoundedRole>> _rights = Collections.emptyMap();

		/**
		 * The types whose attribute rights are in effect for the class, by attribute name.
		 *
		 * @see SecurityConfigurationService#partRightOrigins(TLClass, TypeEntry)
		 */
		Map<String, Set<TLClass>> _partRightOrigins;

		/**
		 * The attribute rights in effect for the class, by attribute name: the most specific entry
		 * of {@link SecurityConfigurationService#_declaredPartRights} on the way from the class to
		 * its generalizations.
		 */
		Map<String, Map<BoundCommandGroup, Set<BoundedRole>>> _partRights = Collections.emptyMap();

		/**
		 * The types whose discover attributes are in effect for the class.
		 *
		 * @see SecurityConfigurationService#discoverAttributeOrigins(TLClass, TypeEntry)
		 */
		Set<TLClass> _discoverAttributeOrigins;

		/**
		 * The names of the {@link TLClassAccessRights#getDiscoverAttributes() discover attributes}
		 * in effect for the class: the setting of the most specific entry on the way from the class
		 * to its generalizations.
		 */
		Set<String> _discoverAttributes = Collections.emptySet();

		/**
		 * The roles holding {@link SimpleBoundCommandGroup#DISCOVER} on the instances of the class.
		 *
		 * @see SecurityConfigurationService#computeDiscoverRoles(TLClass, TypeEntry)
		 */
		Set<BoundedRole> _discoverRoles = Collections.emptySet();

		/**
		 * The part of the {@link #_discoverRoles} that does not stem from the class rights: the
		 * roles of the rights of its attributes and the {@link SimpleBoundCommandGroup#CREATE}
		 * rights of the types creatable in its compositions.
		 *
		 * <p>
		 * A singleton with rights of its own replaces the class rights by its own rights, but is
		 * discoverable by the rights on its attributes and compositions as well.
		 * </p>
		 */
		Set<BoundedRole> _discoverRolesWithoutClassRights = Collections.emptySet();

		/**
		 * The part whose definition excludes the class from access control, if any.
		 *
		 * @see SecurityConfigurationService#getWithoutSecurityOrigin(TLClass)
		 */
		Optional<TLModelPart> _withoutSecurity;

		/**
		 * The part whose definition declares the class internal, if any.
		 *
		 * @see SecurityConfigurationService#getInternalOrigin(TLClass)
		 */
		Optional<TLModelPart> _internal;

		/**
		 * Whether {@link #_hasAccessParent} and {@link #_accessParent} are resolved.
		 */
		boolean _accessParentResolved;

		/**
		 * Whether the class or one of its generalizations has an access parent setting.
		 */
		boolean _hasAccessParent;

		/**
		 * The access parent setting of the class; <code>null</code> for a class deciding for
		 * itself.
		 */
		AccessParentFunction _accessParent;

		/**
		 * The global classes owning a composition that holds objects of the class.
		 */
		Set<TLClass> _containerTypes = new HashSet<>();

		/**
		 * The settings of a class without any: no operation declared, no right granted, no mark, no
		 * access parent and no container.
		 */
		static TypeEntry none() {
			TypeEntry result = new TypeEntry();
			result._declaredOperations = Collections.emptyMap();
			result._passedOnRights = Collections.emptyMap();
			result._partRightOrigins = Collections.emptyMap();
			result._discoverAttributeOrigins = Collections.emptySet();
			result._withoutSecurity = Optional.empty();
			result._internal = Optional.empty();
			result._accessParentResolved = true;
			result._containerTypes = Collections.emptySet();
			return result;
		}

		/**
		 * The roles granted the given operation on instances of the class.
		 */
		Set<BoundedRole> roles(BoundCommandGroup operation) {
			if (SimpleBoundCommandGroup.DISCOVER.equals(operation)) {
				return _discoverRoles;
			}
			return _rights.getOrDefault(operation, Collections.emptySet());
		}
	}

	/**
	 * The marks of a {@link TypeBasedAccessRights type based entry}, see
	 * {@link SecurityConfigurationService#markOrigin(TLClass, TypeEntry, Mark)}.
	 */
	private enum Mark {

		/** @see TypeBasedAccessRights#isWithoutSecurity() */
		WITHOUT_SECURITY {
			@Override
			Optional<TLModelPart> get(TypeEntry entry) {
				return entry._withoutSecurity;
			}

			@Override
			void set(TypeEntry entry, Optional<TLModelPart> origin) {
				entry._withoutSecurity = origin;
			}
		},

		/** @see TypeBasedAccessRights#isInternal() */
		INTERNAL {
			@Override
			Optional<TLModelPart> get(TypeEntry entry) {
				return entry._internal;
			}

			@Override
			void set(TypeEntry entry, Optional<TLModelPart> origin) {
				entry._internal = origin;
			}
		};

		/**
		 * The part declaring this mark of the class of the given entry; <code>null</code> when not
		 * yet computed.
		 */
		abstract Optional<TLModelPart> get(TypeEntry entry);

		/**
		 * Enters the part declaring this mark into the given entry.
		 */
		abstract void set(TypeEntry entry, Optional<TLModelPart> origin);
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
