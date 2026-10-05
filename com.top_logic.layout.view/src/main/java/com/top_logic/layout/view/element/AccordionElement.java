/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.accordion.AccordionSection;
import com.top_logic.layout.react.control.accordion.ReactAccordionControl;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.CliqueRegistry;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ToolbarBuilder;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewCommands;
import com.top_logic.layout.view.navigation.RevealPath;
import com.top_logic.layout.view.navigation.RevealRegistry;

/**
 * UIElement that wraps {@link ReactAccordionControl}.
 *
 * <p>
 * Renders a stack of sections, each with a header and a body that the user expands or collapses.
 * Each {@code <section>} child in the configuration defines a section with an ID, label, optional
 * icon, optional header commands, and inline content elements. The content of a section is created
 * the first time the section is expanded and stays alive while it is collapsed. Like a panel, each
 * section is the command scope of its content: the commands the content contributes are shown in
 * the section header.
 * </p>
 *
 * <p>
 * Which sections are expanded is remembered per user, unless personalization is switched off.
 * Revealing an element within a collapsed section expands the section.
 * </p>
 *
 * @implNote The expansion is stored in the {@link PersonalConfiguration} as a JSON map from section
 *           ID to expansion. The key is the configured
 *           {@link com.top_logic.layout.view.UIElement.Config#getPersonalizationKey()
 *           personalization key} or, if none is configured, the personalization key of the view
 *           context extended by {@link #ACCORDION_SEGMENT}. A change updates only the entry of the
 *           changed section, so that accordions sharing a key with distinct section IDs keep each
 *           other's entries.
 */
@InApp
public class AccordionElement implements UIElement {

	/** Personalization key segment of an accordion without configured personalization key. */
	public static final String ACCORDION_SEGMENT = "accordion";

	/** Personalization key segment of the content of a section. */
	public static final String SECTION_SEGMENT = "section";

	/** The placements of the commands shown in the header of a section, in display order. */
	private static final List<CommandPlacement> HEADER_PLACEMENTS =
		List.of(CommandPlacement.TOOLBAR, CommandPlacement.BUTTON_BAR);

	/**
	 * Configuration for {@link AccordionElement}.
	 */
	@TagName("accordion")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(AccordionElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getSections()}. */
		String SECTIONS = "sections";

		/** Configuration name for {@link #isExclusive()}. */
		String EXCLUSIVE = "exclusive";

		/** Configuration name for {@link #isPersonalize()}. */
		String PERSONALIZE = "personalize";

		/** Configuration name for {@link #getCommandDisplay()}. */
		String COMMAND_DISPLAY = "command-display";

		/**
		 * The sections, from top to bottom.
		 *
		 * @implNote Written directly as {@code <section>} children of the {@code <accordion>} (the
		 *           {@code <sections>} wrapper is optional). Keyed by
		 *           {@link ContentSectionConfig#getId()} so that a configuration fragment in another
		 *           module can add, reposition ({@code config:position}) or override individual
		 *           sections.
		 */
		@Name(SECTIONS)
		@Key(SectionConfig.ID)
		@DefaultContainer
		@TreeProperty
		List<SectionConfig> getSections();

		/**
		 * Whether at most one section is expanded at a time.
		 *
		 * <p>
		 * Expanding a section of an exclusive accordion collapses the section expanded before. All
		 * sections may be collapsed.
		 * </p>
		 */
		@Name(EXCLUSIVE)
		boolean isExclusive();

		/**
		 * Whether the accordion remembers per user which sections are expanded.
		 *
		 * <p>
		 * The remembered expansion of a section takes precedence over its configured
		 * {@link SectionConfig#isExpanded() expansion}. Two remembering accordions in the same
		 * view context need distinct section IDs or distinct personalization keys, since they
		 * otherwise share their remembered expansion.
		 * </p>
		 */
		@Name(PERSONALIZE)
		@BooleanDefault(true)
		boolean isPersonalize();

		/**
		 * How the buttons of the section headers display icon and label.
		 *
		 * <p>
		 * A command's own display setting takes precedence.
		 * </p>
		 */
		@Name(COMMAND_DISPLAY)
		@FormattedDefault("icon-only")
		ButtonDisplayMode getCommandDisplay();
	}

	/**
	 * Configuration for a single section of an accordion.
	 */
	@TagName("section")
	public interface SectionConfig extends ContentSectionConfig {

		/** Configuration name for {@link #isExpanded()}. */
		String EXPANDED = "expanded";

		/** Configuration name for {@link #getCommands()}. */
		String COMMANDS = "commands";

		/**
		 * Whether the section is expanded when the accordion is displayed for the first time.
		 *
		 * <p>
		 * In an exclusive accordion, only the first of the sections configured as expanded is
		 * expanded.
		 * </p>
		 */
		@Name(EXPANDED)
		boolean isExpanded();

		/**
		 * Commands displayed in the header of the section.
		 *
		 * <p>
		 * The header shows the commands placed in the toolbar, followed by those placed in the
		 * button bar (a section has no button bar of its own), whether the section is expanded or
		 * not. Like a panel, every section is the command scope of its content: the commands the
		 * content contributes (e.g. the edit and save commands of a form) are shown in the header
		 * as well, once the section has been expanded for the first time.
		 * </p>
		 */
		@Name(COMMANDS)
		@EntryTag("command")
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();
	}

	private final List<SectionEntry> _sections;

	private final boolean _exclusive;

	private final boolean _personalize;

	private final String _personalizationKey;

	private final ButtonDisplayMode _commandDisplay;

	private final String _cssClass;

	/**
	 * Creates a new {@link AccordionElement} from configuration.
	 */
	@CalledByReflection
	public AccordionElement(InstantiationContext context, Config config) {
		_sections = new ArrayList<>();
		for (SectionConfig sectionConfig : config.getSections()) {
			List<ViewCommand> commands = new ArrayList<>();
			List<ViewCommand.Config> commandConfigs = new ArrayList<>();
			for (PolymorphicConfiguration<? extends ViewCommand> commandConfig : sectionConfig.getCommands()) {
				ViewCommand command = context.getInstance(commandConfig);
				if (command != null && commandConfig instanceof ViewCommand.Config) {
					commands.add(command);
					commandConfigs.add((ViewCommand.Config) commandConfig);
				}
			}
			_sections.add(new SectionEntry(ContentSection.of(context, sectionConfig), sectionConfig.isExpanded(),
				commands, commandConfigs));
		}
		_exclusive = config.isExclusive();
		_personalize = config.isPersonalize();
		_personalizationKey = config.getPersonalizationKey();
		_commandDisplay = config.getCommandDisplay();
		_cssClass = config.getCssClass();
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return _sections.stream()
			.map(entry -> ChildGroup.keyed(entry._section().getId(), entry._section().getChildren()))
			.collect(Collectors.toList());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		String key = _personalize ? personalizationKey(context) : null;
		Map<String, Boolean> stored = key != null ? loadExpansion(key) : Map.of();

		RevealPath here = RevealPath.of(context);
		List<AccordionSection> sections = new ArrayList<>();
		List<ViewCommandModel> commandModels = new ArrayList<>();
		for (SectionEntry entry : _sections) {
			ContentSection section = entry._section();
			if (!section.isAccessible()) {
				// Access denied for the current user: omit the section entirely.
				continue;
			}
			String id = section.getId();

			// The content of a section is created only when the section is first expanded, so the
			// section's context must already say where that content will sit.
			ViewContext sectionContext =
				section.contentContext(context.withScope(RevealPath.class, here.append(this, id)), SECTION_SEGMENT);

			// Like a panel, a section is the command scope of its content. Its header is displayed
			// whether the section is expanded or not, so the scope and the header toolbar are built
			// right away, independent of the content; commands the content contributes once it is
			// created show up in the toolbar then. A section has no button bar, so its button-bar
			// commands are shown in the header as well, after the toolbar ones.
			List<ViewCommandModel> models =
				ViewCommands.buildCommandModels(sectionContext, entry._commands(), entry._commandConfigs());
			commandModels.addAll(models);
			CommandScope scope = new CommandScope(models);
			ViewContext contentContext = sectionContext.withScope(CommandScope.class, scope);
			ReactControl actions =
				ToolbarBuilder.buildLive(context, scope, HEADER_PLACEMENTS, new CliqueRegistry(), _commandDisplay);

			Boolean storedExpansion = stored.get(id);
			AccordionSection accordionSection =
				new AccordionSection(id, section.label(), () -> section.createContent(contentContext))
					.withExpanded(storedExpansion != null ? storedExpansion.booleanValue() : entry._expanded())
					.withIcon(section.getIcon())
					.withActions(actions);
			sections.add(accordionSection);
		}

		ReactAccordionControl accordion = new ReactAccordionControl(context, null, sections, _exclusive);
		accordion.setCssClass(_cssClass);

		if (key != null) {
			accordion.addExpansionListener((control, sectionId, expanded) -> saveExpansion(key, sectionId, expanded));
		}

		ViewCommands.registerLifecycle(context, commandModels, accordion);

		RevealRegistry registry = context.getRevealRegistry();
		if (registry != null) {
			accordion.addCleanupAction(registry.registerContainer(this, here, accordion));
		}

		return accordion;
	}

	/**
	 * The key the expansion of the sections is remembered under.
	 */
	private String personalizationKey(ViewContext context) {
		if (!StringServices.isEmpty(_personalizationKey)) {
			return _personalizationKey;
		}
		return context.getPersonalizationKey() + "." + ACCORDION_SEGMENT;
	}

	/**
	 * The remembered expansion of the sections by section ID, empty if nothing is remembered.
	 */
	private static Map<String, Boolean> loadExpansion(String key) {
		Map<String, Boolean> result = new HashMap<>();
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return result;
		}
		Object value = pc.getJSONValue(key);
		if (value instanceof Map<?, ?> raw) {
			for (Map.Entry<?, ?> entry : raw.entrySet()) {
				if (entry.getKey() instanceof String sectionId && entry.getValue() instanceof Boolean expanded) {
					result.put(sectionId, expanded);
				}
			}
		}
		return result;
	}

	/**
	 * Remembers the expansion of one section, keeping the remembered expansion of all other
	 * sections.
	 */
	private static void saveExpansion(String key, String sectionId, boolean expanded) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}
		Map<String, Boolean> states = loadExpansion(key);
		states.put(sectionId, Boolean.valueOf(expanded));
		pc.setJSONValue(key, states);
	}

	/**
	 * A configured section, as far as it is the same for every session.
	 */
	private record SectionEntry(ContentSection _section, boolean _expanded, List<ViewCommand> _commands,
			List<ViewCommand.Config> _commandConfigs) {
	}
}
