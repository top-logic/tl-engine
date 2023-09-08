/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Part of the configuration that is enhanced with the surrounding component later on.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface ComponentAware {
    public void setComponent(LayoutComponent aComponent);
}

