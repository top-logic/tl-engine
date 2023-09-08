/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.rank;

import java.util.Arrays;

/**
 * Allow a weighted sum of {@link Distance}s.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class WeightedDistance implements Distance {

    protected Distance basedOn[];
    
    protected float    factors[];

    /** Optionally store calculated distance in this array */
    protected float    baseDistances[];

    /** 
     * Create a new WeightedDistance based on given distances.
     * 
     * @param store allow retrival of intermediate results.
     */
    public WeightedDistance(Distance[] aBasedOn, float[] aFactors, boolean store) {
        this.basedOn = aBasedOn;
        this.factors = aFactors;
        if (store) {
            baseDistances = new float[aBasedOn.length];
        }
    }

    /** 
     * Create a new WeightedDistance based on given distances.
     */
    public WeightedDistance(Distance[] basedOn, float[] factors) {
        this(basedOn ,factors, false);
    }

    /** 
     * Create a new WeightedDistance based on given distances using 1 as factor.
     */
    public WeightedDistance(Distance[] aBasedOn) {
        this (aBasedOn,  new float[aBasedOn.length]);
        Arrays.fill(factors, (float) 1.0);
    }

    /** 
     * Create a new WeightedDistance based on two other distances.
     */
    public WeightedDistance(Distance d1, Distance d2) {
        this (new Distance[] {d1, d2});
    }

    /** 
     * Create a new WeightedDistance based on two other distances and factors.
     */
    public WeightedDistance(Distance d1, Distance d2, float f1, float f2) {
        this (new Distance[] {d1, d2}, new float[] { f1, f2 });
    }

    /** 
     * Create a new WeightedDistance based on two other distances and factors.
     */
    public WeightedDistance(Distance d1, Distance d2, float f1, float f2, boolean store) {
        this (new Distance[] {d1, d2}, new float[] { f1, f2 }, store);
    }

    /**
     * Compute distance of o1 and o2 by weighting the baseDistances with given factors.
     */
    @Override
	public float distance(Object o1, Object o2) {
        float sum = 0;
        if (baseDistances == null) {
            for (int i = 0; i < basedOn.length; i++) {
                sum += factors[i] * basedOn[i].distance(o1, o2);
            }
        } else {
            for (int i = 0; i < basedOn.length; i++) {
                sum += factors[i] * (baseDistances[i] = basedOn[i].distance(o1, o2));
            }
        }
        return sum;
    }
    
    /** Allow access to inner distances */
    public Distance getBaseDistance(int i) {
        return basedOn[i];
    }

    /** 
     * Return last calculated sub-distance 
     * 
     * @throws NullPointerException when store was not configured
     */
    public float getBaseValue(int i) {
        return baseDistances[i];
    }
}

