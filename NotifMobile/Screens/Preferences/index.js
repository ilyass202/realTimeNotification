import React, { useEffect } from 'react';
import { View, Text, Switch, Button, StyleSheet, Alert } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useDispatch, useSelector } from 'react-redux';
import {
  setNotificationPreference,
  setNotificationSettings,
  selectNotificationSettings,
} from '../../Store/Slices/PreferencesSlice';

const STORAGE_KEY = '@preferences';
const notificationOptions = [
  { key: 'transaction', label: 'Notifications de transactions' },
  { key: 'payment', label: 'Notifications de paiements' },
  { key: 'promotion', label: 'Promotions et offres' },
];

const Preferences = ({ navigation }) => {
  const dispatch = useDispatch();
  const settings = useSelector(selectNotificationSettings);

  useEffect(() => {
    const loadPreferences = async () => {
      try {
        const json = await AsyncStorage.getItem(STORAGE_KEY);
        if (json) {
          const prefs = JSON.parse(json);
          if (prefs.notificationSettings) {
            dispatch(setNotificationSettings(prefs.notificationSettings));
          }
        }
      } catch (error) {
        Alert.alert('Erreur', 'Impossible de charger les préférences');
      }
    };

    loadPreferences();
  }, [dispatch]);

  const togglePreference = async (key, value) => {
    try {
      dispatch(setNotificationPreference({ key, value }));
      await AsyncStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({ notificationSettings: { ...settings, [key]: value } }),
      );
    } catch (error) {
      Alert.alert('Erreur', 'Impossible de sauvegarder la préférence');
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Préférences de notifications</Text>
      {notificationOptions.map((option) => (
        <View key={option.key} style={styles.row}>
          <Text style={styles.label}>{option.label}</Text>
          <Switch
            value={settings[option.key]}
            onValueChange={(value) => togglePreference(option.key, value)}
          />
        </View>
      ))}
      <Text style={styles.helpText}>
        Activez ou désactivez les notifications par type d’événement comme une application classique.
      </Text>
      <Button title="Retour" onPress={() => navigation.goBack()} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    justifyContent: 'center',
    backgroundColor: '#fff',
  },
  title: {
    fontSize: 22,
    fontWeight: '700',
    marginBottom: 24,
    textAlign: 'center',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  label: {
    fontSize: 16,
    flex: 1,
  },
  helpText: {
    fontSize: 14,
    color: '#555',
    marginBottom: 24,
  },
});

export default Preferences;
