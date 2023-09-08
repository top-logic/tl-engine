/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.currency;

import java.text.NumberFormat;
import java.util.Locale;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.currency.Amount;
import com.top_logic.knowledge.wrap.currency.AmountLabelProvider;
import com.top_logic.knowledge.wrap.unit.Unit;
import com.top_logic.knowledge.wrap.unit.UnitWrapper;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.provider.UnitLabelProvider;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Test the {@link AmountLabelProvider}.
 * 
 * @author     <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestAmountLabelProvider extends BasicTestCase {

    public TestAmountLabelProvider(String name) {
        super(name);
    }

    /**
	 * Test using {@link AmountLabelProvider#INSTANCE}.
	 */
	public void testLabels() {
		Locale currentLocale = TLContext.getLocale();
		NumberFormat numberFormat = NumberFormat.getInstance(currentLocale);
        AmountLabelProvider    provider       = AmountLabelProvider.INSTANCE;
		Unit cm = UnitWrapper.getInstance("cm");
		String unitLabel = Resources.getInstance().getString(UnitLabelProvider.I18N_PREFIX.key("cm"));
        Amount                 someCm     = new Amount(3.141, cm);
		assertEquals(numberFormat.format(3.14d) + " " + unitLabel, provider.getLabel(someCm));
        
		Amount somePieces = new Amount(4.663, UnitWrapper.getPiece0());
		assertEquals(numberFormat.format(5d), provider.getLabel(somePieces));

		Amount someFractionalPieces = new Amount(4.663, UnitWrapper.getPiece3());
		assertEquals(numberFormat.format(4.663d), provider.getLabel(someFractionalPieces));
    }
    
    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(TestAmountLabelProvider.class,
			LabelProviderService.Module.INSTANCE));
    }
    
    /**
     * main function for direct Testing.
     */
    public static void main(String[] args) {
        Logger.configureStdout(); // "INFO"

        TestRunner.run(suite());
    }
    
}
