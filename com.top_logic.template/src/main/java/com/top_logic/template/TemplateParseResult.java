/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template;

import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.dob.MetaObject;
import com.top_logic.template.tree.Template;
import com.top_logic.template.xml.TemplateSettings;

/**
 * The result of parsing a template.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TemplateParseResult {

	private final Template _template;

	private final Collection<MetaObject> _types;

	private final FormatMap _formatsMap;

	private final TemplateSettings _settings;

	/** Creates a new immutable {@link TemplateParseResult}. */
	public TemplateParseResult(
			Template template, TemplateSettings settings, Collection<MetaObject> types, FormatMap formatsMap) {
		_template = template;
		_settings = settings;
		_types = new ArrayList<>(types);
		_formatsMap = formatsMap;
	}

	/** Returns the parsed {@link Template}. */
	public Template getTemplate() {
		return _template;
	}

	/** Getter for the {@link TemplateSettings}. */
	public TemplateSettings getSettings() {
		return _settings;
	}

	/**
	 * Returns the types that could be used in the template.
	 * <p>
	 * This includes predefined types as well as user defined ones. <br/>
	 * Returns a (mutable) copy of the internal stored {@link Collection}.
	 * </p>
	 */
	public Collection<MetaObject> getTypes() {
		return new ArrayList<>(_types);
	}

	/**
	 * Returns the mapping of types to {@link Format}s.
	 */
	public FormatMap getFormatMap() {
		return _formatsMap;
	}

}
