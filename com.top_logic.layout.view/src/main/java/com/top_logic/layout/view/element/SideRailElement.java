/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactSideRailControl;
import com.top_logic.layout.react.control.layout.ReactSideRailControl.Section;
import com.top_logic.layout.react.control.layout.ReactSideRailControl.Side;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.security.AccessChecks;
import com.top_logic.layout.view.security.UnitHiding;
import com.top_logic.util.Resources;

/**
 * A collapsible column beside another element, with sections: {@code <side-rail>}.
 *
 * <p>
 * The rail shows a strip with the icon (and the badge) of every section. A click on an icon opens
 * the rail on that section, a second click on the shown section closes it again. While it is open,
 * the rail shows its sections stacked in a column, each with a header that expands and collapses it.
 * A rail with a single section is the collapsible column for any element, e.g. a list.
 * </p>
 *
 * <pre>
 * &lt;side-rail side="left" drawer-below="44" open-channel="listOpen"&gt;
 *   &lt;section name="list" label="..." icon="css:fa-solid fa-list"&gt;
 *     &lt;data-list .../&gt;
 *   &lt;/section&gt;
 * &lt;/side-rail&gt;
 * </pre>
 *
 * <p>
 * Where the area of the rail is narrower than {@link Config#getDrawerBelow() drawer-below}, only the
 * strip remains, and the rail opens as a modal drawer over its neighbour. The
 * {@link Config#getOpenChannel() open-channel} follows whether the rail is open, and opens and closes
 * it when written, so that any other element - a button, a command - opens the rail. The badge of a
 * section counts as seen while the section is shown; the
 * {@link Config#getBadgeChannel() badge-channel} receives the sum of what the sections count beyond
 * that, e.g. for the button that opens the rail.
 * </p>
 *
 * <p>
 * In a {@link SplitPanelElement split panel}, the rail shrinks its pane to the strip while it is
 * closed, fills its pane while it is open, and a shown section with a {@link SectionConfig#getWidth()
 * width} of its own widens the pane for as long as it is shown. Outside a split panel, the column has
 * the {@link Config#getWidth() width} of the rail, which the user drags.
 * </p>
 *
 * @implNote Whether the column is open, which sections are expanded and the width the user dragged
 *           the column to are stored in the {@link PersonalConfiguration} as a JSON map with the
 *           entries {@link #OPEN_ENTRY}, {@link #SECTIONS_ENTRY} (section name to expansion) and
 *           {@link #WIDTH_ENTRY}. The key is the configured
 *           {@link com.top_logic.layout.view.UIElement.Config#getPersonalizationKey() personalization
 *           key} or, if none is configured, the personalization key of the view context extended by
 *           {@link #SIDE_RAIL_SEGMENT}.
 */
@InApp
public class SideRailElement implements UIElement {

	/** Personalization key segment of a rail without configured personalization key. */
	public static final String SIDE_RAIL_SEGMENT = "side-rail";

	/** Entry of the stored state: whether the column is open. */
	public static final String OPEN_ENTRY = "open";

	/** Entry of the stored state: the expansion of the sections by section name. */
	public static final String SECTIONS_ENTRY = "sections";

	/** Entry of the stored state: the width the user dragged the column to, in pixels. */
	public static final String WIDTH_ENTRY = "width";

	/**
	 * Configuration for {@link SideRailElement}.
	 */
	@TagName("side-rail")
	@TypeIcon("css:fa-solid fa-table-columns")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(SideRailElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getSections()}. */
		String SECTIONS = "sections";

		/** Configuration name for {@link #getSide()}. */
		String SIDE = "side";

		/** Configuration name for {@link #getStrip()}. */
		String STRIP = "strip";

		/** Configuration name for {@link #getOpen()}. */
		String OPEN = "open";

		/** Configuration name for {@link #getOpenChannel()}. */
		String OPEN_CHANNEL = "open-channel";

		/** Configuration name for {@link #getBadgeChannel()}. */
		String BADGE_CHANNEL = "badge-channel";

		/** Configuration name for {@link #getDrawerBelow()}. */
		String DRAWER_BELOW = "drawer-below";

		/** Configuration name for {@link #getWidth()}. */
		String WIDTH = "width";

		/**
		 * The sections, from top to bottom.
		 *
		 * @implNote Written directly as {@code <section>} children of the {@code <side-rail>}. Keyed
		 *           by {@link SectionConfig#getName()}, so that an overlay of the view adds,
		 *           repositions or overrides a single section.
		 */
		@Name(SECTIONS)
		@Key(SectionConfig.NAME)
		@DefaultContainer
		@TreeProperty
		List<SectionConfig> getSections();

		/**
		 * The side of its neighbour the rail stands on: {@code left} (at the start) or {@code right}
		 * (at the end). The drawer slides in from this side.
		 */
		@Name(SIDE)
		@FormattedDefault("left")
		Side getSide();

		/**
		 * Whether the strip of section icons is shown.
		 *
		 * <p>
		 * The strip says what the rail holds while it is closed. Without it, the rail opens only
		 * through its {@link #getOpenChannel() open-channel}.
		 * </p>
		 */
		@Name(STRIP)
		@BooleanDefault(true)
		boolean getStrip();

		/**
		 * Whether the rail starts open.
		 *
		 * <p>
		 * The state for a user who has never opened or closed this rail; the choice of a user who has
		 * is remembered and takes precedence.
		 * </p>
		 */
		@Name(OPEN)
		@BooleanDefault(true)
		boolean getOpen();

		/**
		 * Channel that follows whether the rail is open, and opens or closes it when written.
		 *
		 * <p>
		 * Holds {@code true} while the rail is open - its column, or its drawer where the rail is
		 * one. Writing {@code true} or {@code false} opens or closes the rail,
		 * so that a button or a command elsewhere opens it. Leave it empty when nothing outside the
		 * rail opens it.
		 * </p>
		 */
		@Name(OPEN_CHANNEL)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getOpenChannel();

		/**
		 * Channel receiving what the badges of the sections count beyond what the user has seen.
		 *
		 * <p>
		 * The badge of a section counts as seen while the section is shown: the rail is open, and the
		 * section expanded. The channel holds the sum of what each section counts beyond what was
		 * seen of it, as a number, e.g. for the {@code badge} of the button that opens the rail.
		 * </p>
		 */
		@Name(BADGE_CHANNEL)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getBadgeChannel();

		/**
		 * Width of the area the rail stands in, in rem, below which the rail is a drawer.
		 *
		 * <p>
		 * The area is the element the drawer opens over: the nearest positioned element around the
		 * rail, e.g. the split panel it stands in. Below the width, the rail keeps only its strip, and
		 * opening it opens a modal drawer over its neighbour instead of a column of its own. Set it to
		 * the width the open column and its neighbours need side by side. {@code 0} keeps a column at
		 * every width.
		 * </p>
		 */
		@Name(DRAWER_BELOW)
		@IntDefault(0)
		int getDrawerBelow();

		/**
		 * Width of the column in pixels, when the user has not dragged it.
		 *
		 * <p>
		 * Applies outside a split panel; the user drags the inner edge of the column, and the width
		 * is remembered. In a split panel, the pane of the rail has the size the split panel gives
		 * it. The drawer of the rail has this width as well.
		 * </p>
		 */
		@Name(WIDTH)
		@IntDefault(280)
		int getWidth();
	}

	/**
	 * A section of a {@code <side-rail>}: an icon on the strip and a block of the column holding view
	 * elements.
	 *
	 * <p>
	 * Its {@link #getBadge() badge} stands on its strip icon and in its header, and counts into the
	 * {@link Config#getBadgeChannel() badge-channel} of the rail. Its
	 * {@link #getVisibility() visibility condition} leaves it out of the rail, strip icon included,
	 * e.g. while there is nothing to show in it.
	 * </p>
	 */
	@TagName("section")
	public interface SectionConfig extends WithBadge, WithVisibility {

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getIcon()}. */
		String ICON = "icon";

		/** Configuration name for {@link #getOpen()}. */
		String OPEN = "open";

		/** Configuration name for {@link #getWidth()}. */
		String WIDTH = "width";

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/**
		 * Technical name of the section, unique within the rail.
		 *
		 * <p>
		 * The key under which the expansion of the section is remembered, and by which an overlay of
		 * the view addresses it.
		 * </p>
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * The label of the section, in its header and as the name of its strip icon. Without a label,
		 * the {@link #getName() name} is shown.
		 */
		@Name(LABEL)
		ResKey getLabel();

		/**
		 * The icon on the strip, e.g. {@code css:fa-solid fa-list}. Without one, the strip shows a
		 * generic glyph.
		 */
		@Name(ICON)
		String getIcon();

		/**
		 * Whether the section starts expanded.
		 *
		 * <p>
		 * The state for a user who has never expanded or collapsed this section; the choice of a user
		 * who has is remembered per section {@link #getName() name}.
		 * </p>
		 */
		@Name(OPEN)
		@BooleanDefault(true)
		boolean getOpen();

		/**
		 * Width in pixels the section asks for while it is shown, {@code 0} for none.
		 *
		 * <p>
		 * While the section is shown, the column - or the pane of the rail in a split panel - has this
		 * width instead of the one the user gave it, e.g. for a document that needs room. The width
		 * the user gave it comes back when the section is no longer shown; it is not changed.
		 * </p>
		 */
		@Name(WIDTH)
		@IntDefault(0)
		int getWidth();

		/**
		 * The content of the section.
		 *
		 * @implNote Keyed by {@link UIElement.Config#ID} so that a same-path overlay can address an
		 *           element.
		 */
		@Key(UIElement.Config.ID)
		@Name(CHILDREN)
		@DefaultContainer
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getChildren();
	}

	private final Config _config;

	private final List<SectionEntry> _sections;

	/**
	 * Creates a new {@link SideRailElement} from configuration.
	 */
	@CalledByReflection
	public SideRailElement(InstantiationContext context, Config config) {
		_config = config;
		_sections = new ArrayList<>();
		for (SectionConfig section : config.getSections()) {
			List<UIElement> children = section.getChildren().stream()
				.map(context::getInstance)
				.collect(Collectors.toList());
			_sections.add(new SectionEntry(section, UnitVisibility.create(section.getVisibility()), children));
		}
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return _sections.stream()
			.map(entry -> ChildGroup.keyed(entry._config().getName(), entry._children()))
			.collect(Collectors.toList());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		String key = personalizationKey(context);
		Map<String, Object> stored = load(key);

		Object storedOpen = stored.get(OPEN_ENTRY);
		boolean open = storedOpen instanceof Boolean value ? value.booleanValue() : _config.getOpen();
		Object storedWidth = stored.get(WIDTH_ENTRY);
		int width = storedWidth instanceof Number value ? value.intValue() : _config.getWidth();
		Map<?, ?> storedSections =
			stored.get(SECTIONS_ENTRY) instanceof Map<?, ?> map ? map : Map.of();

		Resources resources = Resources.getInstance();
		List<Section> sections = new ArrayList<>();
		List<String> expanded = new ArrayList<>();
		for (SectionEntry entry : _sections) {
			SectionConfig section = entry._config();
			String name = section.getName();
			ViewContext sectionContext = context.childContext(name).withChildSlotPath(name);
			String label = section.getLabel() == null ? null : resources.getString(section.getLabel(), null);
			sections.add(new Section(name, StringServices.isEmpty(label) ? name : label,
				StringServices.nonEmpty(section.getIcon()), Math.max(0, section.getWidth()),
				ContentControls.toControl(entry._children(), sectionContext)));
			Object storedExpansion = storedSections.get(name);
			if (storedExpansion instanceof Boolean value ? value.booleanValue() : section.getOpen()) {
				expanded.add(name);
			}
		}

		ReactSideRailControl rail = new ReactSideRailControl(context, _config.getSide(), _config.getStrip(),
			_config.getDrawerBelow(), width, open, sections, expanded);
		rail.setCssClass(_config.getCssClass());

		for (SectionEntry entry : _sections) {
			String name = entry._config().getName();
			BadgeBinding badge = BadgeBinding.create(context, entry._config());
			if (badge != null) {
				rail.setBadge(name, badge.current());
				badge.bind(rail, display -> rail.setBadge(name, display));
			}
			UnitHiding hiding = AccessChecks.hiding(null, UnitVisibility.bind(entry._visibility(), context), context);
			if (hiding != null) {
				rail.setHidden(name, hiding.isHidden());
				hiding.bind(rail, hidden -> rail.setHidden(name, hidden.booleanValue()));
			}
		}

		ViewChannel openChannel = resolve(context, _config.getOpenChannel());
		ViewChannel badgeChannel = resolve(context, _config.getBadgeChannel());
		rail.setListener(new ReactSideRailControl.Listener() {
			@Override
			public void openChanged(boolean isOpen) {
				if (openChannel != null) {
					openChannel.set(Boolean.valueOf(isOpen));
				}
			}

			@Override
			public void columnOpenChanged(boolean isOpen) {
				save(key, OPEN_ENTRY, Boolean.valueOf(isOpen));
			}

			@Override
			public void sectionExpandedChanged(String name, boolean isExpanded) {
				Map<String, Object> sectionStates = new HashMap<>();
				if (load(key).get(SECTIONS_ENTRY) instanceof Map<?, ?> map) {
					map.forEach((section, value) -> sectionStates.put(String.valueOf(section), value));
				}
				sectionStates.put(name, Boolean.valueOf(isExpanded));
				save(key, SECTIONS_ENTRY, sectionStates);
			}

			@Override
			public void widthChanged(int newWidth) {
				save(key, WIDTH_ENTRY, Integer.valueOf(newWidth));
			}

			@Override
			public void unseenChanged(int unseen) {
				if (badgeChannel != null) {
					badgeChannel.set(Integer.valueOf(unseen));
				}
			}
		});

		if (openChannel != null) {
			openChannel.set(Boolean.valueOf(rail.isOpen()));
			ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> {
				if (newValue instanceof Boolean value) {
					rail.setOpen(value.booleanValue());
				}
			};
			rail.addCleanupAction(openChannel.addListener(listener)::dispose);
		}
		if (badgeChannel != null) {
			badgeChannel.set(Integer.valueOf(rail.getUnseen()));
		}
		return rail;
	}

	private static ViewChannel resolve(ViewContext context, ChannelRef ref) {
		return ref == null ? null : context.resolveChannel(ref);
	}

	/**
	 * The key the state of the rail is remembered under.
	 */
	private String personalizationKey(ViewContext context) {
		String configured = _config.getPersonalizationKey();
		if (!StringServices.isEmpty(configured)) {
			return configured;
		}
		return context.getPersonalizationKey() + "." + SIDE_RAIL_SEGMENT;
	}

	/**
	 * The remembered state of the rail, empty if nothing is remembered.
	 */
	private static Map<String, Object> load(String key) {
		Map<String, Object> result = new LinkedHashMap<>();
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return result;
		}
		if (pc.getJSONValue(key) instanceof Map<?, ?> raw) {
			raw.forEach((entry, value) -> result.put(String.valueOf(entry), value));
		}
		return result;
	}

	/**
	 * Remembers one entry of the state of the rail, keeping the others.
	 */
	private static void save(String key, String entry, Object value) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}
		Map<String, Object> state = load(key);
		if (Objects.equals(state.get(entry), value)) {
			return;
		}
		state.put(entry, value);
		pc.setJSONValue(key, state);
	}

	/**
	 * A configured section, as far as it is the same for every session.
	 */
	private record SectionEntry(SectionConfig _config, UnitVisibility _visibility, List<UIElement> _children) {
	}
}
