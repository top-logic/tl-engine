/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.meta.form.component.EditAttributedComponent;
import com.top_logic.element.structured.wrap.Mandator;

/**
 * Show overview page for mandators with some attributes for normal user (no admin page).
 * 
 * @author   Boris Hayen
 */
public class MandatorOverviewComponent extends EditAttributedComponent {

	/**
	 * Configuration options for {@link MandatorOverviewComponent}.
	 */
	public interface Config extends EditAttributedComponent.Config {

		@Override
		@StringDefault(StructuredElementApplyHandler.COMMAND)
		String getApplyCommand();

	}

	public MandatorOverviewComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
		super(context, someAttrs);
	}

    /** 
     * We must cache the securtiy since we are used by the ContractTable.
     */
    @Override
	protected Map<Tuple, Boolean> createAllowCache() {
		return new HashMap<>(64);
    }

	/**
	 * We support {@link Mandator}s what did you expect.
	 */
	@Override
	protected boolean supportsInternalModelNonNull(Object anObject) {
		return (anObject instanceof Mandator);
	}

	/**
	 * @see com.top_logic.element.meta.form.component.EditAttributedComponent#getMetaElementName()
	 */
    @Override
	protected String getMetaElementName() {
        return Mandator.MANDATOR_TYPE;
    }
}
