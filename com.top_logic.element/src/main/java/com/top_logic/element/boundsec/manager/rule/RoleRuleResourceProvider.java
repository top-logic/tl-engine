/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.io.IOError;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.Named;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.manager.AccessManager;
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
				return resources.getString(I18NConstants.SIMPLE_TOOLTIP__ID.fill(rule.getId()));
            }
            MetaResourceProvider meta = MetaResourceProvider.INSTANCE;

			ResKey resourceKey = rule.getResourceKey();
			String name = resourceKey == null ? StringServices.EMPTY_STRING : resources.getString(resourceKey);
			String key = resourceKey == null ? StringServices.EMPTY_STRING : meta.getLabel(resourceKey);

			String type = meta.getLabel(rule.getType());
			String role = name(rule.getRole());
			String metaElement = name(rule.getMetaElement());
			String sourceRole = name(rule.getSourceRole());

            String id = rule.getId();

			String sourceMetaElement = name(rule.getSourceMetaElement());
			String path = pathAsString(rule.getPath());

			return resources.getString(I18NConstants.ROLE_RULE_TOOLTIP__NAME_KEY_TYPE_ROLE_ME_ID_SOURCEME_PATH
				.fill(name, key, type, role, metaElement, sourceRole, id, sourceMetaElement, path));
        }
		else if (object instanceof RoleProvider) {
			RoleProvider rule = (RoleProvider) object;
			return Resources.getInstance().getString(I18NConstants.SIMPLE_TOOLTIP__ID.fill(rule.getId()));
		}
        return super.getTooltip(object);
    }

	private static String pathAsString(List<? extends PathElement> value) throws IOError {
		StringBuilder sb = new StringBuilder();
		for (Iterator<? extends PathElement> it = value.iterator(); it.hasNext();) {
			PathElement element = it.next();
		    if (element instanceof IdentityPathElement) continue;
		    sb.append("<br/>").append(HTMLConstants.NBSP).append(HTMLConstants.NBSP).append(HTMLConstants.NBSP).append("> ");
			try {
				element.appendForTooltip(sb);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		}
		return sb.toString();
	}

	private static String name(Named named) {
		return named == null ? StringServices.EMPTY_STRING : named.getName();
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
