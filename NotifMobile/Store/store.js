import { configureStore } from '@reduxjs/toolkit';
import userReducer from './Slices/UserSlice';
import preferencesReducer from './Slices/PreferencesSlice';
import { tokenSlice } from './HttpSlices/tokenSlice';
import { authSlice } from './HttpSlices/authSlice';
import { alertsSlice } from './HttpSlices/alertsSlice';

const store = configureStore({
  reducer: {
    [tokenSlice.reducerPath]: tokenSlice.reducer,
    [authSlice.reducerPath]: authSlice.reducer,
    [alertsSlice.reducerPath]: alertsSlice.reducer,
    user: userReducer,
    preferences: preferencesReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(
      tokenSlice.middleware,
      authSlice.middleware,
      alertsSlice.middleware,
    ),
});
export { store };