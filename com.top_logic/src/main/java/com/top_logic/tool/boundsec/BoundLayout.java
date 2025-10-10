/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.component.LayoutContainerBoundChecker;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

/**
 * A Layout that that will care for Bound security.
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class BoundLayout extends Layout implements LayoutContainerBoundChecker {

	/**
	 * Configuration options for {@link BoundLayout}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends Layout.Config, BoundCheckerLayoutConfig {

		/**
		 * Short-cut name for {@link BoundLayout} in XML configuration.
		 */
		String TAG_NAME = "layout";

		@Override
		@ClassDefault(BoundLayout.class)
		public Class<? extends LayoutComponent> getImplementationClass();

	}

	/** The component to delegate access checks to. */
	private BoundCheckerComponent securityMaster;

    /** Construct a BoundLayout from (XML-)Attributes. */
	public BoundLayout(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
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
	public ResKey hideReason() {
		if (securityMaster != null) {
			return securityMaster.hideReason();
        }

		return super.hideReason();
	}

	/**
	 * Initializes the child component that is responsible for for checking access rights.
	 * 
	 * <p>
	 * This method must be called from components with a {@link WithSecurityMaster} configuration on
	 * their parent component.
	 * </p>
	 */
	public void initSecurityMaster(BoundCheckerComponent masterComponent) {
		if (securityMaster != null && !securityMaster.getName().equals(masterComponent.getName())) {
			Logger.warn("Non-unique security master components in layout '" + getName() + "': "
				+ securityMaster.getName() + " and " + masterComponent.getName(), BoundLayout.class);
		}
		securityMaster = masterComponent;

		// Forward to ancestors.
		if (getParent() instanceof BoundLayout layout) {
			layout.initSecurityMaster(masterComponent);
		}
	}

}
