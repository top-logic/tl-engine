/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.template.expander.TemplateExpander;
import com.top_logic.template.xml.source.TemplateFilesystemSource;
import com.top_logic.template.xml.source.TemplateSource;

/**
 * Utils for testing with XML templates.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TemplateXMLTestUtil {

	public static void assertExpansion(TemplateSource template, String expected) {
		assertExpansion(template, Collections.<String, Object> emptyMap(), expected);
	}

	public static void assertExpansion(TemplateSource template, Map<String, Object> parameterValues, String expected) {
		try {
			String actual = TemplateExpander.expandXmlTemplate(template, parameterValues);
			Assert.assertEquals(expected, actual);
		} catch (IOException ex) {
			fail("Accessing template faild.", ex);
		} catch (XMLStreamException ex) {
			fail("Parsing template faild.", ex);
		}
	}

	public static void assertExpansionFailure(TemplateSource template, Pattern expectedFailure) {
		assertExpansionFailure(template, Collections.<String, Object> emptyMap(), expectedFailure);
	}

	public static void assertExpansionFailure(
			TemplateSource template, Map<String, Object> parameterValues, Pattern expectedFailure) {
		try {
			TemplateExpander.expandXmlTemplate(template, parameterValues);
		} catch (Exception exception) {
			if ((exception.getMessage() != null) && expectedFailure.matcher(exception.getMessage()).matches()) {
				return;
			}
			fail("Expected an error matching '" + expectedFailure.toString() + "' but the error was: '"
				+ exception.getMessage() + "'", exception);
		}
		Assert.fail("Expected an error matching '" + expectedFailure.toString() + "' but no error was thrown.");
	}

	public static TemplateSource createTemplateSource(String fileName, Class<?> contextClass) {
		URL templateUrl = contextClass.getResource(fileName);
		if (templateUrl == null) {
			throw new RuntimeException("File not found: '" + fileName + "'");
		}
		File templateFile = FileUtilities.urlToFile(templateUrl);
		return new TemplateFilesystemSource(templateFile);
	}

	public static void fail(String message, Throwable cause) {
		throw (AssertionFailedError) new AssertionFailedError(message).initCause(cause);
	}

}
