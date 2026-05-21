import React, { useEffect } from 'react';
import { View, Text, Switch, StyleSheet, Alert, ScrollView } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useDispatch, useSelector } from 'react-redux';
import CustomButton from '../../Components/CustomButton';
import {
  setNotificationPreference,
  setNotificationSettings,
  selectNotificationSettings,
} from '../../Store/Slices/PreferencesSlice';

const STORAGE_KEY = '@preferences';
const notificationOptions = [
  { key: 'transaction', label: 'Notifications de transactions' },
  { key: 'payment', label: 'Notifications de paiements' },
  { key: 'security', label: 'Alertes de sécurité' },
  { key: 'creationCarte', label: 'Notifications de création de carte' },
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
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Préférences de notifications</Text>
      {notificationOptions.map((option) => (
        <View key={option.key} style={styles.row}>
          <Text style={styles.label}>{option.label}</Text>
          <Switch
            value={settings[option.key] ?? false}
            onValueChange={(value) => togglePreference(option.key, value)}
            thumbColor="#FF8C00"
            trackColor={{ false: '#ddd', true: '#FFB84D' }}
          />
        </View>
      ))}
      <Text style={styles.helpText}>
        Activez ou désactivez les notifications par type d'événement.
      </Text>
      <View style={styles.buttonContainer}>
        <CustomButton text="Retour" onPress={() => navigation.goBack()} />
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#FFF8F0',
    width: "100%"
  },
  title: {
    fontSize: 26,
    fontWeight: 'bold',
    marginBottom: 28,
    textAlign: 'center',
    color: '#E85D04',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 18,
    paddingHorizontal: 12,
    paddingVertical: 14,
    backgroundColor: '#FFFBF5',
    borderRadius: 12,
    borderLeftWidth: 4,
    borderLeftColor: '#FF8C00',
  },
  label: {
    fontSize: 17,
    flex: 1,
    color: '#333',
    fontWeight: '500',
  },
  helpText: {
    fontSize: 15,
    color: '#666',
    marginBottom: 28,
    marginTop: 20,
    paddingHorizontal: 10,
    textAlign: 'center',
  },
  buttonContainer: {
    width: '100%',
    marginBottom: 10,
  },
});

export default Preferences;
