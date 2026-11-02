/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.coverage;

import java.util.List;

/**
 * Source of the {@link OperationUse}s of the application that the {@link SecurityCoverageAnalysis}
 * checks against the operations declared for the types.
 *
 * <p>
 * The places checking an operation are defined in layers above the access definition - the views,
 * the configured TL-Script functions - so a module defining such places contributes a provider to
 * the {@link SecurityCoverageCheck.Config#getOperationUses() operation uses} of the
 * {@link SecurityCoverageCheck}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface OperationUseProvider {

	/**
	 * The places of the application checking an operation, in the order a developer reads them.
	 *
	 * <p>
	 * A provider may compute the result lazily and keep it until its sources change. A source it
	 * cannot read is left out, it does not fail the analysis.
	 * </p>
	 */
	List<OperationUse> getOperationUses();

}
