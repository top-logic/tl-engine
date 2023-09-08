/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.ObservableList;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Default implementation of {@link TLClass}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLClassImpl extends AbstractTLStructuredType<TLClassPart> implements TLClass {

	// Core properties.
	
	private boolean abstractMod;
	private boolean finalMod;
	
	private final List<TLClass> extendedClasses = new ObservableList<>() {

		@Override
		protected void beforeAdd(TLClass key) {
			TLClassImpl generalization = (TLClassImpl) key;
			boolean added = generalization.specializationsInternal.add(TLClassImpl.this);
			if (!added) {
				throw new IllegalStateException("Super class '" + generalization.getName()
					+ "' already is in the extends list.");
			}
			TLTypePartCommon.updatePartDefinitions(TLClassImpl.this);
		}

		@Override
		protected void afterRemove(TLClass key) {
			TLClassImpl generalization = (TLClassImpl) key;
			generalization.specializationsInternal.remove(TLClassImpl.this);
			TLTypePartCommon.updatePartDefinitions(TLClassImpl.this);
		}

	};

	// Computed properties.

	/* package protected */final Set<TLClass> specializationsInternal = new HashSet<>();

	private final Set<TLClass> specializations = Collections.unmodifiableSet(specializationsInternal);

	/**
	 * Flag for cycle check during resolve.
	 */
	private boolean resolveRunning;
	
	TLClassImpl(TLModel model, String name) {
		super(model, name);
	}
	
	@Override
	public boolean isAbstract() {
		return this.abstractMod;
	}

	@Override
	public void setAbstract(boolean value) {
		this.abstractMod = value;
	}
	
	@Override
	public boolean isFinal() {
		return this.finalMod;
	}
	
	@Override
	public void setFinal(boolean value) {
		this.finalMod = value;
	}

	@Override
	public List<TLClass> getGeneralizations() {
		return this.extendedClasses;
	}

	@Override
	public Collection<TLClass> getSpecializations() {
		return specializations;
	}

	@Override
	public TLStructuredTypePart getPart(String name) {
		TLStructuredTypePart localPart = super.getPart(name);
		if (localPart != null) {
			return localPart;
		}
		
		for (TLClass generalization : getGeneralizations()) {
			TLStructuredTypePart superPart = generalization.getPart(name);
			if (superPart != null) {
				return superPart;
			}
		}
		
		return null;
	}

	@Override
	protected void internalResolve(Protocol log) {
		if (resolveRunning) {
			throw log.fatal("Cyclic extends relation in class '" + this + "'.");
		}
		
		this.resolveRunning = true;
		try {
			// Make sure, extended classes are resolved first.
			for (TLClass extendedClass : extendedClasses) {
				((TLClassImpl) extendedClass).localResolve(log);
			}
			
			super.internalResolve(log);
		} finally {
			resolveRunning = false;
		}
	}

	@Override
	public List<TLClassPart> getLocalClassParts() {
		return getLocalParts();
	}

}
