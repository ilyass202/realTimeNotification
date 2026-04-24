import { useEffect } from 'react';
import { useSelector } from 'react-redux';
import { useSaveTokenMutation } from '../Store/HttpSlices/tokenSlice';
import Notification from '../service/NotificationService';

export const useFcmRefresh = () => {
  const userId = useSelector((state) => state.user.userId);
  const [saveToken] = useSaveTokenMutation();

  useEffect(() => {
    const unsubscribe = Notification.tokenRefreshListener(async (token) => {
      if (!token) {
        return;
      }

      if (!userId) {
        console.log('userId absent, impossible d envoyer le token rafraichi');
        return;
      }

      try {
        await saveToken({ userId, fcmToken: token });
        console.log('token rafraichi enregistre');
      } catch (err) {
        console.log('token refresh upload fail', err);
      }
    });

    return () => {
      if (typeof unsubscribe === 'function') {
        unsubscribe();
      }
    };
  }, [saveToken, userId]);
};