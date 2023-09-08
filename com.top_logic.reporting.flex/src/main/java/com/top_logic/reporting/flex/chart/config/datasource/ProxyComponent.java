/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Utils;

/**
 * LayoutComponent-adapter that can be used to simulate a model for other data-sources.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class ProxyComponent extends LayoutComponent implements Selectable {

	private final ProxyComponent _master;

	/**
	 * Creates a new {@link ProxyComponent} initilized with the given model.
	 */
	public ProxyComponent(Object model) throws ConfigurationException {
		this(model, false);
	}

	/**
	 * Creates a new {@link ProxyComponent}
	 * 
	 * @param model
	 *        the model to set
	 * @param asMaster
	 *        true if the model should be set to a ProxyComponent acting as master, false if the
	 *        model should be set directly to the new instance.
	 */
	public ProxyComponent(Object model, boolean asMaster) throws ConfigurationException {
		super(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, TypedConfiguration
			.newConfigItem(LayoutComponent.Config.class));
		_master = asMaster ? new ProxyComponent(model) : null;
		this.setModel(model);
	}

	@Override
	public LayoutComponent getMaster() {
		return _master;
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return true;
	}

	@Override
	public boolean setSelected(Object aSelection) {
		boolean res = Utils.equals(getModel(), aSelection);
		setModel(aSelection);
		return !res;
	}

	@Override
	public Object getSelected() {
		return getModel();
	}

	@Override
	public void clearSelection() {
		throw new UnsupportedOperationException();
	}

}
