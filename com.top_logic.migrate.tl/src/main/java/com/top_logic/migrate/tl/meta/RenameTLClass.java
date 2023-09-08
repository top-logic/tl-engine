/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.EventRewriterProxy;
import com.top_logic.knowledge.event.convert.StackedEventRewriter;
import com.top_logic.knowledge.event.convert.TLTypeRenaming;
import com.top_logic.knowledge.service.db2.migration.formats.DumpValueSpec;
import com.top_logic.knowledge.service.db2.migration.formats.ValueType;
import com.top_logic.knowledge.service.db2.migration.rewriters.AttributeRewriter;
import com.top_logic.knowledge.service.db2.migration.rewriters.ChangeValue;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;

/**
 * {@link EventRewriterProxy} rewriting the names of {@link TLClass}.
 * 
 * @deprecated Use {@link TLTypeRenaming}: {@link RenameTLClass} does not care about the
 *             {@link TLModule} of the class to rename.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Deprecated
public class RenameTLClass extends EventRewriterProxy<RenameTLClass.Config> {

	/**
	 * Typed configuration interface definition for {@link RenameTLClass}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends EventRewriterProxy.Config<RenameTLClass> {

		/**
		 * Mapping of the original {@link TLClass} name to the new {@link TLClass} name.
		 */
		@MapBinding(key = "old-value", attribute = "new-value")
		Map<String, String> getMappings();

	}

	/**
	 * Create a {@link RenameTLClass}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public RenameTLClass(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected EventRewriter createImplementation(InstantiationContext context, Config config) {
		List<EventRewriter> rewriters = new ArrayList<>();
		for (Entry<String, String> type : config.getMappings().entrySet()) {
			AttributeRewriter.Config typeConfig = newRewriter(type.getKey(), type.getValue());
			EventRewriter instance = context.getInstance(typeConfig);
			if (instance != null) {
				rewriters.add(instance);
			} else {
				context.error("Unable to create rewriter to map '" + type.getKey() + "' to '" + type.getValue() + "'.");
			}
		}
		return StackedEventRewriter.getRewriter(rewriters);
	}

	private static AttributeRewriter.Config newRewriter(String oldValue, String newValue) {
		AttributeRewriter.Config rewriter = TypedConfiguration.newConfigItem(AttributeRewriter.Config.class);
		rewriter.setImplementationClass(AttributeRewriter.class);
		rewriter.setTypeNames(Collections.singleton(ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE));
		rewriter.setAlgorithm(newRewriteAlgorithm(oldValue, newValue));
		return rewriter;
	}

	private static ChangeValue.Config newRewriteAlgorithm(String oldValue, String newValue) {
		ChangeValue.Config algorithm = TypedConfiguration.newConfigItem(ChangeValue.Config.class);
		algorithm.setImplementationClass(ChangeValue.class);
		algorithm.setAttribute(ApplicationObjectUtil.META_ELEMENT_ME_TYPE_ATTR);
		algorithm.setOldValueSpec(new DumpValueSpec(ValueType.STRING, oldValue));
		algorithm.setNewValueSpec(new DumpValueSpec(ValueType.STRING, newValue));
		return algorithm;
	}

}

