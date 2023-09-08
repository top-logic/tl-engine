/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates.node;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.top_logic.layout.scripting.template.gui.templates.I18NConstants;
import com.top_logic.layout.scripting.template.gui.templates.Template;
import com.top_logic.template.xml.source.ResourceTransaction;
import com.top_logic.template.xml.source.TemplateSource;
import com.top_logic.template.xml.source.TemplateSourceFactory;
import com.top_logic.util.error.TopLogicException;

/**
 * A script template of the application.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateResource extends TemplateLocation {

	/**
	 * Creates a {@link TemplateResource}.
	 * 
	 * @param resourceSuffix
	 *        See {@link #getResourceSuffix()}.
	 */
	public TemplateResource(String resourceSuffix) {
		super(resourceSuffix);
	}

	@Override
	public boolean isTemplate() {
		return true;
	}

	/**
	 * Creates a {@link TemplateSource} for parsing the template.
	 */
	public TemplateSource getSource() {
		return TemplateSourceFactory.getInstance().resolve("script:" + getResourceSuffix());
	}

	/**
	 * Parses the referenced {@link Template}.
	 * 
	 * @throws XMLStreamException
	 *         If parsing the template fails.
	 * @throws IOException
	 *         If reading accessing the template source fails.
	 */
	public Template load() throws IOException, XMLStreamException {
		return Template.load(getSource());
	}

	/**
	 * Parses the referenced {@link Template} and reports potential failures using a
	 * {@link TopLogicException}.
	 * 
	 * <p>
	 * Note: This method must not be called during rendering, since it may throw a
	 * {@link TopLogicException}.
	 * </p>
	 * 
	 * @return The parsed {@link Template}.
	 * @throws TopLogicException
	 *         If loading the template fails.
	 */
	public Template tryLoad() throws TopLogicException {
		try {
			return load();
		} catch (IOException | XMLStreamException ex) {
			throw new TopLogicException(I18NConstants.ERROR_TEMPLATE_LOADING_FAILED__NAME_ERROR.fill(
				getName(), ex.getMessage()));
		}
	}

	/**
	 * Creates a {@link ResourceTransaction} for updating this resource.
	 */
	public ResourceTransaction update() throws IOException {
		return getSource().update();
	}

	/**
	 * Deletes this resource.
	 */
	public void delete() throws IOException {
		getSource().delete();
	}

}
