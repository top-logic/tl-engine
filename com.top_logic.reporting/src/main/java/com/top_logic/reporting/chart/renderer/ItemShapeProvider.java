/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.renderer;

import java.awt.Image;
import java.util.Random;

import javax.swing.ImageIcon;

/**
 * Provides an image as shape for the items of a {@link TemplateXYLineAndShapeRenderer}.
 *
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class ItemShapeProvider {
	
	private Random random = new Random();
	Image image;
	
	public ItemShapeProvider(String aPath) {
		image = new ImageIcon(aPath).getImage();
    }
	
	/**
	 * The shape of the item in the given row and column.
	 *
	 * @return The image of this provider.
	 */
	public Object getShape(int aRow, int aColumn) {
		return image;
	}

	/**
	 * Whether the item in the given row and column is displayed with the image of this provider.
	 *
	 * <p>
	 * Note: The decision is made randomly.
	 * </p>
	 */
	public boolean useImage(int aRow, int aColumn) {
		if (random.nextInt(2) == 0) {
            return true; 
        }
        else {
            return false;
        }
    }
}
