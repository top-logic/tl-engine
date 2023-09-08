/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.layout.CommandListener;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.UpdateListener;

/**
 * Simple Implementation of {@link ControlScope}
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class SimpleControlScope implements ControlScope {

	/**
	 * @see #isScopeDisabled()
	 */
	private boolean disabled;
	
    /**
	 * Hold the {@link UpdateListener} to receive updates.
	 */
    private Set<UpdateListener> lazyUpdateListener;
    
    /**
	 * Lazily initialized map of {@link CommandListener} identified by their id
	 */
    private Map<String, CommandListener> lazyCommandListener;
    
	private FrameScope frameScope = SimpleFrameScope.createSimpleFrameScope(null);

    @Override
	public void addUpdateListener(UpdateListener aListener) {
    	if (lazyUpdateListener == null) {
    		lazyUpdateListener = new HashSet<>();
    	}
		lazyUpdateListener.add(aListener);
    }

	@Override
	public boolean removeUpdateListener(UpdateListener aListener) {
		if (lazyUpdateListener == null) {
			return false;
		}
        return lazyUpdateListener.remove(aListener);
    }

    @Override
	public FrameScope getFrameScope() {
    	return frameScope;
    }

	@Override
	public void disableScope(boolean disable) {
		if (disabled == disable) {
			return;
		}
		disabled = disable;
		if (lazyUpdateListener != null) {
			for (UpdateListener updater : lazyUpdateListener) {
				updater.notifyDisabled(disable);
			}
		}
	}
	
	@Override
	public boolean isScopeDisabled() {
		return disabled;
	}

}

