/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.state;

import java.util.Collections;
import java.util.List;

import com.top_logic.tool.execution.service.ConfiguredCommandApprovalService;

/**
 * Abstract view state. Each view state must supply two constructors which were used via java
 * reflection by the {@link ViewStateManager}, {@link #AbstractViewState()} and
 * {@link #AbstractViewState(List, List)}
 *
 * @author <a href="mailto:tgi@top-logic.com> </a>
 * 
 * @deprecated Use configuration of {@link ConfiguredCommandApprovalService}.
 */
@Deprecated
public abstract class AbstractViewState implements ViewState {

	/** A List of configured command names. */
	protected List<String> configuredCommands;

	/** A List of configured command group names. */
	protected List<String> configuredCommandGroups;


    /**
     * Creates a new AbstractViewState without any commands and command groups configured.
     */
    public AbstractViewState() {
		this.configuredCommands = Collections.emptyList();
		this.configuredCommandGroups = Collections.emptyList();
    }

	/**
	 * Creates a new AbstractViewState with the given lists of commands and command groups
	 * configured.
	 *
	 * @param configuredCommands
	 *        a configuration List of command names as hook for subclasses
	 * @param configuredCommandGroups
	 *        a configuration List of command group names as hook for subclasses
	 */
	public AbstractViewState(List<String> configuredCommands, List<String> configuredCommandGroups) {
		this.configuredCommands = (configuredCommands == null ? Collections.emptyList() : configuredCommands);
		this.configuredCommandGroups =
			(configuredCommandGroups == null ? Collections.emptyList() : configuredCommandGroups);
	}

	/**
	 * Creates a new AbstractViewState with the given list of command groups configured.
	 *
	 * @param configuredCommandGroups
	 *        a configuration List of command group names as hook for subclasses
	 */
	public AbstractViewState(List<String> configuredCommandGroups) {
		this.configuredCommands = Collections.emptyList();
		this.configuredCommandGroups =
			(configuredCommandGroups == null ? Collections.emptyList() : configuredCommandGroups);
	}

    @Override
	public List<String> getConfiguredCommands() {
        return configuredCommands;
    }

    @Override
	public List<String> getConfiguredCommandGroups() {
        return configuredCommandGroups;
    }

}
