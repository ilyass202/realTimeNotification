import { configureStore } from '@reduxjs/toolkit';
import userReducer from './Slices/UserSlice';
import { tokenSlice } from './HttpSlices/tokenSlice';
import { authSlice } from './HttpSlices/authSlice';

const store = configureStore({
  reducer: {
    [tokenSlice.reducerPath]: tokenSlice.reducer,
    [authSlice.reducerPath]: authSlice.reducer,
    user: userReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(tokenSlice.middleware, authSlice.middleware),
});
export { store };