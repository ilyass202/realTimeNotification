import { View, Text, Pressable, StyleSheet, TextInput } from 'react-native';
import React, { useState } from 'react';
import Feather from 'react-native-vector-icons/Feather';

const Field = ({ label, error, errorText, type, ...props }) => {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false); 
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
    color: '#E85D04',
    fontWeight: '600',
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 45,
    width: '100%',
    borderRadius: 12,
    borderWidth: 2,
    borderColor: '#FFB84D', 
    backgroundColor: '#FFFBF5',
    paddingHorizontal: 12,
  },
  errorInput: {
    borderColor: '#E74C3C',
    backgroundColor: '#FADBD8',
  },
  input: {
    flex: 1,
    fontSize: 16,
    color: '#333',
    fontWeight: '500',
  },
  iconContainer: {
    marginLeft: 10,
    padding: 5,
  },
  errorContainer: {
    minHeight: 24,
    justifyContent: 'center',
  },
  error: {
    color: '#E74C3C',
    fontSize: 14,
    fontWeight: '500',
  },
});