/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Collection;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;

/**
 * {@link ListModelBuilder} that can be completely configured using model queries.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class ListModelByExpression<C extends ListModelByExpression.Config<?>>
		extends AbstractConfiguredInstance<C> implements ListModelBuilder {

	/**
	 * Configuration options for {@link ListModelByExpression}.
	 */
	@DisplayOrder({
		Config.ELEMENTS_NAME,
		Config.SUPPORTS_ELEMENT_NAME,
		Config.MODEL_FOR_ELEMENT_NAME,
		Config.SUPPORTS_MODEL_NAME
	})
	public interface Config<I extends ListModelByExpression<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Property name for configuration option {@link #getModelForElement()}.
		 */
		String MODEL_FOR_ELEMENT_NAME = "modelForElement";
		
		/**
		 * Property name for configuration option {@link #getSupportsElement()}.
		 */
		String SUPPORTS_ELEMENT_NAME = "supportsElement";
		
		/**
		 * Property name for configuration option {@link #getSupportsModel()}.
		 */
		String SUPPORTS_MODEL_NAME = "supportsModel";

		/**
		 * Property name for configuration option {@link #getElements()}.
		 */
		String ELEMENTS_NAME = "elements";

		/**
		 * Expression retrieving list elements in the context of the component's model.
		 * 
		 * <p>
		 * The expression is expected to be a function taking the component's model as single
		 * argument.
		 * </p>
		 * 
		 * <p>
		 * Example: To retrieve all elements with a specific type, for instance all Demo A, you have
		 * to write:
		 * 
		 * <pre>
		 * <code>all(`DemoTypes:DemoTypes.A`)</code>
		 * </pre>
		 * 
		 * as shortcut for
		 * 
		 * <pre>
		 * <code>model -> all(`DemoTypes:DemoTypes.A`)</code>
		 * </pre>
		 * </p>
		 */
		@Name(ELEMENTS_NAME)
		@Mandatory
		@NonNullable
		Expr getElements();

		/**
		 * Function deciding whether some object can be used as component model.
		 * 
		 * <p>
		 * The expression is expected to be a function taking the candidate for the component's
		 * model as single argument.
		 * </p>
		 * 
		 * Example: To accept only component models of type <code>DemoTypes:DemoTypes.A</code>, you
		 * have to use the predicate:
		 * 
		 * <pre>
		 * <code>model -> $model.instanceOf(`DemoTypes:DemoTypes.A`)</code>
		 * </pre>
		 */
		@Name(SUPPORTS_MODEL_NAME)
		@ItemDefault(Expr.True.class)
		@NonNullable
		Expr getSupportsModel();

		/**
		 * Function deciding whether some (new/changed) object should (now) be part of the
		 * collection of objects being shown.
		 * 
		 * <p>
		 * This is the case, if the object in question would (now) be included in the result of the
		 * evaluation of the {@link #getElements()} function, when re-evaluating it.
		 * </p>
		 * 
		 * <p>
		 * The function is expected take two arguments, the potential list element as first argument
		 * and the current component model as second argument.
		 * </p>
		 */
		@Name(SUPPORTS_ELEMENT_NAME)
		@ItemDefault(Expr.True.class)
		@NonNullable
		Expr getSupportsElement();

		/**
		 * Function retrieving a component model for an element to select.
		 *
		 * <h3>Parameters</h3>
		 * 
		 * <table class="tlDocTable">
		 * <thead>
		 * <tr>
		 * <th>Name</th>
		 * <th>Beschreibung</th>
		 * </tr>
		 * </thead>
		 * 
		 * <tbody>
		 * <tr>
		 * <td><code>selection</code></td>
		 * <td>A new selection that should be set in the component. Note: In a component supporting
		 * multiple selection, this may be a collection of elements.</td>
		 * </tr>
		 * 
		 * <tr>
		 * <td><code>model</code></td>
		 * <td>The current component's model.</td>
		 * </tr>
		 * </tbody>
		 * </table>
		 * 
		 * <h3>Result</h3>
		 * 
		 * <p>
		 * The component's new model adjusted to the given new selection to set. The new model
		 * should be chosen so that the function "{@link #getElements() list elements}" will compute
		 * a list that contains the given selection.
		 * </p>
		 * 
		 * <p>
		 * Note: In the argument order, the component's model goes last to allow leaving out this
		 * argument definition if the argument is not required.
		 * </p>
		 */
		@Name(MODEL_FOR_ELEMENT_NAME)
		@FormattedDefault("element->model->$model")
		@NonNullable
		Expr getModelForElement();

	}

	private QueryExecutor _elements;

	private QueryExecutor _supportsModel;

	private QueryExecutor _modelForElement;

	private QueryExecutor _supportsElement;

	/**
	 * Creates a {@link ListModelByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ListModelByExpression(InstantiationContext context, C config) {
		super(context, config);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLModel model = ModelService.getApplicationModel();
		_elements = QueryExecutor.compile(kb, model, config.getElements());
		_supportsModel = QueryExecutor.compile(kb, model, config.getSupportsModel());
		_supportsElement = QueryExecutor.compile(kb, model, config.getSupportsElement());
		_modelForElement = QueryExecutor.compile(kb, model, config.getModelForElement());
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return (Collection<?>) _elements.execute(businessModel);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		if (!ComponentUtil.isValid(aModel)) {
			return false;
		}
		return (Boolean) _supportsModel.execute(aModel);
	}

	@Override
	public boolean supportsListElement(LayoutComponent aComponent, Object element) {
		if (!ComponentUtil.isValid(element)) {
			return false;
		}
		return (Boolean) _supportsElement.execute(element, aComponent.getModel());
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
		return _modelForElement.execute(anObject, aComponent.getModel());
	}

}
