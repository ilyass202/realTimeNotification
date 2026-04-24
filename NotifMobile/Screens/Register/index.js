import React from 'react';
import { Alert } from 'react-native';
import { useDispatch } from 'react-redux';
import { setUserId } from '../../Store/Slices/UserSlice';
import AuthForm from '../../Components/AuthForm';
import Notification from '../../service/NotificationService';
import { useSaveTokenMutation } from '../../Store/HttpSlices/tokenSlice';
import { useRegisterMutation } from '../../Store/HttpSlices/authSlice';
import AsyncStorage from '@react-native-async-storage/async-storage';

const Register = ({ navigation }) => {
  const dispatch = useDispatch();
  const [saveToken] = useSaveTokenMutation();
  const [register] = useRegisterMutation();

  const submitForm = async (values) => {
    try {
      const object = {
        name: values.name,
        email: values.email,
        password: values.password
      }
      const data = await register(object).unwrap()
      .then((response) => {
        AsyncStorage.setItem('credentials', JSON.stringify(object));
        return response
      });
      const userId = data?.userId || data?.id;
      if (!userId) throw new Error('ID utilisateur manquant dans la réponse de création');
      dispatch(setUserId(userId));

      await Notification.requestPermission();
      const fcmToken = await Notification.getToken();
      console.log(fcmToken);
      console.log(userId);
      if (fcmToken) {
        const response = await saveToken({ userId, fcmToken });
        if(response?.error){
          console.log("error");
        }
        else{
          console.log("Token saved successfully");
        }
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
    navigation.replace('Login');
  };

  return (
  <AuthForm isLogin={false} submitForm={submitForm} navigate={navigate} />
  )
};

export default Register;