/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.model.tickets;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.kbbased.storage.AbstractDerivedStorage;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.TracingScope;

/**
 * Derived headline of a {@link Ticket}: its name followed by the label of its status in
 * parentheses.
 *
 * <p>
 * The status label is itself a derived attribute computed by a script. A form computes the
 * headline of the ticket it edits in a {@link TracingScope}: the storage receives the ticket
 * itself, its reads are reported as dependencies of the headline field and answered from the values
 * entered in the form. Selecting another status therefore changes the status label and, through
 * it, the headline before anything is saved.
 * </p>
 *
 * @implNote The storage reads the ticket through the generated getters {@link Ticket#getName()}
 *           and {@link Ticket#getStatusLabel()}.
 */
public class HeadlineStorage<C extends AbstractDerivedStorage.Config<?>> extends AbstractDerivedStorage<C> {

	/**
	 * Creates a {@link HeadlineStorage} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public HeadlineStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) {
		Ticket ticket = (Ticket) object;
		return ticket.getName() + " (" + ticket.getStatusLabel() + ")";
	}

}
