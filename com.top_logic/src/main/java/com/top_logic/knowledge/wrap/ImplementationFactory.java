/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.TypeContext;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation;
import com.top_logic.knowledge.wrap.binding.ImplementationBinding;
import com.top_logic.model.TLObject;

/**
 * Strategy for binding implementation classes to persistent object types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ImplementationFactory {

	/** {@link ImplementationBinding} for a {@link MetaObject static type}. */
	private Map<MOClass, ImplementationBinding> _bindingForStaticType;

	/** Monitor object to synchronize access to internal state. */
	private final Object _lock = this;

	/**
	 * Creates a {@link ImplementationFactory}.
	 */
	public ImplementationFactory() {
		_bindingForStaticType = new HashMap<>();
	}

	private void registerBinding(MOClass staticType, ImplementationBinding binding) {
		ImplementationBinding clash = _bindingForStaticType.put(staticType, binding);
		assert clash == null : "Inconsistent implementation binding for static type '"
			+ staticType.getName() + "': " + clash + " vs. " + binding;
	}

	/**
	 * Creates the Java binding for the given {@link KnowledgeItem}.
	 * 
	 * @param item
	 *        The KO to be wrapped.
	 * @return The application wrapper.
	 */
	public TLObject createBinding(KnowledgeItem item) {
		return lookupBinding(item).createBinding(item);
	}

	private ImplementationBinding lookupBinding(KnowledgeItem item) {
		return lookupBinding(item.tTable());
	}

	private ImplementationBinding lookupBinding(MetaObject staticType) {
		ImplementationBinding binding;
		synchronized (_lock) {
			binding = _bindingForStaticType.get(staticType);
			if (binding == null) {
				throw new AssertionError("No implementation class binding for static type '" + staticType.getName()
					+ "'.");
			}
		}
		return binding;
	}

	/**
	 * Registers bindings for all static types in the given {@link TypeContext}.
	 */
	public void init(TypeContext typeContext) {
		for (MetaObject type : typeContext.getMetaObjects()) {
			if (!(type instanceof MOClass)) {
				continue;
			}

			MOClass clazz = (MOClass) type;
			if (clazz.isAbstract()) {
				continue;
			}

			ImplementationBindingAnnotation bindingAnnotation = getBindingAnnotation(clazz);
			if (bindingAnnotation != null) {
				PolymorphicConfiguration<? extends ImplementationBinding> bindingConfig =
					bindingAnnotation.getBinding();
				ImplementationBinding binding =
					SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(bindingConfig);
				binding.initTable(clazz);
				registerBinding(clazz, binding);
			}
		}
	}

	private static ImplementationBindingAnnotation getBindingAnnotation(MOClass clazz) {
		ImplementationBindingAnnotation bindingAnnotation =
			clazz.getAnnotation(ImplementationBindingAnnotation.class);
		if (bindingAnnotation == null) {
			MOClass superclass = clazz.getSuperclass();
			if (superclass != null) {
				return getBindingAnnotation(superclass);
			}
		}
		return bindingAnnotation;
	}

	/**
	 * The default implementation class for the given static type, or <code>null</code> when there
	 * is no {@link ImplementationBinding} for the type or the implementation binding has no useful
	 * default implementation class.
	 */
	public Class<? extends TLObject> getImplementationClass(MetaObject staticType) {
		ImplementationBinding wrapperBinding;
		synchronized (_lock) {
			wrapperBinding = _bindingForStaticType.get(staticType);
		}
		if (wrapperBinding == null) {
			return null;
		}

		return wrapperBinding.getDefaultImplementationClassForTable();
	}

}