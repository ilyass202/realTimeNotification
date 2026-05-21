import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useSelector } from 'react-redux';
import { selectNotificationSettings } from '../../Store/Slices/PreferencesSlice';
import CustomButton from '../../Components/CustomButton';

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
      <View style={styles.buttonContainer}>
        <CustomButton title='Gérer les préférences' onPress={() => navigation.navigate('Preferences')} text='Gérer les préférences' />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#FFF8F0',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 30,
    color: '#E85D04',
    textAlign: 'center',
  },
  status: {
    fontSize: 18,
    marginBottom: 24,
    color: '#333',
    backgroundColor: '#FFE5CC',
    paddingVertical: 10,
    paddingHorizontal: 15,
    borderRadius: 10,
    borderLeftWidth: 4,
    borderLeftColor: '#FF8C00',
    width: '100%',
    textAlign: 'center',
  },
  buttonContainer: {
    width: '100%',
    marginTop: 20,
  },
});

export default Home;
