/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.manager;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.func.Function0;
import com.top_logic.model.TLModule;
import com.top_logic.util.model.ModelService;

/**
 * {@link Function0} determining the {@link TLModule}s that are used as security domains.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityStructures extends Function0<List<TLModule>> {

	/** Singleton {@link SecurityStructures} instance. */
	public static final SecurityStructures INSTANCE = new SecurityStructures();

	/**
	 * Creates a new {@link SecurityStructures}.
	 */
	protected SecurityStructures() {
		// singleton instance
	}

	@Override
	public List<TLModule> apply() {
		return AccessManager.getInstance().getStructureNames()
			.stream()
			.map(ModelService.getApplicationModel()::getModule)
			.collect(Collectors.toList());
	}

}

