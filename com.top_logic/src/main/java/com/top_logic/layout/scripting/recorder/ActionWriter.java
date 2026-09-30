/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.layout.scripting.action.ApplicationAction;

/**
 * Serializes {@link ApplicationAction}s to XML.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ActionWriter {

	/**
	 * The instance of the {@link ActionWriter}. This is not a singleton, as (potential) subclasses
	 * can create further instances.
	 */
	public static final ActionWriter INSTANCE = new ActionWriter();

	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the {@link #INSTANCE}
	 * constant directly.
	 */
	protected ActionWriter() {
		// See JavaDoc above.
	}

	/**
	 * Convenience method for printing the result of {@link #writeAction(ApplicationAction)} to
	 * {@link System#out}
	 */
	public void printActionToStdOut(ApplicationAction action) {
		printAction(action, System.out);
	}

	/**
	 * Convenience method for printing the result of {@link #writeAction(ApplicationAction)} to the
	 * given {@link PrintStream}.
	 */
	public void printAction(ApplicationAction action, PrintStream target) {
		target.print(writeAction(action));
		target.print("\n");
	}

	/**
	 * Serializes the {@link ApplicationAction} to XML. Writes the necessary namespace declarations.
	 * Does not write the XML-Header.
	 */
	public String writeAction(ApplicationAction action) {
		return writeAction(action, true);
	}

	/**
	 * Serializes the {@link ApplicationAction} to XML. Does not write the XML-Header.
	 */
	public String writeAction(ApplicationAction action, boolean writeNamespaceDeclaration) {
		return writeAction(action, writeNamespaceDeclaration, false);
	}

	/**
	 * Serializes the {@link ApplicationAction} to XML.
	 */
	public String writeAction(ApplicationAction action, boolean writeNamespaceDeclaration, boolean xmlHeader) {
		XMLPrettyPrinter.Config config = XMLPrettyPrinter.newConfiguration();
		config.setXMLHeader(xmlHeader);
		config.setPreserveWhitespace(true);
		return writeAction(action, config, writeNamespaceDeclaration);
	}
	
	/**
	 * Writes the given {@link ApplicationAction} with namespace declarations.
	 *
	 * @param action The {@link ApplicationAction} to write.
	 * @param config Specification for pretty printing.
	 * @return The serialized action XML.
	 */
	public String writeAction(ApplicationAction action, XMLPrettyPrinter.Config config) {
		return writeAction(action, config, true);
	}

	private String writeAction(ApplicationAction action, XMLPrettyPrinter.Config config,
			boolean writeNamespaceDeclaration) {
		StringWriter buffer = new StringWriter();
		try (ConfigurationWriter w = createConfigurationWriter(buffer)) {
			w.write("action", ApplicationAction.class, action);
		} catch (XMLStreamException ex) {
			String errorMessage = "Action serialisation failed. Action: " + StringServices.getObjectDescription(action);
			throw (AssertionError) new AssertionError(errorMessage).initCause(ex);
		}

		String actionXml = buffer.toString();

		try {
			String prettyXML = pretty(actionXml, config).trim();
			return writeNamespaceDeclaration ? prettyXML : removeNamespaceDeclaration(prettyXML);
		} catch (UnsupportedEncodingException ex) {
			String errorMessage = "Action serialisation failed. Action: " + StringServices.getObjectDescription(action);
			throw (AssertionError) new AssertionError(errorMessage).initCause(ex);
		}
	}

	private String pretty(String xml, XMLPrettyPrinter.Config userConfig) throws UnsupportedEncodingException {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		try {
			XMLPrettyPrinter.Config config = TypedConfiguration.copy(userConfig);
			config.setAdditionalNamespaces(Collections.singletonMap(ConfigurationSchemaConstants.CONFIG_NS_PREFIX,
				ConfigurationSchemaConstants.CONFIG_NS));
			new XMLPrettyPrinter(byteBuffer, config).write(DOMUtil.parse(xml)).close();
		} catch (IOException ex) {
			throw new UnreachableAssertion("Input and output are in memory objects.");
		}
		return new String(byteBuffer.toByteArray(), userConfig.getEncoding());
	}

	private ConfigurationWriter createConfigurationWriter(StringWriter buffer)
			throws XMLStreamException {
		return createTypedConfigWriter(createXmlStreamWriter(buffer));
	}

	private XMLStreamWriter createXmlStreamWriter(StringWriter buffer) throws XMLStreamException {
		return XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(buffer);
	}

	private ConfigurationWriter createTypedConfigWriter(XMLStreamWriter xmlStreamWriter) {
		return new ConfigurationWriter(xmlStreamWriter);
	}

	private static String removeNamespaceDeclaration(String result) {
		return result.replace("\\sxmlns:" + ConfigurationSchemaConstants.CONFIG_NS_PREFIX + "=\""
			+ ConfigurationSchemaConstants.CONFIG_NS + "\"", "");
	}

}
