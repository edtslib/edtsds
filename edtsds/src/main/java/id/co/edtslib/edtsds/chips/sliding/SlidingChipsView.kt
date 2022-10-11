package id.co.edtslib.edtsds.chips.sliding

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.baserecyclerview.BaseRecyclerViewAdapterDelegate
import id.co.edtslib.baserecyclerview.BaseViewHolder
import id.co.edtslib.edtsds.R

open class SlidingChipsView<T> : RecyclerView {
    var autoScroll = true

    class ItemDecoration(private val margin: Int): RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            val position = parent.getChildAdapterPosition(view)
            outRect.left = if (position == 0) 0 else margin
        }
    }

    private var _adapter: ChipAdapter<RecyclerData<T>>? = null

    var firstSelected = false
        set(value) {
            field = value
            if (value) {
                prevSelectedIndex = 0
            }
        }
    var delegate: SlidingChipsDelegate<T>? = null

    private var prevSelectedIndex = -1

    var selectedItem: T? = null
        set(value) {
            field = value
            var i = 0
            for (item in items) {
                if (item?.equals(value) == true) {
                    break
                }

                i++
            }

            selectionIndex = i
        }

    var selectionIndex = 0
        set(value) {
            field = value

            if (prevSelectedIndex >= 0) {
                if (_adapter?.list != null && prevSelectedIndex < _adapter!!.list.size) {

                    val item = _adapter?.list?.get(prevSelectedIndex)
                    item?.selected = false

                    _adapter?.notifyItemChanged(prevSelectedIndex)
                }
            }

            val size = if (_adapter?.list == null) 0 else _adapter!!.list.size
            if (size > 0 && value < size) {
                val item = _adapter?.list?.get(value)
                item?.selected = true

                _adapter?.notifyItemChanged(value)
                prevSelectedIndex = value

                delegate?.onSelected(item?.data!!, value)

                if (autoScroll) {
                    val linearLayoutManager = layoutManager as LinearLayoutManager
                    val last = linearLayoutManager.findLastCompletelyVisibleItemPosition()
                    val first = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                    if (value > last || value < first) {
                        linearLayoutManager.scrollToPosition(value)
                    }
                }
            }
        }

    var items: MutableList<T> = mutableListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value

            val list = mutableListOf<RecyclerData<T>>()
            for ((i, item) in items.withIndex()) {
                list.add(RecyclerData(item, firstSelected && i == 0))
            }

            _adapter?.list = list
            _adapter?.notifyDataSetChanged()
        }

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

    private fun init(attrs: AttributeSet?) {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false

        var textColor =  R.color.colorNeutral50
        var textPadding = resources.getDimensionPixelSize(R.dimen.dimen_20dp).toFloat()
        var chipBackgroundColor = R.color.color_bg_chip
        var strokeColor = 0
        var textStyle = 0

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SlidingChipsView,
                0, 0
            )

            val startEndSpace = a.getDimension(R.styleable.SlidingChipsView_slideChipMargin,
                resources.getDimensionPixelSize(R.dimen.dimen_16dp).toFloat())

            setPadding(startEndSpace.toInt(), 0, startEndSpace.toInt(), 0)

            textColor = a.getResourceId(R.styleable.SlidingChipsView_slideChipTextColor,
                R.color.color_text_chip)

            strokeColor = a.getResourceId(R.styleable.SlidingChipsView_slideChipStrokeColor,
                0)

            textPadding = a.getDimension(R.styleable.SlidingChipsView_slideChipTextPadding,
                resources.getDimensionPixelSize(R.dimen.dimen_20dp).toFloat())

            chipBackgroundColor = a.getResourceId(R.styleable.SlidingChipsView_slideChipBackground,
                R.color.color_bg_chip)

            textStyle = a.getResourceId(R.styleable.SlidingChipsView_slideChipTextStyle, 0)


            a.recycle()
        }

        clipToPadding = false
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)

        addItemDecoration(ItemDecoration(context.resources.getDimensionPixelSize(R.dimen.dimen_8dp)))

        _adapter = ChipAdapter(textColor, textPadding, chipBackgroundColor, strokeColor,
            textStyle)
        _adapter?.delegate = object : BaseRecyclerViewAdapterDelegate<RecyclerData<T>> {
            override fun onClick(t: RecyclerData<T>, position: Int, holder: BaseViewHolder<RecyclerData<T>>?) {
                selectionIndex = position
            }

            override fun onDraw(t: RecyclerData<T>, position: Int) {
            }
        }

        adapter = _adapter
    }
}