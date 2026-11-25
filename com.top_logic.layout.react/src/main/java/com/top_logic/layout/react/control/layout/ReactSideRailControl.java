/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.control.common.BadgeDisplay;
import com.top_logic.layout.react.control.overlay.ReactDrawerControl;
import com.top_logic.layout.react.control.overlay.ReactDrawerControl.Anchor;
import com.top_logic.layout.react.control.overlay.ReactDrawerControl.Position;
import com.top_logic.layout.react.control.overlay.ReactDrawerControl.Size;
import com.top_logic.layout.react.state.SideRailState;

/**
 * A collapsible column beside another element, with sections, the component {@code TLSideRail}.
 *
 * <p>
 * The rail shows a strip with an icon per section and, while it is open, a column with the sections
 * stacked, each with a header that expands and collapses it. A click on a strip icon opens the rail
 * with that section expanded. Where the area of the rail is too narrow for a column, the client
 * reports it ({@link #SET_DRAWER_COMMAND}), the rail keeps only its strip, and opening it opens a
 * modal {@link ReactDrawerControl drawer} with the sections over the neighbour.
 * </p>
 *
 * <p>
 * The rail keeps two open states: the user's choice for the column, and whether the drawer is open.
 * While the rail is a drawer, the column choice is neither read nor changed; it is what the rail
 * shows again when its area is wide enough again. {@link #isOpen()} is the state the user sees: the
 * column's while it is a column, the drawer's while it is a drawer.
 * </p>
 *
 * <p>
 * The badge of a section counts as seen when the section is shown: the rail is open and
 * {@link #setShown(boolean) shown}, and the section expanded and not hidden. The rail reports the sum
 * of what the sections count beyond what was seen of them to {@link Listener#unseenChanged(int)}.
 * </p>
 *
 * <p>
 * In a split panel, the rail is a {@link CollapsibleChild}: closed or a drawer, its pane shrinks to
 * the strip; open, the column fills the pane, and an expanded section that asks for a width of its
 * own {@link CollapseContainer#requestSize(float, DisplayUnit) requests} it from the split panel for
 * as long as it is shown. The rail's own open state takes precedence over a collapse state the split
 * panel restored. The rail closes even as the last open pane of a split panel, without asking
 * {@link CollapseContainer#canCollapse()}: unlike a minimized panel, its strip stays usable and opens
 * it again. Outside a split panel the column has a width of its own, which the user drags.
 * </p>
 */
public class ReactSideRailControl extends ReactControl implements CollapsibleChild {

	private static final String REACT_MODULE = "TLSideRail";

	/**
	 * Command of a click on a strip icon.
	 *
	 * @see #handleShowSection(Map)
	 */
	public static final String SHOW_SECTION_COMMAND = "showSection";

	/**
	 * Command of a click on the header of a section in the column.
	 *
	 * @see ReactSideRailSectionsControl
	 */
	public static final String TOGGLE_SECTION_COMMAND = "toggleSection";

	/**
	 * Command by which the client reports whether the rail is a drawer.
	 *
	 * @see #handleSetDrawer(Map)
	 */
	public static final String SET_DRAWER_COMMAND = "setDrawer";

	/**
	 * Command by which the client reports the width the user dragged the column to.
	 *
	 * @see #handleSetWidth(Map)
	 */
	public static final String SET_WIDTH_COMMAND = "setWidth";

	/** Argument of {@link #SHOW_SECTION_COMMAND} and {@link #TOGGLE_SECTION_COMMAND}: the section name. */
	public static final String NAME_ARG = "name";

	/** Argument of {@link #SET_DRAWER_COMMAND}: whether the rail is a drawer. */
	public static final String DRAWER_ARG = "drawer";

	/** Argument of {@link #SET_WIDTH_COMMAND}: the width in pixels. */
	public static final String WIDTH_ARG = "width";

	/**
	 * The size in pixels the rail reports to its {@link CollapseContainer} when it shows only its
	 * strip.
	 */
	public static final float STRIP_SIZE = 48f;

	/**
	 * The side of its neighbour a rail stands on.
	 */
	public enum Side implements ExternallyNamed {
		/** At the start; the drawer slides in from the start. */
		LEFT(SideRailState.Side.LEFT, Position.LEFT),

		/** At the end; the drawer slides in from the end. */
		RIGHT(SideRailState.Side.RIGHT, Position.RIGHT);

		private final SideRailState.Side _state;

		private final Position _drawer;

		Side(SideRailState.Side state, Position drawer) {
			_state = state;
			_drawer = drawer;
		}

		@Override
		public String getExternalName() {
			return _state.protocolName();
		}
	}

	/**
	 * A section of a rail.
	 *
	 * @param name
	 *        The name of the section, unique within the rail.
	 * @param label
	 *        The label in the header and on the strip icon.
	 * @param icon
	 *        The encoded theme image of the strip icon, {@code null} for a generic glyph.
	 * @param width
	 *        The width in pixels the section asks for while it is shown, {@code 0} for none.
	 * @param headless
	 *        Whether the section has no header and is always expanded, for content that brings a
	 *        title bar of its own.
	 * @param content
	 *        The content of the section.
	 */
	public record Section(String name, String label, String icon, int width, boolean headless,
			ReactControl content) {
		// Sole record.
	}

	/**
	 * Receives what changes in a rail.
	 */
	public interface Listener {

		/**
		 * The state the user sees changed: the rail opened or closed, as a column or as a drawer.
		 */
		default void openChanged(boolean open) {
			// Nothing to do.
		}

		/**
		 * The user's choice for the column changed; it is not changed while the rail is a drawer.
		 */
		default void columnOpenChanged(boolean open) {
			// Nothing to do.
		}

		/**
		 * The user expanded or collapsed a section.
		 */
		default void sectionExpandedChanged(String name, boolean expanded) {
			// Nothing to do.
		}

		/**
		 * The user dragged the column to the given width in pixels.
		 */
		default void widthChanged(int width) {
			// Nothing to do.
		}

		/**
		 * The sum of what the sections count beyond what was seen of them changed.
		 */
		default void unseenChanged(int unseen) {
			// Nothing to do.
		}
	}

	private static final Listener NO_LISTENER = new Listener() {
		// Nothing to do.
	};

	private final Map<String, Section> _sections = new LinkedHashMap<>();

	private final Set<String> _expanded = new HashSet<>();

	private final Set<String> _hidden = new HashSet<>();

	private final Map<String, BadgeDisplay> _badges = new HashMap<>();

	/** What was seen of the badge of each section, as the count it showed. */
	private final Map<String, Integer> _seen = new HashMap<>();

	private final ReactSideRailSectionsControl _body;

	private final ReactDrawerControl _drawer;

	private Listener _listener = NO_LISTENER;

	private CollapseContainer _container;

	private boolean _columnOpen;

	private boolean _drawerMode;

	private int _width;

	private int _unseen;

	private boolean _shown = true;

	/**
	 * Creates a {@link ReactSideRailControl}.
	 *
	 * @param context
	 *        The context of the control.
	 * @param side
	 *        The side of its neighbour the rail stands on.
	 * @param strip
	 *        Whether the strip of section icons is shown.
	 * @param drawerBelow
	 *        The width in rem of the area of the rail below which it is a drawer, {@code 0} for
	 *        never.
	 * @param width
	 *        The width of the column in pixels outside a split panel.
	 * @param open
	 *        Whether the column is open.
	 * @param sections
	 *        The sections, top to bottom.
	 * @param expanded
	 *        The names of the expanded sections.
	 */
	public ReactSideRailControl(ReactContext context, Side side, boolean strip, int drawerBelow, int width,
			boolean open, List<Section> sections, Collection<String> expanded) {
		super(context, null, REACT_MODULE);
		for (Section section : sections) {
			if (_sections.put(section.name(), section) != null) {
				throw new IllegalArgumentException("Duplicate section name: " + section.name());
			}
			if (section.headless()) {
				_expanded.add(section.name());
			}
		}
		for (String name : expanded) {
			if (_sections.containsKey(name)) {
				_expanded.add(name);
			}
		}
		_columnOpen = open;
		_width = width;

		_body = new ReactSideRailSectionsControl(context, this);
		_drawer = new ReactDrawerControl(context, null, side._drawer, Size.NARROW, Anchor.CONTAINER,
			() -> setOpen(false));
		_drawer.setModal(true);
		_drawer.setWidth(width);
		_drawer.setChild(_body);

		putState(SideRailState.SIDE__PROP, side._state.protocolName());
		putState(SideRailState.STRIP__PROP, Boolean.valueOf(strip));
		putState(SideRailState.DRAWER_BELOW__PROP, Integer.valueOf(Math.max(0, drawerBelow)));
		putState(SideRailState.BODY__PROP, _body);
		putState(SideRailState.DRAWER__PROP, _drawer);
		putState(SideRailState.WIDTH__PROP, Integer.valueOf(width));
		putState(SideRailState.RESIZABLE__PROP, Boolean.TRUE);
		putState(SideRailState.OPEN__PROP, Boolean.valueOf(open));
		writeSections();
		updateSeen();
	}

	/**
	 * Sets the receiver of what changes in this rail.
	 */
	public void setListener(Listener listener) {
		_listener = listener == null ? NO_LISTENER : listener;
	}

	/**
	 * The column with the sections, the {@link SideRailState#BODY__PROP body} of the rail.
	 */
	ReactSideRailSectionsControl getBody() {
		return _body;
	}

	/**
	 * The drawer showing the {@link #getBody() column} while the rail is a drawer.
	 */
	ReactDrawerControl getDrawer() {
		return _drawer;
	}

	/**
	 * Whether the rail is open, as the user sees it: its column, or its drawer while it is one.
	 */
	public boolean isOpen() {
		return _drawerMode ? _drawer.isOpen() : _columnOpen;
	}

	/**
	 * Whether the rail is a drawer, as the client last reported.
	 */
	public boolean isDrawer() {
		return _drawerMode;
	}

	/**
	 * Whether the section with the given name is expanded.
	 */
	public boolean isExpanded(String name) {
		return _expanded.contains(name);
	}

	/**
	 * The sum of what the sections count beyond what was seen of them.
	 */
	public int getUnseen() {
		return _unseen;
	}

	/**
	 * Opens or closes the rail: the column, or the drawer while the rail is one.
	 *
	 * <p>
	 * A change of the column is the user's choice and reported to
	 * {@link Listener#columnOpenChanged(boolean)}; every change to {@link Listener#openChanged(boolean)}.
	 * </p>
	 */
	public void setOpen(boolean open) {
		changeOpen(open, true);
	}

	/**
	 * Shows the section with the given name: opens the rail - its column, or its drawer while it is
	 * one - and expands the section.
	 *
	 * <p>
	 * Not a choice of the user: neither the open column nor the expanded section is reported as
	 * one. Used for a section whose content appeared and asks to be seen, e.g. something an agent
	 * presented.
	 * </p>
	 */
	public void reveal(String name) {
		checkSection(name);
		if (_expanded.add(name)) {
			writeSections();
			reportLayout();
			updateSeen();
		}
		changeOpen(true, false);
	}

	/**
	 * Closes the drawer of a rail that is a drawer, e.g. because the user picked something in it.
	 *
	 * <p>
	 * The column of a rail that is no drawer is left as it is, and nothing is remembered.
	 * </p>
	 */
	public void dismissDrawer() {
		if (_drawerMode) {
			changeOpen(false, false);
		}
	}

	private void changeOpen(boolean open, boolean choice) {
		if (isOpen() == open) {
			return;
		}
		if (_drawerMode) {
			if (open) {
				_drawer.open();
			} else {
				_drawer.close();
			}
		} else {
			_columnOpen = open;
		}
		putState(SideRailState.OPEN__PROP, Boolean.valueOf(open));
		reportLayout();
		updateSeen();
		if (choice && !_drawerMode) {
			_listener.columnOpenChanged(open);
		}
		_listener.openChanged(open);
	}

	/**
	 * Expands or collapses the section with the given name.
	 */
	public void setExpanded(String name, boolean expanded) {
		checkSection(name);
		if (!expanded && _sections.get(name).headless()) {
			// Without a header, there is nothing to expand it again.
			return;
		}
		boolean changed = expanded ? _expanded.add(name) : _expanded.remove(name);
		if (!changed) {
			return;
		}
		writeSections();
		reportLayout();
		updateSeen();
		_listener.sectionExpandedChanged(name, expanded);
	}

	/**
	 * Leaves the section with the given name out of the rail, or takes it in again.
	 */
	public void setHidden(String name, boolean hidden) {
		checkSection(name);
		boolean changed = hidden ? _hidden.add(name) : _hidden.remove(name);
		if (!changed) {
			return;
		}
		writeSections();
		reportLayout();
		updateSeen();
	}

	/**
	 * Sets the badge of the section with the given name, {@code null} for none.
	 */
	public void setBadge(String name, BadgeDisplay badge) {
		checkSection(name);
		if (Objects.equals(_badges.get(name), badge)) {
			return;
		}
		if (badge == null) {
			_badges.remove(name);
		} else {
			_badges.put(name, badge);
		}
		writeSections();
		updateSeen();
	}

	/**
	 * Handles a click on a strip icon: opens the rail with the section expanded, or closes it when the
	 * section is shown already.
	 */
	@ReactCommandHandler(value = SHOW_SECTION_COMMAND, params = @ReactParam(name = NAME_ARG, type = "string",
		required = true, description = "Name of the section whose strip icon was clicked."))
	void handleShowSection(Map<String, Object> arguments) {
		String name = (String) arguments.get(NAME_ARG);
		if (!_sections.containsKey(name)) {
			return;
		}
		if (isDisplayed(name)) {
			setOpen(false);
			return;
		}
		setExpanded(name, true);
		setOpen(true);
	}

	/**
	 * Handles a click on the header of a section, sent by the {@link ReactSideRailSectionsControl}.
	 */
	void handleToggleSection(String name) {
		if (!_sections.containsKey(name)) {
			return;
		}
		setExpanded(name, !_expanded.contains(name));
	}

	/**
	 * Handles the report of the client whether the rail is a drawer.
	 *
	 * <p>
	 * Entering the drawer state leaves the column choice alone and starts with a closed drawer;
	 * leaving it gives the rail the column choice back.
	 * </p>
	 */
	@ReactCommandHandler(value = SET_DRAWER_COMMAND, technical = true, params = @ReactParam(name = DRAWER_ARG,
		type = "boolean", required = true, description = "Whether the area of the rail is too narrow for its column."))
	void handleSetDrawer(Map<String, Object> arguments) {
		setDrawer(Boolean.TRUE.equals(arguments.get(DRAWER_ARG)));
	}

	/**
	 * Switches between column and drawer, see {@link #handleSetDrawer(Map)}.
	 */
	public void setDrawer(boolean drawer) {
		if (_drawerMode == drawer) {
			return;
		}
		boolean wasOpen = isOpen();
		_drawer.close();
		_drawerMode = drawer;
		boolean open = isOpen();
		putState(SideRailState.OPEN__PROP, Boolean.valueOf(open));
		reportLayout();
		updateSeen();
		if (open != wasOpen) {
			_listener.openChanged(open);
		}
	}

	/**
	 * Handles the width the user dragged the column to.
	 */
	@ReactCommandHandler(value = SET_WIDTH_COMMAND, technical = true, params = @ReactParam(name = WIDTH_ARG,
		type = "number", required = true, description = "The width of the column in pixels."))
	void handleSetWidth(Map<String, Object> arguments) {
		Object width = arguments.get(WIDTH_ARG);
		if (!(width instanceof Number)) {
			return;
		}
		int newWidth = Math.max(0, ((Number) width).intValue());
		if (newWidth == _width) {
			return;
		}
		_width = newWidth;
		_drawer.setWidth(newWidth);
		putState(SideRailState.WIDTH__PROP, Integer.valueOf(newWidth));
		_listener.widthChanged(newWidth);
	}

	/**
	 * Sets the container of this rail and reports its own state to it: the rail decides whether it
	 * is open, not a collapse state the container restored.
	 */
	@Override
	public void setCollapseContainer(CollapseContainer container) {
		_container = container;
		putState(SideRailState.RESIZABLE__PROP, Boolean.valueOf(container == null));
		reportLayout();
	}

	/**
	 * Tells the rail whether it is on screen, e.g. {@code false} while the frame around it is
	 * closed.
	 *
	 * <p>
	 * While the rail is not on screen, nothing in it counts as seen; whether it is open is not
	 * changed. Becoming shown again marks what it shows as seen.
	 * </p>
	 */
	public void setShown(boolean shown) {
		if (_shown == shown) {
			return;
		}
		_shown = shown;
		updateSeen();
	}

	/**
	 * Whether the section with the given name is shown: the rail is on screen and open, the section
	 * expanded and not hidden.
	 */
	public boolean isShown(String name) {
		return _shown && isDisplayed(name);
	}

	/**
	 * Whether the rail displays the section with the given name: the rail is open, the section
	 * expanded and not hidden.
	 */
	private boolean isDisplayed(String name) {
		return isOpen() && _expanded.contains(name) && !_hidden.contains(name);
	}

	/**
	 * The width an expanded section asks for while it is shown: the largest one among the shown
	 * sections, {@code 0} for none.
	 */
	public int getSectionWidth() {
		int result = 0;
		for (Section section : _sections.values()) {
			if (isDisplayed(section.name())) {
				result = Math.max(result, section.width());
			}
		}
		return result;
	}

	/**
	 * The sections in the form of their state, for the strip without content, for the column with.
	 */
	List<Map<String, Object>> sectionState(boolean withContent) {
		List<Map<String, Object>> result = new ArrayList<>(_sections.size());
		for (Section section : _sections.values()) {
			String name = section.name();
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put(SideRailState.Section.NAME__PROP, name);
			entry.put(SideRailState.Section.LABEL__PROP, section.label());
			if (section.icon() != null) {
				entry.put(SideRailState.Section.ICON__PROP, section.icon());
			}
			entry.put(SideRailState.Section.EXPANDED__PROP, Boolean.valueOf(_expanded.contains(name)));
			entry.put(SideRailState.Section.HIDDEN__PROP, Boolean.valueOf(_hidden.contains(name)));
			if (section.headless()) {
				entry.put(SideRailState.Section.HEADLESS__PROP, Boolean.TRUE);
			}
			BadgeDisplay badge = _badges.get(name);
			if (badge != null) {
				entry.put(SideRailState.Section.BADGE__PROP, badge.toState());
			}
			if (withContent) {
				entry.put(SideRailState.Section.CONTENT__PROP, section.content());
			}
			result.add(entry);
		}
		return result;
	}

	private void writeSections() {
		putState(SideRailState.SECTIONS__PROP, sectionState(false));
		_body.writeSections();
		_drawer.setLabel(drawerLabel());
	}

	/**
	 * The name of the drawer for assistive technology: the labels of the sections it shows.
	 */
	private String drawerLabel() {
		List<String> labels = new ArrayList<>();
		for (Section section : _sections.values()) {
			String name = section.name();
			if (_expanded.contains(name) && !_hidden.contains(name)) {
				labels.add(section.label());
			}
		}
		return labels.isEmpty() ? null : String.join(", ", labels);
	}

	/**
	 * Tells the client and the container how much room the rail takes.
	 */
	private void reportLayout() {
		int sectionWidth = getSectionWidth();
		putState(SideRailState.SECTION_WIDTH__PROP, Integer.valueOf(sectionWidth));
		if (_container == null) {
			return;
		}
		boolean collapsed = _drawerMode || !_columnOpen;
		if (collapsed) {
			_container.collapsed(STRIP_SIZE);
		} else {
			_container.expanded();
		}
		if (!collapsed && sectionWidth > 0) {
			_container.requestSize(sectionWidth, DisplayUnit.PIXEL);
		} else {
			_container.releaseSize();
		}
	}

	/**
	 * Marks the badges of the shown sections as seen and reports a new sum of what is unseen.
	 */
	private void updateSeen() {
		int unseen = 0;
		for (Section section : _sections.values()) {
			String name = section.name();
			int count = count(_badges.get(name));
			if (isShown(name)) {
				_seen.put(name, Integer.valueOf(count));
				continue;
			}
			int seen = _seen.getOrDefault(name, Integer.valueOf(0)).intValue();
			if (count < seen) {
				// The count went down: what is left was seen.
				_seen.put(name, Integer.valueOf(count));
				continue;
			}
			unseen += count - seen;
		}
		if (unseen != _unseen) {
			_unseen = unseen;
			_listener.unseenChanged(unseen);
		}
	}

	/**
	 * What a badge counts: the number it shows, one more than the largest count shown with digits
	 * for an overflow, {@code 0} for a dot or a text.
	 */
	static int count(BadgeDisplay badge) {
		if (badge == null || badge.dot() || badge.content() == null) {
			return 0;
		}
		String content = badge.content();
		if (content.endsWith("+")) {
			return parse(content.substring(0, content.length() - 1)) + 1;
		}
		return parse(content);
	}

	private static int parse(String digits) {
		if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
			return 0;
		}
		try {
			return Integer.parseInt(digits);
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	private void checkSection(String name) {
		if (!_sections.containsKey(name)) {
			throw new IllegalArgumentException("No section of this rail: " + name);
		}
	}
}
