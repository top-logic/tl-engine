/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.element.config.AttributeConfig;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.editor.GroupInlineControlProvider;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLSupportsMultiple;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Editor for {@link TLProperty}s
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLPropertyFormBuilder extends TLStructuredTypePartFormBuilder {

	/**
	 * Configuration display of a {@link TLProperty}.
	 */
	@DisplayOrder({
		PropertyModel.CONFIGURATION_INTERFACE_NAME,
		PropertyModel.NAME,
		PropertyModel.FULL_QUALIFIED_NAME,
		PropertyModel.OVERRIDE,
		PropertyModel.TYPE_SPEC,

		PropertyModel.MANDATORY,
		PropertyModel.MULTIPLE_PROPERTY,
		PropertyModel.ORDERED_PROPERTY,
		PropertyModel.BAG_PROPERTY,
		PropertyModel.ABSTRACT_PROPERTY,

		PropertyModel.LABEL,
		PropertyModel.DESCRIPTION,

		PropertyModel.ANNOTATIONS,
	})
	public interface PropertyModel extends PartModel, AttributeConfig {
		// Pure sum interface.

		@Override
		@DynamicMode(fun = MultipleMode.class, args = { @Ref(TYPE_SPEC) })
		boolean isMultiple();

		/**
		 * Function computing, whether the <code>multiple</code> setting is possible.
		 */
		class MultipleMode extends Function1<FieldMode, String> {

			@Override
			public FieldMode apply(String typeRef) {
				if (StringServices.isEmpty(typeRef)) {
					return FieldMode.DISABLED;
				}

				try {
					TLObject typeObj = TLModelUtil.resolveQualifiedName(typeRef);
					if (!(typeObj instanceof TLPrimitive)) {
						return FieldMode.DISABLED;
					}

					TLPrimitive type = (TLPrimitive) typeObj;
					TLSupportsMultiple annotation = type.getAnnotation(TLSupportsMultiple.class);
					if (annotation == null || !annotation.getValue()) {
						return FieldMode.DISABLED;
					}

					return FieldMode.ACTIVE;
				} catch (TopLogicException ex) {
					return FieldMode.DISABLED;
				}
			}

		}
	}

	/**
	 * @see TLStructuredTypePartFormBuilder.EditModel
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface EditModel extends TLStructuredTypePartFormBuilder.EditModel {

		/**
		 * Configuration of the {@link TLStructuredTypePart} to edit.
		 */
		@Override
		@ItemDefault(PropertyModel.class)
		@DynamicMode(fun = GroupActiveIf.class, args = @Ref(CREATING))
		@ControlProvider(GroupInlineControlProvider.class)
		PartModel getPartModel();

	}

	/**
	 * Creates a {@link TLPropertyFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLPropertyFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Class<? extends TLModelPart> getModelType() {
		return TLProperty.class;
	}

	@Override
	protected Class<? extends EditModel> getFormType(Object contextModel) {
		return EditModel.class;
	}

	/**
	 * Initialisation of the {@link PropertyModel} with the help of the given {@link TLProperty}.
	 */
	public static void initWithProperty(TLStructuredTypePartFormBuilder.EditModel formModel, TLProperty property) {
		PartModel partModel = TypedConfiguration.newConfigItem(PropertyModel.class);
		initWithPart(formModel, partModel, property);
	}

}
