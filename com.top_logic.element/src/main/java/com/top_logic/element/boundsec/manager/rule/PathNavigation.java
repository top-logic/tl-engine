/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.element.boundsec.manager.I18NConstants;
import com.top_logic.element.boundsec.manager.rule.config.PathElementConfig;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

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
		if (((TLReference) part).isDerived()) {
			// Do not check definition, because this may be an abstract attribute. Abstract
			// attributes are derived: See NoStorage#isReadOnly.
			context.error("Path-navigation references derived (computed) attribute '"
					+ TLModelUtil.qualifiedName(part)
					+ "'. Derived attributes do not fire change notifications and cannot be tracked"
					+ " for role-rule invalidation.");
		}
		_reference = (TLReference) ((TLReference) part).getDefinition();
	}

	@Override
	public Collection<TLStructuredTypePart> getRelevantParts() {
		return Collections.singleton(_reference);
	}
    
	private boolean isInverse() {
		return getConfig().isInverse();
    }
    
	@Override
	public Collection<? extends TLObject> getValues(TLObject base) {
		return getValues(base, true);
    }

	private Collection<? extends TLObject> getValues(TLObject base, boolean isForward) {
		Collection<? extends TLObject> result;
        
		if (isInverse() == isForward) {
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
	public BaseObjects<? extends Collection<? extends TLObject>> getSources(TLObject destination) {
		return BaseObjects.of(getValues(destination, false));
    }

	@Override
	public BaseObjects<? extends Collection<? extends TLObject>> getPathBase(TLObject element,
			TLStructuredTypePart part, Supplier<?> partValue) {
		Collection<? extends TLObject> baseElements;
		if (isInverse()) {
			@SuppressWarnings("unchecked")
			Collection<? extends TLObject> refValue =
				(Collection<? extends TLObject>) CollectionUtil.asCollection(partValue.get());
			baseElements = refValue;
		} else {
			baseElements = Collections.singleton(element);
		}
		return BaseObjects.of(baseElements);
	}

	@Override
	public void appendId(Appendable out) throws IOException {
		out.append("ma:");
		out.append(TLModelUtil.qualifiedName(_reference));
		out.append('_');
		out.append(isInverse() ? "back" : "succ");
	}

	@Override
	public void appendForTooltip(Appendable out) throws IOException {
		out.append("MA: ");
		out.append(TagUtil.encodeXML(TLModelUtil.qualifiedName(_reference)));
		out.append("; Inverse: ");
		out.append(String.valueOf(isInverse()));
	}
}
