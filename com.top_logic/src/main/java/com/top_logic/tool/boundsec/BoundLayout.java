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
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;

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
	public interface Config extends Layout.Config, BoundCheckerLayoutConfig, SecurityObjectProviderConfig {

		/**
		 * Short-cut name for {@link BoundLayout} in XML configuration.
		 */
		String TAG_NAME = "layout";

		@Override
		@ClassDefault(BoundLayout.class)
		public Class<? extends LayoutComponent> getImplementationClass();

	}

	/** The component to delegate access checks to. */
	private BoundCheckerComponent _securityMaster;

	private SecurityObjectProvider _securityObjectProvider;

    /** Construct a BoundLayout from (XML-)Attributes. */
	public BoundLayout(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);

		_securityObjectProvider = atts.resolveSecurityObject(context);
    }

	@Override
	public SecurityObjectProvider getSecurityObjectProvider() {
		BoundCheckerComponent securityMaster = securityMaster();
		if (securityMaster != null) {
			return securityMaster.getSecurityObjectProvider();
		}
		return _securityObjectProvider;
	}

	@Override
	public ResKey hideReason() {
		BoundCheckerComponent securityMaster = securityMaster();
		if (securityMaster != null) {
			return securityMaster.hideReason();
		}

		ResKey technicalReason = super.hideReason();
		if (technicalReason != null) {
			return technicalReason;
		}

		ResKey securityReason = BoundChecker.hideReasonForSecurity(this, internalModel());
		if (securityReason != null) {
			return securityReason;
		}

		return null;
	}

	private BoundCheckerComponent securityMaster() {
		if (_securityMaster != null) {
			return _securityMaster;
		}

		if (getChildCount() == 1) {
			/* When there is only one child, treat it as security master. */
			LayoutComponent child = getChild(0);
			if (child instanceof BoundCheckerComponent childChecker) {
				// Only BoundCheckerComponent can be "security master".
				return childChecker;
			}
		}
		return null;
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
		if (_securityMaster != null && !_securityMaster.getName().equals(masterComponent.getName())) {
			Logger.warn("Non-unique security master components in layout '" + getName() + "': "
					+ _securityMaster.getName() + " and " + masterComponent.getName(),
				BoundLayout.class);
		}
		_securityMaster = masterComponent;

		// Forward to ancestors.
		if (getParent() instanceof BoundLayout layout) {
			layout.initSecurityMaster(masterComponent);
		}
	}

}
