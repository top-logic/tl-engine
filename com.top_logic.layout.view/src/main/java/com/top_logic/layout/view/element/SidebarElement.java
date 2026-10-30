/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.GlobalKey;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.util.Resources;
import com.top_logic.layout.react.control.sidebar.DrawerToggleControl;
import com.top_logic.layout.react.control.sidebar.NavigationItem;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.control.sidebar.SeparatorItem;
import com.top_logic.layout.react.control.sidebar.SidebarItem;
import com.top_logic.layout.structure.PersonalizingExpandable;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.navigation.RevealPath;
import com.top_logic.layout.view.navigation.RevealRegistry;
import com.top_logic.layout.view.routing.WithFinalTitle;
import com.top_logic.layout.view.security.AccessChecks;
import com.top_logic.layout.view.security.UnitAccess;
import com.top_logic.layout.view.security.UnitHiding;
import com.top_logic.layout.view.security.WithAccessControl;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.slot.control.SlotContentControl;

/**
 * UIElement that wraps {@link ReactSidebarControl}.
 *
 * <p>
 * Renders a sidebar of items: navigation items with content that is lazily created when the item is
 * selected, collapsible groups of further items, headings, items executing a command, and
 * separators.
 * </p>
 */
@InApp
public class SidebarElement implements UIElement {

	/**
	 * Configuration for {@link SidebarElement}.
	 */
	@TagName("sidebar")
	@TypeIcon("css:bi bi-layout-sidebar")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(SidebarElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getItems()}. */
		String ITEMS = "items";

		/** Configuration name for {@link #getActiveItem()}. */
		String ACTIVE_ITEM = "active-item";

		/** Configuration name for {@link #getCollapsed()}. */
		String COLLAPSED = "collapsed";

		/** Configuration name for {@link #getDrawerOpenSlotName()}. */
		String DRAWER_OPEN_SLOT_NAME = "drawer-open-slot-name";

		/** Configuration name for {@link #getHeader()}. */
		String HEADER = "header";

		/** Configuration name for {@link #getHeaderCollapsed()}. */
		String HEADER_COLLAPSED = "header-collapsed";

		/** Configuration name for {@link #getFooter()}. */
		String FOOTER = "footer";

		/** Configuration name for {@link #getFooterCollapsed()}. */
		String FOOTER_COLLAPSED = "footer-collapsed";

		/**
		 * The sidebar items.
		 *
		 * @implNote Written directly as {@code <nav-item>}, {@code <group>}, {@code <header-item>},
		 *           {@code <command-item>} or {@code <separator>} children of the {@code <sidebar>}
		 *           (the {@code <items>} wrapper is optional). Keyed by
		 *           {@link SidebarItemConfig#getId()} so that a configuration fragment in another
		 *           module can add, reposition ({@code config:position}) or override individual items.
		 */
		@Name(ITEMS)
		@Key(SidebarItemConfig.ID)
		@DefaultContainer
		@TreeProperty
		List<SidebarItemConfig> getItems();

		/**
		 * The ID of the initially active item, or empty for the first navigation item.
		 */
		@Name(ACTIVE_ITEM)
		String getActiveItem();

		/**
		 * Whether the sidebar starts collapsed.
		 */
		@Name(COLLAPSED)
		@BooleanDefault(false)
		boolean getCollapsed();

		/**
		 * Name of the slot into which the sidebar contributes a hamburger button that opens the
		 * drawer while the app shell is compact.
		 *
		 * <p>
		 * If non-empty, the sidebar emits a {@code SlotContentControl} carrying a
		 * {@code DrawerToggleControl} addressed to {@code <slot name="<this-value>"/>}. By
		 * convention the placeholder is declared in the app bar's leading area (e.g.
		 * {@code <leading><slot name="appbar-leading"/></leading>}), so the button surfaces in
		 * the app bar, rendered only while the app shell is compact. Leave empty to suppress the
		 * contribution entirely — useful when the sidebar is used in a layout without an app bar or
		 * with a different entry point for the compact drawer.
		 * </p>
		 */
		@Name(DRAWER_OPEN_SLOT_NAME)
		String getDrawerOpenSlotName();

		/**
		 * What the rail shows above its items: a product logo, the name of the application, a
		 * search field.
		 *
		 * <p>
		 * The content stands at the top of the rail, outside the list of items, and is shown for as
		 * long as the sidebar is on screen. Left empty, the rail begins with its first item.
		 * </p>
		 *
		 * @implNote Created eagerly with the sidebar and handed to {@link ReactSidebarControl} as
		 *           its header content.
		 *           Keyed by {@link UIElement.Config#ID} so that a same-path overlay can
		 *           address an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(HEADER)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getHeader();

		/**
		 * What the rail shows above its items while it is folded to a narrow strip.
		 *
		 * <p>
		 * A folded rail offers room for an icon, not for a name and a search field, so it shows this
		 * in place of the header. Left empty, the folded rail has no header.
		 * </p>
		 *
		 * @implNote Created eagerly with the sidebar and handed to {@link ReactSidebarControl} as
		 *           its collapsed header content.
		 *           Keyed by {@link UIElement.Config#ID} so that a same-path overlay can
		 *           address an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(HEADER_COLLAPSED)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getHeaderCollapsed();

		/**
		 * What the rail shows below its items: the account area, a version note, a help link.
		 *
		 * <p>
		 * The content stands at the bottom of the rail, outside the list of items, and is shown for
		 * as long as the sidebar is on screen. Left empty, the rail ends with its last item.
		 * </p>
		 *
		 * @implNote Created eagerly with the sidebar and handed to {@link ReactSidebarControl} as
		 *           its footer content.
		 *           Keyed by {@link UIElement.Config#ID} so that a same-path overlay can
		 *           address an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(FOOTER)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getFooter();

		/**
		 * What the rail shows below its items while it is folded to a narrow strip.
		 *
		 * <p>
		 * A folded rail offers room for an avatar, not for a name beside it, so it shows this in
		 * place of the footer. Left empty, the folded rail has no footer.
		 * </p>
		 *
		 * @implNote Created eagerly with the sidebar and handed to {@link ReactSidebarControl} as
		 *           its collapsed footer content.
		 *           Keyed by {@link UIElement.Config#ID} so that a same-path overlay can
		 *           address an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(FOOTER_COLLAPSED)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getFooterCollapsed();
	}

	/**
	 * Base interface for sidebar item elements.
	 */
	public interface SidebarItemElement {

		/**
		 * Creates a {@link SidebarItem} for use with {@link ReactSidebarControl}.
		 *
		 * @param context
		 *        The context the sidebar is built in.
		 * @param site
		 *        What the sidebar offers the item being built: the context of the item's content,
		 *        and the bindings to the control that will display it.
		 * @return The item, or {@code null} when the item must be omitted (e.g. because access is
		 *         denied for the current user).
		 */
		SidebarItem createSidebarItem(ViewContext context, ItemSite site);

		/**
		 * The content this item holds, keyed by the {@link SidebarItemConfig#getId() id} of the
		 * item displaying it.
		 *
		 * <p>
		 * An item displaying no content of its own holds nothing; an item holding further items
		 * answers what those hold, so that every navigation item of a sidebar - nested in a group
		 * or not - is addressed at the sidebar itself.
		 * </p>
		 *
		 * @return The groups, in configuration order.
		 *
		 * @see UIElement#getChildGroups()
		 */
		default List<ChildGroup> getChildGroups() {
			return List.of();
		}
	}

	/**
	 * What the sidebar offers its items while they are being built.
	 *
	 * <p>
	 * An item is built before the control displaying it exists, so whatever the item has to say to
	 * that control - a badge to keep up to date, the executability of a command to follow - is
	 * registered as a binding and run as soon as the control is there. The content of an item is
	 * built later still, in the context the site derives for it.
	 * </p>
	 */
	public interface ItemSite {

		/**
		 * The context in which the content an item addresses under the given key is built.
		 *
		 * @param context
		 *        The context the sidebar is built in.
		 * @param key
		 *        The key the item addresses its content by, as its {@link ChildGroup} reports it.
		 * @return The context that content is built in.
		 */
		ViewContext contentContext(ViewContext context, String key);

		/**
		 * Registers a binding that is run once the sidebar control exists.
		 *
		 * @param binding
		 *        What connects an item to the control displaying it.
		 */
		void addBinding(Consumer<ReactSidebarControl> binding);

		/**
		 * The access checks of the groups enclosing the items built at this site, outermost first.
		 *
		 * <p>
		 * The content of a navigation item is displayed only as far as each of them allows, see
		 * {@link #guardedBy(UnitAccess)}.
		 * </p>
		 */
		default List<UnitAccess> guards() {
			return Collections.emptyList();
		}

		/**
		 * The site for the items of a group with the given access check.
		 *
		 * @param access
		 *        The access check of the group, {@code null} for a group without one.
		 * @return A site adding the given check to the {@link #guards()} of this site.
		 */
		default ItemSite guardedBy(UnitAccess access) {
			if (access == null) {
				return this;
			}
			ItemSite outer = this;
			List<UnitAccess> guards = new ArrayList<>(guards());
			guards.add(access);
			List<UnitAccess> allGuards = Collections.unmodifiableList(guards);
			return new ItemSite() {
				@Override
				public ViewContext contentContext(ViewContext itemContext, String itemKey) {
					return outer.contentContext(itemContext, itemKey);
				}

				@Override
				public void addBinding(Consumer<ReactSidebarControl> binding) {
					outer.addBinding(binding);
				}

				@Override
				public List<UnitAccess> guards() {
					return allGuards;
				}
			};
		}
	}

	/**
	 * Common configuration base for the entries of a {@link SidebarElement}.
	 *
	 * @implNote {@link #getId()} is the merge key of the items list: a configuration fragment in
	 *           another module positions ({@code config:position}) or overrides an item by
	 *           referencing this id. A {@code <separator>} usually leaves it empty: an entry
	 *           without id is anonymous, appended in order and addressed by no fragment.
	 */
	public interface SidebarItemConfig extends PolymorphicConfiguration<SidebarItemElement> {

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/**
		 * The item identifier, unique in the whole view, see {@link UIElement.Config#getId()}.
		 */
		@Name(ID)
		@GlobalKey
		String getId();
	}

	/**
	 * A navigation item with content.
	 */
	@TagName("nav-item")
	@TypeIcon("css:bi bi-link-45deg")
	public interface NavItemConfig extends SidebarItemConfig, WithAccessControl, WithFinalTitle {

		@Override
		@ClassDefault(NavItemElement.class)
		Class<? extends SidebarItemElement> getImplementationClass();

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getIcon()}. */
		String ICON = "icon";

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/** Configuration name for {@link #getRoute()}. */
		String ROUTE = "route";

		/** Configuration name for {@link #getBadge()}. */
		String BADGE = "badge";

		/** Configuration name for {@link #getHidden()}. */
		String HIDDEN = "hidden";

		/**
		 * The display label.
		 */
		@Name(LABEL)
		ResKey getLabel();

		/**
		 * The CSS icon class (e.g. "bi bi-speedometer2").
		 */
		@Name(ICON)
		String getIcon();

		/**
		 * The route segment for this item.
		 *
		 * <p>
		 * By default (not set), the item's {@link #getId() ID} is used as the route
		 * segment. Set to {@code "none"} to explicitly opt out of routing. Set to a
		 * custom value to use a route segment different from the ID.
		 * Routes are always relative (no leading slash).
		 * </p>
		 */
		@Name(ROUTE)
		String getRoute();

		/**
		 * Channel whose value is displayed as a badge beside the item's label - the number of
		 * things waiting in the page the item leads to, say.
		 *
		 * <p>
		 * The value is named as the model names it, through the {@link MetaLabelProvider}; no value
		 * and an empty text show no badge at all. The badge follows both a new value on the channel
		 * and a change of the object that value points to, so a count computed from an edited object
		 * is up to date without the channel being written anew.
		 * </p>
		 */
		@Name(BADGE)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getBadge();

		/**
		 * Whether the item is withheld from the sidebar.
		 *
		 * <p>
		 * A hidden item is not displayed, but stays reachable by its route: the page a URL leads to
		 * directly, which has no place of its own in the navigation.
		 * </p>
		 */
		@Name(HIDDEN)
		@BooleanDefault(false)
		boolean getHidden();

		/**
		 * The content elements shown when this item is selected.
		 *
		 * @implNote Keyed by {@link UIElement.Config#ID} so that a same-path overlay can address
		 *           an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(CHILDREN)
		@DefaultContainer
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getChildren();
	}

	/**
	 * A separator line between sidebar items.
	 */
	@TagName("separator")
	@TypeIcon("css:bi bi-dash-lg")
	public interface SeparatorConfig extends SidebarItemConfig {

		@Override
		@ClassDefault(SeparatorElement.class)
		Class<? extends SidebarItemElement> getImplementationClass();
	}

	/**
	 * Implementation of a navigation sidebar item.
	 */
	public static class NavItemElement implements SidebarItemElement {

		private final String _id;

		private final ResKey _label;

		private final String _icon;

		private final String _route;

		private final ChannelRef _badgeRef;

		private final boolean _hidden;

		private final boolean _finalTitle;

		private final UnitAccess _access;

		private final List<UIElement> _children;

		/**
		 * Creates a {@link NavItemElement}.
		 */
		@CalledByReflection
		public NavItemElement(InstantiationContext context, NavItemConfig config) {
			_id = config.getId();
			_label = config.getLabel();
			_icon = config.getIcon();
			_route = config.getRoute();
			_badgeRef = config.getBadge();
			_hidden = config.getHidden();
			_finalTitle = config.getFinalTitle();
			_access = UnitAccess.create(context, config.getAccessControl());
			_children = config.getChildren().stream()
				.map(context::getInstance)
				.collect(Collectors.toList());
		}

		@Override
		public List<ChildGroup> getChildGroups() {
			return List.of(ChildGroup.keyed(_id, _children));
		}

		@Override
		public SidebarItem createSidebarItem(ViewContext context, ItemSite site) {
			if (!AccessChecks.isOffered(_access)) {
				// Access denied for the current user: omit the navigation item entirely.
				return null;
			}
			ViewContext itemContext = site.contentContext(context, _id);
			String label = Resources.getInstance().getString(_label);
			// The item lies within the scope enclosing the sidebar, so what a form of the item
			// holds unsaved is held unsaved there as well.
			DirtyChannel dirtyChannel = new DirtyChannel(context.getDirtyChannel());
			List<UnitAccess> guards = new ArrayList<>(site.guards());
			if (_access != null) {
				guards.add(_access);
			}
			NavigationItem item = new NavigationItem(_id, label, _icon,
				() -> createContent(_children, itemContext, dirtyChannel, guards), dirtyChannel);
			String effectiveRoute = resolveRoute(_route, _id);
			if (effectiveRoute != null) {
				item.withRoute(effectiveRoute);
				item.withFinalTitle(_finalTitle);
			}
			item.withHidden(_hidden);
			withholdWhileDenied(item, _access, context, site);
			if (_badgeRef != null) {
				bindBadge(context, site, item);
			}
			return item;
		}

		/**
		 * Displays the value of the configured badge channel on the given item, and keeps it up to
		 * date for as long as the sidebar is on screen.
		 */
		private void bindBadge(ViewContext context, ItemSite site, NavigationItem item) {
			ViewChannel channel = context.resolveChannel(_badgeRef);
			item.setBadge(badge(channel.get()));

			site.addBinding(sidebar -> {
				Runnable update = () -> sidebar.updateBadge(_id, badge(channel.get()));

				ChannelListener listener = (sender, oldValue, newValue) -> update.run();
				Registration registration = channel.addListener(listener);
				sidebar.addCleanupAction(registration::dispose);

				ChannelObjectObserver observer = new ChannelObjectObserver(List.of(channel), Set.of(), update);
				sidebar.addAttachListener(() -> observer.attach(context.getModelScope()));
				sidebar.addDetachListener(observer::detach);
			});
		}

		/**
		 * The badge text naming the given value, {@code null} for a value that is not worth a badge.
		 */
		private static String badge(Object value) {
			String text = ValueLabel.label(value);
			return text == null || text.isEmpty() ? null : text;
		}
	}

	/**
	 * Leaves the entry of the given item out of the sidebar while its access check fails on its
	 * object, if the item is {@link com.top_logic.layout.view.security.DeniedUnitDisplay#HIDE
	 * hidden} then.
	 *
	 * <p>
	 * Used by navigation items and groups alike. An open navigation item whose entry is left out
	 * stays open: its content shows the "no access" placeholder.
	 * </p>
	 *
	 * @param item
	 *        The item built.
	 * @param access
	 *        The access check of the item, {@code null} for an item without one.
	 * @param context
	 *        The context the sidebar is built in.
	 * @param site
	 *        The site the item is built at, binding the item to the sidebar control.
	 */
	public static void withholdWhileDenied(SidebarItem item, UnitAccess access, ViewContext context,
			ItemSite site) {
		UnitHiding hiding = AccessChecks.hiding(access, context);
		if (hiding == null) {
			return;
		}
		item.setWithheld(hiding.isHidden());
		site.addBinding(
			sidebar -> hiding.bind(sidebar, hidden -> sidebar.setItemWithheld(item.getId(), hidden.booleanValue())));
	}

	/** Value for the {@code route} attribute that explicitly disables routing. */
	public static final String NO_ROUTE = "none";

	/**
	 * Resolves the effective route segment for a navigation element.
	 *
	 * <p>
	 * Convention: empty string (not set in XML) means use the element's ID as route.
	 * {@code "none"} means explicitly not routed. Any other value is used as-is
	 * (custom route different from ID). Leading slashes are stripped.
	 * </p>
	 *
	 * @param configuredRoute
	 *        The configured route value (empty string if not set).
	 * @param id
	 *        The element's ID (used as fallback).
	 * @return The effective route segment, or {@code null} if not routed.
	 */
	public static String resolveRoute(String configuredRoute, String id) {
		if (configuredRoute == null || configuredRoute.isEmpty()) {
			// Not set -> use ID as route.
			return id;
		}
		if (NO_ROUTE.equals(configuredRoute)) {
			// Explicitly opted out of routing.
			return null;
		}
		// Custom route, strip leading slash if present.
		return configuredRoute.startsWith("/") ? configuredRoute.substring(1) : configuredRoute;
	}

	/**
	 * Implementation of a separator sidebar item.
	 */
	public static class SeparatorElement implements SidebarItemElement {

		private static int _counter;

		/**
		 * Creates a {@link SeparatorElement}.
		 */
		@CalledByReflection
		public SeparatorElement(InstantiationContext context, SeparatorConfig config) {
			// No configuration needed.
		}

		@Override
		public SidebarItem createSidebarItem(ViewContext context, ItemSite site) {
			return new SeparatorItem("sep" + (_counter++));
		}
	}

	/**
	 * Segment naming the {@link Config#getHeader() header} of the rail in the context its content is
	 * built in.
	 */
	private static final String HEADER_SEGMENT = "sidebar-header";

	/**
	 * Segment naming the {@link Config#getHeaderCollapsed() collapsed header} of the rail in the
	 * context its content is built in.
	 */
	private static final String HEADER_COLLAPSED_SEGMENT = "sidebar-header-collapsed";

	/**
	 * Segment naming the {@link Config#getFooter() footer} of the rail in the context its content is
	 * built in.
	 */
	private static final String FOOTER_SEGMENT = "sidebar-footer";

	/**
	 * Segment naming the {@link Config#getFooterCollapsed() collapsed footer} of the rail in the
	 * context its content is built in.
	 */
	private static final String FOOTER_COLLAPSED_SEGMENT = "sidebar-footer-collapsed";

	private final List<SidebarItemElement> _items;

	private final String _activeItem;

	private final boolean _collapsed;

	private final String _drawerOpenSlotName;

	private final String _cssClass;

	private final List<UIElement> _header;

	private final List<UIElement> _headerCollapsed;

	private final List<UIElement> _footer;

	private final List<UIElement> _footerCollapsed;

	/**
	 * Creates a new {@link SidebarElement} from configuration.
	 */
	@CalledByReflection
	public SidebarElement(InstantiationContext context, Config config) {
		_items = new ArrayList<>();
		for (SidebarItemConfig itemConfig : config.getItems()) {
			_items.add(context.getInstance(itemConfig));
		}
		_activeItem = config.getActiveItem();
		_collapsed = config.getCollapsed();
		_drawerOpenSlotName = config.getDrawerOpenSlotName();
		_cssClass = config.getCssClass();
		_header = createElements(context, config.getHeader());
		_headerCollapsed = createElements(context, config.getHeaderCollapsed());
		_footer = createElements(context, config.getFooter());
		_footerCollapsed = createElements(context, config.getFooterCollapsed());
	}

	private static List<UIElement> createElements(InstantiationContext context,
			List<UIElement.Config> configs) {
		List<UIElement> result = new ArrayList<>(configs.size());
		for (PolymorphicConfiguration<? extends UIElement> elementConfig : configs) {
			result.add(context.getInstance(elementConfig));
		}
		return result;
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		List<ChildGroup> result = new ArrayList<>();
		// The chrome of the rail is shown for as long as the sidebar is, so it is addressed at the
		// sidebar without a key: whoever reaches a view written there reaches it by opening the
		// sidebar and nothing else.
		addChrome(result, _header);
		addChrome(result, _headerCollapsed);
		for (SidebarItemElement item : _items) {
			result.addAll(item.getChildGroups());
		}
		addChrome(result, _footer);
		addChrome(result, _footerCollapsed);
		return result;
	}

	private static void addChrome(List<ChildGroup> result, List<UIElement> elements) {
		if (!elements.isEmpty()) {
			result.add(ChildGroup.elements(elements));
		}
	}

	/**
	 * Builds the items of a sidebar, dropping the ones that are not offered to the current user.
	 *
	 * <p>
	 * Used by the sidebar for its own items and by a group for the items it contains, so that an
	 * item behaves the same wherever it is written.
	 * </p>
	 *
	 * @param itemElements
	 *        The elements to build, in configuration order.
	 * @param context
	 *        The context the sidebar is built in.
	 * @param site
	 *        What the sidebar offers its items, passed on unchanged by a group.
	 * @return The items to display.
	 */
	public static List<SidebarItem> createItems(List<SidebarItemElement> itemElements, ViewContext context,
			ItemSite site) {
		List<SidebarItem> result = new ArrayList<>();
		for (SidebarItemElement itemElement : itemElements) {
			SidebarItem item = itemElement.createSidebarItem(context, site);
			if (item != null) {
				result.add(item);
			}
		}
		return result;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		String key = resolveKey(context, "sidebar");

		boolean collapsed = PersonalizingExpandable.loadCollapsed(key + ".collapsed", _collapsed);
		Map<String, Boolean> groupStates = loadGroupStates(key);

		RevealPath here = RevealPath.of(context);
		List<Consumer<ReactSidebarControl>> bindings = new ArrayList<>();
		ItemSite site = new ItemSite() {
			@Override
			public ViewContext contentContext(ViewContext itemContext, String itemKey) {
				// The content of an item is created only when the item is first selected, so the
				// item's context must already say where that content will sit. Each item is a
				// position of its own for slots, apart from the other items and the chrome of the rail.
				return itemContext.withScope(RevealPath.class, here.append(SidebarElement.this, itemKey))
					.withChildSlotPath(itemKey);
			}

			@Override
			public void addBinding(Consumer<ReactSidebarControl> binding) {
				bindings.add(binding);
			}
		};
		List<SidebarItem> sidebarItems = createItems(_items, context, site);

		String activeItem = _activeItem != null && !_activeItem.isEmpty() ? _activeItem : null;
		ReactSidebarControl sidebar = new ReactSidebarControl(context,
			sidebarItems, activeItem,
			collapsed, groupStates,
			c -> PersonalizingExpandable.saveCollapsed(key + ".collapsed", c, _collapsed),
			(gid, exp) -> saveGroupState(key, gid, exp),
			createChrome(_header, context, HEADER_SEGMENT),
			createChrome(_headerCollapsed, context, HEADER_COLLAPSED_SEGMENT),
			createChrome(_footer, context, FOOTER_SEGMENT),
			createChrome(_footerCollapsed, context, FOOTER_COLLAPSED_SEGMENT));

		for (Consumer<ReactSidebarControl> binding : bindings) {
			binding.accept(sidebar);
		}

		// If a slot name is configured, contribute a hamburger button that toggles the drawer while
		// the app shell is compact. The placeholder must be declared elsewhere in the view tree
		// (typically the app bar's leading area). Empty/missing -> no contribution, no drawer entry
		// point while the app shell is compact.
		if (_drawerOpenSlotName != null && !_drawerOpenSlotName.isEmpty()) {
			DrawerToggleControl toggleButton = new DrawerToggleControl(context, sidebar);
			SlotContentControl drawerToggleSlot = new SlotContentControl(context, _drawerOpenSlotName,
				context.getSlotPath(), context.getSlotRegistry(), List.of(toggleButton));
			sidebar.setDrawerToggleContribution(drawerToggleSlot);
		}

		sidebar.setCssClass(_cssClass);

		RevealRegistry registry = context.getRevealRegistry();
		if (registry != null) {
			sidebar.addCleanupAction(registry.registerContainer(this, here, sidebar));
		}

		return sidebar;
	}

	private static String resolveKey(ViewContext context, String defaultSegment) {
		return context.getPersonalizationKey() + "." + defaultSegment;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Boolean> loadGroupStates(String key) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return null;
		}
		Object value = pc.getJSONValue(key + ".groups");
		if (value instanceof Map) {
			Map<String, Object> raw = (Map<String, Object>) value;
			Map<String, Boolean> result = new HashMap<>();
			for (Map.Entry<String, Object> entry : raw.entrySet()) {
				if (entry.getValue() instanceof Boolean) {
					result.put(entry.getKey(), (Boolean) entry.getValue());
				}
			}
			return result;
		}
		return null;
	}

	private static void saveGroupState(String key, String groupId, boolean expanded) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}
		Map<String, Boolean> states = loadGroupStates(key);
		if (states == null) {
			states = new HashMap<>();
		}
		states.put(groupId, Boolean.valueOf(expanded));
		if (states.isEmpty()) {
			pc.setJSONValue(key + ".groups", null);
		} else {
			pc.setJSONValue(key + ".groups", states);
		}
	}

	/**
	 * Builds one of the contents standing outside the item list, or {@code null} for a rail that is
	 * written without it.
	 *
	 * @param elements
	 *        The elements written for this part of the rail.
	 * @param context
	 *        The context the sidebar is built in.
	 * @param segment
	 *        Names this part of the rail in the context its content is built in, both in the
	 *        personalization key and in the slot path.
	 * @return The control displaying that content, or {@code null} for nothing to display.
	 */
	private static ReactControl createChrome(List<UIElement> elements, ViewContext context, String segment) {
		if (elements.isEmpty()) {
			return null;
		}
		return ContentControls.toControl(elements, context.childContext(segment).withChildSlotPath(segment));
	}

	/**
	 * Creates the content of a navigation item.
	 *
	 * @param guards
	 *        The access checks of the enclosing groups and of the item itself, outermost first. The
	 *        content is displayed as far as each of them allows, and sees the object of the
	 *        innermost one as its {@link com.top_logic.layout.view.security.SecuredObject}.
	 */
	private static ReactControl createContent(List<UIElement> elements, ViewContext context,
			DirtyChannel dirtyChannel, List<UnitAccess> guards) {
		ViewContext itemContext = context.childContext("sidebar-item");
		itemContext.setDirtyChannel(dirtyChannel);

		return guarded(elements, itemContext, guards, 0);
	}

	private static ReactControl guarded(List<UIElement> elements, ViewContext context, List<UnitAccess> guards,
			int index) {
		if (index == guards.size()) {
			return ContentControls.toControl(elements, context);
		}
		UnitAccess access = guards.get(index);
		return AccessChecks.guard(access, AccessChecks.contentContext(access, context),
			inner -> guarded(elements, inner, guards, index + 1));
	}
}
