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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
		 * 
		 * @param parent
		 *        Either a {@link TLModel} or a {@link ModuleContainer}.
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

		@Override
		public String toString() {
			return "ModuleContainer: " + _name;
		}

		@Override
		public int hashCode() {
			return Objects.hash(_model, _name, _parent);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ModuleContainer other = (ModuleContainer) obj;
			return Objects.equals(_model, other._model) && Objects.equals(_name, other._name)
					&& Objects.equals(_parent, other._parent);
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
				TLModule module = (TLModule) part;
				TLModel model = module.getModel();

				String nameToGroupIn = getNameToGroupIn(module);
				if (nameToGroupIn == getModelDisplayGroup()) {
					// Module is not contained in any group.
					return Collections.singleton(model);
				}
				int groupNameSep = nameToGroupIn.lastIndexOf(GROUP_NAME_DELIMITER);
				if (groupNameSep < 0) {
					// No group separator => Top level group.
					return Collections.singleton(new ModuleContainer(model, model, nameToGroupIn));
				}

				// sort groups lexicographically
				List<String> sortedModuleNames = model.getModules()
					.stream()
					.map(this::getNameToGroupIn)
					.sorted()
					.collect(Collectors.toList());

				int idx = Collections.binarySearch(sortedModuleNames, nameToGroupIn);
				assert idx >= 0;

				List<String> groups = new ArrayList<>();
				groups.add(nameToGroupIn);

				int moduleIdx = idx;
				while (true) {
					moduleIdx--;
					if (moduleIdx < 0) {
						break;
					}
					String group = CollectionUtil.getLast(groups);
					String potentialParentGroup = sortedModuleNames.get(moduleIdx);
					if (isSubgroupOrEqual(group, potentialParentGroup, GROUP_NAME_DELIMITER)) {
						continue;
					}
					int commonSubstringLength = commonSubGroupLength(group, potentialParentGroup);
					if (commonSubstringLength == 0) {
						groups.add(potentialParentGroup);
						break;
					}
					groups.add(potentialParentGroup.substring(0, commonSubstringLength));
				}
				
				moduleIdx = idx;
				int groupIdx = 0;
				while (true) {
					moduleIdx++;
					if (moduleIdx == sortedModuleNames.size()) {
						break;
					}
					String group = groups.get(groupIdx);
					String potentialParentGroup = sortedModuleNames.get(moduleIdx);
					if (isSubgroupOrEqual(group, potentialParentGroup, GROUP_NAME_DELIMITER)) {
						continue;
					}
					int commonSubstringLength = commonSubGroupLength(group, potentialParentGroup);
					if (commonSubstringLength == 0) {
						break;
					}
					String otherGroupName = group.substring(0, commonSubstringLength);
					groupIdx++;
					if (groupIdx == groups.size()) {
						groups.add(otherGroupName);
					} else {
						String nextGroup = groups.get(groupIdx);
						if (commonSubstringLength == nextGroup.length()) {
							assert otherGroupName.equals(nextGroup);
						} else if (commonSubstringLength < nextGroup.length()) {
							assert nextGroup.startsWith(otherGroupName);
							moduleIdx--;
						} else {
							assert otherGroupName.startsWith(nextGroup);
							groups.add(groupIdx, otherGroupName);
						}
					}
				}

				Object result = model;
				for (int i = groups.size() - 1; i >= 0; i--) {
					result = new ModuleContainer(result, model, groups.get(i));
				}

				return Collections.singleton(result);
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
		Map<TLModule, String> nameToGroupInByModule = getNameToGroupInByModule(parent, model);

		return getChildrenModuleNamesInternal(getDisplayGroup(parent), nameToGroupInByModule);
	}

	private Set<String> getChildrenModuleNamesInternal(String parentName, Map<TLModule, String> nameToGroupInByModule) {
		Set<String> names = new HashSet<>();

		for (Entry<TLModule, String> entry : nameToGroupInByModule.entrySet()) {
			if (entry.getValue().equals(parentName)) {
				names.add(entry.getKey().getName());
			} else {
				names.add(getClosestCommonGroup(parentName, entry.getValue(), nameToGroupInByModule.values()));
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
		if (group1.equals(group2)) {
			return group1;
		}
		if (isSubgroup(group2, group1, GROUP_NAME_DELIMITER)) {
			return group2;
		} else if (isSubgroup(group1, group2, GROUP_NAME_DELIMITER)) {
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

	/**
	 * @param parent
	 *        Either a {@link TLModel}, {@link TLModule} or {@link ModuleContainer}.
	 */
	private Map<TLModule, String> getNameToGroupInByModule(Object parent, TLModel model) {
		Map<TLModule, String> mapping = new HashMap<>();
		String parentGroupName = getDisplayGroup(parent);

		for (TLModule module : model.getModules()) {
			String nameToGroupIn = getNameToGroupIn(module);

			if (isSubgroupOrEqual(parentGroupName, nameToGroupIn, GROUP_NAME_DELIMITER)) {
				mapping.put(module, nameToGroupIn);
			}
		}

		return mapping;
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
		return subgroup.startsWith(group) && (group.isEmpty() || subgroup.charAt(group.length()) == delimiter);
	}

	private boolean isSubgroupOrEqual(String group, String subgroup, char delimiter) {
		if (group.equals(subgroup)) {
			return true;
		}
		return subgroup.startsWith(group) && (group.isEmpty() || subgroup.charAt(group.length()) == delimiter);
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

	private static int commonSubGroupLength(String s1, String s2) {
		int index = 0;

		int size = Math.min(s1.length(), s2.length());
		for (int i = 0; i < size; i++) {
			if (s1.charAt(i) == s2.charAt(i)) {
				if (s1.charAt(i) == GROUP_NAME_DELIMITER) {
					index = i;
				}
			} else {
				break;
			}
		}
		return index;

	}
}
