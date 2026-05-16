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
      screenOptions={{
        headerShown: false,
      }}
    >
      <Main.Screen name='Login' component={Login} />
      <Main.Screen name='Register' component={Register} />
      <Main.Screen name='Home' component={Home} />
      <Main.Screen name='Preferences' component={Preferences} />
    </Main.Navigator>
  );
};

export default MainStackNavigator;