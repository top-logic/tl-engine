/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution;

import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.element.layout.meta.ConfiguredAttributedTooltipProvider;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ProcessExecutionResourceProvider extends ConfiguredAttributedTooltipProvider {

	@Override
	public String getLabel(Object anObject) {
		ProcessExecution processExecution = (ProcessExecution) anObject;
		Process process = processExecution.getProcess();
		if (process == null) {
			return "?";
		}
		Collaboration collaboration = process.getCollaboration();
		Participant participant = process.getParticipant();
		return (collaboration == null ? "?" : collaboration.getName()) + " - "
			+ (participant == null ? "?" : participant.getName());
	}

	@Override
	public ThemeImage getImage(Object object, Flavor flavor) {
		ProcessExecution processExecution = (ProcessExecution) object;
		return processExecution.getProcess().getCollaboration().getIcon();
	}
}
