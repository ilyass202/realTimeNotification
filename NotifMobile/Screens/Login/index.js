import React from 'react';
import { Alert } from 'react-native';
import { useDispatch } from 'react-redux';
import { setUserId } from '../../Store/Slices/UserSlice';
import AuthForm from '../../Components/AuthForm';
import { useLoginMutation } from '../../Store/HttpSlices/authSlice';
import AsyncStorage from '@react-native-async-storage/async-storage';

const Login = ({ navigation }) => {
  const dispatch = useDispatch();
  const [login] = useLoginMutation();
  const submitForm = async (values) => {
    try {
      const credentials = {
        email: values.email,
        password: values.password,
      };
      const data = await login(credentials).unwrap();
      await AsyncStorage.setItem('credentials', JSON.stringify(credentials));
      const userId = data?.userId || data?.id;
      if (!userId) throw new Error('ID utilisateur manquant dans la réponse de connexion');
      dispatch(setUserId(userId));
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
