/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.controlprovider.MetaResourceControlProvider;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;

/**
 * A {@link UIElement} that builds a {@link ReactTreeControl} from a {@link DesignTreeNode}
 * hierarchy.
 *
 * <p>
 * Unlike {@link com.top_logic.layout.view.element.TreeElement}, which uses TL-Script expressions to
 * compute children, this element reads the tree structure directly from the {@link DesignTreeNode}
 * model. The input channel provides the root {@link DesignTreeNode}, and the selection channel
 * receives the currently selected node.
 * </p>
 */
public class DesignerTreeElement implements UIElement {

	/**
	 * Configuration for {@link DesignerTreeElement}.
	 */
	@TagName("design-tree")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(DesignerTreeElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Reference to the channel providing the root {@link DesignTreeNode}.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Optional reference to a channel that receives the selected {@link DesignTreeNode}.
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final Config _config;

	/**
	 * Creates a new {@link DesignerTreeElement} from configuration.
	 */
	@CalledByReflection
	public DesignerTreeElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// 1. Resolve the input channel providing the root DesignTreeNode.
		ViewChannel inputChannel = context.resolveChannel(_config.getInput());
		DesignTreeNode rootNode = (DesignTreeNode) inputChannel.get();

		// 2. Build the tree model.
		TreeBuilder<DefaultTreeUINode> builder = createTreeBuilder();
		DefaultTreeUINodeModel treeModel = new DefaultTreeUINodeModel(builder, rootNode);

		// 3. Create selection model.
		DefaultSingleSelectionModel<Object> selectionModel =
			new DefaultSingleSelectionModel<>(SelectionModelOwner.NO_OWNER);

		// 4. Create the ReactTreeControl using MetaResourceControlProvider for node labels.
		ReactTreeControl treeControl =
			new ReactTreeControl(context, treeModel, selectionModel, MetaResourceControlProvider.INSTANCE);

		// 5. Wire selection: push selected DesignTreeNode to the selection channel.
		ChannelRef selectionRef = _config.getSelection();
		if (selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(selectionRef);
			selectionModel.addSelectionListener(new SelectionListener<>() {
				@Override
				public void notifySelectionChanged(SelectionModel<Object> model, SelectionEvent<Object> event) {
					Set<?> newSelection = event.getNewSelection();
					if (newSelection.size() == 1) {
						Object selected = newSelection.iterator().next();
						if (selected instanceof DefaultTreeUINode treeNode) {
							selectionChannel.set(treeNode.getBusinessObject());
						} else {
							selectionChannel.set(selected);
						}
					} else if (newSelection.isEmpty()) {
						selectionChannel.set(null);
					}
				}
			});
		}

		// 6. Listen on the input channel for root changes (e.g. after Revert) and rebuild tree.
		inputChannel.addListener((sender, oldValue, newValue) -> {
			if (newValue instanceof DesignTreeNode newRoot) {
				DefaultTreeUINodeModel newTreeModel = new DefaultTreeUINodeModel(builder, newRoot);
				treeControl.setTreeModel(newTreeModel);
			}
		});

		return treeControl;
	}

	private TreeBuilder<DefaultTreeUINode> createTreeBuilder() {
		return new TreeBuilder<>() {

			@Override
			public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
					DefaultTreeUINode parent, Object userObject) {
				return new DefaultTreeUINode(model, parent, userObject);
			}

			@Override
			public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
				Object businessObj = node.getBusinessObject();
				if (businessObj instanceof DesignTreeNode designNode) {
					List<DesignTreeNode> designChildren = designNode.getChildren();
					List<DefaultTreeUINode> children = new ArrayList<>(designChildren.size());
					for (DesignTreeNode child : designChildren) {
						DefaultTreeUINode childNode = createNode(node.getModel(), node, child);
						if (childNode != null) {
							children.add(childNode);
						}
					}
					return children;
				}
				return Collections.emptyList();
			}

			@Override
			public boolean isFinite() {
				return true;
			}
		};
	}
}
