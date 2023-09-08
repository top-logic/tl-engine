/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.I18NConstants;
import com.top_logic.layout.NotifyListener;
import com.top_logic.layout.UpdateListener;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.Validator;
import com.top_logic.layout.basic.ActivateCommand;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The {@link ControlSupport} is used for registering {@link UpdateListener} (to
 * receive AJXAX updates for the GUI) and {@link CommandListener} to execute
 * commands (triggered by the GUI).
 *
 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
 */
public class ControlSupport implements AJAXSupport, ControlScope {

    private final LayoutComponent component;

    /**
     * Lazily initialized set which contains all UpdateListener to receive
     * updates for the GUI.
     */
    /*package protected*/ HashSet<UpdateListener> lazyUpdateListener;

    private boolean markedAsInvalid;

	/**
	 * caches whether someone told this {@link ControlSupport} to
	 * {@link #disableScope(boolean) disable all views}
	 */
	private boolean disabled;

    /**
     * Creates a {@link ControlSupport}.
     */
    public ControlSupport(LayoutComponent component) {
        this.component = component;
    }

	public LayoutComponent getComponent() {
		return this.component;
	}

    @Override
	public void startRendering() {
        /*
         * Before rendering, controls must only be detached, if the component is
         * rendered by a JSP (the component cannot know, which controls will be
         * displayed during the upcomming rendering).
         */
    	markedAsInvalid = false;
    }

    /**
     * Checks, whether there are any invalid controls that require an
     * (incremental) update.
     *
     * @see AJAXComponent#isRevalidateRequested()
     */
    @Override
	public boolean isRevalidateRequested() {
        if (lazyUpdateListener == null) {
            return false;
        }
        for (Iterator it = lazyUpdateListener.iterator(); it.hasNext();) {
            if (((UpdateListener) it.next()).isInvalid()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Revalidates all registered {@link UpdateListener}, if the component is invalid.
     *
     * @see com.top_logic.layout.basic.component.AJAXSupport#revalidate(com.top_logic.layout.DisplayContext,
     *      UpdateQueue)
     */
    @Override
	public final void revalidate(DisplayContext context, UpdateQueue actions) {
		context.validateScoped(this, ControlSupportValidator.INSTANCE, actions, this);
	}

	/**
	 * Implementation of {@link #revalidate(DisplayContext, UpdateQueue)} using this as current
	 * {@link DisplayContext#getExecutionScope()} for the revalidation process..
	 * 
	 * @see #revalidate(DisplayContext, UpdateQueue)
	 */
	protected void revalidateScoped(DisplayContext context, UpdateQueue actions) {
		if (!component.isInvalid()) {
			if (lazyUpdateListener != null) {
				for (Iterator<UpdateListener> it = lazyUpdateListener.iterator(); it.hasNext();) {
					UpdateListener updateListener = it.next();

					if (updateListener.isInvalid()) {
						updateListener.revalidate(context, actions);
					}
				}
			}
		}
    }

    /**
     * Registers an {@link UpdateListener} for receiving GUI-Updates.
     *
     * @see com.top_logic.layout.ControlScope#addUpdateListener(UpdateListener)
     */
    @Override
	public final void addUpdateListener(UpdateListener aListener) {
    	if (markedAsInvalid || aListener == null) {
    		return;
    	}
        if (lazyUpdateListener == null) {
            lazyUpdateListener = new HashSet<>();
        }
        final boolean succeded = lazyUpdateListener.add(aListener);
        if (succeded) {
        	aListener.notifyAttachedTo(this);
        }
    }

    /**
	 * Removes all registered listener and informs them about removing.
	 */
    protected void detachDisplayedControls() {
        // Hide all currently displayed controls.
        if (lazyUpdateListener != null && !lazyUpdateListener.isEmpty()) {
            // Prevent concurrent modification exceptions.
            HashSet<NotifyListener> listenerToRemove = new HashSet<>(lazyUpdateListener);

            for (Iterator<NotifyListener> it = listenerToRemove.iterator(); it.hasNext();) {
                it.next().notifyDetachedFrom(this);
            }

            // Control shold deregister, when they are detached. Therefore, the
            // map should be already empty here. However, there seems to be
            // conditions, where this does not work and a detached control stays
            // in the map. At this point, the UI is broken. To avoid this, the
            // map is additionally cleared.
            lazyUpdateListener.clear();
        }
    }

    /**
     * Removes the given <code>aListener</code>.
     *
     * @see com.top_logic.layout.ControlScope#removeUpdateListener(com.top_logic.layout.UpdateListener)
     */
    @Override
	public final boolean removeUpdateListener(UpdateListener aListener) {
		if (lazyUpdateListener == null) {
            return false;
        }

        final boolean succeded = lazyUpdateListener.remove(aListener);
        if (succeded && aListener != null) {
        	aListener.notifyDetachedFrom(this);
        }
		return succeded;
    }

    @Override
	public FrameScope getFrameScope() {
        return component.getEnclosingFrameScope();
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

    /**
     * This method dispatches the execution of the given <code>commandName</code> to the
     * {@link CommandListener} registered under <code>listenerId</code>.
     */
	public HandlerResult executeCommand(DisplayContext context, String listenerId, String commandName,
			Map<String, Object> arguments) {
    	CommandListener commandListener = getFrameScope().getCommandListener(listenerId);
    	
        if (commandListener != null) {
			if (ScriptingRecorder.isRecordingActive()
				&& commandListener instanceof ButtonControl
				&& ActivateCommand.COMMAND_ID.equals(commandName)) {
				ScriptingRecorder.recordButtonCommand(component, (ButtonControl) commandListener);
			}

        	return commandListener.executeCommand(context, commandName, arguments);
        } else {
			if (arguments.containsKey(AJAXCommandHandler.SYSTEM_COMMAND_ID)) {
				if ((Boolean) arguments.get(AJAXCommandHandler.SYSTEM_COMMAND_ID)) {
					// This is an automatically triggered command. If the control is no longer
					// present on the server, another user action may already hava disposed the
					// control. This can happen in response to high click frequencies.
					return HandlerResult.DEFAULT_RESULT;
				}
			}

			HandlerResult result = new HandlerResult();
			result.addErrorMessage(I18NConstants.ERROR_TARGET_CONTROL_NOT_FOUND);
	        getFrameScope().addClientAction(JSSnipplet.createPageReload());
			return result;
        }
    }

	@Override
	public void invalidate() {
        detachDisplayedControls();
        markedAsInvalid = true;
    }

    /**
	 * The class {@link ControlSupport.ControlSupportValidator} is a singleton
	 * {@link Validator} which is used to validate {@link ControlSupport}s.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
    private static class ControlSupportValidator implements Validator<ControlSupport> {

        /*package protected*/ static Validator<ControlSupport> INSTANCE = new ControlSupportValidator();

        @Override
		public void validate(DisplayContext context, UpdateQueue queue, ControlSupport validationObject) {
			validationObject.revalidateScoped(context, queue);
        }
    }
}

