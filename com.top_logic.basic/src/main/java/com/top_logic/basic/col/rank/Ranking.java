/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.rank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Compute a Ranking of Objects based on theire Distance from some starting Object.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class Ranking {
    
    /**
     * Return List of {@link Rank} based on aColl orded by distance to base.
     */
    static public List ranking(Object base, Collection aColl, Distance distance) {
        
        List result = new ArrayList(aColl.size());
        for (Iterator iter = aColl.iterator(); iter.hasNext();) {
            Object o = iter.next();
            Rank r = new Rank(distance.distance(base, o), o);
            result.add(r);
        }
        Collections.sort(result);
        return result;
    }
    
    /**
     * A Simple value Holder to allow comparing by a ranking.
     */
    public static class Rank implements Comparable {
        
        /** ranking for base derived by some calculation */
        float  ranking;
        
        /** An arbitrary object wrapped by this ranking */
        Object  base;

        /** 
         * Create a new Rank.
         */
        public Rank(float aRanking, Object aBase) {
            this.ranking = aRanking;
            this.base    = aBase;
        }
        
        /**
         * Provia a reasobale String for debugging
         */
        @Override
		public String toString() {
            return Float.toString(ranking) + '@' + base;
        }
        
        /**
         * Cat and call to Rank specific compareTo.
         */
        @Override
		public int compareTo(Object o) {
            return compareTo((Rank) o);
        }
        
        /**
         * Compare by rankig.
         */
        public int compareTo(Rank r) {
            return Float.compare(ranking, r.ranking);
        }

        /**
         * Return ranking for the BaseObject.
         */
        public float getRanking() {
            return this.ranking;
        }

        /**
         * Return the Object the Ranking is based on.
         */
        public Object getBasedObject() {
            return (this.base);
        }
    }

}

