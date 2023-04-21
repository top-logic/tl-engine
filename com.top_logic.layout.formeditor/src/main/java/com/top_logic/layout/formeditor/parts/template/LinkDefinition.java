/*
 * Copyright (c) 2022 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.layout.formeditor.parts.template;

import java.io.IOError;
import java.io.IOException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;

/**
 * Definition of a script that jumps to a certain object.
 */
public class LinkDefinition extends AbstractConfiguredInstance<LinkDefinition.Config> implements VariableDefinition {

	private QueryExecutor _targetObject;

	/**
	 * Definition of an <code>onclick</code> script that jumps to an object or opens a dialog for an
	 * object.
	 * 
	 * <p>
	 * The variable that is defined by a link computation must be expanded within an
	 * <code>onclick</code> attribute of some element that should trigger the link.
	 * </p>
	 */
	@DisplayOrder({
		Config.NAME,
		Config.TARGET_OBJECT,
	})
	@TagName("link-definition")
	public interface Config extends VariableDefinition.Config<LinkDefinition> {

		/**
		 * @see #getTargetObject()
		 */
		String TARGET_OBJECT = "target-object";

		/**
		 * Function taking the rendered object as argument and computing the object that should be
		 * displayed when clicking.
		 * 
		 * <p>
		 * By default, the rendered object is displayed.
		 * </p>
		 */
		@Name(TARGET_OBJECT)
		Expr getTargetObject();
	}

	/**
	 * Creates a {@link LinkDefinition} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LinkDefinition(InstantiationContext context, Config config) {
		super(context, config);

		_targetObject = QueryExecutor.compileOptional(config.getTargetObject());
	}

	@Override
	public Object eval(DisplayContext displayContext, Object model) {
		StringBuilder out = new StringBuilder();
		try {
			Object target = _targetObject != null ? _targetObject.execute(model) : model;
			GotoHandler.appendJSCallStatement(displayContext, out, target, null);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return out.toString();
	}

}
