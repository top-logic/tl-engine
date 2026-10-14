/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.wrap;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.AbstractWrapper;

/**
 * Base class of persistent objects that are {@link com.top_logic.tool.boundsec.BoundObject}s.
 * 
 * <p>
 * The security behavior is defined by {@link BoundWrapper}.
 * </p>
 *
 * @author <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public abstract class AbstractBoundWrapper extends AbstractWrapper implements BoundWrapper {

	/** Constructor */
    public AbstractBoundWrapper(KnowledgeObject ko) {
        super(ko);
    }

}
