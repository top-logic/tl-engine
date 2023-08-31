/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Function1;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.BackReferenceAttributeValueLocator;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.config.TLModelPartMapping;
import com.top_logic.model.config.TypeRefMandatory;
import com.top_logic.model.util.AllTypes;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AttributeValueLocator} that navigates a reference {@link TLStructuredTypePart} in backwards
 * direction.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NavigateBackwards extends BackReferenceAttributeValueLocator implements
		ConfiguredInstance<NavigateBackwards.Config> {

	/**
	 * Configuration options for {@link NavigateBackwards}.
	 */
	@TagName("navigate-backwards")
	public interface Config extends PolymorphicConfiguration<NavigateBackwards>, TypeRefMandatory {

		@Override
		@Options(fun = AllClasses.class, mapping = TLModelPartMapping.class)
		public String getTypeSpec();
		
		/**
		 * The name of the reference to navigate backwards.
		 */
		@Mandatory
		@Options(fun=ReferencesOfType.class, args=@Ref(TYPE_SPEC))
		String getAttributeName();

		/**
		 * @see #getAttributeName()
		 */
		void setAttributeName(String value);
		
		/**
		 * Option provider allowing to select from all classes in the system.
		 */
		class AllClasses extends AllTypes {
			@Override
			protected Filter<? super TLModelPart> modelFilter() {
				return new IsClass();
			}
			
			class IsClass implements Filter<TLModelPart> {
				@Override
				public boolean accept(TLModelPart anObject) {
					ModelKind modelKind = anObject.getModelKind();
					return modelKind == ModelKind.CLASS || modelKind == ModelKind.MODEL || modelKind == ModelKind.MODULE;
				}
			}
		}
		
		/**
		 * Option provider looking up the names of all parts of the {@link TLStructuredType} with
		 * the given qualified name.
		 */
		class ReferencesOfType extends Function1<List<String>, String> {
			@Override
			public List<String> apply(String typeSpec) {
				if (typeSpec == null) {
					return Collections.emptyList();
				}
				
				TLType type;
				try {
					type = TLModelUtil.findType(typeSpec);
				} catch (TopLogicException ex) {
					return Collections.emptyList();
				}
				
				if (!(type instanceof TLStructuredType)) {
					return Collections.emptyList();
				}
				
				if (type instanceof TLClass) {
					return names(((TLClass) type).getAllClassParts());
				} else {
					return names(((TLStructuredType) type).getLocalParts());
				}
			}

			private List<String> names(Collection<? extends TLStructuredTypePart> parts) {
				ArrayList<String> result = new ArrayList<>();
				for (TLStructuredTypePart part : parts) {
					if (part.getModelKind() == ModelKind.REFERENCE) {
						result.add(part.getName());
					}
				}
				Collections.sort(result);
				return result;
			}
		}
	}

	/**
	 * Creates a {@link NavigateBackwards} configuration.
	 */
	public static PolymorphicConfiguration<? extends AttributeValueLocator> createNavigateBackwards(String module,
			String typeName, String attributeName) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setTypeSpec(TLModelUtil.qualifiedName(module, typeName));
		config.setAttributeName(attributeName);
		return config;
	}
	
	private final String _typeSpec;

	private final String _attributeName;

	private final Config _config;

	/**
	 * Creates a {@link NavigateBackwards} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public NavigateBackwards(InstantiationContext context, Config config) {
		_config = config;
		_typeSpec = config.getTypeSpec();
		_attributeName = config.getAttributeName();
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	protected String getValueTypeSpec() {
		return _typeSpec;
	}

	@Override
	protected String getReverseEndSpec() {
		return _attributeName;
	}

	@Override
	protected boolean isCollection() {
		return true;
	}

	@Override
	public Object internalLocateAttributeValue(Object obj) {
		TLObject baseObject = (TLObject) obj;
		TLReference ref = getReference(baseObject);
		return baseObject.tReferers(ref);
	}

	@Override
	public Set<? extends TLObject> locateReferers(Object value) {
		TLObject baseObject = (TLObject) value;
		TLReference ref = getReference(baseObject);
		Object referenceValue = baseObject.tValue(ref);
		if (referenceValue instanceof Collection<?>) {
			return CollectionUtil.toSet((Collection<? extends TLObject>) referenceValue);
		} else {
			return CollectionUtil.singletonOrEmptySet((TLObject) referenceValue);
		}
	}

	private TLReference getReference(TLObject context) {
		// Note: Looking up the actual reference attribute must occur lazily because during
		// initial system setup, the referenced attribute may not yet exist at the time the
		// reverse attribute is created.
		return Operation.getAttribute(context, _typeSpec, _attributeName);
	}

}