/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

/**
 * Kind of a {@link TLModelPart}.
 * 
 * @see TLModelPart#getModelKind()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum ModelKind {

	/**
	 * {@link TLModel} kind.
	 */
	MODEL,

	/**
	 * {@link TLModule} kind.
	 */
	MODULE,

	/**
	 * {@link TLClass} kind.
	 */
	CLASS,

	/**
	 * {@link TLAssociation} kind.
	 */
	ASSOCIATION,

	/**
	 * {@link TLEnumeration} kind.
	 */
	ENUMERATION,

	/**
	 * {@link TLPrimitive} kind.
	 */
	DATATYPE,

	/**
	 * {@link TLProperty} kind.
	 */
	PROPERTY,

	/**
	 * {@link TLReference} kind.
	 */
	REFERENCE,

	/**
	 * {@link TLAssociationEnd} kind.
	 */
	END,

	/**
	 * {@link TLClassifier} kind.
	 */
	CLASSIFIER,

}
