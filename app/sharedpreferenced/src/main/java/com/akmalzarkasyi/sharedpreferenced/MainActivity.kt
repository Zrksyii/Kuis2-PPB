package com.akmalzarkasyi.sharedpreferenced

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import com.akmalzarkasyi.sharedpreferenced.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var settingModel: SettingModel
    private lateinit var mSettingPreference: SettingPreference
    private val resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.data != null && result.resultCode == SettingPreferenceActivity.RESULT_CODE) {
                settingModel =
                    result.data?.parcelable<SettingModel>(SettingPreferenceActivity.EXTRA_RESULT) as SettingModel
                populateView(settingModel)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.main_title)
        mSettingPreference = SettingPreference(this)

        binding.btnSetting.setOnClickListener {
            val intent = Intent(this, SettingPreferenceActivity::class.java)
            intent.putExtra("SETTING", settingModel)
            resultLauncher.launch(intent)
        }

        showExistingPreferences()
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    private fun showExistingPreferences() {
        settingModel = mSettingPreference.getSetting()
        populateView(settingModel)
    }

    private fun populateView(settingModel: SettingModel) {
        with(binding) {
            tvName.text = settingModel.name.toString().ifEmpty { getString(R.string.empty_message) }
            tvAddress.text =
                settingModel.address.toString().ifEmpty { getString(R.string.empty_message) }
            tvGender.text =
                settingModel.gender.toString().ifEmpty { getString(R.string.empty_message) }
            tvEmail.text =
                settingModel.email.toString().ifEmpty { getString(R.string.empty_message) }
            tvPhone.text =
                settingModel.phoneNumber.toString().ifEmpty { getString(R.string.empty_message) }
            tvAge.text = settingModel.age.toString().ifEmpty { getString(R.string.empty_message) }
            tvTheme.text =
                if (settingModel.isDarkTheme) getString(R.string.dark) else getString(R.string.light)
        }
        if (settingModel.isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            delegate.applyDayNight()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            delegate.applyDayNight()
        }
    }
}