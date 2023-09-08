/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import static com.top_logic.basic.xml.TagUtil.*;
import static com.top_logic.mig.html.HTMLConstants.*;

import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.TypeKeyProvider.Key;
import com.top_logic.basic.TypeKeyRegistry;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.attribute.MetaAttributeBasedSecurityProvider;
import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.security.SecurityAccessor;
import com.top_logic.layout.security.SecurityLabelProvider;
import com.top_logic.layout.security.SecurityProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContext;

/**
 * Show attributes in tooltips in a configurable way.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ConfiguredAttributedTooltipProvider extends WrapperResourceProvider {

	private static final String TOOLTIP_VALUE_CSS_CLASS = "configuredTooltipValue";

	private static final String TOOLTIP_LABEL_CSS_CLASS = "configuredTooltipLabel";

	/**
	 * Single instance of {@link ConfiguredAttributedTooltipProvider}
	 */
    public static final ConfiguredAttributedTooltipProvider INSTANCE = new ConfiguredAttributedTooltipProvider();

	static final String NAME_ATTRIBUTE = "name";

	static final String TYPE_ATTRIBUTE = "tl.type";

	static final ResKey TYPE_LABEL = I18NConstants.TOOLTIP;
    
	@Override
	protected ResKey getTooltipNonNull(Object object) {
		Key typeKey = TypeKeyRegistry.lookupTypeKey(object);
		List<String> attributes = ConfiguredAttributedTooltips.getValue(typeKey);

		return ResKey.text(this.buildTooltip((TLObject) object, attributes));
    }

	private String buildTooltip(TLObject attributed, List<String> attributes) {
		StringBuilder tooltip = new StringBuilder();
		TLStructuredType type = attributed.tType();

		boolean first = true;
		for (String attributeName : attributes) {
			writeTooltip(tooltip, first, attributed, type, attributeName);
			first = false;
		}
		return tooltip.toString();
    }

	private void writeTooltip(StringBuilder out, boolean first, TLObject attributed, TLStructuredType type,
			String attributeName) {
		String label;
		String value;
		if (TYPE_ATTRIBUTE.equals(attributeName)) {
			// Type is label before name.
			return;
		} else if (NAME_ATTRIBUTE.equals(attributeName)) {
			label = MetaLabelProvider.INSTANCE.getLabel(type);
			value = MetaLabelProvider.INSTANCE.getLabel(attributed);
		} else {
			Group representativeGroup = TLContext.getContext().getCurrentPersonWrapper().getRepresentativeGroup();
			SecurityProvider securityProvider = new MetaAttributeBasedSecurityProvider(null, true);
			Accessor securityAccessor =
				new SecurityAccessor(representativeGroup, securityProvider, WrapperAccessor.INSTANCE);
			TLStructuredTypePart attribute = type.getPart(attributeName);
			if (attribute == null) {
				StringBuilder noMA = new StringBuilder();
				noMA.append("Invalid attributes in tooltip for ");
				noMA.append(attributed);
				noMA.append(": No attribute '");
				noMA.append(attributeName);
				noMA.append("'");
				throw new ConfigurationError(noMA.toString());
			}
			label = MetaLabelProvider.INSTANCE.getLabel(attribute);
			Object attributeValue = securityAccessor.getValue(attributed, attributeName);
			value = SecurityLabelProvider.INSTANCE.getLabel(attributeValue);
		}
		writeAttributeTooltip(out, label, value, first);
	}
    
	private void writeAttributeTooltip(StringBuilder tooltip, String label, String value, boolean isFirst) {
		if (!isFirst) {
			beginBeginTag(tooltip, HTMLConstants.BR);
			endEmptyTag(tooltip);
		}
		writeInSpanWithClass(tooltip, TOOLTIP_LABEL_CSS_CLASS, label);
		tooltip.append(": ");
		writeInSpanWithClass(tooltip, TOOLTIP_VALUE_CSS_CLASS, value);
    }

	private void writeInSpanWithClass(StringBuilder tooltip, CharSequence cssClass, String text) {
		beginBeginTag(tooltip, SPAN);
		writeAttribute(tooltip, CLASS_ATTR, cssClass);
		endBeginTag(tooltip);
		{
			writeText(tooltip, text);
		}
		endTag(tooltip, SPAN);
	}
}
