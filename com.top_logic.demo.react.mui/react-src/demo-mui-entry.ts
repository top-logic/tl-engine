// Renders the React demo with Material UI, in the theme of the application (customerTheme.ts).

import { installMui } from 'tl-react-mui';
import customerTheme from './customerTheme';

installMui({ theme: customerTheme, replace: 'all' });
