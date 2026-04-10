/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;

/**
 * One node in a role rule path
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class PathElement {

    /** the meta attribute defining the content */
	private TLReference _reference;
    
    /** 
     * indicates that the attribute is to be resolved in revers,
     * i.e. get all objects that hold a given object via the given meta attribute.
     */
    private boolean       inverse;

    /**
     * Constructor
     */
	public PathElement(TLReference reference, boolean isInvers) {
		_reference = reference;
		if (reference == null) {
			// Special hack for IdentityPathElement!
		} else {
			if (_reference.getDefinition() != _reference) {
				throw new IllegalArgumentException(
					"Only the definition of an TLReference must be given: " + _reference);
			}
		}
        this.inverse       = isInvers;
        // TODO TSA: add consistency checks: type of attribute, ...
    }
    
    /**
     * Getter
     */
    public TLStructuredTypePart getMetaAttribute() {
		return (_reference);
    }
    
    /**
     * Getter
     */
    public boolean isInverse() {
        return (inverse);
    }
    
	/**
	 * Traverses this path element starting from the given base object and returns the reached
	 * objects.
	 *
	 * <p>
	 * For a non-inverse path element, the reference value(s) of {@code base} are returned. For an
	 * inverse path element, all objects that refer to {@code base} via the reference are returned.
	 * </p>
	 *
	 * @param base
	 *        The object to start the traversal from. Must not be <code>null</code>.
	 * @return The objects reached by following this path element. Never <code>null</code>.
	 * 
	 * @see #getSources(TLObject)
	 */
	public Collection<? extends TLObject> getValues(TLObject base) {
		return getValues(base, true);
    }

	private Collection<? extends TLObject> getValues(TLObject base, boolean isForward) {
		Collection<? extends TLObject> result;
        
		if (this.isInverse() == isForward) {
			result = base.tReferers(_reference);
		} else {
			Object value = base.tValue(_reference);
			if (value instanceof Collection) {
				@SuppressWarnings("unchecked")
				Collection<? extends TLObject> cast = (Collection<? extends TLObject>) value;
				result = cast;
			} else if (value != null) {
				result = Collections.singleton((TLObject) value);
			} else {
				result = Collections.emptySet();
			}
        }
		return result != null ? result : Collections.emptySet();
    }
    
	/**
	 * Returns the source objects that reach the given destination object when this path element is
	 * traversed.
	 *
	 * <p>
	 * This is the inverse operation of {@link #getValues(TLObject)}: for a non-inverse path
	 * element, all objects that refer to {@code destination} via the reference are returned; for an
	 * inverse path element, the reference value(s) of {@code destination} are returned.
	 * </p>
	 *
	 * @param destination
	 *        The object to determine the sources for. Must not be <code>null</code>.
	 * @return The objects that reach {@code destination} by following this path element. Never
	 *         <code>null</code>.
	 * 
	 * @see #getValues(TLObject)
	 */
	public Collection<? extends TLObject> getSources(TLObject destination) {
		return this.getValues(destination, false);
    }
}
