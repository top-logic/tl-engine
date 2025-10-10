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
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.component.LayoutContainerBoundChecker;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.mig.html.layout.SubComponentConfig;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerLayoutConfig;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundLayout;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.tool.boundsec.SecurityObjectProviderConfig;
import com.top_logic.tool.boundsec.WithSecurityMaster;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.util.Resources;

/**
 * Extension of AssistentComponent making it security aware.
 *
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class BoundAssistentComponent extends AssistentComponent implements LayoutContainerBoundChecker, HTMLConstants {

	/**
	 * Configuration options for {@link BoundAssistentComponent}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends AssistentComponent.Config, BoundCheckerLayoutConfig, SecurityObjectProviderConfig,
			WithSecurityMaster {

		/**
		 * @see SubComponentConfig#getComponents()
		 */
		String TAG_NAME = "assistent";

		@Override
		@ClassDefault(BoundAssistentComponent.class)
		public Class<? extends LayoutComponent> getImplementationClass();

		@Override
		@FormattedDefault("model")
		PolymorphicConfiguration<? extends SecurityObjectProvider> getSecurityObject();
	}

	private Collection<BoundCommandGroup> commandGroups;

    private Boolean securityState;

    private boolean isSecurityMaster;

	private final SecurityObjectProvider _securityProvider;


	public BoundAssistentComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		this.isSecurityMaster = config.getIsSecurityMaster();

		this.commandGroups = config.getCommandGroups();
		if (commandGroups.isEmpty()) {
			commandGroups = SimpleBoundCommandGroup.READ_SET;
        }
		_securityProvider = SecurityObjectProvider.fromConfiguration(context, config.getSecurityObject());
    }

	@Override
	protected void componentsResolved(InstantiationContext context) {
		super.componentsResolved(context);

		if (isSecurityMaster) {
			if (getParent() instanceof BoundLayout layout) {
				layout.initSecurityMaster(this);
			}
		}
	}

	@Override
	public ResKey hideReason() {
		return BoundChecker.hideReasonForSecurity(this, internalModel());
	}

	@Override
	public SecurityObjectProvider getSecurityObjectProvider() {
		return _securityProvider;
	}

    @Override
	public Collection<BoundCommandGroup> getCommandGroups() {
        return this.commandGroups;
    }

    @Override
	public Set<? extends BoundRole> getRolesForCommandGroup(BoundCommandGroup aCommand) {
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
     * Check if given Person has access to aModel in this class for given command group
     */
    @Override
	public boolean allow(Person aPerson, BoundObject aModel,
            BoundCommandGroup aCmdGroup) {
        return AccessManager.getInstance().hasRole(aPerson, aModel, getRolesForCommandGroup(aCmdGroup));
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
		return this.supportsInternalModel(anObject) && BoundChecker.allowShowSecurityObject(this, anObject);
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
