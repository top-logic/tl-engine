# Declarative Dialogs in View XML

**Date**: 2026-03-16
**Ticket**: #29108
**Branch**: CWS/CWS_29108_declarative_dialogs

## Summary

Add the ability to declare dialogs in standalone `.view.xml` files and open them from button actions without writing Java code per dialog. Two new framework commands enable this: `OpenDialogCommand` (opens a dialog from a view XML path) and `CancelDialogCommand` (closes the topmost dialog without result).

## Components

### OpenDialogCommand

**Location**: `com.top_logic.layout.view.command`

A configurable `ViewCommand` that opens a modal dialog whose content is defined in a separate `.view.xml` file.

**Configuration properties:**

| Property | Type | Required | Description |
|---|---|---|---|
| `dialog-view` | String (view XML path) | Yes | Path to the `.view.xml` file defining the dialog content |
| `close-on-backdrop` | boolean | No | Whether clicking outside the dialog closes it (default: `false`) |
| `<input>` | Channel mappings | No | Parent → dialog channel values, copied once at open time |
| `<output>` | Channel mappings | No | Dialog → parent channel values, propagated on dialog close with result (future) |

**Behavior:**

1. Loads the dialog view XML via `ViewLoader.getOrLoadView(dialogViewPath)`
2. Creates a new `ViewContext` for the dialog (same pattern as view references)
3. Performs one-shot input channel propagation: copies values from parent channels into the dialog's channels
4. Builds the control tree from the loaded `ViewElement`
5. Calls `DialogManager.openDialog(closeOnBackdrop, rootControl, resultHandler)`

The dialog view XML is fully self-contained — it defines its own chrome, layout, content, and action buttons. `OpenDialogCommand` provides no chrome. The dialog uses existing view elements for structure (e.g., `<card>` for a titled container, `<bottom-bar>` for action buttons). No dedicated `<dialog-window>` element is needed — standard layout elements compose the dialog appearance.

**Dialog sizing:** The dialog dimensions are determined by the content of the view XML. The dialog's root element can use CSS styling to control width/height. No explicit size configuration on `OpenDialogCommand`.

**Channel mapping syntax:**

```xml
<button>
  <action class="...OpenDialogCommand"
    dialog-view="WEB-INF/views/demo/my-dialog.view.xml"
    close-on-backdrop="true">
    <inputs>
      <input from="selectedItem" to="item" />
      <input from="currentUser" to="user" />
    </inputs>
    <outputs>
      <output from="confirmedValue" to="result" />
    </outputs>
  </action>
</button>
```

- `<inputs>` — enclosing tag for input channel mappings (zero or more `<input>` children).
- `<input from="parentChannel" to="dialogChannel" />` — copies the value of `parentChannel` in the parent view into `dialogChannel` in the dialog at open time.
- `<outputs>` — enclosing tag for output channel mappings (zero or more `<output>` children).
- `<output from="dialogChannel" to="parentChannel" />` — defines which dialog channel maps back to which parent channel on close with result (mechanism deferred).

**Channel wiring semantics:**

- Input channels are a one-shot copy at open time. After the dialog opens, parent and dialog channels are independent — no live binding.
- Output channels (future): When the dialog closes with a result, values are copied back from dialog channels to parent channels. The exact mechanism for producing a result (beyond cancel) is deferred.

**ViewContext creation:**

The command receives a `ReactContext` at execution time. It obtains the parent `ViewContext` from the `ReactContext` (which holds a reference to it), then creates a new `DefaultViewContext` for the dialog — the same pattern used by `ReferenceElement`. The dialog's `ViewContext` is isolated: it has its own channel registry and command scope.

### CancelDialogCommand

**Location**: `com.top_logic.layout.view.command`

A zero-configuration `ViewCommand` that closes the topmost open dialog without propagating any results.

**Behavior:**

1. Gets the `DialogManager` from the `ViewContext`
2. Calls `DialogManager.closeTopDialog()` with a cancel/no-result signal
3. Output channel propagation is skipped

### Demo

Add a demo to the existing `DemoTypeA` view in `com.top_logic.demo`. The demo shows the full declarative dialog workflow: a tree displays `DemoTypeA` instances, and selecting one and pressing a button opens a dialog that shows the selected object in a form.

**Scenario:** The existing `DemoTypeA` tree view gets a new "Show Details" button. Clicking it opens a dialog that receives the tree's selection via input channel wiring and displays the selected object in a read-only form. The dialog has a "Close" button using `CancelDialogCommand`.

**Button in the existing demo view:**

```xml
<button>
  <action class="com.top_logic.layout.view.command.OpenDialogCommand"
    dialog-view="WEB-INF/views/demo/show-demo-type-a.view.xml"
    close-on-backdrop="true">
    <inputs>
      <input from="selection" to="model" />
    </inputs>
  </action>
</button>
```

The `selection` channel of the parent tree is copied into the `model` channel of the dialog at open time.

**Dialog view file (`show-demo-type-a.view.xml`):**

```xml
<view>
  <stack>
    <card title="DemoTypeA Details">
      <stack>
        <form model="model" />
        <bottom-bar>
          <button>
            <action class="com.top_logic.layout.view.command.CancelDialogCommand"
              label="Close" />
          </button>
        </bottom-bar>
      </stack>
    </card>
  </stack>
</view>
```

The `<form model="model" />` binds to the dialog's `model` channel, which received the selected `DemoTypeA` object from the parent view's `selection` channel.

## Architecture

```
Button click
  → OpenDialogCommand.execute()
    → ViewLoader.getOrLoadView(dialogViewPath)
    → new ViewContext (isolated from parent)
    → copy input channel values (one-shot)
    → ViewElement.createControl(dialogViewContext)
    → DialogManager.openDialog(closeOnBackdrop, rootControl, handler)

Cancel button click (inside dialog)
  → CancelDialogCommand.execute()
    → DialogManager.closeTopDialog(no result)
    → output channels NOT propagated
```

## Scope

**In scope:**
- `OpenDialogCommand` with `dialog-view` and `close-on-backdrop` configuration
- `CancelDialogCommand` (zero-config, closes topmost dialog)
- Input channel mapping configuration (one-shot copy at open time)
- Output channel mapping configuration (wiring defined, propagation deferred)
- Demo in `com.top_logic.demo` showing declarative dialog opening

**Deferred:**
- Result propagation mechanism (how a dialog produces a result on close)
- Explicit `CloseDialogCommand` with result collection
- Live channel binding between parent and dialog

## Error Handling

- **Missing dialog view file**: `ViewLoader` throws `ConfigurationException` — logged as error, dialog not opened.
- **Parse errors in dialog view XML**: Same as any view XML parse error — `InstantiationContext` collects errors, logged.
- **Unknown input channel**: If a referenced parent channel does not exist, log a warning and skip that mapping.
- **CancelDialogCommand with no open dialog**: No-op (no dialog to close).
