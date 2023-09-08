/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Named;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * Basic implementation of the container wrapper interface.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class AbstractContainerWrapper extends AbstractBoundWrapper implements ContainerWrapper {

	/**
     * Construct an instance wrapped around the specified
     * {@link com.top_logic.knowledge.objects.KnowledgeObject}.
     *
     * This CTor is only for the WrapperFactory! <b>DO NEVER USE THIS
     * CONSTRUCTOR!</b> Use always the getInstance() method of the wrappers.
     *  
     * @param    ko    The KnowledgeObject, must never be <code>null</code>.
     */
    public AbstractContainerWrapper(KnowledgeObject ko) {
        super(ko);
    }

	@Override
	public boolean add(TLObject newChild) {
		if (this.contains(newChild)) {
			return false;
		}

		return (this._add(newChild));
    }

	@Override
	public boolean remove(TLObject oldChild) {
		if (this.contains(oldChild)) {
			return (this._remove(oldChild));
        }
        else {
            if (Logger.isDebugEnabled (this)) {
				Logger.debug("Object '" + oldChild + "' not in container!", this);
            }

            return false;
        }
    }

	@Override
	public boolean contains(TLObject content) {
		return getContent().contains(content);
    }

    @Override
	public boolean isEmpty() {
		return getContent().isEmpty();
    }

    @Override
	public int getContentSize() {
		return getContent().size();
    }

	/**
	 * All {@link #getContent()} which are of the given class and its sub-classes.
	 * 
	 * @param aClass
	 *        The class to filter contents with.
	 * @return All contents that is assignment compatible to the given class.
	 */
	public <T> List<T> getContentOfClass(Class<T> aClass) {
		List<T> result = new ArrayList<>();
		for (TLObject content : this.getContent()) {
			if (aClass.isInstance(content)) {
				@SuppressWarnings("unchecked")
				T matchingContent = (T) content;

				result.add(matchingContent);
			}
		}

		return result;
	}

	/**
	 * Find a member of this container with the given {@link Named#getName()}.
	 * 
	 * @param name
	 *        The name of the member to be found.
	 * @return a member of this container with the given name, or <code>null</code>, if no such
	 *         member exists. If this container contains more than one member with the given name,
	 *         an arbitrary one is returned.
	 * 
	 * @deprecated Result is not well-defined. The application must not depend on the uniqueness of
	 *             names, since this cannot be guaranteed under all circumstances.
	 */
	@Deprecated
	public TLObject getChildByName(String name) {
		for (TLObject content : getContent()) {
			if (content instanceof Named && name.equals(((Named) content).getName())) {
				return content;
			}
        }
		return null;
    }

	/**
	 * Checks, whether the folder has a child document or not with the given name or not
	 * 
	 * @return Whether {@link #getChildByName(String)} is non <code>null</code>.
	 */
	public boolean hasChild(String name) {
		for (TLObject content : getContent()) {
			if (content instanceof Named && name.equals(((Named) content).getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Appends the given object to the wrapped.
	 * 
	 * One object can be added to the board only once, so if this method has been called twice with
	 * the same object, it returns false.
	 * 
	 * @param newChild
	 *        The new object for the wrapped object.
	 * @return true, if appending the object succeeds.
	 */
	protected abstract boolean _add(TLObject newChild);

	/**
	 * Removes the given object from the wrapped container.
	 * 
	 * @param oldChild
	 *        The object to be removed.
	 * @return true, if removing the object succeeds.
	 */
	protected abstract boolean _remove(TLObject oldChild);

}
