/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.progress;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.progress.AbstractProgressComponent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.reporting.report.model.FilterVO;
import com.top_logic.reporting.view.component.AbstractProgressFilterComponent;


/**
 * @author    <a href=mailto:olb@top-logic.com>olb</a>
 */
public class ApplyFilterProgressComponent extends AbstractProgressComponent {
	
	public static final ComponentName COMPONENT_ID =
		ComponentName.newName("dialogs/progressDialog.layout.xml", "ApplyFilterProgressComponent");
	private AbstractProgressFilterComponent<FilterVO> openerComp;

	/**
	 * Creates a {@link ApplyFilterProgressComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ApplyFilterProgressComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return true;
	}

	@Override
	public void finishParent() {
		if (this.openerComp != null) {
			AbstractProgressFilterComponent<?> progressFilterComponent = this.openerComp;
			progressFilterComponent.fireEventToSlave();
		} 
		else {
			throw new IllegalArgumentException("The opener is not set");
		}
	}

	public void setOpener(AbstractProgressFilterComponent<FilterVO> anOpenerComp) {
		this.openerComp = anOpenerComp;
	}
}

