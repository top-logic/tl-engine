---
description: Read before driving a React view window without a browser (ScriptingSession observe / resolve / act / addressOf), before writing or changing a ReactControl or a @ReactCommandHandler so that it is well-addressable for the script recorder, replay and agents (scriptingName, scriptingRole, scriptingTransparent, scriptingChildSlot, scriptingPresentationKeys, technical commands, typed ReactCommand arguments, recordCommand, ScriptingModelKey), or when working on the ScriptRecorder, AssertCommand and ReactWindowReplay.
order: 110
---

# The headless interface

The browser sees a React window as a tree of controls whose state the server sends and to which it sends commands back. The headless interface (`com.top_logic.layout.react.scripting`) makes the same tree usable without a browser: a consumer reads the display as an addressable tree, picks a node by a semantic address and dispatches a command to it. There is no second implementation of behavior: acting dispatches through `ReactControl.executeCommand`, the method the browser's commands reach as well.

Its consumers are

- the **script recorder**, which captures the commands a user's interactions dispatch as replayable steps, and the replay of such a script (the recorder side-window and the UI inspector of `tl-layout-view`);
- **agents**: in-process (the page tools of `tl-ai-views` read and rebuild a window through it) or outside the server through the HTTP endpoint of the optional module `tl-layout-view-agent`.

The interface is in-process; reaching it from outside is a matter of its own with its own admission rules, which is why the HTTP surface is a module of its own.

## Observe, resolve, act

`ScriptingSession.forRoot(queue.getRootControl())` opens a session on the window's displayed root (the root `ViewServlet` sets on the window's `SSEUpdateQueue`). Controls registered in the queue but no longer reachable from that root are not part of it.

- `observe()` projects the tree into a `ScriptingNodeView`: the synthetic root `/` (role `app`) with the window's top-level nodes as children. Each node carries `address`, `role`, an optional `name`, its React `module`, its `state`, the `actions` it offers and its `children`. `observeJson()` renders it as JSON.
- `observeActionsJson()` is the compact view for an agent loop: `{"actions": [...]}`, a flat list of only the nodes that offer actions, without children, with the same addresses.
- `resolve(address)` maps an address back to the live control; an address that does not resolve throws `IllegalArgumentException` naming the segment that was not found and the segments that are available.
- `act(address, command, arguments)` resolves the address and calls `executeCommand(command, arguments)`, returning the reply of the command's handler (a msgbuf message, `null` for none, see `ReactCommandHandler` and `ReactCommandAnswer`) and throwing its failure.
- `addressOf(control)` is the reverse of `resolve`: it walks the visible tree with the same segment algorithm, so its result resolves back to the control. It is `null` for a control that is not part of the projection (a cell sub-control, a control inside an elided wrapper).

All of this reads and changes the live control tree of a window. It therefore runs inside the window's subsession and within an `Interaction` of the session (`ReactWindowRegistry.beginInteraction()`, the session's request lock), exactly like a command from the browser. After acting, `ReactWindowRegistry.synthesizeModelEvents(windowName)` lets derived state settle before the next observation, so a headless caller reads a settled state instead of waiting for SSE delivery. `ReactWindowReplay.inWindow(registry, windowName, action)` and `ReactWindowReplay.act(registry, windowName, step)` do all of this for a window other than the one the current request belongs to (installing that window's subsession, running in an interaction, settling, restoring the caller's subsession); the updates the command produces go to the target window's SSE connection, so the effect appears in that browser window.

## Addresses

An address is a `/`-separated path of segments, one per projected ancestor: `/appShell/sidebar/item[attributes]/tabBar/tab[table]/table[Demo]`. A segment is built from the node's role and an optional name discriminator, `role[name]`.

- **Role.** `ReactControl.scriptingRole()`, or derived from the React module (`TLButton` becomes `button`, `TableView` becomes `tableView`).
- **Name.** In this order: `ReactControl.scriptingName()`; the first non-empty string of the state keys `name`, `label`, `title`, `text`; the label of the control's model (`MetaLabelProvider`). A label that is a default `Object.toString()` (`Something@1a2b3c`) is no name. The name is reduced to letters, digits, `_` and `-` (blanks become `_`).
- **Siblings.** Siblings with the same segment get an occurrence index: `row#0`, `row#1`. Such an index depends on the order of the siblings and is the weakest part of an address; a name that tells siblings apart avoids it.
- **Transparent wrappers.** A control whose `scriptingTransparent()` is `true` (stacks, insets, split panels, slot placeholders, reload boundaries) is elided, its children are lifted into its parent. This keeps addresses short and stable against layout refactorings.
- **Navigation slots.** A container that shows content selected by the user addresses that content by the stable configuration id of the selected entry: `scriptingChildSlot(child)` returns `ScriptingControl.slotSegment(role, id)` - the sidebar `item[<nav item id>]`, the tab bar `tab[<tab id>]`, the deck pane `pane[<index>]`. The content's address thereby names the navigation context it belongs to. The container's own address does not change with its selection, and an address recorded under one selection fails to resolve under another, instead of hitting a look-alike control of another context. A slotted child is never elided as transparent.

An address derives from roles, names and the shape of the tree, never from the per-session control ids (`v17`), so it resolves in another session as long as the view structure is the same. A drifted address fails loudly; there is no fuzzy best match. Finding another way is up to the consumer.

## Identities of business objects

A row of a table, a node of a tree, an option of a select or an item of a selectable list is identified not by its position or a session-allocated id but by its business object. `ScriptingModelKey.toKey(context, object)` builds a `ModelName` for the object - the object naming of the classic script recorder, with its `ModelNamingScheme`s - and the projection carries it as `key`, written as configuration JSON. The typed arguments of the key-based commands bind the same JSON (`ModelKeyArguments` for one key, `SelectByKeysArguments` for a selection), so a consumer copies a key verbatim from an observation into an argument:

| Control | Commands by key |
|---|---|
| Table | `selectByKey {key}`, `selectByKeys {keys}`, `activateByKey {key}`, `expandByKey {key, expanded}` |
| Tree | `selectByKey {key}`, `selectByKeys {keys}`, `expandByKey`, `collapseByKey`, `activateByKey`, `contextMenuByKey {key, x, y}` |
| Dropdown | `selectByKey {keys}` |
| Selectable list, board | `selectItemByKey`, `selectCardByKey` (with the `toggle` / `range` modifiers) |
| Drop targets | `dropObjects` |

**The projection carries no session-bound id for an object that has a key.** `ScriptingModelKey.putIdentity(projection, context, object, idProperty, id)` puts the `key` of an object, and its session id under `idProperty` only if the object has no name; `ItemSelection.identifyKeys` does the same for the client keys of a selectable list or board. So the tree projects its displayed `nodes` (`key`, `depth`, `expandable`, `expanded`, `selected`), the dropdown its `options` and its `value`, the selectable list its `itemKeys` and `selected`, the board its `selected`, each by key. A recorded assertion on that state holds in every session, and every control whose ids are left out offers a command by key, so a consumer can act on what it observes. A masked dropdown value is projected with its placeholder label and `masked: true` only - neither its identity nor its id. Client wiring that names other controls or client-side ids (`dropVerdicts`, the drawer toggle's `controls`, the menu region's `childId`) and the table's keyboard cursor are presentation keys and not projected.

- A name is resolved by `ModelResolver` in a `ReactActionContext`, an `ActionContext` carrying only the `DisplayContext` and the session. Its `getMainLayout()` throws: a naming scheme that needs the component tree of the classic UI does not apply in the React view layer.
- `ReactOptionByLabelNaming` names an option by its label relative to its select control (`ReactOptionScope`), so the label only has to be unique among the options of that control. Where two options share a label, the scheme declines and the global scheme names the object instead, so identity is never lost.
- The default global naming is by label (`TLObjectByLabelNaming`): in most models the label is the business identifier, and unlike a database id it survives a fresh database, which is what a recorded test runs against.
- A control resolving a recorded key that names no row, node or option fails the command with a message; it never substitutes another object or drops the key silently.

## Actions

A node's actions are its `@ReactCommandHandler` commands minus the ones that are UI chrome rather than user intent. A command is chrome when its handler says `technical = true` (closing a snackbar, toggling a collapse, popping out a panel, reporting a display class) or when the control lists it in `scriptingHiddenCommands()`. Chrome is neither advertised nor recorded; `nonRecordableCommands()` adds commands that are advertised but not recorded (scrolling, column resizing).

Each action carries the schema of its arguments:

- **Typed arguments.** A handler declares a `ReactCommand` subtype as its argument parameter instead of a raw `Map`. The client's JSON arguments are bound into it through `JsonConfigurationReader` (by the `@Name`s of the properties), and the handler reads getters. The interface is at the same time the advertised JSON schema (`argsSchema`, built by `JsonConfigSchemaBuilder`, with `@Mandatory`, types and the generated property labels; the envelope properties `address`, `name` and `target` are stripped), the human-readable description of a recorded step (its `@Label` is the template, e.g. `Activate tab '{tabId}'`, rendered by `ReactCommands.describe` through `ConfigLabelProvider`) and the recorded step itself. `ReactCommandMap` rejects an argument interface that does not extend `ReactCommand`.
- **Raw maps.** The configuration JSON reader binds no list of primitive values and no map with arbitrary keys. The few commands taking such arguments (`paletteChanged` of the color input, `reorder` of the dashboard, `updateSizes` of the split panel, the dropdown's `valueChanged`) keep a `Map` and describe it with `@ReactParam` in `@ReactCommandHandler(params = ...)`, projected as `params`.

`ScriptingNode` is an optional interface for a control that replaces its whole projected state (`scriptingState()`) or action list (`scriptingActions()`); returning `null` keeps the generic derivation.

## Acting is acting as the user

A headless command takes the path a browser command takes, so it is subject to the same checks - but only to the checks the handler makes. The client offering a command is no check: the browser may be bypassed. A handler therefore refuses what the user interface does not offer, whatever arrives: no write to a field that is not editable, no press of a hidden or disabled button or menu entry (the `CommandModel` decides), no navigation to a hidden sidebar item, no form lifecycle command outside its edit state. A refusal is a `TopLogicException` with a message (`CommandErrors.notExecutable(ExecutableState)` for a command that is not offered), which reaches the consumer as a failed step.

**Echo of a command's state.** A handler may record a state change silently (`updateStateSilently`) because the browser that sent the command already shows it - the value typed into an input, a size dragged by the user. That holds only for a command from the browser. `ReactServlet` dispatches browser commands through `executeClientCommand`; every other caller - `ScriptingSession.act`, a replay, server code - uses `executeCommand`, which sends the control's full state after the command whenever the handler left silent changes behind. A handler needs no logic of its own for where a command came from.

## The script recorder

Each window has a `ScriptRecorder` on its `SSEUpdateQueue` (`queue.getRecorder()`). `start()` begins a fresh recording, discarding the previous steps; `stop()` ends it and keeps the steps until the next start; `steps()` is a snapshot copy.

While recording, `ReactServlet` feeds every command the browser dispatches into the recorder, before the command runs (`ScriptRecorder.capture(root, control, command, arguments)`):

1. A command for which `isRecordable(command)` is false is skipped.
2. `recordCommand(command, arguments)` lets the control translate the command into a replay-stable `RecordedCommand`. The default records the command as its typed item. A control whose arguments are bound to the session overrides it:
   - The table records a plain row click (`select {rowIndex}`) as `selectByKey {key}`, a row activation as `activateByKey` and the expansion of a tree row as `expandByKey`. The tree records `select`, `expand`, `collapse`, `activate` and `contextMenu` with node ids as their counterparts by key.
   - A click with `Ctrl` or `Shift` and the table's `selectAll` and keyboard navigation (`moveSelection`) depend on the range anchor, the cursor and the order of the rows, so they are recorded as the selection they result in: `selectByKeys {keys}`, coalescing, so a run of them is one step. The control computes that selection without applying it, with the same code the handler applies it with. Replaying it sets the selection to the named rows; a selected row the filter hides stays selected, as it does for the gestures.
   - The dropdown records `valueChanged` with option ids as `selectByKey` with option keys.
   - The tree, the table and the board record a drop by the business identities of the dragged objects and the drop location.
   - A gesture on an object without a name is recorded verbatim.
3. The step gets the control's address (`addressOf`) and is recorded.

A `RecordedCommand` marked `coalescing` replaces the previous step if that one has the same address and command, so the keystrokes of one field edit end up as one step with the final value. The recorder merges by address and command only and knows nothing about controls. A command dispatched headlessly (`act`, replay) does not pass `ReactServlet` and is not recorded.

An **assertion** is a step, too: `ScriptRecorder.recordAssertion(address, expectedState)` appends an `AssertCommand` (the pseudo-command `assertState`) carrying a subset of the node's projected state. On replay it is verified instead of dispatched: `AssertCommand.mismatchingKeys(expected, actual)` compares the expected entries with the node's current state (`ScriptingTreeProjector.nodeState`) by canonical JSON, map by map, and reports the paths of the entries that differ. State the assertion does not name is ignored.

`ScriptingSession.replay(step)` replays one recorded step in a window tree - an assertion is verified, any other step dispatched to the control at its address; `ReactWindowReplay.act` runs it in the window's interaction. A step whose address no longer resolves throws an `IllegalArgumentException` with the address; a step whose key no longer resolves, and an assertion that does not hold, throw a `TopLogicException` with its message (`ReactWindowReplay.act` throws it once the window has settled the state the step left behind). `TestCrossSessionReplay` records navigation, a tab switch, row clicks with and without modifiers, a tree expansion and selection, a dropdown choice, field input and an assertion in one session and replays them in a fresh one, comparing the whole projection of both sessions after every step.

The recorder is observable (`addListener`): the listener is called after every start, stop and capture, outside the recorder's monitor. A recorder side-window reflects the steps of the recorder of its opener window this way, live over its own SSE connection.

A recording lives in memory, in the window it was recorded in. It is a list of `ReactCommand` items; the description, the arguments for dispatch (`ReactCommands.arguments`) and the wire form of the agent endpoint are computed from the item.

## Making a control well-addressable

A new control is addressable without any of this, but the generic derivation produces addresses and observations only as good as its state. Check:

- **A stable name.** If the label a user sees is not a stable identity - it is localized, changes with the edit state or names mutable state such as the active tab - override `scriptingName()` with the technical identity (a form field's attribute name, a command's id). Never name a container after its selection.
- **A meaningful role.** Override `scriptingRole()` where the React module name is not what the node is.
- **Structure only?** A control that only arranges its children returns `true` from `scriptingTransparent()`.
- **Selection-driven content?** A container that swaps its content by a selection returns a slot from `scriptingChildSlot(child)` for the content child, named by the configuration id of the selected entry.
- **No noise.** List rendering-only state keys (padding, variant, icon, debounce, CSS class) in `scriptingPresentationKeys()`, extending the inherited set with `presentationKeys(super.scriptingPresentationKeys(), ...)`. `null` values and child controls are left out automatically.
- **Observations about the control** that are not for the user (why rows are missing, how a value came about) go to `putDiagnostic(key, value)`: projected under `diagnostics`, never sent to the browser.
- **Commands.** Mark chrome handlers `technical = true`. Give every other handler a typed `ReactCommand` argument interface with a `@Label` that reads as a step description, and mandatory properties marked `@Mandatory`. Refuse in the handler what the UI does not offer.
- **Session-bound arguments.** If a command's arguments are ids allocated in the session (option ids, row indexes, node ids), add a key-based command taking `ModelName`s (extend `ModelKeyArguments`, or take `SelectByKeysArguments`) and override `recordCommand` to record that one. A gesture whose outcome depends on an anchor or a cursor is recorded as its outcome. Mark per-keystroke commands `coalescing`.
- **Data in the projection.** A control whose state holds only child controls or client-side ids projects nothing a consumer can read. Override `scriptingScalarState()` to add a readable form - the table adds `rows`, each with `rowIndex`, `selected`, the business `key` and the cell texts, bounded to the current viewport (at most 100 rows, `totalRowCount` telling how many there are); the tree adds its displayed `nodes`.
- **No session ids in the projection.** Project an object by its key (`ScriptingModelKey.putIdentity`), not by an id allocated in the session, and list client wiring that names other controls by id in `scriptingPresentationKeys()`.

## Pitfalls

- **Sibling indexes are positional.** `row#3` changes when the siblings reorder. Rows and options are reached by key, not by index.
- **An observation is a snapshot.** It reflects the tree when it was taken; a later change shows up only in a new observation.
- **Observing holds the session.** An observation is taken within the session's interaction, so a browser command of the same session waits while a large tree is projected (including the label of every model).
- **Labels may change between sessions.** A name derived from a localized label resolves only in a session with the same locale; a control that is to be addressed reliably names itself by its technical identity.
- **Recorded arguments must be replay-stable.** A command recorded verbatim with session ids or positions - a gesture on an object without a name, which the controls record verbatim - replays only in the same state of the same session.
