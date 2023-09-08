/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ExpectedFailure;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.ReferenceResolver;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Id;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Test case for identifier properties annotated {@link Id}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigurationReferences extends AbstractTypedConfigurationTestCase {

	public static class A extends AbstractConfiguredInstance<A.Config<?>> {

		/**
		 * Configuration options for {@link A}.
		 */
		@TagName("a")
		public interface Config<I extends A> extends PolymorphicConfiguration<I> {
			/**
			 * The identifier of the instantiated {@link A}.
			 */
			@Id(A.class)
			@Nullable
			String getName();

			/**
			 * Tree of {@link A}s.
			 */
			@DefaultContainer
			List<PolymorphicConfiguration<? extends A>> getAs();

			List<PolymorphicConfiguration<? extends X>> getXs();

			/**
			 * Reference to other {@link A} with given {@link #getName()}.
			 */
			@Nullable
			String getOther();
		}

		private List<A> _as;

		A _otherA;

		X _otherX;

		private List<X> _xs;

		/**
		 * Creates a {@link A} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public A(InstantiationContext context, Config<?> config) {
			super(context, config);

			_as = TypedConfiguration.getInstanceList(context, config.getAs());
			_xs = TypedConfiguration.getInstanceList(context, config.getXs());

			context.resolveReference(config.getOther(), A.class, x -> _otherA = x);
		}

		public String getName() {
			return getConfig().getName();
		}

		public List<A> getAs() {
			return _as;
		}

		public List<X> getXs() {
			return _xs;
		}

		public A getOther() {
			return _otherA;
		}
	}

	public static class X extends AbstractConfiguredInstance<X.Config<?>> {

		@TagName("x")
		public interface Config<I extends X> extends PolymorphicConfiguration<I> {

			/**
			 * An identifier for {@link X} instances.
			 * 
			 * <p>
			 * The scope type is implicitly given by the type parameter of the surrounding
			 * configuration interface.
			 * </p>
			 */
			@Id
			String getName();

			@Nullable
			String getOther();

			PolymorphicConfiguration<? extends A> getNestedBefore();

			PolymorphicConfiguration<? extends A> getNestedAfter();

		}

		protected A _outer;

		protected X _other;

		private A _nestedAfter;

		private A _nestedBefore;

		/**
		 * Creates a {@link X} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public X(InstantiationContext context, Config<?> config) {
			super(context, config);

			_nestedBefore = context.getInstance(config.getNestedBefore());

			context.resolveReference(config.getOther(), X.class, x -> _other = x);
			context.resolveReference(InstantiationContext.OUTER, A.class, x -> _outer = x);

			_nestedAfter = context.getInstance(config.getNestedAfter());
		}

		public A getNestedBefore() {
			return _nestedBefore;
		}

		public A getNestedAfter() {
			return _nestedAfter;
		}

		public X getOther() {
			return _other;
		}

		public A getOuter() {
			return _outer;
		}

	}

	public static class InstanceImplementingReferenceResolver extends A implements ReferenceResolver<Object> {

		/**
		 * Configuration options for {@link InstanceImplementingReferenceResolver}.
		 */
		@TagName("b")
		public interface Config<I extends InstanceImplementingReferenceResolver> extends A.Config<I> {
			// Pure marker interface.
		}

		/**
		 * Creates a {@link InstanceImplementingReferenceResolver} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public InstanceImplementingReferenceResolver(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public void setReference(Object value) {
			throw new UnsupportedOperationException("Must not be called. Instance only used as value.");
		}

	}

	public void testResolveBackRef() throws ConfigurationException {
		A a = instantiate("<a><a name='foo'/><a other='foo'/></a>");
		assertSame(a.getAs().get(0), a.getAs().get(1).getOther());
	}

	public void testResolveForwardsRef() throws ConfigurationException {
		A a = instantiate("<a><a other='foo'/><a name='foo'/></a>");
		assertSame(a.getAs().get(1), a.getAs().get(0).getOther());
	}

	public void testResolveMultiForwardsRef() throws ConfigurationException {
		A a = instantiate("<a><a other='foo'/><a other='foo'/><a name='foo'/></a>");
		assertSame(a.getAs().get(2), a.getAs().get(0).getOther());
		assertSame(a.getAs().get(2), a.getAs().get(1).getOther());
	}

	public void testCyclicRef() throws ConfigurationException {
		A a = instantiate("<a><a name='x' other='y'/><a name='y' other='x'/></a>");
		assertSame(a.getAs().get(0), a.getAs().get(1).getOther());
		assertSame(a.getAs().get(1), a.getAs().get(0).getOther());
	}

	public void testReferenceScopes() throws ConfigurationException {
		A a = instantiate(
			"<a><a other='foo'><xs><x other='foo'/></xs></a><a name='foo'><xs><x name='foo'/></xs></a></a>");
		assertSame(a.getAs().get(1), a.getAs().get(0).getOther());
		assertSame(a.getAs().get(1).getXs().get(0), a.getAs().get(0).getXs().get(0).getOther());
		assertSame(a.getAs().get(0), a.getAs().get(0).getXs().get(0).getOuter());
		assertSame(a.getAs().get(1), a.getAs().get(1).getXs().get(0).getOuter());
	}

	public void testNestedScopes1() throws ConfigurationException {
		A a = instantiate(
			"<a name='outer'><xs><x><nested-before name='inner'/></x></xs></a>");
		assertEquals("outer", a.getName());
		assertEquals("inner", a.getXs().get(0).getNestedBefore().getName());
		assertEquals("outer", a.getXs().get(0).getOuter().getName());
	}

	public void testNestedScopes2() throws ConfigurationException {
		A a = instantiate(
			"<a name='outer'><xs><x><nested-after name='inner'/></x></xs></a>");
		assertEquals("outer", a.getName());
		assertEquals("inner", a.getXs().get(0).getNestedAfter().getName());
		assertEquals("outer", a.getXs().get(0).getOuter().getName());
	}

	public void testNestedScopesDeep() throws ConfigurationException {
		A a = instantiate(
			"<a name='outer'><xs><x name='x1'>"
				+ "<nested-after name='inner'><xs><x name='x2'>"
				+ "<nested-after name='deep'/>"
				+ "</x></xs></nested-after>"
				+ "</x></xs></a>");
		assertEquals("outer", a.getName());
		assertEquals("inner", a.getXs().get(0).getNestedAfter().getName());
		assertEquals("inner", a.getXs().get(0).getNestedAfter().getXs().get(0).getOuter().getName());
		assertEquals("outer", a.getXs().get(0).getOuter().getName());
	}

	public void testValueIsReferenceResolver() throws ConfigurationException {
		A a = instantiate("<a><b name='foo'/><a other='foo'/></a>");
		assertSame(a.getAs().get(0), a.getAs().get(1).getOther());
	}

	public void testDeferReferenceCheck() {
		context.deferredReferenceCheck(() -> {
			try {
				A a1 = instantiate("<a><b name='foo'/></a>");
				A a2 = instantiate("<a><a other='foo'/></a>");
				assertSame(a1.getAs().get(0), a2.getAs().get(0).getOther());
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
			return null;
		});
	}

	public void testNoReferenceAfterInstantiationFinished() throws ConfigurationException {
		initFailureTest();
		try {
			A a1 = instantiate("<a><b name='foo'/></a>");
			A a2 = instantiate("<a><a other='foo'/></a>");
			assertNotNull(a1);
			assertNotNull(a2);
			context.checkErrors();
			fail("Expected failure.");
		} catch (ExpectedFailure ex) {
			BasicTestCase.assertContains("Unresolved reference 'A:foo'", ex.getMessage());
		}
	}

	public void testErrorUnresolved() throws ConfigurationException {
		initFailureTest();
		try {
			instantiate("<a><a other='some-ref'/></a>");
			context.checkErrors();
			fail("Expected failure.");
		} catch (ExpectedFailure ex) {
			BasicTestCase.assertContains("Unresolved reference 'A:some-ref'", ex.getMessage());
		}
	}

	public void testErrorUnresolvedMultiple() throws ConfigurationException {
		initFailureTest();
		try {
			instantiate("<a><a other='some-ref'/><a other='some-ref'/><a other='some-other-ref'/></a>");
			context.checkErrors();
			fail("Expected failure.");
		} catch (ExpectedFailure ex) {
			BasicTestCase.assertContains("Unresolved reference 'A:some-ref'", ex.getMessage());
			BasicTestCase.assertContains("Unresolved reference 'A:some-other-ref'", ex.getMessage());
		}
	}

	public void testErrorDuplicateId() throws ConfigurationException {
		initFailureTest();
		try {
			instantiate("<a><a name='some-id'/><a name='some-id'/></a>");
			context.checkErrors();
			fail("Expected failure.");
		} catch (ExpectedFailure ex) {
			BasicTestCase.assertContains("Duplicate ID 'some-id'", ex.getMessage());
		}
	}

	private A instantiate(String xml) throws ConfigurationException {
		A.Config<?> config = read(xml);
		return context.getInstance(config);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("a", TypedConfiguration.getConfigurationDescriptor(A.Config.class));
	}

	public static Test suite() {
		return suite(TestTypedConfigurationReferences.class);
	}

}
