package com.storyous.delivery.common

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.contains
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.storyous.commonutils.viewBinding
import com.storyous.delivery.common.api.DeliverySettings
import com.storyous.delivery.common.databinding.FragmentDeliverySettingsBinding
import com.storyous.delivery.common.views.Expandable

class DeliverySettingsFragment : Fragment(R.layout.fragment_delivery_settings), Expandable {

    private val binding by viewBinding<FragmentDeliverySettingsBinding>()
    override var expanded: Boolean = false
    private lateinit var constraintCollapsed: ConstraintSet
    private lateinit var constraintExpanded: ConstraintSet
    private val dispatchAdapter: ArrayAdapter<String> by lazy {
        ArrayAdapter<String>(requireContext(), R.layout.spinner_item, listOf(
            getString(R.string.settings_yes), getString(R.string.settings_ask), getString(R.string.settings_no)
        ))
    }
    private val viewModel by viewModels<DeliverySettingsViewModel>({ requireActivity() })
    private var currentPrepTime = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.settings.observe(this, this::updateSettings)
        viewModel.progress.observe(this, this::setVisibility)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        constraintCollapsed = ConstraintSet().apply {
            clone(view as ConstraintLayout)
            setGuidelinePercent(R.id.bottom_guideline, 1F)
            setReferencedIds(
                R.id.flow,
                R.id.accept_orders_label, R.id.accept_orders_info, R.id.accept_orders_switch,
                R.id.prep_time_label, R.id.prep_time_info, R.id.prep_time_link,
                R.id.integrated_dispatch_label, R.id.integrated_dispatch_info, R.id.integrated_dispatch_spinner
            )
        }
        constraintExpanded = ConstraintSet().apply {
            clone(constraintCollapsed)
            setGuidelineBegin(
                R.id.bottom_guideline,
                requireContext().resources.getDimensionPixelSize(R.dimen.settings_height_expanded)
            )
        }

        binding.acceptOrdersSwitch.setOnClickListener {
            saveSettings()
        }

        binding.integratedDispatchSpinner.adapter = dispatchAdapter
        binding.integratedDispatchSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                IntegratedDispatch.apiKeyFromValue(parent?.selectedItem?.toString() ?: "", requireContext())
                    .takeIf { viewModel.settings.value?.integratedDispatch != it }?.run { saveSettings() }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // no action
            }
        }
        binding.prepTimeLink.setOnClickListener {
            TimePickerDialog(currentPrepTime) {
                updatePrepTime(it)
                saveSettings()
            }.show(childFragmentManager, TimePickerDialog.TAG)
        }

        collapse()
        viewModel.loadSettings()
    }

    override fun expand() {
        with(view as ConstraintLayout) {
            constraintExpanded.applyTo(this)
            removeView(binding.flow)
        }
        setVisibility(false)
    }

    override fun toggle(): Boolean {
        if (View.VISIBLE == binding.progress.visibility) {
            return expanded
        }
        return super.toggle()
    }

    override fun collapse() {
        with(view as ConstraintLayout) {
            if (binding.flow != null && !contains(binding.flow)) {
                addView(binding.flow)
            }
            constraintCollapsed.applyTo(this)
        }
        setVisibility(false)
    }

    private fun setVisibility(showProgress: Boolean) {
        binding.progress.isVisible = showProgress
        binding.groupLabels.isVisible = !showProgress
        binding.groupInfos.isVisible = !showProgress && !expanded
        binding.groupButtons.isVisible = !showProgress && expanded
    }

    private fun updateSettings(settings: DeliverySettings) {
        binding.acceptOrdersInfo.text = addSeparator(getString(
            if (settings.acceptNewOrders) R.string.settings_yes else R.string.settings_no
        ))
        binding.acceptOrdersSwitch.isChecked = settings.acceptNewOrders

        binding.integratedDispatchInfo.text = IntegratedDispatch.valueFromApiKey(
            settings.integratedDispatch, requireContext()
        )
        binding.integratedDispatchSpinner.setSelection(dispatchAdapter.getPosition(settings.integratedDispatch))

        updatePrepTime(settings.mealPrepTime)
    }

    private fun updatePrepTime(prepTime: Int) {
        currentPrepTime = prepTime
        binding.prepTimeInfo.text = addSeparator(getString(R.string.settings_minutes, prepTime))
        SpannableString(getString(R.string.settings_minutes, prepTime)).also {
            it.setSpan(URLSpan(""), 0, it.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.prepTimeLink.setText(it, TextView.BufferType.SPANNABLE)
        }
    }

    private fun saveSettings() {
        viewModel.saveSettings(DeliverySettings(
            binding.acceptOrdersSwitch.isChecked,
            currentPrepTime,
            IntegratedDispatch.apiKeyFromValue(
                binding.integratedDispatchSpinner.selectedItem.toString(), 
                requireContext()
            )
        ))
    }

    private fun addSeparator(text: String) = "$text, "
}
