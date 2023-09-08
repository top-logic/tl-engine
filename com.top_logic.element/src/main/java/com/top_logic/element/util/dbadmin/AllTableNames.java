/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.dbadmin;

import java.util.stream.Stream;

import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;

/**
 * Resolves all table and table template names as option list from the context schema.
 * 
 * @see AllReferenceTypeNames
 * @see DeclarativeFormBuilder#CONTEXT
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AllTableNames extends AllReferenceTypeNames {

	/**
	 * Creates a {@link AllTableNames}.
	 */
	public AllTableNames(DeclarativeFormOptions options) {
		super(options);
	}

	@Override
	protected Stream<MetaObjectName> typeStream() {
		return super.typeStream().filter(t -> t instanceof MetaObjectConfig);
	}
}
