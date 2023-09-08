/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.CssClass;
import com.top_logic.layout.scripting.template.gui.ScriptRecorderComponent;

/**
 * Declarative form for editing a template.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DisplayOrder({ TemplateEditModel.PARAMETERS, TemplateEditModel.CONTENT })
public interface TemplateEditModel extends NamedConfigMandatory {
	
	/**
	 * @see #getContent()
	 */
	String PARAMETERS = "parameters";

	/**
	 * @see #getParameters()
	 */
	String CONTENT = "content";

	/**
	 * The path name of the template.
	 */
	@Override
	@Constraint(NameConstraint.class)
	String getName();

	/**
	 * The parameters of the template.
	 */
	@ControlProvider(TabularParameterDisplay.class)
	@Name(PARAMETERS)
	List<TemplateEditModel.Parameter> getParameters();

	/**
	 * The body of the template.
	 */
	@ControlProvider(MultiLineText.class)
	@CssClass(ScriptRecorderComponent.ACTION_FIELD_CSS_CLASS)
	@Name(CONTENT)
	String getContent();
	
	/**
	 * @see #getContent()
	 */
	void setContent(String value);
	
	/**
	 * Edit model of a single template parameter.
	 * 
	 * @see TemplateEditModel#getParameters()
	 */
	@DisplayOrder({ Parameter.NAME_ATTRIBUTE, Parameter.DEFAULT })
	interface Parameter extends NamedConfigMandatory {

		/**
		 * @see #getDefault()
		 */
		String DEFAULT = "default";

		@Override
		@RegexpConstraint("[a-zA-Z_][a-zA-Z_0-9]*")
		String getName();

		/**
		 * The optional default value of the parameter.
		 */
		@Name(DEFAULT)
		@Nullable
		String getDefault();

		void setDefault(String value);

	}

	/**
	 * {@link ValueConstraint} for a template name.
	 * 
	 * @see TemplateEditModel#getName()
	 */
	public class NameConstraint extends ValueConstraint<String> {

		/**
		 * The required file name suffix.
		 */
		private static final String SUFFIX = ".xml";

		private static final String INVALID_CHARS = "\\:*?\"<>|~'$&()";

		private static final Set<Integer> INVALID_CHARS_SET = toSet(INVALID_CHARS);

		/**
		 * Singleton {@link TemplateEditModel.NameConstraint} instance.
		 */
		public static final NameConstraint INSTANCE = new NameConstraint();

		private NameConstraint() {
			super(String.class);
		}

		@Override
		protected void checkValue(PropertyModel<String> propertyModel) {
			String value = propertyModel.getValue();
			if (value.isEmpty()) {
				propertyModel.setProblemDescription(I18NConstants.ERROR_FILE_NAME_MUST_NOT_BE_EMPTY);
				return;
			}
			if (containsInvalidCharacters(value)) {
				propertyModel.setProblemDescription(
					I18NConstants.ERROR_FILE_NAME_MUST_NOT_CONTAIN__CHARS.fill(INVALID_CHARS));
				return;
			}
			String[] parts = value.split("/", -1);
			for (String part : parts) {
				if (part.isEmpty()) {
					propertyModel.setProblemDescription(I18NConstants.ERROR_FILE_NAME_MUST_NOT_BE_EMPTY);
					return;
				}
				if (part.startsWith(".")) {
					propertyModel.setProblemDescription(I18NConstants.ERROR_FILE_NAME_MUST_NOT_START_WITH_DOT);
					return;
				}
				if (Character.isWhitespace(part.charAt(0)) || Character.isWhitespace(part.charAt(part.length() - 1))) {
					propertyModel
						.setProblemDescription(I18NConstants.ERROR_FILE_NAME_MUST_NOT_START_OR_END_WITH_WHITE_SPACE);
					return;
				}
			}
			if (!parts[parts.length - 1].endsWith(SUFFIX)) {
				propertyModel.setProblemDescription(I18NConstants.ERROR_FILE_NAME_MUST_END_IN__SUFFIX.fill(
					SUFFIX));
				return;
			}
		}

		private boolean containsInvalidCharacters(CharSequence string) {
			return string.chars().anyMatch(this::isInvalidCharacter);
		}

		private boolean isInvalidCharacter(int character) {
			return (character < 32) || (character >= 127) || INVALID_CHARS_SET.contains(character);
		}

		private static Set<Integer> toSet(CharSequence string) {
			return string.chars().boxed().collect(Collectors.toSet());
		}

	}

}