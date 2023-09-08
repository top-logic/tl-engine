/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;
import java.util.Optional;

import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;

/**
 * {@link ExecutabilityRule} checking for {@link TLObject#tDeleteVeto()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeleteVetoDisabledRule implements ExecutabilityRule {

	/**
	 * Singleton {@link DeleteVetoDisabledRule} instance.
	 */
	public static final DeleteVetoDisabledRule INSTANCE = new DeleteVetoDisabledRule();

	private DeleteVetoDisabledRule() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (model instanceof TLObject) {
			Optional<ResKey> veto = ((TLObject) model).tDeleteVeto();
			if (veto.isPresent()) {
				return ExecutableState.createDisabledState(veto.get());
			}
		}
		return ExecutableState.EXECUTABLE;
	}

}
