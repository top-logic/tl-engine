/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.base.services.simpleajax.AbstractSystemAjaxCommand;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ControlComponent} is a holder for a set of active
 * {@link Control} elements, which can be dynamically updated during the
 * lifetime of the {@link ControlComponent}'s page.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ControlComponent extends BoundComponent {

	/**
	 * Configuration for the {@link WindowComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends BoundComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			BoundComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerCommand(DispatchAction.COMMAND_NAME);
		}

	}

	/**
	 * Hold the {@link Control}s that are managed by Tags or similar objects that can not store them
	 * in their context.
	 * 
	 * Lazily initialized map of {@link Control#getID() control identifiers} to their
	 * {@link Control}s.
	 */
	private HashMap<String, Control> lazyGlobalControls;

    protected final ControlSupport controlSupport;
	
	public ControlComponent(InstantiationContext context, Config attr) throws ConfigurationException {
	    super(context, attr);
	    this.controlSupport = createControlSupport();
	}

    protected ControlSupport createControlSupport() {
        return new ControlSupport(this);
    }

	
	@Override
	protected final AJAXSupport ajaxSupport() {
		return controlSupport;
	}
	
	@Override
	public final ControlScope getControlScope() {
		return controlSupport;
	}
	
	/**
	 * Add a new {@link Control}  under the given key to this component.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation, see Ticket #13084.
	 */
	@Deprecated
	public final void addControl(String key, Control control) {
		if (this.lazyGlobalControls == null) {
			this.lazyGlobalControls = new HashMap<>();
		}
		
		this.lazyGlobalControls.put(key, control);
	}

    /**
     * Remove a {@link Control} from this component.
     * 
     * @param aControlKey
     *        A key which was used to store the control to remove.
     * @return Returns <code>true</code> if the control could be removed,
     *         <code>false</code> otherwise.
     *         
	 * TODO #6121: Delete TL 5.8.0 deprecation, see Ticket #13084.
	 */
	@Deprecated
	public boolean removeControl(String aControlKey) {
        if (lazyGlobalControls == null) {
            return false;
        }
        
        Control removedControl = lazyGlobalControls.remove(aControlKey);
        if (removedControl != null) {
        	// Stop the removed control from sending events.
        	removedControl.detach();
        }
		return removedControl != null;
    }
	
	/**
	 * Overridden to drop all registered {@link Control}s, since this
	 * component's {@link Control}s are recreated when it is shown the next
	 * time.
	 * 
	 * @see LayoutComponent#becomingInvisible()
	 */
	@Override
	protected void becomingInvisible() {
		resetControls();

		super.becomingInvisible();
	}
	
	/**
	 * @deprecated no longer called, override {@link #beforeRendering(DisplayContext)}.
	 */
	@Deprecated
	public final void startRendering() {
		// Ignored.
    }
	
	/**
	 * {@link Control#detach() Detach} all currently
	 * {@link ControlComponent#addControl(String, Control) registered} controls
	 * and drop the internal {@link Control} list.
	 */
	protected void resetControls() {
		// Clear the currently registered controls. The controls become
		// registered (created) again, if this component becomes visible again.
		controlSupport.detachDisplayedControls();
		if (lazyGlobalControls != null) {
			for (Iterator<Control> it = lazyGlobalControls.values().iterator(); it.hasNext(); ) {
				it.next().detach();
			}
			lazyGlobalControls.clear();
		}
	}

	/**
	 * TODO #6121: Delete TL 5.8.0 deprecation, see Ticket #13084.
	 */
	@Deprecated
	public Control getControl(String key) {
		if (lazyGlobalControls == null) return null;
		
		return lazyGlobalControls.get(key);
	}

	/**
	 * The only command that needs to be to be registered at a
	 * {@link ControlComponent} to forward {@link ControlCommand}s to their
	 * {@link Control}s.
	 */
	public static final class DispatchAction extends AbstractSystemAjaxCommand {

		/**
		 * Parameter name that identifies the command, which should be executed
		 * at the referenced {@link Control}.
		 * 
		 * <p>
		 * The {@link Control} itself is identified by the
		 * {@link ControlCommand#CONTROL_ID_PARAM} parameter name.
		 * </p>
		 */
		public static final String CONTROL_COMMAND_PARAM = "controlCommand";
		
		/**
		 * {@link CommandHandler#getID() ID} of the {@link DispatchAction} in the
		 * {@link CommandHandlerFactory}.
		 */
		public static final String COMMAND_NAME = "dispatchControlCommand";

		/**
		 * Configuration for the {@link DispatchAction}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public static interface Config extends AbstractSystemAjaxCommand.Config {

			@Override
			@StringDefault(COMMAND_NAME)
			String getId();

		}

		public DispatchAction(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public final HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			String controlID = 
				(String) someArguments.get(ControlCommand.CONTROL_ID_PARAM);
			
			String controlCommandName = 
				(String) someArguments.get(CONTROL_COMMAND_PARAM);

			ControlSupport controlSupport = (ControlSupport) aComponent.getControlScope();
			return controlSupport.executeCommand(aContext, controlID, controlCommandName, someArguments);
		}

		@Override
		@Deprecated
		public ResKey getDefaultI18NKey() {
			return I18NConstants.COMMAND_DISPATCH_COMMAND;
		}

		@Override
		protected boolean mustNotRecord(DisplayContext context, LayoutComponent component,
				Map<String, Object> someArguments) {
			return true;
		}

	}

	/**
	 * @deprecated override {@link #writeBody(ServletContext, HttpServletRequest, HttpServletResponse, TagWriter)}
	 */
	@Deprecated
	public final void writeControlBody(ServletContext context, HttpServletRequest req, HttpServletResponse resp, TagWriter out) 
	    throws IOException, ServletException {
	    // can be removed after ?
	}
	
	/**
	 * @see com.top_logic.mig.html.layout.LayoutComponent#invalidate()
	 */
	@Override
	public void invalidate() {
	    super.invalidate();
	    resetControls();
	}

}
