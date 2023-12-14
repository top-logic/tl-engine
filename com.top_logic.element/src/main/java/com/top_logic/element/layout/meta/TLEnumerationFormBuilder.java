/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.List;

import com.top_logic.base.config.i18n.Internationalized;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.TLStructuredTypeFormBuilder.EditModel.AllModules;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.edit.annotation.CollapseEntries;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLClassifierAnnotation;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.config.EnumConfig.ClassifierConfig;
import com.top_logic.model.config.FullQualifiedName;
import com.top_logic.model.config.PartNameConstraints;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link DeclarativeFormBuilder} for properties of a {@link TLEnumeration}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLEnumerationFormBuilder
		extends DeclarativeFormBuilder<TLEnumeration, TLEnumerationFormBuilder.EditModel> {

	/**
	 * Creates a {@link TLEnumerationFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLEnumerationFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Model of the displayed enumeration.
	 */
	@DisplayOrder({
		EditModel.NAME,
		EditModel.FULL_QUALIFIED_NAME,
		EditModel.LABEL,
		EditModel.DESCRIPTION,
		EditModel.I18N_CLASSIFIERS_CONFIG,
		EditModel.ANNOTATIONS
	})
	public interface EditModel extends EnumConfig, Internationalized, FullQualifiedName {

		/**
		 * Name for {@link #getI18NClassifiersConfig()}.
		 */
		public static final String I18N_CLASSIFIERS_CONFIG = "i18nClassifiersConfig";

		/**
		 * @see #getModule()
		 */
		String MODULE = "module";

		/**
		 * @see #isCreate()
		 */
		String CREATE = "create";

		/**
		 * Whether this editor is in create mode.
		 */
		@Name(CREATE)
		boolean isCreate();

		/**
		 * @see #isCreate()
		 */
		void setCreate(boolean value);

		@Override
		@Hidden
		List<ClassifierConfig> getClassifiers();

		/**
		 * Internationalized {@link ClassifierConfig}.
		 */
		@Name(I18N_CLASSIFIERS_CONFIG)
		@TitleProperty(name = ClassifierModel.NAME)
		@CollapseEntries
		List<ClassifierModel> getI18NClassifiersConfig();

		/**
		 * The owning module.
		 */
		@Name(MODULE)
		@InstanceFormat
		@ItemDisplay(ItemDisplayType.VALUE)
		@Options(fun = AllModules.class)
		@OptionLabels(MetaResourceProvider.class)
		@Mandatory
		TLModule getModule();

		/**
		 * @see #getModule()
		 */
		void setModule(TLModule value);

		@Override
		@DynamicMode(fun = HideImmutableIf.class, args = @Ref(CREATE))
		String getFullQualifiedName();

		@DynamicMode(fun = ActiveIf.class, args = @Ref({ CREATE }))
		@RegexpConstraint(value = PartNameConstraints.RECOMMENDED_TYPE_NAME_PATTERN, errorKey = PartNameConstraints.RecommendedTypeNameKey.class, asWarning = true)
		@Constraint(value = TLStructuredTypeFormBuilder.EditModel.TypeNotExistsConstraint.class, args = { @Ref(MODULE),
			@Ref(CREATE) })
		@Override
		String getName();
	}

	/**
	 * Model of the displayed classifier.
	 */
	@DisplayOrder({
		EditModel.NAME,
		ClassifierModel.FULL_QUALIFIED_NAME,
		ClassifierConfig.DEFAULT,
		EditModel.LABEL,
		EditModel.DESCRIPTION,
		EditModel.ANNOTATIONS
	})
	public interface ClassifierModel extends ClassifierConfig, ConfigPart, Internationalized, FullQualifiedName {

		/** Configuration name for the value of {@link #isCreate()} */
		String CREATE = "create";

		@Override
		@DynamicMode(fun = HideImmutableIf.class, args = @Ref({ CREATE }))
		String getFullQualifiedName();

		@Override
		@DynamicMode(fun = ActiveIf.class, args = @Ref({ CREATE }))
		@RegexpConstraint(value = PartNameConstraints.RECOMMENDED_CLASSIFIER_NAME_PATTERN, errorKey = PartNameConstraints.RecommendedClassifierNameKey.class, asWarning = true)
		String getName();

		/**
		 * Whether the {@link ClassifierModel} is currently created.
		 */
		@Hidden
		@Name(CREATE)
		@BooleanDefault(true)
		boolean isCreate();

		/**
		 * Setter for {@link #isCreate()}.
		 */
		void setCreate(boolean b);

	}

	@Override
	protected Class<? extends TLEnumeration> getModelType() {
		return TLEnumeration.class;
	}

	@Override
	protected Class<? extends EditModel> getFormType(Object contextModel) {
		return EditModel.class;
	}

	@Override
	protected void fillFormModel(EditModel formModel, TLEnumeration enumeration) {
		formModel.setName(enumeration.getName());
		formModel.setFullQualifiedName(TLModelUtil.qualifiedName(enumeration));
		formModel.setModule(enumeration.getModule());

		fillClassifiers(formModel, enumeration);
		fillI18N(formModel, enumeration);
		fillAnnotations(formModel, enumeration);
	}

	@Override
	protected void initFormModel(EditModel formModel, Object contextModel) {
		super.initFormModel(formModel, contextModel);

		boolean creating = getConfig().isCreate();
		formModel.setCreate(creating);

		if (contextModel instanceof TLModule) {
			formModel.setModule((TLModule) contextModel);
		} else if (contextModel instanceof TLType) {
			TLType editedType = (TLType) contextModel;

			formModel.setModule(editedType.getModule());
		}
	}

	private void fillAnnotations(EditModel formModel, TLEnumeration enumeration) {
		for (TLAnnotation annotation : enumeration.getAnnotations()) {
			TLTypeAnnotation typeAnnotation = (TLTypeAnnotation) annotation;
			TLTypeAnnotation copy = TypedConfiguration.copy(typeAnnotation);
			formModel.getAnnotations().add(copy);
		}
	}

	private void fillI18N(EditModel formModel, TLEnumeration enumeration) {
		ResKey key = TLModelNamingConvention.enumKey(enumeration);

		formModel.setLabel(key);
		formModel.setDescription(key.tooltip());
	}

	private void fillClassifiers(EditModel formModel, TLEnumeration enumeration) {
		List<ClassifierModel> classifiers = formModel.getI18NClassifiersConfig();

		for(TLClassifier classifier : enumeration.getClassifiers()) {
			ClassifierModel classifierModel = TypedConfiguration.newConfigItem(ClassifierModel.class);

			classifierModel.setName(classifier.getName());
			classifierModel.setFullQualifiedName(TLModelUtil.qualifiedName(classifier));
			classifierModel.setDefault(classifier.isDefault());
			classifierModel.setCreate(false);

			ResKey key = TLModelNamingConvention.classifierKey(classifier);
			classifierModel.setLabel(key);
			classifierModel.setDescription(key.tooltip());

			for (TLAnnotation annotation : classifier.getAnnotations()) {
				TLClassifierAnnotation classifierAnnotation = (TLClassifierAnnotation) annotation;
				TLClassifierAnnotation copy = TypedConfiguration.copy(classifierAnnotation);
				classifierModel.getAnnotations().add(copy);
			}

			classifiers.add(classifierModel);
		}
	}

}
