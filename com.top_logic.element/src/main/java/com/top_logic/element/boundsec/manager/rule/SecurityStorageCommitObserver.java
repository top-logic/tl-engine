/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.util.Map;

import com.top_logic.basic.TLID;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * The SecurityStorageCommitObserver is called when the {@link SecurityManager} handles 
 * the changes to the security due to a commit to the knowledge base;
 * it provides additional {@link BoundObject}s added or removed within the current transaction
 * which are not yet added by the knowledgebase itself. 
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface SecurityStorageCommitObserver extends ConfiguredInstance<SecurityStorageCommitObserver.Config> {

	/**
	 * {@link TypedConfiguration} interface for the {@link SecurityStorageCommitObserver}.
	 */
	public interface Config extends NamedConfiguration, PolymorphicConfiguration<SecurityStorageCommitObserver> {
		// Nothing needed
	}

	/**
	 * Calculate additional {@link BoundObject}s added within the current transaction
	 * 
	 * @param someChanged       the changed objects as calculated by the knowledge base
	 * @param someNew           the new objects as calculated by the knowledge base
	 * @param someRemoved       the removed objects as calculated by the knowledge base
	 * @param aHandler          the {@link CommitHandler} for the current context
	 * @return some additional {@link BoundObject}, never <code>null</code>
	 */
    public Map<TLID, BoundObject> getAdded(Map<TLID, Object> someChanged, Map<TLID, Object> someNew, Map<TLID, Object> someRemoved, CommitHandler aHandler);

    /**
	 * Calculate additional {@link BoundObject}s removed within the current transaction
	 * 
	 * @param someChanged       the changed objects as calculated by the knowledge base
     * @param someNew           the new objects as calculated by the knowledge base
     * @param someRemoved       the removed objects as calculated by the knowledge base
     * @param aHandler          the {@link CommitHandler} for the current context
	 * @return some additional {@link BoundObject}, never <code>null</code>
	 */
    public Map<TLID, BoundObject> getRemoved(Map<TLID, Object> someChanged, Map<TLID, Object> someNew, Map<TLID, Object> someRemoved, CommitHandler aHandler);
}

