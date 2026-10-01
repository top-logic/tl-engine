/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.Test;

import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.util.DBSelection;

/**
 * A {@link Test} (typically a setup) whose complete subtree runs against one database.
 *
 * <p>
 * The {@link DBSelection} uses this binding to distribute the tests of a multi-database run over
 * separate runs, one per external database.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBBoundTest extends Test {

	/**
	 * The database that all tests in this subtree run against.
	 */
	DBType getBoundDB();

}
