/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.ai.chat.view.ChatRailElement.Config;
import com.top_logic.ai.chat.view.ChatRailElement.Section;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.ContentControls;
import com.top_logic.util.Resources;

/**
 * React control for {@code <chat-rail/>}.
 *
 * <p>
 * Builds one child control per configured section and one for the pane, and puts them into its own
 * state, which is how a control reaches the client: the browser renders each of them in its place
 * inside the rail. Every child is built once and stays built - a collapsed section, and a pane
 * nobody has opened, are hidden rather than dropped, so a section keeps reporting its badge while it
 * is out of sight and a reopened pane shows the object as it is now.
 * </p>
 *
 * <p>
 * The state this control owns is the frame: which sections there are, what they are called, their
 * icons, the current badge numbers and whether the pane has something to show. Everything inside a
 * section belongs to the element configured there.
 * </p>
 */
public class ChatRailControl extends ReactControl {

	/** State key for the section badge numbers, keyed by section name. */
	private static final String SECTION_BADGES = "sectionBadges";

	private final ChatViewBinding _binding;

	private final ViewChannel _paneChannel;

	private final Map<String, Integer> _badges = new LinkedHashMap<>();

	/**
	 * Creates a {@link ChatRailControl}.
	 *
	 * @param context
	 *        The view context of the rail element.
	 * @param config
	 *        The rail's configuration.
	 * @param sections
	 *        The configured sections with their content elements.
	 * @param pane
	 *        The elements the pane shows; empty for a rail without a pane.
	 */
	public ChatRailControl(ViewContext context, Config config, List<Section> sections, List<UIElement> pane) {
		super(context, null, blankToDefault(config.getReactElement(), "TLChatRail"));

		// The rail's own chat-id when it has one, otherwise the enclosing <chat>'s, otherwise
		// "default". The browser keys its store by what we push here, so it must be the resolved id.
		String chatId = ChatScope.resolve(context, config.getChatId());
		_binding = ChatViewBinding.bind(context, chatId, "chat-rail");
		_binding.writeSharedState(this::putState);
		putState("chatId", chatId);
		putState("strip", Boolean.valueOf(config.getStrip()));
		putState("open", Boolean.valueOf(config.getOpen()));
		putState("paneMode", config.getPaneMode().getExternalName());
		// A key of its own, because the stored width is written under "paneWidth" on every render:
		// the configured width is what a user who has never dragged the splitter gets.
		putState("paneWidthDefault", Integer.valueOf(Math.max(0, config.getPaneWidth())));

		putState("sections", sectionState(context, sections));
		putState(SECTION_BADGES, new LinkedHashMap<>(_badges));
		// Part of the bootstrap, so a rail that is the first element of its chat renders collapsed
		// where it should be rather than flashing every section open. When another element built the
		// store first, the browser applies the same seed once instead.
		putState("collapsedSections", sections.stream()
			.filter(section -> !section.open())
			.map(Section::name)
			.collect(Collectors.toList()));

		if (!pane.isEmpty()) {
			putState("pane", ContentControls.toControl(pane, context.childContext("pane")
				.withChildSlotPath("pane")));
		}

		ChannelRef inputRef = config.getInput();
		if (inputRef == null) {
			_paneChannel = null;
			applyPane(null);
			return;
		}
		_paneChannel = context.resolveChannel(inputRef);
		ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> applyPane(newValue);
		_paneChannel.addListener(listener);
		addCleanupAction(() -> _paneChannel.removeListener(listener));
		applyPane(_paneChannel.get());
	}

	/**
	 * Re-reads the user's stored layout.
	 *
	 * <p>
	 * A browser reload re-renders the control tree the window already holds, so this hook is the
	 * only place that runs again. Writing the layout in the constructor would replay the first
	 * render's rail state for the life of the window (Gitea #423).
	 * </p>
	 */
	@Override
	protected void onBeforeWrite() {
		super.onBeforeWrite();
		_binding.writeLayoutState(this::putState);
	}

	/** Closes the pane, which is writing {@code null} back to the channel that opened it. */
	@ReactCommandHandler("closePane")
	public void closePane() {
		if (_paneChannel != null) {
			_paneChannel.set(null);
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
			row.put("icon", icon(section.icon()));
			row.put("open", Boolean.valueOf(section.open()));
			// One child context per section, so two sections routing `<slot-content>` of the same
			// name reach independent positions.
			row.put("content", ContentControls.toControl(section.content(),
				context.childContext("section").withChildSlotPath(name)));
			rows.add(row);

			ChannelRef badgeRef = section.badge();
			if (badgeRef == null) {
				continue;
			}
			ViewChannel badge = context.resolveChannel(badgeRef);
			_badges.put(name, Integer.valueOf(count(badge.get())));
			ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> applyBadge(name, newValue);
			badge.addListener(listener);
			addCleanupAction(() -> badge.removeListener(listener));
		}
		return rows;
	}

	private void applyBadge(String name, Object value) {
		Integer count = Integer.valueOf(count(value));
		if (count.equals(_badges.get(name))) {
			return;
		}
		_badges.put(name, count);
		// A map of its own, so the section list - which carries the child controls - is written once
		// and never re-serialized for a changing number.
		putState(SECTION_BADGES, new LinkedHashMap<>(_badges));
	}

	private void applyPane(Object value) {
		Object shown = single(value);
		putState("paneOpen", Boolean.valueOf(shown != null));
		putState("paneTitle", shown == null ? "" : MetaLabelProvider.INSTANCE.getLabel(shown));
	}

	/** The one object a channel holds, {@code null} for nothing and for a set of several. */
	private static Object single(Object value) {
		if (value instanceof Collection<?> many) {
			return many.size() == 1 ? many.iterator().next() : null;
		}
		return value;
	}

	private static int count(Object value) {
		Object single = single(value);
		return single instanceof Number number ? Math.max(0, number.intValue()) : 0;
	}

	private static String label(Resources resources, ResKey key, String fallback) {
		if (key == null) {
			return fallback;
		}
		String label = resources.getString(key, null);
		return label == null || label.isEmpty() ? fallback : label;
	}

	private static String icon(ThemeImage image) {
		return image == null ? "" : image.resolve().toEncodedForm();
	}

	private static String blankToDefault(String value, String fallback) {
		if (value == null) {
			return fallback;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? fallback : trimmed;
	}
}
