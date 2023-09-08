/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.transform;

import java.util.Iterator;

import com.top_logic.basic.Protocol;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;

/**
 * {@link ModelTransformation} that removes empty {@link TLModule}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RemoveEmptyModules extends ModelTransformation {

	/**
	 * Creates a {@link RemoveEmptyModules} transformation.
	 *
	 * @param log
	 *        See {@link ModelTransformation#ModelTransformation(Protocol, TLModel)}.
	 * @param index
	 *        See {@link ModelTransformation#ModelTransformation(Protocol, TLModel)}.
	 */
	public RemoveEmptyModules(Protocol log, TLModel index) {
		super(log, index);
	}

	@Override
	public void transform() {
		for (Iterator<TLModule> it = index.getModules().iterator(); it.hasNext(); ) {
			TLModule module = it.next();
			if (module.getTypes().isEmpty()) {
				it.remove();
			}
		}
	}

}
