/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.basic.component.AJAXComponent;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.tool.execution.service.CommandApprovalService;
import com.top_logic.util.TLContext;

/**
 * A {@link com.top_logic.mig.html.layout.LayoutComponent} implementing the
 * {@link com.top_logic.tool.boundsec.BoundChecker} interface.
 *
 * This implementation allows caching of the Security
 * {@link #allow(BoundCommandGroup, BoundObject)}, be aware that this will be correct only if the
 * {@link Person} / {@link TLContext} is always the same.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 * @author    Dieter Rothbächer
 */
public abstract class BoundComponent extends AJAXComponent implements BoundCheckerComponent {

	private static final int MODEL_INVALID = 1;

	private static final int ALLOWED = 2;

	private static final int NOT_ALLOWED = 3;

	/**
	 * Configuration for {@link BoundComponent}.
	 */
	public interface Config
			extends AJAXComponent.Config, BoundCheckerLayoutConfig, SecurityObjectProviderConfig, WithSecurityMaster {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * The name of the {@link SecurityObjectProvider} which defines on which object the security
		 * will be checked.
		 * 
		 * @deprecated Use {@link #getSecurityObject()}.
		 */
		@Name(XML_ATTRIBUTE_SECURITY_PROVIDER_CLASS)
		@Deprecated
		String getSecurityProviderClass();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			AJAXComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerCommand(GotoHandler.COMMAND);
		}

	}


    /** SecurityProviderClass which defines on which object the security will be checked. */
    public static final String XML_ATTRIBUTE_SECURITY_PROVIDER_CLASS = "securityProviderClass";

    /** {@link ThreadLocal} for accessing the allow cache */
	private static final ThreadLocal<Map<Tuple, Boolean>> SECURITY_REQUEST_CACHE = new ThreadLocal<>();

    /**
     * The command groups used by this component
     *
     * Not used here, but used by many subclasses.
     */
    private   Collection<BoundCommandGroup> commandGroups;
    
    /** Optional Map of Boolean indexed by a CompoundKey of CommandGroupd and Object */
	protected transient Map<Tuple, Boolean> _allowCache;
    
    /** Our persistent representation for the security */
	private final PersBoundComp _persBoundComp;

    /**
     * Will be incremented on every call to allow.
     *
     * TODO MGA remove again ?
     */
    public int allowCount;

	/**
	 * Saves the configured {@link SecurityObjectProvider}.
	 */
	private final SecurityObjectProvider _securityObjectProvider;

    /**
     * Use by CompundSecuritylayout for TODO TSA
     */
    private boolean isSecurityMaster;

    /**
	 * The last visibility state that caused a "security changed" event to be sent.
	 * <code>null</code> means, no event has been sent so far.
	 */
	private Boolean _currentVisibility;

    /**
	 * Construct a bound component from configuration.
	 */
    public BoundComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);

		isSecurityMaster = atts.getIsSecurityMaster();
		_persBoundComp = SecurityComponentCache.lookupPersBoundComp(this);
        try {
			_securityObjectProvider = initSecurityObjectProvider(context, atts);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(I18NConstants.INVALID_SECURITY_OBJECT_PROVIDER_CONFIG, ex);
		}
		_allowCache = createAllowCache();
    }
    
    /**
     * Search for {@link PersBoundComp} in the KBase based on {@link #getSecurityId()}.
     */
    protected final PersBoundComp lookupPersBoundComp()  {
		ComponentName theSecID = this.getSecurityId();
		if (theSecID != null && !LayoutConstants.isSyntheticName(theSecID)) {
            try {
                return SecurityComponentCache.getSecurityComponent(theSecID);
            }
            catch (Exception e) {
                Logger.error("failed to setupPersBoundComp '" + theSecID + "'", e, this);
            }
        }
        return null;
    }
    
	/**
	 * Instantiates the configured {@link Config#getSecurityObject() provider}, or, if none
	 * is configured, tries to resolve the configured {@link Config#getSecurityProviderClass()
	 * provider name}, or, if that fails too, falls back to the
	 * {@link #getDefaultSecurityObjectProvider() default provider}.
	 * 
	 * @throws ConfigurationException
	 *         If the configured {@link SecurityObjectProvider} cannot be instantiated.
	 */
	protected SecurityObjectProvider initSecurityObjectProvider(InstantiationContext context, Config atts)
			throws ConfigurationException {
		PolymorphicConfiguration<? extends SecurityObjectProvider> providerConfig = atts.getSecurityObject();
		if (providerConfig != null) {
			return context.getInstance(providerConfig);
		} else {
			String providerName = StringServices.nonEmpty(atts.getSecurityProviderClass());
			if (!StringServices.isEmpty(providerName)) {
				return SecurityObjectProviderManager.getInstance().getSecurityObjectProvider(providerName);
			} else {
				return getDefaultSecurityObjectProvider();
			}
		}
    }

    /**
	 * Gets the default SecurityObjectProvider which gets used if no one is configured in layout
	 * xml. Subclasses may override this method if necessary
	 * 
	 * @return {@link SecurityObjectProviderManager#getDefaultSecurityObjectProvider()}
	 */
    protected SecurityObjectProvider getDefaultSecurityObjectProvider() {
		return SecurityObjectProviderManager.getInstance().getDefaultSecurityObjectProvider();
    }

    @Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
    protected void componentsResolved(InstantiationContext context) {
    	super.componentsResolved(context);
    	
    	this.initCommandGroups();
    }
    
    /**
     * Is called by CTor to setup the (optional) allow Cache.
     * 
     * Override to create a cache with reasonable size. Think about using LRU- or Weak-Maps.
     * 
     * @return always null here.
     * @deprecated Since new SecurityStorage, caching the security is not necessary anymore;
     *             in addition if caching is active, the component will not get actualized
     *             when changes in security are done.
     */
    @Deprecated
	protected Map<Tuple, Boolean> createAllowCache() {
        return null; // return new HashMap() , new HashMapWeak(), new LRUMaAp()
    }

    /**
     * Allow resetting Security (if cached).
     */
    public void resetAllowCache() {
		if (_allowCache != null) {
			_allowCache.clear();
       }
    }

    /**
     * Implemented to (re-) <code>createAllowCache()</code> after deserialization.
     */
    private void readObject(java.io.ObjectInputStream aStream) throws IOException, ClassNotFoundException {
        aStream.defaultReadObject();
		_allowCache = this.createAllowCache();
    }

    /**
     * Return size (if existing) of allowCache
     */
    public final int getAllowSize() {
		return _allowCache != null ? _allowCache.size() : 0;
    }

	/**
	 * Whether the component can be displayed.
	 * 
	 * <p>
	 * Override {@link #hideReason(Object)} for customizations.
	 * </p>
	 */
	@Override
	public final boolean allow() {
		return BoundCheckerComponent.super.allow();
	}

	@Override
	public ResKey hideReason() {
		return hideReason(internalModel());
	}

    @Override
	public ResKey hideReason(Object potentialModel) {
		if (!ComponentUtil.isValid(potentialModel)) {
			return com.top_logic.tool.execution.I18NConstants.ERROR_INVALID_MODEL;
		}

		if (!supportsInternalModel(potentialModel)) {
			return com.top_logic.tool.execution.I18NConstants.ERROR_MODEL_NOT_SUPPORTED;
		}

		return securityReason(potentialModel);
	}

	/**
	 * Utility to compute {@link #hideReason(Object)} based on access restrictions.
	 * 
	 * @param potentialModel
	 *        See {@link #hideReason(Object)}.
	 */
	protected final ResKey securityReason(Object potentialModel) {
		BoundCommandGroup group = getDefaultCommandGroup();
		if (!allow(group, getCurrentObject(group, potentialModel))) {
			return com.top_logic.tool.execution.I18NConstants.ERROR_NO_PERMISSION;
		}

		return null;
	}

    @Override
	public boolean isSecurityMaster() {
        return (this.isSecurityMaster);
    }

    /**
     * Handles the allowCache and calls unCachedAllow meanwhile.
     *
     * @param   aCmdGroup   The CommandGroup to check
     * @param   anObject    Object for which the security should be checked.
     *
     * @return true, if given CommandGroup is allowed to be performed
     *
     * @see com.top_logic.tool.boundsec.BoundChecker#allow(com.top_logic.tool.boundsec.BoundCommandGroup, com.top_logic.tool.boundsec.BoundObject)
     */
    @Override
	public boolean allow(BoundCommandGroup aCmdGroup, BoundObject anObject) {
        allowCount ++;

        // Check, whether model is valid. 
        //
        // Note: This is a workaround for model events arriving in inappropriate
        // order. E.g. an object was deleted, component A fires model deleted
        // event E1. Component B receives this event and re-sets its model and in
        // response fires a security changed event E2. Component C receives E2
        // before E1, because events are delivered synchronously. Component C
        // now queries its subcomponent D, whether it is still allowed with its
        // now invalid model. D did not yet receive the event E1 and had no
        // chance to update its state.
        if (!ComponentUtil.isValid(anObject)) {
            // Logger.info("Tried to check security on an invalid object.", BoundComponent.class);
            return false;
        }
        
		Map<Tuple, Boolean> allowCache = _allowCache;
		Tuple cacheKey;

		if (allowCache == null) {
			allowCache = getSecurityRequestCache();

			if (allowCache == null) {
				// No caching, exit early.
				return unCachedAllow(aCmdGroup, anObject);
            }

			// Note: In the global cache, the component is required as additional part of the key.
			cacheKey = TupleFactory.newTuple(this, anObject, aCmdGroup);
		} else {
			cacheKey = TupleFactory.newTuple(anObject, aCmdGroup);
        }

		Boolean allow = allowCache.get(cacheKey);
		if (allow == null) {
			allow = Boolean.valueOf(unCachedAllow(aCmdGroup, anObject));

			allowCache.put(cacheKey, allow);
        }

		return allow.booleanValue();
    }
    
    public static boolean useSecurityRequestCache(BoundCommand aCommand) {
		return aCommand.getCommandGroup().isReadGroup();
    }

	/**
	 * Install a security cache if not yet installed.
	 * 
	 * @return Whether a new cache was installed, in that case, {@link #uninstallRequestCache()}
	 *         should be called later on.
	 */
	public static boolean installRequestCacheIfNotExisting() {
		if (hasRequestCache()) {
			return false;
		}

		SECURITY_REQUEST_CACHE.set(new HashMap<>());
		return true;
	}
    
	/**
	 * Removes a request cache installed in {@link #installRequestCacheIfNotExisting()}.
	 */
	public static void uninstallRequestCache() {
		SECURITY_REQUEST_CACHE.remove();
    }
    
    /**
	 * Checks, whether a request cache is currently installed.
	 * 
	 * @return true, if a request is installed, false otherwise.
	 */
	private static boolean hasRequestCache() {
		return getSecurityRequestCache() != null;
    }
    
	private static Map<Tuple, Boolean> getSecurityRequestCache() {
		return SECURITY_REQUEST_CACHE.get();
	}

    /**
     * Clear allowCache if {@link #isCacheRelevant(Object)} returns <code>true</code>.
     *
     * @param    aChangedBy    The object sending the event.
     * @return   <code>true</code>, if calling this method changed the internal state.
     *
     * @see com.top_logic.mig.html.layout.LayoutComponent#receiveModelSecurityChangedEvent(java.lang.Object)
     */
    @Override
	protected boolean receiveModelSecurityChangedEvent(Object aChangedBy) {
        if (this.isCacheRelevant(aChangedBy)) {
            resetAllowCache();
        }

        return super.receiveModelSecurityChangedEvent(aChangedBy);
    }

    /**
     * The allow allowCache will be reset when this method says so.
     *
     * @param    aChangedBy    The object sending the event.
     * @return   <code>true</code> if the event is security relevant and allowCache != null.
     * @see      #receiveModelSecurityChangedEvent(Object)
     */
    protected boolean isCacheRelevant(Object aChangedBy) {
		return (_allowCache != null);
    }

    /**
      * Check if the given {@link com.top_logic.tool.boundsec.BoundCommandGroup}
      * for the current
      * {@link com.top_logic.knowledge.wrap.person.Person} is allowed on the given Object.
      *
      * This is called by the normal allow and eventually cached.
     */
    public boolean unCachedAllow(BoundCommandGroup aCmdGroup, BoundObject anObject) {
        TLContext theContext = TLContext.getContext();
        if (theContext == null)
            return false;

        if (aCmdGroup == null) {
            aCmdGroup = getDefaultCommandGroup();
        }

		if (!CommandApprovalService.canExecute(this, aCmdGroup, anObject))
            return false;


        // special object dependent security:
		if (anObject == null) {
			return supportsInternalModel(null);
		}

        if (!checkAccess(theContext, (Object) anObject , aCmdGroup))
            return false;
        if (anObject instanceof Wrapper && !checkAccess(theContext, (Wrapper) anObject , aCmdGroup))
            return false;

        if (!checkAccess(theContext, anObject , aCmdGroup))
            return false;

        return true; // All Tests pass, let's go
    }

    @Override
	public final BoundObject getCurrentObject(BoundCommandGroup aBCG, Object potentialModel) {
		return getSecurityObject(aBCG, potentialModel);
    }

	@Override
	public BoundObject getSecurityObject(BoundCommandGroup commandGroup, Object potentialModel) {
		return getSecurityObjectProvider().getSecurityObject(this, potentialModel, commandGroup);
	}

    /**
     * Return CommandGroups for all registered commands of this Component.
     *
     * The set contains at least the SimpleBoundCommandGroup.READ.
     *
     * @return all command groups for this Component.
     */
    @Override
	public final Collection<BoundCommandGroup> getCommandGroups() {
    	return commandGroups;
    }

    private void initCommandGroups() {
    	this.commandGroups = Collections.unmodifiableSet(this.createCommandGroups());
    }

    private Set<BoundCommandGroup> createCommandGroups() {
    	Set<BoundCommandGroup> theGroups = new HashSet<>();
		getConfig().addAdditionalCommandGroups(theGroups);
    	theGroups.add(SimpleBoundCommandGroup.READ);
    	
		for (CommandHandler command : this.getCommands()) {
			theGroups.add(command.getCommandGroup());
    	}
    	
    	return theGroups;
    }
    
    /**
     * Get the BoundCommandGroup with the given ID
     *
     * @param aBCGId	the ID
     * @return the BCG or <code>null</code> if none is found
     */
    public final BoundCommandGroup resolveCommandGroup(String aBCGId) {
    	return CommandGroupRegistry.resolve(aBCGId);
    }

    /**
     * The Roles for CommandGroups are configured using the PersBoundComp.
     *
     * @param aCommand the command group
     * @return The Roles for CommandGroups are configured using the PersBoundComp.
     */
    @Override
	public Collection getRolesForCommandGroup(BoundCommandGroup aCommand) {
		WindowComponent enclosingWindow = this.getEnclosingWindow();
		if (enclosingWindow != null) {
			LayoutComponent windowOpener = enclosingWindow.getOpener();
			if (windowOpener instanceof BoundChecker) {
				return ((BoundChecker) windowOpener).getRolesForCommandGroup(aCommand);
    		}
    	}
        if (this.usePersBoundComponent()) { // Prevent allocating persistent view objects for anonymous components.
            try {
                PersBoundComp myView = getPersBoundComp();
                return myView.rolesForCommandGroup(aCommand);
            }
            catch (Exception e) {
                Logger.error("failed to getRolesForCommandGroup " + aCommand , e, this);
            }
        }
        return null;
    }

	@Override
	public PersBoundComp getPersBoundComp() {
		return _persBoundComp;
    }

	/**
     * This method indicates whether the persistence via {@link PersBoundComp} should be used.
     * Override this method in case your component has other criteria.
     *
     * @return <code>true</code> to indicate the usage of PersBoundComp
     */
    protected boolean usePersBoundComponent() {
		return getPersBoundComp() != null;
    }

    /**
     * Implement the nothing command by clearing the dialog state.
     *
     * @param ctx       the servlet context
     * @param request   the request
     * @param response  the response
     */
    public /*final*/ void nothing(ServletContext ctx,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        // do what this function says
    }

    /**
     * Check Access based on arbitrary Object.
     *
     * @param context   The current TLContext (to get user data etc.)
     * @param model     The model of this component.
     * @param aCmdGroup The command group to check against.
     *
     * @return always true here but you may do otherwise
     */
    @Deprecated
	protected final boolean checkAccess(TLContext context, Object model, BoundCommandGroup aCmdGroup) {
        return true;
    }

    /**
     * Check Wrapper / KnowledgeObject access right to given model.
     *
     * @param context   The current TLContext (to get user data etc.)
     * @param model     The Wrapper we actually care for
     * @param aCmdGroup The command group to check against.
     *
     * @return false for historic wrappers and !{@link #isReadOrSystem(BoundCommandGroup)}
     */
    protected boolean checkAccess(TLContext context, Wrapper model, BoundCommandGroup aCmdGroup) {
    	if (!isReadOrSystem(aCmdGroup)) {
    		return WrapperHistoryUtils.getRevision(model).isCurrent();
    	}
    	
        return true; // Don't know ... lets say its OK
    }
    
    /**
	 * Checks whether the given command group is 'read' or 'system'.
	 * 
	 * @param cmdGroup
	 *            a command group to check
	 * @return <code>true</code> if the given group is 'read' or 'system',
	 *         <code>false</code> otherwise.
	 */
    protected static final boolean isReadOrSystem(BoundCommandGroup cmdGroup) {
		return cmdGroup.isSystemGroup() || cmdGroup.isReadGroup();
    }

    /**
     * Check Access based on BoundObject .
     *
     * @param context   The current TLContext (to get user data etc.)
     * @param model     The model of this component.
     * @param aCmdGroup The command group to check against.
     *
     * @return true when the intersection of roles for the commandGroups
     *              and roles of the current Person on the BoundObject
     *              is not empty.
     */
    protected boolean checkAccess(TLContext context, BoundObject model, BoundCommandGroup aCmdGroup) {
        if (ThreadContext.isSuperUser()) {
            return true;    // bypass bound security for SuperUsers
        }

        Person currentPerson = context.getCurrentPersonWrapper();
        if (currentPerson == null)
            return false;   // Don't know how to check this

        return allow(currentPerson, model, aCmdGroup);
    }

    /**
     * Check if given Person has access to aModel in this class for given CommandGroup
     */
    @Override
	public boolean allow(Person aPerson, BoundObject aModel, BoundCommandGroup aCmdGroup) {
		if (!SimpleBoundCommandGroup.isAllowedCommandGroup(aPerson, aCmdGroup)) {
        	return false;
        }
		boolean isSystem = aCmdGroup.isSystemGroup();
		if (isSystem) {
			return true;
		}
        return AccessManager.getInstance().hasRole(aPerson, aModel, getRolesForCommandGroup(aCmdGroup));
    }

    @Override
	public boolean isDefaultCheckerFor(String type, BoundCommandGroup aBCG) {
		return this.isDefaultFor(type);
    }

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		fireSecurityChanged(newModel);
	}

    /**
	 * Fire a security event, if the access rights defined on the given objects is different than
	 * before.
	 *
	 * @param newModel
	 *        The new model, may be <code>null</code>.
	 * @return <code>true</code>, if event has been fired.
	 */
	protected boolean fireSecurityChanged(Object newModel) {
		boolean newVisibility = canShow(newModel);

		// Check, whether the visibility has changed.
		if (_currentVisibility != null) {
			boolean oldVisibility = _currentVisibility.booleanValue();
			boolean visibilityChanged = oldVisibility != newVisibility;
			if (!visibilityChanged) {
				return false;
			}
		}

		_currentVisibility = Boolean.valueOf(newVisibility);
		fireModelEvent(newModel, ModelEventListener.SECURITY_CHANGED);
		return true;
    }

    /**
	 * Check, if the given object can be displayed by this component (security and model check!).
	 *
	 * @param potentialModel
	 *        The object to be checked, may be <code>null</code>.
	 * @return <code>true</code>, if the object can be displayed.
	 */
	private boolean canShow(Object potentialModel) {
		return supportsInternalModel(potentialModel) && allowPotentialModel(potentialModel);
    }

	/**
	 * Gets the configured {@link SecurityObjectProvider}.
	 */
	public SecurityObjectProvider getSecurityObjectProvider() {
		return _securityObjectProvider;
	}
}
