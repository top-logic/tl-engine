/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.rank;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.rank.Distance;
import com.top_logic.basic.col.rank.EuclidDistance;
import com.top_logic.basic.col.rank.ManhattanDistance;
import com.top_logic.basic.col.rank.Ranking;
import com.top_logic.basic.col.rank.Ranking.Rank;
import com.top_logic.basic.col.rank.WeightedDistance;

/**
 * Test the {@link WeightedDistance}
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestWeightedDistance extends TestCase {

   /** Use whith {@link Assert#assertEquals(float, float, float)} */
    protected static final float         EPSILON = 0.1f;
 
    /** 
     * Create a new TestWeightedDistance.
     */
    public TestWeightedDistance(String name) {
        super(name);
    }

    /**
     * Test Ranking using {@link WeightedDistance}
     */
    public void testWeightedDistance() {
        
        Point2D basePoint = new Point2D.Float(2,2);        
        
        Collection points = new ArrayList();
        points.add(new Point2D.Float(1,2));
        points.add(new Point2D.Float(3,2));
        points.add(new Point2D.Float(2,3));
        points.add(new Point2D.Float(2,1));
        points.add(basePoint);
        points.add(new Point2D.Float(2,0));
        points.add(new Point2D.Float(2,4));
        points.add(new Point2D.Float(0,2));
        points.add(new Point2D.Float(4,2));
        points.add(new Point2D.Float(1,3));
        points.add(new Point2D.Float(3,3));
        points.add(new Point2D.Float(3,1));
        points.add(new Point2D.Float(1,1));
        points.add(this);
        
        WeightedDistance wd = new WeightedDistance(
                EuclidDistance.INSTANCE, ManhattanDistance.INSTANCE);
        
        List distance = Ranking.ranking(basePoint, points, wd);
        Rank rank0 = (Rank) distance.get(0);
        // Basepoint is nearest to itself.
        assertEquals(basePoint, rank0.getBasedObject()); 
        assertEquals(0.0f        , rank0.getRanking(), EPSILON); 

        for (int i=1; i < 5; i++) {
            Rank rank = (Rank) distance.get(i);
            assertEquals(2.0f        , rank.getRanking(), EPSILON);
        }  

        for (int i=5; i < 9; i++) {
            Rank rank = (Rank) distance.get(i);
            assertEquals(3.41f        , rank.getRanking(), EPSILON);
        }  

        for (int i=9; i < 13; i++) {
            Rank rank = (Rank) distance.get(i);
            assertEquals(4.0f        , rank.getRanking(), EPSILON);
        }  
        Rank rank13 = (Rank) distance.get(13);
        assertEquals(Distance.INFINITE , rank13.getRanking(), EPSILON);
        assertSame (this, rank13.getBasedObject());
    }

    /**
     * Test Ranking using {@link ManhattanDistance}
     */
    public void testWeightedDistanceFactor() {
        
        Point2D basePoint = new Point2D.Float(2,2);        
        
        Collection points = new ArrayList();
        points.add(new Point2D.Float(1,2));
        points.add(new Point2D.Float(3,2));
        points.add(new Point2D.Float(2,3));
        points.add(new Point2D.Float(2,1));
        points.add(basePoint);
        points.add(new Point2D.Float(2,0));
        points.add(new Point2D.Float(2,4));
        points.add(new Point2D.Float(0,2));
        points.add(new Point2D.Float(4,2));
        points.add(new Point2D.Float(1,3));
        points.add(new Point2D.Float(3,3));
        points.add(new Point2D.Float(3,1));
        points.add(new Point2D.Float(1,1));
        
        WeightedDistance wd = new WeightedDistance(
               new Distance[] { EuclidDistance.INSTANCE, ManhattanDistance.INSTANCE },
               new float[]    { 0.5f                   , 0.5f }, true);
 
        assertEquals(0.0f, wd.distance(basePoint, basePoint), EPSILON);
        assertEquals(0.0f, wd.getBaseValue(0)   , EPSILON);
        assertEquals(0.0f, wd.getBaseValue(1)   , EPSILON);
       
        List distance = Ranking.ranking(basePoint, points, wd);
        Rank rank0 = (Rank) distance.get(0);
        // Basepoint is nearest to itself.
        assertEquals(basePoint   , rank0.getBasedObject()); 
        assertEquals(0.0f        , rank0.getRanking(), EPSILON); 

        assertEquals(1.4142f, wd.getBaseValue(0)   , EPSILON);
        assertEquals(2.0    , wd.getBaseValue(1)   , EPSILON);

        for (int i=1; i < 5; i++) {
            Rank rank = (Rank) distance.get(i);
            assertEquals(1.0f        , rank.getRanking(), EPSILON);
        }  
        
        for (int i=5; i < 9; i++) {
            Rank rank = (Rank) distance.get(i);
            assertEquals(1.70f        , rank.getRanking(), EPSILON);
        }  

        for (int i=9; i < 13; i++) {
            Rank rank = (Rank) distance.get(i);
            assertEquals(2.0f        , rank.getRanking(), EPSILON);
        }  
    }

	public static Test suite() {
		return new TestSuite(TestWeightedDistance.class);
	}

}

