/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilteredIterable;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.NullableString;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.MetaObject;
import com.top_logic.element.boundsec.ElementBoundHelper;
import com.top_logic.element.boundsec.manager.rule.ExternalRoleProvider;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleProvider.Type;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.boundsec.manager.rule.SecurityStorageCommitObserver;
import com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.manager.SimpleGroupMapper;
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
})
public class ElementAccessManager extends AccessManager {

	/**
	 * {@link TypedConfiguration} interface for the {@link ElementAccessManager}.
	 */
	public interface Config extends AccessManager.Config {

		/** Property name of {@link #getGroupMapper()}. */
		String GROUP_MAPPER = "group-mapper";

		/** Property name of {@link #getMetaElements()}. */
		String META_ELEMENTS = "meta-elements";

		/** Property name of {@link #getRoleProvider()}. */
		String ROLE_PROVIDER = "role-provider";

		/** Property name of {@link #getCommitObserver()}. */
		String COMMIT_OBSERVER = "commit-observer";

		@Name(GROUP_MAPPER)
		@InstanceFormat
		@InstanceDefault(SimpleGroupMapper.class)
		Mapping getGroupMapper();

		@Name(META_ELEMENTS)
		@Key(MEConfig.NAME_ATTRIBUTE)
		List<MEConfig> getMetaElements();

		@Name(ROLE_PROVIDER)
		@Key(ExternalRoleProvider.Config.RULE_NAME)
		List<ExternalRoleProvider> getRoleProvider();

		/**
		 * @see ElementAccessManager#commitObservers
		 */
		@Name(COMMIT_OBSERVER)
		@Key(SecurityStorageCommitObserver.Config.NAME_ATTRIBUTE)
		List<SecurityStorageCommitObserver> getCommitObserver();

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

	}

	/**
	 * {@link TypedConfiguration} interface for configuring the role root for a meta element.
	 * 
	 * @see ElementAccessManager.Config#getMetaElements()
	 */
	public interface MEConfig extends NamedConfiguration {

		/** Property name of {@link #getRoleRoot()}. */
		String ROLE_ROOT = "role-root";

		@Name(ROLE_ROOT)
		@Format(NullableString.class)
		String getRoleRoot();

	}

	/** Key to store the current version of the role rule definition in the database. */
	private static final String ROLE_RULES_CONFIG_VERSION_PROPERTY = "roleRules.version";

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

		groupMapper = getConfig().getGroupMapper();

		externalRoleProviders = new HashMap<>();
		externalRoleProvidersByTypes = new HashMap<>();
		for (ExternalRoleProvider roleProvider : getConfig().getRoleProvider()) {
			this.externalRoleProviders.put(roleProvider.getConfig().getRuleName(), roleProvider);
			for (String theType : roleProvider.getAffectingTypes()) {
				Set<ExternalRoleProvider> theSet = this.externalRoleProvidersByTypes.get(theType);
				if (theSet == null) {
					theSet = new HashSet<>();
					this.externalRoleProvidersByTypes.put(theType, theSet);
				}
				theSet.add(roleProvider);
			}
		}
		commitObservers = Collections.unmodifiableList(getConfig().getCommitObserver());
	}

	@Override
	protected void startUp() {
		super.startUp();
		init();
		_securityModuleByClass = loadRoleRootForMetaElements();
		loadInitialRoleRules();
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

	private final class ExternalRoleProviderFilter implements Filter<RoleProvider> {
		private final BoundObject theBO;

		/*package protected*/ ExternalRoleProviderFilter(BoundObject theBO) {
			this.theBO = theBO;
		}

		@Override
		public boolean accept(RoleProvider anObject) {
			return anObject.matches(theBO);
		}
	}

    /**
     * The rules declared for the access manager
     *
     * Key is either a {@link TLClass} or a {@link MetaObject}
     */
    private Map<Object, Collection<RoleProvider>> rules;

    private Map<String, RoleProvider> ruleIds;

	private BidiHashMap/* <String, Integer> */ruleNumbers;
    
    /**
     * external role provides by id ( {@link RoleProvider#getId()}
     */
	private final Map<String, ExternalRoleProvider> externalRoleProviders;
    
    /**
     * maps {@link KnowledgeItem} types ({@link ExternalRoleProvider#getAffectingTypes()})
     * to the {@link ExternalRoleProvider}s affected by changes of objects of such types.
     */
	private final Map<String, Set<ExternalRoleProvider>> externalRoleProvidersByTypes;


    /**
     * MetaAttributes that participate in a rule path.
     *
     * Updates to such attributes must trigger recalculation of access rights.
     */
    private Map<TLStructuredTypePart, Set<RoleProvider> > pathAttributes;

    /**
     * Association types that participate in a rule path
     *
     * Updates to such associations must trigger recalculation of access rights
     */
    private Map<String, Set <RoleProvider>> pathAssociations;

    /**
     * The resolved rules declared for the access manager (respecting the inherit flag)
     */
    private Map<TLClass, Collection<RoleProvider>> resolvedMERules;

    /**
     * The resolved rules declared for the access manager
     *
     * Map < MetaObjects, Map < BoundRole, Collection<Rule> > >
     */
    private Map<MetaObject, Collection<RoleProvider>> resolvedMORules;

	private Map<TLClass, TLModule> _securityModuleByClass;

	private final Mapping groupMapper;

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
    
    /**
     * Observers, that determine additional objects to the sets of
     * new and removed objects in the context of a commit
     */
	private final Collection<SecurityStorageCommitObserver> commitObservers;

	private final ElementBoundHelper _boundHelper;


    /**
	 * Sets the ElementAccessManager into cache mode, which will cache results of the
	 * {@link RoleRule#getGroupsFor(Wrapper)} method until cache mode gets leaved. When calling this
	 * method, ensure to call a #endCacheMode() in a finally block!
	 */
    public synchronized void beginCacheMode() {
        resultCache    = new HashMap<>();
		getGroupsCache = new HashMap<>();
        cacheMode++;
    }

    /**
     * Leaves the cache mode, and frees all cached results.
     * Call this method in a finally block after a call of #beginCacheMode() only!
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
     * This method initializes the ElementAccessManager.
     */
	private void init() {
        this.rules            = new HashMap< >();
        this.ruleIds          = new HashMap<>();
        this.ruleNumbers      = new BidiHashMap();
        this.resolvedMERules  = new HashMap<>();
        this.resolvedMORules  = new HashMap<>();
        this.pathAttributes   = new HashMap< >();
        this.pathAssociations = new HashMap< >();
    }

	private TLModule getSecurityModule(MEConfig meConfig, Log log) {
		String structureName = meConfig.getRoleRoot();
		if (StringServices.isEmpty(structureName)) {
			return _boundHelper.securityModule();
		}
		return TLModelUtil.findModule(structureName);
	}

    public Mapping getGroupMapper() {
        return (groupMapper);
    }

    /**
     * Note: storage/cache based subclasses must override this method and check the
     * {@link #dirty} flag. if the flag is <code>true</code>, they have to reset their
     * storage/cache afterwards.<br/>
     * Warning: Dirty flag is only set when RoleRules.xml doesn't exist. If this file was
     * changed manually instead of deleted, the dirty flag won't be set.
     *
     * @see com.top_logic.tool.boundsec.manager.AccessManager#reload()
     */
    @Override
	public boolean reload() {
		this.init();
		return loadInitialRoleRules();
	}

	/**
	 * Loads the initial role rules file if necessary.
	 */
	private boolean loadInitialRoleRules() {
		try {
			RoleRulesConfig roleRules = getConfig().getRoleRules();
			RoleRulesImporter roleRulesImporter = RoleRulesImporter.loadRules(this, roleRules);
			if (!roleRulesImporter.getProblems().isEmpty()) {
				/* Use english resources, because messages are written to log. */
				Resources resource = Resources.getLogInstance();
				for (ResKey theProblem : roleRulesImporter.getProblems()) {
					Logger.error("Problem while reloading security rules: " + resource.getString(theProblem), this);
				}
				return false;
            }
			storeConfigVersion(roleRules);
			this.setRulesInternal(roleRulesImporter.getRules());
            return true;
        }
        catch (Exception e) {
            Logger.error("Unable to reload security.", e, this);
            return false;
        }
    }

	private void storeConfigVersion(RoleRulesConfig roleRules) {
		String configVersion = configHash(roleRules);
		DBProperties dbProperties = new DBProperties(ConnectionPoolRegistry.getDefaultConnectionPool());
		boolean versionChanged =
			dbProperties.setProperty(DBProperties.GLOBAL_PROPERTY, ROLE_RULES_CONFIG_VERSION_PROPERTY, configVersion);
		if (versionChanged) {
			dirty = true;
		}
	}

	private String configHash(RoleRulesConfig roleRules) {
		String configVersion;
		try {
			StringWriter out = new StringWriter();
			new ConfigurationWriter(out).write("roleRules", RoleRulesConfig.class, roleRules);
			configVersion = String.valueOf(out.toString().hashCode());
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

	protected Collection<SecurityStorageCommitObserver> getCommitObservers() {
		return (commitObservers);
	}

	public Map<Object, Collection<RoleProvider>> getRules() {
        return (rules);
    }

    public Collection<RoleProvider> getRules(TLClass aME) {
        return getFromCollectionMap(aME, this.resolvedMERules);
    }

    public Collection<RoleProvider> getRules(MetaObject aMO) {
        return getFromCollectionMap(aMO, this.resolvedMORules);
    }

    private <U, V> Collection<V> getFromCollectionMap(U aKey, Map<U, Collection<V>> aMap) {
        Collection<V> theRules = aMap.get(aKey);
        return theRules != null ? theRules : Collections.<V>emptyList();
    }

    /**
	 * Set the internal rule caches according to the given rules
	 *
	 * @param someRules
	 *        the rules mapped by the meta element / meta object they are declared on.
	 */
    private void setRulesInternal(Map <Object, Collection<RoleProvider> > someRules) {
    	Map <Object, Collection<RoleProvider> > theRules = someRules == null 
    	    ? Collections.<Object, Collection<RoleProvider> >emptyMap() 
    	    : someRules;
        resolveRules(theRules, this.resolvedMERules, this.resolvedMORules);
        this.pathAttributes   = resolveMataAttributes(theRules);
        this.pathAssociations = resolveAssociations(theRules);
        this.rules            = theRules;
        this.ruleIds          = new HashMap<>();
        this.ruleNumbers      = new BidiHashMap();
		for (Collection<RoleProvider> theRuleColl : theRules.values()) {
			for (RoleProvider theRule : theRuleColl) {
                String   theId   = theRule.getId();
                if (this.ruleIds.containsKey(theId)) {
                    throw new TopLogicException(this.getClass(), "duplicateKeyId", new String[] { theId });
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
    }

	public Integer getPersitancyId(RoleProvider aRule) {
		return (Integer) this.ruleNumbers.get(aRule.getId());
    }

	public RoleProvider getRulefromPersitancyId(Integer aPersitancyID) {
        String theID = (String)this.ruleNumbers.getKey(aPersitancyID);
        return this.ruleIds.get(theID);
    }

    public Set<RoleProvider> getRules(TLStructuredTypePart aMA) {
        Set<RoleProvider> theResult = this.pathAttributes.get(aMA);
        return theResult == null ? Collections.<RoleProvider>emptySet() : theResult;
    }

    public Set<RoleProvider> getRules(String anAssociationType) {
        Set<RoleProvider> theResult = this.pathAssociations.get(anAssociationType);
        return theResult == null ? Collections.<RoleProvider>emptySet() : theResult;
    }

    public Set<ExternalRoleProvider> getAffectedRoleRuleFactories(String aType) {
        Set<ExternalRoleProvider> theResult = this.externalRoleProvidersByTypes.get(aType);
        return theResult != null ? theResult : Collections.<ExternalRoleProvider>emptySet();
    }

    private void resolveRules(Map <Object, Collection<RoleProvider> > someRules, Map <TLClass, Collection<RoleProvider> > aMEMap, Map <MetaObject, Collection<RoleProvider> > aMOMap) {
        for (Map.Entry<Object, Collection<RoleProvider>> theEntry : someRules.entrySet()) {
        	Object      theKey = theEntry.getKey();
        	if (theKey instanceof MetaObject) {
        		aMOMap.put((MetaObject) theKey, theEntry.getValue());
			} else {
        		TLClass theME    = (TLClass) theKey;
				Collection<RoleProvider> theRules = theEntry.getValue();
				for (RoleProvider theRule : theRules) {
        			if (theRule instanceof RoleRule) {
						boolean inherit = ((RoleRule) theRule).isInherit();
						this.addRuleToSubElements(theME, theRule, inherit, aMEMap);
        			}
        		}
        	}
        	
        }
    }

    public Set getRulesWithSourceRole(BoundRole aRole, Type aType) {
        Set<RoleProvider> theResult = new HashSet<>();

		addMatchingProvides(aRole, aType, theResult, this.ruleIds.values());
		addMatchingProvides(aRole, aType, theResult, this.externalRoleProviders.values());

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


	private Map<TLStructuredTypePart, Set<RoleProvider>> resolveMataAttributes(Map<Object, Collection<RoleProvider>> someRules) {
        Map<TLStructuredTypePart, Set<RoleProvider>> theResult = new HashMap<>();
		for (Map.Entry<Object, Collection<RoleProvider>> theEntry : someRules.entrySet()) {
			Collection<RoleProvider> theRules = theEntry.getValue();
			for (Iterator<RoleProvider> theRIt = theRules.iterator(); theRIt.hasNext();) {
                RoleRule theRule = (RoleRule) theRIt.next();

				for (PathElement thePathElement : theRule.getPath()) {
                    TLStructuredTypePart theMA          = thePathElement.getMetaAttribute();
                    if (theMA != null) {
                        this.addRuleToMap(theResult, theMA, theRule);
                    }
                }
            }
        }
        return theResult;
    }

	private Map<String, Set<RoleProvider>> resolveAssociations(Map<Object, Collection<RoleProvider>> someRules) {
        Map<String, Set <RoleProvider>> theResult = new HashMap<>();
		for (Map.Entry<Object, Collection<RoleProvider>> theEntry : someRules.entrySet()) {
			Collection<RoleProvider> theRules = theEntry.getValue();
			for (Iterator<RoleProvider> theRIt = theRules.iterator(); theRIt.hasNext();) {
                RoleRule theRule = (RoleRule) theRIt.next();

				for (PathElement thePathElement : theRule.getPath()) {
                    String      theAssociation = thePathElement.getAssociation();
                    if (theAssociation != null) {
                        this.addRuleToMap(theResult, theAssociation, theRule);
                    }
                }
            }
        }
        return theResult;
    }

    private <V> void addRuleToMap(Map<V, Set <RoleProvider>> someCurrent, V aKey, RoleProvider aRule) {
        Set<RoleProvider> theRules = someCurrent.get(aKey);
        if (theRules == null) {
            theRules = new HashSet<>();
            someCurrent.put(aKey, theRules);
        }
        theRules.add(aRule);
    }

    private void addRuleToSubElements(TLClass aME, RoleProvider aRule, boolean isInherit, Map <TLClass, Collection<RoleProvider> > aResult) {
		if (!aME.isAbstract()) {
			addToCollectionMap(aME, aRule, aResult);
		}
        if (isInherit) {
			for (TLClass theSubME : aME.getSpecializations()) {
                this.addRuleToSubElements(theSubME, aRule, isInherit, aResult);
            }
        }
    }

    private void addToCollectionMap(TLClass aKey, RoleProvider aRule, Map <TLClass, Collection<RoleProvider> > aResult) {
    	Collection<RoleProvider> theRules = aResult.get(aKey);
        if (theRules == null) {
            theRules = new ArrayList<>();
            aResult.put(aKey, theRules);
        }
        theRules.add(aRule);
    }

    /**
     * Get all meta elements to be presented in the classification administration
     */
    public Collection<TLClass> getSupportedMetaElements() {
		return _securityModuleByClass.keySet();
    }

    /**
     * Get the roles that can be given on the meta element
     */
	public Collection<? extends BoundRole> getRolesForMetaElement(TLClass aME) {
		TLModule theBO = _securityModuleByClass.get(aME);
        return BoundHelper.getInstance().getPossibleRoles(theBO);
    }

    @Override
    public Set<BoundRole> getRoles(Person aPerson, BoundObject aBO) {
        Set<BoundRole> theResult = super.getRoles(aPerson, aBO);
        Collection<Group> theGroups = getGroups(aPerson);
        while (aBO != null) {
            if (aBO instanceof Wrapper) {
                addRoleProviderRoles(
					this.getRules((TLClass) ((Wrapper) aBO).tType()),
                        theGroups, aBO, theResult);
            }
            if (aBO instanceof Wrapper) {
                addRoleProviderRoles(
                        this.getRules(((Wrapper)aBO).tTable()), 
                        theGroups, aBO, theResult);
            }
            
            // handle external role providers
            {
                addRoleProviderRoles(
					new FilteredIterable<>(
                                new ExternalRoleProviderFilter(aBO), 
                                this.externalRoleProviders.values()),                   
                        theGroups, aBO, theResult);
            }
            aBO = aBO.getSecurityParent();
        }
        return theResult;
    }


    public Collection<Group> getGroups(BoundObject aBO, BoundRole aRole) {
		Object getGroupCacheKey;
        if (isInCacheMode()) {
			getGroupCacheKey = new TupleFactory.Pair<>(aBO.getID(), aRole.getID());
			Collection<Group> theResult = getGroupsCache.get(getGroupCacheKey);
            if (theResult != null) return theResult;
		} else {
			getGroupCacheKey = null;
        }
        Set<Group> theResult = new HashSet<>();
        if (aBO instanceof Wrapper) {
        	Wrapper theWrapper = (Wrapper) aBO;
        	// first handle rules
			for (Collection<RoleProvider> theRules : this.rules.values()) {
				for (Iterator<RoleProvider> theRuleIt = theRules.iterator(); theRuleIt.hasNext();) {
	                RoleRule theRule = (RoleRule) theRuleIt.next();
	                if ( ! theRule.getRole().equals(aRole)) { // had handling of "any role"
	                    continue;
	                }
					theResult.addAll(theRule.getGroupsFor(theWrapper));
	            }
	        }
	        
	        // handle hasRole association
	        try {
	            handleDirectHasRole(aRole, theResult, theWrapper);
	        }
	        catch (Exception e) {
	            Logger.error("Failed to get direct hasRole associations.", e, this);
	        }
        }
        // handle extenal rules
        for (ExternalRoleProvider theFactory : this.externalRoleProviders.values()) {
			if (theFactory.matches(aBO) && aRole.equals(theFactory.getRole())) {
				theResult.addAll(theFactory.getGroups(aBO));
			}
		}
        if (isInCacheMode()) {
			getGroupsCache.put(getGroupCacheKey, ElementAccessHelper.shrink(theResult));
        }
        return theResult;
    }

	/**
	 * Checks the direct has role associations.
	 */
	protected void handleDirectHasRole(BoundRole role, Set<Group> result, Wrapper wrapper) throws Exception {
		for (Iterator<KnowledgeAssociation> theIt =
			wrapper.tHandle().getOutgoingAssociations(BoundedRole.HAS_ROLE_ASSOCIATION); theIt.hasNext();) {
			KnowledgeAssociation theKA = theIt.next();
			Wrapper theDestination = WrapperFactory.getWrapper(theKA.getDestinationObject());
			if (!theDestination.equals(role)) {
		        continue;
		    }
		    KnowledgeObject owner = (KnowledgeObject) theKA.getAttributeValue(BoundedRole.ATTRIBUTE_OWNER);
		    Group theGroup = (Group) WrapperFactory.getWrapper(owner);
		    result.add(theGroup);
		}
	}


    /**
	 * Gets the groups the given person is member of.
	 *
	 * @param aPerson
	 *        the person to get the groups from
	 * @return {@link List} with the {@link Group}s the given person is member of.
	 */
    protected List<Group> getGroups(Person aPerson) {
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


    protected void addRoleProviderRoles(Iterable<RoleProvider> someRoleProviders, Collection<Group> aGroupList, BoundObject aBO, Collection<BoundRole> someResult) {
        for (RoleProvider theRoleProvider : someRoleProviders) {
            Collection<Group> theRuleGroups = theRoleProvider.getGroups(aBO);
            if (CollectionUtil.containsAny(aGroupList, theRuleGroups)) {
                someResult.add(theRoleProvider.getRole());
            }
        }
    }

    public Map<TLClass, Collection<RoleProvider>> getResolvedMERules() {
        return this.resolvedMERules;
    }

    public Map<MetaObject, Collection<RoleProvider>> getResolvedMORules() {
        return this.resolvedMORules;
    }

    public Map<String, ExternalRoleProvider> getExternalRules() {
        return this.externalRoleProviders;
    }
    
    @Override
	public void handleSecurityUpdate(KnowledgeBase kb, Map<TLID, Object> someChanged,
			Map<TLID, Object> someNew, Map<TLID, Object> someRemoved, CommitHandler aHandler) {
        for(SecurityStorageCommitObserver theObserver : this.getCommitObservers()) {
            someNew    .putAll(theObserver.getAdded  (someChanged, someNew, someRemoved, aHandler));
            someRemoved.putAll(theObserver.getRemoved(someChanged, someNew, someRemoved, aHandler));
        }
        super.handleSecurityUpdate(kb, someChanged, someNew, someRemoved, aHandler);
    }

	final BoundObject getSecurityRoot() {
		return _boundHelper.securityRoot();
	}

}
