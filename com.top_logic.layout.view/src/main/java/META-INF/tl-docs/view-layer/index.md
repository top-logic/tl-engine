---
description: Read before writing or changing a .view.xml of the React view layer (com.top_logic.layout.view) - composing panels, forms, tables, dialogs, commands and channels, or extending the application shell (app.view.xml).
order: 10
---

# React view layer

`com.top_logic.layout.view` is declarative: a `.view.xml` assembles existing React controls through `UIElement` configs (each a `@TagName`) and view commands / actions. A feature is built by composing these via XML plus small Java `ViewCommand` / `ViewAction` / `ViewExecutabilityRule` / `UIElement` classes that reuse existing controls - not by hand-rolling bespoke React components. The articles of this chapter describe the elements available for that composition, from the spacing and height contracts to tables, navigation and the application shell.

- [Basics](doc:view-layer/basics) - the composition principle, forms and view-owned values, the spacing model, the fill contract and `css-class`.
- [Layout](doc:view-layer/layout) - `<stack>` and `<grid>`, weighted `<columns>`, `<object-list>`, `<visible-if>` and `<accordion>`.
- [Displaying values](doc:view-layer/display) - text variants and tones, `<image>` / `<overlay>` / `<avatar>`, `<html>`, colored values and progress.
- [Theme tokens in React stylesheets](doc:view-layer/theme-tokens) - the tokens a stylesheet consumes: colors by role, typography, radius tiers, elevation, and the audit test.
- [Forms and input fields](doc:view-layer/forms) - display variants of input fields and the unsaved-changes question before a channel write.
- [Commands and actions](doc:view-layer/commands) - action chains and branching, toolbar groups, creating and deleting objects, long-running jobs.
- [Tables](doc:view-layer/tables) - `TableViewControl`, selection, row activation, grouping, filtering, pinned columns, drag and drop.
- [Navigation](doc:view-layer/navigation) - `<adaptive-detail>`, `<tile-stack>`, `<wizard>`, object navigation and URL routing.
- [The application shell](doc:view-layer/app-shell) - extending `app.view.xml`, the sidebar, login and anonymous sessions, transitions.
- [UI inspector, diagnostics and assertions](doc:view-layer/diagnostics) - inspecting a running UI and asserting on it.
- [A new `UIElement` with a client component of its own](doc:view-layer/new-ui-element) - when an element is justified, and the files, state contract and registration it needs.
