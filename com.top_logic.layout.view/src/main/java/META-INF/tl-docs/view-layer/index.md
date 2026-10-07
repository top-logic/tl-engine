---
description: Read before writing or changing a .view.xml of the React view layer (com.top_logic.layout.view) - composing panels, forms, tables, dialogs, commands and channels, or extending the application shell (app.view.xml).
order: 10
---

# React view layer

`com.top_logic.layout.view` is declarative: a `.view.xml` assembles existing React controls through `UIElement` configs (each a `@TagName`) and view commands / actions. A feature is built by composing these via XML plus small Java `ViewCommand` / `ViewAction` / `ViewExecutabilityRule` / `UIElement` classes that reuse existing controls - not by hand-rolling bespoke React components. The articles of this chapter describe the elements available for that composition, from the spacing and height contracts to tables, navigation and the application shell.
