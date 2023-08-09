package org.commcare.dalvik.abha.ui.main.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.CreatePatientConsentBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.utility.CommonUtil
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.abha.viewmodel.PatientViewModel
import org.commcare.dalvik.abha.viewmodel.RequestType
import org.commcare.dalvik.domain.model.ConsentValidation
import org.commcare.dalvik.domain.model.DATE_FORMAT
import timber.log.Timber
import java.util.Calendar
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit


class CreatePatientConsentFragment :
    BaseFragment<CreatePatientConsentBinding>(CreatePatientConsentBinding::inflate) {

    lateinit var timechip: Chip
    val viewmodel: PatientViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewmodel.init("test_patient_id@sbx", "test_hiu_id@sbx")
        binding.model = viewmodel.patientConsentModel
        binding.clickHandler = this
        initConsentPurpose()
        binding.addHiType.setOnClickListener {
            openHiTypeDialog()
        }

        observeUiState()
    }

    private fun initConsentPurpose() {
        val items = mutableListOf<String>()

        enumValues<PURPOSE>().forEach {
            items.add(it.displayValue)
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.abha_dropdown_row_item, items)

        (binding.consentPurposeCode as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            setText(adapter.getItem(0).toString(), false);
        }

    }

    private fun renderSelectedHiTypes() {
        binding.chipGroup.removeAllViews()
        viewmodel.patientConsentModel.hiTypes.forEach { selectedHiType ->
            enumValues<HITYPES>().forEach {
                if (selectedHiType == it.name) {
                    val chip = Chip(requireContext(), null, R.style.HiTypeStyle)
                    chip.text = it.displayValue
                    chip.setTextColor(Color.BLACK)
                    binding.chipGroup.addView(chip)
                }
            }
        }
    }

    private fun openHiTypeDialog() {
        val items = mutableListOf<String>()
        val checkedStatus = mutableListOf<Boolean>()
        enumValues<HITYPES>().forEach {
            items.add(it.displayValue)
            checkedStatus.add(viewmodel.patientConsentModel.hiTypes.contains(it.name))
        }

        val hiTypesItems = items.toTypedArray()


        val checkedItems = checkedStatus.toBooleanArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.hiTypes))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                renderSelectedHiTypes()
            }
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                renderSelectedHiTypes()
            }
            .setMultiChoiceItems(hiTypesItems, checkedItems) { dialog, which, checked ->
                val selectedType = hiTypesItems[which]
                enumValues<HITYPES>().forEach {
                    if (selectedType.equals(it.displayValue, false)) {
                        if (checked) {
                            viewmodel.patientConsentModel.hiTypes.add(it.name)
                        } else {
                            viewmodel.patientConsentModel.hiTypes.remove(it.name)
                        }

                    }
                }

                renderSelectedHiTypes()
            }
            .show()
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        view?.id?.let {

            when (it) {
                R.id.createConsentBtn -> {

                    val consentValidation = viewmodel.patientConsentModel.validateConsent()

                    if (consentValidation == ConsentValidation.SUCCESS) {
                        submitPatientConsent()
                    } else {
                        Toast.makeText(requireContext(), consentValidation.msg, Toast.LENGTH_SHORT)
                            .show()
                    }

                }

                R.id.startDate -> {
                    timechip = binding.startDateChip
                    val dateValidator =
                        DateValidatorPointBackward.before(
                            Calendar.getInstance().timeInMillis - 1.days.toLong(DurationUnit.MILLISECONDS)
                        )

                    captureDateAndTime(
                        resources.getString(R.string.startDate),
                        MaterialDatePicker.todayInUtcMilliseconds() - 1.days.toLong(DurationUnit.MILLISECONDS),
                        dateValidator
                    )
                }

                R.id.endDate -> {
                    timechip = binding.endDateChip
                    val dateValidatorStart =
                        DateValidatorPointForward.from(
                            viewmodel.patientConsentModel.getPermissionStartDateInMs()
                        )

                    val dateValidatorEnd =
                        DateValidatorPointBackward.before(
                            Calendar.getInstance().timeInMillis - 1.days.toLong(DurationUnit.MILLISECONDS)
                        )

                    val validatorList = mutableListOf<CalendarConstraints.DateValidator>()
                    validatorList.add(dateValidatorStart)
                    validatorList.add(dateValidatorEnd)

                    val compositeDateValidator = CompositeDateValidator.allOf(validatorList)
                    captureDateAndTime(
                        resources.getString(R.string.endDate),
                        MaterialDatePicker.todayInUtcMilliseconds(),
                        compositeDateValidator
                    )
                }

                R.id.expiryDate -> {
                    timechip = binding.eraseDateChip
                    val dateValidator =
                        DateValidatorPointForward.from(
                            Calendar.getInstance().timeInMillis
                        )
                    captureDateAndTime(
                        resources.getString(R.string.expiryDate),
                        MaterialDatePicker.todayInUtcMilliseconds(),
                        dateValidator
                    )
                }
            }
        }
    }

    private fun submitPatientConsent() {
        viewmodel.submitPatientConsent()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.uiState.collect {
                    when (it) {
                        is GenerateAbhaUiState.Success -> {
                            when (it.requestType) {
                                RequestType.PATIENT_CONSENT -> {


                                }

                                else -> {
                                    //exhaustive block
                                }
                            }

                            viewmodel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        is GenerateAbhaUiState.Error -> {
                            (activity as AbdmActivity).showBlockerDialog(it.data.get("message").asString)
                            viewmodel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        is GenerateAbhaUiState.AbdmError -> {
                            (activity as AbdmActivity).showBlockerDialog(it.data.getActualMessage())
                            viewmodel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        else -> {
                            //exhaustive block
                        }

                    }
                }
            }

        }
    }

    private fun captureDateAndTime(
        title: String,
        selectedTime: Long,
        dateValidator: CalendarConstraints.DateValidator? = null
    ) {
        captureDate(title, selectedTime, ::onDateSelected, dateValidator)
    }

    private fun onDateSelected(selectedDate: Long?) {
        selectedDate?.let {
            captureTime(it)
        } ?: run {
            Toast.makeText(requireContext(), "Date not selected.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun captureTime(selectedDate: Long) {

        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(10)
                .setTitleText(resources.getString(R.string.select_time))
                .build()

        timePicker.show(parentFragmentManager, timePicker.tag)

        timePicker.addOnPositiveButtonClickListener {
            val minutes = timePicker.minute * 60 * 1000
            val hours = timePicker.hour * 60 * 60 * 1000

            val finalTime = selectedDate + hours + minutes

            timechip.text = CommonUtil.getFormattedDateTime(finalTime, DATE_FORMAT.USER.format)

            when (timechip.id) {
                R.id.startDateChip -> {
                    viewmodel.patientConsentModel.setPermissionStartDate(timechip.text.toString())
                }

                R.id.endDateChip -> {
                    viewmodel.patientConsentModel.setPermissionEndDate(timechip.text.toString())
                }

                R.id.eraseDateChip -> {
                    viewmodel.patientConsentModel.setPermissionExpiryDate(timechip.text.toString())
                }
            }
            // call back code
        }
        timePicker.addOnNegativeButtonClickListener {
            Timber.d("")
            // call back code
        }
        timePicker.addOnCancelListener {
            Timber.d("")
            // call back code
        }
        timePicker.addOnDismissListener {
            Timber.d("")
            // call back code
        }

    }

    private fun captureDate(
        title: String,
        selectedDate: Long,
        callback: (selectedDate: Long?) -> Unit,
        dateValidator: CalendarConstraints.DateValidator?
    ) {

        val builder =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setSelection(selectedDate)

        val datePicker = dateValidator?.let {
            val constraints: CalendarConstraints =
                CalendarConstraints.Builder()
                    .setValidator(it)
                    .build()
            builder.setCalendarConstraints(constraints).build()
        } ?: run {
            builder.build()
        }


        datePicker.addOnCancelListener {
            callback(null)
        }

        datePicker.addOnNegativeButtonClickListener {
            callback(null)
        }

        datePicker.addOnPositiveButtonClickListener {
            callback(it)
        }

        datePicker.show(parentFragmentManager, datePicker.toString())

    }

}

enum class PURPOSE(val displayValue: String) {
    CAREMGT("Care Management"),
    PUBHLTH("Public Health"),
    BTG("Break the glass"),
    HPAYMT("Healthcare Payment"),
    DSRCH("Disease Specific Healthcare research")
}

enum class HITYPES(val displayValue: String) {
    PRESCRIPTION("Prescription"),
    DIAGNOSTICREPORT("Diagnostic Report"),
    OPCONSULTATION("OP Consultation"),
    DISCHARGESUMMARY("Discharge Summary"),
    IMMUNIZATIONRECORD("Immunization Record"),
    HEALTHDOCUMENTRECORD("Record Artifact"),
    WELLNESSRECORD("Wellness Record")
}

enum class ACCESS_MODE(val value: String) {
    VIEW("VIEW")
}
