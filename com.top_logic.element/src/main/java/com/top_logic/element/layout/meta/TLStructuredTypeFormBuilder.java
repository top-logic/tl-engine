/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Collection;
import java.util.List;

import com.top_logic.base.config.i18n.Internationalized;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.GenericFunction;
import com.top_logic.basic.func.Not;
import com.top_logic.basic.func.misc.NonEmpty;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.ExtendsConfig;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.model.utility.DefaultTreeOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.config.FullQualifiedName;
import com.top_logic.model.config.PartNameConstraints;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.config.TypeRef;
import com.top_logic.model.util.AllClasses;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * {@link DeclarativeFormBuilder} for properties of a {@link TLClass}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLStructuredTypeFormBuilder
		extends DeclarativeFormBuilder<TLClass, TLStructuredTypeFormBuilder.EditModel> {

	/**
	 * Creates a {@link TLStructuredTypeFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TLStructuredTypeFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Model of the displayed form.
	 */
	@DisplayOrder({
		EditModel.MODULE,
		EditModel.NAME,
		EditModel.FULL_QUALIFIED_NAME,
		EditModel.ABSTRACT,
		EditModel.FINAL,
		EditModel.FINAL,
		EditModel.GENERALIZATION_TYPES,
		EditModel.SPECIALIZATION_TYPES,
		EditModel.LABEL,
		EditModel.DESCRIPTION,
		EditModel.ANNOTATIONS,
	})
	@DisplayInherited(DisplayStrategy.IGNORE)
	public interface EditModel extends ClassConfig, Internationalized, FullQualifiedName, TypeRef {

		/**
		 * @see #hasSpecializations()
		 */
		String HAS_SPECIALIZATIONS = "hasSpecializations";

		/**
		 * @see #getNoSpecializations()
		 */
		String NO_SPECIALIZATIONS = "noSpecializations";

		/**
		 * @see #hasInstances()
		 */
		String HAS_INSTANCES = "hasInstances";

		/**
		 * @see #getNoInstances()
		 */
		String NO_INSTANCES = "noInstances";

		/**
		 * @see #getEditedType()
		 */
		String EDITED_TYPE = "edited-type";

		/**
		 * @see #getModule()
		 */
		String MODULE = "module";

		/**
		 * @see #isConcrete()
		 */
		String CONCRETE = "concrete";

		/**
		 * @see #isExtendable()
		 */
		String EXTENDABLE = "extendable";

		/**
		 * @see #isCreate()
		 */
		String CREATE = "create";

		/**
		 * @see #isEditing()
		 */
		String EDITING = "editing";

		/**
		 * @see #getGeneralizationTypes()
		 */
		String GENERALIZATION_TYPES = "generalization-types";

		/**
		 * @see #getSpecializationTypes()
		 */
		String SPECIALIZATION_TYPES = "specialization-types";

		/**
		 * Whether this editor is in create mode.
		 */
		@Name(CREATE)
		boolean isCreate();

		/**
		 * @see #isCreate()
		 */
		void setCreate(boolean value);

		/**
		 * Not {@link #isCreate()}
		 */
		@Name(EDITING)
		@Hidden
		@Derived(fun = Not.class, args = @Ref(CREATE))
		boolean isEditing();

		/**
		 * The existing type that is currently being {@link #isEditing() edited}.
		 */
		@Name(EDITED_TYPE)
		@InstanceFormat
		@Hidden
		@CalledByReflection
		TLStructuredType getEditedType();

		/**
		 * @see #getEditedType()
		 */
		void setEditedType(TLStructuredType value);

		@Override
		@Hidden
		String getTypeSpec();

		/**
		 * The owning module.
		 */
		@Name(MODULE)
		@InstanceFormat
		@ItemDisplay(ItemDisplayType.VALUE)
		@Options(fun = AllModules.class)
		@OptionLabels(MetaResourceProvider.class)
		@DynamicMode(fun = HideActiveIf.class, args = @Ref(EDITING))
		@Mandatory
		TLModule getModule();

		/**
		 * @see #getModule()
		 */
		void setModule(TLModule value);

		/**
		 * Whether the edited type is potentially instantiated.
		 */
		@Name(HAS_INSTANCES)
		boolean hasInstances();

		/**
		 * @see #hasInstances()
		 */
		void setInstances(boolean value);

		/**
		 * Not {@link #hasInstances()}
		 */
		@Name(NO_INSTANCES)
		@Derived(fun = Not.class, args = @Ref(HAS_INSTANCES))
		boolean getNoInstances();

		/**
		 * Whether the edited type has {@link #getSpecializationTypes()}.
		 */
		@Name(HAS_SPECIALIZATIONS)
		@Derived(fun = NonEmpty.class, args = @Ref(SPECIALIZATION_TYPES))
		boolean hasSpecializations();

		/**
		 * Not {@link #hasSpecializations()}.
		 */
		@Name(NO_SPECIALIZATIONS)
		@Derived(fun = Not.class, args = @Ref(HAS_SPECIALIZATIONS))
		boolean getNoSpecializations();

		@Override
		@DynamicMode(fun = ActiveIf.class, args = @Ref(CREATE))
		@RegexpConstraint(value = PartNameConstraints.RECOMMENDED_TYPE_NAME_PATTERN, errorKey = PartNameConstraints.RecommendedTypeNameKey.class, asWarning = true)
		String getName();

		/**
		 * Not {@link #isAbstract()}
		 */
		@Name(CONCRETE)
		@Hidden
		@Derived(fun = Not.class, args = @Ref(ABSTRACT))
		boolean isConcrete();

		/**
		 * Not {@link #isFinal()}
		 */
		@Name(EXTENDABLE)
		@Hidden
		@Derived(fun = Not.class, args = @Ref(FINAL))
		boolean isExtendable();

		@Override
		@DynamicMode(fun = ActiveIf.class, args = { @Ref(NO_INSTANCES), @Ref(EXTENDABLE) })
		boolean isAbstract();

		@Override
		@DynamicMode(fun = ActiveIf.class, args = { @Ref(NO_SPECIALIZATIONS), @Ref(CONCRETE) })
		boolean isFinal();

		@Override
		@Hidden
		List<ExtendsConfig> getGeneralizations();

		/**
		 * Input property for {@link #getGeneralizations()}.
		 */
		@Name(GENERALIZATION_TYPES)
		@InstanceFormat
		@Options(fun = AllExtendableClassesDirect.class)
		@ItemDisplay(ItemDisplayType.VALUE)
		@OptionLabels(MetaResourceProvider.class)
		@ControlProvider(SelectionControlProvider.class)
		@DynamicMode(fun = ActiveIf.class, args = { @Ref(NO_INSTANCES) })
		List<TLClass> getGeneralizationTypes();

		/**
		 * All currently known specializations of the displayed type.
		 */
		@Name(SPECIALIZATION_TYPES)
		@InstanceFormat
		@ItemDisplay(ItemDisplayType.VALUE)
		@OptionLabels(MetaResourceProvider.class)
		@DynamicMode(fun = HideImmutableIf.class, args = @Ref(CREATE))
		@ControlProvider(SelectionControlProvider.class)
		List<TLClass> getSpecializationTypes();

		@Override
		@DynamicMode(fun = HideImmutableIf.class, args = @Ref(CREATE))
		String getFullQualifiedName();

		/**
		 * {@link AllClasses} that are non-final without converting to and from qualified names.
		 */
		class AllExtendableClassesDirect extends AllClasses {

			private DeclarativeFormOptions _options;

			/**
			 * Creates a new {@link AllExtendableClassesDirect}.
			 */
			public AllExtendableClassesDirect(DeclarativeFormOptions options) {
				_options = options;
			}

			@Override
			protected OptionModel<TLModelPart> options(TypesTree tree, Filter<? super TLModelPart> modelFilter) {
				return new DefaultTreeOptionModel<>(tree, modelFilter);
			}

			@Override
			protected Filter<? super TLModelPart> modelFilter() {
				EditModel editModel = (EditModel) _options.get(DeclarativeFormBuilder.FORM_MODEL);
				TLStructuredType editedType = editModel.getEditedType();
				if (editedType instanceof TLClass) {
					return new IsExtendableType((TLClass) editedType);
				} else {
					return new IsExtendableType();
				}

			}

			public class IsExtendableType implements Filter<TLModelPart> {
				private TLClass _edited;

				public IsExtendableType(TLClass edited) {
					_edited = edited;
				}

				public IsExtendableType() {
					this(null);
				}

				@Override
				public boolean accept(TLModelPart object) {
					if (!(object instanceof TLClass)) {
						return false;
					}
					TLClass tlClass = (TLClass) object;
					if (tlClass.isFinal()) {
						return false;
					}
					if (_edited == null) {
						return true;
					}
					return !TLModelUtil.isGeneralization(_edited, tlClass);
				}
			}
		}

		/**
		 * {@link GenericFunction} retrieving all {@link TLModule}s.
		 */
		class AllModules extends Function0<Collection<TLModule>> {
			@Override
			public Collection<TLModule> apply() {
				return ModelService.getApplicationModel().getModules();
			}
		}

	}

	@Override
	protected Class<? extends TLClass> getModelType() {
		return TLClass.class;
	}

	@Override
	protected Class<? extends EditModel> getFormType(Object contextModel) {
		return EditModel.class;
	}
	
	@Override
	protected void initFormModel(EditModel formModel, Object contextModel) {
		super.initFormModel(formModel, contextModel);
		
		boolean creating = getConfig().isCreate();
		formModel.setCreate(creating);

		if (creating) {
			if (contextModel instanceof TLModule) {
				formModel.setModule((TLModule) contextModel);
			} else if (contextModel instanceof TLType) {
				TLType editedType = (TLType) contextModel;
				formModel.setModule(editedType.getModule());
			}

			if (contextModel instanceof TLClass) {
				formModel.getGeneralizationTypes().add((TLClass) contextModel);
			}

			formModel.setInstances(false);
		}
	}

	@Override
	protected void fillFormModel(EditModel formModel, TLClass businessModel) {
		TLClass type = businessModel;
		formModel.setEditedType(type);
		formModel.setTypeSpec(TLModelUtil.qualifiedName(type));
		formModel.setInstances(!type.getSpecializations().isEmpty() || hasInstances(businessModel));
		formModel.setModule(type.getModule());
		formModel.setAbstract(type.isAbstract());
		formModel.setFinal(type.isFinal());
		formModel.setName(type.getName());
		formModel.setFullQualifiedName(TLModelUtil.qualifiedName(businessModel));
		formModel.getGeneralizationTypes().addAll(type.getGeneralizations());
		formModel.getSpecializationTypes().addAll(type.getSpecializations());

		ResKey key = TLModelNamingConvention.getTypeLabelKey(type);
		formModel.setLabel(key);
		formModel.setDescription(key.tooltip());

		for (TLAnnotation annotation : type.getAnnotations()) {
			TLTypeAnnotation typeAnnotation = (TLTypeAnnotation) annotation;
			TLTypeAnnotation copy = TypedConfiguration.copy(typeAnnotation);
			formModel.getAnnotations().add(copy);
		}
	}

	private boolean hasInstances(TLClass businessModel) {
		try (CloseableIterator<TLObject> it = MetaElementUtil.iterateDirectInstances(businessModel, TLObject.class)) {
			return it.hasNext();
		}
	}

}
