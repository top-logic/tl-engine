/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.event;

import com.top_logic.dob.DataObjectException;

/**
 * Chain of two {@link Modification}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModificationPair implements Modification {

	private final Modification _second;

	private final Modification _first;

	/**
	 * Creates a new {@link ModificationChain}.
	 * 
	 * @param first
	 *        The {@link Modification} to execute first.
	 * @param second
	 *        The {@link Modification} to execute second.
	 */
	public ModificationPair(Modification first, Modification second) {
		_first = first;
		_second = second;
	}

	@Override
	public void execute() throws DataObjectException {
		_first.execute();
		_second.execute();
	}

	@Override
	public Modification andThen(Modification after) {
		ModificationChain chain = new ModificationChain();
		chain.addModification(_first);
		chain.addModification(_second);
		chain.addModification(after);
		return chain;
	}

}
