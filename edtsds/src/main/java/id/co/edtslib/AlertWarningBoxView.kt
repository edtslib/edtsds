package id.co.edtslib

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat

class AlertWarningBoxView: AlertSuccessBoxView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        binding.clContent.setBackgroundResource(R.drawable.bg_support_warning)
        binding.textView.setTextColor(ContextCompat.getColor(context, R.color.colorSupportWarning))
        binding.imageView.setImageResource(R.drawable.ic_alert_warning)

    }

}