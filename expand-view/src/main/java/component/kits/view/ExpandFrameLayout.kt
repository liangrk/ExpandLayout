package component.kits.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

/**
 * @author : wing-hong Create by 2021/12/01 11:01
 */
class ExpandFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var expandTextViewId: Int = -1
    private var expandBottomLayoutRes: Int = -1
    private var expandWidth: Int = LayoutParams.MATCH_PARENT
    private var expandHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT

    private var collapseWidth: Int = LayoutParams.MATCH_PARENT
    private var collapseHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT

    init {
        initAttr(attrs)
    }

    private lateinit var expandDelegate: ExpandDelegate

    private fun initAttr(attrs: AttributeSet?) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandFrameLayout)
        val maxLine = typeArray.getInt(R.styleable.ExpandFrameLayout_expand_max_line, 10)
        val duration = typeArray.getInt(R.styleable.ExpandFrameLayout_expand_anim_duration, 300)

        expandWidth = typeArray.getLayoutDimension(
            R.styleable.ExpandFrameLayout_expand_bottom_width,
            LayoutParams.MATCH_PARENT
        )
        expandHeight = typeArray.getLayoutDimension(
            R.styleable.ExpandFrameLayout_expand_bottom_height,
            LayoutParams.WRAP_CONTENT
        )
        collapseWidth = typeArray.getLayoutDimension(
            R.styleable.ExpandFrameLayout_expand_collapse_width,
            expandWidth
        )
        collapseHeight = typeArray.getLayoutDimension(
            R.styleable.ExpandFrameLayout_expand_collapse_height,
            expandHeight
        )

        expandTextViewId =
            typeArray.getResourceId(R.styleable.ExpandFrameLayout_expand_textView_id, -1)
        expandBottomLayoutRes =
            typeArray.getResourceId(R.styleable.ExpandFrameLayout_expand_bottom_layout, -1)
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
                val bottomView = LayoutInflater.from(context)
                    .inflate(expandBottomLayoutRes, null)
                val params = LayoutParams(expandWidth, expandHeight)
                params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomView.layoutParams = params
                addView(bottomView)
                expandDelegate.bottomLayout = bottomView

                bottomView.post {
                    expandDelegate.setOnClick(diff = bottomView.measuredHeight)
                }
            } else {
                expandDelegate.setOnClick()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            expandDelegate.setOnClick()
        }
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