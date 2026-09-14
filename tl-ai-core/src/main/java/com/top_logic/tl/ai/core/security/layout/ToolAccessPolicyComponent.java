/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.DefaultButtonRenderer;
import com.top_logic.layout.form.control.LabelControl;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.TristateControlProvider;
import com.top_logic.layout.form.template.model.AbstractMember;
import com.top_logic.layout.form.template.model.FieldSetBoxTemplate;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.Columns;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.AllAgentNames;
import com.top_logic.tl.ai.core.security.AllMcpServerNames;
import com.top_logic.tl.ai.core.security.AllModelNames;
import com.top_logic.tl.ai.core.security.AllToolNames;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessRule;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BackgroundChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BaselineChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.CapabilityState;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.RoleTemplate;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;

/*
 * Proposals D1, E and F of doc-internal concept sec-config-ux, and the review of what they produced.
 *
 * The class comment below is exported to META-INF/messages_*.properties by the build's doclet and shown
 * as this component's tooltip in the layout editor, so it is written for an administrator and stays
 * short - TestConfigPresentation enforces a length limit on everything under this package prefix.
 * The design notes live here, where the doclet does not export them.
 *
 * Why an editor of its own rather than the generic service editor, which can already write this policy:
 * that editor renders the stored format, and the stored format is not the decision an administrator
 * makes. They decide "may this role change data" and get a list of permission boxes each holding two
 * mutually exclusive inputs; they decide "does everybody get read access by default" and get an optional
 * element whose absent, empty and filled states look the same.
 *
 * How it is built, after the review: as an ordinary reactive form. The form context carries a template
 * of collapsible fieldsets - Templates.fieldsetBox with a ConfigKey, which is the framework's own
 * collapsible group and remembers per user whether it is open - every field is a plain BooleanField or
 * SelectField in a descriptionBox, every explanation is the tooltip of the member it belongs to, and
 * apply and discard are command handlers in the component toolbar. The first version rendered a control
 * tree of its own, which worked but looked like nothing else in the administration.
 *
 * Two things are still rendered by hand, and both are content rather than input: the messages at the top
 * (banner, refusals, warnings), and the small note next to a capability, which says who else grants it
 * and therefore depends on other fields and has to repaint when those change. Both are DynamicFragment
 * controls embedded with Templates.htmlTemplate.
 *
 * A capability is one tri-state row, not a checkbox plus a second list: empty means the role says
 * nothing, ticked means granted, the third state means forbidden. That is the whole of what the storage
 * can say about one capability, the mutual exclusion is structural rather than a listener correcting a
 * second checkbox, and the empty state keeps the reading a first-time administrator gives it.
 *
 * On rebuilding: a change of *structure* - a role added, an entry added - drops the form context and
 * invalidates the component, which is the engine's own pattern and works here because nothing caches a
 * rendering control any more. A change of *value* never rebuilds: a choice that decides which fields
 * apply toggles their visibility (FormMember.setVisible), which is what the framework does everywhere
 * else and what keeps the field the user is standing in from being replaced underneath them.
 */

/**
 * Configures which security roles may use which agent tools.
 *
 * <p>
 * Writes the policy of the tool access service. What is saved takes effect when that service is
 * restarted, and this view offers the restart.
 * </p>
 *
 * @see ToolCatalogModelBuilder
 * @see EffectiveToolAccessComponent
 */
public class ToolAccessPolicyComponent extends FormComponent {

	/** Name of the group holding every configured role and the way to take another one in. */
	public static final String GROUP_ROLES = "roles";

	/** Name of the group holding the role that is being added and its template. */
	public static final String GROUP_NEW_ROLE = "newRole";

	/** Name of the group holding what every account gets. */
	public static final String GROUP_BASELINE = "baseline";

	/** Name of the group holding which agents, models and MCP servers a role may use. */
	public static final String GROUP_RUN_ACCESS = "runAccess";

	/** Prefix of the group holding one of the three run sections. */
	public static final String GROUP_SECTION_PREFIX = "section_";

	/** Name of the field holding what a section admits from a role it does not name. */
	public static final String FIELD_SECTION_DEFAULT = "sectionDefault";

	/** Prefix of the field saying whether one section names one role at all. */
	public static final String FIELD_RESTRICTED_PREFIX = "restricted_";

	/** Prefix of the field holding what one role may use in one section. */
	public static final String FIELD_ALLOWED_PREFIX = "allowed_";

	/** Prefix of the field holding what one role may never use in one section. */
	public static final String FIELD_FORBIDDEN_PREFIX = "forbidden_";

	/** Name of the group holding what an unattended run may reach. */
	public static final String GROUP_BACKGROUND = "background";

	/** Name of the group holding which tools belong to which capability group. */
	public static final String GROUP_ASSIGNMENTS = "assignments";

	/** Name of the group holding the entries of one role that name single tools. */
	public static final String GROUP_TOOL_ENTRIES = "toolEntries";

	/** Name prefix of the group holding one configured role. */
	public static final String GROUP_ROLE_PREFIX = "role_";

	/** Name of the field choosing which role to add to the policy. */
	public static final String FIELD_ROLE = "role";

	/** Name of the field choosing what a newly added role starts with. */
	public static final String FIELD_TEMPLATE = "template";

	/** Name of the field choosing one of several named states. */
	public static final String FIELD_CHOICE = "choice";

	/** Name of the field choosing capability groups. */
	public static final String FIELD_GROUPS = "groups";

	/** Name of the field choosing single tools. */
	public static final String FIELD_TOOLS = "tools";

	/** Name prefix of the tri-state saying what one role does about one capability group. */
	public static final String FIELD_CAPABILITY_PREFIX = "capability_";

	/** Name of the command adding something to the list it sits in. */
	public static final String FIELD_ADD = "add";

	/** Name of the command removing the thing it sits in. */
	public static final String FIELD_REMOVE = "remove";

	/** Name of the command restarting the service, next to the banner that asks for it. */
	public static final String FIELD_RESTART = "restart";

	/**
	 * How much room the label of a capability gets, so that the tri-states line up.
	 *
	 * <p>
	 * Wider than the framework's default, because a capability is named twice: what it is called and
	 * the technical name a policy file writes.
	 * </p>
	 */
	private static final String CAPABILITY_LABEL_WIDTH = "330px";

	/**
	 * How long the list of role names in the collapsed roles section may get.
	 *
	 * <p>
	 * Beyond it the summary says how many roles there are instead: a title that wraps says less than a
	 * count, and a list cut off in the middle says something false.
	 * </p>
	 */
	private static final int ROLE_NAMES_SUMMARY_LENGTH = 60;

	/**
	 * What one entry of a permission list names.
	 */
	public enum EntryKind {

		/** A whole capability group. */
		GROUP,

		/** Single tools, by their qualified name. */
		TOOLS;
	}

	/**
	 * Configuration options for {@link ToolAccessPolicyComponent}.
	 */
	public interface Config extends FormComponent.Config {

		@Override
		@ClassDefault(ToolAccessPolicyComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();
	}

	/** The policy being edited, or <code>null</code> if the saved one cannot be read. */
	private PolicyDraft _draft;

	/** Why the last apply was refused, empty when the last one succeeded or none has run. */
	private List<ResKey> _refusals = List.of();

	/** Every block of the view that is recomputed when a value changes. */
	private final Map<String, DynamicFragment> _dynamic = new LinkedHashMap<>();

	/**
	 * Whether a value listener is currently bringing dependent fields in line.
	 *
	 * <p>
	 * Setting the value of a field fires its own listener, and that listener would write the value
	 * back into the draft. Choosing "nothing is stated" for the limit of unattended runs removes the
	 * element, then clears the two pickers below it, and the clearing wrote an empty element back.
	 * </p>
	 */
	private boolean _syncing;

	/**
	 * The capability pickers of the group assignments, so that they can be kept apart.
	 *
	 * <p>
	 * One capability group has one assignment, so a group another row already holds is not offered.
	 * Moving one row therefore changes what the others may offer.
	 * </p>
	 */
	private final List<SelectField> _assignmentGroups = new ArrayList<>();

	/**
	 * Creates a {@link ToolAccessPolicyComponent} from configuration.
	 */
	@CalledByReflection
	public ToolAccessPolicyComponent(InstantiationContext context, Config config)
			throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		// The view is about the application's policy; there is no business object it could be opened on.
		return object == null;
	}

	@Override
	public FormContext createFormContext() {
		_dynamic.clear();
		_assignmentGroups.clear();
		FormContext context = new FormContext(this);
		if (!AiToolAccessService.Module.INSTANCE.isActive()) {
			return unreadable(context, I18NConstants.SERVICE_NOT_RUNNING);
		}
		if (_draft == null) {
			_draft = PolicyDraft.ofSavedPolicy();
		}
		if (_draft == null) {
			return unreadable(context, I18NConstants.POLICY_UNREADABLE);
		}

		List<HTMLTemplateFragment> sections = new ArrayList<>();
		sections.add(htmlTemplate(messages(context)));
		addRoleGroups(context, sections);
		addRunAccessGroups(context, sections);
		addBaselineGroup(context, sections);
		addBackgroundGroup(context, sections);
		addAssignmentGroup(context, sections);
		template(context, div(css("aiSecurityView"), fragment(sections)));

		if (!mayWrite()) {
			// Reading the policy is what the two other tabs are for; writing it is an administrator's
			// job, and the fields of a form are not command handlers, so nothing would have checked.
			context.setImmutable(true);
		}
		return context;
	}

	private static FormContext unreadable(FormContext context, ResKey message) {
		template(context, div(css("aiSecurityView"), htmlTemplate(note(message))));
		return context;
	}

	/** Whether the acting account may change the policy rather than only look at it. */
	private boolean mayWrite() {
		return BoundChecker.allowCommand(this, SimpleBoundCommandGroup.WRITE, getModel());
	}

	/**
	 * Rebuilds the form and repaints the whole view.
	 *
	 * <p>
	 * For every change that adds, removes or replaces a field. A change of a value never goes through
	 * here: it would replace the field the user is standing in.
	 * </p>
	 */
	private HandlerResult rebuild() {
		removeFormContext();
		invalidate();
		return HandlerResult.DEFAULT_RESULT;
	}

	/** Brings dependent fields in line with the draft, without letting them write it again. */
	private void syncing(Runnable update) {
		boolean outer = _syncing;
		_syncing = true;
		try {
			update.run();
		} finally {
			_syncing = outer;
		}
	}

	/** Repaints the blocks that depend on what the policy says, without rebuilding a field. */
	private void refreshNotes() {
		for (DynamicFragment note : _dynamic.values()) {
			note.requestRepaint();
		}
	}

	// ---------------------------------------------------------------- the roles

	/**
	 * The roles, in one collapsible section of their own.
	 *
	 * <p>
	 * A section per role and the way to take another one in are the same subject, and a policy with
	 * several roles pushed everything else off the screen while they sat at the top level. Grouped, the
	 * top level is four entries whatever the policy configures.
	 * </p>
	 */
	private void addRoleGroups(FormContext context, List<HTMLTemplateFragment> sections) {
		FormGroup roles = new FormGroup(GROUP_ROLES, getResPrefix());
		roles.setLabel(resources().getString(I18NConstants.SECTION_ROLES));
		roles.setTooltip(resources().getString(I18NConstants.SECTION_ROLES.tooltip()));
		context.addMember(roles);

		List<HTMLTemplateFragment> inner = new ArrayList<>();
		List<String> roleNames = _draft.roleNames();
		if (roleNames.isEmpty()) {
			inner.add(contentBox(htmlTemplate(note(I18NConstants.POLICY_NO_ROLES))));
		}
		for (int r = 0; r < roleNames.size(); r++) {
			String roleName = roleNames.get(r);
			String name = GROUP_ROLE_PREFIX + r;
			String prefix = path(GROUP_ROLES, name);
			FormGroup role = new FormGroup(name, getResPrefix());
			role.setLabel(roleName);
			role.setTooltip(roleTooltip(roleName));
			roles.addMember(role);

			List<HTMLTemplateFragment> capabilities = new ArrayList<>();
			addCapabilityFields(role, prefix, roleName, capabilities);
			List<HTMLTemplateFragment> content = new ArrayList<>();
			content.add(contentBox(htmlTemplate(note(I18NConstants.CAPABILITY_STATES_NOTE))));
			content.add(columns(capabilities));
			addToolEntryGroup(role, prefix, roleName, content);

			CommandField remove = button(role, FIELD_REMOVE, I18NConstants.COMMAND_REMOVE_ROLE, () -> {
				_draft.removeRole(roleName);
				return rebuild();
			});
			inner.add(contentBox(section(role, content, "role." + roleName, r > 0,
				() -> roleSummary(roleName)).setMenu(Menu.create(remove))));
		}
		addNewRoleGroup(roles, inner);

		sections.add(section(roles, inner, "roles", false, this::rolesSummary));
	}

	/**
	 * What the collapsed section of the roles says: which roles the policy configures.
	 *
	 * <p>
	 * The names rather than a count, because the names are the content and a count only says how much
	 * of it there is. Where they no longer fit into a title, an honest count beats a truncated list.
	 * </p>
	 */
	private String rolesSummary() {
		List<String> roleNames = _draft.roleNames();
		String names = String.join(", ", roleNames);
		if (names.length() <= ROLE_NAMES_SUMMARY_LENGTH) {
			return names;
		}
		return resources().getString(
			I18NConstants.SECTION_ROLES_SUMMARY__COUNT.fill(Integer.valueOf(roleNames.size())));
	}

	/** What a collapsed role section says: how much it grants, forbids and lists. */
	private String roleSummary(String roleName) {
		int granted = 0;
		for (String group : _draft.grantableGroups()) {
			if (_draft.isGranted(roleName, group)) {
				granted++;
			}
		}
		int entries = _draft.toolEntries(roleName, false).size()
			+ _draft.toolEntries(roleName, true).size();
		return resources().getString(I18NConstants.ROLE_SUMMARY__GRANTED_DENIED_ENTRIES.fill(
			Integer.valueOf(granted), Integer.valueOf(_draft.deniedGroups(roleName).size()),
			Integer.valueOf(entries)));
	}

	/** One tri-state per capability group, plus the two that are granted whatever the policy says. */
	private void addCapabilityFields(FormGroup role, String prefix, String roleName,
			List<HTMLTemplateFragment> content) {
		for (String group : PolicyDraft.implicitGroups()) {
			BooleanField field = FormFactory.newBooleanField("implicit_" + technical(group));
			field.initializeField(Boolean.TRUE);
			field.setLabel(ToolAccessLabels.groupLabel(group));
			field.setTooltip(implicitTooltip(group));
			// The same control as the rows below it, so that a row nobody decides about does not look
			// like a different kind of question.
			field.setControlProvider(TRISTATE);
			field.setImmutable(true);
			field.set(AbstractMember.FIRST_COLUMN_WIDTH, CAPABILITY_LABEL_WIDTH);
			role.addMember(field);
			content.add(capabilityRow(path(prefix, field.getName()), group, null, null));
		}

		for (String group : _draft.grantableGroups()) {
			BooleanField field = FormFactory.newBooleanField(FIELD_CAPABILITY_PREFIX + technical(group));
			field.initializeField(fieldValue(_draft.capabilityState(roleName, group)));
			field.setLabel(ToolAccessLabels.groupLabel(group));
			field.setTooltip(capabilityTooltip(group));
			field.setControlProvider(TRISTATE);
			field.addValueListener((changed, oldValue, newValue) -> {
				if (_syncing) {
					return;
				}
				_draft.setCapabilityState(roleName, group, capabilityState(newValue));
				refreshNotes();
			});
			field.set(AbstractMember.FIRST_COLUMN_WIDTH, CAPABILITY_LABEL_WIDTH);
			role.addMember(field);
			content.add(capabilityRow(path(prefix, field.getName()), group, roleName, prefix));
		}
	}

	/** How the three states of a capability are stored in the field the tri-state control renders. */
	private static Boolean fieldValue(CapabilityState state) {
		switch (state) {
			case GRANTED:
				return Boolean.TRUE;
			case DENIED:
				return Boolean.FALSE;
			default:
				return null;
		}
	}

	/** The same the other way round; anything but the two booleans is "this role says nothing". */
	private static CapabilityState capabilityState(Object value) {
		if (Boolean.TRUE.equals(value)) {
			return CapabilityState.GRANTED;
		}
		return Boolean.FALSE.equals(value) ? CapabilityState.DENIED : CapabilityState.ABSENT;
	}

	/**
	 * One capability: the label with its risk marker, the tri-state, and what else is true about it.
	 *
	 * @param roleName
	 *        The role the row belongs to, or <code>null</code> for a capability nobody decides about.
	 */
	private HTMLTemplateFragment capabilityRow(String fieldPath, String group, String roleName,
			String prefix) {
		String risk = ToolAccessLabels.risk(group);
		HTMLTemplateFragment marker = risk.isEmpty() ? empty()
			: htmlTemplate(Fragments.span("aiSecurityRisk", Fragments.text(risk)));
		HTMLTemplateFragment note = roleName == null ? empty()
			: htmlTemplate(dynamic(prefix + "." + group, () -> capabilityNote(roleName, group)));
		return member(fieldPath,
			descriptionBox(fragment(Templates.label(), marker), fragment(Templates.self(), note)));
	}

	/**
	 * What is true about one capability of one role beyond the state the control shows.
	 *
	 * <p>
	 * The one remaining note is about a <em>second</em> setting, which is why it cannot be a tooltip:
	 * what the other configured roles grant is written elsewhere and changes while this section is
	 * open. Everything that is true of a state as such is said once, by the legend above the rows, and
	 * everything that is true of a capability as such is in the row's own tooltip. Neither is repeated
	 * per row: a note that is always there is read as decoration and stops being read at all.
	 * </p>
	 */
	private HTMLFragment capabilityNote(String roleName, String group) {
		if (_draft.capabilityState(roleName, group) != CapabilityState.ABSENT) {
			return Fragments.empty();
		}
		return absentNote(roleName, group);
	}

	/**
	 * Who else grants a capability this role says nothing about, which is the question the empty state
	 * raises and the reason it is not the same as a prohibition.
	 *
	 * <p>
	 * Only <em>another role</em> is worth saying. Nobody else granting it is the ordinary case, and the
	 * basic permissions grant the same thing to every row of every role, so both would be a note that is
	 * always there. What is left appears rarely and only where the row would otherwise be misread as a
	 * refusal.
	 * </p>
	 */
	private HTMLFragment absentNote(String roleName, String group) {
		ToolAccessRule rule = _draft.grantedElsewhere(roleName, group);
		if (rule == null || rule.kind() != ToolAccessRule.Kind.ROLE_GRANT) {
			return Fragments.empty();
		}
		return Fragments.span("aiSecurityBaselineState",
			Fragments.message(I18NConstants.ROW_GRANTED_BY_ROLE__ROLE.fill(rule.roleName())));
	}

	/** The entries of one role that name single tools rather than a whole capability. */
	private void addToolEntryGroup(FormGroup role, String prefix, String roleName,
			List<HTMLTemplateFragment> content) {
		FormGroup entries = new FormGroup(GROUP_TOOL_ENTRIES, getResPrefix());
		entries.setLabel(resources().getString(I18NConstants.SECTION_TOOL_ENTRIES));
		entries.setTooltip(resources().getString(I18NConstants.SECTION_TOOL_ENTRIES.tooltip()));
		role.addMember(entries);

		String path = path(prefix, GROUP_TOOL_ENTRIES);
		List<HTMLTemplateFragment> rows = new ArrayList<>();
		int index = 0;
		int count = 0;
		for (boolean denies : List.of(Boolean.FALSE, Boolean.TRUE)) {
			for (Grant entry : _draft.toolEntries(roleName, denies)) {
				rows.add(entryRow(entries, path, roleName, denies, entry, index++));
				count++;
			}
		}
		button(entries, "addGrant", I18NConstants.COMMAND_ADD_GRANT_ENTRY, () -> {
			_draft.addToolEntry(roleName, false);
			return rebuild();
		});
		button(entries, "addDeny", I18NConstants.COMMAND_ADD_DENY_ENTRY, () -> {
			_draft.addToolEntry(roleName, true);
			return rebuild();
		});
		rows.add(contentBox(fragment(
			member(path(path, "addGrant")),
			member(path(path, "addDeny")))));

		int total = count;
		content.add(contentBox(section(entries, rows, "toolEntries." + roleName, true,
			() -> countSummary(total))));
	}

	/**
	 * One entry, rendered as the choice it is.
	 *
	 * <p>
	 * An entry names a whole capability or single tools, never both, so the picker that does not apply
	 * is hidden rather than shown empty.
	 * </p>
	 */
	private HTMLTemplateFragment entryRow(FormGroup entries, String entriesPath, String roleName,
			boolean denies, Grant entry, int index) {
		FormGroup block = new FormGroup("entry_" + index, getResPrefix());
		block.setLabel(resources().getString(
			denies ? I18NConstants.ENTRY_DENY : I18NConstants.ENTRY_GRANT));
		entries.addMember(block);

		SelectField kind = FormFactory.newSelectField(FIELD_CHOICE,
			List.of(EntryKind.values()), false, false, false, null);
		kind.setAsSingleSelection(
			PolicyDraft.namesWholeGroup(entry) ? EntryKind.GROUP : EntryKind.TOOLS);
		kind.setOptionLabelProvider(ENTRY_KIND_LABELS);
		kind.setLabel(resources().getString(I18NConstants.FIELD_ENTRY_KIND));
		kind.setTooltip(resources().getString(I18NConstants.FIELD_ENTRY_KIND.tooltip()));
		block.addMember(kind);

		SelectField group = groupField(FIELD_GROUPS, false, I18NConstants.FIELD_ENTRY_GROUP);
		group.setAsSingleSelection(entry.getGroup());
		group.addValueListener((changed, oldValue, newValue) -> {
			if (_syncing) {
				return;
			}
			entry.setGroup(single(newValue, ToolGroups.MODEL_READ));
			refreshNotes();
		});
		block.addMember(group);

		SelectField tools = toolField(FIELD_TOOLS, I18NConstants.FIELD_ENTRY_TOOLS);
		tools.setAsSelection(entry.getTools());
		tools.addValueListener((changed, oldValue, newValue) -> {
			if (_syncing) {
				return;
			}
			entry.setTools(new ArrayList<>(strings(newValue)));
			refreshNotes();
		});
		block.addMember(tools);

		kind.addValueListener((changed, oldValue, newValue) -> {
			boolean wholeGroup = single(newValue, EntryKind.TOOLS) == EntryKind.GROUP;
			PolicyDraft.setNamesGroup(entry, wholeGroup);
			syncing(() -> {
				group.setAsSingleSelection(entry.getGroup());
				tools.setAsSelection(entry.getTools());
			});
			group.setVisible(wholeGroup);
			tools.setVisible(!wholeGroup);
			refreshNotes();
		});
		boolean wholeGroup = PolicyDraft.namesWholeGroup(entry);
		group.setVisible(wholeGroup);
		tools.setVisible(!wholeGroup);

		button(block, FIELD_REMOVE, I18NConstants.COMMAND_REMOVE_ENTRY, () -> {
			_draft.removeEntry(roleName, denies, entry);
			return rebuild();
		});

		String prefix = path(entriesPath, block.getName());
		return div(css("aiSecurityRow"), contentBox(fragment(
			htmlTemplate(Fragments.span("aiSecurityEntryKind", Fragments.text(block.getLabel()))),
			htmlTemplate(dynamic("entry." + prefix, () -> entryNote(entry))),
			fieldBox(path(prefix, FIELD_CHOICE)),
			fieldBox(path(prefix, FIELD_GROUPS)),
			fieldBox(path(prefix, FIELD_TOOLS)),
			member(path(prefix, FIELD_REMOVE)))));
	}

	/** An entry that names nothing yet would be refused, and says so where it is authored. */
	private static HTMLFragment entryNote(Grant entry) {
		if (PolicyDraft.namesWholeGroup(entry) || !entry.getTools().isEmpty()) {
			return Fragments.empty();
		}
		return Fragments.span("aiSecurityWarning",
			Fragments.message(I18NConstants.ENTRY_NO_TOOL_CHOSEN));
	}

	// -------------------------------------------------------------- add a role

	private void addNewRoleGroup(FormGroup roles, List<HTMLTemplateFragment> sections) {
		FormGroup group = new FormGroup(GROUP_NEW_ROLE, getResPrefix());
		group.setLabel(resources().getString(I18NConstants.SECTION_ADD_ROLE));
		group.setTooltip(resources().getString(I18NConstants.SECTION_ADD_ROLE.tooltip()));
		roles.addMember(group);

		String prefix = path(GROUP_ROLES, GROUP_NEW_ROLE);

		SelectField role =
			FormFactory.newSelectField(FIELD_ROLE, offeredRoles(), false, false, false, null);
		role.setOptionLabelProvider(ROLE_LABELS);
		// The list is ordered by kind, and a selection field sorts by label unless told otherwise,
		// which would put the framework's own roles between the application's.
		role.setOptionComparator(BY_ROLE_KIND);
		role.setLabel(resources().getString(I18NConstants.FIELD_NEW_ROLE));
		role.setTooltip(resources().getString(I18NConstants.FIELD_NEW_ROLE.tooltip()));
		// Deliberately nothing preselected: with the application's own roles already configured, the
		// first option is a role of the framework, and a preselected one of those is a suggestion.
		role.addValueListener((changed, oldValue, newValue) -> refreshNotes());
		group.addMember(role);

		SelectField template = FormFactory.newSelectField(FIELD_TEMPLATE,
			List.of(RoleTemplate.values()), false, false, false, null);
		template.setAsSingleSelection(RoleTemplate.READ_ONLY);
		template.setOptionLabelProvider(TEMPLATE_LABELS);
		template.setLabel(resources().getString(I18NConstants.FIELD_TEMPLATE));
		template.setTooltip(resources().getString(I18NConstants.FIELD_TEMPLATE.tooltip()));
		group.addMember(template);

		button(group, FIELD_ADD, I18NConstants.COMMAND_ADD_ROLE, () -> {
			String roleName = single(role.getValue(), null);
			if (roleName != null) {
				_draft.addRole(roleName, single(template.getValue(), RoleTemplate.NONE));
			}
			return rebuild();
		});

		sections.add(contentBox(section(group, List.of(
			fieldBox(path(prefix, FIELD_ROLE)),
			contentBox(htmlTemplate(dynamic("roleNote", () -> roleNote(role)))),
			fieldBox(path(prefix, FIELD_TEMPLATE)),
			contentBox(member(path(prefix, FIELD_ADD)))),
			"addRole", true, () -> "")));
	}

	/** What is known about the role the picker currently shows. */
	private HTMLFragment roleNote(SelectField picker) {
		String roleName = single(picker.getValue(), null);
		if (roleName == null) {
			return Fragments.empty();
		}
		List<HTMLFragment> notes = new ArrayList<>();
		if (SecurityRoleKind.EVERY_ACCOUNT.equals(roleName)) {
			notes.add(Fragments.message(I18NConstants.ROLE_NOTE_EVERY_ACCOUNT));
		} else if (SecurityRoleKind.ADMINISTRATOR.equals(roleName)) {
			notes.add(Fragments.message(I18NConstants.ROLE_NOTE_ADMINISTRATOR));
		} else if (SecurityRoleKind.of(roleName) == SecurityRoleKind.INTERNAL) {
			notes.add(Fragments.message(I18NConstants.ROLE_NOTE_INTERNAL));
		}
		String description = roleDescription(roleName);
		if (description != null) {
			notes.add(Fragments.text(description));
		}
		if (notes.isEmpty()) {
			return Fragments.empty();
		}
		return Fragments.span("aiSecurityNote", Fragments.concat(fragments(notes)));
	}

	// ------------------------------------------------------- basic permissions

	private void addBaselineGroup(FormContext context, List<HTMLTemplateFragment> sections) {
		FormGroup group = new FormGroup(GROUP_BASELINE, getResPrefix());
		group.setLabel(resources().getString(I18NConstants.POLICY_BASELINE_HEADING));
		group.setTooltip(resources().getString(I18NConstants.POLICY_BASELINE_HEADING.tooltip()));
		context.addMember(group);

		SelectField groups = groupField(FIELD_GROUPS, true, I18NConstants.FIELD_BASELINE_GROUPS);
		groups.setAsSelection(new ArrayList<>(_draft.baselineGroups()));
		groups.addValueListener((changed, oldValue, newValue) -> {
			if (_syncing) {
				return;
			}
			_draft.setBaselineGroups(strings(newValue));
			refreshNotes();
		});

		SelectField tools = toolField(FIELD_TOOLS, I18NConstants.FIELD_BASELINE_TOOLS);
		tools.setAsSelection(new ArrayList<>(_draft.baselineTools()));
		tools.addValueListener((changed, oldValue, newValue) -> {
			if (_syncing) {
				return;
			}
			_draft.setBaselineTools(strings(newValue));
			refreshNotes();
		});

		SelectField choice = FormFactory.newSelectField(FIELD_CHOICE,
			List.of(BaselineChoice.values()), false, false, false, null);
		choice.setAsSingleSelection(_draft.baselineChoice());
		choice.setOptionLabelProvider(BASELINE_LABELS);
		choice.setLabel(resources().getString(I18NConstants.FIELD_BASELINE_CHOICE));
		choice.setTooltip(resources().getString(I18NConstants.FIELD_BASELINE_CHOICE.tooltip()));
		choice.addValueListener((changed, oldValue, newValue) -> {
			BaselineChoice state = single(newValue, BaselineChoice.DEFAULT);
			_draft.setBaselineChoice(state);
			syncing(() -> {
				groups.setAsSelection(new ArrayList<>(_draft.baselineGroups()));
				tools.setAsSelection(new ArrayList<>(_draft.baselineTools()));
			});
			showCustomList(groups, tools, state == BaselineChoice.CUSTOM);
			refreshNotes();
		});
		showCustomList(groups, tools, _draft.baselineChoice() == BaselineChoice.CUSTOM);

		group.addMember(choice);
		group.addMember(groups);
		group.addMember(tools);

		sections.add(section(group, List.of(
			fieldBox(path(GROUP_BASELINE, FIELD_CHOICE)),
			fieldBox(path(GROUP_BASELINE, FIELD_GROUPS)),
			fieldBox(path(GROUP_BASELINE, FIELD_TOOLS))),
			"baseline", false,
			() -> resources().getString(ToolAccessLabels.baselineChoice(_draft.baselineChoice()))));
	}

	// -------------------------------------------------------- unattended runs

	private void addBackgroundGroup(FormContext context, List<HTMLTemplateFragment> sections) {
		FormGroup group = new FormGroup(GROUP_BACKGROUND, getResPrefix());
		group.setLabel(resources().getString(I18NConstants.POLICY_BACKGROUND_HEADING));
		group.setTooltip(resources().getString(I18NConstants.POLICY_BACKGROUND_HEADING.tooltip()));
		context.addMember(group);

		SelectField groups = groupField(FIELD_GROUPS, true, I18NConstants.FIELD_BACKGROUND_GROUPS);
		groups.setAsSelection(new ArrayList<>(_draft.backgroundGroups()));
		groups.addValueListener((changed, oldValue, newValue) -> {
			if (_syncing) {
				return;
			}
			_draft.setBackgroundGroups(strings(newValue));
			refreshNotes();
		});

		SelectField tools = toolField(FIELD_TOOLS, I18NConstants.FIELD_BACKGROUND_TOOLS);
		tools.setAsSelection(new ArrayList<>(_draft.backgroundTools()));
		tools.addValueListener((changed, oldValue, newValue) -> {
			if (_syncing) {
				return;
			}
			_draft.setBackgroundTools(strings(newValue));
			refreshNotes();
		});

		SelectField choice = FormFactory.newSelectField(FIELD_CHOICE,
			List.of(BackgroundChoice.values()), false, false, false, null);
		choice.setAsSingleSelection(_draft.backgroundChoice());
		choice.setOptionLabelProvider(BACKGROUND_LABELS);
		choice.setLabel(resources().getString(I18NConstants.FIELD_BACKGROUND_CHOICE));
		choice.setTooltip(resources().getString(I18NConstants.FIELD_BACKGROUND_CHOICE.tooltip()));
		choice.addValueListener((changed, oldValue, newValue) -> {
			BackgroundChoice state = single(newValue, BackgroundChoice.UNDECLARED);
			_draft.setBackgroundChoice(state);
			syncing(() -> {
				groups.setAsSelection(new ArrayList<>(_draft.backgroundGroups()));
				tools.setAsSelection(new ArrayList<>(_draft.backgroundTools()));
			});
			showCustomList(groups, tools, state == BackgroundChoice.CUSTOM);
			refreshNotes();
		});
		showCustomList(groups, tools, _draft.backgroundChoice() == BackgroundChoice.CUSTOM);

		group.addMember(choice);
		group.addMember(groups);
		group.addMember(tools);

		sections.add(section(group, List.of(
			contentBox(htmlTemplate(note(I18NConstants.POLICY_BACKGROUND_NOTE))),
			fieldBox(path(GROUP_BACKGROUND, FIELD_CHOICE)),
			fieldBox(path(GROUP_BACKGROUND, FIELD_GROUPS)),
			fieldBox(path(GROUP_BACKGROUND, FIELD_TOOLS))),
			"background", false,
			() -> resources()
				.getString(ToolAccessLabels.backgroundChoice(_draft.backgroundChoice()))));
	}

	/** The two pickers of an own list are there exactly when an own list is what was chosen. */
	private static void showCustomList(SelectField groups, SelectField tools, boolean custom) {
		groups.setVisible(custom);
		tools.setVisible(custom);
	}

	// ------------------------------------------------------- group assignments

	/**
	 * Which tools the policy puts into which capability group.
	 *
	 * <p>
	 * A section of its own, because a group assignment says what a tool <em>is</em> rather than what a
	 * role may do. This is the only place the policy states it, so it is edited here and nowhere else.
	 * </p>
	 */
	private void addAssignmentGroup(FormContext context, List<HTMLTemplateFragment> sections) {
		FormGroup group = new FormGroup(GROUP_ASSIGNMENTS, getResPrefix());
		group.setLabel(resources().getString(I18NConstants.POLICY_TOOL_GROUPS_HEADING));
		group.setTooltip(resources().getString(I18NConstants.POLICY_TOOL_GROUPS_HEADING.tooltip()));
		context.addMember(group);

		List<HTMLTemplateFragment> rows = new ArrayList<>();
		rows.add(contentBox(htmlTemplate(note(I18NConstants.POLICY_TOOL_GROUPS_NOTE))));
		List<String> assigned = _draft.assignedGroups();
		if (assigned.isEmpty()) {
			rows.add(contentBox(htmlTemplate(note(I18NConstants.POLICY_TOOL_GROUPS_NONE))));
		}
		for (int a = 0; a < assigned.size(); a++) {
			rows.add(assignmentRow(group, assigned.get(a), a));
		}
		button(group, FIELD_ADD, I18NConstants.COMMAND_ADD_ASSIGNMENT, this::addAssignment);
		rows.add(contentBox(member(path(GROUP_ASSIGNMENTS, FIELD_ADD))));

		int count = assigned.size();
		sections.add(section(group, rows, "assignments", false, () -> countSummary(count)));
	}

	/**
	 * Starts an assignment for a capability group that has none yet.
	 *
	 * <p>
	 * With the first tool of the application already in it: an entry naming no tool assigns nothing and
	 * is refused as a policy that says nothing, so an empty row could not be applied.
	 * </p>
	 */
	private HandlerResult addAssignment() {
		String group = _draft.unassignedGroup();
		String tool = firstToolName();
		if (group == null || tool == null) {
			InfoService.showInfo(I18NConstants.ASSIGNMENT_NOTHING_TO_ADD);
			return HandlerResult.DEFAULT_RESULT;
		}
		_draft.setAssignedTools(group, List.of(tool));
		return rebuild();
	}

	private HTMLTemplateFragment assignmentRow(FormGroup group, String assignedGroup, int index) {
		FormGroup block = new FormGroup("assignment_" + index, getResPrefix());
		group.addMember(block);

		SelectField target = FormFactory.newSelectField(FIELD_GROUPS,
			_draft.assignableGroups(assignedGroup), false, false, false, null);
		target.setOptionLabelProvider(GROUP_LABELS);
		labelAndHelp(target, I18NConstants.FIELD_ASSIGNMENT_GROUP);
		target.setAsSingleSelection(assignedGroup);
		block.addMember(target);
		_assignmentGroups.add(target);

		SelectField tools = toolField(FIELD_TOOLS, I18NConstants.FIELD_ASSIGNMENT_TOOLS);
		tools.setAsSelection(_draft.assignedTools(assignedGroup));
		block.addMember(tools);

		// The capability group of an assignment is its key, so changing it moves the entry rather than
		// editing a value in place. The tools come from the field rather than from the draft, so that
		// an edit that has not been applied yet moves with it.
		target.addValueListener((changed, oldValue, newValue) -> {
			if (_syncing) {
				return;
			}
			String to = single(newValue, null);
			_draft.renameAssignment(single(oldValue, assignedGroup), to);
			_draft.setAssignedTools(to, strings(tools.getValue()));
			refreshAssignmentOptions();
			refreshNotes();
		});
		tools.addValueListener((changed, oldValue, newValue) -> {
			if (_syncing) {
				return;
			}
			_draft.setAssignedTools(single(target.getValue(), assignedGroup), strings(newValue));
			refreshNotes();
		});

		button(block, FIELD_REMOVE, I18NConstants.COMMAND_REMOVE_ASSIGNMENT, () -> {
			_draft.removeAssignment(single(target.getValue(), assignedGroup));
			return rebuild();
		});

		String prefix = path(GROUP_ASSIGNMENTS, block.getName());
		return div(css("aiSecurityRow"), contentBox(fragment(
			fieldBox(path(prefix, FIELD_GROUPS)),
			fieldBox(path(prefix, FIELD_TOOLS)),
			member(path(prefix, FIELD_REMOVE)))));
	}

	// ------------------------------------------------- the messages at the top

	/** The banner, the refusals and the warnings, in the order they have to be read. */
	private HTMLFragment messages(FormContext context) {
		CommandField restart =
			button(context, FIELD_RESTART, I18NConstants.COMMAND_RESTART, this::restart);
		return Fragments.div("aiSecurityMessages",
			dynamic("banner", () -> banner(restart)),
			note(I18NConstants.POLICY_INTRO),
			mayWrite() ? Fragments.empty() : note(I18NConstants.POLICY_READ_ONLY),
			dynamic("refusals", this::refusals),
			dynamic("warnings", this::warnings),
			dynamic("dirty", this::dirty));
	}

	/**
	 * The banner saying that what is saved is not what the application applies.
	 *
	 * <p>
	 * Together with the restart action next to it, this is the honest answer to "is what I am looking
	 * at live". The catalog and this tab report the saved policy; the effective-permissions view reports
	 * the running one, and until the service is restarted the two disagree.
	 * </p>
	 */
	private HTMLFragment banner(CommandField restart) {
		if (!PolicyDraft.differsFromRunningService(AiToolAccessService.savedPolicy())) {
			return Fragments.empty();
		}
		return Fragments.div("aiSecurityBanner",
			Fragments.span(Fragments.message(I18NConstants.BANNER_SAVED_NOT_EFFECTIVE)),
			new ButtonControl(restart, DefaultButtonRenderer.INSTANCE));
	}

	/** Why the last apply was refused, or nothing while none was. */
	private HTMLFragment refusals() {
		if (_refusals.isEmpty()) {
			return Fragments.empty();
		}
		return Fragments.div("aiSecurityRefusals",
			Fragments.p(Fragments.message(I18NConstants.REFUSALS_HEADING)),
			messageList(_refusals));
	}

	/**
	 * What the draft does that its author probably did not mean.
	 *
	 * <p>
	 * Coalesced, and computed from the whole policy: every one of these is about two settings at once,
	 * so none of them can be shown next to a single control.
	 * </p>
	 */
	private HTMLFragment warnings() {
		List<ResKey> messages = PolicyChecks.warnings(_draft.config(), rolesWithModelRights());
		if (messages.isEmpty()) {
			return Fragments.empty();
		}
		return Fragments.div("aiSecurityWarning",
			Fragments.p(Fragments.message(I18NConstants.WARNINGS_HEADING)),
			messageList(messages));
	}

	private HTMLFragment dirty() {
		return note(_draft.isModified() ? I18NConstants.DIRTY_UNSAVED : I18NConstants.DIRTY_NONE);
	}

	private static HTMLFragment messageList(List<ResKey> messages) {
		List<HTMLFragment> items = new ArrayList<>();
		for (ResKey message : messages) {
			items.add(Fragments.li(Fragments.message(message)));
		}
		return Fragments.ul(fragments(items));
	}

	/** The roles named in a Read, Write or Create grant of the model access rights. */
	private static Set<String> rolesWithModelRights() {
		if (!SecurityConfigurationService.Module.INSTANCE.isActive()) {
			return Set.of();
		}
		return AiToolAccessService.rolesWithModelRights(
			SecurityConfigurationService.Module.INSTANCE.getImplementationInstance().getConfig());
	}

	// ------------------------------------------------------ apply and discard

	/**
	 * Checks the draft and, if nothing would stop the next start, writes it.
	 *
	 * <p>
	 * The refusal is the whole point: the raw editor accepts a policy that makes the next application
	 * start fail, hours later and for somebody else.
	 * </p>
	 *
	 * @return Always successful; a refused policy leaves the reasons in the view.
	 */
	public HandlerResult applyPolicy() {
		_refusals = PolicyChecks.refusals(_draft.config(), ToolAccessPolicyComponent::roleExists);
		if (!_refusals.isEmpty()) {
			return rebuild();
		}
		PolicyStore.save(_draft.config());
		_draft = PolicyDraft.ofSavedPolicy();
		InfoService.showInfo(I18NConstants.APPLY_SAVED);
		return rebuild();
	}

	/**
	 * Throws the unsaved changes away and shows the saved policy again.
	 */
	public HandlerResult discardChanges() {
		_refusals = List.of();
		_draft = PolicyDraft.ofSavedPolicy();
		InfoService.showInfo(I18NConstants.DISCARDED);
		return rebuild();
	}

	private HandlerResult restart() {
		PolicyStore.restartService();
		_draft = PolicyDraft.ofSavedPolicy();
		InfoService.showInfo(I18NConstants.RESTART_DONE);
		return rebuild();
	}

	private static boolean roleExists(String roleName) {
		return BoundedRole.getRoleByName(PersistencyLayer.getKnowledgeBase(), roleName) != null;
	}

	// --------------------------------------------------------- small helpers

	/**
	 * One collapsible section, the framework's own group cell.
	 *
	 * @param group
	 *        The form group whose label and tooltip name and explain the section.
	 * @param content
	 *        What the section contains.
	 * @param key
	 *        Distinguishes the section in the personal configuration that remembers whether it is open.
	 * @param collapsed
	 *        Whether the section starts collapsed for a reader who has not touched it.
	 * @param summary
	 *        What the title says about the content, so that a collapsed section still answers something.
	 */
	private FieldSetBoxTemplate section(FormMember group, List<HTMLTemplateFragment> content,
			String key, boolean collapsed, Supplier<String> summary) {
		return fieldsetBoxDirect(
			htmlTemplate(Fragments.concat(
				new LabelControl(group),
				dynamic("summary." + key, () -> summaryFragment(summary)))),
			fragment(content),
			ConfigKey.part(this, key))
				.setInitiallyCollapsed(collapsed);
	}

	// ------------------------------------- agents, models and MCP servers

	/**
	 * What a role may <em>be</em>: which agents it runs, which models it chooses, which MCP servers it
	 * attaches.
	 *
	 * <p>
	 * One section per dimension, each with its default and a pair of lists per role. The three read
	 * alike because they decide alike - an allowlist, a refusal that wins, and a default for everybody
	 * the section does not name - and a single section holding three unrelated defaults would hide
	 * exactly the thing an administrator has to get right.
	 * </p>
	 */
	private void addRunAccessGroups(FormContext context, List<HTMLTemplateFragment> sections) {
		FormGroup runAccess = new FormGroup(GROUP_RUN_ACCESS, getResPrefix());
		runAccess.setLabel(resources().getString(I18NConstants.SECTION_RUN_ACCESS));
		runAccess.setTooltip(resources().getString(I18NConstants.SECTION_RUN_ACCESS.tooltip()));
		context.addMember(runAccess);

		List<HTMLTemplateFragment> inner = new ArrayList<>();
		for (Dimension dimension : Dimension.values()) {
			addSectionGroup(runAccess, dimension, inner);
		}
		sections.add(section(runAccess, inner, "runAccess", true, this::runAccessSummary));
	}

	/** One of the three sections: its default, and a pair of lists per configured role. */
	private void addSectionGroup(FormGroup parent, Dimension dimension,
			List<HTMLTemplateFragment> sections) {
		String name = GROUP_SECTION_PREFIX + dimension.name();
		String prefix = path(GROUP_RUN_ACCESS, name);
		FormGroup group = new FormGroup(name, getResPrefix());
		group.setLabel(ToolAccessLabels.dimensionLabel(dimension));
		parent.addMember(group);

		List<HTMLTemplateFragment> content = new ArrayList<>();
		SelectField defaultField = FormFactory.newSelectField(FIELD_SECTION_DEFAULT,
			PolicyDraft.sectionDefaults(dimension), false,
			List.of(_draft.sectionDefault(dimension)), false, false, null);
		defaultField.setOptionLabelProvider(MetaLabelProvider.INSTANCE);
		labelAndHelp(defaultField, I18NConstants.FIELD_SECTION_DEFAULT);
		defaultField.addValueListener((changed, oldValue, newValue) -> {
			if (!_syncing) {
				_draft.setSectionDefault(dimension, single(newValue, null));
			}
		});
		group.addMember(defaultField);
		content.add(fieldBox(path(prefix, FIELD_SECTION_DEFAULT)));

		List<String> roleNames = _draft.runRoleNames();
		if (roleNames.isEmpty()) {
			content.add(contentBox(htmlTemplate(note(I18NConstants.POLICY_NO_ROLES))));
		}
		for (int r = 0; r < roleNames.size(); r++) {
			String roleName = roleNames.get(r);
			content.add(restrictedField(group, prefix, dimension, roleName, r));
			content.add(namesField(group, prefix, dimension, roleName, r, false));
			content.add(namesField(group, prefix, dimension, roleName, r, true));
		}
		sections.add(contentBox(
			section(group, content, "runAccess." + dimension.name(), false,
				() -> sectionSummary(dimension))));
	}

	/**
	 * Whether one section names one role at all.
	 *
	 * <p>
	 * The switch that makes "this role may use none of them" writable: an entry with no names is what
	 * says it, and picking nothing in the two lists below cannot say it - there is nothing to pick.
	 * Switching it off returns the role's holders to the default of the section.
	 * </p>
	 */
	private HTMLTemplateFragment restrictedField(FormGroup group, String prefix, Dimension dimension,
			String roleName, int index) {
		String fieldName = FIELD_RESTRICTED_PREFIX + index;
		BooleanField field = FormFactory.newBooleanField(fieldName);
		field.initializeField(Boolean.valueOf(_draft.isRunRestricted(dimension, roleName)));
		field.setLabel(resources().getString(I18NConstants.FIELD_ROLE_RESTRICTED__ROLE.fill(roleName)));
		field.setTooltip(resources()
			.getString(I18NConstants.FIELD_ROLE_RESTRICTED__ROLE.fill(roleName).tooltip()));
		field.addValueListener((changed, oldValue, newValue) -> {
			if (_syncing) {
				return;
			}
			boolean restricted = Boolean.TRUE.equals(newValue);
			_draft.setRunRestricted(dimension, roleName, restricted);
			if (!restricted) {
				// Switching off drops the role's entry from the draft, names and all. Emptying the two
				// lists says so on the screen: they would otherwise go on showing names that are no
				// longer anywhere, and switching back on would silently start from nothing.
				syncing(() -> {
					clearSelection(group, FIELD_ALLOWED_PREFIX + index);
					clearSelection(group, FIELD_FORBIDDEN_PREFIX + index);
				});
			}
			refreshNotes();
		});
		group.addMember(field);
		return fieldBox(path(prefix, fieldName));
	}

	/** Empties one of the two name lists, if the form has it. */
	private static void clearSelection(FormGroup group, String fieldName) {
		if (group.hasMember(fieldName) && group.getMember(fieldName) instanceof SelectField names) {
			names.setAsSelection(List.of());
		}
	}

	/** One role's list of permitted - or forbidden - names in one section. */
	private HTMLTemplateFragment namesField(FormGroup group, String prefix, Dimension dimension,
			String roleName, int index, boolean denies) {
		String fieldName = (denies ? FIELD_FORBIDDEN_PREFIX : FIELD_ALLOWED_PREFIX) + index;
		SelectField field = FormFactory.newSelectField(fieldName, runOptions(dimension), true, false,
			false, null);
		field.setAsSelection(_draft.runNames(dimension, roleName, denies));
		field.setLabel(resources().getString((denies
			? I18NConstants.FIELD_ROLE_FORBIDDEN__ROLE
			: I18NConstants.FIELD_ROLE_ALLOWED__ROLE).fill(roleName)));
		field.setTooltip(resources().getString((denies
			? I18NConstants.FIELD_ROLE_FORBIDDEN__ROLE
			: I18NConstants.FIELD_ROLE_ALLOWED__ROLE).fill(roleName).tooltip()));
		field.addValueListener((changed, oldValue, newValue) -> {
			if (!_syncing) {
				_draft.setRunNames(dimension, roleName, denies, strings(newValue));
				refreshNotes();
			}
		});
		group.addMember(field);
		return fieldBox(path(prefix, fieldName));
	}

	/** Everything the application has of one dimension, plus whatever the policy already names. */
	private static List<String> runOptions(Dimension dimension) {
		switch (dimension) {
			case MODEL:
				return new AllModelNames().apply();
			case MCP_SERVER:
				return new AllMcpServerNames().apply();
			default:
				return new AllAgentNames().apply();
		}
	}

	/** What the collapsed run-access section says: the default of each of the three. */
	private String runAccessSummary() {
		List<String> parts = new ArrayList<>();
		for (Dimension dimension : Dimension.values()) {
			parts.add(MetaLabelProvider.INSTANCE.getLabel(_draft.sectionDefault(dimension)));
		}
		return String.join(" · ", parts);
	}

	/**
	 * What a collapsed section says: its default, and how many roles it restricts.
	 *
	 * <p>
	 * Counts the roles the section <em>names</em>, which is what restricts them - a role entry with
	 * no names restricts its holders to nothing at all, and counting names would report that role as
	 * unrestricted while the boot log counts it.
	 * </p>
	 */
	private String sectionSummary(Dimension dimension) {
		int roles = 0;
		for (String roleName : _draft.runRoleNames()) {
			if (_draft.isRunRestricted(dimension, roleName)) {
				roles++;
			}
		}
		return resources().getString(I18NConstants.SECTION_RUN_SUMMARY__DEFAULT_ROLES.fill(
			MetaLabelProvider.INSTANCE.getLabel(_draft.sectionDefault(dimension)),
			Integer.valueOf(roles)));
	}

	/** Offers every assignment the capability groups no other assignment holds, plus its own. */
	private void refreshAssignmentOptions() {
		for (SelectField picker : _assignmentGroups) {
			String own = single(picker.getValue(), null);
			picker.setOptions(_draft.assignableGroups(own));
		}
	}

	/** The engine's own two-column layout, so that eleven capabilities are six rows rather than eleven. */
	private static HTMLTemplateFragment columns(List<HTMLTemplateFragment> content) {
		return div(css(Columns.TWO.appendColsCSSto(ReactiveFormCSS.RF_COLUMNS_LAYOUT)),
			fragment(content));
	}

	private static HTMLFragment summaryFragment(Supplier<String> summary) {
		String text = summary.get();
		return text.isEmpty() ? Fragments.empty()
			: Fragments.span("aiSecuritySummary", Fragments.text(text));
	}

	/** How many things a collapsed section holds. */
	private static String countSummary(int count) {
		return resources().getString(I18NConstants.SECTION_COUNT__COUNT.fill(Integer.valueOf(count)));
	}

	/** A block of the view that is recomputed and repainted when a value below it changes. */
	private DynamicFragment dynamic(String key, Supplier<HTMLFragment> content) {
		DynamicFragment fragment = new DynamicFragment(content);
		_dynamic.put(key, fragment);
		return fragment;
	}

	private static HTMLFragment note(ResKey message) {
		return Fragments.p("aiSecurityNote", Fragments.message(message));
	}

	private CommandField button(FormContainer parent, String name, ResKey label,
			Supplier<HandlerResult> action) {
		CommandField field = FormFactory.newCommandField(name, (Command) context -> action.get());
		field.setControlProvider(BUTTONS);
		field.setLabel(resources().getString(label));
		String tooltip = resources().getString(label.tooltipOptional());
		if (tooltip != null) {
			field.setTooltip(tooltip);
		}
		parent.addMember(field);
		return field;
	}

	private SelectField groupField(String fieldName, boolean multiple, ResKey label) {
		SelectField field =
			FormFactory.newSelectField(fieldName, _draft.grantableGroups(), multiple, false, false,
				null);
		field.setOptionLabelProvider(GROUP_LABELS);
		labelAndHelp(field, label);
		return field;
	}

	private static SelectField toolField(String fieldName, ResKey label) {
		SelectField field =
			FormFactory.newSelectField(fieldName, allToolNames(), true, false, false, null);
		labelAndHelp(field, label);
		return field;
	}

	private static void labelAndHelp(FormMember field, ResKey label) {
		field.setLabel(resources().getString(label));
		String tooltip = resources().getString(label.tooltipOptional());
		if (tooltip != null) {
			field.setTooltip(tooltip);
		}
	}

	/**
	 * The roles the picker offers: the ones the application has and the ones the policy already names,
	 * minus the ones that are already configured.
	 */
	private List<String> offeredRoles() {
		List<String> configured = _draft.roleNames();
		List<String> result = new ArrayList<>();
		for (String roleName : existingRoleNames()) {
			if (!configured.contains(roleName)) {
				result.add(roleName);
			}
		}
		// Application roles first, then the framework's own, then the ones a grant does nothing for.
		result.sort(BY_ROLE_KIND_NAME);
		return result;
	}

	private static List<String> existingRoleNames() {
		List<String> result = new ArrayList<>();
		if (PersistencyLayer.Module.INSTANCE.isActive()) {
			for (BoundedRole role : BoundedRole.getAll()) {
				if (role.getName() != null && !role.getName().isBlank()) {
					result.add(role.getName());
				}
			}
		}
		return result;
	}

	/** The description an administrator gave the role, or <code>null</code> if there is none. */
	private static String roleDescription(String roleName) {
		if (!PersistencyLayer.Module.INSTANCE.isActive()) {
			return null;
		}
		BoundedRole role = BoundedRole.getRoleByName(PersistencyLayer.getKnowledgeBase(), roleName);
		if (role == null) {
			return null;
		}
		Object description = role.getValue(BoundedRole.ATTRIBUTE_DESCRIPTION);
		if (!(description instanceof String text) || text.isBlank()) {
			return null;
		}
		return text;
	}

	/** What kind of role this is, and what its administrator wrote about it. */
	private static String roleTooltip(String roleName) {
		StringBuilder result = new StringBuilder();
		result.append(resources().getString(ToolAccessLabels.roleKind(SecurityRoleKind.of(roleName))));
		String description = roleDescription(roleName);
		if (description != null) {
			result.append(". ").append(description);
		}
		return result.toString();
	}

	/** What granting a capability lets an agent do. */
	private static String capabilityTooltip(String group) {
		String sentence = ToolAccessLabels.groupTooltip(group);
		return sentence == null ? ToolAccessLabels.groupLabel(group) : sentence;
	}

	/** The same, plus why the row has no state to choose. */
	private static String implicitTooltip(String group) {
		return capabilityTooltip(group) + " " + resources().getString(I18NConstants.ROW_ALWAYS_GRANTED);
	}

	/** The technical group name as a field name: a dot would read as a path into a sub-group. */
	private static String technical(String group) {
		return group.replace('.', '_');
	}

	private static String firstToolName() {
		List<String> tools = allToolNames();
		return tools.isEmpty() ? null : tools.get(0);
	}

	private static List<String> allToolNames() {
		return new AllToolNames().apply();
	}

	private static HTMLFragment[] fragments(List<HTMLFragment> content) {
		return content.toArray(new HTMLFragment[content.size()]);
	}

	@SuppressWarnings("unchecked")
	private static <T> T single(Object value, T fallback) {
		if (value instanceof List<?> list && !list.isEmpty()) {
			return (T) list.get(0);
		}
		return fallback;
	}

	@SuppressWarnings("unchecked")
	private static List<String> strings(Object value) {
		return value instanceof List<?> list ? (List<String>) list : List.of();
	}

	private static Resources resources() {
		return Resources.getInstance();
	}

	/**
	 * The framework's own tri-state for a boolean that may be unset, taken as it is.
	 *
	 * <p>
	 * A plain {@link BooleanField} renders as an HTML checkbox, which has two states and shows an unset
	 * value as unticked. This is the control the engine ships for the three-valued case, resettable so
	 * that a click reaches all three.
	 * </p>
	 */
	private static final ControlProvider TRISTATE = TristateControlProvider.INSTANCE_RESETTABLE;

	/** Renders a command as a button carrying its label, not as a link nobody sees. */
	private static final ControlProvider BUTTONS =
		(model, style) -> new ButtonControl((CommandField) model, DefaultButtonRenderer.INSTANCE);

	/** How the two halves of an either-or entry are named. */
	private static final LabelProvider ENTRY_KIND_LABELS = model -> model == EntryKind.GROUP
		? resources().getString(I18NConstants.ENTRY_KIND_GROUP)
		: resources().getString(I18NConstants.ENTRY_KIND_TOOLS);

	/** How the three states of the basic permissions are named. */
	private static final LabelProvider BASELINE_LABELS = model -> model instanceof BaselineChoice choice
		? resources().getString(ToolAccessLabels.baselineChoice(choice)) : String.valueOf(model);

	/** How the three states of the limit for unattended runs are named. */
	private static final LabelProvider BACKGROUND_LABELS =
		model -> model instanceof BackgroundChoice choice
			? resources().getString(ToolAccessLabels.backgroundChoice(choice)) : String.valueOf(model);

	/** How the templates of a new role entry are named. */
	private static final LabelProvider TEMPLATE_LABELS = model -> model instanceof RoleTemplate template
		? resources().getString(ToolAccessLabels.roleTemplate(template)) : String.valueOf(model);

	/** How a capability group is named: display name and technical name, as everywhere else. */
	private static final LabelProvider GROUP_LABELS = model -> model instanceof String group
		? ToolAccessLabels.groupLabel(group) : String.valueOf(model);

	/** The order the role picker offers its options in. */
	private static final Comparator<String> BY_ROLE_KIND_NAME = (left, right) -> {
		int byKind = SecurityRoleKind.of(left).ordinal() - SecurityRoleKind.of(right).ordinal();
		return byKind != 0 ? byKind : left.compareToIgnoreCase(right);
	};

	/** The same order, for the selection field, which sorts by label unless told otherwise. */
	private static final Comparator<Object> BY_ROLE_KIND =
		(left, right) -> BY_ROLE_KIND_NAME.compare(String.valueOf(left), String.valueOf(right));

	/**
	 * How a role is offered: its name, plus what kind of role it is where that is not obvious.
	 */
	private static final LabelProvider ROLE_LABELS = model -> {
		if (!(model instanceof String roleName)) {
			return String.valueOf(model);
		}
		SecurityRoleKind kind = SecurityRoleKind.of(roleName);
		if (kind == SecurityRoleKind.APPLICATION) {
			return roleName;
		}
		return roleName + " (" + resources().getString(ToolAccessLabels.roleKind(kind)) + ")";
	};

	/**
	 * A block of the view that is recomputed and repainted when a value below it changes.
	 *
	 * <p>
	 * Needed because the surrounding template is written once: a plain fragment has no identity in the
	 * page and therefore cannot be replaced. Only the blocks that actually depend on a value carry one.
	 * </p>
	 */
	private static final class DynamicFragment extends AbstractConstantControl {

		private final Supplier<HTMLFragment> _content;

		DynamicFragment(Supplier<HTMLFragment> content) {
			_content = content;
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(SPAN);
			writeControlAttributes(context, out);
			out.endBeginTag();
			_content.get().write(context, out);
			out.endTag(SPAN);
		}

		@Override
		protected String getTypeCssClass() {
			return "aiSecurityDynamic";
		}
	}

}
