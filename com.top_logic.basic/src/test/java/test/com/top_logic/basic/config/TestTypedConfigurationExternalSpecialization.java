/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.List;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * Regression test case for Ticket #17298.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigurationExternalSpecialization extends TestCase {

	public static class A {

		@NoImplementationClassGeneration
		public interface Config extends PolymorphicConfiguration<A> {

			/**
			 * The element descriptor must reflect the fact that only {@link B} instances are
			 * allowed in the list. But {@link B} has no own configuration. Therefore a local
			 * descriptor for {@link B} is created while analyzing the property {@link #getBs()}.
			 * Since this descriptor has {@link A.Config} as its super descriptor, the same problem
			 * exists in the local descriptor. This leads to a {@link StackOverflowError}.
			 */
			@InstanceFormat
			List<B> getBs();

		}

		/**
		 * Creates a {@link TestTypedConfigurationExternalSpecialization.A} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public A(InstantiationContext context, Config config) {
			// Ignore.
		}

	}

	public static class B extends A {

		@NoImplementationClassGeneration
		public interface Config extends A.Config {

			// Annotating the class default for B is sufficient to work-around the problem:
			//
			// @ClassDefault(B.class)
			@Override
			public Class<? extends A> getImplementationClass();
		}

		public B(InstantiationContext context, Config config) {
			super(context, config);
		}

	}

	public void testAnalyze() {
		try {
			TypedConfiguration.getConfigurationDescriptor(A.Config.class);
			BasicTestCase.fail(
				"Known problem fixed? Ticket #17298: Analyzing recursive config interface with subtype list failed");
		} catch (StackOverflowError ex) {
			// Known bug, see above.
			return;
		}

		/* Currently no implementation classes can be generated due to StackOverflow when using
		 * ConfigurationDescriptor. The annotation must be removed and implementation classes be
		 * generated, when #17298 is fixed. */
		assertNull("Ticket #17298: Annotation must be removed to ensure that implementation classes are generated.",
			A.Config.class.getAnnotation(NoImplementationClassGeneration.class));
		assertNull("Ticket #17298: Annotation must be removed to ensure that implementation classes are generated.",
			B.Config.class.getAnnotation(NoImplementationClassGeneration.class));
	}
}
