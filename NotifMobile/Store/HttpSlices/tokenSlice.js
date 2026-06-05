//@ts-nocheck
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import { BASE_URL } from '../../const/const';
import AsyncStorage from '@react-native-async-storage/async-storage';

export const tokenSlice = createApi({
    reducerPath: 'tokenApi',
    baseQuery: fetchBaseQuery({
        baseUrl: BASE_URL,
        prepareHeaders: async (headers) => {
            headers.set('Content-Type', 'application/json');
            const data = await AsyncStorage.getItem('credentials');
            if (data) {
                const { email, password } = JSON.parse(data);
                const token = btoa(`${email}:${password}`);
                headers.set('Authorization', `Basic ${token}`);
            }
            return headers;
        },
    }),
    endpoints: (builder)=> ({
        getToken: builder.query({
            query: (userId)=> `/api/getToken/${userId}`
        }),
        saveToken: builder.mutation({
            query: ({userId, fcmToken})=> ({
                url: `/api/saveToken`,
                method: 'POST',
                body: {userId, fcmToken}
            })
        })
    })
})
export const { useGetTokenQuery, useSaveTokenMutation } = tokenSlice;
