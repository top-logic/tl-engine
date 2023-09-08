/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.breadcrumb;

import static com.top_logic.basic.util.Utils.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.basic.check.ChildrenCheckScopeProvider;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.component.title.TitleProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.provider.ConfiguredProxyResourceProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.selection.DefaultSingleSelectVetoListener;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbControl;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbData;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbDataOwner;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbRenderer;
import com.top_logic.layout.tree.breadcrumb.GenericBreadcrumbDataOwner;
import com.top_logic.layout.tree.breadcrumb.GenericBreadcrumbDataOwner.AnnotatedModel;
import com.top_logic.layout.tree.breadcrumb.MutableBreadcrumbData;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTLTreeNodeBuilder;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.GenericSelectionModelOwner;
import com.top_logic.mig.html.NoImageResourceProvider;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.FindFirstMatchingComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.ContextTileComponent;
import com.top_logic.mig.html.layout.tiles.GroupTileComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent.DisplayPathListener;
import com.top_logic.mig.html.layout.tiles.TileComponentFinder;
import com.top_logic.mig.html.layout.tiles.TileInfo;
import com.top_logic.mig.html.layout.tiles.component.InlinedTileComponent;
import com.top_logic.mig.html.layout.tiles.component.LabelBasedPreview;
import com.top_logic.mig.html.layout.tiles.component.TilePreview;
import com.top_logic.util.Resources;

/**
 * {@link ControlProvider} creating a {@link BreadcrumbControl} in a {@link RootTileComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RootTileBreadcrumbControlProvider extends
		AbstractConfiguredInstance<RootTileBreadcrumbControlProvider.Config> implements ControlProvider, TitleProvider {

	/**
	 * {@link NoImageResourceProvider} for the nodes in the breadcrumb view.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class BreadcrumbNodeResourceProvider
			extends
			ConfiguredProxyResourceProvider<ConfiguredProxyResourceProvider.Config<BreadcrumbNodeResourceProvider>> {

		/**
		 * Property that can be attached to a breadcrumb node to have a custom {@link ImageProvider
		 * image} for that node.
		 * 
		 * <p>
		 * The attached {@link ImageProvider} is called with the business object of the attached
		 * node.
		 * </p>
		 */
		public static final TypedAnnotatable.Property<ImageProvider> CUSTOM_ICON =
			TypedAnnotatable.property(ImageProvider.class, "custom icon");

		/**
		 * Property that can be attached to a breadcrumb node to have a custom {@link LabelProvider
		 * label} for that node.
		 * 
		 * <p>
		 * The attached {@link LabelProvider} is called with the business object of the attached
		 * node.
		 * </p>
		 */
		public static final TypedAnnotatable.Property<LabelProvider> CUSTOM_LABEL =
			TypedAnnotatable.property(LabelProvider.class, "custom label");

		/**
		 * Creates a new {@link BreadcrumbNodeResourceProvider}.
		 */
		public BreadcrumbNodeResourceProvider(InstantiationContext context,
				ConfiguredProxyResourceProvider.Config<BreadcrumbNodeResourceProvider> config) {
			super(context, config);
		}

		@Override
		public String getLabel(Object anObject) {
			if (anObject instanceof TLTreeNode<?>) {
				LabelProvider customLabel = asNode(anObject).get(CUSTOM_LABEL);
				if (customLabel != null) {
					return customLabel.getLabel(asNode(anObject).getBusinessObject());
				}
			}
			return super.getLabel(anObject);
		}

		@Override
		public ThemeImage getImage(Object anObject, Flavor aFlavor) {
			if (anObject instanceof TLTreeNode<?>) {
				ImageProvider customIcon = asNode(anObject).get(CUSTOM_ICON);
				if (customIcon != null) {
					return customIcon.getImage(asNode(anObject).getBusinessObject(), aFlavor);
				}
			}
			return super.getImage(anObject, aFlavor);
		}

		private static TLTreeNode<?> asNode(Object obj) {
			return (TLTreeNode<?>) obj;
		}

	}

	/** {@link ConfigurationItem} for the {@link RootTileBreadcrumbControlProvider}. */
	public interface Config extends PolymorphicConfiguration<RootTileBreadcrumbControlProvider> {

		/** Property name of {@link #getRenderer()}. */
		String RENDERER = "renderer";

		/**
		 * The {@link BreadcrumbRenderer} that should render the breadcrumb.
		 */
		@Name(RENDERER)
		PolymorphicConfiguration<BreadcrumbRenderer> getRenderer();

	}

	private static class UpdateSelectionOnDisplayPathChange implements DisplayPathListener {

		private BreadcrumbControl _control;

		public UpdateSelectionOnDisplayPathChange(BreadcrumbControl ctrl) {
			_control = ctrl;
		}

		@Override
		public void handleDisplayedPathChanged(RootTileComponent sender, List<LayoutComponent> oldPath,
				List<LayoutComponent> newPath) {
			updateDisplayedNode(newPath);
		}

		void updateDisplayedNode(List<LayoutComponent> newPath) {
			if (newPath.isEmpty()) {
				return;
			}
			TLTreeModel<?> tree = _control.getTree();
			Object newDisplaySelection = findLastTreeNode(newPath, tree, false);
			if (newDisplaySelection != null) {
				displayModel().setSingleSelection(newDisplaySelection);
			} else {
				// newPath could not be found in the current tree. It may be that the tree is not up
				// to date and is created later
				DefaultDisplayContext.getDisplayContext().getLayoutContext().notifyInvalid(context -> {
					if (!_control.isAttached()) {
						// Control was detached in the meanwhile.
						return;
					}
					TLTreeModel<?> newTree = _control.getTree();
					displayModel().setSingleSelection(findLastTreeNode(newPath, newTree, true));
				});
			}
		}

		private SingleSelectionModel displayModel() {
			return _control.getModel().getDisplayModel();
		}

		private <N> N findLastTreeNode(List<LayoutComponent> newPath, TLTreeModel<N> tree, boolean bestMatch) {
			int idx = 0;
			N node = tree.getRoot();
			if (isComponent(tree, node, newPath.get(idx))) {
				if (newPath.size() == 1) {
					return node;
				}
				idx = 1;
			}

			pathSegment:
			for (; idx < newPath.size(); idx++) {
				Iterator<? extends N> children = tree.getChildIterator(node);
				while (children.hasNext()) {
					N child = children.next();
					if (isComponent(tree, child, newPath.get(idx))) {
						node = child;
						continue pathSegment;
					}
				}
				if (bestMatch) {
					break;
				} else {
					return null;
				}
			}
			return node;
		}

		<N> boolean isComponent(TLTreeModel<N> tree, N node, LayoutComponent component) {
			return ((LayoutComponent) tree.getBusinessObject(node)).equals(component);
		}

	}

	private static class BreadcrumbSelectionChanged implements SingleSelectionListener {

		private final RootTileComponent _component;

		public BreadcrumbSelectionChanged(RootTileComponent component) {
			_component = component;
		}

		@Override
		public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject,
				Object selectedObject) {
			if (selectedObject instanceof TLTreeNode<?>) {
				selectedObject = ((TLTreeNode<?>) selectedObject).getBusinessObject();
			}
			if (!(selectedObject instanceof LayoutComponent)) {
				return;
			}
			LayoutComponent selected = (LayoutComponent) selectedObject;
			_component.displayTileLayout(selected);
		}
	}

	private final BreadcrumbRenderer _renderer;

	/**
	 * Called by the {@link TypedConfiguration} for creating a
	 * {@link RootTileBreadcrumbControlProvider}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public RootTileBreadcrumbControlProvider(InstantiationContext context, Config config) {
		super(context, config);
		_renderer = requireNonNull(context.getInstance(config.getRenderer()));
	}

	@Override
	public HTMLFragment createTitle(LayoutComponent component) {
		return createBreadbrumb(component);
	}

	@Override
	public ResKey getSimpleTitle(LayoutComponent.Config component) {
		return component.getTitleKey();
	}

	@Override
	public BreadcrumbControl createControl(Object model, String style) {
		if (style != null) {
			throw new IllegalArgumentException(
				"There are no diffent styles. The only valid parameter is null. Actual value: " + debug(style));
		}
		return createBreadbrumb(model);
	}

	private BreadcrumbControl createBreadbrumb(Object model) {
		RootTileComponent component = asRootTile(model);
		BreadcrumbControl ctrl = createControl(createData(component));

		CheckScope checkScope = ChildrenCheckScopeProvider.INSTANCE.getCheckScope(component);
		ctrl.addSelectionVetoListener(new DefaultSingleSelectVetoListener(checkScope));

		AttachedPropertyListener observeDisplayedPath = new AttachedPropertyListener() {

			private UpdateSelectionOnDisplayPathChange _displayedPathObserver =
				new UpdateSelectionOnDisplayPathChange(ctrl);

			@Override
			public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					component.addListener(RootTileComponent.DISPLAYED_PATH_PROPERTY, _displayedPathObserver);
					// Bring view in sync
					_displayedPathObserver.updateDisplayedNode(component.displayedPath());
				} else {
					component.removeListener(RootTileComponent.DISPLAYED_PATH_PROPERTY, _displayedPathObserver);
				}
			}
		};
		ctrl.addListener(AbstractControlBase.ATTACHED_PROPERTY, observeDisplayedPath);
		return ctrl;
	}

	private static RootTileComponent asRootTile(Object model) {
		if (!(model instanceof RootTileComponent)) {
			throw new IllegalArgumentException(
				"The model has to be a " + RootTileComponent.class.getName() + ". Actual value: " + debug(model));
		}
		return (RootTileComponent) model;
	}

	private BreadcrumbData createData(RootTileComponent component) {
		TLTreeModel<?> treeModel = createTreeModel(component);
		SingleSelectionModel selectionModel = createSelectionModel(component);
		BreadcrumbData data = createData(component, treeModel, selectionModel);
		return data;
	}

	private SingleSelectionModel createSelectionModel(RootTileComponent component) {
		GenericSelectionModelOwner.AnnotatedModel algorithm = GenericSelectionModelOwner.AnnotatedModel.INSTANCE;
		SelectionModelOwner owner = new GenericSelectionModelOwner(component, algorithm);
		DefaultSingleSelectionModel selectionModel = new DefaultSingleSelectionModel(owner);
		selectionModel.addSingleSelectionListener(new BreadcrumbSelectionChanged(component));

		algorithm.annotate(component, selectionModel);
		return selectionModel;
	}

	private BreadcrumbData createData(RootTileComponent component, TLTreeModel<?> treeModel,
			SingleSelectionModel selectionModel) {
		AnnotatedModel algorithm = GenericBreadcrumbDataOwner.AnnotatedModel.INSTANCE;
		BreadcrumbDataOwner owner = new GenericBreadcrumbDataOwner(component, algorithm);
		BreadcrumbData data = new MutableBreadcrumbData(treeModel, selectionModel, selectionModel, owner);
		algorithm.annotate(component, data);
		return data;
	}

	private BreadcrumbControl createControl(BreadcrumbData data) {
		return new BreadcrumbControl(getRenderer(), data);
	}

	private TLTreeModel<?> createTreeModel(RootTileComponent component) {
		LayoutComponent rootBO = component.getChild();
		if (rootBO == null) {
			rootBO = component;
		}
		return new DefaultMutableTLTreeModel(new BCTreeBuilder(), rootBO);
	}

	private final BreadcrumbRenderer getRenderer() {
		return _renderer;
	}

	private static class BCTreeBuilder extends AbstractTLTreeNodeBuilder {

		BCTreeBuilder() {
		}

		@Override
		public boolean isFinite() {
			return true;
		}

		@Override
		public List<DefaultMutableTLTreeNode> createChildList(DefaultMutableTLTreeNode node) {
			LayoutComponent businessObject = (LayoutComponent) node.getBusinessObject();

			if (businessObject instanceof ContextTileComponent) {
				return createChildrenFor((ContextTileComponent) businessObject, node);
			} else if (businessObject instanceof GroupTileComponent) {
				return createChildrenFor((GroupTileComponent) businessObject, node);
			}

			// Not a supported component type itself, but a parent may be displayed.
			FindFirstMatchingComponent visitor = new FindFirstMatchingComponent(
				component -> component instanceof ContextTileComponent || component instanceof GroupTileComponent);
			businessObject.acceptVisitorRecursively(visitor);

			LayoutComponent relevantChild = visitor.result();
			if (relevantChild instanceof ContextTileComponent) {
				return createChildrenFor((ContextTileComponent) relevantChild, node);
			} else if (relevantChild instanceof GroupTileComponent) {
				return createChildrenFor((GroupTileComponent) relevantChild, node);
			} else {
				return Collections.emptyList();
			}
		}

		private List<DefaultMutableTLTreeNode> createChildrenFor(ContextTileComponent contextTile,
				DefaultMutableTLTreeNode parent) {
			LayoutComponent content = contextTile.getContent();
			if (content == null) {
				return Collections.emptyList();
			}
			DefaultMutableTLTreeNode contentNode = createNode(parent.getModel(), parent, content);
			contentNode.set(BreadcrumbNodeResourceProvider.CUSTOM_LABEL, new LabelProvider() {

				@Override
				public String getLabel(Object object) {
					/* Ignore the business object of the displayed node (the detail component) value
					 * and display the actual context selection. */
					Object selection = contextTile.getContextSelection().getSelected();
					String contextSelection = MetaLabelProvider.INSTANCE.getLabel(selection);
					return Resources.getInstance().getString(ResKey.message(content.getTitleKey(), contextSelection),
						contextSelection);
				}
			});
			contentNode.set(BreadcrumbNodeResourceProvider.CUSTOM_ICON, new ImageProvider() {

				@Override
				public ThemeImage getImage(Object obj, Flavor flavor) {
					/* Ignore the business object of the displayed node (the detail component) value
					 * and display the image of the actual context selection. */
					Object selection = contextTile.getContextSelection().getSelected();
					return MetaResourceProvider.INSTANCE.getImage(selection, flavor);
				}
			});
			return Collections.singletonList(contentNode);
		}

		private List<DefaultMutableTLTreeNode> createChildrenFor(GroupTileComponent groupTile,
				DefaultMutableTLTreeNode parent) {
			return groupTile.getChildList()
				.stream()
				.map(child -> createNode(parent.getModel(), parent, child))
				.collect(Collectors.toList());
		}

		@Override
		public DefaultMutableTLTreeNode createNode(AbstractMutableTLTreeModel<DefaultMutableTLTreeNode> model,
				DefaultMutableTLTreeNode parent, Object userObject) {
			DefaultMutableTLTreeNode node = super.createNode(model, parent, userObject);
			LayoutComponent comp = (LayoutComponent) userObject;
			InlinedTileComponent inlinedComp =
				TileComponentFinder.getFirstOfType(InlinedTileComponent.class, comp);
			if (inlinedComp != null) {
				node.set(BreadcrumbNodeResourceProvider.CUSTOM_LABEL, new LabelProvider() {

					@Override
					public String getLabel(Object object) {
						Object selected = inlinedComp.getSelected();
						if (selected != null) {
							return MetaLabelProvider.INSTANCE.getLabel(selected);
						}
						return MetaLabelProvider.INSTANCE.getLabel(object);
					}
				});
				node.set(BreadcrumbNodeResourceProvider.CUSTOM_ICON, new ImageProvider() {

					@Override
					public ThemeImage getImage(Object obj, Flavor flavor) {
						Object selected = inlinedComp.getSelected();
						if (selected != null) {
							return MetaResourceProvider.INSTANCE.getImage(selected, flavor);
						}
						return getIcon((LayoutComponent) obj);
					}
				});
			} else {
				ThemeImage image = getIcon(comp);
				if (image != null) {
					node.set(BreadcrumbNodeResourceProvider.CUSTOM_ICON, ImageProvider.constantImageProvider(image));
				}
			}
			return node;
		}

		ThemeImage getIcon(LayoutComponent component) {
			TileInfo tileInfo = component.getConfig().getTileInfo();
			if (tileInfo != null) {
				PolymorphicConfiguration<? extends TilePreview> preview = tileInfo.getPreview();
				if (preview instanceof LabelBasedPreview.Config<?>) {
					return ((LabelBasedPreview.Config<?>) preview).getIcon();
				}
			}
			return null;
		}

	}

}
