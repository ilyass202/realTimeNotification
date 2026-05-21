
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
        backgroundColor: "#FF8C00",
        height: 50, 
        width: '100%',
        justifyContent: 'center',
        borderRadius: 99,
        elevation: 4,
        shadowColor: '#E85D04',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.25,
        shadowRadius: 3.84,
    },
    btnText: {
        color: '#fff',
        fontSize: 20,
        fontWeight: 'bold',
        textAlign: 'center'
    }
})