import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import { BASE_URL } from '../../const/const';

const authSlice = createApi({
  reducerPath: 'authSlice',
  baseQuery: fetchBaseQuery({ 
    baseUrl: BASE_URL,
    prepareHeaders: (headers) => {
      headers.set('Content-Type', 'application/json');
      return headers;
    },
  }),
  endpoints: (builder) => ({
    login: builder.mutation({
      query: ({ email, password }) => ({
        url: '/api/auth/login',
        method: 'POST',
        body: { email, password },
      }),
    }),
    register: builder.mutation({
      query: ({ name, email, password }) => ({
        url: '/api/auth/register',
        method: 'POST',
        body: { name, email, password },
      }),
    }),
  }),
});

export const { useLoginMutation, useRegisterMutation } = authSlice;
export { authSlice };
export default authSlice.reducer;