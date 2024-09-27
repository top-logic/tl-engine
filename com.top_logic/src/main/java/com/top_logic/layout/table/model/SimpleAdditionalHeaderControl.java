/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * An {@link AdditionalHeaderControl} that writes a label and a tooltip.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class SimpleAdditionalHeaderControl extends AdditionalHeaderControl {

	/**
	 * Creates an {@link SimpleAdditionalHeaderControl}.
	 * <p>
	 * See
	 * {@link AdditionalHeaderControl#AdditionalHeaderControl(AdditionalHeaderControlModel, Map)}
	 * for the "inherited" parameters. "style" is ignored.
	 * </p>
	 */
	public SimpleAdditionalHeaderControl(AdditionalHeaderControlModel model, Map<String, ControlCommand> map) {
		super(model, map);
	}

	@Override
	protected void internalWriteContent(DisplayContext context, TagWriter out) {
		out.writeText(context.getResources().getString(getLabel()));
	}

	@Override
	protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		super.writeControlAttributes(context, out);
		ResKey tooltip = getTooltip();
		if (tooltip == null) {
			return;
		}
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out, tooltip);
	}

	/**
	 * The label is written as plain text: XML control characters are quoted.
	 * 
	 * @return Null is equivalent to the empty String.
	 */
	protected ResKey getLabel() {
		return composeStaticWithDynamicPart(getStaticLabelPart(), getDynamicLabelPart());
	}

	/**
	 * @see #getLabel()
	 * @see #composeStaticWithDynamicPart(ResKey, Object)
	 */
	protected abstract ResKey getStaticLabelPart();

	/**
	 * @see #getLabel()
	 * @see #composeStaticWithDynamicPart(ResKey, Object)
	 */
	protected abstract Object getDynamicLabelPart();

	/**
	 * The tooltip is written as plain text: XML control characters are quoted.
	 * 
	 * @return Null is equivalent to the empty String.
	 */
	protected ResKey getTooltip() {
		return composeStaticWithDynamicPart(getStaticTooltipPart(), getDynamicTooltipPart());
	}

	/**
	 * @see #getTooltip()
	 * @see #composeStaticWithDynamicPart(ResKey, Object)
	 */
	protected abstract ResKey getStaticTooltipPart();

	/**
	 * @see #getTooltip()
	 * @see #composeStaticWithDynamicPart(ResKey, Object)
	 */
	protected abstract Object getDynamicTooltipPart();

	/**
	 * The tooltip and the label consist each of two parts: A {@link ResKey} and a dynamically
	 * computed argument for it.
	 * <p>
	 * This is useful to produce values with decoration, such as: "Average: 12%". The "12" is the
	 * dynamic part, and "Average: {0}" is the static {@link ResKey}.
	 * </p>
	 * <p>
	 * If one of these two is null or the empty String, only the other one is used.
	 * </p>
	 */
	protected ResKey composeStaticWithDynamicPart(ResKey staticPart, Object dynamicPart) {
		String dynamicPartLabel = getLabel(dynamicPart);
		if (staticPart == null) {
			return ResKey.text(dynamicPartLabel);
		}
		if (isEmpty(dynamicPartLabel)) {
			return staticPart;
		}
		return staticPart.asResKey1().fill(dynamicPartLabel);
	}

	private String getLabel(Object dynamicPart) {
		return MetaLabelProvider.INSTANCE.getLabel(dynamicPart);
	}

}
