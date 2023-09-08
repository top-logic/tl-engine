/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.format;

import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.format.XMLFragmentString;

/**
 * Test case for {@link XMLFragmentString}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestXMLFragmentString extends AbstractTypedConfigurationTestCase {

	public interface A extends ConfigurationItem {

		@Binding(XMLFragmentString.class)
		String getFragment();

	}

	public void testReadWrite() throws ConfigurationException, XMLStreamException {
		A item = read("<a><fragment><with/><some arbitrary='XML'><without/><any><schema/></any></some></fragment></a>");
		assertEquals("<with></with><some arbitrary=\"XML\"><without></without><any><schema></schema></any></some>",
			item.getFragment());

		assertEquals(
			"<?xml version=\"1.0\" ?><a xmlns:config=\"http://www.top-logic.com/ns/config/6.0\"><fragment><with></with><some arbitrary=\"XML\"><without></without><any><schema></schema></any></some></fragment></a>",
			toXML(item));
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("a", TypedConfiguration.getConfigurationDescriptor(A.class));
	}

}
