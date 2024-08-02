/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.wrap.person.Homepage;
import com.top_logic.knowledge.wrap.person.Homepage.ChannelValue;
import com.top_logic.knowledge.wrap.person.Homepage.Path;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ui.LayoutComponentResolver;
import com.top_logic.layout.scripting.runtime.LiveActionContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.tool.boundsec.BoundMainLayout;


/**
 * Implements the goto handling on startup.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TLMainLayout extends BoundMainLayout {

    public interface Config extends BoundMainLayout.Config {
		@Name(EVENT_HANDLER_ATTRIBUTE)
		@InstanceDefault(EventHandler.class)
		@InstanceFormat
		EventHandler getEventHandler();
	}

	private static final String EVENT_HANDLER_ATTRIBUTE = "eventHandler";

	/** Count recursive calls to modelChanged() for debugging */
    private int eventRecursion;

    /** Handler for model events. */
    private final EventHandler eventHandler;

    public TLMainLayout(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        
        this.eventHandler = atts.getEventHandler();
    }

    @Override
	@Deprecated
	public void modelChanged(Object aModel, Object changedBy, int eventType) {
		broadcastModelEvent(aModel, changedBy, eventType);
    }

    @Override
	public final void broadcastModelEvent(Object aModel, Object changedBy, int eventType) {
        this.eventRecursion ++;
        try {
        	long elapsed = -System.currentTimeMillis();
        	{
                if (changedBy instanceof LayoutComponent) {
					this.eventHandler.broadcastModelEvent(this, aModel, (LayoutComponent) changedBy, eventType);
                } else {
					this.fallbackBroadcastModelEvent(aModel, changedBy, eventType);
                }
        	}
			elapsed += System.currentTimeMillis();
        	
        	if (elapsed >= 500) {
        		Logger.warn(this.eventRecursion + "#" + eventType + " modelChanged " 
        				+ DebugHelper.getTime(elapsed) + " Model " + aModel + " changedBy " + changedBy, TLMainLayout.class);
        	}
        } finally {
        	this.eventRecursion --;
        }
    }
    
    /** 
     * Call back for the super implementation of {@link #internalBroadcastModelEvent(Object, Object, int)}.
     */
    /*package protected*/ final void fallbackBroadcastModelEvent(Object aModel, Object changedBy, int aType) {
		super.broadcastModelEvent(aModel, changedBy, aType);
    }

	/**
	 * Append the goto handling to the start page.
	 */
    @Override
	protected void initialValidateModel(DisplayContext aContext) {
		// Restore home page.
		{
			PersonalConfiguration personalConfig = PersonalConfiguration.getPersonalConfiguration();
			if (personalConfig != null) {
				try {
					restoreHomepage(personalConfig);
				} catch (RuntimeException ex) {
					Logger.warn("Failed to restore home page.", ex, TLMainLayout.class);
					personalConfig.removeHomepage(this);
				}
			}
		}

        super.initialValidateModel(aContext);
    }

	private void restoreHomepage(PersonalConfiguration personalConfig) {
		Homepage homepage;
		try {
			homepage = personalConfig.getHomepage(this);
		} catch (ConfigurationException ex) {
			handleHomepageFormatChanged(personalConfig);
			return;
		}
		if (homepage == null) {
			// No home page set.
			return;
		}

		boolean anythingChanged = installComponentPath(personalConfig, homepage);
		if (anythingChanged) {
			this.invalidate();
		}
	}

	private boolean installComponentPath(PersonalConfiguration personalConfig, Homepage homepage) {
		boolean anythingChanged = false;
		LiveActionContext context = new LiveActionContext(DefaultDisplayContext.getDisplayContext(), this);
		for (Path step : homepage.getComponentPaths()) {
			ComponentName targetComponentName = step.getComponent();
			LayoutComponent component = getComponentByName(targetComponentName);
			if (component == null) {
				handleTargetComponentUnresolvable(personalConfig);
				return anythingChanged;
			}

			boolean visible = component.makeVisible();
			if (!visible) {
				return anythingChanged;
			}
			for (ChannelValue channelValue : step.getChannelValues().values()) {
				Object value;
				try {
					value = locateModel(context, channelValue.getValue());
				} catch (RuntimeException | AssertionError ex) {
					handleTargetObjectUnresolvable(personalConfig);
					continue;
				}
				component.getChannel(channelValue.getName()).set(value);
			}
		}
		return anythingChanged;
	}

	/**
	 * Resolves the model of the {@link Homepage}.
	 */
	private Object locateModel(LiveActionContext context, ModelName name) {
		/* Ensure that eventually hidden components are found. Such a component is made visible when
		 * displaying model. */
		boolean before = LayoutComponentResolver.allowResolvingHiddenComponents(context, true);
		try {
			return ModelResolver.locateModel(context, name);
		} finally {
			LayoutComponentResolver.allowResolvingHiddenComponents(context, before);
		}
	}

	private void handleHomepageFormatChanged(PersonalConfiguration personalConfig) {
		resetHomepage(personalConfig, I18NConstants.HOMEPAGE_RESTORE_PROBLEM_SCHEMA_CHANGED);
	}

	private void handleTargetComponentUnresolvable(PersonalConfiguration personalConfig) {
		resetHomepage(personalConfig, I18NConstants.HOMEPAGE_RESTORE_PROBLEM_COMPONENT_INVALID);
	}

	private void handleTargetObjectUnresolvable(PersonalConfiguration personalConfig) {
		resetHomepage(personalConfig, I18NConstants.HOMEPAGE_RESTORE_PROBLEM_MODEL_INVALID);
	}

	private void resetHomepage(PersonalConfiguration personalConfig, ResKey detail) {
		InfoService.showInfo(I18NConstants.HOMEPAGE_RESTORE_PROBLEM, detail);
		personalConfig.removeHomepage(this);
	}

    /**
     * Processor for model events to be send to other components. 
     * 
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
    public static class EventHandler {

    	public static final EventHandler INSTANCE = new EventHandler();
    	
        // Public methods

		/**
		 * Dispatch the model firing to several methods.
		 * 
		 * @param mainLayout
		 *        The {@link TLMainLayout} that can be used to
		 *        {@link TLMainLayout#fallbackBroadcastModelEvent(Object, Object, int)
		 *        fallback to default broadcast semantics}.
		 */
        public final void broadcastModelEvent(TLMainLayout mainLayout, Object aModel, LayoutComponent sender, int aType) {
            if (Logger.isDebugEnabled(this)) {
                LayoutUtils.logEvent(aType, sender, aModel, mainLayout);
            }
            switch (aType) {
                case ModelEventListener.MODEL_DELETED : 
                case ModelEventListener.MODEL_CREATED :
                case ModelEventListener.MODEL_MODIFIED :
					this.fireModelModified(mainLayout, aModel, sender, aType);
					return;
                    
				case ModelEventListener.SECURITY_CHANGED:
					dispatchSecurityChanged(aModel, sender, aType);
					return;

                default:
					fallbackBroadcastEvent(mainLayout, aModel, sender, aType);
					return;
            }
        }

		private void dispatchSecurityChanged(Object model, LayoutComponent sender, int type) {
			dispatchSecurityChangedTo(sender.getParent(), sender, model, type);
		}

		private static void dispatchSecurityChangedTo(LayoutComponent receiver, LayoutComponent sender, Object model,
				int type) {
			if (receiver != null) {
				receiver.handleModelEvent(model, sender, type);
				dispatchSecurityChangedTo(receiver.getParent(), sender, model, type);
			}
		}

        // Protected methods

        /** 
         * Fire the model modified events to all components known by the main layout.
         * 
		 * @param mainLayout See {@link #fireModelModified(TLMainLayout, Object, LayoutComponent, int)}
         */
        protected void fireModelModified(TLMainLayout mainLayout, Object aModel, LayoutComponent sender, int aType) {
			fallbackBroadcastEvent(mainLayout, aModel, sender, aType);
        }

        /**
         * Dispatches the given event with the default broadcast semantics.
         * 
         * @see MainLayout#broadcastModelEvent(Object, Object, int)
         */
		protected static final void fallbackBroadcastEvent(TLMainLayout mainLayout, Object aModel,
				LayoutComponent sender, int eventType) {
			mainLayout.fallbackBroadcastModelEvent(aModel, sender, eventType);
		}
        
    }

}
