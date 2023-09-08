/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.migrate.tl.flexdata;

import java.sql.SQLException;

import junit.framework.Test;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.migrate.tl.flexdata.RollbackOperation;

/**
 * Test case for {@link RollbackOperation}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Rollback extends BasicTestCase {

	public void testIt() throws UnknownTypeException, SQLException {
		new RollbackOperation(new AssertProtocol(), "hardrevert", 315000).run();
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(Rollback.class);
	}

}
