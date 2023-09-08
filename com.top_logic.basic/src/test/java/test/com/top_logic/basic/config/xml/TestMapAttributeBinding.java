/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.xml;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.xml.MapAttributeBinding;

/**
 * Test case for {@link MapAttributeBinding}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMapAttributeBinding extends AbstractTypedConfigurationTestCase {

	public interface A {
		@MapBinding(tag = "e", key = "k", attribute = "v")
		Map<String, Integer> getMap();
	}

	public void testMapAttributeBinding() throws Throwable {
		A a1 = (A) readConfiguration("values.xml");
		assertEquals(3, a1.getMap().size());
		assertEquals((Integer) (-20), a1.getMap().get("a"));
		assertEquals((Integer) 0, a1.getMap().get("b"));
		assertEquals((Integer) 22, a1.getMap().get("c"));

		A a2 = read(a1.toString());
		assertEquals(a1.getMap(), a2.getMap());
    }

	public void testSimpleConfigModification() throws Throwable {
		A a = (A) readConfigurationStacked("values.xml", "simpleModification.xml");
		assertEquals(5, a.getMap().size());
		assertEquals((Integer) 4, a.getMap().get("a"));
		assertEquals((Integer) 8, a.getMap().get("b"));
		assertEquals((Integer) 22, a.getMap().get("c"));
		assertEquals((Integer) 2, a.getMap().get("d"));
		assertEquals((Integer) 10, a.getMap().get("e"));
	}

	public void testConfigModification() throws Throwable {
		A a = (A) readConfigurationStacked("values.xml", "modification.xml");
		assertEquals(4, a.getMap().size());
		assertEquals((Integer) 4, a.getMap().get("a"));
		assertEquals((Integer) 8, a.getMap().get("b"));
		assertEquals(null, a.getMap().get("c"));
		assertEquals((Integer) 2, a.getMap().get("d"));
		assertEquals((Integer) 10, a.getMap().get("e"));
	}

	public void testInvalidUpdate() {
		try {
			readConfigurationStacked("values.xml", "invalid-update.xml");
		} catch (Throwable ex) {
			BasicTestCase.assertContains("The update operation object 'x' could not be found", ex.getMessage());
		}
	}

	public void testInvalidAdd() {
		try {
			readConfigurationStacked("values.xml", "invalid-add.xml");
		} catch (Throwable ex) {
			BasicTestCase.assertContains("The value 'a' already exists", ex.getMessage());
		}
	}

	public void testInvalidRemove() {
		try {
			readConfigurationStacked("values.xml", "invalid-remove.xml");
		} catch (Throwable ex) {
			BasicTestCase.assertContains("The value 'y' does not exist", ex.getMessage());
		}
	}

	public void testInvalidKeyAttribute() throws Throwable {
		initFailureTest();
		try {
			readConfiguration("invalid-key-attribute.xml");
		} catch (ConfigurationException ex) {
			BasicTestCase.assertContains("Unexpected argument 'kk'", ex.getMessage());
		}
	}

	public void testInvalidValueAttribute() throws Throwable {
		initFailureTest();
		try {
			readConfiguration("invalid-value-attribute.xml");
		} catch (ConfigurationException ex) {
			BasicTestCase.assertContains("Unexpected argument 'vv'", ex.getMessage());
		}
	}

	public void testInvalidValueFormat() throws Throwable {
		initFailureTest();
		try {
			readConfiguration("invalid-value-format.xml");
		} catch (ConfigurationException ex) {
			BasicTestCase.assertContains("Integer value expected", ex.getMessage());
		}
	}

	public static Test suite() {
		return suite(TestMapAttributeBinding.class);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("r", TypedConfiguration.getConfigurationDescriptor(A.class));
	}
}
