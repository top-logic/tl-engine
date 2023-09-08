/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.form;

import com.top_logic.basic.config.InstantiationContext;

/**
 * The DeleteSimpleWrapperWithLogEventCommandHandler just deletes a Wrapper.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 * 
 * @deprecated Use {@link DeleteSimpleWrapperCommandHandler}.
 */
@Deprecated
public class DeleteSimpleWrapperWithLogEventCommandHandler extends DeleteSimpleWrapperCommandHandler {

    public static final String COMMAND = "DeleteSimpleWrapperWithLogEvent";

    public DeleteSimpleWrapperWithLogEventCommandHandler(InstantiationContext context, Config config) {
        super(context, config);
    }

}
