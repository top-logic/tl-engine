---
description: Read before starting or testing in a demo application - URLs and login of tl-demo (classic UI), tl-demo-react (/view/) and tl-ai-demo (React and classic UI), the mui profile, and notes for scripted tests of the classic UI.
order: 50
---

# Demo apps — URLs and login

Three demo applications exist (see the "Repository Structure" section of the root `CLAUDE.md`). Each is served below its artifact ID as context path, e.g. `/tl-demo/`:

- **`tl-demo`** (`com.top_logic.demo`) — the classic layout UI only (Strukturen / Tabellen / … tabs, classic tree / table / grid components), at **`/tl-demo/servlet/LayoutServlet`**. It does not depend on `tl-layout-react` / `tl-layout-view`, so it has no React UI and none of the services of these modules.
- **`tl-demo-react`** (`com.top_logic.demo.react`) — the React-only demo, served at `/view/`. This is the successor test bed for React UI features.
  Built with the Maven profile `mui` (`mvn install -pl com.top_logic.layout.react.mui,com.top_logic.demo.react.mui,com.top_logic.demo.react -P mui`, started with the same profile, e.g. `MAVEN_ARGS=-Pmui`), it renders all replaceable components with Material UI: the profile adds `com.top_logic.demo.react.mui`, which installs the Material UI module `com.top_logic.layout.react.mui` with the theme of the demo (see the article `view-layer/customer-component-library` of `tl-layout-view`).
- **`tl-ai-demo`** (`tl-ai-demo`) — the demo application of the AI modules (see the chapter `ai` of `tl-ai-core`). It has both UIs: it starts and logs in on its React UI at `/view/` (the `ApplicationPages` configuration in `tl-ai-demo.conf.config.xml`); the classic UI, with the chat tab of `tl-chat-classic`, stays reachable at `/servlet/LayoutServlet`.

Each needs its own `root` / `root1234` login. `tl-demo-react` requires it before anything is shown: it configures `login-view="demo-login-page.view.xml"` in its `ViewConfig`, so an anonymous session sees the login page whatever URL it asks for, and the requested URL is the one it lands on after the login.

## Scripted-test notes (classic UI)

- `LabeledButtonActionOp` with only `component-name` + `label` (no `<business-object>`) clicks a component command button such as `invalidate`; adding a `<business-object>` restricts it to table-data buttons.
- Fresh test DBs are empty — create data first (e.g. `script:/demo/create-A.xml`); do not assume `NodeA` / `NodeB` / `NodeC` exist.
