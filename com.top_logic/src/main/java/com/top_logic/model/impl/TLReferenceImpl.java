/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;


import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.Protocol;
import com.top_logic.layout.security.AccessChecker;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;

/**
 * Default implementation of {@link TLReference}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLReferenceImpl extends AbstractStructuredTypePart<TLClass> implements TLReference {

	private TLAssociationEnd end;

	/*package protected*/ TLReferenceImpl(TLModelImpl model, String name) {
		super(model, name);
		updateDefinition();
	}

	@Override
	void internalSetOwner(TLClass value) {
		super.internalSetOwner(value);
		updateDefinition();
	}

	@Override
	public void setType(TLType value) {
		checkState();
		this.end.setType(value);
	}

	@Override
	public TLType getType() {
		checkState();
		return end.getType();
	}
	
	@Override
	public boolean isMandatory() {
		checkState();
		return getEnd().isMandatory();
	}
	
	@Override
	public void setMandatory(boolean value) {
		checkState();
		getEnd().setMandatory(value);
	}
	
	@Override
	public boolean isMultiple() {
		checkState();
		return getEnd().isMultiple();
	}

	@Override
	public void setMultiple(boolean value) {
		checkState();
		getEnd().setMultiple(value);
	}

	@Override
	public boolean isBag() {
		checkState();
		return getEnd().isBag();
	}

	@Override
	public void setBag(boolean value) {
		checkState();
		getEnd().setBag(value);
	}

	@Override
	public boolean isOrdered() {
		checkState();
		return getEnd().isOrdered();
	}

	@Override
	public void setOrdered(boolean value) {
		checkState();
		getEnd().setOrdered(value);
	}

	@Override
	public boolean isAbstract() {
		checkState();
		return getEnd().isAbstract();
	}

	@Override
	public void setAbstract(boolean value) {
		checkState();
		getEnd().setAbstract(value);
	}

	private void checkState() {
		if (this.end == null) {
			throw new IllegalStateException("Reference '" + this + "' has not yet an end assigned.");
		}
	}
	
	@Override
	public TLAssociationEnd getEnd() {
		return this.end;
	}
	
	@Override
	public void setEnd(TLAssociationEnd newValue) {
		TLAssociationEnd oldValue = this.end;
		if (newValue == oldValue) {
			return;
		}
		
		// Check new value.
		if (newValue != null) {
			TLAssociationEndImpl newEnd = (TLAssociationEndImpl) newValue;
			TLReference oldReference = newEnd.getReference();
			if (oldReference != null) {
				if (oldReference != this) {
					throw new IllegalArgumentException(
						"An association end '" + newValue + "' may only implement one reference attribute: Already implements '" + oldReference + "', an attempt was made to also implement '" + this + "'");
				}
			}
		}

		if (oldValue != null) {
			// Detach from old end.
			TLAssociationEndImpl oldEnd = (TLAssociationEndImpl) oldValue;
			oldEnd.internalSetReference(null);
		}

		if (newValue != null) {
			// Attach to new end.
			TLAssociationEndImpl newEnd = (TLAssociationEndImpl) newValue;
			newEnd.internalSetReference(this);
		}
		
		this.end = newValue;
	}
	
	@Override
	protected void internalResolve(Protocol log) {
		if (end == null) {
			log.error("No association end in reference '" + this + "'.");
		} 
		
		super.internalResolve(log);
	}

	@Override
	public Set<? extends TLObject> getReferers(TLObject element) {
		// Not available in transient models.
		return Collections.emptySet();
	}

	@Override
	public AccessChecker getAccessChecker() {
		return getEnd().getAccessChecker();
	}
}
