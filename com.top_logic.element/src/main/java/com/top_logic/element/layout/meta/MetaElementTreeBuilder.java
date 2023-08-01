/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved
 */
package com.top_logic.element.layout.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLModuleDisplayName;

/**
 * Builds the meta elements tree.
 * 
 * <p>
 * For each module, starting from the root node, a subtree with the following structure is created.
 * For this purpose, the name of the module is determined. If the module is annotated with
 * {@link TLModuleDisplayName}, this value, otherwise the technical name of the module is used.
 * </p>
 * 
 * <p>
 * Then this name is separated into its parts using dot as delimiter character. For each of these
 * parts, a new node is added to the tree, which is a child of the node of the preceding part, or a
 * child of the root node if the part is the first.
 * </p>
 * 
 * <p>
 * For a module with the name <code>tl.core.FooBar</code> a subtree of the following structure
 * exists in the tree: <code>ROOT -> tl -> tl.core -> tl.core.FooBar</code>.
 * </p>
 * 
 * <p>
 * The business object of the nodes between the root node and the module itself are of the type
 * {@link ModuleContainer}.
 * </p>
 * 
 * @see TLModuleDisplayName
 * @see ModuleContainer
 * @see MetaElementTreeResourceProvider
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class MetaElementTreeBuilder extends DefaultTreeTableBuilder {

	private static final class InModule implements Filter<TLType> {
		private final TLModule _module;

		public InModule(TLModule module) {
			_module = module;
		}

		@Override
		public boolean accept(TLType other) {
			return other.getModule() == _module;
		}
	}

	/**
	 * The business object of the nodes between the root node and the {@link TLModule} node are of
	 * this type.
	 *
	 * @see MetaElementTreeBuilder
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public static final class ModuleContainer {

		private final TLModel _model;

		private final String _name;

		/**
		 * Creates a {@link ModuleContainer}.
		 */
		public ModuleContainer(TLModel model, String name) {
			_name = name;
			_model = model;
		}

		/**
		 * Returns the technical name.
		 */
		public String getName() {
			return _name;
		}

		/**
		 * Returns the enclosing {@link TLModel} of which this module container is part.
		 */
		public TLModel getModel() {
			return _model;
		}
	}

	@Override
	public DefaultTreeTableNode createNode(AbstractMutableTLTreeModel<DefaultTreeTableNode> model,
			DefaultTreeTableNode parent, Object userObject) {
		DefaultTreeTableNode newNode = super.createNode(model, parent, userObject);
		if (userObject instanceof ModuleContainer) {
			AbstractTreeTableModel.markSynthetic(newNode);
		}
		return newNode;
	}

	@Override
	public List<DefaultTreeTableNode> createChildList(DefaultTreeTableNode node) {
		Object businessObject = node.getBusinessObject();

		if (businessObject instanceof TLModelPart) {
			switch (((TLModelPart) businessObject).getModelKind()) {
				case MODEL: {
					return createNodes(node, getModuleParts((TLModel) businessObject, StringServices.EMPTY_STRING));
				}
				case MODULE: {
					return createNodes(node, getTypes((TLModule) businessObject));
				}
				case CLASS: {
					TLClass clazz = (TLClass) businessObject;

					return createNodes(node, FilterUtil.filterList(new InModule(clazz.getModule()), clazz.getSpecializations()));
				}
				default: {
					return Collections.emptyList();
				}
			}
		} else if (businessObject instanceof ModuleContainer) {
			ModuleContainer moduleContainer = (ModuleContainer) businessObject;

			return createNodes(node, getModuleParts(moduleContainer.getModel(), moduleContainer.getName()));
		} else {
			return Collections.emptyList();
		}
	}

	private List<Object> getModuleParts(TLModel model, String prefix) {
		List<Object> filteredModules = new ArrayList<>();
		Set<String> moduleContainerNames = new HashSet<>();

		for (TLModule module : model.getModules()) {
			String name = getName(module);

			if (hasPartWithPrefix(name, prefix)) {
				int partAfterPrefixIndex = name.indexOf(".", prefix.length() + 1);

				if (partAfterPrefixIndex >= 0) {
					moduleContainerNames.add(name.substring(0, partAfterPrefixIndex));
				} else {
					filteredModules.add(module);
				}
			}
		}

		for (String moduleContainerName : moduleContainerNames) {
			filteredModules.add(new ModuleContainer(model, moduleContainerName));
		}

		return filteredModules;
	}

	private boolean hasPartWithPrefix(String name, String prefix) {
		if (name.startsWith(prefix)) {
			return "".equals(prefix) || (name.length() > prefix.length() && name.charAt(prefix.length()) == '.');
		} else {
			return false;
		}
	}

	private String getName(TLModule module) {
		TLModuleDisplayName displayName = module.getAnnotation(TLModuleDisplayName.class);

		if (displayName != null) {
			return displayName.getValue();
		} else {
			return module.getName();
		}
	}

	private List<TLType> getTypes(TLModule module) {
		List<TLType> types = FilterUtil.filterList(TopLevelClasses.INSTANCE, module.getTypes());
		types.addAll(module.getEnumerations());
		return types;
	}

	private List<DefaultTreeTableNode> createNodes(DefaultTreeTableNode parent, Collection<?> children) {
		List<DefaultTreeTableNode> result = new ArrayList<>();

		for (Object child : children) {
			result.add(createNode(parent.getModel(), parent, child));
		}

		return result;
	}
}
