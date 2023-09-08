/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link Handler} assigning the result of an {@link Config#getValue() expression evaluation} to a
 * model property.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AssignHandler<C extends AssignHandler.Config<?>> extends ConfiguredImportHandler<C> {

	/**
	 * Configuration options for {@link AssignHandler}.
	 */
	@TagName("assign")
	public interface Config<I extends AssignHandler<?>> extends ConfiguredImportHandler.Config<I> {

		/**
		 * Name of the {@link ImportContext#getVar(String) variable} that contains the object on
		 * which the {@link #getValue() expression} is evaluated.
		 */
		@StringDefault(ImportContext.SCOPE_VAR)
		@Nullable
		String getArgumentVar();

		/**
		 * Function taking one argument and producing the value to assign to {@link #getProperty()}.
		 */
		@Mandatory
		Expr getValue();

		/**
		 * Name of the {@link ImportContext#getVar(String) variable} that contains the object on
		 * which the {@link #getProperty() property} is assigned.
		 */
		@StringDefault(ImportContext.THIS_VAR)
		@Nullable
		String getTargetVar();

		/**
		 * Name of the property to which the result of the {@link #getValue() function} evaluation
		 * should be assigned.
		 */
		@Mandatory
		String getProperty();
	}

	/**
	 * Creates a {@link AssignHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AssignHandler(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		Object argument = context.getVar(getConfig().getArgumentVar());
		Object value = context.eval(getConfig().getValue(), argument);
		Object target = context.getVar(getConfig().getTargetVar());
		context.setProperty(this, target, getConfig().getProperty(), value);
		return value;
	}

}
