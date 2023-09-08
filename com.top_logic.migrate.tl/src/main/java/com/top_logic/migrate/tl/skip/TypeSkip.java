/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.skip;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.event.ItemCreation;
import com.top_logic.knowledge.event.convert.TypesConfig;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * {@link SkipFilter} which skips all events of a certain type.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TypeSkip extends AbstractConfiguredInstance<TypeSkip.Config> implements SkipFilter {

	/**
	 * Configuration options for {@link TypeSkip}.
	 */
	public interface Config extends PolymorphicConfiguration<TypeSkip>, TypesConfig {

		// Pure sum interface

	}
	
	private Set<String> skippedTypes;

	/**
	 * Creates a {@link TypeSkip} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TypeSkip(InstantiationContext context, Config config) {
		super(context, config);
		skippedTypes = new HashSet<>(config.getTypeNames());
	}

	/**
	 * Creates a {@link TypeSkip} configuration with the given types to skip.
	 * 
	 * @param types
	 *        See {@link TypeSkip.Config#getTypeNames()}.
	 */
	public static TypeSkip.Config skip(String... types) {
		Config config = TypedConfiguration.newConfigItem(TypeSkip.Config.class);
		HashSet<String> typesSet = new HashSet<>();
		Collections.addAll(typesSet, types);
		config.setTypeNames(typesSet);
		return config;
	}

	@Override
	public boolean skipEvent(ItemCreation event, Set<ObjectBranchId> skippedObjects) {
		String typeName = event.getObjectType().getName();
		if (skippedTypes.contains(typeName)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TypeSkip [skippedTypes=").append(this.skippedTypes).append("]");
		return builder.toString();
	}

}

