/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * Instances of this class are responsible for managing drawings for excel sheets.
 * 
 * @author <a href="mailto:wta@top-logic.com">wta</a>
 */
public class POIDrawingManager {

	/**
	 * A {@link Map} of {@link Sheet} to the associated {@link Drawing}.
	 */
	private final Map<Sheet, Drawing> _drawingBySheet = new HashMap<>();
    
	/**
	 * Returns the top-level {@link Drawing} instance for the specified sheet. If the specified
	 * sheet did not have any drawing yet, a new one will be created.
	 * 
	 * @param sheet
	 *        the {@link Sheet} to retrieve the drawing for
	 * @return the top-level {@link Drawing} for the specified sheet
	 */
	public Drawing getDrawing(final Sheet sheet) {
		Drawing drawing = _drawingBySheet.get(sheet);
        
		if (drawing == null) {
			drawing = sheet.createDrawingPatriarch();
			_drawingBySheet.put(sheet, drawing);
        }
        
		return drawing;
    }
}
