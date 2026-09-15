import { register } from 'tl-react-bridge'
import { App } from './App'
import { TLChatSessions } from './chat-sessions'
import './styles.css'

register('TLAgentChat', App)
register('TLChatSessions', TLChatSessions)
