/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.schema;

import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;

import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.NamedResource;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;

/**
 * {@link TestSetup} that creates the schema for a subset of the configured tables.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectiveSchemaTestSetup extends SchemaTestSetup {

	private static MultipleSetupCounter SETUP_COUNTER = newMultipleCounter();

	private final String[] _schemaNames;

	/**
	 * Creates a {@link SelectiveSchemaTestSetup} with default value for "multiple branches", i.e.
	 * the configured value.
	 * 
	 * @see SelectiveSchemaTestSetup#SelectiveSchemaTestSetup(Test, Decision, String...)
	 */
	public SelectiveSchemaTestSetup(Test test, String... schemaNames) {
		this(test, Decision.DEFAULT, schemaNames);
	}

	/**
	 * Creates a {@link SelectiveSchemaTestSetup}.
	 * 
	 * @param test
	 *        The {@link Test} to decorate.
	 * @param multipleBranches
	 *        Whether the {@link SchemaConfiguration} has multiple branches.
	 * @param schemaNames
	 *        The schema names to set up, see {@link SchemaConfiguration}.
	 */
	public SelectiveSchemaTestSetup(Test test, Decision multipleBranches, String... schemaNames) {
		super(test, multipleBranches, SETUP_COUNTER.getCounterFor(key(multipleBranches, schemaNames)));
		_schemaNames = schemaNames;
	}

	private static Object key(Decision multipleBranches, String[] schemaNames) {
		switch(schemaNames.length) {
			case 0:
				return Collections.singleton(multipleBranches);
			default:
				HashSet<Object> allSchemas = new HashSet<>();
				allSchemas.add(multipleBranches);
				Collections.addAll(allSchemas, schemaNames);
				return allSchemas;
		}
	}

	@Override
	public Object configKey() {
		return TupleFactory.newTuple(super.configKey(), key(_multipleBranches, _schemaNames));
	}

	@Override
	protected List<? extends ResourceDeclaration> getSchemaDeclarations(SchemaConfiguration schemaConfig) {
		List<NamedResource> declarations = new ArrayList<>(_schemaNames.length);
		for (String schemaName : _schemaNames) {
			NamedResource schemaResource = schemaConfig.getSchema(schemaName);
			assertNotNull("Schema '" + schemaName + "' not found.", schemaResource);
			declarations.add(schemaResource);
		}
		return declarations;
	}

}
