/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.mig.html.layout.LayoutComponent.Config;

/**
 * {@link TLLayout} wrapping a fixed {@link Config}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class FixedLayout implements TLLayout {

	private LayoutComponent.Config _config;

	/**
	 * Creates a new fixed layout for the given configuration.
	 * 
	 * @param config
	 *        Layout component configuration.
	 */
	public FixedLayout(LayoutComponent.Config config) {
		_config = config;
	}

	@Override
	public Config get() {
		return _config;
	}

	@Override
	public String getTemplateName() {
		throw new UnsupportedOperationException("Layout does not come from a template.");
	}

	@Override
	public ConfigurationItem getArguments() throws ConfigurationException {
		throw new UnsupportedOperationException("Layout does not come from a template.");
	}

	@Override
	public boolean hasTemplate() {
		return false;
	}

	@Override
	public void writeTo(OutputStream stream, boolean markFinal) throws IOException {
		Config configToWrite;
		if (markFinal == _config.isFinal()) {
			configToWrite = _config;
		} else {
			configToWrite = TypedConfiguration.copy(_config);
			configToWrite.setFinal(markFinal);
		}

		try (Writer out = new OutputStreamWriter(stream, StringServices.CHARSET_UTF_8)) {
			try (ConfigurationWriter confWriter = new ConfigurationWriter(out)) {
				confWriter.write(LayoutComponent.Config.COMPONENT, LayoutComponent.Config.class, configToWrite);
			}
		} catch (XMLStreamException exception) {
			throw new IOException(exception);
		}
	}

}
