// The bundle tl-react-mui: renders the TopLogic React UI with Material UI, and is the Material UI
// of the page.
//
// Loading the bundle changes nothing on the page. The application installs Material UI by calling
// installMui() with its theme (install.ts).
//
// The bundle re-exports Material UI (`@mui/material`, which contains `@mui/material/styles` and the
// `colors`, and `@mui/x-date-pickers`): the theme and the MUI-based controls of the application
// import Material UI from here, so that they share one instance of it -- and with it the theme, the
// style cache and the localization the root wrapper provides -- with the adapters of this module.

export * from '@mui/material';
export * from '@mui/x-date-pickers';
export { installMui, COMPONENT_NAMES, ALL_COMPONENTS } from './install';
export type { ComponentName, MuiOptions } from './install';
