/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link CompareInfo} that consists of a base value pair and additional {@link CompareInfo}s of
 * some assigned attributes.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class AttributeCompareInfo extends CompareInfo {

	private static final int MAX_CHANGED_ATTRIBUTES_TOOLTIP = 7;

	private Map<String, Pair<CompareInfo, String>> _attributeCompareInfos;

	/**
	 * Create a new {@link AttributeCompareInfo}.
	 */
	public AttributeCompareInfo(Object oldValue, Object newValue, LabelProvider labels) {
		super(oldValue, newValue, labels);
		_attributeCompareInfos = new LinkedHashMap<>();
	}

	/**
	 * Adds a new {@link CompareInfo} to this container.
	 */
	public void addCompareInfo(String attributeName, CompareInfo compareInfo, String attributeLabel) {
		_attributeCompareInfos.put(attributeName,
			new Pair<>(compareInfo, attributeLabel));
	}

	/**
	 * label of attribute name
	 */
	protected String getAttributeNameLabel(String attributeName) {
		return _attributeCompareInfos.get(attributeName).getSecond();
	}

	/**
	 * {@link CompareInfo} of attribute.
	 */
	protected CompareInfo getAttributeCompareInfo(String attributeName) {
		return _attributeCompareInfos.get(attributeName).getFirst();
	}

	/**
	 * names of attributes, which have been attached to the object, represented by this
	 *         {@link AttributeCompareInfo}.
	 */
	protected Collection<String> getAttributeNames() {
		return _attributeCompareInfos.keySet();
	}

	@Override
	protected ChangeInfo createChangeInfo() {
		ChangeInfo valuePairChangeInfo = super.createChangeInfo();
		if (hasChanged(valuePairChangeInfo)) {
			return valuePairChangeInfo;
		}

		for (Pair<CompareInfo, String> additionalCompareInfo : _attributeCompareInfos.values()) {
			ChangeInfo attributeChangeInfo = additionalCompareInfo.getFirst().getChangeInfo();
			if (hasChanged(attributeChangeInfo)) {
				return ChangeInfo.DEEP_CHANGED;
			}
		}

		return ChangeInfo.NO_CHANGE;
	}

	@Override
	public ResKey getTooltip() {
		ChangeInfo changeInfo = getChangeInfo();
		if (!hasChanged(changeInfo) || isDirectChange(changeInfo)) {
			return super.getTooltip();
		} else {
			return getChangedAttributesTooltip();
		}
	}


	private ResKey getChangedAttributesTooltip() {
		return I18NConstants.CHANGE_INFO_DEEP_DETAIL.fill(getChangedAttributes());
	}

	private Object getChangedAttributes() {
		StringWriter writer = new StringWriter();
		try (TagWriter out = new TagWriter(writer)) {
			out.beginTag(HTMLConstants.UL);
			int i = 0;
			for (Pair<CompareInfo, String> attributeInfos : _attributeCompareInfos.values()) {
				if (i < MAX_CHANGED_ATTRIBUTES_TOOLTIP) {
					if (hasChanged(attributeInfos.getFirst().getChangeInfo())) {
						out.beginTag(HTMLConstants.LI);
						out.append(attributeInfos.getSecond());
						out.endTag(HTMLConstants.LI);
						i++;
					}
				} else {
					out.beginBeginTag(HTMLConstants.BR);
					out.endEmptyTag();
					out.append("...");
					break;
				}
			}
			out.endTag(HTMLConstants.UL);
		} catch (IOException ex) {
			Logger.warn("Could not create tooltips for attributes " + _attributeCompareInfos.keySet(),
				AttributeCompareInfo.class);
		}
		return writer.toString();
	}

	private boolean isDirectChange(ChangeInfo changeInfo) {
		return changeInfo == ChangeInfo.CREATED || changeInfo == ChangeInfo.REMOVED || changeInfo == ChangeInfo.CHANGED;
	}

	private boolean hasChanged(ChangeInfo changeInfo) {
		return changeInfo != ChangeInfo.NO_CHANGE;
	}
}
