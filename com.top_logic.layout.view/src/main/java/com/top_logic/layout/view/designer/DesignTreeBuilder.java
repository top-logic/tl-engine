/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.layout.configedit.ConfigChildren;
import com.top_logic.layout.configedit.ConfigEditorControl;
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
 * <li>Every set plain property of type {@link ViewRef}. The view file it names is shown as a
 * {@link ViewLinkDesignTreeNode} leaf, through which the designer opens that file: the property
 * itself stays a field of the node's form, and no child container is bound to it.</li>
 * </ul>
 *
 * <p>
 * If a config has exactly one child source, its children are inlined directly into the parent node
 * (no virtual group node). Multiple child sources each get their own {@link VirtualDesignTreeNode}
 * labelled with the property.
 * </p>
 *
 * <p>
 * A tree shows the content of a single view - a file, or a view stored on a model object, see
 * {@link DesignSource}: a referenced view is never expanded into the tree of the view referring to
 * it. A referenced file that cannot be loaded becomes an
 * {@link ErrorDesignTreeNode} instead of a link, so that the problem is visible and a broken view
 * does not prevent the designer from opening.
 * </p>
 *
 * @see DesignSession
 */
public class DesignTreeBuilder {

	/**
	 * Builds the design tree starting from the given root view path.
	 *
	 * @param rootViewPath
	 *        Full path to the root view file (e.g. {@code /WEB-INF/views/app.view.xml}).
	 * @return The root {@link DesignTreeNode}.
	 */
	public DesignTreeNode build(String rootViewPath) throws ConfigurationException {
		return build(new FileDesignSource(rootViewPath));
	}

	/**
	 * Builds the design tree of the view the given source {@link DesignSource#load() loads}.
	 *
	 * @param source
	 *        Where the view is stored.
	 * @return The root {@link DesignTreeNode}.
	 */
	public DesignTreeNode build(DesignSource source) throws ConfigurationException {
		return build(source.load(), source);
	}

	/**
	 * Builds the design tree for an already loaded configuration.
	 *
	 * @param rootConfig
	 *        The configuration forming the root of the tree.
	 * @param source
	 *        Where the view the configuration is part of is stored.
	 * @return The root {@link DesignTreeNode}.
	 */
	public DesignTreeNode build(ConfigurationItem rootConfig, DesignSource source) throws ConfigurationException {
		return buildNode(rootConfig, source);
	}

	private DesignTreeNode buildNode(ConfigurationItem config, DesignSource source) throws ConfigurationException {
		DesignTreeNode node = new ConfigDesignTreeNode(config, source);

		List<PropertyDescriptor> sources = new ArrayList<>();
		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			if (isTreeProperty(property) || isViewReference(config, property)) {
				sources.add(property);
			}
		}

		boolean inline = sources.size() == 1;

		for (PropertyDescriptor property : sources) {
			DesignTreeNode target = inline ? node : createGroupNode(node, property, source);

			if (isViewReference(config, property)) {
				addReferencedView(target, (ViewRef) config.value(property), source);
			} else {
				addElements(target, config, property, source);
			}
		}

		return node;
	}

	/**
	 * Adds the elements of the given tree property as children of the given target node.
	 */
	private void addElements(DesignTreeNode target, ConfigurationItem config, PropertyDescriptor property,
			DesignSource source) throws ConfigurationException {
		// Bind the target node to the property its children are stored in, so that the structural
		// edit commands can write through to the configuration. For an inlined property this is the
		// config node itself; otherwise it is the group node.
		target.setChildContainer(ConfigChildren.create(config, property));

		if (property.kind() == PropertyKind.LIST) {
			List<?> children = (List<?>) config.value(property);
			if (children != null) {
				for (Object childConfig : children) {
					addChild(target, (ConfigurationItem) childConfig, source);
				}
			}
		} else if (property.kind() == PropertyKind.ITEM) {
			Object childConfig = config.value(property);
			if (childConfig instanceof ConfigurationItem childItem) {
				addChild(target, childItem, source);
			}
		}
	}

	/**
	 * Adds the view the given reference names as child of the given target node: a
	 * {@link ViewLinkDesignTreeNode} if the view can be loaded, an {@link ErrorDesignTreeNode}
	 * otherwise.
	 */
	private void addReferencedView(DesignTreeNode target, ViewRef ref, DesignSource source) {
		String refFile = ref.fullPath();
		try {
			ViewLoader.getOrLoadConfig(refFile);
		} catch (Exception ex) {
			// A broken referenced view must not abort building the tree: show an error node instead
			// (keeping the referencing node), so the designer still opens.
			Logger.warn("Adding error node for invalid referenced view '" + refFile
				+ "' while building the design tree.", ex, DesignTreeBuilder.class);
			attach(target, new ErrorDesignTreeNode(source, ref.getPath(), ex.getMessage()));
			return;
		}
		attach(target, new ViewLinkDesignTreeNode(source, ref));
	}

	private DesignTreeNode createGroupNode(DesignTreeNode parent, PropertyDescriptor property, DesignSource source) {
		DesignTreeNode group = new VirtualDesignTreeNode(property, source);
		attach(parent, group);
		return group;
	}

	/**
	 * Whether the given property is annotated with {@link TreeProperty} and should be traversed.
	 *
	 * @see ConfigEditorControl#isTreeProperty(PropertyDescriptor)
	 */
	private static boolean isTreeProperty(PropertyDescriptor property) {
		return ConfigEditorControl.isTreeProperty(property);
	}

	/**
	 * Whether the given property names a view file in the given configuration.
	 */
	private static boolean isViewReference(ConfigurationItem config, PropertyDescriptor property) {
		return property.kind() == PropertyKind.PLAIN && property.getType() == ViewRef.class
			&& config.value(property) != null;
	}

	private void addChild(DesignTreeNode parent, ConfigurationItem childConfig,
			DesignSource source) throws ConfigurationException {
		attach(parent, buildNode(childConfig, source));
	}

	/**
	 * Appends the given node to the children of the given parent.
	 */
	static void attach(DesignTreeNode parent, DesignTreeNode child) {
		child.setParent(parent);
		parent.getChildren().add(child);
	}
}
