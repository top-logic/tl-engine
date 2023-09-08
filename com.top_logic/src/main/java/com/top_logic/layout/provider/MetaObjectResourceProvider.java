/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.dob.MetaObject;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * Resource Provider for MetaObjects.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class MetaObjectResourceProvider extends DefaultResourceProvider {

    @Override
    public String getLabel(Object object) {
        return object instanceof MetaObject ? ((MetaObject)object).getName() : super.getLabel(object);
    }

}
