/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.dbadmin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.func.Function0;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;

/**
 * All attribute names of the context {@link MetaObjectConfig}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ColumnNamesOfContextTable extends Function0<List<String>> {

	private MetaObjectConfig _model;

	private SchemaConfiguration _schema;

	/**
	 * Creates a {@link ColumnNamesOfContextTable}.
	 */
	public ColumnNamesOfContextTable(DeclarativeFormOptions options) {
		_schema = (SchemaConfiguration) options.get(DeclarativeFormBuilder.CONTEXT);
		_model = (MetaObjectConfig) options.get(DeclarativeFormBuilder.FORM_MODEL);
	}

	@Override
	public List<String> apply() {
		MetaObjectConfig current = _model;
		if (current != null) {
			return generalizations(current)
				.stream()
				.flatMap(t -> t.getAttributes().stream())
				.map(a -> a.getAttributeName())
				.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private List<MetaObjectConfig> generalizations(MetaObjectConfig current) {
		List<MetaObjectConfig> generalizations = new ArrayList<>();
		while (current != null) {
			if (generalizations.contains(current)) {
				// Safety: Do not crash in cyclic type hierarchies.
				break;
			}
			generalizations.add(current);
			String superClassName = current.getSuperClass();
			if (StringServices.isEmpty(superClassName)) {
				break;
			}
			current = (MetaObjectConfig) _schema.getMetaObjects().getTypes().get(superClassName);
		}
		return generalizations;
	}

}
