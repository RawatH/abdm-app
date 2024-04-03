package org.commcare.dalvik.abha.ui.main.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.PatientConsentCellBinding
import org.commcare.dalvik.abha.ui.main.fragment.HITYPES
import org.commcare.dalvik.abha.utility.CommonUtil
import org.commcare.dalvik.domain.model.DATE_FORMAT
import org.commcare.dalvik.domain.model.PatientConsentModel

class PatientConsentAdapter(
    val patientName: String?,
    val callback: (patientConsentModel: PatientConsentModel) -> Unit
) :
    PagingDataAdapter<PatientConsentModel, PatientConsentAdapter.PatientConsentViewHolder>(
        COMPARATOR
    ) {

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<PatientConsentModel>() {
            override fun areItemsTheSame(
                oldItem: PatientConsentModel,
                newItem: PatientConsentModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: PatientConsentModel,
                newItem: PatientConsentModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onBindViewHolder(holder: PatientConsentViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bindModel(item)
            holder.binding.patientContentHolder.setOnClickListener {
                callback.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientConsentViewHolder {
        val binding = PatientConsentCellBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatientConsentViewHolder(binding)
    }


    inner class PatientConsentViewHolder(val binding: PatientConsentCellBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindModel(model: PatientConsentModel) {
            binding.model = model
            renderHealthInfoTypes()
            binding.patientName.text = patientName

            model.healthInfoFromDate?.let {
                binding.fromDate.text =
                    CommonUtil.getUtcTimeFromDate(it, DATE_FORMAT.CONSENT_LIST_TIME)
            }

            model.healthInfoToDate?.let {
                binding.toDate.text =
                    CommonUtil.getUtcTimeFromDate(it, DATE_FORMAT.CONSENT_LIST_TIME)
            }

            model.expiryDate?.let {
                binding.expiryDate.text =
                    CommonUtil.getUtcTimeFromDate(it, DATE_FORMAT.CONSENT_LIST_TIME)
            }

            model.creationDate?.let {
                binding.consentCreationDate.text =
                    CommonUtil.getUtcTimeFromDate(it, DATE_FORMAT.CONSENT_LIST_TIME)
            }
            model.lastModified?.let {
                if (model.status != "GRANTED") {
                    binding.consentGrantedDate.text = ""
                } else {
                    binding.consentGrantedDate.text =
                        CommonUtil.getUtcTimeFromDate(it, DATE_FORMAT.CONSENT_LIST_TIME)
                }
            }

            binding.statusDateLabel.apply {
                when (model.status) {
                    "GRANTED" -> {
                        text = resources.getString(R.string.granted_date)
                    }

                    "EXPIRED" -> {
                        text = resources.getString(R.string.expired_date)
                    }

                    "DENIED" -> {
                        text = resources.getString(R.string.denied_date)
                    }

                    "REVOKED" -> {
                        text = resources.getString(R.string.revoked_date)
                    }
                    else ->{
                        text = model.status
                    }
                }
            }
            binding.statusDate.apply {
                when (model.status) {
                    "GRANTED" -> {
                        model.grantedDate?.let {
                            text = CommonUtil.getUtcTimeFromDate(it, DATE_FORMAT.CONSENT_LIST_TIME)
                        } ?: run { text = "Unknown Date" }
                    }

                    "EXPIRED" -> {
                        model.expiryDate?.let {
                            text = CommonUtil.getUtcTimeFromDate(it, DATE_FORMAT.CONSENT_LIST_TIME)
                        } ?: run { text = "Unknown Date" }
                    }

                    "DENIED" -> {
                        model.deniedDate?.let {
                            text = CommonUtil.getUtcTimeFromDate(it, DATE_FORMAT.CONSENT_LIST_TIME)
                        } ?: run { text = "Unknown Date" }
                    }

                    "REVOKED" -> {
                        model.revokedDate?.let {
                            text = CommonUtil.getUtcTimeFromDate(it, DATE_FORMAT.CONSENT_LIST_TIME)
                        } ?: run { text = "Unknown Date" }
                    }
                }
            }

        }


        private fun renderHealthInfoTypes() {

            binding.healthTypeChipGroup.removeAllViews()
            binding.model?.healthInfoType?.forEach {
                val chip = Chip(binding.root.context, null, R.style.HiTypeStyle)
                chip.text = HITYPES.getDisplayValue(it)
                chip.setTextColor(Color.BLACK)
                binding.healthTypeChipGroup.addView(chip)
            }

        }

    }


}