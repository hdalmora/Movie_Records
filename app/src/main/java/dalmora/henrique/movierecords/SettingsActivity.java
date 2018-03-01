package dalmora.henrique.movierecords;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by hdalmora on 25/10/2016.
 */

public class SettingsActivity extends PreferenceActivity {

    public static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    public static final String MOVIE_RECORDS_VERSION = "0.1.0";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file, considering the API version
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            onCreatePreferenceActivity();
        } else {
            onCreatePreferenceFragment();
        }

    }

    /**
     * Wraps legacy {@link #onCreate(Bundle)} code for Android < 3 (i.e. API lvl
     * < 11).
     */
    @SuppressWarnings("deprecation")
    private void onCreatePreferenceActivity() {
        addPreferencesFromResource(R.xml.pref_general);
    }

    /**
     * Wraps {@link #onCreate(Bundle)} code for Android >= 3 (i.e. API lvl >=
     * 11).
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void onCreatePreferenceFragment() {
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment ())
                .commit();
    }



    /**
     * Preference Fragment para implementar Settings em API > 11 (HONEYCOMB)
     */
    public static class MyPreferenceFragment extends PreferenceFragment implements  Preference.OnPreferenceChangeListener
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            Preference openSourceLicensesPreference = findPreference("open_sourece_licenses");
            openSourceLicensesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //TODO: Abrir dialog com as licensas Open Source Utilizadas
                    Toast.makeText(getActivity(), "Open Source Licenses", Toast.LENGTH_SHORT).show();
                    DialogOpenSourceLicenses dialog = new DialogOpenSourceLicenses();
                    dialog.show(getFragmentManager(), "OpenSources Dialog");
                    return true;
                }
            });

            Preference movieRecordsVersion = findPreference("movie_records_version");
            movieRecordsVersion.setSummary(MOVIE_RECORDS_VERSION);
            movieRecordsVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //TODO: Abrir dialog com informações da versão e Change Logs
                    Toast.makeText(getActivity(), "Movie Records Version: " + preference.getSummary(), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
            // updated when the preference changes.
            // Add Preferences
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_theme_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_bg_image_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_language_key)));
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list (since they have separate labels/values).
                final ListPreference listPreference = (ListPreference) preference;
                final int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);

                    if (preference.getKey().equals(getString(R.string.pref_theme_key))){
                        Log.d(LOG_TAG, "Preference TEMA " + preference.toString());
                        sendThemeChangedBroadcastMessage(true);
                    } else if (preference.getKey().equals(getString(R.string.pref_bg_image_key))){
                        Log.d(LOG_TAG, "Preference BG IMAGE: " + preference.toString());
                        sendImageBackGroundChangedBroadcastMessage(true);
                    } else if (preference.getKey().equals(getString(R.string.pref_language_key))){
                        Log.d(LOG_TAG, "Preference Language: " + preference.toString());
                        //TODO Change Language
                        //TODO Send language Change Broadcast Message
                    }

                }
            } else {
                // For other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }

        private void sendThemeChangedBroadcastMessage(boolean themeChanged) {
            Log.d("sender", "Broadcasting message THEME");
            Intent intent = new Intent("ThemeChanged");

            if (themeChanged){
                intent.putExtra("message", "themeChanged");
            } else {
                intent.putExtra("message", "themeNotChanged");
            }
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }

        private void sendImageBackGroundChangedBroadcastMessage(boolean themeChanged) {
            Log.d("sender", "Broadcasting message BG IMAGE");
            Intent intent = new Intent("BgImageChange");

            if (themeChanged){
                intent.putExtra("message", "bgImageChanged");
            } else {
                intent.putExtra("message", "bgImageNotChanged");
            }
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }

        /**
         * Attaches a listener so the summary is always updated with the preference value.
         * Also fires the listener once, to initialize the summary (so it shows up before the value
         * is changed.)
         */
        private void bindPreferenceSummaryToValue(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(this);

            // Trigger the listener immediately with the preference's
            // current value.
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

}
