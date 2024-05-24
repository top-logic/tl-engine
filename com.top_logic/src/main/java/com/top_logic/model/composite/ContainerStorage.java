/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.composite;

/**
 * Description of a storage strategy for the container of a composite reference.
 * 
 * @see SourceTable
 * @see TargetTable
 * @see LinkTable
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ContainerStorage {

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

}
