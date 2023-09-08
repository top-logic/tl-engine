/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html.layout;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.ComponentNameFormat;
import com.top_logic.mig.html.layout.I18NConstants;

/**
 * Test for the {@link ComponentName} and stuff.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestComponentName extends BasicTestCase {

	public void testEqualsLocal() {
		ComponentName name1 = ComponentName.newName("myFunnyComponent");
		ComponentName name2 = ComponentName.newName("myFunnyComponent");
		assertTrue(name1.equals(name1));
		assertTrue(name1.equals(name2));
		assertTrue(name2.equals(name1));
		assertEquals(name1.hashCode(), name2.hashCode());
		ComponentName name3 = ComponentName.newName("myOtherComponent");
		assertFalse(name1.equals(name3));
		assertFalse(name3.equals(name1));
	}

	public void testEqualsQualified() {
		ComponentName name1 = ComponentName.newName("scope1", "myFunnyComponent");
		ComponentName name2 = ComponentName.newName("scope1", "myFunnyComponent");
		assertTrue(name1.equals(name1));
		assertTrue(name1.equals(name2));
		assertTrue(name2.equals(name1));
		assertEquals(name1.hashCode(), name2.hashCode());
		ComponentName name3 = ComponentName.newName("scope1", "myOtherComponent");
		assertFalse(name1.equals(name3));
		assertFalse(name3.equals(name1));
		ComponentName name4 = ComponentName.newName("scope2", "myFunnyComponent");
		assertFalse(name1.equals(name4));
		assertFalse(name4.equals(name1));
	}

	public void testLocalName() {
		ComponentName implicitLocal = ComponentName.newName("myFunnyComponent");
		ComponentName explicitLocal = ComponentName.newName(null, "myFunnyComponent");
		assertTrue(implicitLocal.equals(explicitLocal));
		assertTrue(explicitLocal.isLocalName());
		assertTrue(implicitLocal.isLocalName());

		ComponentName qualified = ComponentName.newName("myScope", "myFunnyComponent");
		assertFalse(qualified.isLocalName());
	}

	public void testParse() throws ConfigurationException {
		ComponentName local = ComponentName.newName("myFunnyComponent");
		String localFormatted = format(local);
		assertEquals("myFunnyComponent", localFormatted);
		assertEquals(local, parse(localFormatted));

		ComponentName qualified = ComponentName.newName("scope", "myFunnyComponent");
		String qualifiedFormatted = format(qualified);
		assertEquals("scope" + ComponentName.SCOPE_SEPARATOR + "myFunnyComponent", qualifiedFormatted);
		assertEquals(qualified, parse(qualifiedFormatted));

		try {
			parse("scope" + ComponentName.SCOPE_SEPARATOR + "componentNameWith" + ComponentName.SCOPE_SEPARATOR
				+ "Separator");
		} catch (ConfigurationException ex) {
			assertEquals(I18NConstants.ERROR_DUPLICATE_SEPARATOR__SEPARATOR, ex.getErrorKey().plain());
		}
	}

	private static ComponentName parse(String name) throws ConfigurationException {
		return ComponentNameFormat.INSTANCE.getValue("prop", name);
	}

	private static String format(ComponentName name) {
		return ComponentNameFormat.INSTANCE.getSpecification(name);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestComponentName}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestComponentName.class);
	}

}

