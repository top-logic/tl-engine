/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.mig.html.layout.SubComponentConfig;
import com.top_logic.tool.boundsec.BoundCheckerComponent;
import com.top_logic.tool.boundsec.BoundCheckerLayoutConfig;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;
import com.top_logic.tool.boundsec.WithSecurityMaster;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.tool.execution.I18NConstants;
import com.top_logic.tool.execution.service.CommandApprovalService;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Extension of AssistentComponent making it security aware.
 *
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class BoundAssistentComponent extends AssistentComponent implements BoundCheckerComponent, HTMLConstants {

	/**
	 * Configuration options for {@link BoundAssistentComponent}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends AssistentComponent.Config, BoundCheckerLayoutConfig, WithSecurityMaster {

		/**
		 * @see SubComponentConfig#getComponents()
		 */
		String TAG_NAME = "assistent";

		@Override
		@ClassDefault(BoundAssistentComponent.class)
		public Class<? extends LayoutComponent> getImplementationClass();

		@Name(BoundComponent.XML_ATTRIBUTE_SECURITY_PROVIDER_CLASS)
		String getSecurityProviderClass();
	}

	private Collection<BoundCommandGroup> commandGroups;

    private Boolean securityState;

    private boolean isSecurityMaster;

    /** Saves whether a SecurityProviderClass was configured in the layout xml. */
    protected boolean securityProviderConfigured;
    
    /** Our persisent representation for the security */
	private final PersBoundComp _persBoundComp;

    /**
     * Saves the configured {@link SecurityObjectProvider}.<br/>
     * This will override useDefaultChecker flag if set.
     */
    protected SecurityObjectProvider securityObjectProvider;
	/** Saves the config to write is back. */
	protected String securityProviderConfigContent;


    public BoundAssistentComponent(InstantiationContext context, Config someAttr) throws ConfigurationException {
        super(context, someAttr);

        this.isSecurityMaster = someAttr.getIsSecurityMaster();

		this.commandGroups = someAttr.getCommandGroups();
		if (commandGroups.isEmpty()) {
			commandGroups = SimpleBoundCommandGroup.READ_SET;
        }
		_persBoundComp = SecurityComponentCache.lookupPersBoundComp(this);
        try {
			initSecurityObjectProvider(context, someAttr);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Initializing the security object provider failed.", ex);
		}
    }

	/**
	 * Fetch the {@link #securityObjectProvider} from XML and set {@link #securityProviderConfigured}
	 * 
	 * @throws ConfigurationException
	 *         If the configured {@link SecurityObjectProvider} cannot be
	 *         instantiated.
	 */
    protected void initSecurityObjectProvider(InstantiationContext context, Config atts) throws ConfigurationException {
    	securityProviderConfigContent = StringServices.nonEmpty(atts.getSecurityProviderClass());
        securityProviderConfigured = !StringServices.isEmpty(securityProviderConfigContent);
        if (securityProviderConfigured) {
            securityObjectProvider = SecurityObjectProviderManager.getInstance().getSecurityObjectProvider(securityProviderConfigContent);
        } else {
            securityObjectProvider = getDefaultSecurityObjectProvider();
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
	public PersBoundComp getPersBoundComp() {
		return _persBoundComp;
    }

	@Override
	public ResKey hideReason() {
		return hideReason(internalModel());
	}

    @Override
	public ResKey hideReason(Object potentialModel) {
		return securityReason(potentialModel);
    }

	/**
	 * Utility to compute {@link #hideReason(Object)} based on access restrictions.
	 */
	protected final ResKey securityReason(Object potentialModel) {
		BoundCommandGroup group = this.getDefaultCommandGroup();
		if (!allow(group, getCurrentObject(group, potentialModel))) {
			return I18NConstants.ERROR_NO_PERMISSION;
		}
		return null;
	}

    /** 
      * Check if the given {@link com.top_logic.tool.boundsec.BoundCommandGroup} 
      * for the current 
      * {@link com.top_logic.knowledge.wrap.person.Person} is allowed on the given Object.
      * 
      * @see com.top_logic.tool.boundsec.BoundChecker#allow(com.top_logic.tool.boundsec.BoundCommandGroup, com.top_logic.tool.boundsec.BoundObject)
     */
    @Override
	public boolean allow(BoundCommandGroup aCmdGroup, BoundObject anObject) {
        TLContext theContext = TLContext.getContext();
        if (theContext == null)
            return false;

        if (aCmdGroup == null) {
            aCmdGroup = getDefaultCommandGroup();
        }

		if (!CommandApprovalService.canExecute(this, aCmdGroup, anObject))
            return false;


        // special object dependent security:
        if (anObject == null)
            return allowNullModel(aCmdGroup);

        
        if (!checkAccess(theContext, (Object) anObject , aCmdGroup))
            return false;
        if (anObject instanceof Wrapper && !checkAccess(theContext, (Wrapper) anObject , aCmdGroup))
            return false;

        return checkAccess(theContext, anObject , aCmdGroup);
    }

    @Override
	public final BoundObject getCurrentObject(BoundCommandGroup aBCG, Object potentialModel) {
		return getSecurityObject(aBCG, getModel());
    }

	@Override
	public BoundObject getSecurityObject(BoundCommandGroup commandGroup, Object potentialModel) {
		return this.securityObjectProvider.getSecurityObject(this, potentialModel, commandGroup);
	}

    @Override
	public Collection<BoundCommandGroup> getCommandGroups() {
        return this.commandGroups;
    }

    @Override
	public Collection getRolesForCommandGroup(BoundCommandGroup aCommand) {
        PersBoundComp persBoundComp = getPersBoundComp();
		if (persBoundComp == null) {
			return Collections.emptySet();
		}
		return persBoundComp.rolesForCommandGroup(aCommand);
    }

    @Override
	public boolean isDefaultCheckerFor(String type, BoundCommandGroup aBCG) {
		return this.isDefaultFor(type);
    }
    
    /**
     * Specify if a Null-Model (non BoundObject) should allow access.
     * 
     * @param aCmdGroup The command group to execute.
     * @return always true here
     */
    protected boolean allowNullModel(BoundCommandGroup aCmdGroup) {
        return this.supportsInternalModel(null);
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
    protected boolean checkAccess(TLContext context, Object model, BoundCommandGroup aCmdGroup) {
        return true;
    }

    /**
     * Check KnowledgeObject access right to given model.
     * 
     * @return true always true - remains as a hook for subclasses
     */
    protected boolean checkAccess(TLContext context, Wrapper model, BoundCommandGroup aCmdGroup) {
        return true;
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
		if (TLContext.isAdmin()) {
            return true;    // bypass bound security for SuperUsers
        }
		Person currentPerson = context.getPerson();
        if (currentPerson == null)
            return false;   // Don't know how to check this
        
        return allow(currentPerson, model, aCmdGroup);
    }
    
    /** 
     * Check if given Person has access to aModel in this class for given command group
     */
    @Override
	public boolean allow(Person aPerson, BoundObject aModel,
            BoundCommandGroup aCmdGroup) {
        return AccessManager.getInstance().hasRole(aPerson, aModel, getRolesForCommandGroup(aCmdGroup));
    }

    @Override
	public boolean isSecurityMaster() {
        return (this.isSecurityMaster);
    }

    public List getProgressRelevantChildren() {
		List progressChilds = new ArrayList(getChildList());
		
		// The first step needs no progress view
		if (firstStepWithoutProgress()) {
			progressChilds.remove(0);
		}
		
		return progressChilds;
	}

    /**
     * Fire a security event, if the security defined on the given objects is different.
     *
     * @param    aNew     The new model, may be <code>null</code>.
     * @return   <code>true</code>, if event has been fired.
     */
    protected boolean fireSecurityChanged(Object aNew) {
        if (aNew instanceof BoundObject) {
            return (this.fireSecurityChanged((BoundObject) aNew));
        }
        else {
            securityState = null; // Reset securityState in case of change from null <->  BoundObject
			fireModelEvent(aNew, ModelEventListener.SECURITY_CHANGED);
			return true;
        }
    }

    /**
     * Fire a security event, if the security defined on the given objects is different.
     *
     * @param    aNew     The new model, may be <code>null</code>.
     * @return   <code>true</code>, if event has been fired.
     */
    protected boolean fireSecurityChanged(BoundObject aNew) {
        boolean theNew    = this.allowVisibility(aNew);
        boolean theResult = securityState == null || theNew != securityState.booleanValue();

        if (theResult) {
			fireModelEvent(aNew, ModelEventListener.SECURITY_CHANGED);

            this.securityState = Boolean.valueOf(theNew);
        }

        return (theResult);
    }

    /**
     * Check, if the given object can be displayed by this component (security and model check!).
     *
     * @param    anObject    The object to be checked, may be <code>null</code>.
     * @return   <code>true</code>, if the object can be displayed.
     * @see      #fireSecurityChanged(BoundObject)
     */
    protected boolean allowVisibility(BoundObject anObject) {
		return this.supportsInternalModel(anObject) && this.allow(anObject);
    }

    /**
	 * This method returns <code>true</code> if and only if the first step is
	 * an info step that needs no progress information, otherwise
	 * <code>false</code>.
	 */
    public boolean firstStepWithoutProgress() {
    	return true;
    }
    
    /**
     * @deprecated please use a {@link AssistentProgressComponent} with horizontal="true"
     */
	@Deprecated
	public void writeStepProgress(DisplayContext aContext, TagWriter aOut) throws IOException {
		Resources  resource = Resources.getInstance();
		ThemeImage okImage = com.top_logic.util.monitor.Icons.GREEN;
		
		// Div start
		aOut.beginBeginTag(DIV);
		aOut.writeAttribute(CLASS_ATTR, "assistantContent");
		aOut.writeAttribute(ALIGN_ATTR, "center");
		aOut.endBeginTag();
		
		// List start
		aOut.beginBeginTag(OL);
		aOut.writeAttribute(CLASS_ATTR, "assistantList");
		aOut.endBeginTag();
		
		List progressChildren = getProgressRelevantChildren();
		for (int i = 0; i < progressChildren.size(); i++) {
			LayoutComponent child            = (LayoutComponent) progressChildren.get(i);
			int             currenChildIndex = progressChildren.indexOf(getCurrentStep());
			
			// List element start
			String classStr = "assistantElement";
			aOut.beginBeginTag(LI);
			if (i == 0) {
				classStr += " assistantFirstElement";
			}

			if (i < currenChildIndex) {
				classStr += " assistantPrev";
			} else if (i == currenChildIndex) {
				classStr += " assistantCurrentStep";
			} else {
				classStr += " assistantNext";
			}
			aOut.writeAttribute(CLASS_ATTR, classStr);
			aOut.endBeginTag();
			
			aOut.writeContent(
				resource.getString(getResPrefix().append("progressStepName.").key(child.getName().qualifiedName())));
			
			if (i < currenChildIndex) {
				// Image
				aOut.writeText(" ");
				okImage.writeWithCss(aContext, aOut, "assistantImage");
			}
			
			// List element end
			aOut.endTag(LI);
		}
		
		// List end
		aOut.endTag(OL);
		
		// Div end
		aOut.endTag(DIV);	

	}
    
}
