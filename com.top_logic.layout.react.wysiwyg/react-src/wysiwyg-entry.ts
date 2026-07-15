// TipTap WYSIWYG editor registration for the tl-react-wysiwyg bundle.
//
// IMPORTANT: All components MUST import React from 'tl-react-bridge' (not 'react').

import { register } from 'tl-react-bridge';
import TLWysiwygEditor from './controls/TLWysiwygEditor';

register('TLWysiwygEditor', TLWysiwygEditor);
