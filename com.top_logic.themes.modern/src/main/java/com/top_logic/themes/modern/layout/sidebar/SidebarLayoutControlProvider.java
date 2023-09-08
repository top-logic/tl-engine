/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.themes.modern.layout.sidebar;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.AbstractSingleSelectionModel;
import com.top_logic.layout.Attachable;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.InvalidationListener;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.accordion.AccordionControl;
import com.top_logic.layout.accordion.TreeNavigatorControl;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.basic.check.ChildrenCheckScopeProvider;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.component.TabComponent.TabbedLayoutComponent;
import com.top_logic.layout.component.configuration.ViewConfiguration;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.selection.SingleSelectVetoListener;
import com.top_logic.layout.structure.DecoratingLayoutControlProvider;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.tabbar.TabBarModel;
import com.top_logic.layout.tabbar.TabInfo;
import com.top_logic.layout.tabbar.TabBarModel.TabBarListener;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.tree.model.TreePath;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.themes.modern.layout.sidebar.SidebarLayoutControlProvider.ComponentTree.Node;
import com.top_logic.themes.modern.layout.views.ToolRowView;
import com.top_logic.themes.modern.layout.views.ToolRowView.GroupView;
import com.top_logic.util.ToBeValidated;

/**
 * {@link LayoutControlProvider} that displays a given {@link TabComponent} and all its descending
 * {@link TabComponent}s with a single {@link AccordionControl}.
 * 
 * <p>
 * In the accordion, the tabs of the {@link TabComponent} are displayed as top-level entries. If
 * some of the tabs have a {@link TabComponent} in their contents, the tabs of the descendent
 * {@link TabComponent}s are displayed as sub-entries of the top-level tab entry.
 * </p>
 * 
 * <p>
 * Using the {@link SidebarLayoutControlProvider} requires to use the
 * {@link DeckpaneOnlyTabControlProvider} for all descending {@link TabComponent}s of the top-level
 * {@link TabComponent}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SidebarLayoutControlProvider extends DecoratingLayoutControlProvider<SidebarLayoutControlProvider.Config> {

	/**
	 * CSS class for the layout control displaying the application side-bar.
	 */
	private static final String SIDEBAR_CSS_CLASS = AccordionControl.ROOT_CSS_CLASS;

	/**
	 * Configuration options of {@link SidebarLayoutControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<LayoutControlProvider> {
	
		/**
		 * CSS class for the created layout control.
		 */
		@StringDefault(SIDEBAR_CSS_CLASS)
		String getCssClass();

		/**
		 * The scroll setting of the layout control.
		 */
		Scrolling getScrolling();

		/**
		 * Use an accordion sidebar instead of a paging tree navigator.
		 */
		boolean getAccordion();

		/**
		 * {@link ResourceProvider} for displaying component nodes.
		 */
		@InstanceFormat
		@InstanceDefault(NodeResources.class)
		ResourceProvider getResourceProvider();
		
		/**
		 * {@link ViewConfiguration}s of clickable buttons of this {@link ToolRowView}
		 */
		List<PolymorphicConfiguration<ViewConfiguration>> getViews();
	}

	/**
	 * Creates a {@link SidebarLayoutControlProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SidebarLayoutControlProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public LayoutControl mkLayout(Strategy strategy, LayoutComponent component) {
		final TabComponent tabComponent = (TabComponent) component;

		// Note: The tab-switch is recorded by the underlying TabComponent implementations.
		// Therefore, the selection model used to communicate with the AccordionControl can be
		// anonymous.
		final SingleSelectionModel componentSelection = new DefaultSingleSelectionModel(null);

		final ComponentTree tree = new ComponentTree(tabComponent, componentSelection);

		/**
		 * Modify events from the UI so that always the innermost component is treated as selected.
		 */
		class UISelection extends AbstractSingleSelectionModel implements SingleSelectionListener {
			@Override
			public void setSingleSelection(Object obj) {
				// Dispatch directly to tab switch to prevent event ping-pong.
				Node node = ((TreePath) obj).toNode(tree);
				if (node != null) {
					node.select();
				}
			}

			@Override
			public boolean isSelectable(Object obj) {
				return componentSelection.isSelectable(obj);
			}

			@Override
			public Object getSingleSelection() {
				return componentSelection.getSingleSelection();
			}

			@Override
			public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject,
					Object selectedObject) {
				// Adapt selection events from the component selection model to this UI selection
				// model.
				fireSingleSelectionChanged(formerlySelectedObject, selectedObject);
			}
		}

		UISelection uiSelection = new UISelection();
		componentSelection.addSingleSelectionListener(uiSelection);

		SelectionVeto selectionVeto = new SelectionVeto(tree);

		AbstractControlBase tabSelector;
		if (getConfig().getAccordion()) {
			AccordionControl<Node> accordion =
				new AccordionControl<>(tree, uiSelection, getConfig().getResourceProvider());
			accordion.setSelectionVeto(selectionVeto);
			tabSelector = accordion;
			
			tabSelector.addListener(AbstractControlBase.ATTACHED_PROPERTY, new AttachedPropertyListener() {
				@Override
				public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
					if (newValue.booleanValue()) {
						tree.attach();
					} else {
						tree.detach();
					}
				}
			});
		} else {
			final TreeNavigatorControl<Node> accordion =
				new TreeNavigatorControl<>(tree, uiSelection, getConfig().getResourceProvider());
			accordion.setSelectionVeto(selectionVeto);
			
			accordion.addListener(AbstractControlBase.ATTACHED_PROPERTY, new AttachedPropertyListener() {
				@Override
				public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
					if (newValue.booleanValue()) {
						tree.attach();
						
						// Establish initial selection.
						tree.setSelection(tree.innermostSelected(tree.getRoot()));

						// Add component validator.
						//
						// Note: This must happen after installing the initial selection, since the
						// initial selection happens during rendering. In that phase, no validation
						// must happen.
						SingleSelectionModel levelSelection = accordion.getLevelSelection();
						levelSelection.addSingleSelectionListener(new SingleSelectionListener() {
							@Override
							public void notifySelectionChanged(SingleSelectionModel model,
									Object formerlySelectedObject,
									Object selectedObject) {
								Node node = ((TreePath) selectedObject).toNode(tree);
								if (node != null) {
									node.validate();
								}
							}
						});
					} else {
						tree.detach();
					}
				}
			});

			tabSelector = accordion;
		}

		GroupView.Config groupConfig = TypedConfiguration.newConfigItem(GroupView.Config.class);
		groupConfig.getViews().addAll(getConfig().getViews());
		GroupView buttonGroup = new GroupView(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, groupConfig);
		HTMLFragment buttonBar = buttonGroup.createView(component);

		LayoutControlAdapter layoutControlAdapter =
			new LayoutControlAdapter(
				concat(
					div("sblAccordion", tabSelector),
					div("sblButtons", buttonBar)));
		layoutControlAdapter.setConstraint(
			new DefaultLayoutData(
				DisplayDimension.HUNDERED_PERCENT, 100,
				DisplayDimension.HUNDERED_PERCENT, 100,
				getConfig().getScrolling()));
		layoutControlAdapter.setCssClass(getConfig().getCssClass());
		return layoutControlAdapter;
	}

	/**
	 * {@link TLTreeModel} containing nodes for all tabs of a top-level {@link TabComponent} and
	 * potentially descending {@link TabComponent}s and their tabs.
	 */
	static class ComponentTree extends AbstractMutableTLTreeModel<ComponentTree.Node> implements Attachable,
			SingleSelectionListener {

		private static final TreeBuilder<Node> BUILDER = new TreeBuilder<>() {
			@Override
			public boolean isFinite() {
				return true;
			}

			@Override
			public Node createNode(AbstractMutableTLTreeModel<Node> model,
					Node parent, Object userObject) {
				return new TabBarNode((ComponentTree) model, parent, null, (TabComponent) userObject);
			}

			@Override
			public List<Node> createChildList(Node node) {
				return node.createChildList();
			}
		};

		private final SingleSelectionModel _selectionModel;

		/**
		 * Creates a {@link ComponentTree} model.
		 * 
		 * @param selectionModel
		 *        See {@link #getSelectionModel()}
		 */
		public ComponentTree(TabComponent root, SingleSelectionModel selectionModel) {
			super(BUILDER, root);

			_selectionModel = selectionModel;
		}

		/**
		 * The {@link SingleSelectionModel} that contains the active tab as selection. The active
		 * tab is a leaf node in the {@link ComponentTree} model.
		 */
		public SingleSelectionModel getSelectionModel() {
			return _selectionModel;
		}

		@Override
		public boolean isAttached() {
			return getRoot().isAttached();
		}

		@Override
		public boolean attach() {
			boolean result = getRoot().attach();
			if (result) {
				// Update initial state.
				setSelection(innermostSelected(getRoot()));

				_selectionModel.addSingleSelectionListener(this);
			}
			return result;
		}

		void setSelection(Node node) {
			_selectionModel.setSingleSelection(TreePath.fromNode(this, node));
		}

		@Override
		public boolean detach() {
			boolean result = getRoot().detach();
			if (result) {
				_selectionModel.removeSingleSelectionListener(this);
			}
			return result;
		}

		/**
		 * Propagates a selection request from the {@link #getSelectionModel()} to the underlying
		 * tab component structure.
		 */
		@Override
		public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject,
				Object selectedObject) {
			if (selectedObject != null) {
				Node selectedNode = ((TreePath) selectedObject).toNode(this);
				if (selectedNode != null) {
					selectedNode.select();
				}
			}
		}

		/**
		 * Handles a selection change in the underlying tab component structure.
		 * 
		 * @param selection
		 *        The newly selected node.
		 */
		void handleSelectionChange(Node selection) {
			setSelection(innermostSelected(selection));
		}

		public Node innermostSelected(Node selection) {
			if (selection != null) {
				// Propagate selection to the innermost selected node.
				while (true) {
					Node inner = selection.getSelected();
					if (inner == null) {
						break;
					}
					selection = inner;
				}
			}
			return selection;
		}

		/**
		 * Base class of nodes/tabs in the {@link ComponentTree}.
		 */
		static abstract class Node extends AbstractMutableTLTreeNode<Node> implements Attachable {

			public Node(AbstractMutableTLTreeModel<Node> model, Node parent, Object businessObject) {
				super(model, parent, businessObject);
			}

			/**
			 * Selects this tab.
			 */
			public void select() {
				Node parent = getParent();
				if (parent != null) {
					parent.setSelected(this);
				}
			}

			/**
			 * Selects the given node/tab within the tabs of this tab bar.
			 * 
			 * @param node
			 *        The tab to choose.
			 */
			protected abstract void setSelected(Node node);

			/**
			 * The active child node/tab within this tab bar.
			 * 
			 * @return The selected child node, or <code>null</code>, if nothing is selected within
			 *         this tab bar.
			 */
			public abstract Node getSelected();

			@Override
			public ComponentTree getModel() {
				return (ComponentTree) super.getModel();
			}

			/**
			 * Makes sure that the {@link #getChildren()} are in sync with the visible tabs of the
			 * underlying tab component.
			 */
			public abstract void validate();

			/**
			 * Node-specific implementation of
			 * {@link TreeBuilder#createChildList(AbstractMutableTLTreeNode)}.
			 */
			public abstract List<Node> createChildList();

			/**
			 * The {@link Card}-implementation of this tab, or <code>null</code>, if this is the
			 * root node (which is not a tab itself).
			 */
			public abstract Card getCard();

			@Override
			public boolean equals(Object obj) {
				if (obj == this) {
					return true;
				}
				if (obj instanceof Node) {
					return Utils.equals(getBusinessObject(), ((Node) obj).getBusinessObject());
				}
				return false;
			}
			
			@Override
			public int hashCode() {
				return getBusinessObject().hashCode();
			}
		}

		/**
		 * A {@link Node} that itself represents a {@link TabComponent}.
		 * 
		 * <p>
		 * A {@link TabBarNode} may have children nodes/tabs.
		 * </p>
		 * 
		 * @see TabNode
		 */
		static class TabBarNode extends Node implements TabBarListener, InvalidationListener {

			private final Card _card;

			private boolean _attached;

			/**
			 * Creates a {@link TabBarNode}.
			 * 
			 * @param model
			 *        See {@link #getModel()}.
			 * @param parent
			 *        See {@link #getParent()}.
			 * @param card
			 *        See {@link #getCard()}.
			 * @param tabComponent
			 *        The underlying {@link TabComponent}, see {@link #tabComponent()}.
			 */
			public TabBarNode(ComponentTree model, Node parent, Card card, TabComponent tabComponent) {
				super(model, parent, tabComponent);
				_card = card;
			}

			@Override
			public Card getCard() {
				return _card;
			}

			@Override
			public void validate() {
				DisplayContext actionContext = DefaultDisplayContext.getDisplayContext();
				LayoutContext layoutContext = actionContext.getLayoutContext();
				final LayoutComponent rootComponent = tabComponent();
				layoutContext.notifyInvalid(new ToBeValidated() {
					int _cnt = 0;

					@Override
					public void validate(DisplayContext context) {
						boolean again = validate(context, rootComponent);
						_cnt++;
						if (again) {
							if (_cnt > 100) {
								throw new IllegalStateException("Cannot validate component tree.");
							}
							// Start over after processing other validation action scheduled during
							// this validation step.
							context.getLayoutContext().notifyInvalid(this);
						} else {
							reset();
						}
					}
					
					private boolean validate(DisplayContext context, LayoutComponent component) {
						boolean again = false;
						if (!component.isModelValid()) {
							again = component.validateModel(context);
						}
						again |= validateSubComponents(context, component);
						return again;
					}

					private boolean validateSubComponents(DisplayContext context, LayoutComponent parent) {
						boolean again = false;
						if (parent instanceof LayoutContainer) {
							for (LayoutComponent child : ((LayoutContainer) parent).getVisibleChildren()) {
								again |= validate(context, child);
							}
						}
						return again;
					}

				});
			}
			
			/**
			 * Make {@link #resetChildren()} accessible for inner classes.
			 */
			protected final void reset() {
				resetChildren();
			}

			@Override
			public List<Node> createChildList() {
				TabBarModel model = tabBar();
				ArrayList<Node> result = new ArrayList<>();
				for (Card card : model.getAllCards()) {
					if (model.isInactive(card)) {
						continue;
					}

					TabbedLayoutComponent tab = (TabbedLayoutComponent) card;
					LayoutComponent contentComponent = skipLayouts((LayoutComponent) tab.getContent());

					if (contentComponent instanceof TabComponent && embedd(contentComponent)) {
						result.add(new TabBarNode(getModel(), this, card, (TabComponent) contentComponent));
					} else {
						result.add(new TabNode(getModel(), this, card));
					}
				}

				if (isAttached()) {
					for (Node node : result) {
						node.attach();
					}
				}

				return result;
			}

			private boolean embedd(LayoutComponent tabComponent) {
				LayoutControlProvider cp = tabComponent.getComponentControlProvider();
				return cp instanceof DeckpaneOnlyTabControlProvider;
			}

			private LayoutComponent skipLayouts(final LayoutComponent content) {
				if (content instanceof Layout) {
					List<LayoutComponent> children = ((Layout) content).getChildList();
					LayoutComponent uniqueTab = null;
					for (LayoutComponent child : children) {
						LayoutComponent inner = skipLayouts(child);
						if (inner instanceof TabComponent) {
							if (uniqueTab != null) {
								return content;
							}
							uniqueTab = inner;
						}
					}

					if (uniqueTab != null) {
						return uniqueTab;
					}
				}
				return content;
			}

			/**
			 * The underlying {@link TabComponent}.
			 * 
			 * <p>
			 * The {@link TabComponent} is used as {@link #getBusinessObject()} of
			 * {@link TabBarNode}s.
			 * </p>
			 */
			private TabComponent tabComponent() {
				return (TabComponent) getBusinessObject();
			}

			/**
			 * The {@link TabBarModel} of {@link #tabBar()}.
			 */
			private TabBarModel tabBar() {
				return tabComponent().getTabBarModel();
			}

			@Override
			public boolean isAttached() {
				return _attached;
			}

			@Override
			public boolean attach() {
				if (_attached) {
					return false;
				}
				_attached = true;
				tabBar().addTabBarListener(this);
				tabComponent().addInvalidationListener(this);
				if (isInitialized()) {
					for (Node child : getChildren()) {
						child.attach();
					}
				}
				return true;
			}

			@Override
			public boolean detach() {
				if (!_attached) {
					return false;
				}
				_attached = false;
				tabComponent().removeInvalidationListener(this);
				tabBar().removeTabBarListener(this);
				if (isInitialized()) {
					for (Node child : getChildren()) {
						child.attach();
					}
				}
				return true;
			}

			@Override
			protected void notifyRemoved() {
				super.notifyRemoved();

				detach();
			}

			@Override
			public void inactiveCardChanged(TabBarModel sender, Card aCard) {
				resetChildren();
			}

			@Override
			public void notifyCardsChanged(TabBarModel sender, List<Card> oldAllCards) {
				resetChildren();
			}

			@Override
			public void notifyInvalid(Object invalidObject) {
				// Relevant, if tab decorations change.
				for (Node child : getChildren()) {
					child.updateNodeProperties();
				}
			}

			@Override
			public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject,
					Object selectedObject) {
				if (tabComponent().isVisible()) {
					// Note: The model itself chooses the innermost selected node.
					getModel().handleSelectionChange(this);
				}
			}

			@Override
			public Node getSelected() {
				TabBarModel tabbar = tabBar();
				int index = tabbar.getSelectedIndex();
				if (index >= 0) {
					Card selectedObject = tabbar.getAllCards().get(index);
					for (Node child : getChildren()) {
						if (child.getCard() == selectedObject) {
							return child;
						}
					}
				}
				return null;
			}

			@Override
			protected void setSelected(Node node) {
				ScriptingRecorder.pause();
				try {
					tabComponent().makeVisible();
				} finally {
					ScriptingRecorder.resume();
				}
				TabBarModel tabbar = tabBar();
				int cardIndex = tabbar.getAllCards().indexOf(node.getCard());
				tabbar.setSelectedIndex(cardIndex);
			}

		}

		/**
		 * A leaf node in the {@link ComponentTree}.
		 * 
		 * <p>
		 * A {@link TabNode} represents a content component or layout that cannot be expanded any
		 * more.
		 * </p>
		 * 
		 * @see TabBarNode
		 */
		static class TabNode extends Node {

			/**
			 * Creates a {@link TabNode}.
			 * 
			 * @param model
			 *        See {@link #getModel()}.
			 * @param parent
			 *        See {@link #getParent()}.
			 * @param card
			 *        See {@link #getCard()}.
			 */
			public TabNode(ComponentTree model, Node parent, Card card) {
				super(model, parent, card);
			}

			@Override
			public Card getCard() {
				return (Card) getBusinessObject();
			}

			@Override
			public void validate() {
				// No children.
			}

			@Override
			public List<Node> createChildList() {
				return Collections.emptyList();
			}

			@Override
			public boolean isAttached() {
				return false;
			}

			@Override
			public boolean attach() {
				return false;
			}

			@Override
			public boolean detach() {
				return false;
			}

			@Override
			public Node getSelected() {
				return null;
			}

			@Override
			protected void setSelected(Node node) {
				// Ignore.
			}
		}

	}

	/**
	 * {@link ResourceProvider} for nodes of the {@link ComponentTree}.
	 */
	public static final class NodeResources extends AbstractNodeResources {

		/**
		 * Singleton {@link SidebarLayoutControlProvider.NodeResources} instance.
		 */
		public static final SidebarLayoutControlProvider.NodeResources INSTANCE =
			new SidebarLayoutControlProvider.NodeResources();

		private NodeResources() {
			// Singleton constructor.
		}

	}

	/**
	 * Base class for {@link ResourceProvider} implementations for the component tree.
	 */
	public static class AbstractNodeResources extends AbstractResourceProvider {
	
		@Override
		public String getLabel(Object object) {
			TabInfo info = info(object);
			if (info == null) {
				return null;
			}
			return info.getLabel();
		}
	
		@Override
		public ThemeImage getImage(Object object, Flavor aFlavor) {
			TabInfo info = info(object);
			if (info == null) {
				return null;
			}
			if (aFlavor == Flavor.EXPANDED) {
				return info.getImageSelected();
			}
			return info.getImage();
		}
	
		/**
		 * The {@link TabInfo} of the given component node, or <code>null</code> if the given node
		 * is not a tab itself.
		 */
		protected final TabInfo info(Object object) {
			if (object == null) {
				return null;
			}
	
			Card card = ((ComponentTree.Node) object).getCard();
			if (card == null) {
				return null;
			}
	
			TabInfo info = (TabInfo) card.getCardInfo();
			return info;
		}
	
	}

	/**
	 * {@link SingleSelectVetoListener} to ask before switching tabs.
	 */
	private final class SelectionVeto implements SingleSelectVetoListener {

		private final ComponentTree _tree;
	
		/**
		 * Creates a {@link SelectionVeto}.
		 * 
		 * @param tree
		 *        The component tree for resolving selection paths to {@link Node}s.
		 */
		public SelectionVeto(ComponentTree tree) {
			_tree = tree;
		}
	
		@Override
		public void checkVeto(SingleSelectionModel singleSelectionModel, Object newSelection,
				boolean programmaticUpdate)
				throws VetoException {
			DirtyHandling.checkVeto(getVetoHandlers(singleSelectionModel));
		}
	
		private Collection<? extends ChangeHandler> getVetoHandlers(SingleSelectionModel selectionModel) {
			TreePath oldSelection = (TreePath) selectionModel.getSingleSelection();
			if (oldSelection == null) {
				return Collections.emptyList();
			}
			Node oldSelectedNode = oldSelection.toNode(_tree);
			if (oldSelectedNode == null) {
				return Collections.emptyList();
			}
			LayoutComponent cardContent = (LayoutComponent) oldSelectedNode.getCard().getContent();
			return ChildrenCheckScopeProvider.INSTANCE.getCheckScope(cardContent).getAffectedFormHandlers();
		}
	}

}
