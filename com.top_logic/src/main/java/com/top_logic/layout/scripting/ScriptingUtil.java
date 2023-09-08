/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ValueResolver;
import com.top_logic.layout.scripting.recorder.ref.value.object.CompactLabelPath;
import com.top_logic.layout.scripting.recorder.ref.value.object.LabeledValue;
import com.top_logic.layout.scripting.recorder.ref.value.object.NamedValue;
import com.top_logic.layout.scripting.recorder.ref.value.object.ValuePath;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.structure.BrowserWindowControl;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponentScope;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.util.Utils;

/**
 * Generally useful methods for working with the scripting framework.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptingUtil {

	/**
	 * Finds the node specified by the label path in the tree. The first element in the path
	 * identifies the root of the tree. The last element identifies the searched node.
	 * 
	 * @param labelPath
	 *        Must not be <code>null</code>.
	 * @param treeModel
	 *        Must not be <code>null</code>.
	 * @param labelProvider
	 *        Must not be <code>null</code>.
	 * @return Can be null. (If the {@link TLTreeModel} allows <code>null</code> as nodes.
	 */
	public static Object findNodeByLabelPath(
			CompactLabelPath labelPath, TLTreeModel treeModel, LabelProvider labelProvider) {

		return findNodeByLabelPath(labelPath.getLabelPath(), treeModel, labelProvider);
	}

	/**
	 * A variant of {@link #findNodeByLabelPath(CompactLabelPath, TLTreeModel, LabelProvider)} that
	 * gets not a {@link CompactLabelPath} but directly a {@link List} of {@link String}s.
	 * 
	 * @see #findNodeByLabelPath(List, TLTreeModel, LabelProvider, boolean)
	 * 
	 * @param labelPath
	 *        Must not be <code>null</code>.
	 * @param treeModel
	 *        Must not be <code>null</code>.
	 * @param labelProvider
	 *        Must not be <code>null</code>.
	 * @return Can be null. (If the {@link TLTreeModel} allows <code>null</code> as nodes.
	 */
	public static Object findNodeByLabelPath(List<String> labelPath, TLTreeModel treeModel, LabelProvider labelProvider) {
		boolean containsRoot = isRootGiven(labelPath, treeModel, labelProvider);
		return findNodeByLabelPath(labelPath, treeModel, labelProvider, containsRoot);
	}

	/**
	 * A variant of {@link #findNodeByLabelPath(CompactLabelPath, TLTreeModel, LabelProvider)} that
	 * gets not a {@link CompactLabelPath} but directly a {@link List} of {@link String}s.
	 * 
	 * @param labelPath
	 *        Must not be <code>null</code>.
	 * @param treeModel
	 *        Must not be <code>null</code>.
	 * @param labelProvider
	 *        Must not be <code>null</code>.
	 * @param pathContainsRoot
	 *        Whether the label path contains root at position 0.
	 * @return Can be null. (If the {@link TLTreeModel} allows <code>null</code> as nodes.
	 */
	public static Object findNodeByLabelPath(List<String> labelPath, TLTreeModel treeModel, LabelProvider labelProvider,
			boolean pathContainsRoot) {
		if (pathContainsRoot) {
			labelPath.remove(0);
		}

		Object child = treeModel.getRoot();
		for (Object label : labelPath) {
			List<?> children = treeModel.getChildren(child);
			child = findNodeByLabel(label, children, labelProvider);
		}
		return child;
	}

	private static boolean isRootGiven(List<String> labelPath, TLTreeModel treeModel, LabelProvider labelProvider) {
		if (labelPath.isEmpty()) {
			return false;
		}
		String actualRootLabel = labelProvider.getLabel(treeModel.getRoot());
		String possibleRootLabel = labelPath.get(0);
		if (Utils.equals(possibleRootLabel, actualRootLabel)) {
			checkNoChildHasRootsLabel(treeModel, labelProvider);
			return true;
		} else {
			return false;
		}
	}

	private static void checkNoChildHasRootsLabel(TLTreeModel treeModel, LabelProvider labelProvider) {
		String actualRootLabel = labelProvider.getLabel(treeModel.getRoot());
		for (Object child : treeModel.getChildren(treeModel.getRoot())) {
			String childLabel = labelProvider.getLabel(child);
			assert !Utils.equals(childLabel, actualRootLabel);
		}
	}

	private static Object findNodeByLabel(Object labelOfSearchChild, List<?> children, LabelProvider labelProvider) {
		for (Object currentChild : children) {
			String labelOfCurrentChild = labelProvider.getLabel(currentChild);
			if (StringServices.equals(labelOfCurrentChild, labelOfSearchChild)) {
				return currentChild;
			}
		}

		StringBuilder foundNames = new StringBuilder();
		for (Object currentChild : children) {
			if (foundNames.length() > 0) {
				foundNames.append(", ");
			}
			foundNames.append(labelProvider.getLabel(currentChild));
		}

		throw new AssertionError("Could not find a child with the given label. Searched label: '"
				+ labelOfSearchChild + "', all existing labels: '" + foundNames + "'");
	}

	/**
	 * Finds the {@link TLTreeNode} under the given path of business objects. The path ends with the
	 * searched node and does not include root.
	 */
	public static Object findNodeByValuePath(ValuePath valuePath, TLTreeModel<?> treeModel, LabelProvider labelProvider,
			ActionContext actionContext) {

		return findNodeByValuePath(valuePath.getNodes(), treeModel, labelProvider, actionContext);
	}

	/**
	 * Variant of {@link #findNodeByValuePath(ValuePath, TLTreeModel, LabelProvider, ActionContext)}
	 * that gets not a {@link ValuePath} but directly a {@link List} of {@link ModelName}s.
	 */
	public static Object findNodeByValuePath(List<ModelName> valuePath, TLTreeModel treeModel,
			LabelProvider labelProvider, ActionContext actionContext) {
		Object currentNode = treeModel.getRoot();
		for (ModelName valueRef : valuePath) {
			List<?> childNodes = treeModel.getChildren(currentNode);
			Object searchedBusinessObject;
			if (valueRef instanceof NamedValue) {
				List<Object> childBusinessObjects = ScriptingUtil.getBusinessObjects(childNodes, treeModel);
				searchedBusinessObject =
					ValueNamingScheme.resolveNamedValue(actionContext, childBusinessObjects, (NamedValue) valueRef);
			} else if (valueRef instanceof LabeledValue) {
				searchedBusinessObject = ValueResolver.resolveLabeledValue(
					actionContext, childNodes, labelProvider, (LabeledValue) valueRef);
			} else {
				searchedBusinessObject = actionContext.resolve(valueRef, childNodes);
			}
			currentNode = findNodeByBusinessObject(searchedBusinessObject, childNodes, treeModel);
		}
		return currentNode;
	}

	private static Object findNodeByBusinessObject(Object businessObject, List<?> nodes, TLTreeModel treeModel) {
		Object match = null;
		for (Object node : nodes) {
			if (isSearchedNode(businessObject, node, treeModel)) {
				if (match != null) {
					throw new AssertionError(
						"More than one node on this level of the tree has the searched business object. Searched: "
							+ StringServices.getObjectDescription(businessObject) + "; First match: "
							+ StringServices.getObjectDescription(treeModel.getBusinessObject(match))
							+ "; Second match: "
							+ StringServices.getObjectDescription(treeModel.getBusinessObject(node)));
				}
				match = node;
			}
		}

		if (match == null) {
			throw new AssertionError("Could not find a child with the given business object. Searched: "
				+ StringServices.getObjectDescription(businessObject));
		}
		return match;
	}

	private static boolean isSearchedNode(Object businessObject, Object node, TLTreeModel treeModel) {
		// In some cases (in Coms at least) the tree node to find IS the business object.
		// And the business object of the node is something else...
		// Therefore, we check not only the business object but also the tree node itself
		// against the searched object.
		return Utils.equals(node, businessObject)
			|| Utils.equals(treeModel.getBusinessObject(node), businessObject);
	}

	/**
	 * Creates a path to the <code>targetNode</code> in the {@link TLTreeModel}. The path consists
	 * of the labels of the nodes from the root (exclusive) of the tree down to the node itself
	 * (inclusive).
	 */
	public static Maybe<List<String>> createTreeLabelPath(Object targetNode, TLTreeModel treeModel,
			LabelProvider labelProvider) {
		List<?> nodePath = createPathToNode(targetNode, treeModel);
		return labelObjects(nodePath, labelProvider, treeModel);
	}

	/**
	 * Checks that the label is unique among siblings.
	 */
	private static Maybe<List<String>> labelObjects(List<?> nodePath, LabelProvider labelProvider, TLTreeModel tree) {
		List<String> labels = new ArrayList<>(nodePath.size());

		for (Object node : nodePath) {
			String nodeLabel = labelProvider.getLabel(node);

			if (isValidNodeLabel(labelProvider, tree, node, nodeLabel)) {
				labels.add(nodeLabel);
			} else {
				return Maybe.none();
			}
		}

		return Maybe.some(labels);
	}

	private static boolean isValidNodeLabel(LabelProvider labelProvider, TLTreeModel tree, Object node, String label) {
		return !StringServices.isEmpty(label) && isUniqueNodeLabel(labelProvider, tree, node, label);
	}

	private static boolean isUniqueNodeLabel(LabelProvider labelProvider, TLTreeModel tree, Object node, String label) {
		Object parent = tree.getParent(node);

		if (parent != null) {
			for (Object child : tree.getChildren(parent)) {
				if (child == node) {
					continue;
				}

				if (label.equals(labelProvider.getLabel(child))) {
					return false;
				}
			}
		}

		return true;
	}

	private static List<?> createPathToNode(Object targetNode, TLTreeModel treeModel) {
		List<?> nodePath = treeModel.createPathToRoot(targetNode);
		Collections.reverse(nodePath); // Is: Root last; We want: Root first
		nodePath.remove(0); // Remove root
		return nodePath;
	}

	/**
	 * Returns a new {@link List} containing the labels for the objects provided by the given
	 * {@link LabelProvider}.
	 */
	public static Maybe<List<String>> labelObjects(Collection<?> objectsToLabel, LabelProvider labelProvider) {
		List<String> labels = new ArrayList<>(objectsToLabel.size());
		for (Object object : objectsToLabel) {
			String label = labelProvider.getLabel(object);
			if (label == null) {
				return Maybe.none();
			}
			labels.add(label);
		}
		return Maybe.some(labels);
	}

	/**
	 * Creates a path of {@link ModelName} to the {@link TLTreeNode}. The path consists of the nodes
	 * from the root (exclusive) of the tree down to the <code>targetNode</code> itself (inclusive).
	 */
	public static Maybe<ValuePath> createTreeValuePath(TLTreeNode targetNode, TreeData treeData) {
		List<TLTreeNode> nodePath = createPathToNode(targetNode);
		List<Object> businessObjectPath = getBusinessObjects(nodePath, treeData.getTreeModel());
		Maybe<List<ModelName>> pathRef = ModelResolver.buildModelNamesIfAvailable(treeData, businessObjectPath);
		if (!pathRef.hasValue()) {
			return Maybe.none();
		}
		return Maybe.some(ReferenceInstantiator.valuePath(pathRef.get()));
	}

	/**
	 * Creates a path to the {@link TLTreeNode}. The path consists of the {@link TLTreeNode}s, from
	 * the root (exclusive) of the tree down to the node itself (inclusive).
	 */
	private static List<TLTreeNode> createPathToNode(TLTreeNode targetNode) {
		if (isRoot(targetNode)) {
			return new ArrayList<>();
		} else {
			List<TLTreeNode> parents = createPathToNode(targetNode.getParent());
			parents.add(targetNode);
			return parents;
		}
	}

	private static boolean isRoot(TLTreeNode targetNode) {
		return targetNode.getParent() == null;
	}

	private static List<Object> getBusinessObjects(List<?> nodePath, TLTreeModel treeModel) {
		List<Object> businessObjects = CollectionUtil.createList();
		for (Object treeNode : nodePath) {
			businessObjects.add(treeModel.getBusinessObject(treeNode));
		}
		return businessObjects;
	}

	/**
	 * The top-level component in the current layout.
	 */
	public static MainLayout getMainLayout() {
		return DefaultDisplayContext.getDisplayContext().getLayoutContext().getMainLayout();
	}

	/**
	 * If the given {@link LayoutComponent} is part of a window, the responsible
	 * {@link WindowComponent} is returned. Otherwise the {@link MainLayout}.
	 */
	public static LayoutComponent getWindowElseMainLayout(LayoutComponent component) {
		WindowComponent window = component.getEnclosingWindow();
		if (window != null) {
			return window;
		}
		return component.getMainLayout();
	}

	/**
	 * Getter for the currently active dialog.
	 * 
	 * @return <code>null</code>, if no dialog is open.
	 * 
	 * @see WindowScope#getActiveDialog()
	 */
	public static DialogWindowControl getActiveDialog(MainLayout mainLayout) {
		return mainLayout.getEnclosingFrameScope().getWindowScope().getActiveDialog();
	}

	/**
	 * Getter for the open dialogs.
	 * 
	 * @return <code>null</code>, if no dialog is open.
	 * 
	 * @see WindowScope#getDialogs()
	 */
	public static List<DialogWindowControl> getDialogs(MainLayout mainLayout) {
		return mainLayout.getEnclosingFrameScope().getWindowScope().getDialogs();
	}

	/**
	 * Returns the {@link ResourceProvider} used to render the given {@link TreeData}.
	 */
	public static ResourceProvider getResourceProvider(TreeData treeData) {
		ResourceProvider resourceProvider = treeData.getTreeRenderer().getTreeContentRenderer().getResourceProvider();
		if (resourceProvider != null) {
			return resourceProvider;
		}
		return MetaResourceProvider.INSTANCE;
	}

	/**
	 * Returns the {@link LayoutComponent} for the given {@link WindowScope}.
	 * 
	 * @param windowScope
	 *        The window to get component for.
	 */
	public static LayoutComponent getComponent(WindowScope windowScope) {
		BrowserWindowControl control = (BrowserWindowControl) windowScope;
		if (!control.isAttached()) {
			/* This happens during restoring the homepage. The user currently logs in, and the
			 * stored homepage may use an action calling this method (e.g. FuzzyComponentNaming). In
			 * this case the control is not yet rendered. */
			return getMainLayout();
		}
		return ((LayoutComponentScope) windowScope.getTopLevelFrameScope()).getComponent();
	}

}
