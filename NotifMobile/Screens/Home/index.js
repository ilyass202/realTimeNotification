import React from 'react';
import { View, Text, Button, StyleSheet } from 'react-native';
import { useSelector } from 'react-redux';
import { selectNotificationSettings } from '../../Store/Slices/PreferencesSlice';

const Home = ({ navigation }) => {
  const settings = useSelector(selectNotificationSettings);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Accueil</Text>
      <Text style={styles.status}>
        Transactions : {settings.transaction ? 'Activées' : 'Désactivées'}
      </Text>
      <Text style={styles.status}>
        Paiements : {settings.payment ? 'Activées' : 'Désactivées'}
      </Text>
      <Text style={styles.status}>
        Promotions : {settings.promotion ? 'Activées' : 'Désactivées'}
      </Text>
      <Button title='Gérer les préférences' onPress={() => navigation.navigate('Preferences')} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#fff',
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    marginBottom: 20,
  },
  status: {
    fontSize: 16,
    marginBottom: 24,
  },
});

export default Home;
