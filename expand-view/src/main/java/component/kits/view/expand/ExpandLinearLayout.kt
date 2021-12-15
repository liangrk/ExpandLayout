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

    var expandDuration: Int = 300
        private set

    var collapseMaxLine: Int = 8
        private set

    private var textViewEnableClick = true

    init {
        initAttr(context, attributeSet)
        orientation = VERTICAL
    }

    private var textView: TextView? = null
    private var bottomLayout: View? = null

    /**
     * 收起的标志. 用于在 onMeasure 时限制 [textView] 的最大行数
     */
    private var collapseState = true

    private lateinit var measureDelegate: ExpandMeasureDelegate

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

    override fun onFinishInflate() {
        super.onFinishInflate()

        // 为了适配textview样式问题, 此view外部设置, 控件不参与绘制只持有引用
        textView = findViewById(expandTextViewId)
            ?: throw IllegalArgumentException("折叠控件的textview是必须的不能为空, 请在$this 控件中定义并指定其id")

        enableTextClickable(textViewEnableClick)

        if (expandBottomLayoutRes < 0) {
            // 委托测量textview的最大跟最小高度. 测量完成后设置默认最小高度.
            measureDelegate = ExpandMeasureDelegate(textView!!, collapseMaxLine)
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

        // 委托测量textview的最大跟最小高度. 测量完成后设置默认最小高度.
        measureDelegate = ExpandMeasureDelegate(textView!!, collapseMaxLine) {
            if (textView!!.lineCount <= collapseMaxLine) {
                bottomLayout?.visibility = View.GONE
            }
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
        addExpandCollapseObserver(onExpand, onCollapse)
    }

    private var onIntercept = false

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return onIntercept
    }

    fun getExpandHeight(): Int {
        return if (this::measureDelegate.isInitialized) {
            measureDelegate.realTotalHeight
        } else {
            -1
        }
    }

    fun getCollapseHeight(): Int {
        return if (this::measureDelegate.isInitialized) {
            measureDelegate.collapseHeight
        } else {
            -1
        }
    }

    /**
     * 点击事件处理
     * 1.折叠标志位更改.
     * 2.动画播放
     */
    private val safeListener = OnClickListener {
        // 因 measureDelegate 采用延迟声明 注意此处必须加上expandBottomLayoutRes的判断
        if (onIntercept) return@OnClickListener
        onIntercept = true
        val params = textView!!.layoutParams
        val startHeight = if (collapseState) {
            measureDelegate.collapseHeight
        } else {
            measureDelegate.realTotalHeight
        }
        val endHeight = if (collapseState) {
            measureDelegate.realTotalHeight
        } else {
            measureDelegate.collapseHeight
        }

        collapseState = !collapseState

        if (startHeight == endHeight) {
            onIntercept = false
            return@OnClickListener
        }
        val animation = ExpandAnimation(startHeight, endHeight, expandDuration.toLong())
        animation.executable(onEnd = { state ->
            if (state) {
                onExpand?.invoke(bottomLayout)
            } else {
                onCollapse?.invoke(bottomLayout)
            }
            onIntercept = false
        }, onChange = {
            val changeVal = it.animatedValue as Int
            when (changeVal - measureDelegate.collapseHeight) {
                0 -> {
                    // 刚开始展开 || 收起的最后一刻
                    params.height = measureDelegate.collapseHeight
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