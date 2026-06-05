import React, { useEffect, useCallback, useState } from 'react';
import {
  View,
  Text,
  Switch,
  StyleSheet,
  Alert,
  ScrollView,
  ActivityIndicator,
} from 'react-native';
import { useDispatch, useSelector } from 'react-redux';
import CustomButton from '../../Components/CustomButton';
import {
  setNotificationPreference,
  setNotificationSettings,
  selectNotificationSettings,
} from '../../Store/Slices/PreferencesSlice';
import { useGetAlertsQuery, useCreateAlertsMutation, usePatchAlertMutation, usePutAlertMutation } from '../../Store/HttpSlices/alertsSlice';

const notificationOptions = [
  { key: 'alerteTransaction', label: 'Alertes de transaction' },
  { key: 'alerteFraude', label: 'Alertes de fraude' },
  { key: 'alerteCarte', label: 'Alertes de carte' },

];

const DEFAULT_TELEPHONE = '0612345678';
const DEFAULT_PREFERENCES = {
  alerteTransaction: true,
  alerteFraude: true,
  alerteCarte: true,
  active: true,
  blackList: false,
  telephone: DEFAULT_TELEPHONE,
};

const mapApiToSettings = (apiData) => ({
  alerteTransaction: apiData?.alerteTransaction ?? true,
  alerteFraude: apiData?.alerteFraude ?? true,
  alerteCarte: apiData?.alerteCarte ?? true,
  active: apiData?.active ?? true,
  blackList: apiData?.blackList ?? false,
});

const mapSettingsToApi = (settings, clientId) => ({
  clientId,
  alerteTransaction: settings.alerteTransaction ?? true,
  alerteFraude: settings.alerteFraude ?? true,
  alerteCarte: settings.alerteCarte ?? true,
  active: settings.active ?? true,
  blackList: settings.blackList ?? false,
  telephone: DEFAULT_TELEPHONE,
});

const Preferences = ({ navigation }) => {
  const dispatch = useDispatch();
  const userId = useSelector((state) => state.user.userId);
  const settings = useSelector(selectNotificationSettings);
  const { data, error, isLoading, isFetching, isError } = useGetAlertsQuery(userId, {
    skip: !userId,
  });
  const [createAlerts] = useCreateAlertsMutation();
  const [patchAlert] = usePatchAlertMutation();
  const [putAlert] = usePutAlertMutation();
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (data) {
      dispatch(setNotificationSettings(mapApiToSettings(data)));
    }
  }, [data, dispatch]);

  useEffect(() => {
    if (isError && userId) {
      const status = error?.status || error?.originalStatus;
      if (status === 404) {
        createDefaultPreferences();
      }
    }
  }, [error, isError, userId]);

  const createDefaultPreferences = useCallback(async () => {
    try {
      const body = mapSettingsToApi(DEFAULT_PREFERENCES, userId);
      console.log('POST /api/alerts →', JSON.stringify(body));
      const created = await createAlerts(body).unwrap();
      dispatch(setNotificationSettings(mapApiToSettings(created)));
    } catch (createError) {
      console.log('Create default preferences failed', createError);
      Alert.alert('Erreur', 'Impossible d’initialiser les préférences.');
    }
  }, [createAlerts, dispatch, userId]);

  const togglePreference = async (key, value) => {
    const previousValue = settings[key];
    dispatch(setNotificationPreference({ key, value }));
    try {
      const body = { [key]: value };
      console.log('PATCH /api/alerts/' + userId + ' →', JSON.stringify(body));
      await patchAlert({ clientId: userId, body }).unwrap();
    } catch (updateError) {
      dispatch(setNotificationPreference({ key, value: previousValue }));
      Alert.alert('Erreur', 'Impossible de mettre à jour cette préférence.');
    }
  };

  const savePreferences = async () => {
    if (!userId) {
      Alert.alert('Erreur', 'Utilisateur non connecté.');
      return;
    }
    setIsSaving(true);
    try {
      const body = mapSettingsToApi(settings, userId);
      console.log('PUT /api/alerts/' + userId + ' →', JSON.stringify(body));
      await putAlert({ clientId: userId, body }).unwrap();
      Alert.alert('Succès', 'Préférences sauvegardées.');
    } catch (saveError) {
      Alert.alert('Erreur', 'Impossible de sauvegarder toutes les préférences.');
    } finally {
      setIsSaving(false);
    }
  };

  const loading = isLoading || isFetching;

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Préférences de notifications</Text>
      {loading ? (
        <View style={styles.loader}>
          <ActivityIndicator size="large" color="#FF8C00" />
          <Text style={styles.loadingText}>Chargement des préférences...</Text>
        </View>
      ) : (
        notificationOptions.map((option) => (
          <View key={option.key} style={styles.row}>
            <Text style={styles.label}>{option.label}</Text>
            <Switch
              value={settings[option.key] ?? false}
              onValueChange={(value) => togglePreference(option.key, value)}
              thumbColor="#FF8C00"
              trackColor={{ false: '#ddd', true: '#FFB84D' }}
            />
          </View>
        ))
      )}

      <View style={styles.buttonContainer}>
        <CustomButton text={isSaving ? 'Enregistrement...' : 'Sauvegarder'} onPress={savePreferences} disabled={isSaving || loading} />
      </View>
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
    width: '100%',
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
    marginBottom: 20,
    marginTop: 20,
    paddingHorizontal: 10,
    textAlign: 'center',
  },
  buttonContainer: {
    width: '100%',
    marginBottom: 10,
  },
  loader: {
    marginTop: 40,
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 12,
    color: '#666',
    fontSize: 16,
  },
});

export default Preferences;
