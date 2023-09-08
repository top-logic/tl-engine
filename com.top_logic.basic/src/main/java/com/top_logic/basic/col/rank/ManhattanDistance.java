/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.rank;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Compute the <a href="http://en.wikipedia.org/wiki/Manhattan_distance">Manhatten</a> 
 * / Taxi {@link Distance} between two {@link Point}s.
 }
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class ManhattanDistance implements Distance {

    /** as this class is stateless a Singleton will do */
    public static ManhattanDistance INSTANCE = new ManhattanDistance();
    
    /** 
     * Enforce usage of INSTANCE by private CTor.
     */
    private ManhattanDistance() { /* nothing to do here */ }

    /**
     * Check for correct class and then forward to specialized variant.
     */
    @Override
	public float distance(Object o1, Object o2) {
        if (! (o1 instanceof Point2D) || ! (o2 instanceof Point2D)) {
            return INFINITE; 
        }
        return distance((Point2D) o1, (Point2D) o2);
    }
    
    /**
     * Compute Manhattan distance between two Points.
     */
    public float distance(Point2D p1, Point2D p2) {
        return Math.abs((float) (p1.getX() - p2.getX())) 
             + Math.abs((float) (p1.getY() - p2.getY()));
    }

}

