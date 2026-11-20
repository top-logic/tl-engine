/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.ai.chat.view.ChatRailElement.Config;
import com.top_logic.ai.chat.view.ChatRailElement.Section;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.common.BadgeDisplay;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.element.BadgeBinding;
import com.top_logic.layout.view.element.ContentControls;
import com.top_logic.util.Resources;

/**
 * React control for {@code <chat-rail/>}.
 *
 * <p>
 * Builds one child control per configured section and puts them into its own state, which is how a
 * control reaches the client: the browser renders each of them in its place inside the rail. Every
 * section is built once and stays built - a collapsed section is hidden rather than dropped, so it
 * keeps reporting its badge while it is out of sight.
 * </p>
 *
 * <p>
 * The pane is a {@link PresentationPane} over the {@code input} channel: the steps of its history
 * are child controls too, all mounted and the top one shown, so going back shows a step exactly as
 * it was left.
 * </p>
 *
 * <p>
 * The state this control owns is the frame: which sections there are, what they are called, their
 * icons, the badges they currently show and the pane's history. Everything inside a section belongs to the
 * element configured there.
 * </p>
 */
public class ChatRailControl extends ChatControl {

	/**
	 * State key for the badges of the sections: per section name the state of its
	 * {@link BadgeDisplay}, a section showing no badge left out.
	 */
	public static final String SECTION_BADGES = "sectionBadges";

	private final PresentationPane _pane;

	/** The badge each section shows, by section name; a section showing none is left out. */
	private final Map<String, BadgeDisplay> _badges = new LinkedHashMap<>();

	/**
	 * Creates a {@link ChatRailControl}.
	 *
	 * @param context
	 *        The view context of the rail element.
	 * @param config
	 *        The rail's configuration.
	 * @param sections
	 *        The configured sections with their content elements.
	 */
	public ChatRailControl(ViewContext context, Config config, List<Section> sections) {
		// The rail's own chat-id when it has one, otherwise the enclosing <chat>'s, otherwise
		// "default".
		super(context, blankToDefault(config.getReactElement(), "TLChatRail"),
			ChatScope.resolve(context, config.getChatId()), "chat-rail");
		setCssClass(config.getCssClass());

		putState("strip", Boolean.valueOf(config.getStrip()));
		putState("open", Boolean.valueOf(config.getOpen()));
		putState("drawerBelow", Integer.valueOf(config.getDrawerBelow()));
		putState("paneMode", config.getPaneMode().getExternalName());
		// A key of its own, because the stored width is written under "paneWidth" on every render:
		// the configured width is what a user who has never dragged the splitter gets.
		putState("paneWidthDefault", Integer.valueOf(Math.max(0, config.getPaneWidth())));

		putState("sections", sectionState(context, sections));
		putState(SECTION_BADGES, badgeState());
		// Part of the bootstrap, so a rail that is the first element of its chat renders collapsed
		// where it should be rather than flashing every section open. When another element built the
		// store first, the browser applies the same seed once instead.
		putState("collapsedSections", sections.stream()
			.filter(section -> !section.open())
			.map(Section::name)
			.collect(Collectors.toList()));

		ChannelRef inputRef = config.getInput();
		if (inputRef == null) {
			_pane = null;
			applyPane();
			return;
		}
		_pane = new PresentationPane(context, context.resolveChannel(inputRef), this, this::applyPane);
		addCleanupAction(_pane::dispose);
		applyPane();
	}

	/** Closes the pane, which is writing {@code null} back to the channel that opened it. */
	@ReactCommandHandler("closePane")
	public void closePane() {
		if (_pane != null) {
			_pane.close();
		}
	}

	/** Returns to what the pane showed before, or closes it when it showed nothing before. */
	@ReactCommandHandler("paneBack")
	public void paneBack() {
		if (_pane != null) {
			_pane.back();
		}
	}

	private List<Map<String, Object>> sectionState(ViewContext context, List<Section> sections) {
		Resources resources = Resources.getInstance();
		List<Map<String, Object>> rows = new ArrayList<>(sections.size());
		for (Section section : sections) {
			String name = section.name();
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("name", name);
			row.put("label", label(resources, section.label(), name));
			row.put("icon", ChatIcons.encode(context, section.icon()));
			row.put("open", Boolean.valueOf(section.open()));
			// One child context per section, so two sections routing `<slot-content>` of the same
			// name reach independent positions.
			row.put("content", ContentControls.toControl(section.content(),
				context.childContext("section").withChildSlotPath(name)));
			rows.add(row);

			BadgeBinding badge = BadgeBinding.create(context, section.badge());
			if (badge == null) {
				continue;
			}
			BadgeDisplay current = badge.current();
			if (current != null) {
				_badges.put(name, current);
			}
			badge.bind(this, display -> applyBadge(name, display));
		}
		return rows;
	}

	private void applyBadge(String name, BadgeDisplay display) {
		if (Objects.equals(display, _badges.get(name))) {
			return;
		}
		if (display == null) {
			_badges.remove(name);
		} else {
			_badges.put(name, display);
		}
		// A map of its own, so the section list - which carries the child controls - is written once
		// and never re-serialized for a changing badge.
		putState(SECTION_BADGES, badgeState());
	}

	private Map<String, Object> badgeState() {
		Map<String, Object> result = new LinkedHashMap<>();
		_badges.forEach((name, display) -> result.put(name, display.toState()));
		return result;
	}

	/**
	 * Tells the browser what the pane's history now is: the steps, bottom first, the top one shown.
	 * An empty history closes the pane.
	 */
	private void applyPane() {
		putState("paneSteps", _pane == null ? List.of() : _pane.state());
	}

	private static String label(Resources resources, ResKey key, String fallback) {
		if (key == null) {
			return fallback;
		}
		String label = resources.getString(key, null);
		return label == null || label.isEmpty() ? fallback : label;
	}
}
