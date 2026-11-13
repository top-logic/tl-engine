/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.events;

import java.util.List;

import com.top_logic.basic.func.Function0;

/**
 * The ids of the tabs of the event detail ({@code tl-ai-demo/events/event-detail.view.xml}), as
 * options of a configuration property.
 *
 * <p>
 * The allowed values of the parameter of the agent command that opens one of these tabs
 * ({@code tl-chat/albert.view.xml}): {@code <options fun="com.top_logic.ai.demo.events.EventDetailTabs"/>}.
 * </p>
 */
public class EventDetailTabs extends Function0<List<String>> {

	@Override
	public List<String> apply() {
		return List.of("overview", "sessions", "registrations", "sponsorships", "invoices");
	}

}
