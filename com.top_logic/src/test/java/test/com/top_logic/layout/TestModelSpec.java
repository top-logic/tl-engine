/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.Provider;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Test case for {@link ModelSpec} format.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestModelSpec extends BasicTestCase {

	public static class TestProvider implements ModelProvider {

		public interface Config extends PolymorphicConfiguration<ModelProvider> {
			@IntDefault(42)
			int getFoo();
		}

		/**
		 * Creates a {@link TestModelSpec.TestProvider} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public TestProvider(InstantiationContext context, Config config) {
			assertEquals(42, config.getFoo());
		}

		@Override
		public Object getBusinessModel(LayoutComponent businessComponent) {
			return null;
		}

	}

	public void testEmpty() throws ConfigurationException {
		assertEquals("", parseAndSerialize(""));
	}

	public void testNull() throws ConfigurationException {
		assertParse("");
	}

	public void testNullAndOther() throws ConfigurationException {
		assertEquals("selection(foo)", parseAndSerialize("selection(foo)"));
	}

	public void testUnion() throws ConfigurationException {
		assertParse("selection(foo),selection(bar)");
	}

	public void testMultiChannel() throws ConfigurationException {
		assertEquals("selection(foo),selection(bar)", parseAndSerialize("selection(foo,bar)"));
	}

	public void testChannel() throws ConfigurationException {
		assertParse("model(foobar)");
	}

	public void testNullChannel() throws ConfigurationException {
		assertEquals("", parseAndSerialize("model()"));
	}

	public void testChannelRelation() throws ConfigurationException {
		assertParse("model(dialogParent())");
	}

	public void testInvalidRelation() {
		try {
			assertParse("model(invalidRelation())");
			fail("Must not accept invalid component relation.");
		} catch (ConfigurationException ex) {
			BasicTestCase.assertContains("Invalid component relation", ex.getMessage());
		}
	}

	public void testUndefinedRelation() {
		try {
			assertParse("model(foobar())");
		} catch (ConfigurationException ex) {
			BasicTestCase.assertContains("component relation", ex.getMessage());
		}
	}

	public void testProvider() throws ConfigurationException {
		String shortcut = "provider(" + TestProvider.class.getName() + ")";
		Provider spec = (Provider) parse(shortcut);
		assertNotNull(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(spec.getImpl()));
		assertEquals(shortcut, serialize(spec));
	}

	private void assertParse(String spec) throws ConfigurationException {
		assertEquals(spec, parseAndSerialize(spec));
	}

	private String parseAndSerialize(String spec) throws ConfigurationException {
		return serialize(parse(spec));
	}

	private String serialize(ModelSpec value) {
		return ModelSpec.Format.INSTANCE.getSpecification(value);
	}

	private ModelSpec parse(String spec) throws ConfigurationException {
		ModelSpec value = ModelSpec.Format.INSTANCE.getValue("x", spec);
		return value;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestModelSpec}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestModelSpec.class);
	}

}
