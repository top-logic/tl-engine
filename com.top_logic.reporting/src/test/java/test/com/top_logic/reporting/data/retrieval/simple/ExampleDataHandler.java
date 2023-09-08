/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.data.retrieval.simple;

import com.top_logic.reporting.data.base.description.Description;
import com.top_logic.reporting.data.base.description.simple.SimpleDescription;
import com.top_logic.reporting.data.base.value.common.NumberValue;
import com.top_logic.reporting.data.base.value.common.NumericTuple;
import com.top_logic.reporting.data.base.value.common.NumericValue;
import com.top_logic.reporting.data.retrieval.DataHandler;
import com.top_logic.reporting.data.retrieval.simple.ExtendableValueHolder;
import com.top_logic.reporting.data.retrieval.simple.SimpleDataHandler;
import com.top_logic.reporting.data.retrieval.simple.SimpleRange;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class ExampleDataHandler extends SimpleDataHandler {

    /**
     * Constructor for ExampleDataHandler.
     */
    public ExampleDataHandler(String aName, Description[] someDesc) {
        super(aName, someDesc);
    }

    /**
     * Constructor for ExampleDataHandler.
     */
    public ExampleDataHandler(String aName,
                              Description[] someDesc, 
                              int aDepth,
                              DataHandler aHandler)
                                            throws IllegalArgumentException {
        super(aName, someDesc, aDepth, new DataHandler[] {aHandler});
    }

    public static Description[] getExampleDescriptions() {
        Description[] theResult = new Description[4];

        theResult[0] = new SimpleDescription("Costs", NumericValue.class);
        theResult[1] = new SimpleDescription("Profit", NumericValue.class);
        theResult[2] = new SimpleDescription("Budget", NumericValue.class);
        theResult[3] = new SimpleDescription("Estimate Time to Complete", NumericValue.class);

        return (theResult);
    }

    public static DataHandler getExample(int aDepth) {
        Description[]     theDesc    = getExampleDescriptions();
        SimpleDataHandler theHandler = new ExampleDataHandler("Example 2", 
                                                              theDesc);

        init(theHandler, theDesc);

        theHandler = new ExampleDataHandler("Example 2", theDesc, 1, theHandler);

        init(theHandler, theDesc);

        return (theHandler);
    }

    public static DataHandler getExample() {
        Description[]     theDesc    = getExampleDescriptions();
        SimpleDataHandler theHandler = new ExampleDataHandler("Example", 
                                                              theDesc);

        init(theHandler, theDesc);

        return (theHandler);
    }

    private static void init(SimpleDataHandler aHandler, 
                             Description[] someDesc) {
        ExtendableValueHolder theHolder;

        theHolder  = new ExtendableValueHolder("Costs");

        theHolder.addValue(new SimpleRange("2002"), new NumberValue(20000));
        theHolder.addValue(new SimpleRange("2003"), new NumberValue(30000));
        theHolder.addValue(new SimpleRange("2004"), new NumberValue(40000));
        theHolder.addValue(new SimpleRange("2005"), new NumberValue(50000));
        theHolder.addValue(new SimpleRange("2006"), new NumberValue(60000));
        theHolder.addValue(new SimpleRange("2007"), new NumberValue(70000));
        theHolder.addValue(new SimpleRange("2008"), new NumberValue(80000));

        aHandler.addValue(someDesc[0], theHolder);

        theHolder  = new ExtendableValueHolder("Profit");

        theHolder.addValue(new SimpleRange("2003"), new NumberValue(3000));
        theHolder.addValue(new SimpleRange("2004"), new NumberValue(4000));
        theHolder.addValue(new SimpleRange("2005"), new NumberValue(5000));
        theHolder.addValue(new SimpleRange("2006"), new NumberValue(6000));
        theHolder.addValue(new SimpleRange("2007"), new NumberValue(7000));
        theHolder.addValue(new SimpleRange("2008"), new NumberValue(8000));
        theHolder.addValue(new SimpleRange("2009"), new NumberValue(9000));
        theHolder.addValue(new SimpleRange("2010"), new NumberValue(1000000));

        aHandler.addValue(someDesc[1], theHolder);

        theHolder  = new ExtendableValueHolder("Budget");

        theHolder.addValue(new SimpleRange("2002"), new NumericTuple(new double[] {2000, 1345}));
        theHolder.addValue(new SimpleRange("2003"), new NumericTuple(new double[] {3000, 3220}));
        theHolder.addValue(new SimpleRange("2004"), new NumericTuple(new double[] {4000, 2537}));
        theHolder.addValue(new SimpleRange("2005"), new NumericTuple(new double[] {5000, 263}));
        theHolder.addValue(new SimpleRange("2006"), new NumericTuple(new double[] {6000, 304}));
        theHolder.addValue(new SimpleRange("2007"), new NumericTuple(new double[] {7000, 0}));
        theHolder.addValue(new SimpleRange("2008"), new NumericTuple(new double[] {8000, 0}));

        aHandler.addValue(someDesc[2], theHolder);

        theHolder  = new ExtendableValueHolder("Estimate Time to Complete");

        theHolder.addValue(new SimpleRange("2002"), new NumberValue(0));
        theHolder.addValue(new SimpleRange("2003"), new NumberValue(120));
        theHolder.addValue(new SimpleRange("2004"), new NumberValue(200));
        theHolder.addValue(new SimpleRange("2005"), new NumberValue(400));
        theHolder.addValue(new SimpleRange("2006"), new NumberValue(400));
        theHolder.addValue(new SimpleRange("2007"), new NumberValue(400));

        aHandler.addValue(someDesc[3], theHolder);
    }
}
