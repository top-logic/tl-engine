/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import static com.top_logic.basic.util.Utils.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.mig.html.ElementUpdate;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.util.error.TopLogicException;
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
		Config.SUPPORTS_MODEL_NAME,
		Config.TYPES_TO_OBSERVE,
		Config.ELEMENTS_TO_UPDATE,
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
		 * Property name for configuration option {@link #getTypesToObserve()}.
		 */
		String TYPES_TO_OBSERVE = "typesToObserve";

		/**
		 * Property name for configuration option {@link #getElementsToUpdate()}.
		 */
		String ELEMENTS_TO_UPDATE = "elementsToUpdate";

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
		 * This function decides whether a new or changed object should be included in the
		 * collection of objects being shown.
		 * 
		 * <p>
		 * This is the case if the object in question would be included in the result of
		 * re-evaluating the {@link #getElements()} function.
		 * </p>
		 * 
		 * <p>
		 * The function is expected to take two arguments: the potential list element as the first
		 * argument and the current component model as the second:
		 * <ul>
		 * <li>When the component supports the configuration of row types, the potential list
		 * element's type is checked beforehand.</li>
		 * <li>When the method returns <code>true</code> or <code>"add"</code>, the updated or
		 * created element is included within the elements.</li>
		 * <li>When the method returns <code>false</code> or <code>"remove"</code>, an updated
		 * element is removed from the elements and a newly created element is not added.</li>
		 * <li>When the method returns <code>"no-change"</code>, the element does not trigger any
		 * change in the table.</li>
		 * <li>When the method returns <code>"unknown"</code>, it cannot be determined whether the
		 * element must be added to or removed from the list. In this case, the list is recreated
		 * and the function {@link #getElements()} decides whether the object in question is added
		 * to the list.</li>
		 * </ul>
		 * </p>
		 * 
		 * <p>
		 * If no function is specified, the collection of displayed elements is always recreated
		 * whenever an updated or newly created object has the correct row type.
		 * </p>
		 */
		@Name(SUPPORTS_ELEMENT_NAME)
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

		/**
		 * Types whose instances are observed for changes in addition to the types of the list
		 * elements.
		 *
		 * <p>
		 * When an instance of one of these types is modified, the function
		 * {@link #getElementsToUpdate()} determines the list elements whose display must be
		 * refreshed, e.g. because a column shows a value of the modified object.
		 * </p>
		 *
		 * @see ListModelByExpression#getTypesToObserve()
		 */
		@Name(TYPES_TO_OBSERVE)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getTypesToObserve();

		/**
		 * Function computing the list elements to refresh when an object of one of the
		 * {@link #getTypesToObserve() observed types} is modified.
		 *
		 * <p>
		 * The function receives the modified object as first argument and the current component
		 * model as second argument. It returns the list elements whose display depends on the
		 * modified object. A list element is refreshed by default when it is modified itself.
		 * </p>
		 *
		 * <p>
		 * Example: Refresh the contact of a modified account, whose row shows values of the
		 * account:<br/>
		 * <code>account -&gt; $account.get(`tl.accounts:Person#contact`)</code>
		 * </p>
		 *
		 * @see ListModelByExpression#getElementsToUpdate(LayoutComponent, Object)
		 */
		@Name(ELEMENTS_TO_UPDATE)
		@ItemDefault(Expr.Null.class)
		Expr getElementsToUpdate();

	}

	static final String UPDATE_NO_CHANGE = "no_change";

	static final String UPDATE_UNKNOWN = "unknown";

	private QueryExecutor _elements;

	private QueryExecutor _supportsModel;

	private QueryExecutor _modelForElement;

	private QueryExecutor _supportsElement;

	private final QueryExecutor _elementsToUpdate;

	private final Set<TLStructuredType> _typesToObserve;

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
		_supportsElement = QueryExecutor.compileOptional(kb, model, config.getSupportsElement());
		_modelForElement = QueryExecutor.compile(kb, model, config.getModelForElement());
		_elementsToUpdate = QueryExecutor.compile(kb, model, config.getElementsToUpdate());
		_typesToObserve = resolveTypes(context, config.getTypesToObserve());
	}

	private static Set<TLStructuredType> resolveTypes(InstantiationContext context, List<TLModelPartRef> typeRefs) {
		Set<TLStructuredType> types = new HashSet<>();
		for (TLModelPartRef ref : typeRefs) {
			TLClass type = resolve(context, ref);
			if (type != null) {
				types.add(type);
			}
		}
		return Set.copyOf(types);
	}

	private static TLClass resolve(InstantiationContext context, TLModelPartRef typeReference) {
		try {
			return typeReference.resolveClass();
		} catch (ConfigurationException exception) {
			context.error("Failed to resolve " + debug(typeReference) + ".", exception);
			return null;
		}
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		// Note: The result contains only elements the current user is allowed to read, the executor
		// secures it, see QueryExecutor#executeWith(EvalContext, Args).
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
	public ElementUpdate supportsListElement(LayoutComponent aComponent, Object element) {
		if (!ComponentUtil.isValid(element)) {
			return ElementUpdate.REMOVE;
		}
		if (_supportsElement == null) {
			return ElementUpdate.UNKNOWN;
		}
		Object result = _supportsElement.execute(element, aComponent.getModel());
		if (result instanceof CharSequence updateString) {
			try {
				return ConfigUtil.getEnum(ElementUpdate.class, updateString.toString());
			} catch (ConfigurationException ex) {
				throw new TopLogicException(
					I18NConstants.INVALID_UPDATE_TYPE__FUN_EX.fill(_supportsElement.getSearch(), ex.getErrorKey()));
			}
		}
		if (result == null) {
			return ElementUpdate.NO_CHANGE;
		}
		return ElementUpdate.fromDecision(SearchExpression.asBoolean(result));
	}

	@Override
	public Collection<?> getElementsToUpdate(LayoutComponent component, Object businessObject) {
		return SearchExpression.asCollection(_elementsToUpdate.execute(businessObject, component.getModel()));
	}

	@Override
	public Set<TLStructuredType> getTypesToObserve() {
		return _typesToObserve;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
		return _modelForElement.execute(anObject, aComponent.getModel());
	}

}
