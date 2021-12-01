package component.kits.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView

/**
 * @author : wing-hong Create by 2021/12/01 10:55
 */
class ExpandLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var expandTextViewId: Int = -1
    private var expandBottomLayoutRes: Int = -1

    init {
        initAttr(attrs)
        orientation = VERTICAL
    }

    private lateinit var expandDelegate: ExpandDelegate

    private fun initAttr(attrs: AttributeSet?) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandLinearLayout)
        val maxLine = typeArray.getInt(R.styleable.ExpandLinearLayout_expand_max_line, 8)
        val duration = typeArray.getInt(R.styleable.ExpandLinearLayout_expand_anim_duration, 300)

        expandTextViewId =
            typeArray.getResourceId(R.styleable.ExpandLinearLayout_expand_textView_id, -1)
        expandBottomLayoutRes =
            typeArray.getResourceId(R.styleable.ExpandLinearLayout_expand_bottom_layout, -1)
        expandDelegate = ExpandDelegate(maxLine, duration)
        typeArray.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        val targetTextView: TextView = findViewById(expandTextViewId)
            ?: throw IllegalArgumentException("expand-textview的id必须设置,当前资源id:$expandTextViewId")

        expandDelegate.inflateTextView(targetTextView)
        try {
            if (expandBottomLayoutRes > 0) {
                expandDelegate.bottomLayout = inflate(context, expandBottomLayoutRes, this)
            }
        } catch (ignore: Exception) {
            // ignore bottom layout added err
        }

        expandDelegate.setOnClick()
    }

    /**
     * 设置允许textview点击展开
     */
    fun setArrowTextClick() {
        expandDelegate.setOnClick(force = true)
    }

    override fun setOrientation(orientation: Int) {
        if (orientation != VERTICAL) throw IllegalArgumentException("目前折叠布局仅支持垂直方向")
        super.setOrientation(orientation)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!expandDelegate.isNeedMeasure || this.visibility == GONE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        // 允许长文本再测量
        expandDelegate.onMeasureChange()
        expandDelegate.textView.maxLines = Int.MAX_VALUE
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 设置textview真实高度 & 获取折叠后的高度. 默认折叠
        expandDelegate.measureTextHeight(this) {
            // 设置了maxLines 重新测量
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            measuredHeight
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return expandDelegate.isInterceptTouch
    }

    fun setExpandText(charSequence: CharSequence, listener: OnExpandStateListener? = null) {
        expandDelegate.setText(charSequence)
        expandDelegate.onCollapseListener = listener
    }

    fun getExpandHeight() = expandDelegate.realTotalHeight

    fun getCollapseHeight() = expandDelegate.collapseHeight
}