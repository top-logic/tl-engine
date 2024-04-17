/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Test case for {@link TagName} annotation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTagNameAnnotation extends AbstractTypedConfigurationTestCase {

	public interface A extends ConfigurationItem {

		public static final String TYPED_BS = "typed-bs";

		List<B> getBs();

		@Key(CONFIGURATION_INTERFACE_NAME)
		List<B> getBsForType();

		@Name(TYPED_BS)
		@Key(CONFIGURATION_INTERFACE_NAME)
		Map<Class<? extends B>, B> getTypedBs();

		@Indexed(collection = TYPED_BS)
		<T extends B> T getTypedB(Class<T> type);

	}

	@Abstract
	public interface X extends ConfigurationItem {
		// Pure base interface.
	}

	@TagName("b")
	public interface B extends X {

		String getS();

	}

	@TagName("c")
	public interface C extends B {

		int getX();

	}

	@TagName("d")
	public interface D extends C {

		int getY();

	}

	@TagName("d")
	public interface TagClash extends X {

		int getZ();

	}

	public interface WithTagClash extends ConfigurationItem {

		List<X> getBs();

	}

	public interface WithResolvedTagClash extends ConfigurationItem {

		@Subtypes({
			@Subtype(tag = "b", type = B.class),
			@Subtype(tag = "d", type = TagClash.class),
		})
		List<X> getBs();

	}

	public interface WithPolymorphicList extends ConfigurationItem {

		List<PolymorphicConfiguration<I1>> getBs();

		List<PolymorphicConfiguration<? extends I1>> getCs();

	}

	public interface WithHiddenPolymorphicList extends ConfigurationItem {

		List<LikePolyConfig<Object, I1>> getBs();

	}

	public interface I1 {
		// Some implementation interface
	}

	public interface LikePolyConfig<X, T> extends PolymorphicConfiguration<T> {
		// Technical super-interface that forwards type variables.

	}

	public static class Impl1 implements I1 {

		@TagName(Config.TAG_NAME)
		public interface Config extends LikePolyConfig<Object, Impl1> {

			String TAG_NAME = "impl";

			int getI1();
		}

		/**
		 * Creates a {@link TestTagNameAnnotation.I1} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Impl1(InstantiationContext context, Config config) {
			super();
		}

	}

	public interface I2 {
		// Some other implementation interface
	}

	public static class Impl2 implements I2 {

		/**
		 * Configuration with same tag as {@link Impl1}, must not be considered a conflict, since
		 * its a {@link PolymorphicConfiguration} of {@link I2}, not {@link I1}.
		 */
		@TagName(Impl1.Config.TAG_NAME) // Must not be considered as clash.
		public interface Config extends LikePolyConfig<Object, Impl2> {
			int getI2();
		}

		/**
		 * Creates a {@link TestTagNameAnnotation.I1} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Impl2(InstantiationContext context, Config config) {
			super();
		}

	}

	public static class Special1 implements I1 {

		@TagName("special")
		public interface Config<X, T extends Special1> extends LikePolyConfig<X, T> {
			int getS();
		}

		/**
		 * Creates a {@link TestTagNameAnnotation.I1} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Special1(InstantiationContext context, Config<?, ?> config) {
			super();
		}

	}

	public interface WithLocallyResolvedTagClash extends ConfigurationItem {

		@Subtypes(value = {
			@Subtype(tag = "d", type = TagClash.class),
		}, adjust = true)
		List<X> getBs();

	}

	public static class F1Impl {
		/**
		 * Potential clash with {@link TestTagNameAnnotation.F2Impl.Config}
		 */
		@TagName("f")
		public interface Config<I extends F1Impl> extends PolymorphicConfiguration<I> {
			int getX();
		}
	}

	public static class F2Impl {
		/**
		 * Potential tag-name clash with {@link TestTagNameAnnotation.F1Impl.Config}.
		 */
		@TagName("f")
		public interface Config<I extends F2Impl> extends PolymorphicConfiguration<I> {
			String getName();
		}
	}

	public interface F1Usage extends ConfigurationItem {
		/**
		 * Tag name <code>f</code> is not a clash, since the <code>?</code> wildcard is implicitly
		 * bound to {@link TestTagNameAnnotation.F1Impl}.
		 */
		@DefaultContainer
		F1Impl.Config<?> getImpl();
	}

	public void testParse() throws ConfigurationException {
		A config = read("<a><bs><b s='1'/><c x='2'/><d y='3'/></bs></a>");
		assertEquals(3, config.getBs().size());
	}

	public void testReadTypeKeyList() throws ConfigurationException {
		A config = read("<a><bs-for-type><b s='1'/><c x='2'/></bs-for-type></a>");
		assertEquals(2, config.getBsForType().size());

		A config2 = read(config, "<a><bs-for-type><b s='10'/><c x='20'/><d y='30'/></bs-for-type></a>");
		List<B> list = config2.getBsForType();
		assertEquals(3, list.size());
		assertEquals("10", list.get(0).getS());
		assertEquals(20, ((C) list.get(1)).getX());
		assertEquals(30, ((D) list.get(2)).getY());
	}

	public void testReadTypeKeyMap() throws ConfigurationException {
		A config = read("<a><typed-bs><b s='1'/><c x='2'/></typed-bs></a>");
		assertEquals(2, config.getTypedBs().size());

		A config2 = read(config, "<a><typed-bs><b s='10'/><c x='20'/><d y='30'/></typed-bs></a>");
		assertEquals(3, config2.getTypedBs().size());
		assertEquals("10", config2.getTypedB(B.class).getS());
		assertEquals(20, config2.getTypedB(C.class).getX());
		assertEquals(30, config2.getTypedB(D.class).getY());
	}

	public void testDetectClash() {
		initFailureTest();

		try {
			WithTagClash result = readT(WithTagClash.class, "<a><bs><d/></bs></a>");
			fail("Must not parse tag 'd' with clash: " + result);
		} catch (ConfigurationException ex) {
			BasicTestCase.assertContains("Unexpected list element 'd' for property 'bs'", ex.getMessage());
		}
	}

	public void testClashIgnoredWhenTagNameNotUsed() throws ConfigurationException {
		WithTagClash result = readT(WithTagClash.class, "<a><bs><b s='foo'/><c x='42'/></bs></a>");
		assertNotNull(result);
		assertEquals(2, result.getBs().size());
	}

	public void testClashResolve() throws ConfigurationException {
		WithResolvedTagClash config =
			readT(WithResolvedTagClash.class, "<a><bs><b s='1'/><d z='2'/></bs></a>");
		assertEquals(2, config.getBs().size());
	}

	public void testTagWithClashNotDefinedAfterResolve() {
		initFailureTest();
		try {
			readT(WithResolvedTagClash.class, "<a><bs><c/></bs></a>");
			fail("Must not parse invalid config.");
		} catch (ConfigurationException ex) {
			BasicTestCase.assertContains(
				"Unexpected list element 'c' for property 'bs' (test.com.top_logic.basic.config.TestTagNameAnnotation$WithResolvedTagClash.getBs()), expected one of '[b, d]'",
				ex.getMessage());
		}
	}

	public void testLocalClashResolve() throws ConfigurationException {
		WithLocallyResolvedTagClash config =
			readT(WithLocallyResolvedTagClash.class, "<a><bs><b s='1'/><c x='2'/><d z='3'/></bs></a>");
		assertEquals(3, config.getBs().size());
	}

	public void testPolymorphicList() throws ConfigurationException, XMLStreamException {
		WithPolymorphicList config =
			readT(WithPolymorphicList.class, "<a><bs><impl i1='1'/></bs></a>");
		assertEquals(1, config.getBs().size());
		assertEquals(Impl1.class, config.getBs().get(0).getImplementationClass());

		assertEquals(
			"No class or config interface annotation must be serialized additionally to the custom tag name.",
			"<?xml version=\"1.0\" ?><a xmlns:config=\"http://www.top-logic.com/ns/config/6.0\"><bs><impl i1=\"1\"></impl></bs></a>",
			toXML(descriptors(WithPolymorphicList.class), config));
	}

	public void testHiddenPolymorphicList() throws ConfigurationException {
		WithHiddenPolymorphicList config =
			readT(WithHiddenPolymorphicList.class, "<a><bs><impl i1='1'/></bs></a>");
		assertEquals(1, config.getBs().size());
	}

	public void testBoundedTypeVariable() throws ConfigurationException {
		WithHiddenPolymorphicList config =
			readT(WithHiddenPolymorphicList.class, "<a><bs><special s='1'/></bs></a>");
		assertEquals(1, config.getBs().size());
	}

	public void testDifferentiateWithBoundedWildcard() throws ConfigurationException {
		F1Usage config =
			readT(F1Usage.class, "<a><f x='42'/></a>");
		assertNotNull(config.getImpl());
	}

	protected <T extends ConfigurationItem> T readT(Class<T> type, String... sources)
			throws ConfigurationException {
		return read(descriptors(type), sources);
	}

	private Map<String, ConfigurationDescriptor> descriptors(Class<?> type) {
		return Collections.singletonMap("a", TypedConfiguration.getConfigurationDescriptor(type));
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("a", TypedConfiguration.getConfigurationDescriptor(A.class));
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestTagNameAnnotation.class);
	}
}
