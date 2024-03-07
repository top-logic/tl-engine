/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.migration.data;

import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLType;

/**
 * {@link BranchIdType} representing a {@link TLType}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Type extends BranchIdType {

	/**
	 * Kind of a {@link Type}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	enum Kind {
		/** A {@link TLPrimitive}. */
		DATATYPE,
		/** A {@link TLClass} */
		CLASS,
		/** A {@link TLAssociation} */
		ASSOCIATION,
		/** A {@link TLEnumeration} */
		ENUM;
	}

	/**
	 * Name of the corresponding {@link TLType}.
	 */
	String getTypeName();

	/**
	 * Setter for {@link #getTypeName()}.
	 */
	void setTypeName(String type);

	/**
	 * {@link Module} of the given {@link TLType}.
	 */
	Module getModule();

	/**
	 * Setter for {@link #getModule()}.
	 */
	void setModule(Module module);

	/**
	 * Kind of this type.
	 */
	Kind getKind();

	/**
	 * Setter for {@link #getKind()}.
	 */
	void setKind(Kind kind);
}

