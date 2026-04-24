/**
 * @format
 */

//import 'react-native-gesture-handler';
import { AppRegistry } from 'react-native';
import App from './App';
import { name as appName } from './app.json';
import Notification from './service/NotificationService';
import { enableScreens } from 'react-native-screens';
enableScreens();
try {
  Notification.backgroundHandler();
} catch (error) {
  console.log('Notification background handler failed', error);
}
AppRegistry.registerComponent(appName, () => App);