/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.EventRewriterProxy;
import com.top_logic.knowledge.event.convert.StackedEventRewriter;
import com.top_logic.knowledge.event.convert.TypesConfig;
import com.top_logic.knowledge.service.db2.migration.formats.DumpValueSpec;
import com.top_logic.knowledge.service.db2.migration.formats.ValueType;
import com.top_logic.migrate.tl.attribute.SetInitialAttributeValue;

/**
 * {@link EventRewriterProxy} setting the static type of an dynamic object also as dynamic type.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SetStaticAsDynamicType extends EventRewriterProxy<SetStaticAsDynamicType.Config> {

	/**
	 * Typed configuration interface definition for {@link SetStaticAsDynamicType}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends EventRewriterProxy.Config<SetStaticAsDynamicType>, TypesConfig {
		// configuration interface definition
	}

	/**
	 * Create a {@link SetStaticAsDynamicType}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SetStaticAsDynamicType(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected EventRewriter createImplementation(InstantiationContext context, Config config) {
		List<EventRewriter> rewriters = new ArrayList<>();
		for (String type : config.getTypeNames()) {
			SetInitialAttributeValue.Config typeConfig =
				TypedConfiguration.newConfigItem(SetInitialAttributeValue.Config.class);
			typeConfig.setImplementationClass(SetInitialAttributeValue.class);

			typeConfig.setTypeNames(Collections.singleton(type));
			typeConfig.setInitialValues(Collections.singletonMap(ConcreteTypeComputation.ELEMENT_NAME_ATTRIBUTE,
				new DumpValueSpec(ValueType.STRING, type)));

			EventRewriter instance = context.getInstance(typeConfig);
			if (instance != null) {
				rewriters.add(instance);
			}
		}
		return StackedEventRewriter.getRewriter(rewriters);
	}

}

