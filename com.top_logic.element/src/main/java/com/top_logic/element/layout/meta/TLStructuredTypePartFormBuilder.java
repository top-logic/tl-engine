/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Locale;

import com.top_logic.base.config.i18n.Internationalized;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.Step;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency3;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.func.And;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.Function2;
import com.top_logic.basic.func.Not;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.config.PartConfig;
import com.top_logic.element.config.TypedPartAspect;
import com.top_logic.layout.Control;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.ValueDisplayControl;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.edit.ConfigLabelProvider;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.editor.GroupInlineControlProvider;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.config.FullQualifiedName;
import com.top_logic.model.config.PartNameConstraints;
import com.top_logic.model.config.TypeRef;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Editor for {@link TLStructuredTypePart}s
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLStructuredTypePartFormBuilder extends
		DeclarativeFormBuilder<TLModelPart, TLStructuredTypePartFormBuilder.EditModel> {

	/**
	 * Model of the {@link TLStructuredTypePart} editor.
	 */
	public interface EditModel extends TypeRef, FullQualifiedName {
		
		/**
		 * @see #getPartModel()
		 */
		String PART_MODEL = "part-model";

		/**
		 * @see #isCreating()
		 */
		String CREATING = "creating";

		/**
		 * @see #getContextType()
		 */
		String CONTEXT_TYPE = "context-type";

		/**
		 * Whether a new attribute is created (not a previously existing one is edited).
		 */
		@Name(CREATING)
		@Hidden
		boolean isCreating();

		/**
		 * @see #isCreating()
		 */
		void setCreating(boolean value);

		/**
		 * The type in which the attribute is defined.
		 */
		@InstanceFormat
		@Hidden
		@Name(CONTEXT_TYPE)
		TLStructuredType getContextType();

		/**
		 * @see #getContextType()
		 */
		void setContextType(TLStructuredType tlStructuredType);

		/**
		 * Configuration of the {@link TLStructuredTypePart} to edit.
		 */
		@Name(PART_MODEL)
		@DynamicMode(fun = GroupActiveIf.class, args = @Ref(CREATING))
		@ControlProvider(GroupInlineControlProvider.class)
		PartModel getPartModel();

		/**
		 * @see #getPartModel()
		 */
		void setPartModel(PartModel partModel);

		@Override
		@Hidden
		@DerivedRef(steps = { @Step(PART_MODEL), @Step(value = TYPE_SPEC, type = PartModel.class) })
		String getTypeSpec();

		@Override
		@Hidden
		@DerivedRef(steps = { @Step(PART_MODEL), @Step(value = FULL_QUALIFIED_NAME, type = PartModel.class) })
		String getFullQualifiedName();

	}

	/**
	 * Base edit properties of all {@link TLStructuredTypePart} configuration.
	 */
	@Abstract
	public interface PartModel extends ConfigPart, PartConfig, TypedPartAspect, Internationalized, FullQualifiedName {

		/**
		 * @see #getEditModel()
		 */
		String EDIT_MODEL = "editModel";

		/**
		 * @see #isNewAttribute()
		 */
		String NEW_ATTRIBUTE = "newAttribute";

		/**
		 * @see #getMultipleAndNew()
		 */
		String MULTIPLE_AND_NEW = "multipleAndNew";

		/**
		 * @see #getResolvedType()
		 */
		String RESOLVED_TYPE = "resolved-type";

		/**
		 * @see #getTypeKind()
		 */
		String TYPE_KIND = "type-kind";

		@Override
		@Hidden(false)
		@ControlProvider(ConfigLabel.class)
		Class<?> getConfigurationInterface();

		/**
		 * {@link com.top_logic.layout.form.template.ControlProvider} displaying a label created
		 * with {@link ConfigLabelProvider}.
		 */
		class ConfigLabel implements com.top_logic.layout.form.template.ControlProvider {

			private final Renderer<Object> _renderer = ResourceRenderer.newResourceRenderer(new ConfigLabelProvider());

			@Override
			public Control createControl(Object model, String style) {
				return new ValueDisplayControl((FormField) model, _renderer);
			}
		}

		/**
		 * Access to the top-level editor.
		 */
		@Container
		@Hidden
		@Name(EDIT_MODEL)
		EditModel getEditModel();

		@Override
		@DynamicMode(fun = HideImmutableIf.class, args = @Ref({ EDIT_MODEL, EditModel.CREATING }))
		boolean isOverride();

		@Override
		@DynamicMode(fun = HideImmutableIf.class, args = @Ref({ EDIT_MODEL, EditModel.CREATING }))
		String getFullQualifiedName();

		/**
		 * Not {@link #isOverride()}
		 */
		@Name(NEW_ATTRIBUTE)
		@Derived(fun = Not.class, args = @Ref(OVERRIDE))
		@Hidden
		boolean isNewAttribute();

		@Override
		@DynamicMode(fun = ActiveIf.class, args = @Ref({ EDIT_MODEL, EditModel.CREATING }))
		@RegexpConstraint(value = PartNameConstraints.RECOMMENDED_TYPE_PART_NAME_PATTERN, errorKey = PartNameConstraints.RecommendedTypePartNameKey.class, asWarning = true)
		@Constraint(value = TypePartNotExistsConstraint.class, args = { @Ref({ EDIT_MODEL, EditModel.CONTEXT_TYPE }),
			@Ref(OVERRIDE), @Ref({ EDIT_MODEL, EditModel.CREATING }) })
		public String getName();

		/**
		 * Whether it is allowed to change the collection properties.
		 */
		@Name(MULTIPLE_AND_NEW)
		@Derived(fun = And.class, args = { @Ref(MULTIPLE_PROPERTY), @Ref(NEW_ATTRIBUTE) })
		@Hidden
		boolean getMultipleAndNew();

		@Override
		@DynamicMode(fun = ActiveIf.class, args = { @Ref({ EDIT_MODEL, EditModel.CREATING }) })
		String getTypeSpec();

		/**
		 * The resolved version of {@link #getTypeSpec()}
		 */
		@Name(RESOLVED_TYPE)
		@InstanceFormat
		@Hidden
		@Derived(fun = ResolveType.class, args = { @Ref({ EDIT_MODEL, EditModel.CONTEXT_TYPE }), @Ref(TYPE_SPEC) })
		@CalledByReflection
		TLType getResolvedType();

		@Override
		@DynamicMode(fun = ActiveIf.class, args = { @Ref({ EDIT_MODEL, EditModel.CREATING }), @Ref(NEW_ATTRIBUTE) })
		public boolean isMultiple();

		@DynamicMode(fun = ActiveAndEnabledIf.class, args = { @Ref({ EDIT_MODEL, EditModel.CREATING }),
			@Ref(MULTIPLE_AND_NEW) })
		@Override
		boolean isOrdered();

		@DynamicMode(fun = ActiveAndEnabledIf.class, args = { @Ref({ EDIT_MODEL, EditModel.CREATING }),
			@Ref(MULTIPLE_AND_NEW) })
		@Override
		boolean isBag();

		/**
		 * Kind of this part model used to limit the set of annotations that can be applied.
		 */
		@Name(TYPE_KIND)
		@Derived(fun = ResolveTypeKind.class, args = @Ref(RESOLVED_TYPE))
		@Hidden
		TLTypeKind getTypeKind();

		/**
		 * Function resolving the {@link TLTypeKind} for a given {@link TLType}.
		 */
		class ResolveTypeKind extends Function1<TLTypeKind, TLType> {
			@Override
			public TLTypeKind apply(TLType type) {
				if (type != null) {
					return TLTypeKind.getTLTypeKind(type);
				}

				return null;
			}
		}

		/**
		 * Function resolving a textual type description in the context of some {@link TLModelPart}.
		 */
		class ResolveType extends Function2<TLType, TLModelPart, String> {
			@Override
			public TLType apply(TLModelPart contextType, String arg) {
				if (contextType == null || arg == null || arg.isBlank()) {
					return null;
				}
				try {
					return TLModelUtil.findType(contextType.getModel(), arg);
				} catch (TopLogicException ex) {
					return null;
				}
			}
		}

		/**
		 * {@link ValueConstraint} that checks that no {@link TLStructuredTypePart} with the
		 * configured name exists in the given {@link TLStructuredType}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		class TypePartNotExistsConstraint extends GenericValueDependency3<String, TLStructuredType, Boolean, Boolean> {

			/**
			 * Creates a {@link TypePartNotExistsConstraint}.
			 */
			public TypePartNotExistsConstraint() {
				super(String.class, TLStructuredType.class, Boolean.class, Boolean.class);
			}

			@Override
			protected void checkValue(PropertyModel<String> propertyModel, PropertyModel<TLStructuredType> typeModel,
					PropertyModel<Boolean> overrideModel, PropertyModel<Boolean> createModel) {
				Boolean isOverride = overrideModel.getValue();
				if (isOverride == null || isOverride.booleanValue()) {
					return;
				}
				Boolean isCreate = createModel.getValue();
				if (isCreate == null || !isCreate.booleanValue()) {
					return;
				}
				String newPartName = propertyModel.getValue();
				if (StringServices.isEmpty(newPartName)) {
					return;
				}
				TLStructuredType type = typeModel.getValue();
				if (type == null) {
					return;
				}
				TLStructuredTypePart existingPart = type.getPart(newPartName);
				if (existingPart != null) {
					TLStructuredType owner = existingPart.getOwner();
					ResKey error;
					if (owner == type) {
						error = I18NConstants.ERROR_PART_WITH_NAME_EXISTS__NAME_TYPE.fill(newPartName, owner);
					} else {
						error = I18NConstants.ERROR_PART_WITH_NAME_EXISTS__NAME_GENERALIZATION.fill(newPartName, owner);
					}
					propertyModel.setProblemDescription(error);
				}

			}

		}

	}

	/**
	 * Creates a {@link TLStructuredTypePartFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TLStructuredTypePartFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Class<? extends TLModelPart> getModelType() {
		if (getConfig().isCreate()) {
			return TLStructuredType.class;
		}
		return TLTypePart.class;
	}

	@Override
	protected Class<? extends EditModel> getFormType(Object contextModel) {
		return EditModel.class;
	}

	@Override
	protected void initFormModel(EditModel formModel, Object contextModel) {
		formModel.setCreating(getConfig().isCreate());
		if (contextModel != null) {
			if (getConfig().isCreate()) {
				formModel.setContextType((TLStructuredType) contextModel);
			} else {
				formModel.setContextType(((TLStructuredTypePart) contextModel).getOwner());
			}
		}
	}

	@Override
	protected void fillFormModel(EditModel formModel, TLModelPart businessModel) {
		switch (businessModel.getModelKind()) {
			case REFERENCE:
				TLReference reference = (TLReference) businessModel;

				if (TLModelUtil.getEndIndex(reference.getEnd()) == 0) {
					TLBackReferenceFormBuilder.initWithReference(formModel, reference);
				} else {
					TLReferenceFormBuilder.initWithReference(formModel, reference);
				}

				break;
			case PROPERTY:
				TLPropertyFormBuilder.initWithProperty(formModel, (TLProperty) businessModel);
				break;
			case CLASS:
			case ASSOCIATION:
				break;
			default:
				// Ignore.
		}
	}

	/**
	 * Initialisation of the {@link PartModel} with the help of the given
	 * {@link TLStructuredTypePart}.
	 */
	protected static void initWithPart(EditModel formModel, PartModel partModel, TLStructuredTypePart part) {
		formModel.setPartModel(partModel);

		partModel.setOverride(part.isOverride());
		partModel.setName(part.getName());
		partModel.setFullQualifiedName(TLModelUtil.qualifiedName(part));
		partModel.setTypeSpec(TLModelUtil.qualifiedName(part.getType()));

		ResKey key = TLModelI18N.getI18NKey(part);
		partModel.setLabel(key);
		partModel.setDescription(key.tooltip());

		for (TLAnnotation annotation : part.getAnnotations()) {
			TLAttributeAnnotation attributeAnnotation = (TLAttributeAnnotation) annotation;
			TLAttributeAnnotation annotationCopy = TypedConfiguration.copy(attributeAnnotation);
			partModel.getAnnotations().add(annotationCopy);
		}
		partModel.setBag(part.isBag());
		partModel.setMandatory(part.isMandatory());
		partModel.setMultiple(part.isMultiple());
		partModel.setOrdered(part.isOrdered());

		PropertyDescriptor nameProperty = formModel.descriptor().getProperty(PartModel.NAME);
		ConfigurationListener listener = new ConfigurationListener() {
			@Override
			public void onChange(ConfigurationChange change) {
				PartModel model = (PartModel) change.getModel();

				ResKey label = model.getLabel();
				if (label == null) {
					model.setLabel(
						ResKey.literal(ResKey.langString(Locale.ENGLISH, change.getNewValue().toString())));
				}
			}
		};
		partModel.addConfigurationListener(nameProperty, listener);

	}

}
