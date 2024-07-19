/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.util.model;

import java.util.Objects;

import com.top_logic.model.TLClass;

/**
 * Implementation of {@link TLInheritance}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLInheritanceImpl implements TLInheritance {

	private TLClass _generalization;

	private TLClass _specialization;

	/**
	 * Creates a {@link TLInheritance} for the given {@link TLClass}es.
	 */
	public TLInheritanceImpl(TLClass generalization, TLClass specialization) {
		_generalization = generalization;
		_specialization = specialization;
	}

	@Override
	public TLClass getSpecialization() {
		return _generalization;
	}

	/**
	 * @see #getSpecialization()
	 */
	public void setSource(TLClass source) {
		_generalization = source;
	}

	@Override
	public TLClass getGeneralization() {
		return _specialization;
	}

	/**
	 * @see #getGeneralization()
	 */
	public void setTarget(TLClass target) {
		_specialization = target;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_generalization, _specialization);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TLInheritanceImpl other = (TLInheritanceImpl) obj;
		return Objects.equals(_generalization, other._generalization) && Objects.equals(_specialization, other._specialization);
	}

}