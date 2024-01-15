/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;

/**
 * Definition of a script that jumps to a certain object.
 */
public class LinkDefinition extends AbstractVariableDefinition<LinkDefinition.Config> {

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
		Config.TARGET_COMPONENT,
	})
	@TagName("link-definition")
	public interface Config extends VariableDefinition.Config<LinkDefinition> {

		/**
		 * @see #getTargetObject()
		 */
		String TARGET_OBJECT = "target-object";

		/**
		 * @see #getTargetComponent()
		 */
		String TARGET_COMPONENT = "target-component";

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

		/**
		 * The component to show the clicked object in.
		 * 
		 * <p>
		 * If not given, the default view of the clicked object is shown.
		 * </p>
		 */
		@Name(TARGET_COMPONENT)
		ComponentName getTargetComponent();
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
	public Object eval(LayoutComponent component, FormEditorContext editorContext, Object model) {
		return new GotoFragment(_targetObject != null ? _targetObject.execute(model) : model,
			getConfig().getTargetComponent());
	}

	private static final class GotoFragment implements HTMLFragment {
		private final Object _model;

		private final ComponentName _commentName;

		/**
		 * Creates a {@link GotoFragment}.
		 */
		public GotoFragment(Object model, ComponentName commentName) {
			_model = model;
			_commentName = commentName;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			GotoHandler.appendJSCallStatement(context, out, _model, _commentName);
		}
	}

}
