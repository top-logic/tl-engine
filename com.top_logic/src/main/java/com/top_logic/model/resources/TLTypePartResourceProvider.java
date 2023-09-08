/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.resources;

import java.util.Locale;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;

/**
 * Translates {@link TLStructuredTypePart}s into his internationalized label in the locale of the
 * current user.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLTypePartResourceProvider extends WrapperResourceProvider {
	
	/**
	 * Singleton instance of {@link TLTypePartResourceProvider}.
	 */
	@SuppressWarnings("hiding")
	public static final TLTypePartResourceProvider INSTANCE = new TLTypePartResourceProvider();

    /**
	 * {@link ResourceProvider} for {@link TLStructuredTypePart}s.
	 * 
	 * @see TLTypePartResourceProvider
	 */
	protected TLTypePartResourceProvider() {
        super();
    }
    
    @Override
	public String getLabel(Object object) {
    	if (object instanceof TLStructuredTypePart) {
	        try {
	            return getTLTypePartLabel((TLStructuredTypePart) object);
	        }
	        catch (Exception ex) {
	            return super.getLabel(object);
	        }
    	}
    	return super.getLabel(object);
    }

	/**
	 * The internationalized label of the given {@link TLStructuredTypePart} in the {@link Locale}
	 * of the user context.
	 */
	public static String getTLTypePartLabel(TLStructuredTypePart part) {
		return Resources.getInstance().getString(labelKey(part));
	}

	/**
	 * {@link ResKey} for the given {@link TLStructuredTypePart}'s label.
	 */
	public static ResKey labelKey(TLStructuredTypePart part) {
		ResKey fallbackKey;
		if (part.getModelKind() == ModelKind.END) {
			fallbackKey = getGenericAssociationEndKey((TLAssociationEnd) part);
		} else {
			// Do not report missing keys for technical attributes. Use attribute name as last
			// fallback.
			fallbackKey = null;
		}
		ResKey modelKey = TLModelI18N.getI18NKey(part);
		return ResKey.fallback(modelKey, fallbackKey);
	}

	private static ResKey getGenericAssociationEndKey(TLAssociationEnd model) {
		ResKey associationKey = TLModelNamingConvention.getTypeLabelKey(model.getOwner());
		ResKey associationEndKey = I18NConstants.ASSOCIATION_END.key(model.getName());
		/* This key allows a translation for all ends with the same name, e.g. all ends with name
		 * "dest". */
		ResKey genericForModelName = ResKey.message(associationEndKey, associationKey);
		/* This key allows a translation for all ends with name of end as argument. */
		ResKey genericKey = I18NConstants.ASSOCIATION_END__OWNER__NAME.fill(associationKey, model.getName());
		return ResKey.fallback(genericForModelName, genericKey);
	}

	@Override
	public String getType(Object object) {
        if (object instanceof TLStructuredTypePart) {
            return object.getClass().getSimpleName();
        }
		return ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE;
    }
    
	@Override
	protected ResKey getTooltipNonNull(Object object) {
		TLTypePart part = (TLTypePart) object;
		return com.top_logic.model.visit.I18NConstants.TYPE_PART_TOOLTIP.fill(
			quote(MetaLabelProvider.INSTANCE.getLabel(object)),
			quote(MetaLabelProvider.INSTANCE.getLabel(part.getOwner())),
			quote(MetaResourceProvider.INSTANCE.getLabel(TLModelUtil.type(part))),
			quote(MetaLabelProvider.INSTANCE.getLabel(part.getType())),
			quote(part.getName()));
	}

}
