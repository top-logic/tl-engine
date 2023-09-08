/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.specialcases;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.scripting.ScriptingUtil;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.TLObjectTreeNaming;
import com.top_logic.layout.scripting.recorder.ref.value.object.CompactLabelPath;
import com.top_logic.layout.scripting.recorder.ref.value.object.ContextRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.GlobalVariableNaming;
import com.top_logic.layout.scripting.recorder.ref.value.object.IndexedObjectNaming;
import com.top_logic.layout.scripting.recorder.ref.value.object.LabelPath;
import com.top_logic.layout.scripting.recorder.ref.value.object.ValuePath;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * Identifies an TreeData node by a path of labels (from the root node to the target target node).
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan.Stolzenburg</a>
 */
class TreeValueScope implements ValueScope {

	private final TLTreeModel _treeModel;

	private final LabelProvider _labelProvider;

	private final boolean _asBusinessObject;

	public TreeValueScope(TLTreeModel treeModel, LabelProvider labelProvider, boolean asBusinessObject) {
		_treeModel = treeModel;
		_labelProvider = (labelProvider != null) ? labelProvider : MetaLabelProvider.INSTANCE;
		_asBusinessObject = asBusinessObject;
	}

	@Override
	@SuppressWarnings("deprecation") // As long as the deprecated class exists, it has to be supported here.
	public Object resolveReference(ActionContext actionContext, ContextRef contextRef) {
		Object result;
		if ((contextRef instanceof LabelPath) || (contextRef instanceof CompactLabelPath)) {
			result = ScriptingUtil.findNodeByLabelPath(getLabelPath(contextRef), _treeModel, _labelProvider);
		} else {
			result =
				ScriptingUtil.findNodeByValuePath((ValuePath) contextRef, _treeModel, _labelProvider, actionContext);
		}
		if (_asBusinessObject) {
			return _treeModel.getBusinessObject(result);
		} else {
			return result;
		}
	}

	@SuppressWarnings("deprecation")
	// As long as the deprecated class exists, it has to be supported here.
	private List<String> getLabelPath(ContextRef contextRef) {
		if (contextRef instanceof LabelPath) {
			return ((LabelPath) contextRef).getLabelPath();
		} else {
			return ((CompactLabelPath) contextRef).getLabelPath();
		}
	}

	/**
	 * @param targetNode
	 *        Can be either a TLTreeNode, a DefaultMutableTreeNode or directly the business object.
	 */
	@Override
	public Maybe<? extends ModelName> buildReference(Object targetNode) {
		List<?> pathWithRoot = createPathToRoot(targetNode);
		if (pathWithRoot == null) {
			return Maybe.none();
		}
		// Is: Root last; We want: Root first
		Collections.reverse(pathWithRoot);

		Maybe<List<String>> labels = ScriptingUtil.createTreeLabelPath(targetNode, _treeModel, _labelProvider);
		if (labels.hasValue()) {
			return Maybe.some(ReferenceInstantiator.labelPath(labels.get()));
		}
		List<?> path = pathWithRoot.subList(1, pathWithRoot.size());
		List<ModelName> namePath = new ArrayList<>();
		for (Object nodeObject : path) {
			Object businessObject;
			if (nodeObject instanceof TLTreeNode) {
				businessObject = ((TLTreeNode<?>) nodeObject).getBusinessObject();
			} else if (nodeObject instanceof DefaultMutableTreeNode) {
				businessObject = ((DefaultMutableTreeNode) nodeObject).getUserObject();
			} else {
				businessObject = nodeObject;
			}
			Maybe<? extends ModelName> valueRef =
				ModelResolver.buildModelNameIfAvailable(getSiblings(nodeObject), businessObject);
			if (valueRef.hasValue()) {
				namePath.add(valueRef.get());
			} else {
				return Maybe.none();
			}
		}
		if (!namePath.isEmpty()) {
			ModelName lastStep = namePath.get(namePath.size() - 1);
			if (lastStep instanceof TLObjectTreeNaming.TLObjectTreeName
				|| lastStep instanceof IndexedObjectNaming.Name
				|| lastStep instanceof GlobalVariableNaming.GlobalVariableName) {
				// ModelName's of these types identifies an object globally
				return Maybe.some(lastStep);
			}

		}

		return Maybe.some(ReferenceInstantiator.valuePath(namePath));
	}

	/**
	 * Workaround for the fact that the {@link #_treeModel} node type is unknown.
	 * <p>
	 * The "targetNode" object might be the business object of the actual tree node object. In that
	 * case, {@link TLTreeModel#createPathToRoot(Object)} will throw a {@link ClassCastException}.
	 * </p>
	 */
	private List<?> createPathToRoot(Object targetNode) {
		try {
			return _treeModel.createPathToRoot(targetNode);
		} catch (ClassCastException ex) {
			return null;
		}
	}

	private Object getSiblings(Object node) {
		return _treeModel.getChildren(_treeModel.getParent(node));
	}
}
