/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import static com.top_logic.layout.DisplayDimension.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.component.BreadcrumbComponent;
import com.top_logic.layout.buttonbar.ButtonBarFactory;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.dynamic.DynamicLayoutContainer;
import com.top_logic.layout.structure.LayoutControlProvider.Layouting;
import com.top_logic.layout.structure.LayoutControlProvider.Strategy;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.tabbar.TabBarControlProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.layout.window.WindowManager;
import com.top_logic.layout.xml.LayoutControlComponent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.DialogComponent;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.LayoutInfo;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CockpitLayout;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;

/**
 * The class {@link LayoutControlFactory} creates {@link LayoutControl}s for
 * {@link LayoutComponent}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LayoutControlFactory<C extends LayoutControlFactory.Config<?>> implements LayoutControlProvider.Strategy,
		ConfiguredInstance<C>, Layouting {

	/**
	 * Configuration options for {@link LayoutControlFactory}.
	 */
	public interface Config<I extends LayoutControlFactory<?>> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getAutomaticToolbars()
		 */
		String AUTOMATIC_TOOLBARS = "automatic-toolbars";

		/**
		 * @see #getAutomaticToolbarsInDialogs()
		 */
		String AUTOMATIC_TOOLBARS_IN_DIALOGS = "automatic-toolbars-in-dialogs";

		/**
		 * @see #getAutomaticMaximizationGroups()
		 */
		String AUTOMATIC_MAXIMIZATION_GROUPS = "automatic-maximization-groups";
		
		/**
		 * Whether to automatically add component toolbars based on the
		 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config#hasToolbar()} option.
		 */
		@Name(AUTOMATIC_TOOLBARS)
		@BooleanDefault(true)
		boolean getAutomaticToolbars();

		/**
		 * Whether to also use automatic toolbars within dialogs, too.
		 * 
		 * @see #getAutomaticToolbars()
		 */
		@Name(AUTOMATIC_TOOLBARS_IN_DIALOGS)
		@BooleanDefault(true)
		boolean getAutomaticToolbarsInDialogs();
		
		/**
		 * Whether to automatically create maximization groups based on the master-slave relation of components.
		 */
		@Name(AUTOMATIC_MAXIMIZATION_GROUPS)
		@BooleanDefault(true)
		boolean getAutomaticMaximizationGroups();

		/**
		 * {@link LayoutControlProvider} to use for leaf components that have no
		 * {@link LayoutControlProvider} assigned.
		 */
		@Name("defaultLayoutControlProvider")
		@ItemDefault(ControlRepresentableCP.class)
		PolymorphicConfiguration<? extends LayoutControlProvider> getDefaultLayoutControlProvider();
		
	}

	/**
	 * Settings for {@link LayoutControlFactory} that are layout and theme-idependent.
	 */
	public interface GlobalConfig extends ConfigurationItem {
		/**
		 * Extension point for processing dialogs before opening them.
		 */
		@Name("dialogEnhancers")
		@InstanceFormat
		List<DialogEnhancer> getDialogEnhancers();
	}

	/**
	 * Annotation property for {@link LayoutComponent#set(Property, Object)}.
	 * 
	 * <p>
	 * It annotates a component <code>B</code> to a surrounding layout <code>A</code>, if the
	 * component <code>A</code> should be display a toolbar on behalf of <code>B</code>. A toolbar
	 * can be minimized, if there are sibling components in the directly surrounding layout. The
	 * minimization state is stored at the minimization lead component <code>B</code>.
	 * </p>
	 */
	private static final Property<LayoutComponent> TOOLBAR_FOR = TypedAnnotatable.property(LayoutComponent.class, "toolbarFor");

	private static final Property<Boolean> SHOW_MAXIMIZE = TypedAnnotatable.property(Boolean.class, "showMaximize");

	/**
	 * Annotation property for {@link LayoutComponent#set(Property, Object)}.
	 * 
	 * <p>
	 * It annotates a component <code>B</code> to a component <code>A</code>, if the component
	 * <code>A</code> should be wrapped with a maximizable layout for the maximizable lead component
	 * <code>B</code>. The maximizable layout of component <code>A</code> has only a visible
	 * toolbar, if component <code>A</code> is also annotated with {@link #TOOLBAR_FOR}.
	 * </p>
	 */
	private static final Property<List<LayoutComponent>> MAXIMIZE_ROOT =
		TypedAnnotatable.propertyList("maximizeRoot");

	private ToolBar _contextToolbar;

	private final C _config;

	private LayoutControlProvider _defaultCP;

	private final List<DialogEnhancer> _dialogEnhancers;

	/**
	 * Creates a {@link LayoutControlFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LayoutControlFactory(InstantiationContext context, C config) {
		_config = config;
		_defaultCP = context.getInstance(config.getDefaultLayoutControlProvider());
		_dialogEnhancers = ApplicationConfig.getInstance().getConfig(GlobalConfig.class).getDialogEnhancers();
	}

	@Override
	public C getConfig() {
		return _config;
	}

	@Override
	public LayoutControl createLayout(LayoutComponent component) {
		LayoutControlProvider customProvider = component.getComponentControlProvider();
		LayoutControl componentLayout;
		if (customProvider == null) {
			componentLayout = createDefaultLayout(component);
		} else {
			componentLayout = createSpecificLayout(component, customProvider);
		}

		if (!component.definesButtonBar()) {
			return componentLayout;
		}

		// Wrap component with button bar.
		FixedFlowLayoutControl buttonLayout = new FixedFlowLayoutControl(Orientation.VERTICAL);
		buttonLayout.addChild(componentLayout);
		LayoutControlAdapter buttonBar =
			new LayoutControlAdapter(ButtonBarFactory.createButtonBar(component.getButtonBar()));
		buttonBar.setConstraint(new DefaultLayoutData(DisplayDimension.HUNDERED_PERCENT, 100,
			DisplayDimension.px(60), 100, Scrolling.NO));
		buttonLayout.addChild(buttonBar);

		LayoutData componentConstraint = componentLayout.getConstraint();
		Scrolling scrolling = componentConstraint.getScrollable();
		buttonLayout.setConstraint(componentConstraint.withScrolling(Scrolling.NO));
		componentLayout.setConstraint(scrolling == Scrolling.AUTO ? DefaultLayoutData.DEFAULT_CONSTRAINT
			: DefaultLayoutData.NO_SCROLL_CONSTRAINT);

		return buttonLayout;
	}

	@Override
	public LayoutControl createLayout(LayoutComponent component, ToolBar contextToolbar) {
		ToolBar before = _contextToolbar;
		_contextToolbar = contextToolbar;
		try {
			markMaximizables(component);
			if (contextToolbar != null) {
				// Prevent duplicate toolbars in dialog title and top-level component.
				clearMinimizable(component);
			}
			return createLayout(component);
		} finally {
			_contextToolbar = before;
		}
	}

	/**
	 * Hook that that allows subclasses to override the component's {@link LayoutControlProvider}.
	 * 
	 * @param component
	 *        The component to display.
	 * @param customProvider
	 *        The configured {@link LayoutControlProvider} to use by default.
	 * @return The component display.
	 */
	protected LayoutControl createSpecificLayout(LayoutComponent component, LayoutControlProvider customProvider) {
		return customProvider.createLayoutControl(this, component);
	}

	@Override
	public LayoutControl createDefaultLayout(LayoutComponent component) {
		return decorate(component, this);
	}

	@Override
	public LayoutControl decorate(LayoutComponent component, Layouting layouting) {
		List<LayoutComponent> maximizables = clearMaximizable(component);
		if (maximizables.size() > 2) {
			// Note: The maximizable root component must listen on a unique expandable model for
			// maximization.
			Logger.error("Non-unique maximizable root specifications in: " + maximizables,
				LayoutControlFactory.class);
		}
		Expandable maximizationModel = maximizables.isEmpty() ? null : maximizables.get(0);
		boolean shouldMaximize = maximizationModel != null;

		LayoutComponent toolbarModel = clearMinimizable(component);
		if (toolbarModel != null) {
			ToolbarOptions toolbarConfig = toolbarModel.getConfig();
			CollapsibleControl result =
				new CollapsibleControl(toolbarModel.getTitleKey(), toolbarModel, shouldMaximize, toolbarConfig);
			result.getToolbar().setShowMaximizeDefault(clearShowMaximize(component));
			
			ToolBar before = _contextToolbar;
			_contextToolbar = result.getToolbar();
			try {
				installToolbar(component);
				result.setChildControl(layouting.mkLayout(this, component));
			} finally {
				_contextToolbar = before;
			}
			return result;
		} else if (shouldMaximize) {
			installToolbar(component);

			// A maximizable layout without its own toolbar.
			MaximizableControl result = new MaximizableControl(maximizationModel);
			result.setChildControl(layouting.mkLayout(this, component));
			return result;
		} else {
			installToolbar(component);

			return layouting.mkLayout(this, component);
		}
	}

	private void installToolbar(LayoutComponent component) {
		if (_contextToolbar != null) {
			component.setToolBar(_contextToolbar);
		}
	}

	@Override
	public LayoutControl mkLayout(Strategy strategy, LayoutComponent aBusinessComponent) {
		if (aBusinessComponent instanceof LayoutContainer) {
			if (aBusinessComponent instanceof MainLayout) {
				if (_config.getAutomaticToolbars()) {
					markMaximizables(aBusinessComponent);
				}
				return createMainLayout((MainLayout) aBusinessComponent);
			}
			if (aBusinessComponent instanceof Layout) {
				if (aBusinessComponent instanceof CockpitLayout) {
					return createCockpitLayout((CockpitLayout) aBusinessComponent);
				}
				if (aBusinessComponent instanceof WindowComponent) {
					return createWindowComponent((WindowComponent) aBusinessComponent);
				}
				return createLayout((Layout) aBusinessComponent);
			}
			if (aBusinessComponent instanceof AssistentComponent) {
				return createAssistantLayout((AssistentComponent) aBusinessComponent);
			}
			if (aBusinessComponent instanceof TabComponent) {
				ToolBar before = _contextToolbar;
				_contextToolbar = null;
				try {
					// Reset toolbar, tabs never have a toolbar.
					aBusinessComponent.setToolBar(null);
					return createTabComponentLayout((TabComponent) aBusinessComponent);
				} finally {
					_contextToolbar = before;
				}
			}
			if (aBusinessComponent instanceof DynamicLayoutContainer) {
			    return createDynamicLayout((DynamicLayoutContainer) aBusinessComponent);
			}
			return createLayoutContainerLayout((LayoutContainer) aBusinessComponent);
		}
//		if (aBusinessComponent instanceof InfoComponent) {
//			return createInfoComponentLayout((InfoComponent) aBusinessComponent);
//		}

		return createContentLayout(strategy, aBusinessComponent);
	}

	/**
	 * Creates the default layout for any content component.
	 */
	protected LayoutControl createContentLayout(Strategy strategy, LayoutComponent component) {
		return component.getConfig().getContentLayouting().mkLayout(strategy, component);
	}

	public static void checkForComponentNameValidity(LayoutComponent aBusinessComponent) {
		if (isComponentInvalidForFlexibleLayout(aBusinessComponent)) {
			Logger.warn(
					"Component of class '"
						+ aBusinessComponent.getClass().getName()
					+ "' in file '" + aBusinessComponent.getLocation()
					+ "' does have a generated name, but shall be part of" +
					" a flexible layout or adjustable dialog. To enable persistable layout adjustments " +
					"give this component an explicit name.",
					LayoutControlFactory.class);
		}
	}

	public static boolean isComponentInvalidForFlexibleLayout(LayoutComponent aBusinessComponent) {
		if (aBusinessComponent instanceof Layout) {
			return getConfigComponent((Layout) aBusinessComponent) == null;
		} else {
			return !markedAsTechnical(aBusinessComponent) && LayoutConstants.hasSynthesizedName(aBusinessComponent);
		}
	}

	private LayoutControl createTabComponentLayout(TabComponent aTabComponent) {
        // try to resolve the for the current theme.
		final LayoutControlProvider provider =
			ThemeFactory.getTheme().getValue(Icons.TAB_COMPONENT_DEFAULT_CONTROL_PROVIDER);
		if (provider != null) {
			return provider.createLayoutControl(this, aTabComponent);
        }

        // if the theme default could not be resolved, we have to fall back
        // to this implementation.
        else {
			final FlowLayoutControl theContainerControl =
				new FixedFlowLayoutControl(Orientation.VERTICAL);
			theContainerControl.addChild(TabBarControlProvider.INSTANCE.createLayoutControl(this, aTabComponent));
			theContainerControl.addChild(DeckPaneControlProvider.INSTANCE.createLayoutControl(this, aTabComponent));
            theContainerControl.listenForInvalidation(aTabComponent);
            return theContainerControl;
        }
    }

    private LayoutControl createDynamicLayout(DynamicLayoutContainer aDLC) {
        // content of the Tabs
        SelectionModelLayoutControl theContainerControl = 
            new SelectionModelLayoutControl(
                    aDLC.getLayoutModel(), 
                    this, 
				Orientation.VERTICAL);
        theContainerControl.listenForInvalidation(aDLC);
        return theContainerControl;
    }

	protected LayoutControl createCockpitLayout(CockpitLayout aCockpitLayout) {
		CockpitControl cockpitControl = new CockpitControl();
		cockpitControl.setChildControl(createLayout(aCockpitLayout));
		cockpitControl.listenForInvalidation(aCockpitLayout);
		return cockpitControl;
	}

	protected LayoutControl createAssistantLayout(AssistentComponent anAssistant) {
		DeckPaneControl theControl =
			new DeckPaneControl(anAssistant.getDeckPaneModel());
		for (LayoutComponent child : anAssistant.getChildList()) {
			LayoutControl theChild = createControl(child);
			theControl.addChild(theChild);
		}
		theControl.listenForInvalidation(anAssistant);
		return theControl;
	}

	protected LayoutControl createLayout(Layout aLayout) {
		boolean isFlexible = isFlexibal(aLayout);
		List<LayoutData> theLayoutDataList = createLayoutDataFor(aLayout, isFlexible);
		FlowLayoutControl theFlowLayoutControl;
		if (isFlexible) {
			theFlowLayoutControl =
				new FlexibleFlowLayoutControl(aLayout.getResizeMode(), Orientation.horizontal(aLayout.isHorizontal()));
		} else {
			theFlowLayoutControl =
				new FixedFlowLayoutControl(Orientation.horizontal(aLayout.isHorizontal()));
		}

		List<LayoutComponent> theLayoutChildren = aLayout.getChildList();
		for (int index = 0, size = theLayoutChildren.size(); index < size; index++) {
			LayoutComponent currentChild = theLayoutChildren.get(index);

			LayoutData currentLayoutData = theLayoutDataList.get(index);
			LayoutControl theNewLayout = createControl(currentChild);
			if (theNewLayout != null) {
				theNewLayout.setConstraint(currentLayoutData);
				theFlowLayoutControl.addChild(theNewLayout);
				if (notResizable(currentChild)) {
					theNewLayout.setResizable(false);
				}
			}

		}
		theFlowLayoutControl.listenForInvalidation(aLayout);
		return theFlowLayoutControl;
	}

	static boolean isFlexibal(Layout aLayout) {
		return aLayout.isResizable() && layoutChildrenResizable(aLayout);
	}

	/**
	 * Creates a {@link ConfigKey} for storing component size information.
	 * 
	 * <p>
	 * If no stable name can be created for the given component, a warning is logged.
	 * </p>
	 */
	public static ConfigKey getComponentSizeConfigKey(LayoutComponent component) {
		checkForComponentNameValidity(component);

		if (component instanceof Layout) {
			LayoutComponent configComponent = getConfigComponent((Layout) component);
			if (configComponent == null) {
				return ConfigKey.none();
			} else {
				return ConfigKey.componentSize(configComponent);
			}
		} else {
			return ConfigKey.componentSize(component);
		}
	}

	/**
	 * The named {@link LayoutComponent} that is used to store personalized size information of the
	 * given {@link Layout}.
	 */
	private static LayoutComponent getConfigComponent(Layout layout) {
		if (LayoutConstants.hasSynthesizedName(layout)) {
			List<LayoutComponent> children = layout.getChildList();
			List<LayoutComponent> resizableChilds = new LinkedList<>();
			for (int i = 0; i < children.size(); i++) {
				LayoutComponent child = children.get(i);
				if (!isTechnicalComponent(child)) {
					resizableChilds.add(child);
				}
			}
			if (resizableChilds.size() == 1) {
				if (resizableChilds.get(0) instanceof Layout) {
					return getConfigComponent((Layout) resizableChilds.get(0));
				} else {
					return resizableChilds.get(0);
				}
			} else {
				return null;
			}
		} else {
			return layout;
		}
	}

	private static boolean layoutChildrenResizable(Layout layout) {
		List<LayoutComponent> layoutChildren = layout.getChildList();
		if (layoutChildren.size() > 1) {
			int cntResizable = 0;
			for (int i = 0, cnt = layoutChildren.size(); i < cnt; i++) {
				LayoutComponent currentChild = layoutChildren.get(i);
				if (resizable(currentChild)) {
					cntResizable++;
					if (cntResizable > 1) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private static boolean resizable(LayoutComponent currentChild) {
		return !notResizable(currentChild);
	}

	private static boolean notResizable(LayoutComponent previousComponent) {
		return isTechnicalComponent(previousComponent);
	}

	private static boolean isTechnicalComponent(LayoutComponent component) {
		return isInvisible(component) || markedAsTechnical(component);
	}

	private static boolean markedAsTechnical(LayoutComponent component) {
		return component instanceof BreadcrumbComponent;
	}

	private static boolean isInvisible(LayoutComponent component) {
		DisplayDimension size = component.getLayoutInfo().getSize();

		// Note: Null as size means evenly distributed space among the children.
		return size != null && size.getValue() == 0;
	}

	protected static List<LayoutData> createLayoutDataFor(Layout aLayout, boolean personalized) {
		boolean horizontal = aLayout.isHorizontal();
		List<LayoutComponent> children = aLayout.getChildList();
		ArrayList<LayoutData> theLayoutDataList = new ArrayList<>(children.size());
		int sumOfNullConstraints = 0, sumOfPercent = 0;
		for (int index = 0, cnt = children.size(); index < cnt; index++) {
			LayoutInfo theConstraint = children.get(index).getLayoutInfo();
			DisplayDimension size = theConstraint.getSize();
			if (size == null) {
				sumOfNullConstraints++;
			} else {
				if (size.getUnit() == DisplayUnit.PERCENT) {
					int parseInt = Math.round(size.getValue());
					sumOfPercent += parseInt;
				}
			}
		}

		int sizeNullConstraint = 0;
		if (sumOfPercent < 100 && sumOfNullConstraints > 0) {
			sizeNullConstraint = (100 - sumOfPercent) / sumOfNullConstraints;
		}
		
		/*
		 * if the size for null constraints is != 0 then the sum of all percent size elements
		 * together with the constraints which have no size equals 100. If the size for "null
		 * constraints" is 0 then all percental space is used for elements with percental sizes, so
		 * normalization is needed if the sum of percent is not equal to 100
		 */
		boolean needsNormalization = sizeNullConstraint == 0 && sumOfPercent != 100;
		
		for (int index = 0, cnt = children.size(); index < cnt; index++) {
			LayoutComponent lc = children.get(index);
			LayoutInfo theConstraint = lc.getLayoutInfo();
			Scrolling scrollable = theConstraint.isScrolleable() ? Scrolling.AUTO : Scrolling.NO;
			DisplayDimension dimension;

			DisplayDimension size = theConstraint.getSize();
			if (size == null) {
				// no explicit size is given in LayoutConstraint
				dimension = DisplayDimension.dim(sizeNullConstraint, DisplayUnit.PERCENT);
			} else {
				if (needsNormalization && size.getUnit() == DisplayUnit.PERCENT) {
					dimension = DisplayDimension.dim((size.getValue() * 100) / sumOfPercent, size.getUnit());
				} else {
					dimension = size;
				}
			}

			DisplayDimension width;
			DisplayDimension height;
			int minWidth;
			int minHeight;
			if (horizontal) {
				width = dimension;
				minWidth = theConstraint.getMinSize();
				height = DisplayDimension.HUNDERED_PERCENT;
				minHeight = 0;
			} else {
				width = DisplayDimension.HUNDERED_PERCENT;
				minWidth = 0;
				height = dimension;
				minHeight = theConstraint.getMinSize();
			}
			
			theLayoutDataList.add(
				createLayoutData(personalized, horizontal, lc, width, height, minWidth, minHeight, scrollable));

		}
		return theLayoutDataList;
	}

	private static LayoutData createLayoutData(boolean personalized, boolean parentLayoutedHorizontally,
			LayoutComponent component, DisplayDimension width, DisplayDimension height, int minWidth,
			int minHeight, Scrolling scrollable) {
		if (personalized) {
			ConfigKey configKey = getComponentSizeConfigKey(component);
			PersonalizedLayoutData result = new PersonalizedLayoutData(configKey, parentLayoutedHorizontally, width, minWidth,
				100, height, minHeight, 100, scrollable);
			return result.copyWithAppliedPersonalization();
		} else {
			return new DefaultLayoutData(width, minWidth, 100, height, minHeight, 100, scrollable);
		}
	}

	protected LayoutControl createMainLayout(MainLayout aMainLayout) {
		DefaultWindowModel windowModel = new DefaultWindowModel(DefaultLayoutData.NO_SCROLL_CONSTRAINT, null);
		WindowManager windowManager = aMainLayout.getWindowManager();
		return createBrowserWindowControl(aMainLayout, null, windowManager, windowModel);
	}

	private BrowserWindowControl createBrowserWindowControl(Layout layout, WindowScope opener, WindowManager windowManager, WindowModel windowModel) {
		BrowserWindowControl windowControl =
			new BrowserWindowControl(opener, windowManager, windowModel, layout.getName());
		
		LayoutControl content = createLayout(layout);
		CockpitControl cockpit = new CockpitControl();
		cockpit.setChildControl(content);
		windowControl.setChildControl(cockpit);
		windowControl.listenForInvalidation(layout);
		return windowControl;
	}
	
	protected LayoutControl createWindowComponent(WindowComponent windowComponent) {
		markMaximizables(windowComponent);
		WindowScope mainWindow = windowComponent.getMainLayout().getLayoutControl();
		return createBrowserWindowControl(windowComponent, mainWindow, null, new DefaultWindowModel(
			DefaultLayoutData.NO_SCROLL_CONSTRAINT, null));
	}

	@Override
	public DialogWindowControl createDialogLayout(DialogComponent currentDialog) {
		enhanceDialog(currentDialog);
		
		DialogWindowControl theDialogLayout =
			new DialogWindowControl(currentDialog);
		final LayoutComponent contentComponent = currentDialog.getContentComponent();
		
		currentDialog.addListener(DialogModel.CLOSED_PROPERTY, (sender, oldValue, newValue) -> {
			if (newValue) {
				// Detached.
				contentComponent.acceptVisitorRecursively(new DefaultDescendingLayoutVisitor() {
					@Override
					public boolean visitLayoutComponent(LayoutComponent aComponent) {
						// Reset toolbars.
						aComponent.setToolBar(null);
						return true;
					}
				});
			}
		});
		
		LayoutControl theContent;

		if (_config.getAutomaticToolbars() && _config.getAutomaticToolbarsInDialogs()) {
			markMaximizables(contentComponent);

			// Prevent duplicate toolbars in dialog title and top-level component.
			clearMinimizable(contentComponent);

			_contextToolbar = currentDialog.getToolbar();
		}
		try {
			theContent = createLayout(contentComponent);
		} finally {
			_contextToolbar = null;
		}

		theDialogLayout.setChildControl(theContent);
		theDialogLayout.setConstraint(currentDialog.getLayoutData());
		return theDialogLayout;
	}

	private void enhanceDialog(DialogComponent currentDialog) {
		for (DialogEnhancer enhancer : _dialogEnhancers) {
			enhancer.enhanceDialog(currentDialog);
		}
	}

	protected LayoutControl createLayoutContainerLayout(LayoutContainer container) {
		FixedFlowLayoutControl result = new FixedFlowLayoutControl(Orientation.VERTICAL);
		int numberOfVisibleChildren = 0;
		Collection<LayoutComponent> childList = container.getChildList();
		for (LayoutComponent child : childList) {
			if (child.isVisible()) {
				numberOfVisibleChildren++;
			}
		}
		if (numberOfVisibleChildren != 0) {
			int sizeOfEachComponent = 100 / numberOfVisibleChildren;
			int index = 0;
			for (LayoutComponent child : childList) {
				if (!child.isVisible()) {
					continue;
				}
				LayoutControl newLayout = createControl(child);
				result.addChild(newLayout);
				int height;
				if (index == numberOfVisibleChildren - 1) {
					/* Last visible child takes the remaining height. This may not be equal to
					 * others due to rounding errors. */
					height = 100 - ((numberOfVisibleChildren - 1) * sizeOfEachComponent);
				} else {
					height = sizeOfEachComponent;
				}
				LayoutData constraint = new DefaultLayoutData(
					DisplayDimension.HUNDERED_PERCENT, 100,
					dim(height, DisplayUnit.PERCENT), 100,
					Scrolling.NO);
				newLayout.setConstraint(constraint);

				index++;
			}
		}
		result.listenForInvalidation(container);
		return result;
	}

	private LayoutControl createControl(LayoutComponent component) {
		return createLayout(component);
	}

	/**
	 * Main entry point for the algorithm placing toolbars in the layout tree and marking
	 * maximizable layouts.
	 * 
	 * @param aBusinessComponent
	 *        The top-level component to start the search from.
	 */
	private void markMaximizables(LayoutComponent aBusinessComponent) {
		MaximizableResults results = internalMarkMaximizables(aBusinessComponent);
		for (MaximizableResult result : results) {
			applyAnnotations(result);
		}
	}

	/**
	 * Adds the {@link #TOOLBAR_FOR} and {@link #MAXIMIZE_ROOT} markers to toolbar components in the
	 * given component hierarchy.
	 * 
	 * @param component
	 *        The root of the component hierarchy to process.
	 * @return The analysis result for the given subtree.
	 */
	private MaximizableResults internalMarkMaximizables(LayoutComponent component) {
		if (component instanceof DynamicLayoutContainer) {
			/* For DynamicLayoutContainer a LayoutControl is created which always create new
			 * controls when the container changes the displayed component. As the maximize root
			 * marker is removed from the component when a control is created a second control
			 * creation for the same component won't find the marker, such that the layout differs
			 * from the first time. Therefore it is better to omit maximization for
			 * DynamicLayoutContainer. */
			return MaximizableResults.NOT_MAXIMIZABLE;
		} else if (component instanceof LayoutContainer) {
			LayoutContainer layoutContainer = (LayoutContainer) component;

			if (isContent(component)) {
				MaximizableResults result = processChildren(layoutContainer);
				result.add(MaximizableResult.canMaximize(component));
				return result;
			}

			if (component instanceof Layout && !isFlexibal((Layout) layoutContainer)) {
				MaximizableResults result = new MaximizableResults();

				// Check for implicitly selecting this container as minimize root for the only
				// child component having a toolbar.
				for (LayoutComponent child : layoutContainer.getChildList()) {
					MaximizableResults childResults = internalMarkMaximizables(child);
					result.markMaximizableContents(childResults.hasMaximizableContent());
					result.addAll(childResults);
				}
				
				pulloutMinimizeRoot:
				{
					if (result.size() == 1 && (!(component instanceof WindowComponent))) {
						// In this layout, there is only a single direct child owning a toolbar.
						// Increase the scope of this toolbar to the whole layout (e.g. editor and
						// its
						// button component).
						MaximizableResult singleResult = result.get(0);

						LayoutControlProvider cp = singleResult.minimizeRoot().getComponentControlProvider();
						if (cp == null || cp.getClass().getAnnotation(IntroducesToolbar.class) == null) {
							// Implicitly use this technical layout as minimizable root.
							singleResult.setMinimizeRoot(component);
							break pulloutMinimizeRoot;
						}
					}

					// Else: There was a reason found, why the minimize root cannot be pulled out
					// further. Mark the results in a way that pulling out is prevented, even if a
					// pullout-condition is found in some surrounding scope.
					result.freezeMinimizables();
				}

				// Find configured maximizeRoot components.
				Iterator<MaximizableResult> results = result.iterator();
				while (results.hasNext()) {
					MaximizableResult childResult = results.next();
					childResult.offerMaximizableRoot(component);
				}

				return result;
			} else {
				MaximizableResults result = processChildren(layoutContainer);
				result.freezeMinimizables();
				return result;
			}
		} else if (isContent(component)) {
			MaximizableResult result = MaximizableResult.canMaximize(component);
			if (component.getConfig().getMaximizeRoot() == null) {
				// No explicit maximization root was configured.
				applyMaximizationHeuristics(component, result);
			}
			return new MaximizableResults(result);
		} else if (component instanceof LayoutControlComponent) {
			// Assume the worst case.
			return MaximizableResults.HAS_MAXIMIZABLE_CONTENTS;
		} else {
			return MaximizableResults.NOT_MAXIMIZABLE;
		}
	}

	private void applyMaximizationHeuristics(LayoutComponent component, MaximizableResult result) {
		if (_config.getAutomaticMaximizationGroups()) {
			LayoutComponent tab = tab(component);

			// Heuristic to enlarge the maximization scope of master components.
			Collection<? extends LayoutComponent> slaves = slavesWithoutDialogs(component);
			if (!slaves.isEmpty()) {

				if (uniqueInSameTab(tab, slaves)) {
					// Master is maximized together with its unique slave in this tab.
					result.setShowMaximize(false);
					return;
				} else {
					if (havingMastersInSameTab(component, tab, result)) {
						return;
					}

					// This is the top-level master in this view. Use this component as the
					// maximization trigger for its whole view.
					LayoutComponent root = commonAnchestor(component, transitiveSlaves(component));
					LayoutControlProvider cp = root.getComponentControlProvider();
					if (cp == null || cp.getClass().getAnnotation(IntroducesToolbar.class) == null) {
						result.setMaximizeRoot(root);
					}
				}
			} else {
				havingMastersInSameTab(component, tab, result);
			}
		}
	}

	private boolean havingMastersInSameTab(LayoutComponent component, LayoutComponent tab, MaximizableResult result) {
		Collection<LayoutComponent> masters = inSameTab(tab, transitiveMasters(component));
		if (!masters.isEmpty()) {
			masters.add(component);
			for (LayoutComponent master : masters) {
				if (!masters.containsAll(slavesWithoutDialogs(master))) {
					// There are other slaves of this component's masters in this tab. Do not use
					// this component as maximization trigger for the whole view.
					return true;
				}
			}

			// The component is the unique slave within its tab. Use this component as maximiztation
			// trigger of its tab.
			result.setMaximizeRoot(tab);
			return true;
		}
		return false;
	}

	private List<LayoutComponent> inSameTab(LayoutComponent tab, Collection<? extends LayoutComponent> components) {
		ArrayList<LayoutComponent> result = new ArrayList<>();
		for (LayoutComponent component : components) {
			if (tab(component) == tab) {
				result.add(component);
			}
		}
		return result;
	}

	private boolean uniqueInSameTab(LayoutComponent tab, Collection<? extends LayoutComponent> components) {
		boolean hasSlaveInSameTab = false;
		for (LayoutComponent component : components) {
			if (tab(component) == tab) {
				if (hasSlaveInSameTab) {
					// Not unique.
					return false;
				}
				hasSlaveInSameTab = true;
			}
		}
		return hasSlaveInSameTab;
	}

	private LayoutComponent tab(LayoutComponent component) {
		LayoutComponent result = component;
		while (result.getParent() instanceof Layout && !(result.getParent() instanceof WindowComponent)) {
			result = result.getParent();
		}
		if (result.getParent() instanceof TabComponent) {
			return result;
		} else {
			return component;
		}
	}

	private Collection<? extends LayoutComponent> transitiveMasters(LayoutComponent component) {
		List<LayoutComponent> result = new ArrayList<>(component.getMasters());
		for (int n = 0; n < result.size(); n++) {
			LayoutComponent slave = result.get(n);
			if (slave.getDialogParent() != component.getDialogParent()) {
				// Exclude components in other dialogs.
				result.remove(n--);
				continue;
			}

			result.addAll(slave.getMasters());
		}
		return result;
	}

	private Collection<? extends LayoutComponent> slavesWithoutDialogs(LayoutComponent component) {
		return removeDialogs(component, new ArrayList<>(component.getSlaves()));
	}

	private Collection<? extends LayoutComponent> transitiveSlaves(LayoutComponent component) {
		List<LayoutComponent> result = new ArrayList<>(component.getSlaves());
		for (int n = 0; n < result.size(); n++) {
			result.addAll(result.get(n).getSlaves());
		}
		return removeDialogs(component, result);
	}

	private List<LayoutComponent> removeDialogs(LayoutComponent component, List<LayoutComponent> result) {
		LayoutComponent dialogParent = component.getDialogParent();
		int m = 0;
		int size = result.size();
		for (int n = 0; n < size; n++) {
			LayoutComponent other = result.get(n);
			if (other.getDialogParent() != dialogParent) {
				// Exclude components in other dialogs.
				continue;
			}
			result.set(m++, other);
		}
		return m < size ? result.subList(0, m) : result;
	}

	private LayoutComponent commonAnchestor(LayoutComponent component, Collection<? extends LayoutComponent> slaves) {
		LayoutComponent result = component;
		Map<LayoutComponent, Integer> pathToRoot = pathToRoot(component);
		for (LayoutComponent slave : slaves) {
			LayoutComponent anchestor = slave;
			while (!pathToRoot.containsKey(anchestor)) {
				anchestor = anchestor.getParent();
			}

			if (pathToRoot.get(anchestor) > pathToRoot.get(result)) {
				result = anchestor;
			}
		}
		return result;
	}

	private Map<LayoutComponent, Integer> pathToRoot(LayoutComponent component) {
		HashMap<LayoutComponent, Integer> result = new HashMap<>();
		int index = 0;
		while (component != null) {
			result.put(component, index++);
			component = component.getParent();
		}
		return result;
	}

	private MaximizableResults processChildren(LayoutContainer layoutContainer) {
		MaximizableResults result = new MaximizableResults();
		for (LayoutComponent child : layoutContainer.getChildList()) {
			MaximizableResults childResults = internalMarkMaximizables(child);
			result.markMaximizableContents(childResults.hasMaximizableContent());

			for (MaximizableResult childResult : childResults) {
				childResult.offerMaximizableRoot(layoutContainer);
				if (childResult.maximizableRootFound()) {
					applyAnnotations(childResult);
				} else {
					// Pass to the outside.
					result.add(childResult);
				}
			}
		}
		return result;
	}

	static class MaximizableResults extends ArrayList<MaximizableResult> {

		public static final MaximizableResults HAS_MAXIMIZABLE_CONTENTS = new MaximizableResults(true);

		public static final MaximizableResults NOT_MAXIMIZABLE = new MaximizableResults(false);

		private boolean _maximizableContents;

		public MaximizableResults(MaximizableResult singleResult) {
			this();
			add(singleResult);
		}

		public void freezeMinimizables() {
			for (MaximizableResult result : this) {
				result.freezeMinimizable();
			}
		}

		private MaximizableResults(boolean hasMaximizableContent) {
			this();
			setMaximizableContents(hasMaximizableContent);
		}

		public MaximizableResults() {
			super();
		}

		public boolean hasMaximizableContent() {
			return _maximizableContents;
		}

		public void markMaximizableContents(boolean hasMaximizableContent) {
			_maximizableContents |= hasMaximizableContent;
		}

		public void setMaximizableContents(boolean maximizableContents) {
			_maximizableContents = maximizableContents;
		}
	}

	/**
	 * Maximization/minimization information created for components.
	 */
	static abstract class MaximizableResult {

		/**
		 * {@link MaximizableResult} describing an atomic maximizable component.
		 * 
		 * @param toolbarContent
		 *        The maximizable content.
		 * @return The {@link MaximizableResult} describing the maximization config of the given
		 *         component.
		 */
		public static MaximizableResult canMaximize(final LayoutComponent toolbarContent) {
			ComponentName configuredMaximizeRoot = toolbarContent.getConfig().getMaximizeRoot();
			final ComponentName maximizeRootName;
			if (configuredMaximizeRoot == null) {
				maximizeRootName = toolbarContent.getName();
			} else {
				maximizeRootName = configuredMaximizeRoot;
			}
			return new MaximizableResult() {
				private ComponentName _maximizeRootName = maximizeRootName;

				private LayoutComponent _minimizeRoot = toolbarContent;

				private LayoutComponent _maximizeRoot = toolbarContent;

				private boolean _showMinimize = true;

				@Override
				public LayoutComponent toolbarModel() {
					return toolbarContent;
				}

				@Override
				public ComponentName maximizeRootName() {
					return _maximizeRootName;
				}

				@Override
				public void offerMaximizableRoot(LayoutComponent component) {
					if (_maximizeRootName.equals(component.getName())) {
						_maximizeRoot = component;
					}
				}

				@Override
				public LayoutComponent minimizeRoot() {
					return _minimizeRoot;
				}

				@Override
				public boolean getShowMaximize() {
					return _showMinimize;
				}

				@Override
				public void setShowMaximize(boolean showMinimize) {
					_showMinimize = showMinimize;
				}

				@Override
				public void doSetMinimizeRoot(LayoutComponent component) {
					LayoutComponent minimizeRootBefore = _minimizeRoot;

					_minimizeRoot = component;

					if (_maximizeRootName.equals(minimizeRootBefore.getName())) {
						// The maximize root was set to the former minimize root.
						//
						// Ensure that the maximize root is not smaller than the minimize root.
						setMaximizeRoot(component);
					}
				}

				@Override
				public void setMaximizeRoot(LayoutComponent component) {
					_maximizeRoot = component;
					_maximizeRootName = component.getName();
				}

				@Override
				public LayoutComponent maximizeRoot() {
					return _maximizeRoot;
				}

				@Override
				public boolean maximizableRootFound() {
					return _maximizeRootName.equals(_maximizeRoot.getName());
				}

			};
		}

		private boolean _stopPullout;

		public void freezeMinimizable() {
			_stopPullout = true;
		}

		public abstract boolean getShowMaximize();

		public abstract void setShowMaximize(boolean b);

		public abstract LayoutComponent toolbarModel();

		public final void setMinimizeRoot(LayoutComponent value) {
			if (_stopPullout) {
				return;
			}
			doSetMinimizeRoot(value);
		}

		public abstract void doSetMinimizeRoot(LayoutComponent value);

		public abstract LayoutComponent minimizeRoot();

		public abstract ComponentName maximizeRootName();

		public abstract void offerMaximizableRoot(LayoutComponent component);

		public abstract void setMaximizeRoot(LayoutComponent component);

		public abstract boolean maximizableRootFound();

		public abstract LayoutComponent maximizeRoot();
	}

	private LayoutComponent clearMinimizable(LayoutComponent component) {
		return component.reset(TOOLBAR_FOR);
	}

	private List<LayoutComponent> clearMaximizable(LayoutComponent component) {
		return component.reset(MAXIMIZE_ROOT);
	}

	/**
	 * Fetches the result of the heuristics for showing the maximize button from the given
	 * component.
	 * 
	 * @param component
	 *        The component in question.
	 * @return Whether the given component should get the maximize button.
	 */
	protected boolean clearShowMaximize(LayoutComponent component) {
		return component.reset(SHOW_MAXIMIZE).booleanValue();
	}

	private void applyAnnotations(MaximizableResult result) {
		LayoutComponent toolbarModel = result.toolbarModel();
		if (!result.maximizableRootFound()) {
			Logger.error("Maximizable root '" + result.maximizeRootName()
				+ "' not found from toolbar component '"
				+ toolbarModel.getName() + "' at " + toolbarModel.getLocation() + ".",
				LayoutControlFactory.class);
		}
		LayoutComponent maximizeRoot = result.maximizeRoot();
		List<LayoutComponent> maximizables = maximizeRoot.get(MAXIMIZE_ROOT);
		if (maximizables == Collections.EMPTY_LIST) {
			maximizeRoot.set(MAXIMIZE_ROOT, Collections.singletonList(toolbarModel));
		} else if (maximizables.contains(toolbarModel)) {
			// Ignore if computation results in already determined maximizable
		} else {
			// This is an indicator of an error reported later on and should normally not happen.
			if (maximizables.size() == 1) {
				// Was created as unmodifiable list above.
				maximizables = new ArrayList<>(maximizables);
				maximizeRoot.set(MAXIMIZE_ROOT, maximizables);
			}
			maximizables.add(toolbarModel);
		}
		result.minimizeRoot().set(TOOLBAR_FOR, toolbarModel);
		result.minimizeRoot().set(SHOW_MAXIMIZE, result.getShowMaximize());
	}

	private boolean isContent(LayoutComponent component) {
		return component.getConfig().hasToolbar();
	}

}
