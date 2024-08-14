/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.common.folder.ui.FolderComponent;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Homepage;
import com.top_logic.knowledge.wrap.person.Homepage.ChannelValue;
import com.top_logic.knowledge.wrap.person.Homepage.Path;
import com.top_logic.knowledge.wrap.person.NoStartPageAutomatismExecutability;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.channel.ModelChannel;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * Class for setting the personal home-page.
 * 
 * The home page should normally be a business component (in this case a {@link FormComponent})
 * which has a model of type {@link Wrapper}. If the found {@link FormComponent} is also a
 * {@link Selectable}, take the selection, otherwise the {@link LayoutComponent#getModel() model} as
 * business object to be displayed.
 * 
 * If there is no matching business component, we use the nearest {@link Selectable} to find a
 * context we can reconstruct later on.
 * 
 * @author <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
 * 
 * @deprecated Use {@link SetHomepageHandler}.
 */
@Deprecated
public class CompoundSetHomepageHandler extends AbstractCommandHandler {

	/** {@link ConfigurationItem} of the {@link CompoundSetHomepageHandler}. */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@ListDefault(NoStartPageAutomatismExecutability.class)
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

	}

    /** The unique command ID for this handler. */
    public static final String COMMAND_ID = "setHomepage";

    /** 
     * Creates a {@link CompoundSetHomepageHandler}.
     */
    public CompoundSetHomepageHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
     * @see com.top_logic.tool.boundsec.CommandHandler#handleCommand(DisplayContext, com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		setHomepage(aComponent.getMainLayout());

        return HandlerResult.DEFAULT_RESULT;
    }

    /**
	 * Set the business component currently active as homepage (with its model) for the current
	 * user.
	 * 
	 * The method will store the homepage settings in the {@link PersonalConfiguration} which is (at
	 * this moment) a transient one. On logout the information will be transformed into a persistent
	 * variant.
	 * 
	 * @param mainLayout
	 *        The main layout containing all layout components, must not be <code>null</code>.
	 * @see #getHomepage(MainLayout)
	 */
	public void setHomepage(MainLayout mainLayout) {
		PersonalConfiguration personalConfiguration = PersonalConfiguration.getPersonalConfiguration();
		if (personalConfiguration != null) {
			personalConfiguration.setHomepage(mainLayout, getHomepage(mainLayout));
		}
    }

    /**
	 * Get the homepage information from the given main layout.
	 * 
	 * The method will traverse the layout tree to the innermost visible
	 * {@link CompoundSecurityLayout}. That will be handed over to the
	 * {@link #getInnerModel(LayoutContainer)} method for finding the real business component and
	 * its model.
	 * 
	 * @param mainLayout
	 *        The main layout containing all layout components, must not be <code>null</code>.
	 * @return The requested {@link Homepage} information. May be <code>null</code>.
	 */
	public Homepage getHomepage(MainLayout mainLayout) {
		CompoundSecurityLayout theProjectLayout = CompoundSecurityLayout.getNextVisibleChildCompoundLayout(mainLayout);
        CompoundSecurityLayout theInnerLayout   = null;

        while (theProjectLayout != null) {
            theInnerLayout   = theProjectLayout;
            theProjectLayout = CompoundSecurityLayout.getNextVisibleChildCompoundLayout(theProjectLayout);
        }

		Homepage homepage;
        if (theInnerLayout != null) {
			homepage = this.getInnerModel(theInnerLayout);
			if (homepage == null) {
				homepage = newHomePage(theInnerLayout);
			}
        } else {
			homepage = null;
        }


		return homepage;
    }

    /**
	 * Find the inner model for the goto handling.
	 * 
	 * If the normal mode didn't succeed in finding a component, this method will inspect the
	 * children and look for one child, which is holding a wrapper. The first component found will
	 * then be the target for the goto handling later on.
	 * 
	 * @param container
	 *        The container to find a matching child.
	 * @return The information model for the goto, may be <code>null</code>.
	 */
	protected Homepage getInnerModel(LayoutContainer container) {
		Homepage homepage = this.findFormComponent(container, true);
		if (homepage == null) {
			homepage = this.findSelectable(container, true);
        }
		if (homepage == null) {
			homepage = this.findFormComponent(container, false);
        }
		if (homepage == null) {
			homepage = this.findSelectable(container, false);
		}
		return homepage;
    }

    /**
	 * Find form components providing a wrapper based model for goto handling.
	 * 
	 * @param container
	 *        The container to find a matching child.
	 * @param withWrapper
	 *        <code>true</code> to accept form components only, when its model is a wrapper.
	 * @return The information model for the goto, may be <code>null</code>.
	 */
	protected Homepage findFormComponent(LayoutContainer container, boolean withWrapper) {
		Homepage homepage = null;
		for (LayoutComponent child : container.getChildList()) {
			if (!child.isVisible()) {
                continue;
            }
			else if (child instanceof LayoutContainer) {
				homepage = this.findFormComponent((LayoutContainer) child, withWrapper);
            }
			else if (child instanceof FolderComponent) {
				Object theModel = child.getModel();
				homepage = getHomepageFromModel(theModel, child, withWrapper);
            }
			else if (child instanceof FormComponent) {
				if (child instanceof Selectable) {
					Object theModel = ((Selectable) child).getSelected();
					homepage = getHomepageFromSelection(theModel, child, withWrapper);
                }
				if (homepage == null) {
					Object theModel = child.getModel();
					homepage = getHomepageFromModel(theModel, child, withWrapper);
                }
            }
        }

		return homepage;
    }

    /**
	 * Gets a {@link Homepage} for the given model and component.
	 */
	protected Homepage getHomepageFromModel(Object model, LayoutComponent component, boolean withWrapper) {
		ModelName target = extractTargetObjectFromModel(model);
		Homepage info ;
		if (target != null) {
			info = newHomePage(component, target, false);
		}
		else if (!withWrapper) {
			info = newHomePage(component);
		} else {
			info = null;
		}
		return info;
	}

	/**
	 * Extracts the wrapper from the model if available.
	 */
	protected ModelName extractTargetObjectFromModel(Object model) {
		return ModelResolver.buildModelNameIfAvailable(model).getElse(null);
	}

	/**
	 * Find selectable components providing selection for goto handling.
	 * 
	 * @param aContainer
	 *        The container to find a matching child.
	 * @param withWrapper
	 *        <code>true</code> to accept selectables only, when its selection is a wrapper.
	 * @return The information model for the goto, may be <code>null</code>.
	 */
	protected Homepage findSelectable(LayoutContainer aContainer, boolean withWrapper) {
		Homepage theInfo = null;
		for (LayoutComponent theComp : aContainer.getChildList()) {
			if (theComp instanceof LayoutContainer) {
				theInfo = this.findSelectable((LayoutContainer) theComp, withWrapper);
			}
			else if (theComp instanceof Selectable) {
				Object theSelection = ((Selectable) theComp).getSelected();
				theInfo = getHomepageFromSelection(theSelection, theComp, withWrapper);
			}
		}
		return theInfo;
    }

	/**
	 * Gets a {@link Homepage} for the given selection and component.
	 */
	protected Homepage getHomepageFromSelection(Object selection, LayoutComponent component, boolean withWrapper) {
		ModelName target = extractTargetObjectFromModel(selection);
		Homepage info;
		if (target != null) {
			info = newHomePage(component, target, true);
		} else if (!withWrapper) {
			if (isSelectionDeactivated(component)) {
				info = getHomepageFromModel(component.getModel(), component, false);
			} else {
				info = newHomePage(component);
			}
		} else {
			info = null;
		}
		return info;
	}

	/**
	 * Checks whether the given component is a selectable but selection is disabled.
	 */
	private boolean isSelectionDeactivated(LayoutComponent component) {
		return component instanceof TableComponent && !((TableComponent) component).isSelectable();
	}

	private Homepage newHomePage(LayoutComponent component) {
		return newHomePage(component, null, false);
	}

	private Homepage newHomePage(LayoutComponent component, ModelName model, boolean isSelection) {
		return newHomePage(component.getMainLayout().getLocation(), component.getName(), model,
			isSelection ? SelectionChannel.NAME : ModelChannel.NAME);
	}

	private Homepage newHomePage(String mainLayout, ComponentName component, ModelName model, String channelName) {
		Homepage homepage = TypedConfiguration.newConfigItem(Homepage.class);
		homepage.setMainLayout(mainLayout);
		Path path = TypedConfiguration.newConfigItem(Path.class);
		path.setComponent(component);
		if (model != null) {
			ChannelValue value = TypedConfiguration.newConfigItem(ChannelValue.class);
			value.setName(channelName);
			value.setValue(model);
			path.getChannelValues().put(channelName, value);
		}
		homepage.getComponentPaths().add(path);
		return homepage;
	}

}
