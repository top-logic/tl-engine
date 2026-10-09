---
description: Read when inspecting a running React view-layer UI or writing UI diagnostics and assertions (UI inspector).
order: 90
---

# UI inspector, diagnostics and assertions

## UI inspector, diagnostics and assertions

The UI inspector is a side-window that shows the control behind an element of the application window as the *headless projection* sees it — the same view of the display the agent API observes and a recorded assertion is verified against. It is opened by a generic `<open-window>` command configured with its view, gated by `<inspect-enabled/>`:

```xml
<open-window
  height="640"
  image="css:bi bi-search"
  placement="TOOLBAR"
  singleton-key="ui-inspector"
  view="ui-inspector.view.xml"
  width="720"
>
  <title><en>UI inspector</en></title>
  <label><en>Inspect</en></label>
  <executability><inspect-enabled/></executability>
</open-window>
```

`InspectEnabled` (`com.top_logic.layout.view.command`) asks `GuiInspectorUtil.isGuiInspectorEnabled()`, so the command appears under exactly the configuration that enables the inspection of the classic UI: `DebuggingConfig.inspectEnabled`, wired in `top-logic.config.xml` to the `%tl_enable_debug%` alias (the `tl_enable_debug` environment variable, `false` unless `enable-debug.xml` is in effect), or an enabled scripting recorder. That is a deployment switch, not a permission — whoever works on a system that has it on sees the tooling regardless of their roles. The app bar of `com.top_logic.demo.react` carries it beside the script recorder (`script-recorder.view.xml`, the side-window that starts and stops a recording, lists its steps live and replays them one by one).

**Picking and navigating.** *Pick element* starts a pick on the window that opened the side-window (`ElementPicker`, `PickKind.CONTROL`): the user's next click in the application window reports the control that was hit, the inspector comes back to the front, and its `node` channel holds an `InspectedNode` — the window, the control's semantic address (`/appShell/panel/table[Demo]`) and the projection taken at it. *Parent* inspects the enclosing element (the address without its last segment), *Refresh* takes the same address anew, which is what shows a state that changed meanwhile: the projection is a snapshot, not a live binding.

**What the state table shows.** The header names the address and the control's role, name and React module; the table below lists the projected state flattened to one row per leaf value, addressed by the dotted path an assertion names it by (`diagnostics.hiddenByAccess.count`). It is the projection, not the React props: the keys a control declares as presentation-only are omitted, and a nested object contributes its leaves rather than one opaque row. The second table lists the actions the control offers — the commands a script may dispatch at that address, with their argument schema.

**Diagnostics never reach the browser.** `ReactControl.putDiagnostic(key, value)` records an observation *about* a control — how the displayed state came about, where the display itself cannot say so. Such observations appear in the projection under the `diagnostics` key (hence in the inspector, and capturable by an assertion) and never in the state sent to the client, because they are information for whoever inspects the UI, not for whoever uses it. The value is plain data (string, number, boolean, list or map of those), so it serializes as JSON like any projected state.

**`hiddenByAccess`.** A `<table>` records what the current user's read rights removed from its rows: `{count, byType}` under `diagnostics.hiddenByAccess`, absent when nothing was removed. The rows expression runs through a `QueryExecutor`, whose *result* is filtered for read rights — `TableElement` attaches a `SecurityFilterReport` to the `EvalContext` (`EvalContext.setSecurityReport`), which makes the filter record what it drops instead of dropping it silently. The filter sits on the value leaving the execution, not on every value an expression touches: a `get()` inside the expression yields what it yields, and only the result is filtered. A table showing fewer rows than its query found is therefore explained in the inspector, while the user sees only the rows.

**Recording an assertion.** The state table is multi-select, and the selected paths reach the view's `selection` channel (one path as that path, several as the set of them, none as `null`). *Record assertion* (`RecordAssertionAction`) turns them into a step of the recording running on the inspected window: it takes the part of the projected state those paths address (`InspectedNode.stateSubset`, which keeps the shape of the state, so paths sharing a prefix contribute to one nested object and a path the state no longer has is skipped) and appends an `AssertCommand` through `ScriptRecorder.recordAssertion` — the one place an assertion step is built, shared with the agent endpoint's `record/assert`. The step appears in the recorder side-window as `Check state of '<address>'`, between the interaction steps it belongs between. Without a running recording nothing is recorded and the inspector says to start the recorder first: an assertion is a step of a script.

On replay — the recorder's *Step* button (`ReactWindowReplay.verify`) or the agent's replay endpoint — such a step is verified rather than dispatched. `AssertCommand.mismatchingKeys(expected, actual)` compares the recorded entries against the node's live state as a *subset*: state the assertion does not name is ignored, values are compared by canonical JSON, and where both sides hold a map the expected map is compared entry by entry, so an assertion on `diagnostics.hiddenByAccess.count` still holds when a sibling entry of that group changes. The paths it reports are the ones the inspector showed, so a failure names the row the user selected.
