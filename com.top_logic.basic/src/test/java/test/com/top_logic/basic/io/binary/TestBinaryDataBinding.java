/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataBinding;
import com.top_logic.basic.io.binary.BinaryDataFactory;

/**
 * Test case for {@link BinaryDataBinding}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestBinaryDataBinding extends AbstractTypedConfigurationTestCase {

	public interface A extends ConfigurationItem {
		BinaryData getMyData();

		void setMyData(BinaryData value);
	}

	public void testReadWriteNull() throws ConfigurationException, IOException {
		A item = TypedConfiguration.newConfigItem(A.class);
		doDumpLoad(item);
	}

	public void testRead() throws ConfigurationException, IOException {
		String NL = "\r\n";
		A item = read(
			"<config><my-data name='myname' content-type='text/plain'>" + NL
				+ "SGVsbG 8gd29y " + NL + 
				"bGQh" + NL + 
			"</my-data></config>");
		assertEquals("myname", item.getMyData().getName());
		assertEquals("text/plain", item.getMyData().getContentType());
		assertEquals("Hello world!", StreamUtilities.readAllFromStream(item.getMyData().getStream(), "utf-8"));
	}

	public void testReadWrite() throws ConfigurationException, IOException {
		A item = TypedConfiguration.newConfigItem(A.class);
		item.setMyData(
			BinaryDataFactory.createBinaryData("Hello world!".getBytes("utf-8"), "text/plain", "myname"));
		doDumpLoad(item);
	}

	public void testReadWriteLarge() throws ConfigurationException, IOException {
		A item = TypedConfiguration.newConfigItem(A.class);
		byte[] bytes = new byte[1024 * 10 + 13];
		new Random(42).nextBytes(bytes);
		item.setMyData(BinaryDataFactory.createBinaryData(bytes));
		doDumpLoad(item);
	}

	public void testReadWriteEmpty() throws ConfigurationException, IOException {
		A item = TypedConfiguration.newConfigItem(A.class);
		byte[] bytes = new byte[0];
		new Random(42).nextBytes(bytes);
		item.setMyData(BinaryDataFactory.createBinaryData(bytes));
		doDumpLoad(item);
	}

	public void testReadWriteAligned() throws ConfigurationException, IOException {
		A item = TypedConfiguration.newConfigItem(A.class);
		byte[] bytes = new byte[1024];
		new Random(42).nextBytes(bytes);
		item.setMyData(BinaryDataFactory.createBinaryData(bytes));
		doDumpLoad(item);
	}

	private void doDumpLoad(A item) throws ConfigurationException, IOException {
		StringWriter out = new StringWriter();
		TypedConfiguration.serialize(item, out);
		A read = read(out.toString());
		assertEquals(item, read);
	}

	private void assertEquals(A expected, A seen) throws IOException {
		if (expected.getMyData() != null) {
			assertEquals(expected.getMyData().getContentType(), seen.getMyData().getContentType());
			assertEquals(expected.getMyData().getName(), seen.getMyData().getName());
			assertTrue(
				StreamUtilities.equalsStreamContents(expected.getMyData().getStream(), seen.getMyData().getStream()));
		}
		assertEquals(expected.getMyData(), seen.getMyData());
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("config", TypedConfiguration.getConfigurationDescriptor(A.class));
	}

	public static Test suite() {
		return suite(TestBinaryDataBinding.class);
	}
}
