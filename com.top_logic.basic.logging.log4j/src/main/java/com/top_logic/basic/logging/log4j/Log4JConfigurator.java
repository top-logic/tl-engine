/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.logging.log4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.top_logic.basic.logging.LogConfigurator;

/**
 * {@link LogConfigurator} for use with Log4J logging.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Log4JConfigurator extends LogConfigurator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Log4JConfigurator.class);

	@Override
	public void configureStdout(String aLevel) {
		String level = aLevel.toLowerCase();
		/* Configuring Appender STDOUT twice is actually a hack for the case that the System.out has
		 * changed, which occurs in test cases: The ConsoleAppender has an OutputStreamManager which
		 * is statically cached. The key for the cache contains the target (STD_OUT, STD_ERR), such
		 * that if the target has not changed, the ObjectStreamManager is not rebuilt. Changing the
		 * target forces Log4J2 to reset the OutputStreamManager. */
		configureSTDOUTAppender("SYSTEM_ERR", level);
		configureSTDOUTAppender("SYSTEM_OUT", level);
	}

	private void configureSTDOUTAppender(String target, String level) {
		String config =
			"<Configuration " + TRACE_EXCEPTIONS_PROPERTY + "=\"true\">" +
				"	<Appenders>" +
				"		<Console name=\"STDOUT\" target=\"" + target + "\">" +
				"			<PatternLayout pattern=\"%date{ISO8601} %-5level [%thread]: %logger - %message%n%throwable\"/>" +
				"		</Console>" +
				"	</Appenders>" +
				"	<Loggers>" +
				"		<Root level=\"" + level + "\">" +
				"			<AppenderRef ref=\"STDOUT\"/>" +
				"		</Root>" +
				"	</Loggers>" +
				"</Configuration>";

		File tmpFile;
		try {
			tmpFile = File.createTempFile("logConfigStdOut", ".xml");
			// Do not leave garbage behind.
			tmpFile.deleteOnExit();

			try (FileWriter out = new FileWriter(tmpFile)) {
				out.write(config);
			}
			configure(tmpFile.toURI().toURL());
		} catch (IOException ex) {
			LOGGER.error("Unable to configure stdout to " + level, ex);
		}
	}

	@Override
	public void configure(URL configuration) {
		LoggerContext context = LoggerContext.getContext(false);
		boolean success = internalConfigure(context, configuration);
		if (!success) {
			return;
		}
		Configuration config = context.getConfiguration();
		if (config instanceof AbstractConfiguration) {
			Node rootNode = ((AbstractConfiguration) config).getRootNode();
			Map<String, String> attributes = rootNode.getAttributes();
			String traceExceptions = attributes.get(TRACE_EXCEPTIONS_PROPERTY);
			if (traceExceptions != null) {
				setTraceExceptions(Boolean.parseBoolean(traceExceptions));
			}
			String traceMessages = attributes.get(TRACE_MESSAGES_PROPERTY);
			if (traceMessages != null) {
				setTraceMessages(Boolean.parseBoolean(traceMessages));
			}
			String exceptionLevel = attributes.get(EXCEPTION_LEVEL_PROPERTY);
			if (exceptionLevel != null) {
				setExceptionLevel(exceptionLevel);
			}
		}
	}

	@SuppressWarnings("unused")
	private boolean internalConfigure(LoggerContext context, URL configuration) {
		if (false) {
			/* Note: This would be the natural way to set the new configuration. But the given URL
			 * has a special URLStreamHandler which resolves aliases in the content. When the URL is
			 * first transformed to an URI and back, this special feature is lost. */
			try {
				context.setConfigLocation(configuration.toURI());
				return true;
			} catch (URISyntaxException ex) {
				LOGGER.error("Reconfiguration failed: Invalid configuration URL " + configuration, ex);
				return false;
			}
		} else {
			try {
				ConfigurationSource source = new ConfigurationSource(configuration.openStream(), configuration);
				Configuration newConfiguration = ConfigurationFactory.getInstance().getConfiguration(context, source);
				if (newConfiguration == null) {
					LOGGER.error("Reconfiguration failed: No configuration found for '{}'", configuration);
					return false;
				}
				context.reconfigure(newConfiguration);
				return true;
			} catch (IOException ex) {
				LOGGER.error("Unable to get content from configuration URL: " + configuration, ex,
					Log4JConfigurator.class);
				return false;
			}
		}
	}

	@Override
	public void reset() {
		configureStdout("INFO");
	}

	@Override
	public void addLogMark(String key, String value) {
		ThreadContext.put(key, value);
	}

	@Override
	public void removeLogMark(String key) {
		ThreadContext.remove(key);
	}

}
