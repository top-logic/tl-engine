/*
 * Copyright (c) 2024 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.model.composite;

import java.util.Objects;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;

/**
 * {@link TargetTable} whose container column is used by more than one {@link TLReference}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PolymorphicContainerColumn extends TargetTable {

	private String _referenceColumn;

	/**
	 * Creates a new {@link PolymorphicContainerColumn}.
	 * 
	 * @param referenceColumn
	 *        Name of the column containing the composite reference.
	 */
	public PolymorphicContainerColumn(String container, String referenceColumn) {
		super(container);
		_referenceColumn = referenceColumn;
	}

	@Override
	public TLReference getReference(TLObject part) {
		TLReference compositeRef = part.tGetDataReference(TLReference.class, _referenceColumn);
		if (compositeRef != null) {
			// Container found
			TLObject container = part.tGetDataReference(TLObject.class, getContainer());
			return (TLReference) container.tType().getPartOrFail(compositeRef.getName());
		}
		return null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getContainer(), _referenceColumn);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PolymorphicContainerColumn other = (PolymorphicContainerColumn) obj;
		return Objects.equals(getContainer(), other.getContainer())
				&& Objects.equals(_referenceColumn, other._referenceColumn);
	}

}

