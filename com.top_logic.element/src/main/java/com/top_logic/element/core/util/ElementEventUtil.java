/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core.util;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.event.ModelTrackingService;
import com.top_logic.model.TLObject;

/**
 * Utilities for sending generic {@link MonitorEvent}s for {@link TLObject}s.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ElementEventUtil {

    /**
	 * Sends a {@link MonitorEvent} on the {@link ModelTrackingService}.
	 * 
	 * @param object
	 *        An object, used as message of the event.
	 * @param eventType
	 *        An event type, see {@link MonitorEvent#CREATED}, {@link MonitorEvent#MODIFIED},
	 *        {@link MonitorEvent#DELETED}.
	 */
	public static void sendEvent(TLObject object, String eventType) {
        if (! ModelTrackingService.Module.INSTANCE.isActive()) {
            return; 
        }
        
		TLObject parent = object instanceof StructuredElement ? ((StructuredElement) object).getParent() : null;
		if (parent == null) {
			parent = object;
        }
        
		if (object.tValid() && parent.tValid()) {
			ModelTrackingService.sendEvent(object, parent, eventType);
        }
    }
}

