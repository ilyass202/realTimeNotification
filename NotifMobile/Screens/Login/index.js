import React from 'react';
import { Alert } from 'react-native';
import { useDispatch } from 'react-redux';
import { setUserId } from '../../Store/Slices/UserSlice';
import AuthForm from '../../Components/AuthForm';
import Notification from '../../service/NotificationService';
import { useSaveTokenMutation } from '../../Store/HttpSlices/tokenSlice';
import { useLoginMutation } from '../../Store/HttpSlices/authSlice';

const Login = ({ navigation }) => {
  const dispatch = useDispatch();
  const [login] = useLoginMutation();
  const [saveToken] = useSaveTokenMutation();
  const submitForm = async (values) => {
    try {
      const data = await login({ email: values.email, password: values.password }).unwrap();
      const userId = data?.userId || data?.id;
      if (!userId) throw new Error('ID utilisateur manquant dans la réponse de connexion');
      dispatch(setUserId(userId));

      await Notification.requestPermission();
      const token = await Notification.getToken();
      if (token) {
        await saveToken({ userId, token });
      }

      navigation.replace('Home');
    } catch (error) {
      let errorMessage = 'Une erreur est survenue';
      if (error?.message) {
        errorMessage = error.message;
      } else if (error?.data?.message) {
        errorMessage = error.data.message;
      } else if (error?.status) {
        errorMessage = `Erreur serveur (${error.status})`;
      }
      Alert.alert('Erreur', errorMessage);
    }
  };

  const navigate = () => {
    navigation.replace('Register');
  };

  return <AuthForm isLogin={true} submitForm={submitForm} navigate={navigate} />;
};

export default Login;