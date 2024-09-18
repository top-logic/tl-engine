/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.Function2;
import com.top_logic.basic.shared.string.StringServicesShared;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.element.layout.meta.TLPropertyFormBuilder.PropertyModel;
import com.top_logic.knowledge.service.db2.AssociationReference.CurrentDefault;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.editor.GroupInlineControlProvider;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.config.TLModelPartMapping;
import com.top_logic.model.util.AllTypes;
import com.top_logic.model.util.TLModelUtil;

/**
 * Editor for {@link TLReference}s
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLReferenceFormBuilder extends TLStructuredTypePartFormBuilder {

	/**
	 * Configuration display of a {@link TLReference}.
	 */
	@DisplayOrder({
		PropertyModel.CONFIGURATION_INTERFACE_NAME,
		ReferenceModel.NAME,
		ReferenceModel.FULL_QUALIFIED_NAME,
		ReferenceModel.OVERRIDE,
		ReferenceModel.TYPE_SPEC,
		ReferenceModel.KIND,
		ReferenceModel.FORWARDS_REFERENCE,

		ReferenceModel.MANDATORY,
		ReferenceModel.MULTIPLE_PROPERTY,
		ReferenceModel.ORDERED_PROPERTY,
		ReferenceModel.BAG_PROPERTY,
		ReferenceModel.ABSTRACT_PROPERTY,
		ReferenceModel.COMPOSITE_PROPERTY,
		ReferenceModel.AGGREGATE_PROPERTY,
		ReferenceModel.NAVIGATE_PROPERTY,

		ReferenceModel.HISTORY_TYPE_PROPERTY,
		ReferenceModel.DELETION_POLICY_PROPERTY,

		ReferenceModel.LABEL,
		ReferenceModel.DESCRIPTION,

		ReferenceModel.ANNOTATIONS,
	})
	public interface ReferenceModel extends PartModel, ReferenceConfig {

		/**
		 * Configuration name for {@link #getForwardsReference()}.
		 */
		String FORWARDS_REFERENCE = "forwards-reference";

		@Override
		@Options(fun = AllReferenceTypes.class, mapping = TLModelPartMapping.class)
		String getTypeSpec();

		/**
		 * {@link com.top_logic.model.util.AllTypes} reduced to only {@link TLClass
		 * classes} and {@link TLEnumeration}s.
		 */
		class AllReferenceTypes extends AllTypes {
			@Override
			protected TypesTree tree() {
				return new ReferenceTypesTree();
			}

			protected static class ReferenceTypesTree extends TypesTree {
				@Override
				protected boolean acceptType(TLModule module, TLType type) {
					ModelKind kind = type.getModelKind();
					if (kind != ModelKind.CLASS && kind != ModelKind.ENUMERATION) {
						return false;
					}
					return super.acceptType(module, type);
				}
			}
		}

		@DynamicMode(fun = CompositeActive.class, args = {
			@Ref({ EDIT_MODEL, EditModel.CREATING }),
			@Ref({ HISTORY_TYPE_PROPERTY })
		})
		@Override
		boolean isComposite();

		@Override
		@DynamicMode(fun = ShowAggregate.class, args = @Ref({ EDIT_MODEL, EditModel.CREATING }))
		boolean isAggregate();

		@Override
		@BooleanDefault(true)
		@DynamicMode(fun = ActiveIf.class, args = @Ref({ EDIT_MODEL, EditModel.CREATING }))
		boolean canNavigate();

		@Override
		@ComplexDefault(CurrentDefault.class)
		@DynamicMode(fun = ActiveIf.class, args = @Ref({ EDIT_MODEL, EditModel.CREATING }))
		HistoryType getHistoryType();

		@Override
		@DynamicMode(fun = CurrentAndCreating.class, args = {
			@Ref(HISTORY_TYPE_PROPERTY),
			@Ref({ EDIT_MODEL, EditModel.CREATING })
		})
		DeletionPolicy getDeletionPolicy();

		/**
		 * {@link FieldMode} of {@link ReferenceModel#getDeletionPolicy()}.
		 */
		class CurrentAndCreating extends Function2<FieldMode, HistoryType, Boolean> {
			@Override
			public FieldMode apply(HistoryType arg1, Boolean creating) {
				return arg1 == HistoryType.CURRENT ? 
					(Utils.isTrue(creating) ? FieldMode.ACTIVE : FieldMode.IMMUTABLE)
					: FieldMode.DISABLED;
			}
		}

		@Override
		@FormattedDefault(ReferenceKind.Names.FORWARDS_NAME)
		@DynamicMode(fun = HideImmutableIf.class, args = @Ref({ EDIT_MODEL, EditModel.CREATING }))
		ReferenceKind getKind();

		@Override
		@Hidden
		String getInverseReference();

		/**
		 * If this reference is a
		 * {@link com.top_logic.element.config.ReferenceConfig.ReferenceKind#BACKWARDS backwards}
		 * reference, then this is the corresponding forwards reference.
		 */
		@DynamicMode(fun = HideIfNotBackwards.class, args = @Ref(KIND))
		@Derived(fun = ForwardsRefComputation.class, args = { @Ref(TYPE_SPEC), @Ref(INVERSE_REFERENCE) })
		@Name(FORWARDS_REFERENCE)
		@InstanceFormat
		TLTypePart getForwardsReference();

		@Override
		@Hidden
		String getEndName();

		@Override
		@Derived(fun = ResolveTypeKind.class, args = { @Ref(COMPOSITE_PROPERTY), @Ref(RESOLVED_TYPE) })
		TLTypeKind getTypeKind();

		/**
		 * Function resolving the {@link TLTypeKind} for a {@link TLReference}.
		 */
		class ResolveTypeKind extends Function2<TLTypeKind, Boolean, TLType> {
			@Override
			public TLTypeKind apply(Boolean isComposite, TLType type) {
				if (isComposite) {
					return TLTypeKind.COMPOSITION;
				}

				if (type != null) {
					return TLTypeKind.getTLTypeKind(type);
				}

				return null;
			}
		}

		/**
		 * Dynamic {@link FieldMode} computation of {@link ReferenceModel#isAggregate()}.
		 */
		class ShowAggregate extends Function1<FieldMode, Boolean> {
			@Override
			public FieldMode apply(Boolean creating) {
				return creating != null && creating.booleanValue() ? FieldMode.INVISIBLE : FieldMode.IMMUTABLE;
			}
		}

		/**
		 * Dynamic {@link FieldMode} computation of {@link ReferenceModel#isComposite()}.
		 */
		class CompositeActive extends Function2<FieldMode, Boolean, HistoryType> {

			@Override
			public FieldMode apply(Boolean createMode, HistoryType historyType) {
				if (!Utils.isTrue(createMode)) {
					return FieldMode.IMMUTABLE;
				}
				if (historyType == null) {
					return FieldMode.ACTIVE;
				}
				switch (historyType) {
					case CURRENT:
						return FieldMode.ACTIVE;
					case HISTORIC:
					case MIXED:
						return FieldMode.DISABLED;

				}
				throw new UnreachableAssertion("All history types covered.");
			}

		}

		/**
		 * Function returning {@link FieldMode#IMMUTABLE} for
		 * {@link com.top_logic.element.config.ReferenceConfig.ReferenceKind#BACKWARDS} and
		 * {@link FieldMode#INVISIBLE} otherwise.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		class HideIfNotBackwards extends Function1<FieldMode, ReferenceKind> {

			@Override
			public FieldMode apply(ReferenceKind arg) {
				if (arg == ReferenceKind.BACKWARDS) {
					return FieldMode.IMMUTABLE;
				}
				return FieldMode.INVISIBLE;
			}
		}

		/**
		 * Function computing the forwards reference for a backwards reference.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		class ForwardsRefComputation extends Function2<TLTypePart, String, String> {

			@Override
			public TLTypePart apply(String typeSpec, String partSpec) {
				if (StringServicesShared.isEmpty(typeSpec) || StringServicesShared.isEmpty(partSpec)) {
					return null;
				}
				TLType type = TLModelUtil.findType(typeSpec);
				if (!(type instanceof TLStructuredType)) {
					return null;
				}
				return ((TLStructuredType) type).getPart(partSpec);
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
		@ItemDefault(ReferenceModel.class)
		@DynamicMode(fun = GroupActiveIf.class, args = @Ref(CREATING))
		@ControlProvider(GroupInlineControlProvider.class)
		PartModel getPartModel();

	}

	/**
	 * Creates a {@link TLReferenceFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLReferenceFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Class<? extends TLModelPart> getModelType() {
		return TLReference.class;
	}

	@Override
	protected Class<? extends EditModel> getFormType(Object contextModel) {
		return EditModel.class;
	}

	/**
	 * Initialisation of the {@link ReferenceModel} with the help of the given {@link TLReference}.
	 */
	public static void initWithReference(TLStructuredTypePartFormBuilder.EditModel formModel, TLReference reference) {
		ReferenceModel referenceModel = TypedConfiguration.newConfigItem(ReferenceModel.class);
		referenceModel.setAggregate(reference.getEnd().isAggregate());
		referenceModel.setComposite(reference.getEnd().isComposite());
		referenceModel.setNavigate(reference.getEnd().canNavigate());
		referenceModel.setKind(TLMetaModelUtil.getReferenceKind(reference));
		referenceModel.setHistoryType(reference.getHistoryType());

		TLReference inverse = TLModelUtil.getOtherEnd(reference.getEnd()).getReference();
		if (inverse != null) {
			referenceModel.setInverseReference(inverse.getName());
		}
		initWithPart(formModel, referenceModel, reference);
	}

}
