# FAQ: Demo apps — URLs and login

Two demo applications exist (see the "Repository Structure" section of the root `CLAUDE.md`):

- **`tl-demo`** (`com.top_logic.demo`) — the classic layout UI (Strukturen / Tabellen / … tabs, classic tree / table / grid components) is at **`/tl-demo/servlet/LayoutServlet`**. Its React `app.view.xml` demo is at `/view/` (`tl-layout-view` overrides `startPage` to it).
- **`tl-demo-react`** (`com.top_logic.demo.react`) — the React-only demo, served at `/view/`. This is the successor test bed for React UI features.
  Built with the Maven profile `corporate-example` (`mvn install -pl com.top_logic.demo.react -P corporate-example`, started with the same profile, e.g. `MAVEN_ARGS=-Pcorporate-example`), it renders buttons and checkboxes with the example customer component library of `com.top_logic.demo.react.corporate`.

Each needs its own `root` / `root1234` login. `tl-demo-react` requires it before anything is shown: it configures `login-view="login-page.view.xml"` in its `ViewConfig`, so an anonymous session sees the login page whatever URL it asks for, and the requested URL is the one it lands on after the login. The React view of `tl-demo` configures no login view, so it is browsed anonymously and the login is reached through the account area of its app bar.

## Scripted-test notes (classic UI)

- `LabeledButtonActionOp` with only `component-name` + `label` (no `<business-object>`) clicks a component command button such as `invalidate`; adding a `<business-object>` restricts it to table-data buttons.
- Fresh test DBs are empty — create data first (e.g. `script:/demo/create-A.xml`); do not assume `NodeA` / `NodeB` / `NodeC` exist.
