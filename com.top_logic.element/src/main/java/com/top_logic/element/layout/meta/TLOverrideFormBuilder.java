/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.Function2;
import com.top_logic.element.layout.meta.TLPropertyFormBuilder.PropertyModel;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.DefaultTreeOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.editor.GroupInlineControlProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.config.TLModelPartMapping;
import com.top_logic.model.util.AllTypes;
import com.top_logic.model.util.TLModelUtil;

/**
 * Editor for overriding {@link TLProperty}s
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLOverrideFormBuilder extends TLStructuredTypePartFormBuilder {

	/**
	 * Simplified configuration display of a {@link TLProperty}.
	 */
	@DisplayOrder({
		PropertyModel.CONFIGURATION_INTERFACE_NAME,
		PropertyModel.NAME,
		PropertyModel.FULL_QUALIFIED_NAME,
		PropertyModel.TYPE_SPEC,

		PropertyModel.MANDATORY,

		PropertyModel.LABEL,
		PropertyModel.DESCRIPTION,

		PropertyModel.ANNOTATIONS,
	})
	@DisplayInherited(DisplayStrategy.IGNORE)
	public interface OverrideModel extends PartModel {

		/**
		 * @see #getOverriddenPart()
		 */
		String OVERRIDDEN_PART = "overridden-part";

		@Override
		@Options(fun = PartNames.class, args = { @Ref({ EDIT_MODEL, EditModel.CONTEXT_TYPE }) })
		String getName();

		/**
		 * Reference to the overridden {@link TLStructuredTypePart}.
		 */
		@Name(OVERRIDDEN_PART)
		@Hidden
		@Derived(fun = FindPart.class, args = { @Ref({ EDIT_MODEL, EditModel.CONTEXT_TYPE }), @Ref(NAME) })
		TLStructuredTypePart getOverriddenPart();

		@Override
		@Options(fun = SpecializationsOf.class, args = { @Ref(OVERRIDDEN_PART) }, mapping = TLModelPartMapping.class)
		String getTypeSpec();

		@Override
		@BooleanDefault(true)
		boolean isOverride();

		/**
		 * Options of part names to override.
		 */
		class PartNames extends Function1<List<String>, TLStructuredType> {

			@Override
			public List<String> apply(TLStructuredType type) {
				ArrayList<String> result = new ArrayList<>();

				Collection<? extends TLStructuredTypePart> parts;
				if (type instanceof TLClass) {
					parts = ((TLClass) type).getAllClassParts();
				} else {
					parts = type.getLocalParts();
				}
				for (TLStructuredTypePart part : parts) {
					result.add(part.getName());
				}

				return result;
			}

		}

		/**
		 * Locates the overridden {@link TLStructuredTypePart} in the given type with given name.
		 */
		class FindPart extends Function2<TLStructuredTypePart, TLStructuredType, String> {
			@Override
			public TLStructuredTypePart apply(TLStructuredType baseClass, String name) {
				if (baseClass == null) {
					return null;
				}
				if (StringServices.isEmpty(name)) {
					return null;
				}

				return baseClass.getPart(name);
			}
		}

		/**
		 * Options tree only showing specializations of a given type.
		 */
		class SpecializationsOf extends Function1<OptionModel<TLModelPart>, TLStructuredTypePart> {

			@Override
			public OptionModel<TLModelPart> apply(TLStructuredTypePart overriddenPart) {
				if (overriddenPart == null) {
					return empty();
				}

				final TLType baseType = overriddenPart.getType();

				Filter<TLModelPart> selectableFilter = new Filter<>() {
					@Override
					public boolean accept(TLModelPart anObject) {
						if (anObject instanceof TLType) {
							TLType actualType = (TLType) anObject;
							return TLModelUtil.isCompatibleType(baseType, actualType);
						} else {
							return false;
						}
					}
				};
				return new DefaultTreeOptionModel<>(new AllTypes.TypesTree(), selectableFilter);
			}

			private DefaultListOptionModel<TLModelPart> empty() {
				return new DefaultListOptionModel<>(Collections.emptyList());
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
		@ItemDefault(OverrideModel.class)
		@DynamicMode(fun = GroupActiveIf.class, args = @Ref(CREATING))
		@ControlProvider(GroupInlineControlProvider.class)
		PartModel getPartModel();

	}

	/**
	 * Creates a {@link TLOverrideFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLOverrideFormBuilder(InstantiationContext context, Config config) {
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

}
