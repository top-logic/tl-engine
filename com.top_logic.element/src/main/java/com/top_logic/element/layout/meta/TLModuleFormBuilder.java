/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Collection;

import com.top_logic.base.config.i18n.Internationalized;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.config.TLModuleAnnotation;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.util.model.ModelService;

/**
 * {@link DeclarativeFormBuilder} for a {@link TLModule}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLModuleFormBuilder extends DeclarativeFormBuilder<TLModule, TLModuleFormBuilder.EditModel> {

	/**
	 * Creates a {@link TLModuleFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLModuleFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Base edit properties of all {@link TLModule} configurations.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	@DisplayOrder({
		EditModel.NAME,
		EditModel.LABEL,
		EditModel.DESCRIPTION,
		EditModel.ANNOTATIONS
	})
	@DisplayInherited(DisplayStrategy.IGNORE)
	public interface EditModel extends ModuleConfig, Internationalized {
		@Override
		@Hidden
		Collection<TypeConfig> getTypes();

		/**
		 * @see #isCreate()
		 */
		String CREATE = "create";

		/**
		 * Whether this editor is in create mode.
		 */
		@Name(CREATE)
		@Hidden
		boolean isCreate();

		/**
		 * @see #isCreate()
		 */
		void setCreate(boolean value);

		@DynamicMode(fun = ActiveIf.class, args = @Ref({ CREATE }))
		@Override
		@Constraint(value = ModuleNotExistsConstraint.class, args = @Ref(CREATE))
		String getName();

		/**
		 * {@link ValueConstraint} that checks that no {@link TLModule} with the configured name
		 * exists.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		class ModuleNotExistsConstraint extends GenericValueDependency<String, Boolean> {

			/**
			 * Creates a {@link ModuleNotExistsConstraint}.
			 */
			public ModuleNotExistsConstraint() {
				super(String.class, Boolean.class);
			}

			@Override
			protected void checkValue(PropertyModel<String> propertyModel, PropertyModel<Boolean> createModel) {
				Boolean isCreate = createModel.getValue();
				if (isCreate == null || !isCreate.booleanValue()) {
					return;
				}
				String newModuleName = propertyModel.getValue();
				if (StringServices.isEmpty(newModuleName)) {
					return;
				}
				TLModule existingModule = ModelService.getApplicationModel().getModule(newModuleName);
				if (existingModule != null) {
					propertyModel
						.setProblemDescription(I18NConstants.ERROR_MODULE_WITH_NAME_EXISTS__NAME.fill(newModuleName));
				}

			}

		}
	}


	@Override
	protected Class<? extends TLModule> getModelType() {
		return TLModule.class;
	}

	@Override
	protected Class<? extends EditModel> getFormType(Object contextModel) {
		return EditModel.class;
	}

	@Override
	protected void initFormModel(EditModel formModel, Object contextModel) {
		super.initFormModel(formModel, contextModel);
		formModel.setCreate(getConfig().isCreate());
	}

	@Override
	protected void fillFormModel(EditModel formModel, TLModule businessModel) {
		formModel.setName(businessModel.getName());

		ResKey key = TLModelNamingConvention.getModuleLabelKey(businessModel);
		formModel.setLabel(key);
		formModel.setDescription(key.tooltip());
		formModel.setCreate(getConfig().isCreate());

		for (TLAnnotation annotation : businessModel.getAnnotations()) {
			TLModuleAnnotation moduleAnnotation = (TLModuleAnnotation) annotation;
			TLModuleAnnotation copy = TypedConfiguration.copy(moduleAnnotation);
			formModel.getAnnotations().add(copy);
		}
	}

}
