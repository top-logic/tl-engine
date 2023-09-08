/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.constraint.annotation;

import junit.framework.TestCase;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.ExpectedFailureProtocol;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;

/**
 * Test case for {@link RegexpConstraint}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestRegexpConstraint extends TestCase {

	public interface Config extends ConfigurationItem {
		@RegexpConstraint("[a-zA-Z_][a-zA-Z_0-9]*")
		String getId();

		void setId(String value);
	}

	public void testCheck() throws ConfigurationException {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setId("fooBar42");

		check(config, new AssertProtocol());

		config.setId("fooBar<42>");

		try {
			check(config, new ExpectedFailureProtocol());

			BasicTestCase.fail("Failure expected");
		} catch (ExpectedFailure ex) {
			// Expected.
		}
	}

	private void check(Config config, Protocol log) throws ConfigurationException {
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(config);
		for (ConstraintFailure failure : checker.getFailures()) {
			log.error(failure.getMessage().toString());
		}
		log.checkErrors();
	}

}
