/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * The run limits section as it was before the agent services were split: the capacity of
 * {@code AgentRuntimeService} and the budget of {@code AgentPolicyService} in one element.
 *
 * <p>
 * Only read where an old configuration has to be recognised, and as the one item a test hands to
 * the run limit settings in place of both sections.
 * </p>
 *
 * @see RunLimitSettings#override(RunLimitsConfig)
 * @see CapacityConfig
 * @see BudgetConfig
 */
@TagName("run-limits")
@DisplayOrder({
	CapacityConfig.CHAT_WORKERS,
	CapacityConfig.CHAT_QUEUE,
	CapacityConfig.CONCURRENT_RUNS_PER_ACCOUNT,
	BudgetConfig.TOKEN_BUDGET,
	BudgetConfig.MONEY_BUDGET,
	BudgetConfig.CURRENCY,
	BudgetConfig.EXCHANGE_RATE,
	BudgetConfig.BUDGET_WINDOW_HOURS,
	BudgetConfig.ROLE_BUDGETS,
})
public interface RunLimitsConfig extends CapacityConfig, BudgetConfig {
	// The union of both sections.
}
