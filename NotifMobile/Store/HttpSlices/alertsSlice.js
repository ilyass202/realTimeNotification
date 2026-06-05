import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import { BASE_URL } from '../../const/const';
import AsyncStorage from '@react-native-async-storage/async-storage';

export const alertsSlice = createApi({
  reducerPath: 'alertsApi',
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
  endpoints: (builder) => ({
    getAlerts: builder.query({
      query: (clientId) => `/api/alerts/${clientId}`,
    }),
    createAlerts: builder.mutation({
      query: (body) => ({
        url: '/api/alerts',
        method: 'POST',
        body,
      }),
    }),
    patchAlert: builder.mutation({
      query: ({ clientId, body }) => ({
        url: `/api/alerts/${clientId}`,
        method: 'PATCH',
        body,
      }),
    }),
    putAlert: builder.mutation({
      query: ({ clientId, body }) => ({
        url: `/api/alerts/${clientId}`,
        method: 'PUT',
        body,
      }),
    }),
  }),
});

export const {
  useGetAlertsQuery,
  useCreateAlertsMutation,
  usePatchAlertMutation,
  usePutAlertMutation,
} = alertsSlice;
