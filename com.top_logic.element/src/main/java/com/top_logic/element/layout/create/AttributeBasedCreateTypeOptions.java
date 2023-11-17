/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.create;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.util.Utils.*;
import static com.top_logic.model.util.TLModelUtil.*;

import java.util.List;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.basic.LabelSorter;
import com.top_logic.layout.editor.config.TypeWithReferenceTemplateParameters.MultipleReferencesOfType;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link CreateTypeOptions} that computes the {@link TLClass TLClasses} that are subtypes of a
 * given attribute.
 * 
 * @tooltip Use the types that are subtypes of the type of a given attribute.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class AttributeBasedCreateTypeOptions
		extends AbstractConfiguredInstance<AttributeBasedCreateTypeOptions.Config>
		implements CreateTypeOptions {

	/** {@link ConfigurationItem} for the {@link AttributeBasedCreateTypeOptions}. */
	@DisplayOrder({ Config.OWNER, Config.ATTRIBUTE })
	public interface Config extends PolymorphicConfiguration<AttributeBasedCreateTypeOptions>, ConfigPart {

		/** Property name of {@link #getOwner()}. */
		String OWNER = "owner";

		/** Property name of {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/**
		 * The type on which the {@link #getAttribute() attribute} is defined.
		 * <p>
		 * This can also be a super type of a type that defines the attribute. That makes it
		 * possible to use this class in a layout template where the user specifies only a single
		 * type and that type is also used for computing the table configuration.
		 * </p>
		 * 
		 * @implNote This is used only for computing the options for {@link #getAttribute()}.
		 */
		@Mandatory
		@Name(OWNER)
		TLModelPartRef getOwner();

		/** The attribute whose value type should be computed. */
		@Mandatory
		@Options(fun = MultipleReferencesOfType.class, args = @Ref(OWNER), mapping = TLModelPartRef.PartMapping.class)
		@Name(ATTRIBUTE)
		TLModelPartRef getAttribute();

	}

	private final TLClass _attributeOwner;

	private final TLStructuredTypePart _attribute;

	/** {@link TypedConfiguration} constructor for {@link AttributeBasedCreateTypeOptions}. */
	public AttributeBasedCreateTypeOptions(InstantiationContext context, Config config) {
		super(context, config);
		_attribute = resolveAttribute(context, config);
		/* It is not possible to reuse the type specified in the configuration, as it might not
		 * declare this attribute, as it might be declared on one of its subtypes. That makes it
		 * possible to define a tree table with just one type parameter. Example: A table that
		 * displays a tree. The table configuration needs to get the common super type of all types
		 * that can appear in the table. But that common super type might not define the attribute
		 * by which this tree is created. Because some types in the tree could be "leafs": They
		 * should not have children nodes and therefore don't have the attribute that defines the
		 * tree. Therefore, the common super type cannot have this attribute either. */
		_attributeOwner = (TLClass) _attribute.getOwner();
	}

	private TLStructuredTypePart resolveAttribute(InstantiationContext context, Config config) {
		TLTypePart attribute = config.getAttribute().resolvePart();
		if (!(attribute instanceof TLStructuredTypePart)) {
			context.error("Attribute is not an instance of " + TLStructuredTypePart.class.getSimpleName() + ": "
				+ debug(attribute));
		}
		return (TLStructuredTypePart) attribute;
	}

	@Override
	public boolean supportsContext(Object contextModel) {
		if (!CreateTypeOptions.super.supportsContext(contextModel)) {
			return false;
		}
		if (!(contextModel instanceof TLObject)) {
			return false;
		}
		TLObject contextTLObject = (TLObject) contextModel;
		TLStructuredType contextType = contextTLObject.tType();
		if (!(contextType instanceof TLClass)) {
			return false;
		}
		if (!isCompatibleType(getAttributeOwner(), contextType)) {
			return false;
		}
		/* Resolve the attribute on the concrete context model, as the context model can be an
		 * instance of a subtype which overrides the attribute and specializes the type. */
		TLStructuredTypePart subtypeAttribute = contextType.getPart(getAttribute().getName());
		if (subtypeAttribute == null) {
			return false;
		}
		if (subtypeAttribute.isDerived()) {
			return false;
		}
		TLType attributeType = subtypeAttribute.getType();
		if (!(attributeType instanceof TLClass)) {
			return false;
		}
		return !getConcreteSpecializations((TLClass) attributeType).isEmpty();
	}

	@Override
	public List<TLClass> getPossibleTypes(Object contextModel) {
		if (!supportsContext(contextModel)) {
			return List.of();
		}
		TLClass attributeType = resolveAttributeTypeOnModel(contextModel);
		List<TLClass> result = list(getConcreteSpecializations(attributeType));
		LabelSorter.sortByLabelInline(result, MetaLabelProvider.INSTANCE);
		return result;
	}

	private TLClass getAttributeOwner() {
		return _attributeOwner;
	}

	private TLStructuredTypePart getAttribute() {
		return _attribute;
	}

	@Override
	public TLClass getDefaultType(Object contextModel, List<TLClass> typeOptions) {
		if (typeOptions.isEmpty()) {
			return null;
		}
		if (typeOptions.size() == 1) {
			return typeOptions.get(0);
		}
		TLClass attributeType = resolveAttributeTypeOnModel(contextModel);
		if (!attributeType.isAbstract()) {
			return attributeType;
		}
		return CreateTypeOptions.super.getDefaultType(contextModel, typeOptions);
	}

	/**
	 * Resolve the attribute on the concrete model.
	 * <p>
	 * The context model can be an instance of a subtype which overrides the attribute and
	 * specializes the type. It is therefore necessary to resolve the attribute on the model and not
	 * just take the configured attribute.
	 * </p>
	 */
	protected TLClass resolveAttributeTypeOnModel(Object model) {
		/* No need to check the casts or anything: 'supportsContext' did that already. */
		TLObject contextTLObject = (TLObject) model;
		TLClass contextType = (TLClass) contextTLObject.tType();
		TLStructuredTypePart subtypeAttribute = contextType.getPartOrFail(getAttribute().getName());
		return (TLClass) subtypeAttribute.getType();
	}

}
