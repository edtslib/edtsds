package id.co.edtslib.edtsds.boarding

import android.annotation.SuppressLint
import android.view.Gravity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import id.co.edtslib.baserecyclerview.BaseRecyclerViewAdapterDelegate
import id.co.edtslib.baserecyclerview.BaseViewHolder
import id.co.edtslib.edtsds.databinding.AdapterBoardingItemBinding

class BoardingHolder(private val viewBinding: AdapterBoardingItemBinding, private var height: Float,
                     private var alignment: BoardingView.Alignment
) : BaseViewHolder<BoardingData>(viewBinding) {
    @SuppressLint("DiscouragedApi")
    override fun setData(
        list: MutableList<BoardingData>,
        position: Int,
        delegate: BaseRecyclerViewAdapterDelegate<BoardingData>?
    ) {
        viewBinding.tvTitle.text = list[position].title
        val layoutParamsTitle = viewBinding.tvTitle.layoutParams as LinearLayoutCompat.LayoutParams

        viewBinding.tvDescription.text = list[position].description
        val layoutParamsDescription = viewBinding.tvDescription.layoutParams as LinearLayoutCompat.LayoutParams

        when (alignment) {
            BoardingView.Alignment.Center -> {
                viewBinding.tvTitle.gravity = Gravity.CENTER_HORIZONTAL
                layoutParamsTitle.gravity = Gravity.CENTER_HORIZONTAL

                viewBinding.tvDescription.gravity = Gravity.CENTER_HORIZONTAL
                layoutParamsDescription.gravity = Gravity.CENTER_HORIZONTAL
            }
            BoardingView.Alignment.Right -> {
                viewBinding.tvTitle.gravity = Gravity.END
                layoutParamsTitle.gravity = Gravity.END

                viewBinding.tvDescription.gravity = Gravity.END
                layoutParamsDescription.gravity = Gravity.END
            }
            else -> {
                viewBinding.tvTitle.gravity = Gravity.START
                layoutParamsTitle.gravity = Gravity.START

                viewBinding.tvDescription.gravity = Gravity.START
                layoutParamsDescription.gravity = Gravity.START
            }
        }

        val layoutParams = viewBinding.imageView.layoutParams as LinearLayoutCompat.LayoutParams
        if (height > 0f && layoutParams.height != height.toInt()) {
            layoutParams.height = height.toInt()
        }

        val image = list[position].image
        if (image != null) {
            if (image.startsWith("http")) {
                Glide.with(viewBinding.root).load(image).into(viewBinding.imageView)
            }
            else {
                if (viewBinding.root.context is FragmentActivity) {
                    val fragmentActivity = viewBinding.root.context as FragmentActivity
                    val resourceId = fragmentActivity.resources.getIdentifier(
                        image, "drawable",
                        fragmentActivity.packageName
                    )
                    Glide.with(fragmentActivity).load(resourceId).into(viewBinding.imageView)
                }
            }
        }
    }

}