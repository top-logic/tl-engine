/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.ref;

import com.top_logic.basic.config.annotation.TagName;

/**
 * A structural component relation.
 */
@TagName("relation")
public interface ComponentRelation extends ComponentRef {

	/**
	 * Possible types of {@link ComponentRelation}s.
	 */
	enum Kind {

		/**
		 * The component on which the model spec is declared.
		 */
		self,

		/**
		 * The component that declares the dialog in which the referencing component lives.
		 */
		dialogParent;

	}

	/**
	 * The component relation to resolve dynamically.
	 */
	ComponentRelation.Kind getKind();

	/**
	 * @see #getKind()
	 */
	void setKind(ComponentRelation.Kind value);

}