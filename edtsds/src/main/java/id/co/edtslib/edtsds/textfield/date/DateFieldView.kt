package id.co.edtslib.edtsds.textfield.date

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.view.isVisible
import id.co.edtslib.edtsds.R
import id.co.edtslib.edtsds.bottom.BottomLayoutDialog
import id.co.edtslib.edtsds.databinding.DsDateFieldSpinnerBinding
import id.co.edtslib.edtsds.databinding.DsViewDateFieldBinding
import id.co.edtslib.edtsds.databinding.ViewDatePickerBinding
import java.text.SimpleDateFormat
import java.util.*

class DateFieldView: FrameLayout {
    constructor(context: Context) : super(context) {
        init(null)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    enum class CalendarType {
        Calendar, Spinner
    }

    private val binding = DsViewDateFieldBinding.inflate(LayoutInflater.from(context), this, true)
    private var selectedDate: Date? = null

    var delegate: DateFieldDelegate? = null

    var spinnerTitle: String? = null
    var spinnerButtonText: String? = null
    var showIcon = true
        set(value) {
            field = value
            binding.imageView.isVisible = value
        }

    var calendarType = CalendarType.Calendar

    var date: Date? = null
        set(value) {
            field = value
            setTextValue()

            if (value != null) {
                val simpleDateFormat = SimpleDateFormat(format, Locale("ID"))
                delegate?.onDateChanged(value, simpleDateFormat.format(value))
            }

            binding.tvLabel.isVisible = autoShowLabel != false || value != null
        }

    var format = "dd-MM-yyyy"
    var minAge = 0

    var hint: String? = null
        set(value) {
            field = value
            setTextValue()
        }

    var label: String? = null
        set(value) {
            field = value
            binding.tvLabel.text = label
        }

    var autoShowLabel: Boolean? = null
        set(value) {
            field = value
            binding.tvLabel.isVisible = value != false
        }

    var error: String? = null
        set(value) {
            field = value

            binding.tvError.isVisible = value?.isNotEmpty() == true
            binding.tvError.text = value

            isSelected = value?.isNotEmpty() == true
        }



    private fun setTextValue() {
        binding.tvValue.text = if (date == null) hint else {
            val simpleDateFormat = SimpleDateFormat(format, Locale("ID"))
            simpleDateFormat.format(date!!)
        }
        binding.tvValue.isActivated = date != null
    }

    private fun init(attrs: AttributeSet?) {
        error = null

        binding.editText.setOnFocusChangeListener { v, b ->
            if (b) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
            }
            isActivated = b
            binding.tvValue.isActivated = date != null

            binding.tvLabel.isVisible = b || autoShowLabel != false || date != null
        }

        binding.root.setOnClickListener {
            binding.editText.requestFocus()

            if (calendarType == CalendarType.Spinner) {
                showSpinner()
            }
            else {
                showCalendar()
            }
        }

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.DateFieldView,
                0, 0
            )

            minAge = a.getInteger(R.styleable.DateFieldView_minAge, 0)
            val lFormat = a.getString(R.styleable.DateFieldView_dateFormat)
            if (lFormat != null) {
                format = lFormat
            }

            spinnerTitle = a.getString(R.styleable.DateFieldView_spinnerTitle)
            spinnerButtonText = a.getString(R.styleable.DateFieldView_spinnerButtonText)
            hint = a.getString(R.styleable.DateFieldView_hint)
            label = a.getString(R.styleable.DateFieldView_label)
            autoShowLabel = a.getBoolean(R.styleable.DateFieldView_autoShowLabel, true)
            showIcon =  a.getBoolean(R.styleable.DateFieldView_showIcon, true)

            val calendarTypeIndex = a.getInt(R.styleable.DateFieldView_calendarType, 0)
            calendarType = CalendarType.values()[calendarTypeIndex]

            a.recycle()
        }
    }

    private fun getMaxDate(): Long {
        val now = Calendar.getInstance()
        now.time = Date()
        now.set(Calendar.YEAR, now.get(Calendar.YEAR)-minAge)

        return now.time.time
    }

    private fun showSpinner() {
        val binding = DsDateFieldSpinnerBinding.inflate(LayoutInflater.from(context))
        binding.bvSubmit.text = spinnerButtonText
        binding.datePicker.maxDate = getMaxDate()

        selectedDate = if (date == null) Date() else date!!

        val calendar = Calendar.getInstance()
        calendar.time = if (selectedDate == null) Date() else selectedDate!!

        binding.datePicker.init(calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)
        ) { _, year, month, date ->
            val result = Calendar.getInstance()
            result.set(Calendar.YEAR, year)
            result.set(Calendar.MONTH, month)
            result.set(Calendar.DATE, date)

            selectedDate = result.time
        }

        val dialog = (if (spinnerTitle == null) "" else spinnerTitle)?.let {
            BottomLayoutDialog.showTray(context = context,
                title = it, contentView = binding.root)
        }

        binding.bvSubmit.setOnClickListener {
            date = selectedDate
            dialog?.close()
        }


    }

    private fun showCalendar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            val calendar = Calendar.getInstance()
            calendar.time = if (date == null) Date() else date!!
            selectedDate = calendar.time

            val dialog = DatePickerDialog(context, R.style.CalendarDatePickerDialog,
                { _, year, month, date ->
                    val result = Calendar.getInstance()
                    result.set(Calendar.YEAR, year)
                    result.set(Calendar.MONTH, month)
                    result.set(Calendar.DATE, date)

                    this@DateFieldView.date = result.time
                }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE))

            dialog.datePicker.maxDate = getMaxDate()
            dialog.show()
        }
        else {
            val binding = ViewDatePickerBinding.inflate(LayoutInflater.from(context), null, false)
            binding.datePicker.maxDate = getMaxDate()

            val calendar = Calendar.getInstance()
            calendar.time = if (date == null) Date() else date!!

            binding.datePicker.init(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)
            ) { _, year, month, date ->
                val result = Calendar.getInstance()
                result.set(Calendar.YEAR, year)
                result.set(Calendar.MONTH, month)
                result.set(Calendar.DATE, date)

                selectedDate = result.time
            }

            val builder = AlertDialog.Builder(context)
            builder.setView(binding.root)
            builder.setNegativeButton(android.R.string.cancel) {
                    p0, _ -> p0.dismiss()
            }

            builder.setPositiveButton(android.R.string.ok
            ) { p0, _ ->
                this@DateFieldView.date = selectedDate
                p0?.dismiss()
            }
            builder.show()
        }
    }
}