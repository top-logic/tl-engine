/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;

/**
 * I18N constants of the "Agent-Sicherheit" administration view.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Provenance: the capability is granted to everyone without being configured.
	 *
	 * @en Granted implicitly, not withholdable
	 * @de Implizit gewährt, nicht entziehbar
	 */
	public static ResKey RULE_IMPLICIT_BASELINE;

	/**
	 * Provenance: the shipped read-only baseline applies because the policy declares none.
	 *
	 * @en Default basic permissions, because the policy declares none
	 * @de Standard-Grundberechtigung, weil die Richtlinie keine eigene festlegt
	 */
	public static ResKey RULE_DEFAULT_BASELINE;

	/**
	 * Provenance: an entry of the policy's declared baseline, which reaches every account.
	 *
	 * @en Basic permissions, granted to every account
	 * @de Grundberechtigung, für jedes Konto gewährt
	 */
	public static ResKey RULE_BASELINE;

	/**
	 * Provenance: a grant of the named role.
	 *
	 * @en Granted by role {0}
	 * @de Berechtigung der Rolle {0}
	 */
	public static ResKey1 RULE_ROLE_GRANT__ROLE;

	/**
	 * Provenance: a grant of the named role that names this single tool instead of a group.
	 *
	 * @en Granted by role {0} for this single tool
	 * @de Berechtigung der Rolle {0} für dieses einzelne Tool
	 */
	public static ResKey1 RULE_ROLE_GRANT_TOOL__ROLE;

	/**
	 * Provenance: an explicit deny of the named role, which wins over every grant.
	 *
	 * @en Forbidden by role {0}. A prohibition always wins.
	 * @de Verbot der Rolle {0}. Ein Verbot gewinnt immer.
	 */
	public static ResKey1 RULE_ROLE_DENY__ROLE;

	/**
	 * Provenance: the background allowlist caps the run.
	 *
	 * @en Capped by the background allowlist
	 * @de Durch die Hintergrund-Zulassungsliste begrenzt
	 */
	public static ResKey RULE_BACKGROUND_CEILING;

	/**
	 * Provenance: the account is an administrator and bypasses the tool layer.
	 *
	 * @en Administrator, bypasses the tool layer
	 * @de Administrator, umgeht die Tool-Ebene
	 */
	public static ResKey RULE_ADMIN_BYPASS;

	/**
	 * Provenance: the tool belongs to no capability group and is therefore denied to everyone.
	 *
	 * @en In no group and granted by name nowhere, so denied to everyone
	 * @de In keiner Gruppe und nirgends namentlich gewährt, daher für alle gesperrt
	 */
	public static ResKey RULE_UNCLASSIFIED;

	/**
	 * Provenance: nothing in the policy grants the capability.
	 *
	 * @en No role grants it
	 * @de Keine Rolle gewährt es
	 */
	public static ResKey RULE_NOT_GRANTED;

	/**
	 * Names the implicit baseline inside a sentence about another rule.
	 *
	 * @en The implicit basic permissions
	 * @de Die implizite Grundberechtigung
	 */
	public static ResKey SUBJECT_IMPLICIT_BASELINE;

	/**
	 * Names the shipped default baseline inside a sentence about another rule.
	 *
	 * @en The default basic permissions
	 * @de Die Standard-Grundberechtigung
	 */
	public static ResKey SUBJECT_DEFAULT_BASELINE;

	/**
	 * Names the declared baseline inside a sentence about another rule.
	 *
	 * @en The basic permissions
	 * @de Die Grundberechtigung
	 */
	public static ResKey SUBJECT_BASELINE;

	/**
	 * Names a role inside a sentence about another rule.
	 *
	 * @en Role {0}
	 * @de Die Rolle {0}
	 */
	public static ResKey1 SUBJECT_ROLE__ROLE;

	/**
	 * Names the administrator status inside a sentence about another rule.
	 *
	 * @en Administrator status
	 * @de Der Administratorstatus
	 */
	public static ResKey SUBJECT_ADMIN;

	/**
	 * What the background allowlist took away, appended to the ceiling's own sentence.
	 *
	 * @en {0} grants it, but a run nobody watches may not use it.
	 * @de {0} gewährt es, aber ein unbeaufsichtigter Lauf darf es nicht.
	 */
	public static ResKey1 CAPPED_ALTHOUGH_GRANTED__SUBJECT;

	/**
	 * What a deny took away, appended to the deny's own sentence.
	 *
	 * @en {0} grants it.
	 * @de {0} gewährt es.
	 */
	public static ResKey1 DENIED_ALTHOUGH_GRANTED__SUBJECT;

	/**
	 * What the administrator bypass covered up, appended to its own sentence.
	 *
	 * @en The policy itself says: {0}.
	 * @de Die Richtlinie selbst sagt: {0}.
	 */
	public static ResKey1 ALLOWED_ALTHOUGH_POLICY__RULE;

	/**
	 * The extra price of changing data through the TL-Script tool, where the account can pay it.
	 *
	 * @en A change with changeData (anything but a dry run) additionally costs data.write, which this account holds
	 * @de Eine Änderung mit changeData (alles außer einem Probelauf) kostet zusätzlich data.write, und das hält dieses Konto
	 */
	public static ResKey SCRIPT_COMMIT_ALLOWED;

	/**
	 * The extra price of changing data through the TL-Script tool, where the account cannot pay it.
	 *
	 * @en A change with changeData (anything but a dry run) additionally costs data.write, which is denied here
	 * @de Eine Änderung mit changeData (alles außer einem Probelauf) kostet zusätzlich data.write, und das ist hier verweigert
	 */
	public static ResKey SCRIPT_COMMIT_DENIED;

	/**
	 * Verdict word for a capability the account may use.
	 *
	 * @en Allowed
	 * @de Gewährt
	 */
	public static ResKey VERDICT_ALLOWED;

	/**
	 * Verdict word for a capability the account may not use.
	 *
	 * @en Denied
	 * @de Verweigert
	 */
	public static ResKey VERDICT_DENIED;

	/**
	 * Group source: the tool declares its capability group itself, in its own code.
	 *
	 * @en declared by the tool
	 * @de vom Tool deklariert
	 */
	public static ResKey GROUP_SOURCE_DECLARED;

	/**
	 * Group source: a group assignment of the policy puts the tool into its group.
	 *
	 * @en assigned by the policy
	 * @de per Richtlinie zugeordnet
	 */
	public static ResKey GROUP_SOURCE_CONFIGURED;

	/**
	 * Group source: nothing classified the tool, which is why nobody but an administrator reaches it.
	 *
	 * @en assigned nowhere, denied to everyone
	 * @de nirgends zugeordnet, für alle gesperrt
	 */
	public static ResKey GROUP_SOURCE_NONE;

	/**
	 * The capability group of a tool that has none.
	 *
	 * @en NO GROUP
	 * @de KEINE GRUPPE
	 */
	public static ResKey GROUP_NONE;

	/**
	 * Risk marker of a capability whose tools can express any other capability.
	 *
	 * @en privileged
	 * @de privilegiert
	 */
	public static ResKey RISK_PRIVILEGED;

	/**
	 * Risk marker of a capability whose tools send data out of the application.
	 *
	 * @en sends data outward
	 * @de sendet Daten nach außen
	 */
	public static ResKey RISK_EGRESS;

	/**
	 * Risk marker of a capability every account holds whether or not it is configured.
	 *
	 * @en granted implicitly
	 * @de implizit gewährt
	 */
	public static ResKey RISK_IMPLICIT;

	/**
	 * Reachability of a tool that every account reaches through the baseline.
	 *
	 * @en everyone (baseline)
	 * @de alle (Grundberechtigung)
	 */
	public static ResKey REACHABLE_EVERYONE;

	/**
	 * Reachability of a tool that no configured role and no baseline reaches.
	 *
	 * @en nobody
	 * @de niemand
	 */
	public static ResKey REACHABLE_NOBODY;

	/**
	 * The run mode with a user who can answer an approval prompt.
	 *
	 * @en interactive
	 * @de interaktiv
	 */
	public static ResKey MODE_INTERACTIVE;

	/**
	 * The run mode with nobody to answer an approval prompt.
	 *
	 * @en background (unattended)
	 * @de Hintergrund (unbeaufsichtigt)
	 */
	public static ResKey MODE_BACKGROUND;

	/**
	 * How a hypothetical role is offered in the account picker.
	 *
	 * @en Anybody holding {0}
	 * @de Wer {0} hält
	 */
	public static ResKey1 SUBJECT_ROLE_OPTION__ROLE;

	/**
	 * The roles the chosen account holds on the security root, shown next to the picker.
	 *
	 * @en Roles on the security root: {0}
	 * @de Rollen auf der Sicherheits-Wurzel: {0}
	 */
	public static ResKey1 HELD_ROLES__ROLES;

	/**
	 * Shown for an account that holds no role at all beyond what every account holds.
	 *
	 * @en No role on the security root. Only the basic permissions apply.
	 * @de Keine Rolle auf der Sicherheits-Wurzel. Es gilt nur die Grundberechtigung.
	 */
	public static ResKey HELD_ROLES_NONE;

	/**
	 * The banner an administrator account needs, because its rows prove nothing about the policy.
	 *
	 * @en Administrators bypass the tool layer, so {0} reaches every tool. Check the policy with a
	 *     non-administrator account.
	 * @de Administratoren umgehen die Tool-Ebene, daher erreicht {0} jedes Tool. Prüfen Sie die Richtlinie
	 *     mit einem Konto ohne Administratorrechte.
	 */
	public static ResKey1 ADMIN_BANNER__ACCOUNT;

	/**
	 * The sentence that keeps the view from being read as an answer about data.
	 *
	 * @en This view shows the tool layer only. Which data an allowed call then sees is decided by the
	 *     model access rights.
	 * @de Diese Ansicht zeigt nur die Tool-Ebene. Welche Daten ein erlaubter Aufruf dann sieht,
	 *     entscheiden die Modellrechte.
	 */
	public static ResKey LAYER_ONE_ONLY_NOTE;

	/**
	 * What an unattended run is, where the mode toggle is.
	 *
	 * @en An unattended run has nobody to answer an approval prompt, so every call counts as approved. The
	 *     background allowlist is what caps it, administrators included.
	 * @de Ein unbeaufsichtigter Lauf hat niemanden, der eine Bestätigung beantworten kann, daher gilt
	 *     jeder Aufruf als genehmigt. Begrenzt wird er durch die Hintergrund-Zulassungsliste, auch für
	 *     Administratoren.
	 */
	public static ResKey BACKGROUND_MODE_NOTE;

	/**
	 * The cross-layer warning about a role with data capabilities and no model access rights.
	 *
	 * @en Role {0} holds {1}, but appears in no Read, Write or Create grant of the model access
	 *     rights: its data tools will see nothing and change nothing, and a refused read comes back
	 *     empty rather than as an error.
	 * @de Die Rolle {0} hält {1}, kommt aber in keinem Read-, Write- oder Create-Grant der
	 *     Modellrechte vor: Ihre Daten-Tools sehen nichts und ändern nichts, und eine
	 *     verweigerte Leseoperation liefert ein leeres Ergebnis statt eines Fehlers.
	 */
	public static ResKey2 WARN_DATA_ROLE_WITHOUT_MODEL_RIGHTS__ROLE_GROUPS;

	/**
	 * The cross-layer warning about a role with data capabilities and no model access rights, in an
	 * application whose model access rights mark types or modules as without security.
	 *
	 * @en Role {0} holds {1}, but appears in no Read, Write or Create grant of the model access
	 *     rights: its data tools reach only what is marked without security ({2}) and see nothing
	 *     and change nothing elsewhere, and a refused read comes back empty rather than as an
	 *     error.
	 * @de Die Rolle {0} hält {1}, kommt aber in keinem Read-, Write- oder Create-Grant der
	 *     Modellrechte vor: Ihre Daten-Tools erreichen nur, was ohne Sicherheit markiert ist ({2}),
	 *     und sehen und ändern sonst nichts, und eine verweigerte Leseoperation liefert ein leeres
	 *     Ergebnis statt eines Fehlers.
	 */
	public static ResKey3 WARN_DATA_ROLE_ONLY_WITHOUT_SECURITY__ROLE_GROUPS_PARTS;

	/**
	 * The cross-layer warning about a role nobody can hold where tool access reads roles.
	 *
	 * @en Role {0} is granted tools, but no role rule of this application can produce it on the security
	 *     root. Its permissions reach nobody.
	 * @de Der Rolle {0} sind Tools gewährt, aber keine Rollenregel dieser Anwendung kann sie auf der
	 *     Sicherheits-Wurzel erzeugen. Ihre Berechtigungen erreichen niemanden.
	 */
	public static ResKey1 WARN_ROLE_UNREACHABLE_ON_ROOT__ROLE;

	/**
	 * Shown instead of the rows while the tool access service is not running.
	 *
	 * @en The agent tool access service is not running, so there is no policy to report on.
	 * @de Der Zugangsdienst für KI-Tools läuft nicht, daher gibt es keine Richtlinie zu berichten.
	 */
	public static ResKey SERVICE_NOT_RUNNING;

	/**
	 * Baseline state: the shipped read-only default applies.
	 *
	 * @en Standard: read the model and the data
	 * @de Standard: Modell und Daten lesen
	 * @tooltip No element is written, so every account may read the model and the business data.
	 */
	public static ResKey BASELINE_DEFAULT;

	/**
	 * Baseline state: nothing is granted to everyone.
	 *
	 * @en Nothing
	 * @de Nichts
	 * @tooltip Without a role nobody gets anything, apart from the two capabilities no policy can
	 *     withhold.
	 */
	public static ResKey BASELINE_NOTHING;

	/**
	 * Baseline state: an own list of capabilities, replacing the default.
	 *
	 * @en Own list
	 * @de Eigene Liste
	 * @tooltip Exactly what is chosen below, replacing the standard. An empty list is the same as Nothing.
	 */
	public static ResKey BASELINE_CUSTOM;

	/**
	 * Background limit state: nothing is stated, which no policy naming a role may leave.
	 *
	 * @en Not stated
	 * @de Nicht festgelegt
	 * @tooltip Refused as soon as the policy names a role. What an unattended run may reach has to be
	 *     said.
	 */
	public static ResKey BACKGROUND_UNDECLARED;

	/**
	 * Background limit state: no limit of its own.
	 *
	 * @en Everything the roles allow
	 * @de Alles, was die Rollen erlauben
	 * @tooltip Gives up the only control that stands in for the missing confirmation.
	 */
	public static ResKey BACKGROUND_ALL;

	/**
	 * Background limit state: an own list of capabilities.
	 *
	 * @en Own list
	 * @de Eigene Liste
	 * @tooltip Only what is chosen below is reachable unattended, administrators included.
	 */
	public static ResKey BACKGROUND_CUSTOM;

	/**
	 * Role group of the picker: the roles of this application.
	 *
	 * @en Application roles
	 * @de Anwendungsrollen
	 */
	public static ResKey ROLE_KIND_APPLICATION;

	/**
	 * Role group of the picker: roles of the framework whose meaning has to be known.
	 *
	 * @en Framework role
	 * @de Framework-Rolle
	 */
	public static ResKey ROLE_KIND_FRAMEWORK;

	/**
	 * Role group of the picker: roles a tool grant does nothing for.
	 *
	 * @en Internal role, reaches no account
	 * @de Interne Rolle, erreicht kein Konto
	 */
	public static ResKey ROLE_KIND_INTERNAL;









	/**
	 * Refusal: the policy names a role the application does not have.
	 *
	 * @en The role {0} does not exist. The next application start would be refused.
	 * @de Die Rolle {0} existiert nicht. Der nächste Anwendungsstart würde abgewiesen.
	 */
	public static ResKey1 REFUSE_UNKNOWN_ROLE__ROLE;

	/**
	 * Refusal: an entry names neither a capability group nor tools.
	 *
	 * @en {0}, entry {1} names nothing. An entry names either a whole capability or single tools.
	 * @de {0}, Eintrag {1} benennt nichts. Ein Eintrag nennt entweder eine ganze Tool-Gruppe oder einzelne
	 *     Tools.
	 */
	public static ResKey2 REFUSE_ENTRY_NAMES_NOTHING__WHERE_POSITION;

	/**
	 * Refusal: an entry names a capability group and tools at once.
	 *
	 * @en {0}, entry {1} names a capability and single tools at once. An entry names exactly one of the
	 *     two.
	 * @de {0}, Eintrag {1} benennt eine Tool-Gruppe und einzelne Tools zugleich. Ein Eintrag nennt genau
	 *     eines von beiden.
	 */
	public static ResKey2 REFUSE_ENTRY_NAMES_BOTH__WHERE_POSITION;

	/**
	 * Refusal: the policy names roles and says nothing about unattended runs.
	 *
	 * @en No limit is stated for runs where nobody can confirm a call. A policy that names roles has to
	 *     state one.
	 * @de Für Läufe, in denen niemand einen Aufruf bestätigen kann, ist keine Obergrenze festgelegt. Eine
	 *     Richtlinie, die Rollen nennt, muss eine festlegen.
	 */
	public static ResKey REFUSE_BACKGROUND_ALLOWLIST_UNDECLARED;

	/**
	 * Names the baseline inside a message about one of its entries.
	 *
	 * @en Basic permissions
	 * @de Grundberechtigungen
	 */
	public static ResKey WHERE_BASELINE;

	/**
	 * Names the background limit inside a message about one of its entries.
	 *
	 * @en Background allow list
	 * @de Hintergrund-Zulassungsliste
	 */
	public static ResKey WHERE_BACKGROUND_ALLOWLIST;

	/**
	 * Names a role's permissions inside a message about one of its entries.
	 *
	 * @en Role {0}, permissions
	 * @de Rolle {0}, Berechtigungen
	 */
	public static ResKey1 WHERE_ROLE_GRANTS__ROLE;

	/**
	 * Names a role's refusals inside a message about one of its entries.
	 *
	 * @en Role {0}, refusals
	 * @de Rolle {0}, Verbote
	 */
	public static ResKey1 WHERE_ROLE_DENIES__ROLE;

	/**
	 * Warning: a role may run TL-Script and change data, which together express every change.
	 *
	 * @en Role {0} may run TL-Script and change data: every data change is expressible as a script.
	 *     Each one still asks the user to confirm.
	 * @de Die Rolle {0} darf TL-Script ausführen und Daten ändern: Per Skript ist jede
	 *     Datenänderung ausdrückbar. Jede bleibt im Gespräch bestätigungspflichtig.
	 */
	public static ResKey1 WARN_SCRIPT_WITH_DATA_WRITE__ROLE;

	/**
	 * Warning: a role may run TL-Script but not change data, so scripts cannot persist anything.
	 *
	 * @en Role {0} may execute TL-Script but not change data. Persisting a change made by a script is
	 *     refused.
	 * @de Die Rolle {0} darf TL-Script ausführen, aber keine Daten ändern. Das Persistieren von
	 *     Skript-Änderungen wird verweigert.
	 */
	public static ResKey1 WARN_SCRIPT_WITHOUT_DATA_WRITE__ROLE;

	/**
	 * Warning: a role may read data and send data outward.
	 *
	 * @en Role {0} may read data and send data out of the application: what it reads can leave.
	 * @de Die Rolle {0} darf Daten lesen und Daten nach außen senden: Gelesenes kann die Anwendung
	 *     verlassen.
	 */
	public static ResKey1 WARN_DATA_AND_EGRESS__ROLE;

	/**
	 * Warning: a role may author agents, and can therefore grant itself anything.
	 *
	 * @en Role {0} may create and wire agents. An agent acting for it can grant itself every tool.
	 * @de Die Rolle {0} darf Agenten anlegen und verknüpfen. Ein Agent in ihrem Auftrag kann sich jedes
	 *     Tool selbst gewähren.
	 */
	public static ResKey1 WARN_AGENT_AUTHORING__ROLE;

	/**
	 * Warning: the policy configures a role the framework uses internally.
	 *
	 * @en Role {0} is a role of the framework's user interface. Its entry reaches nobody and is
	 *     probably a mistake.
	 * @de Die Rolle {0} ist eine Rolle der Framework-Oberfläche. Ihr Eintrag erreicht niemanden und
	 *     ist vermutlich ein Versehen.
	 */
	public static ResKey1 WARN_INTERNAL_ROLE__ROLE;

	/**
	 * Warning: the policy grants something to the role every account holds.
	 *
	 * @en Every logged-in account holds role {0}. These permissions apply to all users.
	 * @de Die Rolle {0} hält jedes angemeldete Konto. Diese Berechtigungen gelten für alle Benutzer.
	 */
	public static ResKey1 WARN_EVERY_ACCOUNT_ROLE__ROLE;

	/**
	 * Warning: unattended runs have no limit of their own.
	 *
	 * @en Unattended runs may use every tool. Every call approves itself, writing and outward-sending ones
	 *     included.
	 * @de Unbeaufsichtigte Läufe dürfen jedes Tool. Jeder Aufruf genehmigt sich selbst, auch schreibende
	 *     und nach außen sendende.
	 */
	public static ResKey WARN_BACKGROUND_ALL;

	/**
	 * Warning: unattended runs may run TL-Script but not persist what it changes.
	 *
	 * @en Runs nobody watches may run TL-Script. They cannot persist what a script changes, because
	 *     that needs the permission to change data, which is not on the list.
	 * @de Unbeaufsichtigte Läufe dürfen TL-Script ausführen. Persistieren können sie nichts, denn
	 *     dafür braucht es das Ändern von Daten, und das steht nicht auf der Liste.
	 */
	public static ResKey WARN_BACKGROUND_SCRIPT;

	/**
	 * Warning: unattended runs may run TL-Script and persist what it changes.
	 *
	 * @en Unattended runs may execute TL-Script and change data. They can persist every change a script
	 *     expresses, with nobody confirming.
	 * @de Unbeaufsichtigte Läufe dürfen TL-Script ausführen und Daten ändern. Sie können jede per Skript
	 *     ausdrückbare Änderung persistieren, ohne Bestätigung.
	 */
	public static ResKey WARN_BACKGROUND_SCRIPT_AND_DATA_WRITE;

	/**
	 * Heading of the block listing what a policy does that its author probably did not mean.
	 *
	 * @en What this policy does that may surprise
	 * @de Hinweise zur Wirkung dieser Richtlinie
	 */
	public static ResKey WARNINGS_HEADING;

	/**
	 * Heading of the block listing why an apply was refused.
	 *
	 * @en Not applied. This policy would stop the next application start:
	 * @de Nicht übernommen. Diese Richtlinie würde den nächsten Anwendungsstart verhindern:
	 */
	public static ResKey REFUSALS_HEADING;





	/**
	 * Heading of the block listing which tools the policy classifies into which group.
	 *
	 * @en Group assignments
	 * @de Gruppen-Zuordnungen
	 * @tooltip Which tools belong to which capability group. This says what a tool is, not what a role may
	 *     do.
	 */
	public static ResKey POLICY_TOOL_GROUPS_HEADING;

	/**
	 * What the group assignments are, where they are shown.
	 *
	 * @en Tools of a connected server and tools authored in the running application declare no group of
	 *     their own. An assignment here is what makes them reachable at all.
	 * @de Tools eines verbundenen Servers und in der Anwendung angelegte Tools deklarieren selbst keine
	 *     Gruppe. Erst eine Zuordnung hier macht sie überhaupt erreichbar.
	 */
	public static ResKey POLICY_TOOL_GROUPS_NOTE;

	/**
	 * Shown where the group assignments would be, when there are none.
	 *
	 * @en No assignment. Every tool is in the group it declares itself.
	 * @de Keine Zuordnung. Jedes Tool steht in der Gruppe, die es selbst deklariert.
	 */
	public static ResKey POLICY_TOOL_GROUPS_NONE;








	/**
	 * The button that makes the saved policy take effect.
	 *
	 * @en Restart the service now
	 * @de Dienst jetzt neu starten
	 */
	public static ResKey COMMAND_RESTART;










	/**
	 * Shown next to the apply button while nothing has been changed.
	 *
	 * @en No unsaved change.
	 * @de Keine ungespeicherten Änderungen.
	 */
	public static ResKey DIRTY_NONE;

	/**
	 * Shown next to the apply button while there are unsaved changes.
	 *
	 * @en Unsaved changes. Apply checks and saves them.
	 * @de Ungespeicherte Änderungen. Übernehmen prüft und speichert.
	 */
	public static ResKey DIRTY_UNSAVED;

	/**
	 * The confirmation of a successful apply, which also says what was overwritten.
	 *
	 * @en Policy checked and saved. It overwrites the whole tool access policy of the configuration
	 *     files and takes effect when the service is restarted.
	 * @de Richtlinie geprüft und gespeichert. Sie überschreibt die gesamte Richtlinie der
	 *     Konfigurationsdateien und wird mit dem Neustart des Dienstes wirksam.
	 */
	public static ResKey APPLY_SAVED;

	/**
	 * The confirmation that the changes were thrown away.
	 *
	 * @en Changes discarded.
	 * @de Änderungen verworfen.
	 */
	public static ResKey DISCARDED;

	/**
	 * The confirmation that the saved policy is now the one being applied.
	 *
	 * @en The service was restarted. The saved policy is now in effect.
	 * @de Der Dienst wurde neu gestartet. Die gespeicherte Richtlinie ist jetzt wirksam.
	 */
	public static ResKey RESTART_DONE;

	/**
	 * The banner shown while the saved policy is not the one being applied.
	 *
	 * @en Policy saved, but not yet in effect. The service keeps applying the policy of its last
	 *     start, and so do all agent runs.
	 * @de Richtlinie gespeichert, aber noch nicht wirksam. Der Dienst wendet weiterhin die Richtlinie
	 *     seines letzten Starts an, und alle Agentenläufe ebenso.
	 */
	public static ResKey BANNER_SAVED_NOT_EFFECTIVE;

	/**
	 * Reported when the policy cannot be written.
	 *
	 * @en The tool access policy could not be written. Nothing was changed.
	 * @de Die Richtlinie konnte nicht geschrieben werden. Es wurde nichts geändert.
	 */
	public static ResKey POLICY_SAVE_FAILED;

	/**
	 * Reported when the service does not come up again.
	 *
	 * @en The tool access service did not start again, so no agent run is possible. The reason is in the
	 *     application log.
	 * @de Der Zugangsdienst für KI-Tools ist nicht wieder gestartet, daher ist kein Agentenlauf möglich.
	 *     Der Grund steht im Anwendungsprotokoll.
	 */
	public static ResKey POLICY_RESTART_FAILED;

	/**
	 * Shown instead of the editor while the saved configuration cannot be read.
	 *
	 * @en The saved tool access configuration cannot be read. There is nothing to edit here.
	 * @de Die gespeicherte Konfiguration des Tool-Zugriffs kann nicht gelesen werden. Hier gibt es nichts
	 *     zu bearbeiten.
	 */
	public static ResKey POLICY_UNREADABLE;

	/**
	 * Marks a tool description that is the text the language model is given rather than an
	 * administrator-facing one.
	 *
	 * @en (Description for the model, EN) {0}
	 * @de (Beschreibung für das Modell, EN) {0}
	 */
	/*
	 * The marker comes first because the cell it lands in truncates: appended, it is the part a reader
	 * never sees, which is the opposite of what marking a fallback is for.
	 */
	public static ResKey1 DESCRIPTION_FROM_MODEL__TEXT;

	/**
	 * Shown instead of the buttons for an account that may look at the policy but not change it.
	 *
	 * @en You may look at this policy but not change it. Changing it needs write permission on the
	 *     security root.
	 * @de Sie dürfen diese Richtlinie ansehen, aber nicht ändern. Dafür ist die Schreibberechtigung
	 *     auf der Sicherheits-Wurzel nötig.
	 */
	public static ResKey POLICY_READ_ONLY;

	/**
	 * Column header detail of an agent the application runs for users rather than letting them converse with it.
	 *
	 * @en Infrastructure
	 * @de Infrastruktur
	 */
	public static ResKey AGENT_INFRASTRUCTURE;

	/**
	 * Column header detail naming the model an agent is assigned.
	 *
	 * @en Model: {0}
	 * @de Modell: {0}
	 */
	public static ResKey1 AGENT_MODEL__MODEL;

	/**
	 * Button above a policy matrix opening the bulk edit over the ticked roles.
	 *
	 * @en Edit selection …
	 * @de Auswahl bearbeiten …
	 * @tooltip Grants, forbids or clears chosen columns for every ticked role at once.
	 */
	public static ResKey COMMAND_EDIT_SELECTION;

	/**
	 * Bulk action putting the chosen cells into the granted state.
	 *
	 * @en Grant ✓
	 * @de Gewähren ✓
	 */
	public static ResKey BULK_ACTION_GRANT;

	/**
	 * Bulk action putting the chosen cells into the forbidden state.
	 *
	 * @en Forbid ✕
	 * @de Verbieten ✕
	 */
	public static ResKey BULK_ACTION_DENY;

	/**
	 * Bulk action emptying the chosen cells.
	 *
	 * @en Clear
	 * @de Leeren
	 */
	public static ResKey BULK_ACTION_CLEAR;

	/**
	 * Button of a dialog of the policy matrix that writes what the dialog chose into the draft.
	 *
	 * @en Apply
	 * @de Anwenden
	 */
	public static ResKey BULK_APPLY;

	/**
	 * Note of the bulk dialog that emptying a row never widens it.
	 *
	 * @en A row this empties stays restricted and may then use none.
	 * @de Eine Zeile, die dadurch leer wird, bleibt „eingeschränkt“ (darf keins).
	 */
	public static ResKey BULK_EMPTY_ROW_STAYS;

	/**
	 * Note of the bulk dialog about the locked rows it restricts first.
	 *
	 * @en {0} rows not named so far are restricted first.
	 * @de {0} bisher nicht genannte Zeilen werden vorher eingeschränkt.
	 */
	public static ResKey1 BULK_RESTRICTED_FIRST__COUNT;

	/**
	 * Note of the bulk dialog about the locked rows it leaves alone.
	 *
	 * @en {0} rows not named are skipped.
	 * @de {0} nicht genannte Zeilen werden übersprungen.
	 */
	public static ResKey1 BULK_SKIPPED__COUNT;

	/**
	 * Label of the selected roles in the bulk dialog.
	 *
	 * @en Roles
	 * @de Rollen
	 */
	public static ResKey BULK_ROLES;

	/**
	 * What the bulk dialog would change.
	 *
	 * @en Changes {0} cells in {1} rows.
	 * @de Betrifft {0} Zellen in {1} Zeilen.
	 */
	public static ResKey2 BULK_SUMMARY__CELLS_ROWS;

	/**
	 * Tooltip of a cell of the background row that cannot be clicked.
	 *
	 * @en Choose "Own list" in this row to pick what unattended runs may reach.
	 * @de Für eine eigene Auswahl in dieser Zeile „Eigene Liste“ wählen.
	 */
	public static ResKey CELL_BACKGROUND_LOCKED;

	/**
	 * Tooltip of a cell of the baseline row that cannot be clicked.
	 *
	 * @en Choose "Own list" in this row to pick the basic permissions yourself.
	 * @de Für eine eigene Auswahl in dieser Zeile „Eigene Liste“ wählen.
	 */
	public static ResKey CELL_BASELINE_LOCKED;

	/**
	 * Name of the command a click on a cell of the policy matrix executes.
	 *
	 * @en Change cell
	 * @de Zelle ändern
	 */
	public static ResKey CELL_CLICK;

	/**
	 * Tooltip prefix of a forbidden cell.
	 *
	 * @en Forbidden (✕): beats every other role. Changed through the context menu.
	 * @de Verboten (✕): gewinnt gegen jede andere Rolle. Änderbar über das Kontextmenü.
	 */
	public static ResKey CELL_DENIED;

	/**
	 * Tooltip prefix of a cell of a row the section does not name.
	 *
	 * @en Not named: the section default decides. To decide per cell, set the row to "restricted".
	 * @de Nicht genannt: die Voreinstellung entscheidet. Um je Zelle zu entscheiden, die Zeile auf „eingeschränkt“ stellen.
	 */
	public static ResKey CELL_LOCKED;

	/**
	 * Context menu entry granting the name of a cell.
	 *
	 * @en Grant ✓
	 * @de Gewähren ✓
	 */
	public static ResKey CELL_MENU_GRANT;

	/**
	 * Context menu entry forbidding the name of a cell.
	 *
	 * @en Forbid ✕
	 * @de Verbieten ✕
	 * @tooltip Beats every permission of every other role the account holds.
	 */
	public static ResKey CELL_MENU_DENY;

	/**
	 * Context menu entry emptying a granted cell.
	 *
	 * @en Clear
	 * @de Leeren
	 */
	public static ResKey CELL_MENU_CLEAR;

	/**
	 * Context menu entry emptying a forbidden cell.
	 *
	 * @en Lift prohibition
	 * @de Verbot aufheben
	 */
	public static ResKey CELL_MENU_CLEAR_DENY;

	/**
	 * Tooltip suffix of a capability the limit of unattended runs does not admit.
	 *
	 * @en Unattended runs do not reach it.
	 * @de Unbeaufsichtigte Läufe erreichen sie nicht.
	 */
	public static ResKey CELL_NOT_IN_BACKGROUND;

	/**
	 * Tooltip of a cell of a run section: what a run of the role would decide.
	 *
	 * @en A run of this role alone: {0}
	 * @de Ein Lauf dieser Rolle allein: {0}
	 */
	public static ResKey1 CELL_RUN_VERDICT__VERDICT;

	/**
	 * Tooltip addition of a run-section cell reachable only because agents the role may run use it
	 * themselves.
	 *
	 * @en Agents that use it themselves: {0}
	 * @de Agenten, die es selbst verwenden: {0}
	 */
	public static ResKey1 CELL_AGENTS_OWN__AGENTS;

	/**
	 * Tooltip of the single-tools cell of a row.
	 *
	 * @en Grants and prohibitions of single tools. Click to edit.
	 * @de Erlaubnisse und Verbote einzelner Tools. Zum Bearbeiten klicken.
	 */
	public static ResKey CELL_TOOLS;

	/**
	 * Tooltip of the single-tools cell of a row that cannot be edited.
	 *
	 * @en Single tools of this row, editable only with an own list.
	 * @de Einzelne Tools dieser Zeile, nur mit eigener Liste änderbar.
	 */
	public static ResKey CELL_TOOLS_LOCKED;

	/**
	 * Header of the role column of the policy matrix.
	 *
	 * @en Role
	 * @de Rolle
	 */
	public static ResKey COLUMN_ROLE;

	/**
	 * Header of the row-state column of a run section.
	 *
	 * @en Row
	 * @de Zeile
	 * @tooltip Not named: the section default decides for the role. Restricted: the role may use exactly what its cells grant, and nothing once they are empty.
	 */
	public static ResKey COLUMN_ROW_STATE;

	/**
	 * Header of the row-state column of the capability matrix.
	 *
	 * @en Entry
	 * @de Eintrag
	 * @tooltip Whether the role has an entry of its own. Removing it takes back everything the role grants and forbids.
	 */
	public static ResKey COLUMN_ENTRY;

	/**
	 * Header of the column counting the single-tool entries of a row.
	 *
	 * @en Single tools
	 * @de Einzeltools
	 */
	public static ResKey COLUMN_TOOLS;

	/**
	 * Title of the bulk dialog.
	 *
	 * @en Edit selection - {0} roles
	 * @de Auswahl bearbeiten - {0} Rollen
	 */
	public static ResKey1 DIALOG_EDIT_SELECTION__COUNT;

	/**
	 * Title of the dialog listing the single tools of a row.
	 *
	 * @en Single tools - {0}
	 * @de Einzeltools - {0}
	 */
	public static ResKey1 DIALOG_TOOL_ENTRIES__ROW;

	/**
	 * Label of the action of the bulk dialog.
	 *
	 * @en Action
	 * @de Aktion
	 */
	public static ResKey FIELD_BULK_ACTION;

	/**
	 * Label of the capability groups the bulk dialog changes.
	 *
	 * @en Capabilities
	 * @de Fähigkeiten
	 */
	public static ResKey FIELD_BULK_GROUPS;

	/**
	 * Label of the names the bulk dialog changes.
	 *
	 * @en Columns
	 * @de Spalten
	 */
	public static ResKey FIELD_BULK_NAMES;

	/**
	 * Label of the switch restricting locked rows before a bulk edit.
	 *
	 * @en Restrict rows not named first
	 * @de Nicht genannte Zeilen vorher einschränken
	 * @tooltip Otherwise the rows the section does not name are skipped.
	 */
	public static ResKey FIELD_BULK_RESTRICT_FIRST;

	/**
	 * Label of the switch showing the overlay.
	 *
	 * @en Show effective
	 * @de Effektiv anzeigen
	 * @tooltip Marks what a run of the role alone refuses although the cell grants it (⊘), and what it reaches only because an agent uses it itself (A). Computed from the draft.
	 */
	public static ResKey FIELD_OVERLAY;

	/**
	 * Label of the role search of the policy matrix.
	 *
	 * @en Search role
	 * @de Rolle suchen
	 */
	public static ResKey FIELD_ROLE_SEARCH;

	/**
	 * Label of the row filter of the policy matrix.
	 *
	 * @en Rows
	 * @de Zeilen
	 */
	public static ResKey FIELD_ROW_FILTER;

	/**
	 * Label of the single tools the baseline or the background limit names.
	 *
	 * @en Tools
	 * @de Tools
	 */
	public static ResKey FIELD_TOOLS_ADMITTED;

	/**
	 * Label of the single tools a role forbids.
	 *
	 * @en Forbidden tools
	 * @de Verbotene Tools
	 */
	public static ResKey FIELD_TOOLS_DENIED;

	/**
	 * Label of the single tools a role grants.
	 *
	 * @en Granted tools
	 * @de Erlaubte Tools
	 */
	public static ResKey FIELD_TOOLS_GRANTED;

	/**
	 * Label of the switch asking about unattended runs.
	 *
	 * @en Unattended runs
	 * @de Hintergrundläufe
	 * @tooltip Asks about a run with nobody to confirm a tool call, which the limit of unattended runs binds.
	 */
	public static ResKey FIELD_UNATTENDED;

	/**
	 * Legend above a run section matrix.
	 *
	 * @en ✓ may use · ✕ may never use, beats every other role (context menu or "Edit selection …") · hatched: not named, the default in the header of the row column decides. The rows are all roles; the cells are evaluated for one role on its own.
	 * @de ✓ darf verwenden · ✕ darf niemals verwenden, gewinnt gegen jede andere Rolle (Kontextmenü oder „Auswahl bearbeiten …“) · schraffiert: nicht genannt, die Voreinstellung im Kopf der Zeilen-Spalte entscheidet. Die Zeilen sind alle Rollen; bewertet wird jede Rolle für sich.
	 */
	public static ResKey MATRIX_LEGEND_RUN;

	/**
	 * Legend above the capability matrix.
	 *
	 * @en ✓ grants · ✕ forbids, beats every other role and the basic permissions (context menu or "Edit selection …") · the first two rows are the basic permissions of every account and the limit of unattended runs.
	 * @de ✓ gewährt · ✕ verbietet, gewinnt gegen jede andere Rolle und die Grundberechtigung (Kontextmenü oder „Auswahl bearbeiten …“) · die ersten beiden Zeilen sind die Grundberechtigung jedes Kontos und die Obergrenze für unbeaufsichtigte Läufe.
	 */
	public static ResKey MATRIX_LEGEND_CAPABILITIES;

	/**
	 * Marks a name the policy mentions and the application does not have.
	 *
	 * @en unknown
	 * @de unbekannt
	 */
	public static ResKey NAME_UNKNOWN;

	/**
	 * Why redo is disabled.
	 *
	 * @en Nothing to redo.
	 * @de Nichts zu wiederholen.
	 */
	public static ResKey NOTHING_TO_REDO;

	/**
	 * Why undo is disabled.
	 *
	 * @en Nothing to undo.
	 * @de Nichts rückgängig zu machen.
	 */
	public static ResKey NOTHING_TO_UNDO;

	/**
	 * Marks a role the policy names and the application does not have.
	 *
	 * @en {0} - role does not exist
	 * @de {0} - Rolle existiert nicht
	 */
	public static ResKey1 ROLE_UNKNOWN__ROLE;

	/**
	 * Label of the row of the limit of unattended runs.
	 *
	 * @en Limit of unattended runs
	 * @de Obergrenze Hintergrund
	 * @tooltip What a run with nobody to confirm a tool call may reach at most, whatever the roles grant. Binds administrators too.
	 */
	public static ResKey ROW_BACKGROUND;

	/**
	 * Label of the row of the basic permissions.
	 *
	 * @en Basic permissions (all accounts)
	 * @de Grundberechtigung (alle Konten)
	 * @tooltip What every account may use without holding a role.
	 */
	public static ResKey ROW_BASELINE;

	/**
	 * Row filter option showing every role.
	 *
	 * @en all rows
	 * @de alle Zeilen
	 */
	public static ResKey ROW_FILTER_ALL;

	/**
	 * Row filter option showing the roles a section does not name.
	 *
	 * @en only not named
	 * @de nur nicht genannte
	 */
	public static ResKey ROW_FILTER_NOT_NAMED;

	/**
	 * Row filter option showing the roles a section restricts.
	 *
	 * @en only restricted
	 * @de nur eingeschränkte
	 */
	public static ResKey ROW_FILTER_RESTRICTED;

	/**
	 * Row filter option showing the roles with an entry.
	 *
	 * @en only with entry
	 * @de nur mit Eintrag
	 */
	public static ResKey ROW_FILTER_WITH_ENTRY;

	/**
	 * Row filter option showing the roles without an entry.
	 *
	 * @en only without entry
	 * @de nur ohne Eintrag
	 */
	public static ResKey ROW_FILTER_WITHOUT_ENTRY;

	/**
	 * Row state of a role with an entry of the tool access section.
	 *
	 * @en entry
	 * @de Eintrag
	 */
	public static ResKey ROW_STATE_ENTRY;

	/**
	 * Row state of a role without an entry of the tool access section.
	 *
	 * @en no entry
	 * @de kein Eintrag
	 */
	public static ResKey ROW_STATE_NO_ENTRY;

	/**
	 * Row state of a role a run section does not name.
	 *
	 * @en not named
	 * @de nicht genannt
	 */
	public static ResKey ROW_STATE_NOT_NAMED;

	/**
	 * Row state of a role a run section restricts.
	 *
	 * @en restricted
	 * @de eingeschränkt
	 */
	public static ResKey ROW_STATE_RESTRICTED;

	/**
	 * Message when the bulk edit is opened without a selection.
	 *
	 * @en Select the roles to edit first, with the checkbox in front of the row.
	 * @de Zuerst die zu bearbeitenden Rollen auswählen, mit dem Häkchen vor der Zeile.
	 */
	public static ResKey SELECTION_EMPTY;

	/**
	 * Note at the top of the single-tools dialog.
	 *
	 * @en Entries naming single tools instead of a whole capability. A prohibition beats every permission.
	 * @de Einträge, die einzelne Tools statt einer ganzen Fähigkeit nennen. Ein Verbot gewinnt gegen jede Erlaubnis.
	 */
	public static ResKey TOOL_ENTRIES_NOTE;

	static {
		initConstants(I18NConstants.class);
	}

	/**
	 * How many entries a collapsed section holds.
	 *
	 * @en ({0})
	 * @de ({0})
	 */
	public static ResKey1 SECTION_COUNT__COUNT;



	/**
	 * Refusal of a policy naming an agent, a model or an MCP server the application does not have.
	 *
	 * @en {0} does not exist. The next start would be refused over it.
	 * @de {0} gibt es nicht. Der nächste Start würde deswegen abgelehnt.
	 */
	public static ResKey1 REFUSE_UNKNOWN_NAME__NAME;



	/**
	 * Label of the field holding what a section admits from a role it does not name.
	 *
	 * @en Everybody else
	 * @de Alle anderen
	 * @tooltip What a user may use who holds none of the roles named below. Naming a role takes its
	 *          holders out of this and leaves everybody else where they were.
	 */
	public static ResKey FIELD_SECTION_DEFAULT;




	/**
	 * Why an agent this account is permitted still cannot run for it.
	 *
	 * <p>
	 * The row of an agent whose <em>own</em> model, or one of whose own MCP servers, a role of this
	 * account forbids. The agent section permits it and another section stops it, so its row says
	 * which name and which role rather than repeating the agent rule that did let it through.
	 * </p>
	 *
	 * @en The agent {0} cannot run for this account: it uses {1} itself, which the role {2} forbids.
	 * @de Der Agent {0} kann für dieses Konto nicht laufen: Er verwendet selbst {1}, und das ist für
	 *     die Rolle {2} ausgeschlossen.
	 */
	public static ResKey3 AGENT_USES_DENIED__AGENT_NAME_ROLE;

	/**
	 * Heading of the agent rows of the effective-access table, and of the agent section of the editor.
	 *
	 * @en Agents
	 * @de Agenten
	 * @tooltip Which agents this account may run.
	 */
	public static ResKey SECTION_AGENT_ACCESS;

	/**
	 * Heading of the model rows of the effective-access table, and of the model section of the editor.
	 *
	 * @en Models
	 * @de Modelle
	 * @tooltip Which language models this account may choose for a single request.
	 */
	public static ResKey SECTION_MODEL_ACCESS;

	/**
	 * Heading of the MCP rows of the effective-access table, and of the MCP section of the editor.
	 *
	 * @en MCP servers
	 * @de MCP-Server
	 * @tooltip Which MCP servers this account may attach to a single request.
	 */
	public static ResKey SECTION_MCP_ACCESS;

	/**
	 * What a section says when the application has none of what it decides about.
	 *
	 * @en None configured
	 * @de Keine konfiguriert
	 */
	public static ResKey SECTION_SUMMARY_NONE_CONFIGURED;

	/**
	 * How much of one section an account reaches.
	 *
	 * @en {0} of {1} reachable
	 * @de {0} von {1} erreichbar
	 */
	public static ResKey2 SECTION_SUMMARY__ALLOWED_TOTAL;

	/**
	 * Why an agent, a model or a server is reachable: a role of this account permits it.
	 *
	 * @en Permitted by the role {0}.
	 * @de Durch die Rolle {0} erlaubt.
	 */
	public static ResKey1 ALLOWED_BY_ROLE__ROLE;

	/**
	 * Why an agent, a model or a server is reachable: nothing restricts it.
	 *
	 * @en Permitted by the default of this section.
	 * @de Durch die Voreinstellung dieses Abschnitts erlaubt.
	 */
	public static ResKey ALLOWED_BY_SECTION_DEFAULT;

	/**
	 * Why a model or a server is reachable: it is what the agent itself uses, which is not a choice.
	 *
	 * @en The agent uses this itself, so choosing it takes no permission.
	 * @de Der Agent verwendet dies selbst; es zu wählen erfordert daher keine Erlaubnis.
	 */
	public static ResKey ALLOWED_BY_THE_AGENT;

	/**
	 * The note under the effective-access table about what the three run sections do not say.
	 *
	 * @en Which agent, model and MCP server a request may name is decided before the run starts;
	 *     these rows say nothing about what the run may then do, which is the table above.
	 * @de Welchen Agenten, welches Modell und welchen MCP-Server eine Anfrage nennen darf, wird vor
	 *     dem Lauf entschieden; diese Zeilen sagen nichts darüber, was der Lauf danach tun darf - das
	 *     steht in der Tabelle darüber.
	 */
	public static ResKey RUN_SECTIONS_NOTE;












	/**
	 * Label of the capability group an assignment puts tools into.
	 *
	 * @en Capability
	 * @de Tool-Gruppe
	 * @tooltip The capability group the tools next to it belong to.
	 */
	public static ResKey FIELD_ASSIGNMENT_GROUP;

	/**
	 * Label of the tools an assignment puts into a capability group.
	 *
	 * @en Tools
	 * @de Tools
	 * @tooltip The tools in this group. Removing the last one removes the assignment.
	 */
	public static ResKey FIELD_ASSIGNMENT_TOOLS;

	/**
	 * The button that starts another group assignment.
	 *
	 * @en Add an assignment
	 * @de Zuordnung hinzufügen
	 */
	public static ResKey COMMAND_ADD_ASSIGNMENT;

	/**
	 * The button that removes one group assignment.
	 *
	 * @en Remove
	 * @de Entfernen
	 */
	public static ResKey COMMAND_REMOVE_ASSIGNMENT;

	/**
	 * Shown when every known capability group already has an assignment.
	 *
	 * @en Every known capability group already has an assignment.
	 * @de Jede bekannte Tool-Gruppe hat bereits eine Zuordnung.
	 */
	public static ResKey ASSIGNMENT_NOTHING_TO_ADD;

}
