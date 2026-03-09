/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.table.ReactTextCellControl;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUIBuilder;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo {@link LayoutComponent} that showcases the React tree control.
 *
 * <p>
 * Creates a {@link ReactTreeControl} with a synthetic 3-level tree (5 folders, each with 5 items,
 * each with 3 leaves) to demonstrate tree expansion, collapse, and node selection.
 * </p>
 */
public class DemoReactTreeComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoReactTreeComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private ReactContext _context;

	private ReactTreeControl _treeControl;

	/**
	 * Creates a new {@link DemoReactTreeComponent}.
	 */
	public DemoReactTreeComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void writeBody(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		if (_treeControl == null) {
			_context = ReactContext.fromDisplayContext(displayContext);
			_treeControl = createDemoTree();
		}

		out.beginTag(HTMLConstants.H2);
		out.writeText("React Tree Demo");
		out.endTag(HTMLConstants.H2);

		out.beginTag(HTMLConstants.PARAGRAPH);
		out.writeText("A tree with 3 levels: 5 folders, each with 5 items, each with 3 leaves. "
			+ "Click the expand arrow to open nodes. Click nodes to select.");
		out.endTag(HTMLConstants.PARAGRAPH);

		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, "height: 500px; border: 1px solid #ccc;");
		out.endBeginTag();
		_treeControl.write(displayContext, out);
		out.endTag(HTMLConstants.DIV);
	}

	private ReactTreeControl createDemoTree() {
		DefaultTreeUINodeModel treeModel =
			new DefaultTreeUINodeModel(new DefaultTreeUIBuilder(), "Root");
		treeModel.setRootVisible(false);

		DefaultTreeUINode root = treeModel.getRoot();

		for (int f = 1; f <= 5; f++) {
			DefaultTreeUINode folder = root.createChild("Folder " + f);
			for (int i = 1; i <= 5; i++) {
				DefaultTreeUINode item = folder.createChild("Item " + f + "." + i);
				for (int l = 1; l <= 3; l++) {
					item.createChild("Leaf " + f + "." + i + "." + l);
				}
			}
		}

		DefaultMultiSelectionModel<Object> selectionModel =
			new DefaultMultiSelectionModel<>(SelectionModelOwner.NO_OWNER);

		LabelProvider labels = MetaLabelProvider.INSTANCE;
		ReactTreeControl tree = new ReactTreeControl(_context, treeModel, selectionModel,
			(ctx, node) -> {
				Object businessObject = node;
				if (node instanceof AbstractMutableTLTreeNode) {
					businessObject = ((AbstractMutableTLTreeNode<?>) node).getBusinessObject();
				}
				return new ReactTextCellControl(ctx, labels.getLabel(businessObject));
			});
		tree.setSelectionMode("multi");
		return tree;
	}
}
