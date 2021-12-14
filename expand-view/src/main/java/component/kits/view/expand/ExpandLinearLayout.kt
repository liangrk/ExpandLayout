package component.kits.view.expand

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import component.kits.view.R
import component.kits.view.ViewKits

/**
 * @author : wing-hong Create by 2021/12/14 13:49
 *
 * 垂直方向的textview + ViewGroup折叠控件.
 * 注意: 当前控件只支持两个子view 且第一个view必须为textView
 */
class ExpandLinearLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attributeSet, defStyle) {

    private var expandTextViewId: Int = -1
    private var expandBottomLayoutRes: Int = -1

    private var expandDuration: Int = 300
    private var collapseMaxLine: Int = 8

    private var textViewEnableClick = true

    init {
        initAttr(context, attributeSet)
        orientation = VERTICAL
    }

    private var textView: TextView? = null
    private var bottomLayout: View? = null

    /**
     * 是否需要重新测量
     * 对应 [textView] 是否重新设置了maxLines
     */
    private var isRevisitMeasure = true

    /**
     * [textView] 有数据无折叠的真实高度
     */
    private var realTotalTextHeight = -1

    /**
     * [textView] 折叠 [collapseMaxLine] 行后剩余的高度
     */
    private var collapseTextHeight = -1

    /**
     * 收起的标志. 用于在 onMeasure 时限制 [textView] 的最大行数
     */
    private var collapseState = true

    /**
     * 用于记录 [textView] 与 parent 间距
     */
    private var textViewPadding = -1

    private var onExpand: ExpandFunction? = null
    private var onCollapse: ExpandFunction? = null

    private fun initAttr(context: Context, attributeSet: AttributeSet?) {
        val typeArr = context.obtainStyledAttributes(attributeSet, R.styleable.ExpandLinearLayout)
        expandTextViewId = typeArr.getResourceId(
            R.styleable.ExpandLinearLayout_expand_textView_id,
            expandTextViewId
        )
        expandBottomLayoutRes = typeArr.getResourceId(
            R.styleable.ExpandLinearLayout_expand_bottom_layout,
            expandBottomLayoutRes
        )
        expandDuration =
            typeArr.getInt(R.styleable.ExpandLinearLayout_expand_anim_duration, expandDuration)
        collapseMaxLine =
            typeArr.getInt(R.styleable.ExpandLinearLayout_expand_collapse_max_line, collapseMaxLine)
        textViewEnableClick = typeArr.getBoolean(
            R.styleable.ExpandLinearLayout_expand_text_clickable,
            textViewEnableClick
        )
        typeArr.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!isRevisitMeasure || visibility == GONE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        isRevisitMeasure = false
        // 获取最大高度.
        textView?.maxLines = Int.MAX_VALUE
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        realTotalTextHeight = ViewKits.measureTestViewHeight(textView)

        if (textView!!.lineCount <= collapseMaxLine) {
            // 当前内容行数少于指定行数
            bottomLayout?.visibility = GONE
            return
        }

        // 获取最小的高度.
        bottomLayout?.visibility = VISIBLE
        if (collapseState) {
            textView?.maxLines = collapseMaxLine
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
        textView?.post {
            textViewPadding = this.height - (textView?.height ?: 0)
        }
        collapseTextHeight = measuredHeight - (bottomLayout?.height ?: 0)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        // 为了适配textview样式问题, 此view外部设置, 控件不参与绘制只持有引用
        textView = findViewById(expandTextViewId)
            ?: throw IllegalArgumentException("折叠控件的textview是必须的不能为空, 请在$this 控件中定义并指定其id")
        textView?.setOnClickListener(safeListener)

        if (expandBottomLayoutRes < 0) {
            return
        }
        try {
            val currentView = inflate(context, expandBottomLayoutRes, this) as ViewGroup
            if (currentView.childCount > 1) {
                bottomLayout = currentView.getChildAt(1)
                bottomLayout?.setOnClickListener(safeListener)
            }
        } catch (e: Throwable) {
            ViewKits.log("$this expandBottomLayoutRes inflate err:${e.message} trace:${e.printStackTrace()}")
        }
    }

    override fun setOrientation(orientation: Int) {
        super.setOrientation(VERTICAL)
    }

    /**
     * 是否设置 [textView] 的点击事件
     * @param enable true: 允许响应 | false: 不
     */
    fun enableTextClickable(enable: Boolean) {
        val listener = if (enable) safeListener else null
        textView?.setOnClickListener(listener)
    }

    /**
     * 增加展开/折叠的监听函数
     * @param onExpand 展开回调函数
     * @param onCollapse 收起回调函数
     */
    private fun addExpandCollapseObserver(
        onExpand: ExpandFunction? = null,
        onCollapse: ExpandFunction? = null
    ) {
        this.onExpand = onExpand
        this.onCollapse = onCollapse
    }

    fun setText(
        charSequence: CharSequence,
        onExpand: ExpandFunction? = null,
        onCollapse: ExpandFunction? = null
    ) {
        textView?.text = charSequence
        addExpandCollapseObserver(onExpand,onCollapse)
        isRevisitMeasure = true
    }

    private var onIntercept = false

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return onIntercept
    }

    /**
     * 点击事件处理
     * 1.折叠标志位更改.
     * 2.动画播放
     */
    private val safeListener = OnClickListener {
        if (onIntercept) return@OnClickListener
        onIntercept = true
        val params = textView!!.layoutParams
        val startHeight = if (collapseState) {
            collapseTextHeight
        } else {
            realTotalTextHeight
        }
        val endHeight = if (collapseState) {
            realTotalTextHeight
        } else {
            collapseTextHeight
        }

        val animation = ExpandAnimation(startHeight, endHeight, expandDuration.toLong())
        // 因为动画执行中需要不断 onMeasure 期间有collapseState参数影响
        // 但此参数亦应用在动画判断条件中. 因此改变状态放在动画执行之前且在动画参数之后
        collapseState = !collapseState
        animation.executable(onEnd = { state ->
            if (state) {
                onExpand?.invoke()
            } else {
                onCollapse?.invoke()
            }
            onIntercept = false
        }, onChange = {
            val changeVal = it.animatedValue as Int
            when (changeVal - collapseTextHeight) {
                0 -> {
                    // 刚开始展开 || 收起的最后一刻
                    params.height = collapseTextHeight - textViewPadding
                }
                else -> {
                    // 展开/收起的变化过程
                    params.height = changeVal
                }
            }
            textView?.layoutParams = params
        })
    }
}