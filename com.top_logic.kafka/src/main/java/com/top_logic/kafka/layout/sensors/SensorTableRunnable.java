/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.sensors;

import java.util.Date;

import com.top_logic.kafka.layout.sensors.ProgressTableComponent.TableComponentValueUpdater;
import com.top_logic.kafka.services.sensors.Sensor;
import com.top_logic.kafka.services.sensors.SensorService;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.EditableRowTableModel;

/**
 * Identify sensors which have changed their signal or state.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SensorTableRunnable implements TableComponentValueUpdater {

    private Date _lastUpdate;

    private Date updateTimestamp() {
        Date theDate = (_lastUpdate != null) ? _lastUpdate : new Date(0);

        _lastUpdate = new Date();

        return theDate;
    }

	@Override
	public void update(ProgressTableComponent progressTableComponent) {
        final EditableRowTableModel model  = progressTableComponent.getTableModel();

		for (final Sensor<?, ?> sensor : SensorService.getInstance().getSensors()) {
        	// the sensor is known
			if(model.containsRowObject(sensor)) {
				
				// the sensor is visible
				final int rowIndex = model.getRowOfObject(sensor);

				if(TableModel.NO_ROW != rowIndex) {
					model.updateRows(rowIndex, rowIndex);
				}
			}
			else {
				// add a new sensor
				model.addRowObject(sensor);
			}
		}
        
        // update the timestamp
        updateTimestamp();
	}
}

