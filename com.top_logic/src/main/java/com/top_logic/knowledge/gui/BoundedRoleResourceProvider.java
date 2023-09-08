/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResPrefix;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;

/**
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class BoundedRoleResourceProvider extends WrapperResourceProvider {

	public static final ResPrefix RESOURCE_PREFIX = I18NConstants.ROLE_NAME;

    public BoundedRoleResourceProvider() {
        super();
    }
    
    /** 
     * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
     */
    @Override
	public String getLabel(Object anObject) {
        if (anObject instanceof BoundedRole) {
            try {
				BoundedRole role = (BoundedRole) anObject;
				String technicalName = role.getName();
				Resources resources = Resources.getInstance();
				String i18nLabel = resources.getString(RESOURCE_PREFIX.key(technicalName), null);
				if (StringServices.isEmpty(i18nLabel)) {
					String securityModule = role.getScope().getName();
					if (technicalName.startsWith(securityModule)) {
						if (technicalName.length() > securityModule.length()
							&& technicalName.charAt(securityModule.length()) == '.') {
							return technicalName.substring(securityModule.length() + 1);
						}
					}
					return technicalName;
				} else {
					return i18nLabel;
                }
            } catch (Exception ex) {
                return super.getLabel(anObject);
            }
        }
        return super.getLabel(anObject);
    }

	@Override
	protected ResKey getTooltipNonNull(Object object) {
		String techName = object instanceof BoundRole ? ((BoundRole) object).getName() : StringServices.EMPTY_STRING;
		return I18NConstants.BOUNDED_ROLE_TOOLTIP.fill(
			quote(object),
			quote(TLModelUtil.type(object)),
			quote(techName));
    }

}
