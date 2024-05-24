/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.composite;

import java.util.Objects;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;

/**
 * {@link TargetTable} whose container column is only used by one reference.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MonomorphicContainerColumn extends TargetTable {

	private TLReference _reference;

	/**
	 * Creates a new {@link MonomorphicContainerColumn}.
	 * 
	 * @param reference
	 *        The represented composite reference.
	 */
	public MonomorphicContainerColumn(String container, TLReference reference) {
		super(container);
		_reference = reference;
	}

	@Override
	public TLReference getReference(TLObject part) {
		return _reference;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getContainer(), _reference);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MonomorphicContainerColumn other = (MonomorphicContainerColumn) obj;
		return Objects.equals(getContainer(), other.getContainer()) && Objects.equals(_reference, other._reference);
	}

}

