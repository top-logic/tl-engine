/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person;

import com.top_logic.contact.layout.PersonContactLabelProvider;
import com.top_logic.knowledge.gui.WrapperResourceProvider;

/**
 * The PersonContactResourceProvider
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class PersonContactResourceProvider extends WrapperResourceProvider {

    @Override
	public String getLabel(Object aObject) {
        return PersonContactLabelProvider.INSTANCE.getLabel(aObject);
    }
}

