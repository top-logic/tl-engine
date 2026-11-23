/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.resources.TLPartInOwnerResourceProvider;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link AccessParentDefinition} delegating to the objects that point to an object through a
 * reference, of any cardinality.
 *
 * <p>
 * Each referring object is an access parent of its own. Only committed referring objects count, an
 * object created in the current transaction gives no verdict. A referring object whose type is not
 * access controlled (without security, or not holding roles at all) opens the object to everybody.
 * </p>
 *
 * @see RefererRelation
 */
@Label("Referring objects")
public class RefererAccessParent extends AbstractConfiguredInstance<RefererAccessParent.Config>
		implements AccessParentDefinition {

	/**
	 * Configuration of {@link RefererAccessParent}.
	 */
	@TagName("referer")
	@DisplayOrder({ Config.REFERENCE, OperationRestriction.OPERATIONS })
	public interface Config extends PolymorphicConfiguration<RefererAccessParent>, OperationRestriction {

		/** Configuration name for {@link #getReference()}. */
		String REFERENCE = "reference";

		/**
		 * The reference pointing to objects of the type, navigated backwards: the objects holding
		 * an object of the type in this reference are its access parents.
		 */
		@Name(REFERENCE)
		@Mandatory
		@Options(fun = AccessParentReferenceOptions.Referers.class, args = @Ref(OWNER),
			mapping = TLModelPartRef.PartMapping.class)
		@OptionLabels(TLPartInOwnerResourceProvider.class)
		TLModelPartRef getReference();

		/**
		 * Setter for {@link #getReference()}.
		 */
		void setReference(TLModelPartRef value);
	}

	/**
	 * Creates a {@link RefererAccessParent} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public RefererAccessParent(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public AccessParentFunction resolve(InstantiationContext context, TLClass type) {
		TLModelPartRef ref = getConfig().getReference();
		String typeName = TLModelUtil.qualifiedName(type);
		TLModelPart part = AccessParentReferenceOptions.resolve(context, ref, typeName);
		if (part == null) {
			return null;
		}
		if (part instanceof TLReference reference && TLModelUtil.isCompatibleType(reference.getType(), type)) {
			return new RefererRelation(reference);
		}
		context.error("The access reference " + ref + " of " + typeName
			+ " does not point to objects of the type.");
		return null;
	}

}
