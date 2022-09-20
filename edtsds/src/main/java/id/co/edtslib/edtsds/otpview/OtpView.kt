package id.co.edtslib.edtsds.otpview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.core.widget.addTextChangedListener
import id.co.edtslib.edtsds.R
import id.co.edtslib.edtsds.base.NoPasteEditText

class OtpView : LinearLayoutCompat {
    var editText: AppCompatEditText? = null
    private val textColorResId = ContextCompat.getColorStateList(context, R.color.color_neutral70_support_error)
    private val shapeResId = R.drawable.bg_otp
    private var length = 4
    private var showPassword = false // false. show *
    private var pinType = PinType.Number.ordinal
    private var pinPasswordSymbol: String? = null

    enum class PinType {
        Number, Password, PasswordWithEye
    }

    constructor(context: Context) : super(context)
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

    var delegate: OtpDelegate? = null

    @SuppressLint("ClickableViewAccessibility")
    private fun init(attrs: AttributeSet?) {
        orientation = HORIZONTAL

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.OtpView,
                0, 0
            )

            length = a.getInteger(R.styleable.OtpView_pinLength, 4)
            val textStyleReId = R.style.D1
            val margin = resources.getDimensionPixelSize(R.dimen.dimen_8dp)

            pinType = a.getInteger(R.styleable.OtpView_pinType, 0)
            val pinEyePassword = a.getResourceId(R.styleable.OtpView_pinEyePassword,
                R.drawable.img_password_eye)

            val pinPasswordAnimate = a.getInteger(R.styleable.OtpView_pinPasswordAnimate, 0)
            pinPasswordSymbol = a.getString(R.styleable.OtpView_pinPasswordSymbol)
            if (pinPasswordSymbol == null) {
                pinPasswordSymbol = "*"
            }

            val placeHolderText = a.getString(R.styleable.OtpView_pinPlaceHolderText)
            val placeHolderTextColor = a.getColor(R.styleable.OtpView_pinPlaceHolderTextColor, 0)


            editText = NoPasteEditText(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                editText?.importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO
            }
            editText?.isFocusableInTouchMode = true
            editText?.inputType = InputType.TYPE_CLASS_NUMBER
            editText?.gravity = Gravity.CENTER
            editText?.isSingleLine = true
            editText?.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.transparent
                )
            )
            editText?.isCursorVisible = false
            editText?.imeOptions = EditorInfo.IME_ACTION_DONE

            editText?.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(length))

            addView(editText)

            val layoutParamsEditText = editText?.layoutParams as LayoutParams
            layoutParamsEditText.width = 1//width.toInt()
            layoutParamsEditText.height = 1//height.toInt()

            repeat(length) {
                val textView = AppCompatTextView(context)
                addView(textView)
                textView.gravity = Gravity.CENTER
                textView.isSelected = it == 0

                textView.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        showKeyboard()
                    }

                    false
                }

                val layoutParams = textView.layoutParams as LayoutParams
                layoutParams.width = resources.getDimensionPixelSize(R.dimen.dimen_40dp)
                layoutParams.height = resources.getDimensionPixelSize(R.dimen.dimen_50dp)
                layoutParams.gravity = Gravity.CENTER
                if (it > 0) {
                    layoutParams.leftMargin = margin
                }

                if (shapeResId != 0) {
                    textView.setBackgroundResource(shapeResId)
                }

                if (textStyleReId != 0) {
                    TextViewCompat.setTextAppearance(textView, textStyleReId)
                }

                if (textColorResId != null) {
                    textView.setTextColor(textColorResId)
                }

                if (placeHolderText?.isNotEmpty() == true) {
                    textView.hint = placeHolderText
                    textView.setHintTextColor(placeHolderTextColor)
                }
            }

            editText?.addTextChangedListener {
                if (it != null) {
                    val l = it.length
                    repeat(length) { idx ->
                        if (idx < length) {
                            val textView = getChildAt(idx + 1) as AppCompatTextView
                            textView.text = if (idx < it.length) {
                                when (pinType) {
                                    PinType.Password.ordinal -> {
                                        if (textView.text != pinPasswordSymbol && idx+1 == l) {
                                            animate(idx, pinPasswordAnimate)
                                            it[idx].toString()
                                        }
                                        else {
                                            pinPasswordSymbol
                                        }
                                    }
                                    PinType.PasswordWithEye.ordinal ->
                                        if (showPassword) {
                                            it[idx].toString()
                                        } else {
                                            if (pinPasswordAnimate > 0) {
                                                if (textView.text != pinPasswordSymbol && idx+1 == l) {
                                                    animate(idx, pinPasswordAnimate)
                                                    it[idx].toString()
                                                }
                                                else {
                                                    pinPasswordSymbol
                                                }
                                            }
                                            else {
                                                pinPasswordSymbol
                                            }
                                        }

                                    else -> it[idx].toString()
                                }
                            } else ""
                            textView.isSelected = idx == it.length
                            if (idx == it.length) {
                                if (textColorResId != null) {
                                    textView.setTextColor(textColorResId)
                                }
                                if (shapeResId != 0) {
                                    textView.setBackgroundResource(shapeResId)
                                }
                            }
                        }
                    }

                    if (it.length >= length) {
                        delegate?.onCompleted(it.toString())
                    }

                    delegate?.onTextChanged(it.toString())
                }
            }

            if (pinType == PinType.PasswordWithEye.ordinal) {
                val imageView = AppCompatImageView(context)
                addView(imageView)

                imageView.setImageResource(pinEyePassword)

                val layoutParams = imageView.layoutParams as LinearLayout.LayoutParams
                layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
                layoutParams.marginStart = margin

                imageView.setOnClickListener {
                    showPassword = ! showPassword
                    imageView.isSelected = showPassword

                    redraw(showPassword)
                }
            }

            a.recycle()

            postDelayed({
                showKeyboard()
            }, 250)
        }
    }

    private fun animate(idx: Int, delay: Int) {
        val textView = getChildAt(idx + 1) as AppCompatTextView
        if (textView.tag != null && textView.tag is Runnable) {
            textView.removeCallbacks(textView.tag as Runnable)
        }

        val runnable = {
            if (editText != null && idx <  editText!!.length() ) {
                textView.text = pinPasswordSymbol
            }
            else {
                textView.text = ""
            }
            textView.tag = null
        }
        textView.tag = runnable
        textView.postDelayed(runnable, delay.toLong())
    }

    fun setPinType(pinType: PinType) {
        this.pinType = pinType.ordinal
        redraw(pinType == PinType.Number)
    }

    private fun redraw(showPassword: Boolean) {
        repeat(length) { idx ->
            val s = editText?.text?.toString()

            if (s != null) {
                val textView = getChildAt(idx + 1) as AppCompatTextView
                val cc = if (idx < s.length) s[idx].toString() else ""
                if (cc.isNotEmpty()) {
                    textView.text = if (showPassword) s[idx].toString() else "*"
                }
                else {
                    textView.text = ""
                }
            }
        }
    }

    fun clear() {
        editText?.setText("")

        for (i in 1 until length+1) {
            val textView = getChildAt(i) as AppCompatTextView
            if (textColorResId != null) {
                textView.setTextColor(textColorResId)
            }
            if (shapeResId != 0) {
                textView.setBackgroundResource(shapeResId)
            }
        }
    }

    private fun setInitialColor() {
        if (textColorResId != null) {
            for (i in 1 until length+1) {
                val textView = getChildAt(i) as AppCompatTextView
                textView.setTextColor(textColorResId)
            }
        }
    }

    private fun showKeyboard() {
        editText?.requestFocus()

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun setTextColor(color: Int) {
        for (i in 1 until length+1) {
            val textView = getChildAt(i) as AppCompatTextView
            textView.setTextColor(ContextCompat.getColor(context, color))
        }
    }

    fun setShapeBackgroundResource(resId: Int) {
        for (i in 1 until length+1) {
            val textView = getChildAt(i) as AppCompatTextView
            textView.setBackgroundResource(resId)
        }
    }


}