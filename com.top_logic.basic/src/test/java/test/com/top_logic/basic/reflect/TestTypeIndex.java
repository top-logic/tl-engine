/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.reflect;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Collection;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.logging.LogConfigurator;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ModuleUtil.ModuleContext;
import com.top_logic.basic.reflect.TypeIndex;

/**
 * Test case for {@link TypeIndex}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypeIndex extends TestCase {

	public interface I {
		// Pure marker interface.
	}

	public interface J extends I {
		// Pure marker interface.
	}

	public static class A implements I {
		// Pure test class.
	}

	public static class B implements J {
		// Pure test class.
	}

	public void testIndex() throws ModuleException {
		AssertNoErrorLogListener listener = new AssertNoErrorLogListener(true);
		listener.activate();
		try {
			try (ModuleContext context = ModuleUtil.INSTANCE.begin()) {
				ModuleUtil.INSTANCE.startModule(TypeIndex.class);

				TypeIndex index = TypeIndex.getInstance();
				Collection<Class<?>> types =
					index.getSpecializations(LogConfigurator.class, true, false, true);

				assertTrue("Log configurator types: " + types, types.size() > 1);

				Collection<Class<?>> implementations =
					index.getSpecializations(LogConfigurator.class, true, false, false);

				assertTrue("Log configurator implementations: " + implementations, implementations.size() > 0);

				assertEquals(set(J.class, A.class),
					new HashSet<>(index.getSpecializations(I.class, false, false, true)));
				assertEquals(set(A.class, B.class),
					new HashSet<>(index.getSpecializations(I.class, true, false, false)));
			}
			listener.assertNoErrorLogged("Errors while activating TypeIndex service: ");
		} finally {
			listener.deactivate();
		}
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(TestTypeIndex.class);
	}

}
