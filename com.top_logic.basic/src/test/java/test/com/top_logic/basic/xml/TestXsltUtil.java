/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Settings;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileBasedBinaryContent;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.xml.XsltUtil;

/**
 * Test case for {@link XsltUtil}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestXsltUtil extends TestCase {

	public void testEncoding() throws IOException {
		File source = new File("test/fixtures/TestXsltUtil/source.xml");
		File transform = new File("test/fixtures/TestXsltUtil/transform.xslt");
		File output = BasicTestCase.createNamedTestFile("TestXsltUtil-output.xml");

		XsltUtil.transformFile(source.getAbsolutePath(), transform.getAbsolutePath(), output.getAbsolutePath(), true);

		String result = FileUtilities.readFileToString(output, "utf-16");

		assertEquals("\u901F\u6BD4:(\u534E\u590F:Hua Xia)", result);
	}

	public void testTransformContent() throws IOException, TransformerFactoryConfigurationError, TransformerException {
		File source = new File("test/fixtures/TestXsltUtil/source.xml");
		File transform = new File("test/fixtures/TestXsltUtil/transform.xslt");

		Transformer transformer = XsltUtil.createTransformer(new StreamSource(transform), true);
		BinaryContent transformed = XsltUtil.transform(FileBasedBinaryContent.createBinaryContent(source), transformer);

		try (InputStream in = transformed.getStream()) {
			String result = StreamUtilities.readAllFromStream(in, "utf-16");
			assertEquals("\u901F\u6BD4:(\u534E\u590F:Hua Xia)", result);
		}
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestXsltUtil.class, Settings.Module.INSTANCE));
	}

}
