import { Provider } from 'react-redux';
import { store } from './Store/store';
import { NavigationContainer as NavigatorContainer } from '@react-navigation/native';
import MainStackNavigator from './Navigators/MainStackNavigator';
import Notification from './service/NotificationService';
import { useFcmRefresh } from './CustomHook/FcmRefresh';
import { useEffect } from 'react';
const AppContent = () => {
  useFcmRefresh();

  useEffect(() => {
    const initNotification = async () => {
      try {
        await Notification.requestPermission();
        Notification.foregroundHandler();
      } catch (error) {
        console.log('Notification init failed', error);
      }
    };

    initNotification();
  }, []);

  return (
    <NavigatorContainer>
      <MainStackNavigator />
    </NavigatorContainer>
  );
};

function App() {
  return (
    <Provider store={store}>
      <AppContent />
    </Provider>
  );
}

export default App;
