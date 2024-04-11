/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure.sidebar;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.knowledge.gui.layout.ButtonComponent;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.buttonbar.ButtonBarControl;
import com.top_logic.layout.buttonbar.ButtonBarFactory;
import com.top_logic.layout.buttonbar.SimpleButtonBarModel;
import com.top_logic.layout.component.configuration.ViewConfiguration;
import com.top_logic.layout.component.model.ModelChangeListener;
import com.top_logic.layout.structure.ConfiguredLayoutControlProvider;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.util.ToBeValidated;

/**
 * {@link ViewConfiguration} showing a {@link ButtonBarControl} with all buttons of currently
 * visible components.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TitleButtonsViewProvider extends ConfiguredLayoutControlProvider<TitleButtonsViewProvider.Config> {

	/**
	 * Configuration options for {@link TitleButtonsViewProvider}
	 */
	public interface Config extends PolymorphicConfiguration<LayoutControlProvider> {
		/**
		 * CSS class used for the top-level layout control.
		 */
		String getCssClass();
		
		/**
		 * The size of the button bar if it is active (has buttons).
		 */
		DisplayDimension getSize();
	}

	/**
	 * Creates a {@link TitleButtonsViewProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TitleButtonsViewProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public LayoutControl createLayoutControl(Strategy strategy, final LayoutComponent component) {
		final SimpleButtonBarModel model = new SimpleButtonBarModel();
		ButtonBarControl buttonBar = ButtonBarFactory.createButtonBar(model);
		final LayoutControlAdapter layout = new LayoutControlAdapter(buttonBar);

		class ButtonUpdater extends DefaultDescendingLayoutVisitor implements AttachedPropertyListener, VisibilityListener,
				ModelChangeListener, ToBeValidated {

			private List<CommandModel> _collectedButtons;

			@Override
			public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue()) {
					collectButtons();
					component.addListener(LayoutComponent.VISIBILITY_EVENT, this);
					component.addListener(ModelChangeListener.MODEL_CHANGED, this);
				} else {
					component.removeListener(LayoutComponent.VISIBILITY_EVENT, this);
					component.removeListener(ModelChangeListener.MODEL_CHANGED, this);
				}
			}
			
			@Override
			public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
				// Update buttons after event processing has stopped.
				DefaultDisplayContext.getDisplayContext().getLayoutContext().notifyInvalid(this);
				return Bubble.BUBBLE;
			}

			@Override
			public Bubble modelChanged(Object sender, Object oldModel, Object newModel) {
				if (sender instanceof ButtonComponent) {
					DefaultDisplayContext.getDisplayContext().getLayoutContext().notifyInvalid(this);
				}
				return Bubble.BUBBLE;
			}

			@Override
			public void validate(DisplayContext context) {
				collectButtons();
			}

			private void collectButtons() {
				// Search for visible button components.
				_collectedButtons = new ArrayList<>();
				if (component instanceof LayoutContainer) {
					for (LayoutComponent child : ((LayoutContainer) component).getChildList()) {
						child.acceptVisitorRecursively(this);
					}
				}
				model.setButtons(_collectedButtons);
				LayoutData oldConstraint = layout.getConstraint();
				LayoutData newConstraint;
				if (_collectedButtons.isEmpty()) {
					newConstraint = oldConstraint.resized(DisplayDimension.ZERO, DisplayDimension.ZERO);
				} else {
					newConstraint = oldConstraint.resized(DisplayDimension.HUNDERED_PERCENT,
						TitleButtonsViewProvider.this.getConfig().getSize());
				}
				layout.setConstraint(newConstraint);
				_collectedButtons = null;
			}

			@Override
			public boolean visitLayoutComponent(LayoutComponent layoutComponent) {
				boolean isDialog = layoutComponent.getDialogTopLayout() != null;
				if (isDialog) {
					return false;
				}
				if (layoutComponent.isVisible()) {

					if (layoutComponent instanceof ButtonComponent) {
						List<CommandModel> buttons = ((ButtonComponent) layoutComponent).getButtons();
						if (buttons != null) {
							_collectedButtons.addAll(buttons);
						}
					}
					return true;
				} else {
					return false;
				}
			}

		}

		layout.addListener(AbstractControlBase.ATTACHED_PROPERTY, new ButtonUpdater());
		layout.setCssClass(getConfig().getCssClass());
		return layout;
	}

}
