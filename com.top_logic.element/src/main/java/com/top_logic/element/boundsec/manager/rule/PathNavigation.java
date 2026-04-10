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
 * One node in a role rule path that navigates a {@link TLReference}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class PathNavigation implements PathElement {

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
	public PathNavigation(TLReference reference, boolean isInvers) {
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
	@Override
	public TLStructuredTypePart getMetaAttribute() {
		return (_reference);
    }
    
    /**
     * Getter
     */
	@Override
	public boolean isInverse() {
        return (inverse);
    }
    
	@Override
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
    
	@Override
	public Collection<? extends TLObject> getSources(TLObject destination) {
		return this.getValues(destination, false);
    }
}
