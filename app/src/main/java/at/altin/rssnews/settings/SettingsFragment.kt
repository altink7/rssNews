package at.altin.rssnews.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import at.altin.rssnews.R

class SettingsFragment : PreferenceFragmentCompat() {
  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    addPreferencesFromResource(R.xml.pref_general)
  }
}
