/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * Test case for {@link TypedConfiguration} working with type parametrized configuration interfaces.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigurationParameterization extends TestCase {

	public interface X extends ConfigurationItem {
		int getX();
	}
	
	public interface Y extends X {
		int getY();
	}

	public interface A<T extends X> extends ConfigurationItem {

		List<T> getTs();

		List<? extends T> getBoundedTs();

		T getT();

		List<X> getXs();

		List<? extends X> getBoundedXs();

		X getX();

	}
	
	public void testInstantiate() {
		@SuppressWarnings("unchecked")
		A<Y> a = TypedConfiguration.newConfigItem(A.class);
		assertNotNull(a);
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestTypedConfigurationParameterization.class);
	}

}
