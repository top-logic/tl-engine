/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.generator;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.LabelProvider;

/**
 * The MaxLabelLengthProvider stores labels for objects. All labels are minimized which are
 * longer as the maximum label length. The minimized labels are unique. 
 * This label provider knows only labels for object which are added before 
 * (see {@link #addLabel(Object, String)}).
 * 
 * E.g. obj 1, label 1 = "0123456789"
 *      minimized = "012...6789"
 *      obj 2, label 2 = "0123456789" same label
 *      minimized = "012...67~2"
 *      obj 3, label 3 = "0123456789" same label
 *      minimized = "012...67~3"
 *      ...
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class MaxLabelLengthProvider implements LabelProvider {

    /** The label number separator (e.g. home...town~2). */
    public static final String SEP = "~";

    /** The maximum label length. */
    private int maxLabelLength;
    /**
     * This map stores the minimized label for the objects.
     * Key: Objects.
     * Value: labels
     * 
     *  E.g. home...to~3
     */
    private Map objectLabelMap;
    /**
     * This map stores how often a minimized label is used.
     * Key:   Minimzized label (e.g. homw...town).
     * Value: The number of uses.
     * 
     * E.g. key:   home...town
     *      value: 3
     *      
     *      home...town, home...to~2 and home...to~3
     */
    private Map labelCountMap;

    /**
     * Creates a {@link MaxLabelLengthProvider}.
     * 
     * @param aMaxLabelLength
     *        The maximum length of the returned labels. The max label length
     *        value must be greater or equals than 10 (three capitals for ...,
     *        and the rest for ~1). E.g. Home...town~3.
     */
    public MaxLabelLengthProvider(int aMaxLabelLength) {
        if (aMaxLabelLength < 10) {
            throw new IllegalArgumentException("The max label length must be greater or equals than 10.");
        }
        
        this.maxLabelLength = aMaxLabelLength;
        this.objectLabelMap = new HashMap();
        this.labelCountMap  = new HashMap();
    }

    @Override
	public String getLabel(Object aObject) {
        String label = (String) this.objectLabelMap.get(aObject);
        
        return label != null ? label : "";
    }

    /**
     * This method adds the label for the given key. If the label is longer as
     * the maximum size it will be minimized.
     * 
     * @param aKey
     *        A key object. Must not be <code>null</code>.
     * @param aLabel
     *        A label for the object. Must not be <code>null</code>.
     */
    public void addLabel(Object aKey, String aLabel) {
        String  minimizedLabel      = StringServices.minimizeString(aLabel, this.maxLabelLength);
        Integer minimizedLabelCount = (Integer) this.labelCountMap.get(minimizedLabel);
        
        if (minimizedLabelCount == null) {
            // Case 1: For the object is no label stored
            this.objectLabelMap.put(aKey,           minimizedLabel);
			this.labelCountMap.put(minimizedLabel, Integer.valueOf(0));
        } else {
            // Case 2: For the object is a label stored
            //         Find the next free label (e.g. Home...town~5).
            String  nextLabel      = minimizedLabel + SEP + "2";
			Integer nextLabelCount = Integer.valueOf(2);
            for (int i = 2; i < 1000; i++) {
                nextLabel      = minimizedLabel + SEP + String.valueOf(i);
                nextLabelCount = (Integer) this.labelCountMap.get(nextLabel);
                
                if (nextLabelCount == null) {
					nextLabelCount = Integer.valueOf(i);
                    break;
                } 
            }
            
            // Calculate the additional length (e.g. ~1).
            int numberLength     = String.valueOf(nextLabelCount).length();
            int additionalLength = SEP.length() + numberLength;
            
            // Create final label
            String finalLabel = StringServices.minimizeString(minimizedLabel, this.maxLabelLength - additionalLength);
            finalLabel = finalLabel + SEP + nextLabelCount.toString();
            
            this.objectLabelMap.put(aKey,    finalLabel);
            this.labelCountMap .put(nextLabel, nextLabelCount);
        }
    }
    
}

