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
import com.top_logic.basic.config.annotation.Nullable;
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
 * {@link AccessParentDefinition} delegating to the container holding an object, through whichever
 * composition or through the configured one.
 */
@Label("Container")
public class ContainerAccessParent extends AbstractConfiguredInstance<ContainerAccessParent.Config> implements AccessParentDefinition {

	/**
	 * Configuration of {@link ContainerAccessParent}.
	 */
	@TagName("container")
	public interface Config extends PolymorphicConfiguration<ContainerAccessParent>, ConfigPart {

		/** Configuration name for {@link #getReference()}. */
		String REFERENCE = "reference";

		/** Configuration name for {@link #getOwner()}. */
		String OWNER = "owner";

		/**
		 * The composition holding objects of the type, navigated backwards: the container is the
		 * access parent only when it holds the object through this composition. Without one,
		 * whichever composition holds the object leads to the access parent.
		 */
		@Name(REFERENCE)
		@Nullable
		@Options(fun = AccessParentReferenceOptions.Compositions.class, args = @Ref(steps = {
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
	 * Creates a {@link ContainerAccessParent} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ContainerAccessParent(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public AccessParentFunction resolve(InstantiationContext context, TLClass type) {
		TLModelPartRef ref = getConfig().getReference();
		if (ref == null) {
			return ContainerRelation.ANY;
		}
		String typeName = TLModelUtil.qualifiedName(type);
		TLModelPart part = AccessParentReferenceOptions.resolve(context, ref, typeName);
		if (part == null) {
			return null;
		}
		if (part instanceof TLReference reference && reference.isComposite()
			&& TLModelUtil.isCompatibleType(reference.getType(), type)) {
			return new ContainerRelation(reference, true);
		}
		context.error("The access reference " + ref + " of " + typeName
			+ " is not a composition holding objects of the type.");
		return null;
	}

}
