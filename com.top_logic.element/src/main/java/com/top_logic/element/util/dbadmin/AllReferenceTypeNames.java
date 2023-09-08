/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.dbadmin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.func.Function0;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;

/**
 * Resolves reference target type names including alternative types.
 * 
 * @see AllTableNames
 * @see DeclarativeFormBuilder#CONTEXT
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AllReferenceTypeNames extends Function0<List<String>> {

	private SchemaConfiguration _schema;

	/**
	 * Creates a {@link AllReferenceTypeNames}.
	 *
	 */
	public AllReferenceTypeNames(DeclarativeFormOptions options) {
		_schema = (SchemaConfiguration) options.get(DeclarativeFormBuilder.CONTEXT);
	}

	@Override
	public List<String> apply() {
		if (_schema == null) {
			return Collections.emptyList();
		}
		return toNames(typeStream());
	}

	private List<String> toNames(Stream<MetaObjectName> typeStream) {
		return typeStream
			.map(t -> t.getObjectName())
			.sorted()
			.collect(Collectors.toList());
	}

	/**
	 * All type definitions in the context {@link SchemaConfiguration}.
	 */
	protected Stream<MetaObjectName> typeStream() {
		return _schema
			.getMetaObjects()
			.getTypes()
			.values()
			.stream();
	}
}
