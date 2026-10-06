// The MUI theme of the application: the single source of the look of the UI. installMui() of
// tl-react-mui renders the MUI components with it unchanged, and the TopLogic components follow it
// through the styling properties derived from it.
//
// The theme of this demo is the theme of the "Onepirate" template of Material UI, ported to
// MUI 9 `createTheme` options:
//   https://raw.githubusercontent.com/mui/material-ui/v4.12.4/docs/src/pages/premium-themes/onepirate/modules/theme.js
//   (component overrides: the sibling directory modules/components/)
// Copyright (c) 2014 Call-Em-All, licensed under the MIT License
// (https://github.com/mui/material-ui/blob/v4.12.4/LICENSE).
//
// The template's keys outside the MUI theme are left out (`xLight`, `background.placeholder`,
// `typography.fontHeader`, `typography.fontFamilySecondary`); its header style is part of the
// heading variants `h1` … `h6` here. Its fonts (Work Sans, Roboto Condensed) are bundled with the
// module (Fontsource, SIL Open Font License 1.1) and published as its stylesheet.
//
// Material UI is imported from tl-react-mui, the Material UI of the page.

import { colors } from 'tl-react-mui';
import type { ThemeOptions } from 'tl-react-mui';
import '@fontsource/work-sans/latin-300.css';
import '@fontsource/work-sans/latin-400.css';
import '@fontsource/work-sans/latin-700.css';
import '@fontsource/roboto-condensed/latin-700.css';

/** The font of the texts. */
const FONT_FAMILY = "'Work Sans', sans-serif";

/** The font of the headings and the buttons. */
const FONT_FAMILY_SECONDARY = "'Roboto Condensed', sans-serif";

/** Weight of the light texts (Work Sans). */
const FONT_WEIGHT_LIGHT = 300;

/** Weight of the regular texts (Work Sans). */
const FONT_WEIGHT_REGULAR = 400;

/** Weight of the emphasized texts (Roboto Condensed). */
const FONT_WEIGHT_MEDIUM = 700;

/** The style the headings share: the secondary font, emphasized, in capitals. */
const FONT_HEADER = {
  fontWeight: FONT_WEIGHT_MEDIUM,
  fontFamily: FONT_FAMILY_SECONDARY,
  textTransform: 'uppercase',
} as const;

/**
 * The options of the customer's MUI theme, as `createTheme` takes them.
 *
 * <p>Of the template's components, the button is part of the theme: square, in the secondary
 * font, without a shadow. Its padding is left out: the template sizes the large call-to-action
 * buttons of a landing page with it, the buttons of an application keep the sizes of MUI. The
 * template's app bar is flat (elevation 0); its other components (paper, text field, snackbar,
 * toolbar) are wrappers with properties of their own and not part of the theme.</p>
 */
const customerTheme: ThemeOptions = {
  palette: {
    primary: {
      light: '#69696a',
      main: '#28282a',
      dark: '#1e1e1f',
    },
    secondary: {
      light: '#fff5f8',
      main: '#ff3366',
      dark: '#e62958',
    },
    warning: {
      main: '#ffc071',
      dark: '#ffb25e',
    },
    error: {
      main: colors.red[500],
      dark: colors.red[700],
    },
    success: {
      main: colors.green[500],
      dark: colors.green[700],
    },
    background: {
      default: '#fff',
    },
  },
  typography: palette => ({
    fontFamily: FONT_FAMILY,
    fontSize: 14,
    fontWeightLight: FONT_WEIGHT_LIGHT,
    fontWeightRegular: FONT_WEIGHT_REGULAR,
    fontWeightMedium: FONT_WEIGHT_MEDIUM,
    h1: { ...FONT_HEADER, color: palette.text.primary, letterSpacing: 0, fontSize: 60 },
    h2: { ...FONT_HEADER, color: palette.text.primary, fontSize: 48 },
    h3: { ...FONT_HEADER, color: palette.text.primary, fontSize: 42 },
    h4: { ...FONT_HEADER, color: palette.text.primary, fontSize: 36 },
    h5: { fontSize: 20, fontWeight: FONT_WEIGHT_LIGHT },
    h6: { ...FONT_HEADER, color: palette.text.primary, fontSize: 18 },
    subtitle1: { fontSize: 18 },
    // The template swaps the base styles of the two body variants and resizes them.
    body1: { fontWeight: FONT_WEIGHT_REGULAR, fontSize: 16, lineHeight: 1.43, letterSpacing: '0.01071em' },
    body2: { fontSize: 14, lineHeight: 1.5, letterSpacing: '0.00938em' },
  }),
  components: {
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: {
          borderRadius: 0,
          fontWeight: FONT_WEIGHT_MEDIUM,
          fontFamily: FONT_FAMILY_SECONDARY,
        },
      },
    },
    MuiAppBar: {
      defaultProps: {
        elevation: 0,
      },
    },
  },
};

export default customerTheme;
