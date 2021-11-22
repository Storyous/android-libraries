package com.storyous.delivery.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.storyous.delivery.common.databinding.TimePickerDialogBinding
import me.tankery.lib.circularseekbar.CircularSeekBar

class TimePickerDialog(
    private val initValue: Int,
    private val onConfirm: (Int) -> Unit
) : DialogFragment() {

    companion object {
        const val TAG = "prepTimeDialog"
        private const val MIN_MINUTE_RANGE = 5
    }

    private lateinit var binding: TimePickerDialogBinding
    private val selectedValue = MutableLiveData<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedValue.observe(
            this,
            { binding.value.text = getString(R.string.settings_minutes, it) })
    }

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        binding = TimePickerDialogBinding.inflate(LayoutInflater.from(requireContext()))
        binding.picker.setOnSeekBarChangeListener(
            object : CircularSeekBar.OnCircularSeekBarChangeListener {
                override fun onProgressChanged(
                    circularSeekBar: CircularSeekBar?,
                    progress: Float,
                    fromUser: Boolean
                ) {
                    selectedValue.value = progress.toInt() * MIN_MINUTE_RANGE
                }

                override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
                    // not implemented
                }

                override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {
                    // not implemented
                }
            }
        )
        binding.picker.progress = initValue.toFloat() / MIN_MINUTE_RANGE

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_choose_preparation_time)
            .setView(binding.value)
            .setPositiveButton(R.string.confirm) { _, _ ->
                onConfirm(selectedValue.value ?: initValue)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->

            }
            .create()
    }
}
