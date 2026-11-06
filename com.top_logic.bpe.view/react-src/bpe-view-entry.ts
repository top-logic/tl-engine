// Control registration of the tl-bpe-view bundle.
//
// IMPORTANT: All components MUST import React from 'tl-react-bridge' (not 'react').

import { register } from 'tl-react-bridge';
import TLBpmnEditor from './controls/TLBpmnEditor';

register('TLBpmnEditor', TLBpmnEditor);
