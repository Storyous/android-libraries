package com.storyous.delivery.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.storyous.commonutils.viewBinding
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

    private val binding by viewBinding(this) {
        TimePickerDialogBinding.inflate(LayoutInflater.from(requireContext()), null, false)
    }

    private val pickerListener = object : CircularSeekBar.OnCircularSeekBarChangeListener {
        override fun onProgressChanged(
            circularSeekBar: CircularSeekBar?,
            progress: Float,
            fromUser: Boolean
        ) {
            binding.value.text = getString(
                R.string.settings_minutes,
                progress.toInt() * MIN_MINUTE_RANGE
            )
        }

        override fun onStopTrackingTouch(seekBar: CircularSeekBar?) = Unit

        override fun onStartTrackingTouch(seekBar: CircularSeekBar?) = Unit
    }

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        binding.picker.apply {
            setOnSeekBarChangeListener(pickerListener)
            progress = initValue.toFloat() / MIN_MINUTE_RANGE
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_choose_preparation_time)
            .setView(binding.root)
            .setPositiveButton(R.string.confirm) { _, _ ->
                onConfirm(binding.picker.progress.toInt() * MIN_MINUTE_RANGE)
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
    }
}
