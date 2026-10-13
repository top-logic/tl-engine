/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.wrap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.core.wrap.WrapperTLElement;
import com.top_logic.element.layout.structured.I18NConstants;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.tool.boundsec.BoundObject;


/**
 * Wrapper based implementation of the interface {@link com.top_logic.element.structured.StructuredElement}.
 * 
 * This implementation will not call a commit on the knowledgebase, when changing
 * attributes or elements, this has to be done somewhere else.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class StructuredElementWrapper extends WrapperTLElement {

    /** 
     * The name of the associations, which link this objects together.
     * 
     * @since TL 5.6.1 (was protected before).
     */
	public static final String CHILD_ASSOCIATION = ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION;

    public StructuredElementWrapper(KnowledgeObject ko) {
        super(ko);
    }

    @Override
	public Optional<ResKey> tDeleteVeto() {
		return isRoot() ? Optional.of(I18NConstants.ERROR_ROOT_CANNOT_BE_DELETED) : super.tDeleteVeto();
    }

    @Override
	public Collection<? extends BoundObject> getSecurityChildren() {
		return (Collection<? extends BoundObject>) getChildren();
    }

    /**
     * Do NOT inherit roles from security parents.
     * 
     * @see com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper#getSecurityParents()
     */
    @Override
	public Collection<? extends BoundObject> getSecurityParents() {
		return Collections.emptyList();
    }

	/**
	 * Fill the children caches and load the children of all given parent objects.
	 */
	public static void preloadChildren(PreloadContext context, List<? extends TLObject> parents) {
		MetaElementUtil.preloadAttribute(context, parents, StructuredElementWrapper.CHILDREN_ATTR);
	}

}
