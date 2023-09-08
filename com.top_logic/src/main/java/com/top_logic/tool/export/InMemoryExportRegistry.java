/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.export.Export.State;
import com.top_logic.util.TLContext;

/**
 * The InMemoryExportRegistry is a basic implementation of {@link ExportRegistry}. It does
 * not provide the functionality of persistent exports nor it is able to run in a cluster.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class InMemoryExportRegistry implements ExportRegistry {

	private static ExportRegistry instance;
	
	private final Map<Tuple, Export> exports = new HashMap<>();

	public static synchronized ExportRegistry getInstance() {
		if (instance == null) {
			instance = new InMemoryExportRegistry();
		}
		return instance;
	}
	
    @Override
	public Export getExport(String aHandlerID, Object aModel) {
        assert aModel instanceof Wrapper;

        ExportHandler theHandler = ExportHandlerRegistry.getInstance().getHandler(aHandlerID);
        Person thePerson = null;
        if (theHandler.isPersonalized()) {
        	thePerson = TLContext.getContext().getCurrentPersonWrapper();
        }
        
        Tuple theKey = TupleFactory.newTuple(aHandlerID, aModel, thePerson);
        Export theExport = this.exports.get(theKey);
        if (theExport == null) {
            theExport = new DefaultExport(aHandlerID, (Wrapper) aModel, thePerson);
            this.exports.put(theKey, theExport);
        }
        return theExport;
    }

	@Override
	public Export getNextRunningExport(final String aTechnology, final String aDuration) throws ExportFailure {
        List<Export> theExports = new ArrayList<>(this.exports.values());

        theExports = FilterUtil.filterList(new Filter<Export>() {
            @Override
			public boolean accept(Export aAnObject) {
                return State.QUEUED == aAnObject.getState() && aTechnology.equals(aAnObject.getExportTechnology());
            }
        }, theExports);

        if (theExports.isEmpty()) {
            return null;
        }

        // sort by duration, mine first
        // sort by queue date, descending
        Collections.sort(theExports, new Comparator<Export>() {
            @Override
			public int compare(Export aO1, Export aO2) {
                long d1 = aO1.getTimeQueued().getTime();
                long d2 = aO2.getTimeQueued().getTime();

                // if its not our duration, then push the time into the future
                // this will move the export backwards in the sorted list
                if (!aDuration.equals(aO1.getExportDuration())) {
                    d1 += System.currentTimeMillis();
                }
                if (!aDuration.equals(aO2.getExportDuration())) {
                    d2 += System.currentTimeMillis();
                }
                return Double.compare(d1, d2);
            }
        });

        Export theExport = theExports.get(0);
        if (theExport.setStateRunning()) {
            return theExport;
        }
        else {
            return null;
        }
    }

    public void clear() {
        this.exports.clear();
    }

	@Override
	public void startup() {
		// Nothing special to do here.
	}

	@Override
	public void shutdown() {
		clear();
	}
}
