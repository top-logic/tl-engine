/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.demo.info;

import java.awt.Image;
import java.util.Random;

import javax.swing.ImageIcon;

import org.jfree.data.category.CategoryDataset;

import com.top_logic.reporting.chart.demo.OverviewMain;
import com.top_logic.reporting.chart.info.OverviewInfo;

/**
 * The ImageOverviewInfo generates a example overview chart with images.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ImageOverviewInfo extends OverviewInfo {
    
    private Random random = new Random();
    private Image  ball   = new ImageIcon(OverviewMain.PATH + "img/ball.jpg").  getImage();
    private Image  snoopy = new ImageIcon(OverviewMain.PATH + "img/snoopy.jpg").getImage();
    
    /** 
     * @see com.top_logic.reporting.chart.info.TemplateInfo#getShape(double, double, double, double, int, int, org.jfree.data.category.CategoryDataset)
     */
    @Override
	public Object getShape(double aX, double aY, double aWidth, double aHeight, int aRow, int aColumn, CategoryDataset aDataset) {
        if (random.nextInt(2) == 0) {
            return ball; 
        }
        else {
            return snoopy;
        }
    }
    
}

