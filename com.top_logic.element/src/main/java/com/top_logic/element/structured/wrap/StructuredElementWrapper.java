/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.wrap;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.core.TLElementVisitor;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.wrap.WrapperTLElement;
import com.top_logic.element.layout.structured.I18NConstants;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.tool.boundsec.BoundObject;


/**
 * Wrapper based implementation of the interface {@link com.top_logic.element.structured.StructuredElement}.
 * 
 * This implementation will not call a commit on the knowledgebase, when changing
 * attributes or elements, this has to be done somewhere else. The internal
 * caching of parent and children can be deactivated by changing the constant
 * {@link WrapperTLElement#USE_CACHE}. 
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
     * @see com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper#getSecurityParent()
     */
    @Override
	public BoundObject getSecurityParent() {
        return null;
    }

    /**
     * Return the factory for this kind of elements.
     *
     * @return    The factory for wrapper elements. 
     */
	protected StructuredElementWrapperFactory getFactory() {
		return (StructuredElementWrapperFactory) DynamicModelService.getFactoryFor(getStructureName());
    }

    /**
     * @param    aVisitor    The visitor to be used for visiting.
     * @return   <code>false</code>, if traversing has been finished by the
     *           given visitor, not the depth.
     */
    protected boolean traverseUp(TLElementVisitor aVisitor) {
        boolean theResult = aVisitor.onVisit(this, TraversalFactory.ANCESTORS);

        if (theResult) {
            StructuredElement theParent = this.getParent();

            if (theParent != null) {
                theResult = TraversalFactory.traverse(theParent, aVisitor, TraversalFactory.ANCESTORS);
            }
        }
        
        return (theResult);
    }

	/**
	 * Fill the children caches and load the children of all given parent objects.
	 */
	public static void preloadChildren(PreloadContext context, List<? extends TLObject> parents) {
		MetaElementUtil.preloadAttribute(context, parents, StructuredElementWrapper.CHILDREN_ATTR);
	}

	/**
	 * Fill the parent caches and load the parents of all given child objects.
	 */
	public static void preloadParents(PreloadContext context, List<? extends TLObject> children) {
		MetaElementUtil.preloadAttribute(context, children, StructuredElementWrapper.PARENT_ATTR);
	}

}
