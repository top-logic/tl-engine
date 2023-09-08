/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import static com.top_logic.basic.util.Utils.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.export.NoPreload;
import com.top_logic.model.export.PreloadOperation;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLTypeRefsFormat;
import com.top_logic.util.model.ModelService;

/**
 * {@link TreeModelBuilder} that can be completely configured using model queries.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp
public class TreeModelByExpression<C extends TreeModelByExpression.Config<?>> extends
		AbstractConfiguredInstance<C> implements TreeModelBuilder<Object> {

	/**
	 * Configuration options for {@link TreeModelByExpression}.
	 */
	@DisplayOrder({
		Config.MODEL_PREDICATE,
		Config.ROOT_NODE,
		Config.CHILDREN,
		Config.LEAF_PREDICATE,
		Config.NODE_PREDICATE,
		Config.PARENTS,
		Config.MODEL_QUERY,
		Config.TYPES_TO_OBSERVE,
		Config.NODES_TO_UPDATE,
		Config.FINITE,
	})
	public interface Config<I extends TreeModelByExpression<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Configuration property label for {@link #getParents()}
		 */
		String PARENTS = "parents";

		/**
		 * Configuration property label for {@link #getModelQuery()}
		 */
		String MODEL_QUERY = "modelQuery";

		/**
		 * Configuration property label for {@link #getModelPredicate()}
		 */
		String MODEL_PREDICATE = "modelPredicate";

		/**
		 * Configuration property label for {@link #getRootNode()}
		 */
		String ROOT_NODE = "rootNode";

		/**
		 * Configuration property label for {@link #getChildren()}
		 */
		String CHILDREN = "children";

		/**
		 * Configuration property label for {@link #getLeafPredicate()}
		 */
		String LEAF_PREDICATE = "leafPredicate";

		/**
		 * Configuration property label for {@link #getNodePredicate()}
		 */
		String NODE_PREDICATE = "nodePredicate";

		/**
		 * @see #isFinite()
		 */
		String FINITE = "finite";

		/**
		 * Configuration property label for {@link #getNodesToUpdate()}
		 */
		String NODES_TO_UPDATE = "nodesToUpdate";

		/** Property name of {@link #getTypesToObserve()}. */
		String TYPES_TO_OBSERVE = "typesToObserve";

		/**
		 * Whether it is possible to expand all nodes.
		 * 
		 * <p>
		 * This option might only be enabled, if the tree is guaranteed to be finite.
		 * </p>
		 * 
		 * @see TreeModelByExpression#canExpandAll()
		 */
		@BooleanDefault(true)
		@Name(FINITE)
		boolean isFinite();

		/**
		 * Function checking whether a given object is part of this tree.
		 * 
		 * <p>
		 * When e.g. an object creation is observed, the new object is inserted into this tree, if
		 * the following holds: The {@link #getNodePredicate()} returns <code>true</code> for the
		 * new object. The root node of this tree can be reached by recursively calling
		 * {@link #getParents()} on the newly created object.
		 * </p>
		 * 
		 * @see TreeModelByExpression#supportsNode(LayoutComponent, Object)
		 */
		@Name(NODE_PREDICATE)
		@ItemDefault(Expr.False.class)
		@NonNullable
		Expr getNodePredicate();

		/**
		 * Function checking whether a given node is a leaf node.
		 * 
		 * <p>
		 * A leaf node is not further queried for {@link #getChildren()}. Visually there is no
		 * difference between a leaf node and a non leaf node that has no {@link #getChildren()}.
		 * </p>
		 * 
		 * @see TreeModelByExpression#isLeaf(LayoutComponent, Object)
		 */
		@Name(LEAF_PREDICATE)
		@ItemDefault(Expr.False.class)
		@NonNullable
		Expr getLeafPredicate();

		/**
		 * Function resolving the children of a given object in this tree.
		 * 
		 * <p>
		 * The function receives a tree node as first argument and the component model as second
		 * argument. As result, a list of children nodes of the given tree node is expected. A
		 * result of <code>null</code> or the empty list means that the given node is a leaf node.
		 * </p>
		 * 
		 * @see TreeModelByExpression#getChildIterator(LayoutComponent, Object)
		 */
		@Name(CHILDREN)
		@Mandatory
		@NonNullable
		Expr getChildren();

		/**
		 * Mapping function that receives the component model returns the root node of this tree.
		 * 
		 * <p>
		 * The input component model was accepted by {@link #getModelPredicate()} before.
		 * </p>
		 * 
		 * @see TreeModelByExpression#getModel(Object, LayoutComponent)
		 */
		@Name(ROOT_NODE)
		@FormattedDefault("x -> $x")
		@NonNullable
		Expr getRootNode();

		/**
		 * Predicate that decides whether a given object is a valid input for
		 * {@link #getRootNode()}.
		 * 
		 * @see TreeModelByExpression#supportsModel(Object, LayoutComponent)
		 */
		@Name(MODEL_PREDICATE)
		@ItemDefault(Expr.True.class)
		@NonNullable
		Expr getModelPredicate();

		/**
		 * Function retrieving a valid component model from an object for which
		 * {@link #getNodePredicate()} returns <code>true</code>.
		 * 
		 * <p>
		 * A valid component model is an object, for which {@link #getModelPredicate()} returns
		 * <code>true</code>.
		 * </p>
		 * 
		 * <p>
		 * The function receives the potential tree node as first argument and the current component
		 * model as second argument. The function is expected to return an object for which
		 * {@link #getModelPredicate()} returns <code>true</code>.
		 * </p>
		 * 
		 * @see TreeModelByExpression#retrieveModelFromNode(LayoutComponent, Object)
		 */
		@Name(MODEL_QUERY)
		@ItemDefault(Expr.Null.class)
		@NonNullable
		Expr getModelQuery();

		/**
		 * Function resolving the parent node(s) in this tree.
		 * 
		 * <p>
		 * As first argument the function takes an object for which {@link #getNodePredicate()}
		 * yields <code>true</code>. The component model is passed as second argument. The function
		 * must return a list of those nodes in this tree for which the given object is in the
		 * collection computed by {@link #getChildren()}. A single result is not required to be
		 * wrapped into a list. A result of <code>null</code> is interpreted as empty list.
		 * </p>
		 * 
		 * @see TreeModelByExpression#getParents(LayoutComponent, Object)
		 */
		@Name(PARENTS)
		@ItemDefault(Expr.Null.class)
		@NonNullable
		Expr getParents();

		/**
		 * Function computing the additional nodes to update if a given object changes.
		 * 
		 * <p>
		 * In a tree, the direct parents and children of the corresponding nodes are updated by
		 * default.
		 * </p>
		 * 
		 * <p>
		 * The function receives the changed business object as first argument and the current
		 * component model as second argument.
		 * </p>
		 * 
		 * @see TreeModelByExpression#getNodesToUpdate(LayoutComponent, Object)
		 */
		@Name(NODES_TO_UPDATE)
		@ItemDefault(Expr.Null.class)
		Expr getNodesToUpdate();

		/**
		 * The types whose instances have to be observed for {@link #getNodesToUpdate()} to be
		 * triggered.
		 */
		@Name(TYPES_TO_OBSERVE)
		@Format(TLTypeRefsFormat.class)
		List<TLModelPartRef> getTypesToObserve();

	}

	/**
	 * Creates a {@link TreeModelByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TreeModelByExpression(InstantiationContext context, C config) {
		super(context, config);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLModel model = ModelService.getApplicationModel();

		_supportsNode = QueryExecutor.compile(kb, model, config.getNodePredicate());
		_isLeaf = QueryExecutor.compile(kb, model, config.getLeafPredicate());
		_children = QueryExecutor.compile(kb, model, config.getChildren());
		_getModel = QueryExecutor.compile(kb, model, config.getRootNode());
		_supportsModel = QueryExecutor.compile(kb, model, config.getModelPredicate());
		_retrieveModelFromNode = QueryExecutor.compile(kb, model, config.getModelQuery());
		_getParents = QueryExecutor.compile(kb, model, config.getParents());
		_nodesToUpdate = QueryExecutor.compile(kb, model, config.getNodesToUpdate());
		_typesToObserve = Set.copyOf(resolveTypes(context, config.getTypesToObserve()));
	}

	private final QueryExecutor _supportsNode;

	private final QueryExecutor _isLeaf;

	private final QueryExecutor _children;

	private final QueryExecutor _getModel;

	private final QueryExecutor _supportsModel;

	private final QueryExecutor _retrieveModelFromNode;

	private final QueryExecutor _getParents;

	private final QueryExecutor _nodesToUpdate;

	private final Set<TLStructuredType> _typesToObserve;

	private Set<TLStructuredType> resolveTypes(InstantiationContext context, List<TLModelPartRef> typeRefs) {
		if (typeRefs == null) {
			return Set.of();
		}
		Set<TLStructuredType> types = CollectionFactory.set();
		for (TLModelPartRef ref : typeRefs) {
			types.add(resolve(context, ref));
		}
		return types;
	}

	private TLClass resolve(InstantiationContext context, TLModelPartRef typeReference) {
		try {
			return typeReference.resolveClass();
		} catch (ConfigurationException exception) {
			context.error("Failed to resolve " + debug(typeReference) + ".", exception);
			return null;
		}
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		return asBoolean(_supportsNode.execute(node));
	}

	@Override
	public boolean canExpandAll() {
		return getConfig().isFinite();
	}

	@Override
	public boolean isLeaf(LayoutComponent contextComponent, Object node) {
		return asBoolean(_isLeaf.execute(node));
	}

	@Override
	public Iterator<? extends Object> getChildIterator(LayoutComponent contextComponent, Object node) {
		return (SearchExpression.asCollection(_children.execute(node, contextComponent.getModel()))).iterator();
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		return _getModel.execute(businessModel);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return asBoolean(_supportsModel.execute(aModel));
	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, Object node) {
		return _retrieveModelFromNode.execute(node, contextComponent.getModel());
	}

	@Override
	public Collection<? extends Object> getParents(LayoutComponent contextComponent, Object node) {
		return SearchExpression.asCollection(_getParents.execute(node, contextComponent.getModel()));
	}

	@Override
	public PreloadOperation loadForExpansion() {
		return NoPreload.INSTANCE;
	}

	@Override
	public Collection<? extends Object> getNodesToUpdate(LayoutComponent contextComponent, Object businessObject) {
		return SearchExpression.asCollection(_nodesToUpdate.execute(businessObject, contextComponent.getModel()));
	}

	@Override
	public Set<TLStructuredType> getTypesToObserve() {
		return _typesToObserve;
	}

	private static boolean asBoolean(Object result) {
		if (result instanceof Boolean) {
			return ((Boolean) result).booleanValue();
		} else {
			return false;
		}
	}

}
