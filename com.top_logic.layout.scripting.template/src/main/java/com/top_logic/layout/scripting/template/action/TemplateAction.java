/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.action;

import java.util.List;

import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.DynamicAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * An {@link ApplicationAction} that expands a template and executes the resulting
 * {@link ApplicationAction}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public interface TemplateAction extends DynamicAction {

	/**
	 * @see #getTemplateNameComment()
	 */
	public static final String TEMPLATE_NAME_COMMENT = "template-name-comment";

	/** A parameter of for a template. */
	public static interface Parameter extends NamedConfiguration {

		/** The name of the template parameter. */
		@Override
		public String getName();

		/**
		 * The value of the parameter.
		 * <p>
		 * If the value is a string, it can be written directly into the xml-attribute.
		 * </p>
		 */
		@Format(StringValueFormat.class)
		public ModelName getValue();

		/** @see #getValue() */
		public void setValue(ModelName value);

	}

	/**
	 * A descriptive name of the template for display in a UI.
	 */
	@Name(TEMPLATE_NAME_COMMENT)
	String getTemplateNameComment();

	/**
	 * @see #getTemplateNameComment()
	 */
	void setTemplateNameComment(String value);

	/** The parameters of the template. */
	@Key(Parameter.NAME_ATTRIBUTE)
	public List<Parameter> getParameters();

	/** @see #getParameters() */
	public void setParameters(List<Parameter> parameters);

}
