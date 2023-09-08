/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.currency;

import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.wrap.unit.Unit;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.util.TLContext;

/**
 * Label provider for an {@link Amount}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class AmountLabelProvider implements LabelProvider {

	/** Default {@link AmountLabelProvider}. */
	public static final AmountLabelProvider INSTANCE = new AmountLabelProvider();

    /** Do not append Unit to the formatted String */
    private final boolean noUnit;

    /** 
     * Creates a {@link AmountLabelProvider}, using TYPE.LONG
     */
    public AmountLabelProvider() {
		this(false);
    }

    /** 
     * Creates a {@link AmountLabelProvider}.
     */
	public AmountLabelProvider(boolean withoutUnit) {
        this.noUnit = withoutUnit;
    }

    /**
     * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
     */
    @Override
	public String getLabel(Object aNumber) {
        Amount  theAmount    = (Amount) aNumber;
        Unit    theUnit      = theAmount.getUnit();
		String valueText = theUnit.getFormat(TLContext.getLocale()).format(theAmount.getValue());
		if (this.noUnit) {
			return valueText;
        }

        String unitLabel = MetaLabelProvider.INSTANCE.getLabel(theUnit);
		if (StringServices.isEmpty(unitLabel)) {
			return valueText;
		} else {
			return valueText + ' ' + unitLabel;
		}
    }
    
    /**
     * Display an amount without its label (but with its format). 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class NoUnitAmountLabelProvider extends AmountLabelProvider {

		/**
		 * Singleton {@link NoUnitAmountLabelProvider} instance.
		 */
		@SuppressWarnings("hiding")
		public static final NoUnitAmountLabelProvider INSTANCE = new NoUnitAmountLabelProvider();

		/** {@link AmountLabelProvider} that suppresses the unit. */
		public static final NoUnitAmountLabelProvider INSTANCE_NO_UNIT = INSTANCE;

		/** 
		 * Creates a {@link AmountLabelProvider}.NoUnitAmountLabelProvider.
		 */
		private NoUnitAmountLabelProvider() {
			super(true);
		}
    }
}

