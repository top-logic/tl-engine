/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLNamed;
import com.top_logic.util.Resources;

/**
 * This implementation resolves label strings and tooltips for
 * {@link FastListElement} instances.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class FastListElementResourceProvider extends DefaultResourceProvider {

    /**
     * We have to override this constant in oder to use our own implementation.
     */
    public static final FastListElementResourceProvider INSTANCE = new FastListElementResourceProvider();

    @Override
	public String getTooltip(Object anObject) {
		if (anObject instanceof TLClassifier) {
			ResKey theName = FastListElementLabelProvider.labelKey((TLClassifier) anObject).tooltip();
			return (Resources.getInstance().getString(theName, null));
        }
        return super.getTooltip(anObject);
    }

    @Override
	public String getLabel(Object anObject) {
		if (anObject instanceof TLClassifier) {
			// Note: Must not inherit functionality from
			// I18NWrapperResourceProvider, because this does not mark the name
			// as missing resource, if the internationalization is missing.
			ResKey theName = FastListElementLabelProvider.labelKey((TLClassifier) anObject);
			return (Resources.getInstance().getString(theName));
		}
		if (anObject instanceof TLEnumeration) {
			// Note: Must not inherit functionality from
			// I18NWrapperResourceProvider, because this does not mark the name
			// as missing resource, if the internationalization is missing.
			ResKey theName = FastListElementLabelProvider.labelKey((TLEnumeration) anObject);
			return (Resources.getInstance().getString(theName));
		}
		return super.getLabel(anObject);
	}

	@Override
	public String getType(Object object) {
		if (object instanceof TLNamed) {
			return ((TLNamed) object).tTable().getName();
		}
		return super.getType(object);
	}

}
