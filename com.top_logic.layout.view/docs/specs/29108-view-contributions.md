# Cross-module view contributions

## Problem

A view defined in one module cannot be optionally extended by a depending
module. Module A provides a view V1 (e.g. the admin view); module B depends on A
and wants to add a sub-view V2. An app that only depends on A should get V1
without V2; an app that depends on A and B should get V1 with V2.

The only previous workaround was a forward `<view-ref>` from V1 to a file shipped
by B. Because `<view-ref>` resolves through the merged webapp and **hard-fails**
when the target file is absent, that forces *every* app using A to also ship B —
the dependency points the wrong way.

## Approach: named extension points + contributions

Inverted from the legacy `.layout.overlay.xml` mechanism (which *mutated* a base
component by name and could not add top-level entries). Instead:

- An upstream view declares a **named extension point** on a content container —
  a deliberate, documented insertion site (e.g. the admin tab-bar exposes
  `admin.sections`).
- A downstream module declares a **contribution** targeting that point by its
  stable string id. The contributor names the anchor; the anchor's owner never
  names the contributor, so the dependency only ever points upstream.

Discovery reuses TopLogic's strongest cross-module-without-dependency primitive:
a service whose configuration is a `@Key`-merged list. Module config fragments
merge additively by key (the same mechanism behind `LabelProviderService`), so
any module appends contributions without anyone depending on it.

## Parts

### `ViewContributionService`

A `ConfiguredManagedClass` (package `com.top_logic.layout.view.contribution`).
Its config holds a keyed `List<Contribution>`; the service indexes them by
`target` and orders each group by `rank` (then `id`). Activated in
`tl-layout-view.conf.config.xml`.

### `Contribution`

| Property  | Meaning                                                            |
|-----------|-------------------------------------------------------------------|
| `id`      | Stable unique id; the merge key and the default route segment.    |
| `target`  | The extension-point id to contribute into.                        |
| `view`    | Path of the `.view.xml` to embed (relative to `/WEB-INF/views/`). |
| `label`   | Presentation label for the produced tab / item.                   |
| `icon`    | Presentation icon (CSS icon class).                               |
| `route`   | Optional route-segment override (defaults to `id`).               |
| `rank`    | Order among contributions to the same target (default `100`).     |

The contribution is pure wiring; the contributed content stays an ordinary
`.view.xml` loaded through the existing `<view-ref>` machinery.

### Extension point on a container

A container exposes an extension point through an `extension-id` attribute. The
first consumer is `<tab-bar extension-id="...">`: after its static `<tab>`s,
`TabBarElement` appends one native tab per contribution (ordered by `rank`),
each tab's content being a `view-ref` to the contributed view. Contributed tabs
are first-class — access-controlled, routed and ordered exactly like
hand-authored tabs.

Further containers (sidebar, generic child lists, command bars) can expose an
`extension-id` the same way; each maps a contribution to its own entry type
while sharing the one `Contribution` payload.

## Why this is better than the legacy overlay

- **Decoupled**: a contribution names a published anchor by string id; it never
  reaches into a base view's internal element names.
- **Top-level adds are first-class**: an extension point is a sanctioned
  insertion site, curing the legacy "cannot add a new top-level component".
- **Stable ordering**: by `rank` / `id`, not by fragile deep resource-path
  references.
- **Inspectable**: the service can list every contribution to a target.
- **Minimal surface**: reuses the existing config-merge, `<view-ref>` loading and
  container build paths; no new merge engine, no offline tooling.

## First consumer

The admin tab-bar in `admin/admin.view.xml` (module `tl-layout-view`) declares
`extension-id="admin.sections"` and no longer hard-codes a Development section.
Module `tl-model-search-react` contributes the Development section (the
TL-Script console) into `admin.sections`. An app with only `tl-layout-view`
shows the admin view without Development; an app that also has
`tl-model-search-react` gets it.
