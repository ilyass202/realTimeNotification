import { PermissionsAndroid, Platform } from "react-native";
import messaging from "@react-native-firebase/messaging";
import notifee, { AndroidImportance } from '@notifee/react-native';

class Notification {
  static async requestPermission() {
    if (Platform.OS === 'android') {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
        {
          title: "Notification Permission",
          message: "L'application a besoin de votre permission pour envoyer des notifications",
        }
      );

      if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
        return null;
      }
    }

    const authStatus = await messaging().requestPermission();
    const enabled =
      authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
      authStatus === messaging.AuthorizationStatus.PROVISIONAL;

    if (!enabled) {
      return null;
    }

    return this.getToken();
  }

  static async getToken() {
    try {
      return await messaging().getToken();
    } catch (error) {
      return null;
    }
  }

  static tokenRefreshListener(onRefresh) {
    const unsubscribe = messaging().onTokenRefresh(async (token) => {
      if (typeof onRefresh === 'function') {
        onRefresh(token);
      }
    });

    return unsubscribe;
  }

  static async setupChannel() {
    try {
      const channel = await notifee.createChannel({
        id: 'default',
        name: 'default channel',
        android: {
          importance: AndroidImportance.HIGH,
          sound: 'default',
          vibration: true,
        },
      });

      return channel;
    } catch (error) {
      console.log('error in creating channel', error);
      return null;
    }
  }

  static async displayNotification(remoteMessage) {
    try {
      console.log('remoteMessage:', JSON.stringify(remoteMessage, null, 2));
      if (!remoteMessage?.data) {
        return;
      }

      await this.setupChannel();
      await notifee.displayNotification({
        title: remoteMessage.data.title,
        body: remoteMessage.data.body,
        android: {
          channelId: 'default',
          sound: 'default',
          smallIcon: 'ic_message',
          pressAction: {
            id: 'default',
          },
          color: remoteMessage.data.critical === "true" ? '#ff0000' : '#2196f3',
        },
      });
    } catch (error) {
      console.log('error in displaying notification', error);
    }
  }
  static foregroundHandler = async ( )=> {
    messaging().onMessage(async remoteMessage => {
      console.log('Foreground message received:', JSON.stringify(remoteMessage, null, 2));
      console.log('Data:', remoteMessage.data);
      console.log('Montant:', remoteMessage.data?.montant);
      await this.displayNotification(remoteMessage);
    }
    );
  }
  static backgroundHandler = async () => {
    messaging().setBackgroundMessageHandler(async remoteMessage => {
        console.log('Background message received:', JSON.stringify(remoteMessage, null, 2));
        console.log('Data:', remoteMessage.data);
        console.log('Montant:', remoteMessage.data?.montant);
        await this.displayNotification(remoteMessage);
    });
  }
}

export default Notification;
