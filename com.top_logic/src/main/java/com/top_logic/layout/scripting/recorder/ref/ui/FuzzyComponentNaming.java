/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TransformIterators;
import com.top_logic.basic.col.TreeView;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.col.search.SearchResult;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.component.AJAXComponent;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.scripting.ScriptingUtil;
import com.top_logic.layout.scripting.recorder.ref.GlobalModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.runtime.action.NamedTabSwitchOp;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.tabbar.TabInfo;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.DialogComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.QualifiedComponentNameConstraint;
import com.top_logic.mig.html.layout.tiles.ContextTileComponent;
import com.top_logic.mig.html.layout.tiles.GroupTileComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;
import com.top_logic.mig.html.layout.tiles.TileComponentFinder;
import com.top_logic.mig.html.layout.tiles.component.InlinedTileComponent;
import com.top_logic.mig.html.layout.treeview.ComponentTreeViewNoDialogsOrWindows;
import com.top_logic.util.Resources;

/**
 * {@link ModelNamingScheme} for {@link LayoutComponent}s which stores the path to the component as
 * "Breadcrumb Path": <br/>
 * <code>First Level Tab > Second Level Tab > Third Level Tab</code>
 * 
 * <p>
 * Moreover if the component is part of a {@link RootTileComponent}, the breadcrumb path of the
 * {@link RootTileComponent} is used to identify the component.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class FuzzyComponentNaming extends GlobalModelNamingScheme<LayoutComponent, FuzzyComponentNaming.Name>
		implements LayoutComponentResolver {

	/** The {@link ModelName} used by the {@link FuzzyComponentNaming}. */
	public static interface Name extends ModelName {

		/**
		 * The path to a tab as "Breadcrumb String".
		 * 
		 * @see BreadcrumbStrings
		 */
		@Format(BreadcrumbStrings.class)
		List<String> getTabPath();

		/** @see #getTabPath() */
		void setTabPath(List<String> value);

		/**
		 * The technical name of the searched component, if it is not a direct child of a
		 * {@link TabComponent}.
		 */
		@Constraint(QualifiedComponentNameConstraint.class)
		ComponentName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(ComponentName name);

	}

	/**
	 * Creates a new {@link FuzzyComponentNaming}.
	 */
	public FuzzyComponentNaming() {
		super(LayoutComponent.class, Name.class);
	}

	@Override
	public LayoutComponent locateModel(ActionContext context, Name name) {
		return locateModel(context.getDisplayContext(), name);
	}

	private LayoutComponent locateModel(DisplayContext context, Name name) {
		List<String> componentLabels = name.getTabPath();
		LayoutComponent layout = getRootLayout(context);
		for (int i = 0, componentNamesSize = componentLabels.size(); i < componentNamesSize;) {
			String label = componentLabels.get(i);
			int processedLabels = 1;
			SearchResult<LayoutComponent> searchResult = new SearchResult<>();
			for (TabComponent tab : descendantsOrSelf(layout, TabComponent.class)) {
				int index = NamedTabSwitchOp.findCardIndexByLabel(tab, label);
				if (index == NamedTabSwitchOp.NOTHING_FOUND) {
					continue;
				}
				LayoutComponent childComponent = tab.getChild(index);
				assert childComponent != null : "No component for tab " + printSubPath(componentLabels, i + 1) + ".";
				searchResult.add(childComponent);
			}
			Resources resources = Resources.getInstance();
			roots:
			for (RootTileComponent tile : descendantsOrSelf(layout, RootTileComponent.class)) {
				List<LayoutComponent> displayedPath = tile.displayedPath();
				if (displayedPath.isEmpty()) {
					continue;
				}
				int pathIndex = 0;
				for (LayoutComponent inPath : displayedPath) {
					int labelsIndex = i + pathIndex;
					if (labelsIndex >= componentNamesSize) {
						break;
					}

					if (!componentLabels.get(labelsIndex).equals(componentLabel(resources, inPath))) {
						continue roots;
					}
					pathIndex++;
				}
				processedLabels = pathIndex;
				searchResult.add(displayedPath.get(pathIndex - 1));
			}

			switch (searchResult.getResults().size()) {
				case 0: {
					if (i == (componentNamesSize - 1)) {
						if (layout instanceof GroupTileComponent) {
							for (LayoutComponent child : ((GroupTileComponent) layout).getChildList()) {
								InlinedTileComponent inlinedComponent =
									TileComponentFinder.getFirstOfType(InlinedTileComponent.class, child);
								if (inlinedComponent != null
									&& label.equals(componentLabel(resources, inlinedComponent))) {
									/* Workaround: When recording the selection of an element in an
									 * InlinedTileComponent, the record occurs when the InlinedTile
									 * is visible, i.e. it is displayed in the tile path. When
									 * replaying the action, the component is not contained in the
									 * path, because selection causes the component to be contained
									 * in the path. */
									layout = inlinedComponent;
									break;
								}
							}
						} else {
							// Last entry can be the title of the component.
							layout = resolveByTitle(layout, label);
							if (layout == null) {
								ApplicationAssertions.fail(name, "Component with title '" + label + "' not found in "
									+ printSubPath(componentLabels, i + 1) + ".");
							}
						}
					} else {
						// Will break command execution, because there is no result
						searchResult
							.getSingleResult(
								"No component " + printPath(componentLabels) + " found.");
						throw new UnreachableAssertion("No result, getSingleResult() must fail.");
					}
					break;
				}
				case 1: {
					layout = searchResult.getSingleResult("Must not fail!");
					break;
				}
				default: {
					// Will break command execution, because there is more than one result
					searchResult.getSingleResult(
						"Search for tab " + printPath(componentLabels) + " failed.");
					throw new UnreachableAssertion("Non-unique result, getSingleResult() must fail.");
				}
			}
			i += processedLabels;
		}
		if (name.getName() != null) {
			layout = findComponent(layout, name.getName(), componentLabels);
		}
		return layout;
	}

	private LayoutComponent resolveByTitle(LayoutComponent layout, String title) {
		Resources resources = Resources.getInstance();
		SearchResult<LayoutComponent> searchResult = new SearchResult<>();
		layout.visitChildrenRecursively(new DefaultDescendingLayoutVisitor() {
			@Override
			public boolean visitLayoutComponent(LayoutComponent aComponent) {
				if (!aComponent.isVisible()) {
					return false;
				}
				if (title.equals(componentLabel(resources, aComponent))) {
					searchResult.add(aComponent);
					// Stop descending
					return false;
				}
				return super.visitLayoutComponent(aComponent);
			}
		});
		return searchResult.getSingleResult("Search for component " + title + " failed.");
	}

	private LayoutComponent getRootLayout(DisplayContext context) {
		WindowScope windowScope = context.getWindowScope();
		LayoutComponent topLevelComponent = ScriptingUtil.getComponent(windowScope);
		for (DialogWindowControl activeDialog : getDialogs(windowScope)) {
			if (activeDialog.getDialogModel() instanceof DialogComponent) {
				return ((DialogComponent) activeDialog.getDialogModel()).getContentComponent();
			}
		}
		return topLevelComponent;
	}

	private List<DialogWindowControl> getDialogs(WindowScope windowScope) {
		List<DialogWindowControl> dialogs = CollectionFactory.list(windowScope.getDialogs());
		/* Change the order from top-most dialog last to top-most dialog first. */
		Collections.reverse(dialogs);
		return dialogs;
	}

	@Override
	public Maybe<Name> buildName(LayoutComponent model) {
		List<String> path = buildPath(model);
		if (path == null) {
			return Maybe.none();
		}

		LayoutComponent parent = getParent(model);
		boolean pathIsSufficient = parent == null || parent instanceof TabComponent;
		if (!pathIsSufficient) {
			LayoutComponent displayedComponent = RootTileComponent.getDisplayedComponent(model);
			if (model == displayedComponent) {
				// component displayed in path is the model.
				pathIsSufficient = true;
			}
		}

		Name name = createName();
		if (pathIsSufficient) {
			// Component is root layout, direct child of a tab, or part of the displayed path in a
			// tile. No additional information needed.
		} else {
			Resources resources = Resources.getInstance();
			String title = componentLabel(resources, model);
			if (!StringServices.EMPTY_STRING.equals(title) && isUniqueTitle(resources, model, title)) {
				path.add(title);
			} else {
				name.setName(model.getName());
			}
		}
		name.setTabPath(path);

		return checkName(name, model);
	}

	private Maybe<Name> checkName(Name name, LayoutComponent model) {
		// Try to resolve.
		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		Object resolved = locateModel(context, name);
		if (resolved != model) {
			return Maybe.none();
		}
		return Maybe.some(name);
	}

	private boolean isUniqueTitle(Resources resources, LayoutComponent model, String title) {
		LayoutComponent nextComponentPathContent = null;
		LayoutComponent parent = getParent(model);
		while (parent != null) {
			if (parent instanceof TabComponent || parent instanceof RootTileComponent) {
				nextComponentPathContent = parent;
				break;
			}
			if (parent instanceof ContextTileComponent || parent instanceof GroupTileComponent) {
				nextComponentPathContent = RootTileComponent.getDisplayedComponent(parent);
				break;
			}
			parent = getParent(parent);
		}
		if (nextComponentPathContent == null) {
			return true;
		}
		BooleanFlag uniqueTitle = new BooleanFlag(true);
		// Tab must not be visited, because the label doesn't matter.
		nextComponentPathContent.visitChildrenRecursively(new DefaultDescendingLayoutVisitor() {
			@Override
			public boolean visitAJAXComponent(AJAXComponent aComponent) {
				if (!uniqueTitle.get() || aComponent == model) {
					return false;
				}
				if (title.equals(componentLabel(resources, aComponent))) {
					uniqueTitle.set(false);
					return false;
				}
				return super.visitAJAXComponent(aComponent);
			}
		});
		return uniqueTitle.get();
	}

	/**
	 * The path of labels to find the given component, or <code>null</code> if this is not possible.
	 */
	private List<String> buildPath(LayoutComponent component) {
		List<String> path = new ArrayList<>();

		while (true) {
			LayoutComponent parent = getParent(component);
			if (parent == null) {
				break;
			}
			if (parent instanceof TabComponent) {
				String tabLabel = getTabLabel(component, (TabComponent) parent);
				path.add(tabLabel);
				component = parent;
			} else if (parent instanceof GroupTileComponent || parent instanceof ContextTileComponent
				|| parent instanceof RootTileComponent) {
				LayoutComponent displayedComponent = RootTileComponent.getDisplayedComponent(component);
				RootTileComponent rootTile = RootTileComponent.getRootTile(displayedComponent); 
				List<LayoutComponent> displayedPath = rootTile.displayedPath();
				for (int i = displayedPath.lastIndexOf(displayedComponent); i >= 0; i--) {
					String label = componentLabel(Resources.getInstance(), displayedPath.get(i));
					if (StringServices.EMPTY_STRING.equals(label)) {
						// Component without name cannot be identified.
						return null;
					}
					path.add(label);
				}
				component = rootTile;
			} else {
				component = parent;
			}
		}

		Collections.reverse(path);
		return path;
	}

	private String componentLabel(Resources resources, LayoutComponent model) {
		if (model.getParent() instanceof ContextTileComponent context) {
			// Constant signaling that the content of a context selector is resolved.
			return "{0}";
		}
		ResKey titleKey = LayoutComponent.Config.getEffectiveTitleKey(model.getConfig());
		return resources.getString(titleKey, StringServices.EMPTY_STRING);
	}

	private LayoutComponent getParent(LayoutComponent component) {
		// The outermost dialog component is treated as a root,
		// as everything outside the dialog cannot be used anyway.
		if (component == component.getDialogTopLayout()) {
			return null;
		}
		return component.getParent();
	}

	private String getTabLabel(LayoutComponent tab, TabComponent tabComponent) {
		int tabIndex = tabComponent.getIndexOfChild(tab);

		Card card = tabComponent.getCard(tabIndex);
		TabInfo tabInfo = (TabInfo) card.getCardInfo();
		return tabInfo.getLabel();
	}

	private <T extends LayoutComponent> Iterable<T> descendantsOrSelf(LayoutComponent component, Class<T> implClass) {
		if (implClass.isInstance(component)) {
			return Collections.singletonList(implClass.cast(component));
		}
		return new Iterable<>() {
			@Override
			public Iterator<T> iterator() {
				return TransformIterators.transform(
					TransformIterators.transform(descend(), implClass::isInstance),
					implClass::cast);
			}

			private DescendantDFSIterator<LayoutComponent> descend() {
				return new DescendantDFSIterator<>(noTabOrTileChildren(), component);
			}

			private TreeView<LayoutComponent> noTabOrTileChildren() {
				return new ComponentTreeViewNoDialogsOrWindows() {
					@Override
					protected List<LayoutComponent> children(LayoutComponent node) {
						if (node instanceof TabComponent) {
							return Collections.<LayoutComponent> emptyList();
						}
						if (node instanceof RootTileComponent) {
							return Collections.<LayoutComponent> emptyList();
						}
						return super.children(node);
					}
				};
			}

		};
	}

	private LayoutComponent findComponent(LayoutComponent component, ComponentName name, List<String> path) {
		SearchResult<LayoutComponent> result = new SearchResult<>();
		findComponent(Collections.singletonList(component), name, result);
		return result.getSingleResult("Search for component '" + name + "' in " + printPath(path) + " failed.");
	}

	private String printSubPath(List<String> componentLabels, int i) {
		return printPath(componentLabels.subList(0, i));
	}

	private String printPath(List<String> path) {
		return '"' + BreadcrumbStrings.INSTANCE.getSpecification(path) + '"';
	}

	private void findComponent(
			LayoutContainer component, ComponentName name, SearchResult<LayoutComponent> result) {

		if (component instanceof TabComponent) {
			// Don't search within TabComponents, as the path would contain the tab name
			// if the searched component was within a tab.
			return;
		}
		findComponent(CollectionUtil.nonNull(component.getChildList()), name, result);
	}

	private void findComponent(
			Collection<? extends LayoutComponent> children, ComponentName name, SearchResult<LayoutComponent> result) {
		for (LayoutComponent child : children) {
			ComponentName childName = child.getName();
			result.addCandidate(childName);
			if (StringServices.equals(childName, name)) {
				result.add(child);
				// Don't return, keep on searching for multiple matches, which would be errors.
			}
			if (child instanceof LayoutContainer) {
				findComponent((LayoutContainer) child, name, result);
			}
		}
	}

}
