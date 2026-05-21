import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  notificationSettings: {
    transaction: true,
    payment: true,
    promotion: false,
    security: false,
    reminder: false,
    creationCarte: false,
  },
};

export const preferencesSlice = createSlice({
  name: 'preferences',
  initialState,
  reducers: {
    setNotificationPreference: (state, action) => {
      const { key, value } = action.payload;
      state.notificationSettings[key] = value;
    },
    setNotificationSettings: (state, action) => {
      state.notificationSettings = {
        ...state.notificationSettings,
        ...action.payload,
      };
    },
  },
});

export const { setNotificationPreference, setNotificationSettings } = preferencesSlice.actions;
export const selectNotificationSettings = (state) => state.preferences.notificationSettings;

export default preferencesSlice.reducer;
