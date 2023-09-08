/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.schema.properties;

import junit.extensions.TestSetup;
import junit.framework.Test;

import test.com.top_logic.basic.db.schema.SelectiveSchemaTestSetup;

import com.top_logic.basic.db.schema.properties.DBProperties;

/**
 * {@link TestSetup} for tables of {@link DBProperties}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DBPropertiesTableSetup extends SelectiveSchemaTestSetup {

	/**
	 * Creates a {@link DBPropertiesTableSetup}.
	 */
	private DBPropertiesTableSetup(Test test) {
		super(test, "dbproperties");
	}

	/**
	 * Wraps the given test with a {@link DBPropertiesTableSetup}.
	 */
	public static Test setup(Test test) {
		return new DBPropertiesTableSetup(test);
	}

}
