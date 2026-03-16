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

The dialog view XML is fully self-contained — it defines its own window chrome (title bar, close button, layout, action buttons). `OpenDialogCommand` provides no chrome.

**Channel wiring:**

- Input channels are a one-shot copy at open time. After the dialog opens, parent and dialog channels are independent — no live binding.
- Output channels (future): When the dialog closes with a result, values are copied back from dialog channels to parent channels. The exact mechanism for producing a result (beyond cancel) is deferred.

### CancelDialogCommand

**Location**: `com.top_logic.layout.view.command`

A zero-configuration `ViewCommand` that closes the topmost open dialog without propagating any results.

**Behavior:**

1. Gets the `DialogManager` from the `ViewContext`
2. Calls `DialogManager.closeTopDialog()` with a cancel/no-result signal
3. Output channel propagation is skipped

### Demo

Add a demo to the existing `DemoTypeA` instance view in `com.top_logic.demo`:

- A new button on the `DemoTypeA` list view uses `OpenDialogCommand` to open a dialog
- The dialog is defined in a separate `.view.xml` file
- Dialog content is placeholder for now (demonstrates the mechanism), to be expanded into a full creation form later

**Example usage:**

Button in the existing demo view:
```xml
<button>
  <action class="com.top_logic.layout.view.command.OpenDialogCommand"
    dialog-view="WEB-INF/views/demo/create-demo-type-a.view.xml"
    close-on-backdrop="true"
  />
</button>
```

Dialog view file (`create-demo-type-a.view.xml`):
```xml
<view>
  <stack>
    <card title="Create DemoTypeA">
      <stack>
        <p>Dialog content goes here.</p>
        <bottom-bar>
          <button>
            <action class="com.top_logic.layout.view.command.CancelDialogCommand"
              label="Cancel" />
          </button>
        </bottom-bar>
      </stack>
    </card>
  </stack>
</view>
```

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
