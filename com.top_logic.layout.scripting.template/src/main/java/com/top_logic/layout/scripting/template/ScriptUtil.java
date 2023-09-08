/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template;

import java.io.File;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ActionWriter;
import com.top_logic.layout.scripting.runtime.ActionReader;
import com.top_logic.template.expander.TemplateExpander;
import com.top_logic.template.xml.source.TemplateSource;
import com.top_logic.template.xml.source.TemplateSourceFactory;

/**
 * Useful methods when working with {@link ApplicationAction} scripts.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptUtil {

	/**
	 * The instance of the {@link ScriptUtil}. This is not a singleton, as (potential) subclasses
	 * can create further instances.
	 */
	public static final ScriptUtil DEFAULT = new ScriptUtil();

	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the {@link #DEFAULT}
	 * constant directly.
	 */
	protected ScriptUtil() {
		// See JavaDoc above.
	}

	/**
	 * Serializes the {@link ApplicationAction} to XML.
	 * <p>
	 * Writes the necessary namespace declarations. <b>Does not write the XML-Header.</b>
	 * </p>
	 * 
	 * @see ActionWriter
	 */
	public String actionToXml(ApplicationAction action) {
		return ActionWriter.INSTANCE.writeAction(action);
	}

	/**
	 * Parse the {@link ApplicationAction} from the given {@link String}.
	 * 
	 * @see ActionReader
	 */
	public ApplicationAction parseAction(String actionXml) {
		try {
			return ActionReader.INSTANCE.readAction(actionXml);
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Read the {@link ApplicationAction} from the given {@link File}.
	 * 
	 * @see ActionReader
	 */
	public ApplicationAction readAction(File file) {
		return ActionReader.INSTANCE.readAction(file);
	}

	/**
	 * Expands the given template in the given {@link File} with the given template parameters and
	 * parses the resulting {@link ApplicationAction} script.
	 * <p>
	 * There is no variant of this method that accepts a {@link String} instead of a {@link File},
	 * as a template may reference another template. Those references use relative paths and
	 * therefore don't work if the path (= the {@link File}) is unknown. Which would be the case if
	 * only a {@link String} would be used to expand the template.
	 * </p>
	 * 
	 * @param templateResource
	 *        Must not be <code>null</code>.
	 * @param templateParameters
	 *        The values for the template parameters. Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public ApplicationAction createActionFromTemplate(String templateResource, Map<String, ?> templateParameters) {
		try {
			return parseScript(expandTemplate(templateResource, templateParameters));
		} catch (Exception ex) {
			throw new RuntimeException("Creating the actions failed for '" + templateResource
				+ "'; Message: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Expands the given template and returns the result as {@link String}.
	 * <p>
	 * There is no variant of this method that accepts a {@link String} instead of a {@link File},
	 * as a template may reference another template. Those references use relative paths and
	 * therefore don't work if the path (= the {@link File}) is unknown. Which would be the case if
	 * only a {@link String} would be used to expand the template.
	 * </p>
	 * 
	 * @param templateResource
	 *        Must not be <code>null</code>.
	 * @param templateParameters
	 *        The values for the template parameters. Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public String expandTemplate(String templateResource, Map<String, ?> templateParameters) {
		TemplateSource source = TemplateSourceFactory.getInstance().resolve(templateResource);
		String result = null;
		try {
			result = TemplateExpander.expandXmlTemplate(source, templateParameters);
		} catch (Throwable e) {
			throw new RuntimeException("Expanding the template failed! File: '" + templateResource
				+ "', Cause: " + e.getMessage(), e);
		}

		if (result == null) {
			throw new NullPointerException("TemplateExpander returned null for file: "
				+ templateResource);
		}
		return result;
	}

	/**
	 * Creates the {@link ApplicationAction}s by parsing the given script.
	 * <p>
	 * <b>This is an internal method, that should be used only by the {@link ScriptUtil} and it's
	 * subclasses.</b> If you want to parse {@link ApplicationAction}s from a {@link String}, please
	 * use the {@link ActionReader} instead.
	 * </p>
	 * 
	 * @param script
	 *        Must not be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	protected ApplicationAction parseScript(String script) {
		try {
			ApplicationAction action = ActionReader.INSTANCE.readAction(script);
			if (action == null) {
				throw new RuntimeException("Parsing the test script failed!");
			}
			return action;
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Parsing the test script failed! Message: " + ex.getMessage(), ex);
		}
	}

}
