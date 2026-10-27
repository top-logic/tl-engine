/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.layout.configedit.ConfigChildren;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.ViewRef;

/**
 * Builds a {@link DesignTreeNode} tree from a root view's
 * {@link com.top_logic.layout.view.UIElement.Config} hierarchy.
 *
 * <p>
 * The children of a configuration node come from its child sources:
 * </p>
 * <ul>
 * <li>Every {@link TreeProperty}-annotated LIST and ITEM property. Its elements are the child nodes,
 * and the property is the {@link DesignTreeNode#getChildContainer() child container} the structural
 * edit commands work on. This allows configuration authors to explicitly control which structural
 * properties appear as tree nodes vs. editable form fields.</li>
 * <li>Every set plain property of type {@link ViewRef}. The view file it names is loaded through
 * {@link ViewLoader#getOrLoadConfig(String)} and shown as a read-only child: the property itself
 * stays a field of the node's form, and no child container is bound to it.</li>
 * </ul>
 *
 * <p>
 * If a config has exactly one child source, its children are inlined directly into the parent node
 * (no virtual group node). Multiple child sources each get their own {@link VirtualDesignTreeNode}
 * labelled with the property.
 * </p>
 *
 * <p>
 * Each view file is expanded only once per tree, at its first occurrence in build order (the root
 * file counts as expanded). Every further reference to an expanded file becomes a
 * {@link ViewLinkDesignTreeNode}, which also terminates reference cycles. A referenced file that
 * cannot be loaded becomes an {@link ErrorDesignTreeNode}, so that a broken view does not prevent
 * the designer from opening.
 * </p>
 */
public class DesignTreeBuilder {

	/**
	 * The full paths of the view files expanded in the tree being built.
	 */
	private final Set<String> _expandedFiles = new HashSet<>();

	/**
	 * Records the view files shown in the given tree as expanded, so that a subtree built afterwards
	 * for that tree refers to them through {@link ViewLinkDesignTreeNode}s instead of expanding them
	 * a second time.
	 *
	 * @param tree
	 *        The tree the subtree built afterwards is inserted into.
	 * @return This instance for call chaining.
	 */
	public DesignTreeBuilder markExpanded(DesignTreeNode tree) {
		if (tree != null) {
			if (tree.getSourceFile() != null) {
				_expandedFiles.add(tree.getSourceFile());
			}
			for (DesignTreeNode child : tree.getChildren()) {
				markExpanded(child);
			}
		}
		return this;
	}

	/**
	 * Builds the design tree starting from the given root view path.
	 *
	 * @param rootViewPath
	 *        Full path to the root view file (e.g. {@code /WEB-INF/views/app.view.xml}).
	 * @return The root {@link DesignTreeNode}.
	 */
	public DesignTreeNode build(String rootViewPath) throws ConfigurationException {
		ViewElement.Config rootConfig = ViewLoader.getOrLoadConfig(rootViewPath);
		return build(rootConfig, rootViewPath);
	}

	/**
	 * Builds the design tree for an already loaded configuration.
	 *
	 * @param rootConfig
	 *        The configuration forming the root of the tree.
	 * @param sourceFile
	 *        The {@code .view.xml} file the configuration originates from.
	 * @return The root {@link DesignTreeNode}.
	 */
	public DesignTreeNode build(ConfigurationItem rootConfig, String sourceFile) throws ConfigurationException {
		if (sourceFile != null) {
			_expandedFiles.add(sourceFile);
		}
		return buildNode(rootConfig, sourceFile);
	}

	private DesignTreeNode buildNode(ConfigurationItem config, String sourceFile) throws ConfigurationException {
		DesignTreeNode node = new ConfigDesignTreeNode(config, sourceFile);

		List<PropertyDescriptor> sources = new ArrayList<>();
		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			if (isTreeProperty(property) || isViewReference(config, property)) {
				sources.add(property);
			}
		}

		boolean inline = sources.size() == 1;

		for (PropertyDescriptor property : sources) {
			DesignTreeNode target = inline ? node : createGroupNode(node, property, sourceFile);

			if (isViewReference(config, property)) {
				addReferencedView(target, (ViewRef) config.value(property), sourceFile);
			} else {
				addElements(target, config, property, sourceFile);
			}
		}

		return node;
	}

	/**
	 * Adds the elements of the given tree property as children of the given target node.
	 */
	private void addElements(DesignTreeNode target, ConfigurationItem config, PropertyDescriptor property,
			String sourceFile) throws ConfigurationException {
		// Bind the target node to the property its children are stored in, so that the structural
		// edit commands can write through to the configuration. For an inlined property this is the
		// config node itself; otherwise it is the group node.
		target.setChildContainer(ConfigChildren.create(config, property));

		if (property.kind() == PropertyKind.LIST) {
			List<?> children = (List<?>) config.value(property);
			if (children != null) {
				for (Object childConfig : children) {
					addChild(target, (ConfigurationItem) childConfig, sourceFile);
				}
			}
		} else if (property.kind() == PropertyKind.ITEM) {
			Object childConfig = config.value(property);
			if (childConfig instanceof ConfigurationItem childItem) {
				addChild(target, childItem, sourceFile);
			}
		}
	}

	/**
	 * Adds the view the given reference names as child of the given target node: its expanded
	 * subtree at its first occurrence, a {@link ViewLinkDesignTreeNode} otherwise.
	 */
	private void addReferencedView(DesignTreeNode target, ViewRef ref, String sourceFile)
			throws ConfigurationException {
		String refFile = ref.fullPath();
		if (!_expandedFiles.add(refFile)) {
			attach(target, new ViewLinkDesignTreeNode(sourceFile, ref));
			return;
		}

		ViewElement.Config refViewConfig;
		try {
			refViewConfig = ViewLoader.getOrLoadConfig(refFile);
		} catch (Exception ex) {
			// A broken referenced view must not abort building the whole design tree: show an error
			// node instead (keeping the referencing node), so the designer still opens. The file is
			// not expanded, so every reference to it reports the problem.
			_expandedFiles.remove(refFile);
			Logger.warn("Adding error node for invalid referenced view '" + refFile
				+ "' while building the design tree.", ex, DesignTreeBuilder.class);
			attach(target, new ErrorDesignTreeNode(sourceFile, ref.getPath(), ex.getMessage()));
			return;
		}
		addChild(target, refViewConfig, refFile);
	}

	private DesignTreeNode createGroupNode(DesignTreeNode parent, PropertyDescriptor property, String sourceFile) {
		DesignTreeNode group = new VirtualDesignTreeNode(property, sourceFile);
		attach(parent, group);
		return group;
	}

	/**
	 * Whether the given property is annotated with {@link TreeProperty} and should be traversed.
	 */
	private static boolean isTreeProperty(PropertyDescriptor property) {
		TreeProperty annotation = property.getAnnotation(TreeProperty.class);
		return annotation != null && annotation.value();
	}

	/**
	 * Whether the given property names a view file in the given configuration.
	 */
	private static boolean isViewReference(ConfigurationItem config, PropertyDescriptor property) {
		return property.kind() == PropertyKind.PLAIN && property.getType() == ViewRef.class
			&& config.value(property) != null;
	}

	private void addChild(DesignTreeNode parent, ConfigurationItem childConfig,
			String sourceFile) throws ConfigurationException {
		attach(parent, buildNode(childConfig, sourceFile));
	}

	/**
	 * Appends the given node to the children of the given parent.
	 */
	static void attach(DesignTreeNode parent, DesignTreeNode child) {
		child.setParent(parent);
		parent.getChildren().add(child);
	}
}
