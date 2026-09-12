# FAQ: Demo apps — URLs and login

Two demo applications exist (see the "Repository Structure" section of the root `CLAUDE.md`):

- **`tl-demo`** (`com.top_logic.demo`) — the classic layout UI (Strukturen / Tabellen / … tabs, classic tree / table / grid components) is at **`/tl-demo/servlet/LayoutServlet`**. Its React `app.view.xml` demo is at `/view/` (`tl-layout-view` overrides `startPage` to it).
- **`tl-demo-react`** (`com.top_logic.demo.react`) — the React-only demo, served at `/view/`. This is the successor test bed for React UI features.

Each needs its own `root` / `root1234` login.

## Scripted-test notes (classic UI)

- `LabeledButtonActionOp` with only `component-name` + `label` (no `<business-object>`) clicks a component command button such as `invalidate`; adding a `<business-object>` restricts it to table-data buttons.
- Fresh test DBs are empty — create data first (e.g. `script:/demo/create-A.xml`); do not assume `NodeA` / `NodeB` / `NodeC` exist.
