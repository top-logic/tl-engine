# Design: ViewAction Chain for Generic View Commands

**Ticket**: #29108
**Date**: 2026-03-23

## Problem

The view system needs a way to compose reusable command building blocks. Specifically, "create object in dialog" requires: (1) create a transient object, (2) open a dialog with it. These steps should be independently reusable.

## Architecture

### ViewAction

Lightweight functional interface. Same context as ViewCommand but returns `Object` (the result) instead of `HandlerResult`. No configuration for label, image, executability — purely functional.

```java
public interface ViewAction {
    Object execute(ReactContext context, Object input);
}
```

### GenericViewCommand

A `ViewCommand` with a configured `List<ViewAction>`. Executes the chain sequentially — each action's return value becomes the next action's input.

```java
@TagName("generic-command")
public interface Config extends ViewCommand.Config {
    @DefaultContainer
    List<PolymorphicConfiguration<ViewAction>> getActions();
}
```

### ExecuteScriptAction

Evaluates a configured TL-Script **function** with the input as argument. The function is called as `f(input)` — no implicit variables.

```xml
<execute-script function="x -> new(`test.constraints:ConstraintTestType`, transient: true)"/>
```

### OpenDialogAction

Opens a dialog. The input object is injected into a named dialog channel (default: `model`).

```xml
<open-dialog dialog-view="demo/create-constraint-test.view.xml" bind-input-to="model"/>
```

### OpenDialogCommand Refactoring

The existing `OpenDialogCommand` delegates to `OpenDialogAction` internally. Its additional config (label, bindings, close-on-backdrop) wraps around the action.

## Usage: Create Object in Dialog

```xml
<command class="...GenericViewCommand" label="New" placement="TOOLBAR">
    <execute-script function="x -> new(`test.constraints:ConstraintTestType`, transient: true)"/>
    <open-dialog dialog-view="demo/create-constraint-test.view.xml" bind-input-to="model"/>
</command>
```

The dialog view:
```xml
<view>
    <channels><channel name="model"/></channels>
    <window title="New Object" width="600px">
        <form input="model" initial-edit-mode="true" mode-switch="false">
            <field attribute="mandatoryString"/>
            ...
        </form>
        <actions>
            <button><action class="...CommitCreateCommand" label="Create" input="model"/></button>
            <button><action class="...CancelDialogCommand" label="Cancel"/></button>
        </actions>
    </window>
</view>
```
