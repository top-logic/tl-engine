/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLClass;

/**
 * {@link AccessParentDefinition} of a type deciding for itself through its grants and the roles
 * users hold on its objects. It switches off the default of a composition part and an access
 * parent inherited from a generalization.
 */
@Label("Own decision")
public class SelfAccessParent extends AbstractConfiguredInstance<SelfAccessParent.Config>
		implements AccessParentDefinition {

	/**
	 * Configuration of {@link SelfAccessParent}.
	 */
	@TagName("self")
	public interface Config extends PolymorphicConfiguration<SelfAccessParent> {
		// No properties.
	}

	/**
	 * Creates a {@link SelfAccessParent} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public SelfAccessParent(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * @return Always <code>null</code>: the type does not delegate.
	 */
	@Override
	public AccessParentFunction resolve(InstantiationContext context, TLClass type) {
		return null;
	}

}
