/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.boundsec.manager.I18NConstants;
import com.top_logic.element.boundsec.manager.rule.config.PathElementConfig;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;

/**
 * One node in a role rule path that navigates a {@link TLReference}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class PathNavigation extends AbstractConfiguredInstance<PathElementConfig> implements PathElement {

    /** the meta attribute defining the content */
	private final TLReference _reference;
    
	/**
	 * Create a {@link PathNavigation}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public PathNavigation(InstantiationContext context, PathElementConfig config) {
		super(context, config);

		TLModelPart part = config.getAttribute().resolve();
		if (!(part instanceof TLReference)) {
			throw new ConfigurationError(I18NConstants.NOT_A_REFERENCE__PART.fill(part));
		}
		_reference = (TLReference) ((TLReference) part).getDefinition();
	}

    /**
     * Getter
     */
	@Override
	public TLStructuredTypePart getMetaAttribute() {
		return (_reference);
    }
    
	@Override
	public boolean isInverse() {
		return getConfig().isInverse();
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
