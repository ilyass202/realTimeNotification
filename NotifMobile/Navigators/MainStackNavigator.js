import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import Login from '../Screens/Login';
import Register from '../Screens/Register';
import Home from '../Screens/Home';
import Preferences from '../Screens/Preferences';

const Main = createNativeStackNavigator();
const MainStackNavigator = () => {
  return (
    <Main.Navigator
      screenOptions={({ route }) => ({
        headerShown: route.name !== 'Login' && route.name !== 'Register',
        headerStyle: {
          backgroundColor: '#FF8C00',
        },
        headerTintColor: '#fff',
        headerTitleStyle: {
          fontWeight: 'bold',
          fontSize: 18,
          color: '#fff',
        },
      })}
    >
      <Main.Screen name='Login' component={Login} />
      <Main.Screen name='Register' component={Register} />
      <Main.Screen 
        name='Home' 
        component={Home}
        options={{ title: 'Accueil' }}
      />
      <Main.Screen 
        name='Preferences' 
        component={Preferences}
        options={{ title: 'Préférences' }}
      />
    </Main.Navigator>
  );
};

export default MainStackNavigator;