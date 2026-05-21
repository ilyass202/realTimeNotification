import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import React from 'react';
import * as Yup from 'yup';
import { Formik } from 'formik';
import CustomButton from './CustomButton';
import Field from './Field';

const AuthForm = ({ isLogin, submitForm, navigate }) => {

    const initialValues = isLogin ? {
        email: "",
        password: ""
    } : {
        name: "",
        email: "",
        password: ""
    }
    const nameValidation = !isLogin ? {name: Yup.string().required("nom doit être rempli")} : {};
    const validationSchema = Yup.object().shape({
        ...nameValidation, 
        email: Yup.string().email("email doit être valide").required("email doit être rempli"),
        password: Yup.string().min(6, "le mot de passe doit contenir au moins 6 caractéres").required("le mot de passe doit être rempli")
    })
  return (
    <View style={styles.container}>
      <Formik initialValues={initialValues} validationSchema={validationSchema} onSubmit={submitForm}>
        {({handleChange, handleSubmit, handleBlur ,values, errors, touched})=> (
            <>
            {
                !isLogin ? (
                    <Field label="nom" error={errors.name && touched.name} errorText={errors.name} value={values.name} onChangeText={handleChange("name")} onBlur={handleBlur("name")} />
                ) :
                null
            }
            <Field label="email" error={errors.email && touched.email} errorText={errors.email} value={values.email} onChangeText={handleChange("email")} onBlur={handleBlur("email")} />
            <Field label="mot de passe" error={errors.password && touched.password} errorText={errors.password} value={values.password} type ="password" onChangeText={handleChange("password")} onBlur={handleBlur("password")}/>
            <CustomButton text={isLogin ? "se connecter" : "s'inscrire"} onPress={handleSubmit}/>
          </>
        )    
        }
      </Formik>
      <TouchableOpacity onPress={navigate}>
        <Text style={styles.linkText}>
          {isLogin ? "pas de compte ? s'inscrire" : "déjà un compte ? se connecter"}
        </Text>
      </TouchableOpacity>
    </View>
  )
}

export default AuthForm
const styles = StyleSheet.create({
    container: {
        flex: 1, 
        justifyContent: 'center',
        backgroundColor: '#FFF8F0',
        margin: 20,
        borderRadius: 15,
        padding: 20,
        height: 400,
        elevation: 5,
        borderWidth: 2,
        borderColor: '#FFB84D',
    },
    linkText: {
        color: '#FF8C00',
        marginTop: 15,
        textAlign: 'center',
        justifyContent: 'center',
        fontSize: 16,
        fontWeight: '600',
    }
})