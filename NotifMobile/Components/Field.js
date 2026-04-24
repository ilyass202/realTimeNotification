import { View, Text, Pressable, StyleSheet, TextInput } from 'react-native';
import React, { useState } from 'react';
import Feather from 'react-native-vector-icons/Feather'; // Ajouté

const Field = ({ label, error, errorText, type, ...props }) => {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false); // Renommé pour clarté
  const togglePassword = () => {
    setIsPasswordVisible(!isPasswordVisible);
  };

  const inputContainerStyles = [styles.inputContainer];
  if (error) {
    inputContainerStyles.push(styles.errorInput);
  }

  return (
    <View style={styles.container}>
      <View style={styles.labelContainer}>
        <Text style={styles.label}>{label}</Text>
      </View>
      <View style={inputContainerStyles}> 
        <TextInput
          style={styles.input}
          {...props}
          secureTextEntry={type === 'password' && !isPasswordVisible}
        />
        {type === 'password' && (
          <Pressable onPress={togglePassword} style={styles.iconContainer}>
            <Feather
              name={isPasswordVisible ? 'eye' : 'eye-off'}
              size={20}
              color="grey"
            />
          </Pressable>
        )}
      </View>
      <View style={styles.errorContainer}>
        {error && errorText ? <Text style={styles.error}>{errorText}</Text> : null}
      </View>
    </View>
  );
};

export default Field;

const styles = StyleSheet.create({
  container: {
    marginBottom: 24,
  },
  labelContainer: {
    marginBottom: 8, 
  },
  label: {
    fontSize: 16,
    color: '#333',
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 40,
    width: '100%',
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#ccc', 
    backgroundColor: '#f9f9f9',
    paddingHorizontal: 10, 
  },
  errorInput: {
    borderColor: 'red',
  },
  input: {
    flex: 1,
    fontSize: 16,
    color: '#333',
  },
  iconContainer: {
    marginLeft: 10,
  },
  errorContainer: {
    minHeight: 24,
    justifyContent: 'center',
  },
  error: {
    color: 'red',
    fontSize: 14,
  },
});