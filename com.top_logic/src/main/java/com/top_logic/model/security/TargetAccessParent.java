/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.Step;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.resources.TLPartInOwnerResourceProvider;
import com.top_logic.model.security.SecurityConfigurationService.TLClassAccessRights;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link AccessParentDefinition} delegating to the object a to-one reference of an object points
 * to.
 */
@Label("Reference target")
public class TargetAccessParent extends AbstractConfiguredInstance<TargetAccessParent.Config> implements AccessParentDefinition {

	/**
	 * Configuration of {@link TargetAccessParent}.
	 */
	@TagName("target")
	public interface Config extends PolymorphicConfiguration<TargetAccessParent>, ConfigPart {

		/** Configuration name for {@link #getReference()}. */
		String REFERENCE = "reference";

		/** Configuration name for {@link #getOwner()}. */
		String OWNER = "owner";

		/**
		 * The to-one reference of the type whose target is the access parent, navigated forwards.
		 */
		@Name(REFERENCE)
		@Mandatory
		@Options(fun = AccessParentReferenceOptions.ToOneReferences.class, args = @Ref(steps = {
			@Step(OWNER),
			@Step(AccessParentConfig.RIGHTS),
			@Step(TLClassAccessRights.NAME_ATTRIBUTE) }), mapping = TLModelPartRef.PartMapping.class)
		@OptionLabels(TLPartInOwnerResourceProvider.class)
		TLModelPartRef getReference();

		/**
		 * Setter for {@link #getReference()}.
		 */
		void setReference(TLModelPartRef value);

		/**
		 * The setting this definition belongs to.
		 */
		@Name(OWNER)
		@Hidden
		@Container
		AccessParentConfig getOwner();
	}

	/**
	 * Creates a {@link TargetAccessParent} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TargetAccessParent(InstantiationContext context, Config config) {
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
		if (part instanceof TLReference reference && !reference.isMultiple()
			&& TLModelUtil.isCompatibleType(reference.getOwner(), type)) {
			return new TargetRelation(reference);
		}
		context.error("The access reference " + ref + " of " + typeName + " is not a to-one reference of the type.");
		return null;
	}

}
