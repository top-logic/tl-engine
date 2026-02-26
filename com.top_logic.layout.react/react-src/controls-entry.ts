// Control registration for the tl-react-controls bundle.
//
// IMPORTANT: All control components MUST import React from 'tl-react-bridge' (not 'react').
// The controls bundle externalizes 'tl-react-bridge' so it shares the single React instance
// provided by the bridge.  Importing from 'react' directly bundles a duplicate React copy,
// which breaks hooks at runtime ("useState is null").
//
// Use:   import { React, useTLState, useTLCommand } from 'tl-react-bridge';
//        React.useState(...)   React.useRef(...)   React.useCallback(...)
import { register } from 'tl-react-bridge';

import TLTextInput from './controls/TLTextInput';
import TLNumberInput from './controls/TLNumberInput';
import TLDatePicker from './controls/TLDatePicker';
import TLSelect from './controls/TLSelect';
import TLCheckbox from './controls/TLCheckbox';
import TLTable from './controls/TLTable';
import TLButton from './controls/TLButton';
import TLToggleButton from './controls/TLToggleButton';
import TLCounter from './controls/TLCounter';
import TLTabBar from './controls/TLTabBar';
import TLFieldList from './controls/TLFieldList';
import TLAudioRecorder from './controls/TLAudioRecorder';

register('TLButton', TLButton);
register('TLToggleButton', TLToggleButton);
register('TLTextInput', TLTextInput);
register('TLNumberInput', TLNumberInput);
register('TLDatePicker', TLDatePicker);
register('TLSelect', TLSelect);
register('TLCheckbox', TLCheckbox);
register('TLTable', TLTable);
register('TLCounter', TLCounter);
register('TLTabBar', TLTabBar);
register('TLFieldList', TLFieldList);
register('TLAudioRecorder', TLAudioRecorder);
