/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.binding;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.config.JavaClass;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link ImplementationBinding} that determines the application type based on the {@link JavaClass}
 * annotation of the {@link Config#getTypeRef() specified} type reference of the object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DynamicBinding extends AbstractImplementationBinding implements ConfiguredInstance<DynamicBinding.Config> {

	/**
	 * Configuration options of {@link DynamicBinding}.
	 */
	public interface Config extends PolymorphicConfiguration<DynamicBinding> {

		/**
		 * @see #getTypeRef()
		 */
		public static final String TYPE_REF = "type-ref";

		/**
		 * @see #getDefaultApplicationType()
		 */
		public static final String DEFAULT_APPLICATION_TYPE = "default-application-type";

		/**
		 * The reference pointing to the {@link TLClass} type of the object.
		 */
		@Name(TYPE_REF)
		@Nullable
		String getTypeRef();

		/**
		 * The application type to use, if the {@link #getTypeRef() referenced type} of the object
		 * has no {@link JavaClass} annotation.
		 */
		@Name(DEFAULT_APPLICATION_TYPE)
		Class<? extends Wrapper> getDefaultApplicationType();

	}

	private Config _config;

	private MOAttribute _typeAttr;

	private final Map<TLID, Class<? extends TLObject>> _implementationClasses = new ConcurrentHashMap<>();

	/**
	 * Creates a {@link DynamicBinding} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DynamicBinding(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void initTable(MOClass staticType) {
		super.initTable(staticType);
		String typeRef = _config.getTypeRef();
		if (typeRef == null) {
			typeRef = findTypeRef(staticType.getSuperclass());
			if (typeRef == null) {
				throw new KnowledgeBaseRuntimeException(
					"Missing type reference in dynamic binding annotation of type '" + staticTypeName() + "'.");
			}
		}
		try {
			_typeAttr = staticType.getAttribute(typeRef);
		} catch (NoSuchAttributeException ex) {
			throw new KnowledgeBaseRuntimeException("Missing reference '" + typeRef + "' in table '"
				+ staticTypeName() + "' with dynamic implementation binding.");
		}

		if (!_typeAttr.isInitial()) {
			throw new KnowledgeBaseRuntimeException(
				"Type reference '" + typeRef + "' of type '" + staticTypeName()
					+ "' must be declared initial.");
		}
	}

	private String findTypeRef(MOClass type) {
		if (type == null) {
			return null;
		}
		ImplementationBindingAnnotation bindingAnnotation = type.getAnnotation(ImplementationBindingAnnotation.class);
		if (bindingAnnotation != null) {
			PolymorphicConfiguration<? extends ImplementationBinding> bindingConfig = bindingAnnotation.getBinding();
			if (bindingConfig instanceof Config) {
				String typeRef = ((Config) bindingConfig).getTypeRef();
				if (typeRef != null) {
					return typeRef;
				}
			}
		}
		return findTypeRef(type.getSuperclass());
	}

	@Override
	public TLObject createBinding(KnowledgeItem item) {
		return wrapWith(findImplClass(type(item)), item);
	}

	private Class<? extends TLObject> findImplClass(TLType type) {
		return _implementationClasses.computeIfAbsent(type.tIdLocal(), localId -> findImplClassUncached(type));
	}

	private Class<? extends TLObject> findImplClassUncached(TLType type) {
		JavaClass bindingAnnotation = type.getAnnotation(JavaClass.class);
		if (bindingAnnotation == null) {
			// Try finding class by naming convention.
			String className = TLModelNamingConvention.implementationName(type);
			if (className != null) {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends TLObject> guessedImplClass = (Class<? extends TLObject>) Class.forName(className);
					if (TLObject.class.isAssignableFrom(guessedImplClass)) {
						return guessedImplClass;
					}

					Logger.error(
						"Implementation class '" + className + "' for '" + type + "' must be a subtype of '"
								+ TLObject.class + "'.",
						DynamicBinding.class);
				} catch (ClassNotFoundException ex) {
					// Ignore.
				}
			}
		} else {
			Class<? extends Wrapper> annotatedClass = bindingAnnotation.getImplementationClass();
			if (annotatedClass != null) {
				return annotatedClass;
			}

			if (!bindingAnnotation.getClassName().isEmpty()) {
				// Something was annotated, but it could not be resolved to a class.
				Logger.error(
					"Cannot resolve implementation class '" + bindingAnnotation.getClassName() + "' for '" + type
							+ "'.",
					DynamicBinding.class);
			}
		}

		// Inherit from primary generalization.
		TLClass superClass = TLModelUtil.getPrimaryGeneralization(type);
		if (superClass != null) {
			/* Don't use the cache, as this call is already coming from the cache, which is trying
			 * to compute an absent value. Using it here again would be a nested update, which is
			 * not supported. */
			return findImplClassUncached(superClass);
		}

		return defaultApplicationType();
	}

	@Override
	public Class<? extends TLObject> getDefaultImplementationClassForTable() {
		return defaultApplicationType();
	}

	private TLType type(KnowledgeItem item) {
		KnowledgeItem typeHandle = (KnowledgeItem) item.getValue(_typeAttr);
		return (TLType) typeHandle.getWrapper();
	}

	private Class<? extends Wrapper> defaultApplicationType() {
		return _config.getDefaultApplicationType();
	}

}
