/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerDelegate;
import com.top_logic.tool.boundsec.simple.AllowAllChecker;
import com.top_logic.tool.boundsec.simple.AllowNoneChecker;

/**
 * Dummy {@link LayoutComponent}, which has no model and is always visible. Also implements
 * {@link BoundChecker} and allows all.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DummyBoundComponent extends LayoutComponent implements BoundCheckerDelegate {

	private boolean isVisibleAndAllowsAll;

	private BoundChecker _delegate;;

	DummyBoundComponent() throws ConfigurationException {
		super(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, TypedConfiguration
			.newConfigItem(LayoutComponent.Config.class));
		setVisibleAndAllowsAll();
	}

	public void setVisibleAndAllowsAll() {
		this.isVisibleAndAllowsAll = true;
		_delegate = new AllowAllChecker(getName());
	}

	public void setInvisibleAndDisallowsAll() {
		this.isVisibleAndAllowsAll = false;
		_delegate = new AllowNoneChecker(getName());
	}

	@Override
	public boolean isVisible() {
		return isVisibleAndAllowsAll;
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return true;
	}

	@Override
	public BoundChecker getDelegate() {
		return _delegate;
	}

	@Override
	public ResKey hideReason() {
		return hideReason(internalModel());
	}

}