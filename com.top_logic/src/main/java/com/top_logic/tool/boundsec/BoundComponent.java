/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.basic.component.AJAXComponent;
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

/**
 * A {@link com.top_logic.mig.html.layout.LayoutComponent} implementing the
 * {@link com.top_logic.tool.boundsec.BoundChecker} interface.
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

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			AJAXComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerCommand(GotoHandler.COMMAND);
		}

	}

    /** {@link ThreadLocal} for accessing the allow cache */
	private static final ThreadLocal<Map<Tuple, Boolean>> SECURITY_REQUEST_CACHE = new ThreadLocal<>();

    /**
     * The command groups used by this component
     *
     * Not used here, but used by many subclasses.
     */
    private   Collection<BoundCommandGroup> commandGroups;
    
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
	public BoundComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		isSecurityMaster = config.getIsSecurityMaster();
		_securityObjectProvider = SecurityObjectProvider.fromConfiguration(context, config.getSecurityObject());
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
    
	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
    protected void componentsResolved(InstantiationContext context) {
    	super.componentsResolved(context);
    	
    	this.initCommandGroups();

		if (isSecurityMaster) {
			if (getParent() instanceof BoundLayout layout) {
				layout.initSecurityMaster(this);
			}
		}
    }

	@Override
	public ResKey hideReason() {
		ResKey hideReason = super.hideReason();
		if (hideReason != null) {
			return hideReason;
		}

		return BoundChecker.hideReasonForSecurity(this, internalModel());
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
	public Set<? extends BoundRole> getRolesForCommandGroup(BoundCommandGroup aCommand) {
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
     * Check if given Person has access to aModel in this class for given CommandGroup
     */
    @Override
	public boolean allow(Person aPerson, BoundObject aModel, BoundCommandGroup aCmdGroup) {
		boolean isSystem = aCmdGroup.isSystemGroup();
		if (isSystem) {
			return true;
		}
		if (!SimpleBoundCommandGroup.isAllowedCommandGroup(aPerson, aCmdGroup)) {
        	return false;
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
		return supportsInternalModel(potentialModel) && BoundChecker.allowShowModel(this, potentialModel);
    }

	@Override
	public SecurityObjectProvider getSecurityObjectProvider() {
		return _securityObjectProvider;
	}
}
