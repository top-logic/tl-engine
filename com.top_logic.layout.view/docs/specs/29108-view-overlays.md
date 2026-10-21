# Cross-module view extension via overlays

## Problem

A view defined in one module must be optionally extendable by a depending
module. Module A provides a view V (e.g. the admin view); module B depends on A
and wants to add a section. An app depending only on A gets V unchanged; an app
depending on A and B gets V with B's addition. The dependency must point only
upstream (B knows A; A never knows B).

A forward `<view-ref>` from V to a file shipped by B does not work: it
**hard-fails** when B is absent, forcing every app using A to also ship B — the
dependency points the wrong way.

## Approach: same-path view overlays

A view is extended by **dropping a file at the same resource path** in the
depending module. `WEB-INF/views/admin/admin.view.xml` in the framework is the
base; a module that ships its own `WEB-INF/views/admin/admin.view.xml` overlays
it. No extension point is declared and no registry is involved — any view is
extensible by path, and the overlay uses the standard typed-configuration list
merge (`config:operation` / `config:position`) to add, reposition or override
elements.

This is the legacy layout-overlay concept on the typed-config view stack, but
with config-grade merge semantics instead of file replacement.

### The three enabling pieces

- **Multiplicity** — `FileManager.getDataOverlays(path)` returns *all* same-path
  copies across the stacked module fragments (the classpath is a multi-map; a
  flattened WAR is not — see build-time merge below).
- **Order** — the copies must be applied base-first, then each dependent
  module's overlay after its dependencies, so `config:position` can reference
  base elements. The single ordering authority is
  `DependencyResolver.createBuildOrder()` (topological over the POM graph), the
  same one that orders the config `metaConf` chain and the legacy layouts.
  `getDataOverlays()` returns highest-priority first, so the list is reversed
  into dependency order before merging.
- **Merge** — `ConfigurationReader.setSources(list)` folds the ordered sources
  (the first is the base view, the rest are increments) with the same increment
  engine that merges application-config fragments.

### Prerequisites on the view model

- `ViewElement.getContent()` is **single-valued** so an overlay's content
  recurse-merges into the base content element instead of replacing or appending
  it.
- Every element list of the view is **keyed** by `id` so entries merge and
  `config:position` can address them: the child lists of the elements
  (`List<UIElement.Config>`, `@Key(UIElement.Config.ID)`, the optional `id` of
  `UIElement.Config`), `TabBarElement.getTabs()`, `SidebarElement.getItems()`
  (via the shared `SidebarItemConfig` base), the panes of a split panel, the
  cases of a switch, the steps of a wizard and the tiles of a dashboard. An
  entry without id is **anonymous**: it is appended in order, matches no entry
  of another source, and any number of anonymous entries may occur.
  `config:index="n"` addresses the entry at position n of the list as merged so
  far, which is the only way to descend into an anonymous element. Positioning
  still requires ids on the elements you position against; contributing to the
  *end* does not. The four slots of `<app-shell>` are keyed by the kind of
  their element instead (at most one element per kind).

## Runtime and build

- **Development / workspace** (one resource root per module): `ViewLoader`
  merges on the fly via `getDataOverlays` → reverse → `setSources`, caching on a
  signature over *all* overlay files so an edit to any contributor invalidates
  it.
- **Deployed WAR** (all module webapps flattened into one tree — multiplicity
  lost): `ViewOverlayMerger` runs during application-WAR assembly, while the
  dependency-ordered `FileManager` still exposes every module copy, and writes
  the merged view into the target webapp. It reuses `ViewLoader.loadConfig`, so
  build and runtime share one merge implementation. It is invoked reflectively
  from `LayoutCreator.createLayouts` so the core layer keeps no dependency on the
  view module.

## Failure modes (loud, not silent)

- A dangling `config:reference` (e.g. a base tab a contributor positions against
  was renamed) fails with the **view path** named.
- If the lowest-priority copy of a view is itself an overlay (uses
  `config:operation`) there is no base to extend — typically the base view was
  renamed or removed while contributor overlays still target its old path —
  which raises an explicit *"No base view found"* error instead of rendering an
  incomplete view.

## Consumers

- **Admin**: the framework's self-contained `admin/admin.view.xml`;
  `tl-model-search-react` and `tl-dev-tools` each drop a same-path overlay adding
  the Development and Model sections (`config:position` places Model after
  `system`, Development at the end).
- **Playground**: a framework `views/playground/playground.view.xml` that the
  React demo extends with a same-path overlay inserting an application tab before
  `settings`.
