
import { View, Text } from 'react-native'
import React, { Activity } from 'react'
import { TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native'

const CustomButton = ({text, onPress, isLoading}) => {
  return (
    <TouchableOpacity style={styles.btn} onPress={onPress}>
        {isLoading ? (
            <ActivityIndicator size="small" color="#fff"/>
        ): (
          <Text style={styles.btnText}>{text}</Text>
        )
        }
    </TouchableOpacity>
  )
}

export default CustomButton
const styles = StyleSheet.create({
    btn: {
        backgroundColor: "#007bff",
        height: 50, 
        width: '100%',
        justifyContent: 'center',
        borderRadius: 99
    },
    btnText: {
        color: '#fff',
        fontSize: 20,
        fontWeight: 'bold',
        textAlign: 'center'
    }
})