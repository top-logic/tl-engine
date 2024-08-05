/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.top_logic.dob.DataObjectException;

/**
 * Chain of multiple {@link Modification}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModificationChain implements Modification {

	private final List<Modification> _modifications;

	/**
	 * Creates a new {@link ModificationChain}.
	 */
	public ModificationChain() {
		_modifications = new ArrayList<>();
	}

	@Override
	public void execute() throws DataObjectException {
		for (int i = 0, size = _modifications.size(); i < size; i++) {
			_modifications.get(i).execute();
		}
	}

	/**
	 * Adds the given {@link Modification} to the modifications to execute.
	 */
	public void addModification(Modification modification) {
		_modifications.add(modification);
	}

	@Override
	public Modification andThen(Modification after) {
		if (after instanceof ModificationChain chain) {
			_modifications.addAll(chain._modifications);
		} else {
			Objects.requireNonNull(after);
			_modifications.add(after);
		}
		return this;
	}

}
