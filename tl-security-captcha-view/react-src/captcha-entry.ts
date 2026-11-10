// Captcha control registration for the tl-security-captcha-view bundle.
//
// IMPORTANT: All components MUST import React from 'tl-react-bridge' (not 'react').

import { register } from 'tl-react-bridge';
import TLCaptcha from './controls/TLCaptcha';

register('TLCaptcha', TLCaptcha);
