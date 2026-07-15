// Entry point for the TL-Script editor bundle.
//
// IMPORTANT: All control components MUST import React from 'tl-react-bridge' (not 'react').
import { register } from 'tl-react-bridge';

import TLScriptEditor from './controls/TLScriptEditor';

register('TLScriptEditor', TLScriptEditor);
