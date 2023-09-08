/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.dob.MetaObject;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;

/**
 * ResourceProvider for RoleRules.
 * 
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class RoleRuleResourceProvider extends DefaultResourceProvider {

    public static final RoleRuleResourceProvider INSTANCE = new RoleRuleResourceProvider();

	/** Show full rule ID instead of only storage short ID as label. */
	private final boolean fullID;

	/** Show only rule ID in tooltip. */
	private final boolean simpleTooltip;

	/**
	 * Creates a new {@link RoleRuleResourceProvider}.
	 */
    public RoleRuleResourceProvider() {
		this(false, false);
    }

	/**
	 * Creates a new {@link RoleRuleResourceProvider}.
	 */
	public RoleRuleResourceProvider(boolean fullID, boolean simpleTooltip) {
		this.fullID = fullID;
		this.simpleTooltip = simpleTooltip;
    }



    @Override
    public String getLabel(Object object) {
		if (object instanceof RoleProvider) {
            AccessManager am = AccessManager.getInstance();
			String label = ((RoleProvider) object).getId();
            if (am instanceof ElementAccessManager) {
				String shortID = ((ElementAccessManager) am).getPersitancyId((RoleProvider) object).toString();
				return fullID ? shortID + " (" + label + ")" : shortID;
            }
			return label;
        }
        return super.getLabel(object);
    }


    @Override
    public String getTooltip(Object object) {
        if (object instanceof RoleRule) {
            RoleRule rule = (RoleRule)object;
            Resources resources = Resources.getInstance();
			if (simpleTooltip) {
				return resources.getMessage(I18NConstants.SIMPLE_TOOLTIP, rule.getId());
            }
            MetaResourceProvider meta = MetaResourceProvider.INSTANCE;

            List values = new ArrayList();

			ResKey resourceKey = rule.getResourceKey();
            values.add(resourceKey == null ? StringServices.EMPTY_STRING : resources.getString(resourceKey));
            values.add(resourceKey == null ? StringServices.EMPTY_STRING : resourceKey);

            values.add(rule.getType());
            values.add(rule.getRole());
            values.add(rule.getMetaElement());
            values.add(rule.getMetaObject());
            values.add(rule.getSourceRole());
            values.add(rule.getBaseString());

            values.add(rule.getId());

            values.add(rule.getSourceMetaElement());
            values.add(rule.getSourceMetaObject());
            values.add(rule.getPath());

            for (int i = 0, length = values.size(); i < length; i++) {
                Object value = values.get(i);
                if (value instanceof MetaObject) {
                    value = ((MetaObject)value).getName();
                }
                else if (value instanceof TLClass) {
                    value = ((TLClass)value).getName();
                }
                else if (value instanceof BoundedRole) {
                    value = ((BoundedRole)value).getName();
                }

                if (value instanceof List) {
                    StringBuilder sb = new StringBuilder();
                    for (Iterator it = ((List)value).iterator(); it.hasNext(); ) {
                        boolean separator = false;
                        PathElement element = (PathElement)it.next();
                        if (element instanceof IdentityPathElement) continue;
                        sb.append("<br/>").append(HTMLConstants.NBSP).append(HTMLConstants.NBSP).append(HTMLConstants.NBSP).append("> ");
                        TLStructuredTypePart metaAttribute = element.getMetaAttribute();
                        if (metaAttribute != null) {
                            TLClass metaElement = AttributeOperations.getMetaElement(metaAttribute);
                            if (metaElement != null) {
                                if (separator) sb.append("; ");
                                sb.append("ME: ").append(TagUtil.encodeXML(metaElement.getName())).append(' ');
                                separator = true;
                            }
                            if (separator) sb.append("; ");
                            sb.append("MA: ").append(TagUtil.encodeXML(metaAttribute.getName())).append(' ');
                            separator = true;
                        }
                        else {
                            if (separator) sb.append("; ");
                            sb.append("Assoc: ").append(TagUtil.encodeXML(StringServices.getEmptyString(element.getAssociation()))).append(' ');
                            separator = true;
                        }
                        if (separator) sb.append("; ");
                        sb.append("Inverse: ").append(element.isInverse());
                        separator = true;
                    }
                    values.set(i, sb.toString());
                }
                else {
                    values.set(i, TagUtil.encodeXML(StringServices.getEmptyString(meta.getLabel(value))));
                }
            }
			return resources.getMessage(I18NConstants.ROLE_RULE_TOOLTIP, values.toArray());
        }
		else if (object instanceof RoleProvider) {
			RoleProvider rule = (RoleProvider) object;
			return Resources.getInstance().getMessage(I18NConstants.SIMPLE_TOOLTIP, rule.getId());
		}
        return super.getTooltip(object);
    }


    @Override
    public ThemeImage getImage(Object object, Flavor aFlavor) {
		if (object instanceof RoleProvider) {
			return DefaultResourceProvider.getTypeImage(getType(object), aFlavor);
        }
        return super.getImage(object, aFlavor);
    }

    @Override
    public String getType(Object object) {
        return "RoleRule";
    }

}
