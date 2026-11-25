/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.TLID;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.boundsec.manager.rule.PathNavigation;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.element.boundsec.manager.rule.RuleHistory;
import com.top_logic.element.boundsec.manager.rule.config.HistoryMode;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import java.util.function.Predicate;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.ElementBoundHelper;
import com.top_logic.element.boundsec.manager.rule.NavigationRule;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleProvider.Type;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig;
import com.top_logic.element.boundsec.manager.rule.config.SecurityParentsConfig;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Element access manager extends the access manager in order to support roles based on
 * attribute values.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
@ServiceDependencies({
	Settings.Module.class,
	/* ElementAccessHelper gets unique ME. ElementAccessHelper adds all roles from security root to
	 * available roles. */
	ModelService.Module.class,
	MetaElementFactory.Module.class,
	BoundHelper.Module.class,
	InitialRolesManager.Module.class,
})
public class ElementAccessManager extends AccessManager {

	/**
	 * {@link TypedConfiguration} interface for the {@link ElementAccessManager}.
	 */
	public interface Config extends AccessManager.Config {

		/** Property name of {@link #getMetaElements()}. */
		String META_ELEMENTS = "meta-elements";

		/** Property name of {@link #getSecurityParents()}. */
		String SECURITY_PARENTS = "security-parents";

		/**
		 * Types whose instances can be used for attribute classification, together with the module
		 * that holds the roles for them.
		 */
		@Name(META_ELEMENTS)
		@Key(MEConfig.NAME_ATTRIBUTE)
		List<MEConfig> getMetaElements();

		/**
		 * The role rule definition for the access manager.
		 */
		@Name(RoleRulesConfig.XML_TAG_ROLE_RULES)
		@ItemDefault
		RoleRulesConfig getRoleRules();

		/**
		 * Setter for {@link #getRoleRules()}.
		 */
		void setRoleRules(RoleRulesConfig roleRules);

		/**
		 * The role parent rules of the access manager: for a type, the navigation to the objects
		 * whose roles an object of the type inherits.
		 */
		@ItemDefault
		@Name(SECURITY_PARENTS)
		@Label("Role parents")
		SecurityParentsConfig getSecurityParents();

		/**
		 * Setter for {@link #getSecurityParents()}.
		 */
		void setSecurityParents(SecurityParentsConfig roleRules);

	}

	/**
	 * {@link TypedConfiguration} interface for configuring the role root for a meta element.
	 * 
	 * @see ElementAccessManager.Config#getMetaElements()
	 */
	public interface MEConfig extends NamedConfiguration {

		/** Property name of {@link #getRoleRoot()}. */
		String ROLE_ROOT = "role-root";

		/**
		 * Name of the module that defines the roles for the type.
		 * 
		 * <p>
		 * If not given, the module of the security structure is used.
		 * </p>
		 */
		@Name(ROLE_ROOT)
		@Nullable
		String getRoleRoot();

	}

	/** Key to store the current version of the role rule definition in the database. */
	private static final String ROLE_RULES_CONFIG_VERSION_PROPERTY = "roleRules.version";

	/**
	 * Suffix of the stored version of the rule definitions that identifies which entries the rules
	 * produce in a security storage.
	 *
	 * <p>
	 * Whenever a change of the engine lets unchanged rules produce other entries, this value is
	 * changed. The stored version then differs from the computed one at the first startup with the
	 * new engine, which lets a security storage be rebuilt once.
	 * </p>
	 */
	private static final String STORAGE_CONTENT_VERSION = ":2";

	/** Key to store the current version of the role parents definition in the database. */
	private static final String SECURITY_PARENTS_CONFIG_VERSION_PROPERTY = "security-parents.version";

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ElementAccessManager}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ElementAccessManager(InstantiationContext context, Config config) {
		super(context, config);
		_boundHelper = (ElementBoundHelper) BoundHelper.getInstance();
	}

	@Override
	protected void startUp() {
		super.startUp();
		init();
		_securityModuleByClass = loadRoleRootForMetaElements();
		loadInitialRules();
	}

	private Map<TLClass, TLModule> loadRoleRootForMetaElements() {
		// Build Meta element map used to identify the configured meta elements
		Map<String, TLClass> theUniqueMEs = ElementAccessHelper.getUniqueMetaElements();
		// clean map from entries marked as duplicates
		for (Iterator<Map.Entry<String, TLClass>> theIt = theUniqueMEs.entrySet().iterator(); theIt.hasNext();) {
			Object theValue = theIt.next().getValue();
			if (theValue == null) {
				theIt.remove();
			}
		}
		HashMap<TLClass, TLModule> securityModuleForClass = new HashMap<>();
		Log log = ApplicationConfig.getInstance().getServiceStartupContext();
		for (MEConfig meConfig : getConfig().getMetaElements()) {
			TLModule securityModule = getSecurityModule(meConfig, log);
			if (securityModule == null) {
				continue;
			}
			String meName = meConfig.getName();
			TLClass metaElement = theUniqueMEs.get(meName);
			if (metaElement == null) {
				log.error("Configured meta element is not available or not unique,"
					+ " will not be available for attribute classification: " + meName);
			} else {
				securityModuleForClass.put(metaElement, securityModule);
			}
		}
		return securityModuleForClass;
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

    /**
	 * The rules declared for the access manager.
	 */
	private Map<TLClass, Collection<RoleProvider>> rules;

    private Map<String, RoleProvider> ruleIds;

	private BidiHashMap/* <String, Integer> */ruleNumbers;
    
    /**
     * MetaAttributes that participate in a rule path.
     *
     * Updates to such attributes must trigger recalculation of access rights.
     */
    private Map<TLStructuredTypePart, Set<RoleProvider> > pathAttributes;

	private Map<TLClass, Collection<RoleProvider>> _resolvedMERules;

	private Map<TLClass, Set<BoundedRole>> _potentialRolesForType;

	/**
	 * Resolved security-parent rules per type.
	 *
	 * <p>
	 * Initialized to an empty map so that a failed rule load (an exception, or reported problems in
	 * {@link #loadSecurityParentRules()}) does not leave it <code>null</code>: {@link #getSecurityParents(BoundObject)}
	 * is consulted on every access check and must not fail with a {@link NullPointerException}. On a
	 * failed reload the previously resolved rules are retained; only a successful load replaces them.
	 * </p>
	 */
	private Map<TLClass, Collection<NavigationRule>> _resolvedSecurityParents = Collections.emptyMap();

	private Map<TLClass, TLModule> _securityModuleByClass;

	/** Properties derived from the loaded rules. */
	private volatile RuleAnalysis _analysis = RuleAnalysis.EMPTY;

	/** The roles granted by some role rule. */
	private Set<BoundRole> _ruleRoles = Collections.emptySet();

    /** Flag indicating that storage/cache based subclasses should refresh their storage/cache. */
    protected boolean dirty = false;

    /**
     * Flag indicating whether element access manager is currently in update/cached mode
     * (> 0) or not (= 0). This is implemented as an int for the case that more than one
     * updates are triggered parallel.
     */
    protected int cacheMode = 0;

    /**
	 * Cache for rule evaluation, saving the results of the {@link RoleRule#getGroupsFor(Wrapper)}
	 * method while the element access manager is in cache mode.
	 */
    private Map<Object, Object>     resultCache    = new HashMap<>();

	private Map<Object, Collection<Group>> getGroupsCache = new HashMap<>();
    
	private final ElementBoundHelper _boundHelper;


    /**
	 * Sets the {@link ElementAccessManager} into cache mode, which will cache results of the
	 * {@link RoleRule#getGroupsFor(Wrapper)} method until cache mode gets leaved. When calling this
	 * method, ensure to call a {@link #endCacheMode()} in a finally block!
	 */
    public synchronized void beginCacheMode() {
        resultCache    = new HashMap<>();
		getGroupsCache = new HashMap<>();
        cacheMode++;
    }

    /**
     * Leaves the cache mode, and frees all cached results.
     * Call this method in a finally block after a call of {@link #beginCacheMode()} only!
     */
    public synchronized void endCacheMode() {
        cacheMode--;
        if (cacheMode < 1) {
            resultCache    = new HashMap<>();
			getGroupsCache = new HashMap<>();
        }
    }

    /**
     * Checks whether the element access manager is currently in cache/update mode.
     *
     * @return <code>true</code>, if the element access manager is currently in
     *         cache/update mode, <code>false</code> otherwise
     */
    public boolean isInCacheMode() {
        return cacheMode > 0;
    }

    /**
     * Stores a value in the cache. The value is stored until cache mode gets leaved or
     * reentered.
     *
     * @param aKey
     *        the key for the value to store
     * @param aValue
     *        the value to store
     */
    public void putResult(Object aKey, Object aValue) {
        resultCache.put(aKey, aValue);
    }

    /**
     * Gets a stored value from the cache. Values are only stored until cache mode gets
     * leaved or reentered.
     *
     * @param aKey
     *        the key for the value to get
     * @return the stored value or <code>null</code>, if no value was stored before or
     *         the element access manager is currently not in cache/update mode
     */
    public Object getResult(Object aKey) {
        return resultCache.get(aKey);
    }


    /**
     * This method initializes the {@link ElementAccessManager}.
     */
	private void init() {
        this.rules            = new HashMap< >();
        this.ruleIds          = new HashMap<>();
        this.ruleNumbers      = new BidiHashMap();
		_resolvedMERules = new HashMap<>();
		_potentialRolesForType = new HashMap<>();
        this.pathAttributes   = new HashMap< >();
    }

	private TLModule getSecurityModule(MEConfig meConfig, Log log) {
		String structureName = meConfig.getRoleRoot();
		if (StringServices.isEmpty(structureName)) {
			return _boundHelper.securityModule();
		}
		return TLModelUtil.findModule(structureName);
	}

    /**
     * Note: storage/cache based subclasses must override this method and check the
     * {@link #dirty} flag. if the flag is <code>true</code>, they have to reset their
     * storage/cache afterwards.<br/>
     * Warning: Dirty flag is only set when {@code RoleRules.xml} doesn't exist. If this file was
     * changed manually instead of deleted, the dirty flag won't be set.
     *
     * @see com.top_logic.tool.boundsec.manager.AccessManager#reload()
     */
    @Override
	public boolean reload() {
		this.init();
		return loadInitialRules();
	}

	private boolean loadInitialRules() {
		boolean securityParentsresult = loadSecurityParentRules();
		boolean roleRuleResult = loadInitialRoleRules();
		_analysis = new RuleAnalysis(_resolvedMERules, _resolvedSecurityParents);
		return roleRuleResult && securityParentsresult;
	}

	/**
	 * Loads the role parent rules file if necessary.
	 */
	private boolean loadSecurityParentRules() {
		try {
			SecurityParentsConfig securityParents = getConfig().getSecurityParents();
			NavigationRulesImporter rulesImporter = NavigationRulesImporter.loadRules(securityParents.getRules());
			if (!rulesImporter.getProblems().isEmpty()) {
				/* Use english resources, because messages are written to log. */
				Resources resource = Resources.getLogInstance();
				for (ResKey theProblem : rulesImporter.getProblems()) {
					Logger.error("Problem while reloading role parents: " + resource.getString(theProblem), this);
				}
				return false;
			}
			storeConfigVersion(securityParents, SecurityParentsConfig.class, Config.SECURITY_PARENTS,
				SECURITY_PARENTS_CONFIG_VERSION_PROPERTY);

			HashMap<TLClass, Collection<NavigationRule>> resolved = new HashMap<>();
			for (Entry<TLClass, Collection<NavigationRule>> entry : rulesImporter.getRules().entrySet()) {
				TLClass theME = entry.getKey();
				Collection<NavigationRule> theRules = entry.getValue();
				for (NavigationRule rule : theRules) {
					boolean inherit = rule.isInherit();
					addRuleToSubElements(theME, rule, inherit, resolved);
				}
			}

			_resolvedSecurityParents = resolved;
			return true;
		} catch (Exception e) {
			Logger.error("Unable to reload security.", e, this);
			return false;
		}
	}

	/**
	 * Loads the initial role rules file if necessary.
	 */
	private boolean loadInitialRoleRules() {
		try {
			RoleRulesConfig roleRules = getConfig().getRoleRules();
			RoleRulesImporter roleRulesImporter = RoleRulesImporter.loadRules(roleRules);
			if (!roleRulesImporter.getProblems().isEmpty()) {
				/* Use english resources, because messages are written to log. */
				Resources resource = Resources.getLogInstance();
				for (ResKey theProblem : roleRulesImporter.getProblems()) {
					Logger.error("Problem while reloading security rules: " + resource.getString(theProblem), this);
				}
				return false;
            }
			storeConfigVersion(roleRules, RoleRulesConfig.class, "roleRules", ROLE_RULES_CONFIG_VERSION_PROPERTY);
			this.setRulesInternal(roleRulesImporter.getRules());
            return true;
        }
        catch (Exception e) {
            Logger.error("Unable to reload security.", e, this);
            return false;
        }
    }

	private <T extends ConfigurationItem> void storeConfigVersion(T item, Class<T> staticType, String rootTag,
			String propertyName) {
		String configVersion = configHash(item, staticType, rootTag);
		DBProperties dbProperties = new DBProperties(ConnectionPoolRegistry.getDefaultConnectionPool());
		boolean versionChanged = dbProperties.setProperty(DBProperties.GLOBAL_PROPERTY, propertyName, configVersion);
		if (versionChanged) {
			dirty = true;
		}
	}

	private <T extends ConfigurationItem> String configHash(T item, Class<T> staticType, String rootTag) {
		String configVersion;
		try {
			StringWriter out = new StringWriter();
			try (ConfigurationWriter w = new ConfigurationWriter(out)) {
				w.write(rootTag, staticType, item);
			}
			configVersion = out.toString().hashCode() + STORAGE_CONTENT_VERSION;
		} catch (XMLStreamException ex) {
			configVersion = String.valueOf(System.currentTimeMillis());
		}
		return configVersion;
	}

	@Override
	protected void shutDown() {
		if (dirty) {
			/* Reset property in case startup has not been finished completely, e.g. in tests. */
			DBProperties dbProperties = new DBProperties(ConnectionPoolRegistry.getDefaultConnectionPool());
			dbProperties.setProperty(DBProperties.GLOBAL_PROPERTY, ROLE_RULES_CONFIG_VERSION_PROPERTY, null);
		}
		super.shutDown();
	}

	/**
	 * The rules declared for the access manager, indexed by the type they are declared for.
	 * 
	 * @see #getResolvedMERules()
	 */
	public Map<TLClass, Collection<RoleProvider>> getRules() {
        return (rules);
    }

    /**
     * The rules that apply to instances of the given type, including inherited rules.
     */
    public Collection<RoleProvider> getRules(TLClass aME) {
		return getResolvedMERules().getOrDefault(aME, Collections.emptyList());
    }

    /**
	 * Set the internal rule caches according to the given rules
	 *
	 * @param someRules
	 *        the rules mapped by the meta element / meta object they are declared on.
	 */
	private void setRulesInternal(Map<TLClass, Collection<RoleProvider>> someRules) {
		Map<TLClass, Collection<RoleProvider>> theRules = someRules == null
			? Collections.emptyMap()
    	    : someRules;
		_resolvedMERules = resolveRules(theRules);
		_potentialRolesForType = resolvePotentialRoles(_resolvedMERules);
        this.pathAttributes   = resolveMetaAttributes(theRules);
        this.rules            = theRules;
        this.ruleIds          = new HashMap<>();
        this.ruleNumbers      = new BidiHashMap();
		Set<BoundRole> ruleRoles = new HashSet<>();
		for (Collection<RoleProvider> theRuleColl : theRules.values()) {
			for (RoleProvider theRule : theRuleColl) {
				ruleRoles.add(theRule.getRole());
                String   theId   = theRule.getId();
                if (this.ruleIds.containsKey(theId)) {
					throw new TopLogicException(I18NConstants.ERROR_DUPLICATE_RULE_ID__ID.fill(theId));
                }
                this.ruleIds.put(theId, theRule);
            }
        }
        List<String> theIds = new ArrayList<>(this.ruleIds.keySet());
        Collections.sort(theIds);
        int theSize = theIds.size();
        for (int i = 0; i < theSize; i++) {
            String theRuleId = theIds.get(i);
			this.ruleNumbers.put(theRuleId, i);
        }
		_ruleRoles = ruleRoles;
    }

	/**
	 * Properties derived from the loaded rules: which rules are not stored, and which roles may be
	 * held beyond the entries of a security storage.
	 */
	public RuleAnalysis getRuleAnalysis() {
		return _analysis;
	}

	/**
	 * The number under which the given rule is stored in the security storage.
	 * 
	 * @see #getRulefromPersitancyId(Integer)
	 */
	public Integer getPersitancyId(RoleProvider aRule) {
		return (Integer) this.ruleNumbers.get(aRule.getId());
    }

	/**
	 * Resolves the rule that is stored under the given number in the security storage.
	 * 
	 * @see #getPersitancyId(RoleProvider)
	 */
	public RoleProvider getRulefromPersitancyId(Integer aPersitancyID) {
        String theID = (String)this.ruleNumbers.getKey(aPersitancyID);
        return this.ruleIds.get(theID);
    }

	/**
	 * Determines the rules which uses the given part.
	 */
    public Set<RoleProvider> getRules(TLStructuredTypePart aMA) {
		return pathAttributes.getOrDefault(aMA, Collections.emptySet());
    }

	private Map<TLClass, Set<BoundedRole>> resolvePotentialRoles(
			Map<TLClass, Collection<RoleProvider>> resolvedMERules) {
		Map<TLClass, Set<BoundedRole>> result = new HashMap<>();
		for (Entry<TLClass, Collection<RoleProvider>> e : resolvedMERules.entrySet()) {
			Set<BoundedRole> roles = e.getValue()
				.stream()
				.filter(RoleRule.class::isInstance)
				.map(RoleRule.class::cast)
				.map(RoleRule::getRole)
				.map(BoundedRole.class::cast)
				.collect(Collectors.toSet());
			addRolesRecursive(result, e.getKey(), roles, new HashSet<>());
		}
		return result;
	}

	private void addRolesRecursive(Map<TLClass, Set<BoundedRole>> result, TLClass key, Set<BoundedRole> roles,
			Set<TLClass> seen) {
		if (!seen.add(key)) {
			// Already visited along this propagation; guard against diamond hierarchies and cycles.
			return;
		}
		result.computeIfAbsent(key, unused -> new HashSet<>()).addAll(roles);
		for (TLClass generalization : key.getGeneralizations()) {
			addRolesRecursive(result, generalization, roles, seen);
		}
	}

	private Map<TLClass, Collection<RoleProvider>> resolveRules(Map<TLClass, Collection<RoleProvider>> someRules) {
		HashMap<TLClass, Collection<RoleProvider>> resolved = new HashMap<>();
		for (Entry<TLClass, Collection<RoleProvider>> entry : someRules.entrySet()) {
			TLClass theME = entry.getKey();
			Collection<RoleProvider> theRules = entry.getValue();
			for (RoleProvider theRule : theRules) {
				if (theRule instanceof RoleRule rule) {
					boolean inherit = rule.isInherit();
					addRuleToSubElements(theME, theRule, inherit, resolved);
				}
			}
		}
		return resolved;
    }

	/**
	 * All rules that use the given role as source role (or provide the given role, if they have no
	 * source role).
	 * 
	 * @param aRole
	 *        The role to search rules for.
	 * @param aType
	 *        The rule type to restrict the result to, {@code null} for all rules.
	 */
	public Set<RoleProvider> getRulesWithSourceRole(BoundRole aRole, Type aType) {
        Set<RoleProvider> theResult = new HashSet<>();

		addMatchingProvides(aRole, aType, theResult, this.ruleIds.values());

        return theResult;
    }
    private void addMatchingProvides(BoundRole aRole, Type aType, Set<RoleProvider> theResult, Collection<? extends RoleProvider> theRoleProviders) {
		for (RoleProvider theRule : theRoleProviders) {
    		BoundRole theRole = theRule.getSourceRole();
    		if (theRole == null) {
    			theRole = theRule.getRole();
    		}
    		if ( ! theRole.equals(aRole)) {
    			continue;
    		}
    		if (aType != null && ! theRule.getType().equals(aType)) {
    			continue;
    		}
    		theResult.add(theRule);
    	}
    }


	private Map<TLStructuredTypePart, Set<RoleProvider>> resolveMetaAttributes(
			Map<TLClass, Collection<RoleProvider>> someRules) {
        Map<TLStructuredTypePart, Set<RoleProvider>> theResult = new HashMap<>();
		for (Map.Entry<TLClass, Collection<RoleProvider>> theEntry : someRules.entrySet()) {
			Collection<RoleProvider> theRules = theEntry.getValue();
			for (Iterator<RoleProvider> theRIt = theRules.iterator(); theRIt.hasNext();) {
                RoleRule theRule = (RoleRule) theRIt.next();

				for (PathElement thePathElement : theRule.getPath()) {
					Collection<TLStructuredTypePart> parts = thePathElement.getRelevantParts();
					parts.forEach(part -> MultiMaps.add(theResult, part, theRule));
                }
            }
        }
        return theResult;
    }

	private <T> void addRuleToSubElements(TLClass aME, T aRule, boolean isInherit,
			Map<TLClass, Collection<T>> aResult) {
		if (!aME.isAbstract()) {
			MultiMaps.add(aResult, aME, aRule, ArrayList::new);
		}
        if (isInherit) {
			for (TLClass theSubME : aME.getSpecializations()) {
                this.addRuleToSubElements(theSubME, aRule, isInherit, aResult);
            }
        }
    }

	/**
	 * The resolved security-parent rules that apply to objects of the given type.
	 *
	 * <p>
	 * The result already respects the {@link NavigationRule#isInherit() inheritance} of the
	 * configured rules: a rule declared on a generalization with inheritance enabled is contained
	 * in the result of each of its specializations. An abstract type never carries a rule, since no
	 * object has an abstract type.
	 * </p>
	 *
	 * @param type
	 *        The type to look up the rules for.
	 * @return The rules applying to objects of the given type, an empty collection if the type has
	 *         no role parent. The result must not be modified.
	 *
	 * @see Config#getSecurityParents()
	 */
	public Collection<NavigationRule> getRoleParentRules(TLClass type) {
		return _resolvedSecurityParents.getOrDefault(type, Collections.emptyList());
	}

	@Override
	public boolean hasRoleSource(TLClass type) {
		return !getRules(type).isEmpty() || !getRoleParentRules(type).isEmpty();
	}

	@Override
	public Collection<? extends BoundObject> getSecurityParents(BoundObject object) {
		if (!object.tValid()) {
			// A deleted object has no security parents.
			return Collections.emptyList();
		}
		Set<TLObject> out = new HashSet<>();
		if (object.tType() instanceof TLClass type) {
			for (NavigationRule rule : getRoleParentRules(type)) {
				if (rule.getHistory().getMode() == HistoryMode.ANCHORED) {
					// The parents of such a rule give their roles only to the groups the object is
					// anchored for, see getEffectiveGroups(BoundObject, BoundRole).
					continue;
				}
				rule.getRoleParents(object, out);
			}
		}
		return out.stream()
			.filter(BoundObject.class::isInstance)
			.map(BoundObject.class::cast)
			.toList();
	}

	/**
	 * Loads the values the rules navigate first when the given objects are checked, with one
	 * request per reference instead of one request per object.
	 *
	 * <p>
	 * For a historic version, these are the first steps of the role parent rules and role rules
	 * navigated in the version. For a current object, these are the first steps of the role parent
	 * rules.
	 * </p>
	 *
	 * @param context
	 *        Keeps the loaded values. It must stay open until the objects are checked.
	 * @param objects
	 *        The objects that are checked next.
	 * @param currentObjects
	 *        Whether to load the values for the current objects among the given objects, too.
	 */
	protected final void preloadRuleNavigation(PreloadContext context, Collection<? extends BoundObject> objects,
			boolean currentObjects) {
		Map<TLClass, List<TLObject>> versions = new HashMap<>();
		Map<TLClass, List<TLObject>> current = new HashMap<>();
		for (BoundObject object : objects) {
			if (!object.tValid() || !(object.tType() instanceof TLClass type)) {
				continue;
			}
			if (RuleHistory.isHistoric(object)) {
				versions.computeIfAbsent(type, x -> new ArrayList<>()).add(object);
			} else if (currentObjects) {
				current.computeIfAbsent(type, x -> new ArrayList<>()).add(object);
			}
		}
		for (Map.Entry<TLClass, List<TLObject>> entry : versions.entrySet()) {
			preloadFirstSteps(context, entry.getKey(), entry.getValue(), true);
		}
		for (Map.Entry<TLClass, List<TLObject>> entry : current.entrySet()) {
			preloadFirstSteps(context, entry.getKey(), entry.getValue(), false);
		}
	}

	private void preloadFirstSteps(PreloadContext context, TLClass type, List<TLObject> objects, boolean historic) {
		if (objects.size() < 2) {
			return;
		}
		Set<Object> loaded = new HashSet<>();
		for (NavigationRule rule : getRoleParentRules(type)) {
			HistoryMode mode = rule.getHistory().getMode();
			if (mode == HistoryMode.ANCHORED || (historic && mode == HistoryMode.CURRENT)) {
				// Navigated only for an open gate, or in the current data.
				continue;
			}
			preloadFirstStep(context, objects, rule.getPath(), loaded);
		}
		if (historic) {
			for (RoleProvider provider : getRules(type)) {
				if (provider instanceof RoleRule rule && rule.getHistory().getMode() == HistoryMode.HISTORIC) {
					preloadFirstStep(context, objects, rule.getPath(), loaded);
				}
			}
		}
	}

	private static void preloadFirstStep(PreloadContext context, List<TLObject> objects, List<PathElement> path,
			Set<Object> loaded) {
		if (path.isEmpty() || !(path.get(0) instanceof PathNavigation step)) {
			return;
		}
		TLReference reference = step.getReference();
		if (!loaded.add(Arrays.asList(reference, Boolean.valueOf(step.isInverse())))) {
			return;
		}
		if (step.isInverse()) {
			if (step.isBackwardsNavigable()) {
				MetaElementUtil.reversePreloadAttribute(context, objects, reference);
			}
		} else {
			MetaElementUtil.preloadAttribute(context, objects, reference);
		}
	}

    /**
     * Get all meta elements to be presented in the classification administration
     */
    public Collection<TLClass> getSupportedMetaElements() {
		return _securityModuleByClass.keySet();
    }

    @Override
	public Set<? extends BoundRole> getRoles(Person aPerson, BoundObject context) {
		Set<BoundRole> result = new HashSet<>(super.getRoles(aPerson, context));
		if (context != null) {
			Collection<Group> theGroups = getGroups(aPerson);
			for (BoundRole role : _ruleRoles) {
				if (!result.contains(role) && CollectionUtil.containsAny(theGroups, getEffectiveGroups(context, role))) {
					result.add(role);
				}
			}
		}
		return result;
    }

	/**
	 * The groups that have the given role on the given object, either through role rules or through
	 * direct role assignments.
	 *
	 * <p>
	 * The object may be a historic version. Roles inherited from role parents are not included, see
	 * {@link #getEffectiveGroups(BoundObject, BoundRole)}.
	 * </p>
	 *
	 * @return The groups. Must not be modified.
	 */
	public Collection<Group> getGroups(BoundObject aBO, BoundRole aRole) {
		if (!(aBO instanceof Wrapper wrapper) || aRole == null) {
			return Collections.emptySet();
		}
		return RoleQuestions.ask(wrapper.tKnowledgeBase(), useRoleMemo(),
			new RoleQuestion(false, wrapper.tId(), aRole.getID()), Collections.<Group> emptySet(),
			() -> computeGroups(wrapper, aRole));
	}

	/**
	 * The groups that have the given role on the given object: through role rules, through direct
	 * role assignments, or by inheriting the role from a role parent of the object.
	 *
	 * <p>
	 * The object may be a historic version. For each rule, its history mode selects the data it is
	 * evaluated on. A role parent passes on the roles it holds and the roles it inherits itself.
	 * </p>
	 *
	 * @return The groups. Must not be modified.
	 */
	public Set<Group> getEffectiveGroups(BoundObject object, BoundRole role) {
		if (!(object instanceof Wrapper wrapper) || role == null) {
			return Collections.emptySet();
		}
		return RoleQuestions.ask(wrapper.tKnowledgeBase(), useRoleMemo(),
			new RoleQuestion(true, wrapper.tId(), role.getID()), Collections.<Group> emptySet(),
			() -> computeEffectiveGroups(wrapper, (BoundObject) wrapper, role));
	}

	private Set<Group> computeEffectiveGroups(Wrapper wrapper, BoundObject object, BoundRole role) {
		Set<Group> result = new HashSet<>(getGroups(object, role));
		for (BoundObject parent : object.getSecurityParents()) {
			result.addAll(getEffectiveGroups(parent, role));
		}
		if (wrapper.tValid() && RuleHistory.isHistoric(wrapper) && wrapper.tType() instanceof TLClass type
			&& _analysis.hasAnchoredRoleParents(type)) {
			for (NavigationRule rule : getRoleParentRules(type)) {
				if (rule.getHistory().getMode() != HistoryMode.ANCHORED) {
					continue;
				}
				Set<TLObject> parents = new HashSet<>();
				rule.getRoleParents(wrapper, parents);
				Set<Group> pathGroups = new HashSet<>();
				for (TLObject parent : parents) {
					if (parent instanceof BoundObject roleParent) {
						pathGroups.addAll(getEffectiveGroups(roleParent, role));
					}
				}
				result.addAll(anchoredGroups(wrapper, pathGroups, rule.getHistory().getAnchors()));
			}
		}
		return result;
	}

	/**
	 * Whether answers about roles may be kept for the current interaction.
	 */
	protected boolean useRoleMemo() {
		return !isInCacheMode();
	}

	/**
	 * Computes {@link #getGroups(BoundObject, BoundRole)} by evaluating the rules.
	 */
	protected Set<Group> computeGroups(Wrapper wrapper, BoundRole role) {
		boolean useCache = isInCacheMode() && !RuleHistory.isHistoric(wrapper);
		Object cacheKey = null;
		if (useCache) {
			cacheKey = new TupleFactory.Pair<>(((BoundObject) wrapper).getID(), role.getID());
			Collection<Group> cached = getGroupsCache.get(cacheKey);
			if (cached != null) {
				return cached instanceof Set ? (Set<Group>) cached : new HashSet<>(cached);
			}
		}
		Set<Group> result = new HashSet<>();
		addRuleGroups(result, wrapper, role, rule -> true);
		addDirectGroups(result, wrapper, role);
		if (useCache) {
			getGroupsCache.put(cacheKey, ElementAccessHelper.shrink(result));
		}
		return result;
	}

	/**
	 * Adds the groups the role rules accepted by the given filter grant the given role to on the
	 * given object.
	 */
	protected final void addRuleGroups(Set<Group> result, Wrapper wrapper, BoundRole role,
			Predicate<? super RoleRule> filter) {
		for (Collection<RoleProvider> theRules : this.rules.values()) {
			for (RoleProvider provider : theRules) {
				RoleRule rule = (RoleRule) provider;
				if (!rule.getRole().equals(role) || !filter.test(rule)) {
					continue;
				}
				result.addAll(rule.getGroupsFor(wrapper));
			}
		}
	}

	/**
	 * Adds the groups the given role is directly assigned to on the given object.
	 */
	protected final void addDirectGroups(Set<Group> result, Wrapper wrapper, BoundRole role) {
		try {
			handleDirectHasRole(role, result, wrapper);
		} catch (Exception e) {
			Logger.error("Failed to get direct hasRole associations.", e, this);
		}
	}

	/**
	 * The groups that receive a role from a rule with mode {@link HistoryMode#ANCHORED} on the given
	 * historic version.
	 *
	 * @param version
	 *        The historic version the rule is evaluated for.
	 * @param pathGroups
	 *        The groups the path of the rule leads to.
	 * @param anchors
	 *        The anchor references of the rule.
	 * @return The path groups and gate groups for which path and gate agree, see
	 *         {@link GateGroups#restrict(Collection)}.
	 */
	public Set<Group> anchoredGroups(TLObject version, Collection<Group> pathGroups, List<TLReference> anchors) {
		if (pathGroups.isEmpty() || !RuleHistory.isHistoric(version)) {
			return Collections.emptySet();
		}
		return getGate(version, anchors).restrict(pathGroups);
	}

	/**
	 * The groups the given historic version is anchored for through the given anchor references.
	 *
	 * <p>
	 * A version is covered by a current object referring to exactly this version, or to the version
	 * of one of its containers in the same revision. The search therefore walks up the containers
	 * of the version.
	 * </p>
	 */
	public GateGroups getGate(TLObject version, List<TLReference> anchors) {
		GateGroups gate = GateGroups.NONE;
		Set<TLObject> seen = new HashSet<>();
		TLObject element = version;
		while (element != null && seen.add(element)) {
			for (TLReference anchor : anchors) {
				if (canReferTo(anchor, element)) {
					gate = gate.union(getGate(element, anchor));
				}
			}
			element = element.tContainer();
		}
		return gate;
	}

	private static boolean canReferTo(TLReference anchor, TLObject target) {
		TLType targetType = anchor.getType();
		return !(targetType instanceof TLClass) || TLModelUtil.isCompatibleInstance(targetType, target);
	}

	private GateGroups getGate(TLObject element, TLReference anchor) {
		return RoleQuestions.ask(element.tKnowledgeBase(), useRoleMemo(),
			new GateQuestion(element.tId(), anchor.tId()), GateGroups.NONE, () -> computeGate(element, anchor));
	}

	private GateGroups computeGate(TLObject element, TLReference anchor) {
		if (!PathNavigation.isBackwardsNavigable(anchor)) {
			return GateGroups.NONE;
		}
		GateGroups gate = GateGroups.NONE;
		for (TLObject owner : element.tReferers(anchor)) {
			if (RuleHistory.isHistoric(owner) || !owner.tValid()) {
				continue;
			}
			gate = gate.union(readersOf(owner, anchor));
		}
		return gate;
	}

	/**
	 * The groups that may read the value of the given reference on the given current object.
	 */
	private GateGroups readersOf(TLObject owner, TLReference reference) {
		if (!(owner.tType() instanceof TLClass type)) {
			return GateGroups.NONE;
		}
		ModelAccessRights rights = ModelAccessRights.getInstance();
		if (rights.isWithoutSecurity(type)) {
			return GateGroups.EVERYBODY;
		}
		Set<BoundedRole> roles;
		if (rights.hasGrant(type, reference, SimpleBoundCommandGroup.READ)) {
			// The grants of the attribute replace the grants of the type for this attribute.
			roles = rights.getAllowedRoles(type, reference, SimpleBoundCommandGroup.READ);
		} else {
			roles = rights.getAdmittingRoles(owner, SimpleBoundCommandGroup.READ);
		}
		if (roles.isEmpty() || !(rights.getRoleHolder(owner) instanceof BoundObject holder)) {
			return GateGroups.NONE;
		}
		Set<Group> groups = new HashSet<>();
		for (BoundedRole role : roles) {
			groups.addAll(getEffectiveGroups(holder, role));
		}
		return GateGroups.of(groups);
	}

	/**
	 * Decides like {@link #hasRole(Person, BoundObject, Collection)} was decided before roles could
	 * depend on the version of an object: The role parents are those of the given version, but each
	 * object is asked for the roles of its identity, i.e. of its current object.
	 */
	@Override
	protected boolean internalHasRoleOfIdentity(Person person, BoundObject object,
			Collection<? extends BoundRole> roles) {
		List<BoundObject> holders = new ArrayList<>();
		holders.add(object);
		BoundHelper.collectAllSecurityParents(object, holders::add);
		Collection<Group> personGroups = getGroups(person);
		for (BoundObject holder : holders) {
			BoundObject current =
				holder instanceof Wrapper version ? (BoundObject) WrapperHistoryUtils.getCurrent(version) : holder;
			if (current == null) {
				continue;
			}
			for (BoundRole role : roles) {
				if (CollectionUtil.containsAny(personGroups, getGroups(current, role))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Key of the question which groups hold a role on an object.
	 *
	 * @param inherited
	 *        Whether the roles inherited from role parents are included.
	 * @param object
	 *        The object, including its revision.
	 * @param role
	 *        The identifier of the role.
	 */
	private record RoleQuestion(boolean inherited, ObjectKey object, TLID role) {
		// Pure value.
	}

	/**
	 * Key of the question for which groups a current object referring to exactly the given version
	 * through the given reference opens the gate.
	 *
	 * @param element
	 *        The historic version, including its revision.
	 * @param anchor
	 *        The anchor reference.
	 */
	private record GateQuestion(ObjectKey element, ObjectKey anchor) {
		// Pure value.
	}

	/**
	 * Checks the direct has role associations.
	 */
	protected void handleDirectHasRole(BoundRole role, Set<Group> result, Wrapper wrapper) throws Exception {
		// A role assignment holds for each version of the object, it is found at the current object.
		Wrapper holder = RuleHistory.isHistoric(wrapper) ? WrapperHistoryUtils.getCurrent(wrapper) : wrapper;
		if (holder == null) {
			return;
		}
		try (CloseableIterator<KnowledgeObject> theIt = BoundedRole.roleAssignmentsForContext(holder.tHandle())) {
			while (theIt.hasNext()) {
				KnowledgeObject theKA = theIt.next();
				Wrapper theDestination =
					WrapperFactory.getWrapper((KnowledgeItem) theKA.getAttributeValue(BoundedRole.ATTRIBUTE_ROLE));
				if (!theDestination.equals(role)) {
					continue;
				}
				KnowledgeObject owner = (KnowledgeObject) theKA.getAttributeValue(BoundedRole.ATTRIBUTE_OWNER);
				Group theGroup = (Group) WrapperFactory.getWrapper(owner);
				result.add(theGroup);
			}
		}
	}


    /**
	 * Gets the groups the given person is member of.
	 *
	 * <p>
	 * These are the groups the roles of the person are looked up for: the representative group of
	 * the person and every group the person is a member of, directly or through a nested group.
	 * </p>
	 *
	 * @param aPerson
	 *        the person to get the groups from
	 * @return {@link List} with the {@link Group}s the given person is member of.
	 */
    public List<Group> getGroups(Person aPerson) {
        if (aPerson == null || !aPerson.tValid()) return Collections.emptyList();
        Group theGroup = aPerson.getRepresentativeGroup();
        List<Group> theGroupList = (theGroup == null ? new ArrayList<>() : CollectionUtil.intoList(theGroup));
        try {
            theGroupList.addAll(Group.getGroups(aPerson, true, true));
        }
        catch (Exception e) {
            Logger.warn("Failed to get groups for person " + aPerson, e, this);
        }
        return theGroupList;
    }


    /**
     * Adds the roles of all given role providers that grant their role to one of the given groups
     * on the given object.
     * 
     * @param someRoleProviders
     *        The role providers to check.
     * @param aGroupList
     *        The groups to check.
     * @param aBO
     *        The object to check roles on.
     * @param someResult
     *        The collection to add the found roles to.
     */
    protected void addRoleProviderRoles(Iterable<RoleProvider> someRoleProviders, Collection<Group> aGroupList, BoundObject aBO, Collection<BoundRole> someResult) {
        for (RoleProvider theRoleProvider : someRoleProviders) {
            Collection<Group> theRuleGroups = theRoleProvider.getGroups(aBO);
            if (CollectionUtil.containsAny(aGroupList, theRuleGroups)) {
                someResult.add(theRoleProvider.getRole());
            }
        }
    }

	/**
	 * The resolved rules declared for the access manager (respecting the inherit flag)
	 */
    public Map<TLClass, Collection<RoleProvider>> getResolvedMERules() {
		return _resolvedMERules;
    }

	@Override
	public boolean canHaveRole(TLClass type, BoundedRole role) {
		return _potentialRolesForType.getOrDefault(type, Collections.emptySet()).contains(role);
	}

	final BoundObject getSecurityRoot() {
		return _boundHelper.securityRoot();
	}

}
