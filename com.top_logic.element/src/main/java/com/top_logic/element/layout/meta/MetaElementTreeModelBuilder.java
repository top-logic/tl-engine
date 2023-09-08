/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.layout.tree.component.AbstractTreeModelBuilder;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.util.model.ModelService;

/**
 * {@link TreeModelBuilder} creates the tree of all {@link TLClass}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MetaElementTreeModelBuilder extends AbstractTreeModelBuilder<Object> {

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
	 * Singleton {@link MetaElementTreeModelBuilder} instance.
	 */
	public static final MetaElementTreeModelBuilder INSTANCE = new MetaElementTreeModelBuilder();

	private MetaElementTreeModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		return ModelService.getApplicationModel();
	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, Object node) {
		if (node instanceof TLModelPart) {
			return ((TLModelPart) node).getModel();
		}
		return null;
	}

	@Override
	public Collection<?> getParents(LayoutComponent contextComponent, Object node) {
		switch (kind(node)) {
			case CLASS: {
				TLClass type = (TLClass) node;
				List<TLClass> generalizationsInSameModule =
					FilterUtil.filterList(new InModule(type.getModule()), type.getGeneralizations());
				if (generalizationsInSameModule.isEmpty()) {
					// Add defining modules to parents.
					return Collections.singletonList(type.getModule());
				} else {
					return generalizationsInSameModule;
				}
			}
			case MODULE: {
				return Collections.singleton(((TLModule) node).getModel());
			}
			case ENUMERATION:
				return Collections.singleton(((TLEnumeration) node).getModule());
			default: {
				return Collections.emptyList();
			}
		}
	}

	private ModelKind kind(Object node) {
		return ((TLModelPart) node).getModelKind();
	}

	static boolean hasGeneralizationsInSameModule(TLClass type) {
		TLModule module = type.getModule();
		for (TLClass superType : type.getGeneralizations()) {
			if (superType.getModule() == module) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		if (!(node instanceof TLModelPart)) {
			return false;
		}
		switch (kind(node)) {
			case MODEL:
			case MODULE:
			case CLASS:
			case ENUMERATION:
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof TLModel;
	}

	@Override
	public Iterator<?> getChildIterator(Object node) {
		switch (kind(node)) {
			case MODEL: {
				return ((TLModel) node).getModules().iterator();
			}
			case MODULE: {
				return getTypesIterator((TLModule) node);
			}
			case CLASS: {
				TLClass clazz = (TLClass) node;
				return FilterUtil.filterIterator(new InModule(clazz.getModule()),
					clazz.getSpecializations().iterator());
			}
			default: {
				return Collections.emptyIterator();
			}
		}
	}

	private Iterator<?> getTypesIterator(TLModule module) {
		List<TLType> types = FilterUtil.filterList(TopLevelClasses.INSTANCE, module.getTypes());
		types.addAll(module.getEnumerations());

		return types.iterator();
	}

}
