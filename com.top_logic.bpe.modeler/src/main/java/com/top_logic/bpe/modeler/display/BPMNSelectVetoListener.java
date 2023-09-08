/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.display;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.VetoListener;

/**
 * {@link VetoListener} to prevent selecting a new GUI element in a BPMN diagram.
 * 
 * <p>
 * A {@link BPMNSelectVetoListener} can react on the GUI side change of a selection and throw a
 * {@link VetoException} when changing the selection should not be possible for some reasons.
 * </p>
 * 
 * @see BPMNDisplay#addSelectVetoListener(BPMNSelectVetoListener)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface BPMNSelectVetoListener extends VetoListener {

	/**
	 * Checks whether the new selection of the given element in the given {@link BPMNDisplay} is
	 * allowed.
	 * 
	 * @param bpmn
	 *        The {@link BPMNDisplay} in which the selection should be changed.
	 * @param newSelection
	 *        The newly selected BPMN element.
	 * @throws VetoException
	 *         Iff this listener does not allow changing the selection of the given element.
	 */
	void checkVeto(BPMNDisplay bpmn, Object newSelection) throws VetoException;

}

