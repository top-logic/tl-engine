/*
 * Copyright (c) 2022 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.layout.formeditor.parts.template;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;

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
	 * @param component
	 *        The context component.
	 * @param model
	 *        The currently rendered object.
	 * @return The value that should be rendered when expanding the template variable.
	 */
	EvalResult eval(DisplayContext displayContext, LayoutComponent component, Object model);

	/**
	 * Potentially observable evaluation result of a {@link VariableDefinition}.
	 */
	interface EvalResult {
		/**
		 * The actual value to render.
		 */
		Object getValue(DisplayContext displayContext);

		/**
		 * Adds an {@link InvalidateListener} to this result that is informed, when the value has
		 * potentially changed.
		 *
		 * @param listener
		 *        The listener to register.
		 * @return Whether the listener was registered. If <code>false</code>, the listener was
		 *         already registered, or the value is not observable.
		 * 
		 * @implNote Only a single listener can be registered at any time.
		 */
		boolean addInvalidateListener(InvalidateListener listener);

		/**
		 * Removes the given {@link InvalidateListener} from this result.
		 *
		 * @param listener
		 *        The listener to remove.
		 * @return Whether the given listener was registered before.
		 */
		boolean removeInvalidateListener(InvalidateListener listener);

		/**
		 * Call-back interface for notifying observers of an {@link EvalResult} that the value has
		 * potentially changed.
		 */
		interface InvalidateListener {
			/**
			 * The value of the given {@link EvalResult} has potentially changed and must be
			 * re-evaluated.
			 * 
			 * <p>
			 * Note: A listener is only informed once. After invalidation a listener is
			 * automatically removed.
			 * </p>
			 * 
			 * @param result
			 *        The {@link EvalResult} that was observed.
			 */
			void handleValueInvalidation(EvalResult result);
		}
	}

}
