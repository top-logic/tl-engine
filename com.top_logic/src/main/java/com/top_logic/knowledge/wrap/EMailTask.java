/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * Generic Task to send EMails to some List of useres.
 * 
 * In addition to "normal" Task a EMailTask can be disabled.
 * 
 * nice to have: We might save an Attachment in the PhysicalResource
 * (which is not supported, yet).
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 * @deprecated No longer supported, left-over to prevent data migration.
 */
@Deprecated
public class EMailTask extends TaskWrapper {

    /**
     * CTor used to support creation by WrapperFactory.
     */
    public EMailTask(KnowledgeObject ko) {
        super(ko);

		throw new UnsupportedOperationException("Legacy e-mail tasks no longer supported.");
    }

}
