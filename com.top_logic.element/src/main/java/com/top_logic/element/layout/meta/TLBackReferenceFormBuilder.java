/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.Function2;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.element.meta.kbbased.PersistentStructuredTypePart;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.AssociationReference.CurrentDefault;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.editor.GroupInlineControlProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.util.TLModelUtil;

/**
 * Editor for backwards {@link TLReference}s
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLBackReferenceFormBuilder extends TLReferenceFormBuilder {

	/**
	 * Configuration display of a {@link TLReference}.
	 */
	@DisplayOrder({
		BackReferenceModel.CONFIGURATION_INTERFACE_NAME,
		BackReferenceModel.NAME,
		BackReferenceModel.FULL_QUALIFIED_NAME,
		BackReferenceModel.OVERRIDE,
		BackReferenceModel.OTHER_END,
		BackReferenceModel.NAVIGATE_PROPERTY,

		BackReferenceModel.LABEL,
		BackReferenceModel.DESCRIPTION,

		BackReferenceModel.ANNOTATIONS,
	})
	@DisplayInherited(DisplayStrategy.IGNORE)
	public interface BackReferenceModel extends PartModel, ReferenceConfig {

		/**
		 * Name for the #isOtherEndSelectable() property.
		 */
		String SELECT_OTHER_END = "selectOtherEnd";

		/**
		 * @see #getOtherEnd()
		 */
		String OTHER_END = "otherEnd";

		/**
		 * The {@link TLReference} for for which a back reference should be created.
		 */
		@Name(OTHER_END)
		@Options(fun = AllForwardReferences.class, args = @Ref({ EDIT_MODEL, EditModel.CONTEXT_TYPE }))
		@ControlProvider(SelectionControlProvider.class)
		@InstanceFormat
		@ItemDisplay(ItemDisplayType.VALUE)
		@OptionLabels(ReferenceNames.class)
		@DynamicMode(fun = ActiveIf.class, args = @Ref(SELECT_OTHER_END))
		@Mandatory
		TLReference getOtherEnd();

		/**
		 * @see #getOtherEnd()
		 */
		void setOtherEnd(TLReference forwardReference);

		/**
		 * True if {@link #getOtherEnd()} is selectable otherwise it's immutable and not
		 *         active.
		 */
		@Name(SELECT_OTHER_END)
		@BooleanDefault(true)
		boolean isOtherEndSelectable();

		/**
		 * @see #isOtherEndSelectable()
		 */
		void setOtherEndSelectable(boolean isSelectable);

		@Override
		@Derived(fun = TypeOf.class, args = @Ref(OTHER_END))
		@Hidden
		String getTypeSpec();

		@Override
		@Hidden
		@Derived(fun = NameOf.class, args = @Ref(OTHER_END))
		String getInverseReference();

		@Override
		@BooleanDefault(true)
		boolean isMultiple();

		@Override
		@FormattedDefault(ReferenceKind.Names.BACKWARDS_NAME)
		@Hidden
		ReferenceKind getKind();

		@Override
		@BooleanDefault(true)
		boolean canNavigate();

		@Override
		@Hidden
		@ComplexDefault(CurrentDefault.class)
		HistoryType getHistoryType();

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
		 * The owner type name of the given {@link TLStructuredTypePart}.
		 */
		class TypeOf extends Function1<String, TLStructuredTypePart> {
			@Override
			public String apply(TLStructuredTypePart part) {
				if (part == null) {
					return null;
				}
				TLStructuredType type = part.getOwner();
				if (type == null) {
					return null;
				}
				return TLModelUtil.qualifiedName(type);
			}
		}

		/**
		 * The name of the given {@link TLNamedPart}.
		 */
		class NameOf extends Function1<String, TLNamedPart> {
			@Override
			public String apply(TLNamedPart part) {
				if (part == null) {
					return null;
				}
				return part.getName();
			}
		}

		/**
		 * Labels used in {@link BackReferenceModel#getOtherEnd()} options.
		 */
		class ReferenceNames implements LabelProvider {
			@Override
			public String getLabel(Object object) {
				if (object instanceof TLModelPart) {
					return TLModelUtil.qualifiedName((TLModelPart) object);
				} else {
					return MetaLabelProvider.INSTANCE.getLabel(object);
				}
			}
		}

		/**
		 * All forwards references that potentially point to the given context type.
		 */
		class AllForwardReferences extends Function1<OptionModel<TLReference>, TLStructuredType> {

			@Override
			public OptionModel<TLReference> apply(TLStructuredType targetType) {
				return new DefaultListOptionModel<>(getAllForwardReferences(targetType));
			}

			private List<TLReference> getAllForwardReferences(TLStructuredType targetType) {
				KnowledgeBase knowledgeBase = getKnowledgeBase(targetType);
				Iterator<KnowledgeItem> objectsByAttribute = getPossibleReferences(targetType, knowledgeBase);

				return getReferences(objectsByAttribute);
			}

			private List<TLReference> getReferences(Iterator<KnowledgeItem> objectsByAttribute) {
				List<TLReference> references = new LinkedList<>();

				while (objectsByAttribute.hasNext()) {
					KnowledgeItem next = objectsByAttribute.next();
					TLObject wrapper = next.getWrapper();
					if (wrapper instanceof TLAssociationEnd) {
						TLReference reference = ((TLAssociationEnd) wrapper).getReference();
						if (reference != null) {
							references.add(reference);
						}
					}
				}

				return references;
			}

			private Iterator<KnowledgeItem> getPossibleReferences(TLStructuredType type, KnowledgeBase knowledgeBase) {
				return knowledgeBase.getObjectsByAttribute(KBBasedMetaAttribute.OBJECT_NAME,
					PersistentStructuredTypePart.TYPE_REF, type.tHandle());
			}

			private KnowledgeBase getKnowledgeBase(TLStructuredType type) {
				return type.tHandle().getKnowledgeBase();
			}
		}
	}

	/**
	 * @see TLStructuredTypePartFormBuilder.EditModel
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface EditModel extends TLReferenceFormBuilder.EditModel {

		/**
		 * Configuration of the {@link TLStructuredTypePart} to edit.
		 */
		@Override
		@ItemDefault(BackReferenceModel.class)
		@DynamicMode(fun = GroupActiveIf.class, args = @Ref(CREATING))
		@ControlProvider(GroupInlineControlProvider.class)
		PartModel getPartModel();

	}

	/**
	 * Creates a {@link TLBackReferenceFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLBackReferenceFormBuilder(InstantiationContext context, Config config) {
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

	@Override
	protected void initFormModel(TLStructuredTypePartFormBuilder.EditModel formModel, Object contextModel) {
		formModel.setCreating(getConfig().isCreate());
		if (getConfig().isCreate()) {
			if (contextModel instanceof TLReference) {
				formModel.setContextType((TLStructuredType) ((TLStructuredTypePart) contextModel).getType());
				BackReferenceModel partModel = (BackReferenceModel) formModel.getPartModel();
				partModel.setOtherEnd((TLReference) contextModel);
				partModel.setOtherEndSelectable(false);
			} else {
				formModel.setContextType((TLStructuredType) contextModel);
			}
		} else {
			formModel.setContextType(((TLStructuredTypePart) contextModel).getOwner());
		}
	}

}
