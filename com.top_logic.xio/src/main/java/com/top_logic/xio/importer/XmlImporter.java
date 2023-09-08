/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer;

import java.util.Collections;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.xio.importer.binding.DefaultImportContext;
import com.top_logic.xio.importer.binding.ImportContext;
import com.top_logic.xio.importer.binding.ModelBinding;
import com.top_logic.xio.importer.handlers.DispatchingImporter;
import com.top_logic.xio.importer.handlers.Handler;

/**
 * Configured importer for BPML
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class XmlImporter {

	/**
	 * Creates an {@link XmlImporter} with the given top-level import {@link Handler}.
	 *
	 * @param log
	 *        The {@link Log} output to write potential problems to.
	 * @param importConfig
	 *        The configured importer XML definition. The value is expected to be a XML serialization
	 *        of a {@link DispatchingImporter} configuration.
	 */
	public static XmlImporter newInstance(I18NLog log, Content importConfig) {
		return newInstance(log, readImportConfig(log, importConfig));
	}

	/**
	 * Creates an {@link XmlImporter} with the given top-level import {@link Handler}.
	 * 
	 * @param log
	 *        The {@link Log} output to write potential problems to.
	 * @param handler
	 *        The top-level importer {@link Handler}.
	 */
	public static XmlImporter newInstance(I18NLog log, Handler handler) {
		return new XmlImporter(log, handler);
	}

	/**
	 * Reads an import {@link Handler} configuration from XML.
	 * 
	 * @param log
	 *        The error log.
	 * @param importConfig
	 *        The configured importer source.
	 * @return The {@link Handler} performing the actual import.
	 */
	public static Handler readImportConfig(I18NLog log, Content importConfig) {
		return readImportConfig(new DefaultInstantiationContext(log.asLog()), importConfig);
	}

	/**
	 * Reads an import {@link Handler} configuration from XML.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} to instantiate the {@link Handler} configuration.
	 * @param importConfig
	 *        The configured importer source.
	 * @return The {@link Handler} performing the actual import.
	 */
	public static Handler readImportConfig(InstantiationContext context, Content importConfig) {
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap("handler",
			TypedConfiguration.getConfigurationDescriptor(DispatchingImporter.Config.class)));
		reader.setSource(importConfig);
		Handler handler;
		try {
			ConfigurationItem config = reader.read();
			handler = (Handler) context.getInstance((PolymorphicConfiguration<?>) config);
			context.checkErrors();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		return handler;
	}

	private final I18NLog _log;

	private final Handler _handler;

	private boolean _logCreations = true;

	/**
	 * Creates a {@link XmlImporter}.
	 * 
	 * @param log
	 *        The {@link Log} output to write potential problems to.
	 * @param handler
	 *        The top-level importer {@link Handler}.
	 */
	private XmlImporter(I18NLog log, Handler handler) {
		_log = log;
		_handler = handler;
	}

	/**
	 * Whether to log create operations to the appliation log.
	 */
	public boolean getLogCreations() {
		return _logCreations;
	}

	/**
	 * @see #getLogCreations()
	 */
	public void setLogCreations(boolean value) {
		_logCreations = value;
	}

	/**
	 * Processes the given input source and builds the model through the given {@link ModelBinding}.
	 *
	 * @param binding
	 *        Algorithm to build model elements from the given input source.
	 * @param source
	 *        The XML input to interpret.
	 * @return The (top-level) model element that was built by the import.
	 */
	public Object importModel(ModelBinding binding, Source source) throws XMLStreamException {
		try (DefaultImportContext importContext = new DefaultImportContext(_log, binding)) {
			importContext.setLogCreations(_logCreations);

			return readModel(source, importContext);
		}
	}

	private Object readModel(Source source, ImportContext context) throws XMLStreamException {
		XMLStreamReader in = XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(source);
		try {
			return context.importXml(_handler, in);
		} finally {
			in.close();
		}
	}

}
