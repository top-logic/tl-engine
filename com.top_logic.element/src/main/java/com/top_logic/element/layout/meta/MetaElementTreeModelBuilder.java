/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved
 */
package com.top_logic.element.layout.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
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
		return getModules(moduleContainer, moduleContainer.getModel()).iterator();
	}

	private Iterator<? extends Object> getModelPartChildIterator(TLModelPart part) {
		switch (kind(part)) {
			case MODEL: {
				TLModel model = (TLModel) part;

				return getModules(model, model).iterator();
			}
			case MODULE: {
				TLModule module = (TLModule) part;

				List<Object> types = getTypes(module);
				List<Object> modules = getModules(module, module.getModel());

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

	private List<Object> getModules(Object parent, TLModel model) {
		List<Object> modules = new ArrayList<>();

		for (String name : removeExtendingElements(getModuleNames(parent, model))) {
			TLModule module = model.getModule(name);

			if (module != null) {
				modules.add(module);
			} else {
				modules.add(new ModuleContainer(parent, model, name));
			}
		}

		return modules;
	}

	private Set<String> removeExtendingElements(Set<String> strings) {
		Set<String> result = new HashSet<>();

		for (String string : strings) {
			boolean isExtending = false;

			for (String other : strings) {
				if (!other.equals(string) && string.startsWith(other)) {
					isExtending = true;
					break;
				}
			}

			if (!isExtending) {
				result.add(string);
			}
		}

		return result;
	}

	private Set<String> getModuleNames(Object parent, TLModel model) {
		Set<String> names = new LinkedHashSet<>();
		String name = getDisplayGroup(parent);

		for (TLModule module : model.getModules()) {
			String nameToGroupIn = getNameToGroupIn(module);

			if (nameToGroupIn.equals(name)) {
				names.add(module.getName());
			} else if (nameToGroupIn.startsWith(name)) {
				if (isSubgroup(name, nameToGroupIn, GROUP_NAME_DELIMITER)) {
					names.add(nameToGroupIn);
				}
			}
		}

		return names;
	}

	private String getDisplayGroup(Object object) {
		if (object instanceof TLModel) {
			return getModelDisplayGroup();
		} else if (object instanceof Named) {
			return ((Named) object).getName();
		} else {
			throw new UnreachableAssertion("Only TLModelPart's and module containers are grouped into each other.");
		}
	}

	private String getModelDisplayGroup() {
		return StringServices.EMPTY_STRING;
	}

	private boolean isSubgroup(String name1, String name2, char delimiter) {
		return name1.isEmpty() || name2.length() > name1.length() && name2.charAt(name1.length()) == delimiter;
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
