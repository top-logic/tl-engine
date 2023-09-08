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
public class BoundLayout extends Layout implements BoundCheckerDelegate, SecurityMasterBoundChecker {

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

    /** Cached first child that is {@link BoundCheckerComponent#isSecurityMaster()}. */
    private BoundCheckerComponent securityMaster;

    /** Sticky flag if securityMaster is valid (even if null). */
    private boolean validSecurityMaster;

	private final BoundChecker _boundCheckerDelegate = new LayoutContainerBoundChecker<>(this);

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
	public BoundChecker getDelegate() {
		return _boundCheckerDelegate;
    }

	@Override
	public ResKey hideReason() {
		return hideReason(internalModel());
	}

    /**
     * Fetch {@link #securityMaster} from my children.
     */
    protected void findSecurityMaster() {
        int count = getChildCount();
        for (int i=0; i < count; i++) {
            LayoutComponent theCurrent = getChild(i);

            if (theCurrent instanceof BoundCheckerComponent) {
                BoundCheckerComponent theBoundComp = (BoundCheckerComponent) theCurrent;

                if (theBoundComp.isSecurityMaster()) {
                    if (securityMaster != null) {
                        Logger.warn("More than one SecurityMaster " + securityMaster + "|?|" + theBoundComp , this);
                    }
                    else {
                        securityMaster = theBoundComp;
                    }
                }
            }
        }
        validSecurityMaster = true;
    }


    @Override
	public boolean allowBySecurityMaster(BoundObject anObject, boolean useDefaultChecker) {
        if (!validSecurityMaster) {
            findSecurityMaster();
        }
        if (securityMaster != null) {
			if (!((useDefaultChecker || ((LayoutComponent) securityMaster).supportsModel(anObject))
				&& securityMaster.allow(anObject))) {
                return false;
            }
        }
        int count = getChildCount();
		for (LayoutComponent theCurrent : getChildList()) {
            if (theCurrent instanceof SecurityMasterBoundChecker) {
                if (!((SecurityMasterBoundChecker)theCurrent).allowBySecurityMaster(anObject, useDefaultChecker)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
	public ResKey hideReasonMaster() {
        if (!validSecurityMaster) {
            findSecurityMaster();
        }
		if (securityMaster != null) {
			ResKey masterReason = securityMaster.hideReason();
			if (masterReason != null) {
				return masterReason;
			}
        }
		for (LayoutComponent theCurrent : getChildList()) {
            if (theCurrent instanceof SecurityMasterBoundChecker) {
                ResKey masterReason = ((SecurityMasterBoundChecker)theCurrent).hideReasonMaster();
				if (masterReason != null) {
					return masterReason;
                }
            }
        }
		return null;
    }
    
}
