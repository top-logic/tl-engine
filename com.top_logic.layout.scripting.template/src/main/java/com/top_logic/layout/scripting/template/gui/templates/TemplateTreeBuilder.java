/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.io.IOException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateFolder;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateLocation;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateResource;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.layout.tree.component.TreeNodeBasedTreeModelBuilder;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.MutableTLTreeNode;
import com.top_logic.layout.tree.model.StructureTreeModel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link TreeModelBuilder} that collects all script templates from the web application folder.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateTreeBuilder extends TreeNodeBasedTreeModelBuilder<DefaultMutableTLTreeNode> implements
		ConfiguredInstance<TemplateTreeBuilder.Config> {

	private final Config _config;

	/**
	 * Configuration options for {@link TemplateTreeBuilder}.
	 */
	public interface Config extends PolymorphicConfiguration<TreeModelBuilder<?>> {

		/**
		 * The {@link FileManager} path of the root directory of templates to display.
		 */
		@Mandatory
		String getTemplatePath();

	}

	private String _templateRootResourcePath;

	/**
	 * Creates a {@link TemplateTreeBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TemplateTreeBuilder(InstantiationContext context, Config config) {
		_config = config;
		_templateRootResourcePath = getTemplateRootResourcePath(config);
	}

	private String getTemplateRootResourcePath(Config config) {
		String resourcePath = config.getTemplatePath();

		if (!resourcePath.isEmpty() && resourcePath.charAt(resourcePath.length() - 1) != '/') {
			return resourcePath + "/";
		}

		return resourcePath;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		StructureTreeModel model = new StructureTreeModel(null);

		try {
			buildTemplateTree(model.getRoot());
		} catch (IOException ex) {
			Logger.error("Failed to build template tree.", ex, TemplateTreeBuilder.class);
		}

		return model.getRoot();
	}

	private void buildTemplateTree(MutableTLTreeNode<?> parent) throws IOException {
		if (FileManager.getInstance().isDirectory(_templateRootResourcePath)) {
			TemplateFolder folder = new TemplateFolder("");
			parent.setBusinessObject(folder);
			folder.initLocation(parent);
			scanFolder(parent, _templateRootResourcePath);
		}
	}

	private void scanFolder(MutableTLTreeNode<?> parent, String resourcePath) throws IOException {
		FileManager fileManager = FileManager.getInstance();
		for (String path : fileManager.getResourcePaths(resourcePath)) {
			String contentResource = path.substring(_templateRootResourcePath.length());
			String fileName = FileUtilities.getFilenameOfResource(contentResource);
			if (fileName.startsWith(".")) {
				continue;
			}
			MutableTLTreeNode<?> contentNode = findNode(parent, fileName);
			if (fileManager.isDirectory(path)) {
				if (contentNode != null && !isFolder(contentNode)) {
					parent.removeChild(parent.getIndex(contentNode));
					contentNode = null;
				}
				if (contentNode == null) {
					TemplateFolder folder = new TemplateFolder(contentResource);
					contentNode = parent.createChild(folder);
					folder.initLocation(contentNode);
				}
				scanFolder(contentNode, path);
				if (contentNode.getChildCount() == 0) {
					// Do not show empty directories.
					parent.removeChild(parent.getIndex(contentNode));
				}
			} else {
				if (!fileName.endsWith(".xml")) {
					continue;
				}
				if (contentNode != null && !isTemplate(contentNode)) {
					parent.removeChild(parent.getIndex(contentNode));
					contentNode = null;
				}
				TemplateResource template =
					new TemplateResource(contentResource);
				if (contentNode == null) {
					contentNode = parent.createChild(template);
				} else {
					contentNode.setBusinessObject(template);
				}
				template.initLocation(contentNode);
			}
		}
	}

	/**
	 * Resource path of the template root folder.
	 */
	public String getTemplateRootResourcePath() {
		return _templateRootResourcePath;
	}

	private boolean isTemplate(MutableTLTreeNode<?> node) {
		return IsTemplateSelected.isTemplate(node);
	}

	private boolean isFolder(MutableTLTreeNode<?> node) {
		return !isTemplate(node);
	}

	private MutableTLTreeNode<?> findNode(MutableTLTreeNode<?> parent, String name) {
		for (MutableTLTreeNode<?> node : parent.getChildren()) {
			if (((TemplateLocation) node.getBusinessObject()).getName().equals(name)) {
				return node;
			}
		}
		return null;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof DefaultMutableTLTreeNode;
	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, DefaultMutableTLTreeNode node) {
		return node.getBusinessObject();
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		return node instanceof DefaultMutableTLTreeNode;
	}

}
