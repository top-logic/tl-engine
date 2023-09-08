/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * Holder class for parameter used in migration
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MigrateParameters {

	final Protocol protocol;

	final String srcKBName;

	final String targetKBName;

	BasicRuntimeModule<?>[] neededModules = BasicRuntimeModule.NO_MODULES;

	List<? extends LazyEventRewriter> rewriters = Collections.emptyList();

	Collection<String> branchedTypes = null;

	Mapping<String, String> typeNameConversion = Mappings.identity();

	/**
	 * Creates a {@link MigrateParameters}.
	 * 
	 * @param protocol
	 *        some {@link Protocol} to notify messages
	 * @param srcKBName
	 *        the name of the source {@link KnowledgeBase} to get events from
	 * @param targetKBName
	 *        the name of the {@link KnowledgeBase} to write events to
	 */
	public MigrateParameters(Protocol protocol, String srcKBName, String targetKBName) {
		this.protocol = protocol;
		this.srcKBName = srcKBName;
		this.targetKBName = targetKBName;
	}

	/**
	 * Sets the modules needed to start {@link MigrateUtils#migrate(MigrateParameters)}. The
	 * {@link PersistencyLayer} is started additionally.
	 * 
	 * @param neededModules
	 *        must not be <code>null</code>.
	 */
	public void setNeededModules(BasicRuntimeModule<?>[] neededModules) {
		this.neededModules = neededModules;
	}

	/**
	 * Sets the additional {@link EventRewriter} (in lazy form) to use during migration
	 * 
	 * @param rewriters
	 *        must neither be <code>null</code> nor contain <code>null</code>
	 */
	public void setRewriters(List<? extends LazyEventRewriter> rewriters) {
		this.rewriters = rewriters;
	}

	/**
	 * Sets the types which must be branched automatically at each branch event
	 */
	public void setAdditionalBranchTypes(Collection<String> branchedTypes) {
		this.branchedTypes = branchedTypes;
	}

	/**
	 * Mapping of the name of the types in the source {@link KnowledgeBase} to the name of the
	 * corresponding type in the target {@link KnowledgeBase}.
	 */
	public void setTypeNameConversion(Mapping<String, String> typeNameConversion) {
		this.typeNameConversion = typeNameConversion;
	}

}
