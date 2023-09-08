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

import com.top_logic.basic.col.rank.EuclidDistance;
import com.top_logic.basic.col.rank.ManhattanDistance;
import com.top_logic.basic.col.rank.Ranking;
import com.top_logic.basic.col.rank.Ranking.Rank;

/**
 * Test the {@link Ranking}
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestRanking extends TestCase {

   /** Use whith {@link Assert#assertEquals(float, float, float)} */
    protected static final float         EPSILON = 0.1f;
 
    /** 
     * Create a new TestRanking.
     */
    public TestRanking(String name) {
        super(name);
    }

    /**
     * Test Ranking using {@link EuclidDistance}
     */
    public void testEuclidRanking() {
        
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
        
        List distance = Ranking.ranking(basePoint, points, EuclidDistance.INSTANCE);
        Rank rank0 = (Rank) distance.get(0);
        assertNotNull(rank0.toString());
        
        // Basepoint is nearest to itself.
        assertEquals(basePoint, rank0.getBasedObject()); 
        assertEquals(0.0f        , rank0.getRanking(), EPSILON); 

        for (int i=1; i < 5; i++) {
            Rank rank = (Rank) distance.get(i);
            assertEquals(1.0f        , rank.getRanking(), EPSILON);
        }  

        for (int i=5; i < 9; i++) {
            Rank rank = (Rank) distance.get(i);
            assertEquals(1.41f        , rank.getRanking(), EPSILON);
        }  

        for (int i=9; i < 13; i++) {
            Rank rank = (Rank) distance.get(i);
            assertEquals(2.0f        , rank.getRanking(), EPSILON);
        }  
    }

    /**
     * Test Ranking using {@link ManhattanDistance}
     */
    public void testManhattanRanking() {
        
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
        
        List distance = Ranking.ranking(basePoint, points, ManhattanDistance.INSTANCE);
        Rank rank0 = (Rank) distance.get(0);
        // Basepoint is nearest to itself.
        assertEquals(basePoint, rank0.getBasedObject()); 
        assertEquals(0.0f        , rank0.getRanking(), EPSILON); 

        for (int i=1; i < 5; i++) {
            Rank rank = (Rank) distance.get(i);
            assertEquals(1.0f        , rank.getRanking(), EPSILON);
        }  
        
        // In Manhatten Circles work some other way ;-)
        for (int i=5; i < 13; i++) {
            Rank rank = (Rank) distance.get(i);
            assertEquals(2.0f        , rank.getRanking(), EPSILON);
        }  
    }

    /**
     * Call the (default) CTor.
     */
    public void testCTor() {
        new Ranking() { /* just to get 100% coverage ;-) */ };
    }
    
	public static Test suite() {
		return new TestSuite(TestRanking.class);
	}

}

