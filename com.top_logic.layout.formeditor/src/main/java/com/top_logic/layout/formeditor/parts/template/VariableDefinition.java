/*
 * Copyright (c) 2022 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.layout.formeditor.parts.template;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.DisplayContext;

/**
 * Algorithm defining the value of a template variable.
 */
public interface VariableDefinition {

	/**
	 * The configuration of a {@link VariableDefinition}.
	 */
	@Abstract
	interface Config<I extends VariableDefinition> extends PolymorphicConfiguration<I> {
		/**
		 * @see #getName()
		 */
		String NAME = "name";

		/**
		 * The name of the variable to bind.
		 */
		@Name(NAME)
		@Mandatory
		String getName();

	}

	/**
	 * Computes the value of the template variable.
	 *
	 * @param displayContext
	 *        The current {@link DisplayContext}.
	 * @param model
	 *        The currently rendered object.
	 * @return The value that should be rendered for the template variable.
	 */
	Object eval(DisplayContext displayContext, Object model);

}
