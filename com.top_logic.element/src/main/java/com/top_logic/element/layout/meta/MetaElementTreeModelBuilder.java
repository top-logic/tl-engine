/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Named;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.layout.tree.component.AbstractTreeModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLModuleDisplayGroup;

/**
 * Builds the meta elements tree model.
 * 
 * <p>
 * For each module, starting from the root node, a subtree with the following structure is created.
 * For this purpose, the name of the module is determined. If the module is annotated with
 * {@link TLModuleDisplayGroup}, this value, otherwise the technical name of the module is used.
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
 * @see TLModuleDisplayGroup
 * @see ModuleContainer
 * @see MetaElementTreeResourceProvider
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class MetaElementTreeModelBuilder extends AbstractTreeModelBuilder<Object> {

	private static final char GROUP_NAME_DELIMITER = '.';

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
	 * @see MetaElementTreeModelBuilder
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public static final class ModuleContainer implements Named {

		private final Object _parent;

		private final TLModel _model;

		private final String _name;

		/**
		 * Creates a {@link ModuleContainer}.
		 */
		public ModuleContainer(Object parent, TLModel model, String name) {
			_parent = parent;
			_model = model;
			_name = name;
		}

		/**
		 * The business object of its tree parent node.
		 */
		public Object getParent() {
			return _parent;
		}

		/**
		 * The enclosing {@link TLModel} of which this module container is a part.
		 */
		public TLModel getModel() {
			return _model;
		}

		/**
		 * The technical name.
		 */
		@Override
		public String getName() {
			return _name;
		}

		/**
		 * The containers label.
		 */
		public String getLabel() {
			String parentName = getDisplayGroup(_parent);

			if (parentName.isEmpty()) {
				return _name;
			} else {
				return _name.substring(parentName.length() + 1);
			}
		}

	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, Object node) {
		if (node instanceof TLModelPart) {
			return ((TLModelPart) node).getModel();
		} else if (node instanceof ModuleContainer) {
			return ((ModuleContainer) node).getModel();
		} else {
			return null;
		}
	}

	@Override
	public Collection<? extends Object> getParents(LayoutComponent contextComponent, Object node) {
		if (node instanceof TLModelPart) {
			return getModelPartParents((TLModelPart) node);
		} else if (node instanceof ModuleContainer) {
			return Collections.singleton(((ModuleContainer) node).getParent());
		} else {
			return Collections.emptyList();
		}
	}

	private Collection<? extends Object> getModelPartParents(TLModelPart part) {
		switch (kind(part)) {
			case CLASS: {
				TLClass type = (TLClass) part;
				List<TLClass> generalizationsInSameModule =
					FilterUtil.filterList(new InModule(type.getModule()), type.getGeneralizations());
				if (generalizationsInSameModule.isEmpty()) {
					return Collections.singletonList(type.getModule());
				} else {
					return generalizationsInSameModule;
				}
			}
			case MODULE: {
				return Collections.singleton(((TLModule) part).getModel());
			}
			case ENUMERATION:
				return Collections.singleton(((TLEnumeration) part).getModule());
			default: {
				return Collections.emptyList();
			}
		}
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		if (node instanceof TLModelPart) {
			return isSupportedModelPartKind(kind(node));
		} else if (node instanceof ModuleContainer) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isSupportedModelPartKind(ModelKind kind) {
		switch (kind) {
			case MODEL:
			case MODULE:
			case CLASS:
			case ENUMERATION:
				return true;
			default:
				return false;
		}
	}

	private ModelKind kind(Object node) {
		return ((TLModelPart) node).getModelKind();
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return model instanceof TLModel;
	}

	@Override
	public Iterator<? extends Object> getChildIterator(Object node) {
		if (node instanceof TLModelPart) {
			return getModelPartChildIterator((TLModelPart) node);
		} else if (node instanceof ModuleContainer) {
			return getModuleContainerChildIterator((ModuleContainer) node);
		} else {
			return Collections.emptyIterator();
		}
	}

	private Iterator<? extends Object> getModuleContainerChildIterator(ModuleContainer moduleContainer) {
		return getChildrenModules(moduleContainer, moduleContainer.getModel()).iterator();
	}

	private Iterator<? extends Object> getModelPartChildIterator(TLModelPart part) {
		switch (kind(part)) {
			case MODEL: {
				TLModel model = (TLModel) part;

				return getChildrenModules(model, model).iterator();
			}
			case MODULE: {
				TLModule module = (TLModule) part;

				List<Object> types = getTypes(module);
				List<Object> modules = getChildrenModules(module, module.getModel());

				return CollectionUtil.concat(types, modules).iterator();
			}
			case CLASS: {
				TLClass clazz = (TLClass) part;
				Iterator<TLClass> specializations = clazz.getSpecializations().iterator();

				return FilterUtil.filterIterator(new InModule(clazz.getModule()), specializations);
			}
			default: {
				return Collections.emptyIterator();
			}
		}
	}

	private List<Object> getChildrenModules(Object parent, TLModel model) {
		List<Object> childModules = new ArrayList<>();

		for (String name : getChildrenModuleNames(parent, model)) {
			TLModule module = model.getModule(name);

			if (module != null) {
				childModules.add(module);
			} else {
				childModules.add(new ModuleContainer(parent, model, name));
			}
		}

		return childModules;
	}

	private Set<String> getChildrenModuleNames(Object parent, TLModel model) {
		Map<TLModule, String> moduleByNameToGroupIn = getModuleByNameToGroupIn(parent, model);

		return getChildrenModuleNamesInternal(getDisplayGroup(parent), moduleByNameToGroupIn);
	}

	private Set<String> getChildrenModuleNamesInternal(String parentName, Map<TLModule, String> moduleByNameToGroupIn) {
		Set<String> names = new HashSet<>();

		for (Entry<TLModule, String> entry : moduleByNameToGroupIn.entrySet()) {
			if (entry.getValue().equals(parentName)) {
				names.add(entry.getKey().getName());
			} else {
				names.add(getClosestCommonGroup(parentName, entry.getValue(), moduleByNameToGroupIn.values()));
			}
		}

		return names;
	}

	private String getClosestCommonGroup(String parentGroup, String referenceGroup, Collection<String> groups) {
		String closestCommonGroup = referenceGroup;

		for (String group : groups) {
			String commonGroup = getCommonGroup(referenceGroup, group);

			if (!parentGroup.equals(commonGroup) && commonGroup != null && commonGroup.length() < closestCommonGroup.length()) {
				closestCommonGroup = commonGroup;
			}
		}

		return closestCommonGroup;
	}

	private String getCommonGroup(String group1, String group2) {
		if (group1.startsWith(group2)) {
			return group2;
		} else if (group2.startsWith(group1)) {
			return group1;
		} else {
			int index = 0;

			for (int i = 0; i < Math.min(group2.length(), group1.length()); i++) {
				if (group2.charAt(i) == group1.charAt(i)) {
					if (group2.charAt(i) == GROUP_NAME_DELIMITER) {
						index = i;
					}
				} else {
					break;
				}
			}

			if (index != 0) {
				return group2.substring(0, index);
			} else {
				return null;
			}
		}
	}

	private Map<TLModule, String> getModuleByNameToGroupIn(Object parent, TLModel model) {
		Map<TLModule, String> moduleByNameToGroupIn = new HashMap<>();
		String parentGroupName = getDisplayGroup(parent);

		for (TLModule module : model.getModules()) {
			String nameToGroupIn = getNameToGroupIn(module);

			if (nameToGroupIn.equals(parentGroupName)) {
				moduleByNameToGroupIn.put(module, nameToGroupIn);
			} else if (isSubgroup(parentGroupName, nameToGroupIn, GROUP_NAME_DELIMITER)) {
				moduleByNameToGroupIn.put(module, nameToGroupIn);
			}
		}

		return moduleByNameToGroupIn;
	}

	private static String getDisplayGroup(Object object) {
		if (object instanceof TLModel) {
			return getModelDisplayGroup();
		} else if (object instanceof Named) {
			return ((Named) object).getName();
		} else {
			throw new UnreachableAssertion("Only TLModelPart's and module containers are grouped into each other.");
		}
	}

	private static String getModelDisplayGroup() {
		return StringServices.EMPTY_STRING;
	}

	private boolean isSubgroup(String group, String subgroup, char delimiter) {
		if (group.equals(subgroup)) {
			return false;
		} else {
			return subgroup.startsWith(group) && (group.isEmpty() || subgroup.charAt(group.length()) == delimiter);
		}
	}

	private String getNameToGroupIn(TLModule module) {
		TLModuleDisplayGroup displayName = module.getAnnotation(TLModuleDisplayGroup.class);

		if (displayName != null) {
			return displayName.getValue();
		} else {
			return getNameToGroupInInternal(module.getName());
		}
	}

	private String getNameToGroupInInternal(String name) {
		int index = name.lastIndexOf(GROUP_NAME_DELIMITER);

		if (index > -1) {
			return name.substring(0, index);
		} else {
			return getModelDisplayGroup();
		}
	}

	private List<Object> getTypes(TLModule module) {
		List<TLType> types = FilterUtil.filterList(TopLevelClasses.INSTANCE, module.getTypes());
		types.addAll(module.getEnumerations());
		return CollectionUtil.dynamicCastView(Object.class, types);
	}
}
