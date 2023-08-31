/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Locator for resolving a wrapper value reverse (by using
 * {@link WrapperMetaAttributeUtil#getWrappersWithValue(com.top_logic.basic.TLID, com.top_logic.model.TLObject)}).
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ReverseMetaAttributeLocator extends BackReferenceAttributeValueLocator {

    public static final char ME_SEPARATOR = '#';

    private final String maName;
    
    private final String meName;

	private TLStructuredTypePart metaAttribute;

    /**
     * Create a new reverse lookup locator with full meta attribute name (including meta element).
     * 
     * @param    aConfig    The configuration to find the meta attribute from.
     */
    public ReverseMetaAttributeLocator(String aConfig) {
        super();

        int thePos = aConfig.indexOf(ReverseMetaAttributeLocator.ME_SEPARATOR);

        this.meName = aConfig.substring(0, thePos);
        this.maName = aConfig.substring(thePos + 1);

        if (StringServices.isEmpty(this.meName)) {
			throw new IllegalArgumentException("Path config must not be empty");
        }
        else if (StringServices.isEmpty(this.maName)) {
			throw new IllegalArgumentException("Type name must not be empty");
        }
    }

    /**
     * Locate the value from the given wrapper.
     * 
     * @see com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator#locateAttributeValue(Object)
     */
    @Override
	public Object internalLocateAttributeValue(Object anObject) {
        if (anObject instanceof Wrapper) {
            return AttributeOperations.getReferers((Wrapper) anObject, this.getMetaAttribute());
        }
        else {
            return null;
        }
    }

	@Override
	public Set<? extends TLObject> locateReferers(Object value) {
		TLObject baseObject = (TLObject) value;
		Object referenceValue = AttributeOperations.getAttributeValue(baseObject, this.getMetaAttribute());
		if (referenceValue instanceof Collection<?>) {
			return CollectionUtil.toSet((Collection<? extends TLObject>) referenceValue);
		} else {
			return CollectionUtil.singletonOrEmptySet((TLObject) referenceValue);
		}
	}

	@Override
	protected boolean isCollection() {
		return true;
	}

	@Override
	protected String getValueTypeSpec() {
		return "me:" + meName;
	}

	@Override
	protected String getReverseEndSpec() {
		return "maName";
	}

    /** 
     * Framework-internal access to the {@link TLStructuredTypePart} for reverse lookup.
     * 
     * @throws    IllegalArgumentException    If there is no such meta attribute ID.
     */
	public TLStructuredTypePart getMetaAttribute() {
        if (this.metaAttribute == null) {
            Set<TLClass> theMEs = MetaElementFactory.getInstance().getAllMetaElements();
            TLClass      theME  = null;

            for (TLClass theCurrent : theMEs) {
                String theName = theCurrent.getName();

                if (this.meName.equals(theName)) {
                    theME = theCurrent;
                    break;
                }
            }

            if (theME == null) {
				throw new IllegalArgumentException("Type named '" + this.meName + "' is unknown to the system");
            }

            try {
				this.metaAttribute = MetaElementUtil.getMetaAttribute(theME, this.maName);
            }
            catch (Exception ex) {
				throw new IllegalArgumentException(
					"Attribute named '" + this.maName + "' is unknown by '" + this.meName + "'", ex);
            }
        }

        return this.metaAttribute;
    }
}
