/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person.related;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.gui.layout.person.NewPersonCommandHandler;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Relate new Persons with a {@link PersonContact} or create a new one if needed.
 *
 * This implies that<ul>
 *   <li>the ContactPersonManager is configured. This class will
 *       not complain in this case.</li>
 *   <li>This command is part of a NewRelatedPersonComponent or a compatible replacement</li>
 *   <li>The Page "NewRelatedPerson.jsp"  or a compatible replacement is used </li>
 * </ul>
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class NewRelatedPersonCommandHandler extends NewPersonCommandHandler {

	/**
	 * Creates a new {@link NewRelatedPersonCommandHandler}.
	 */
	public NewRelatedPersonCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			Map arguments) {
		return super.createObject(component, createContext, formContainer, arguments);
	}

}
