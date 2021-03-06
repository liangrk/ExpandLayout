package component.kits.view.expand

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import component.kits.view.R
import component.kits.view.ViewKits

/**
 * @author : wing-hong Create by 2021/12/14 18:21
 */
class ExpandFrameLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle), IExpandEffective {

    private var expandTextViewId = -1
    private var bottomLayoutRes = -1

    var expandDuration = 300
        private set

    var collapseMaxLine: Int = 8
        private set

    private var textViewEnableClick = true

    private var textView: TextView? = null
    private var bottomLayout: View? = null

    private var bottomLayoutHeight = -1
    private var configBottomLayoutHeight = -1

    private var lineSpacingMultiplier = 1f

    /**
     * 收起的标志. 用于在 onMeasure 时限制 [textView] 的最大行数
     */
    private var collapseState = true

    private lateinit var measureDelegate: ExpandMeasureDelegate

    private var onExpand: ExpandFunction? = null
    private var onCollapse: ExpandFunction? = null
    private var onReady: ExpandFunction? = null
    private var arrowClick: (() -> Boolean)? = null

    init {
        val typeArr = context.obtainStyledAttributes(attributeSet, R.styleable.ExpandFrameLayout)
        expandTextViewId =
            typeArr.getResourceId(
                R.styleable.ExpandFrameLayout_expand_textView_id,
                expandTextViewId
            )
        bottomLayoutRes = typeArr.getResourceId(
            R.styleable.ExpandFrameLayout_expand_bottom_layout,
            bottomLayoutRes
        )

        expandDuration = typeArr.getInt(
            R.styleable.ExpandFrameLayout_expand_anim_duration,
            expandDuration
        )
        collapseMaxLine = typeArr.getInt(
            R.styleable.ExpandFrameLayout_expand_collapse_max_line,
            collapseMaxLine
        )
        textViewEnableClick = typeArr.getBoolean(
            R.styleable.ExpandFrameLayout_expand_text_clickable,
            textViewEnableClick
        )
        configBottomLayoutHeight = typeArr.getLayoutDimension(
            R.styleable.ExpandFrameLayout_expand_bottom_expand_height,
            bottomLayoutHeight
        )

        lineSpacingMultiplier = typeArr.getFloat(
            R.styleable.ExpandFrameLayout_expand_line_spacing_multiplier,
            lineSpacingMultiplier
        )
        typeArr.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        // 为了适配textview样式问题, 此view外部设置, 控件不参与绘制只持有引用
        textView = findViewById(expandTextViewId)
            ?: throw IllegalArgumentException("折叠控件的textview是必须的不能为空, 请在$this 控件中定义并指定其id")
        enableTextClickable(textViewEnableClick)

        if (bottomLayoutRes < 0) {
            // 委托测量textview的最大跟最小高度. 测量完成后设置默认最小高度.
            measureDelegate =
                ExpandMeasureDelegate(textView!!, collapseMaxLine, lineSpacingMultiplier)
            return
        }

        // 委托测量textview的最大跟最小高度. 测量完成后设置默认最小高度.
        resetMeasureDelegate()
    }

    private fun resetMeasureDelegate() {
        try {
            if (bottomLayout == null) {
                val currentView = inflate(context, bottomLayoutRes, this) as ViewGroup
                if (currentView.childCount > 1) {
                    bottomLayout = currentView.getChildAt(1)
                    bottomLayout?.setOnClickListener(safeListener)
                }
            }
        } catch (e: Throwable) {
            ViewKits.log("$this expandBottomLayoutRes inflate err:${e.message} trace:${e.printStackTrace()}")
        } finally {
            bottomLayout?.post {
                bottomLayoutHeight = bottomLayout?.height ?: 0
                ViewKits.log("bottomLayout-height:$bottomLayoutHeight, bottomLayoutMeasureHeight:${bottomLayout?.measuredHeight}")
            }
        }

        measureDelegate =
            ExpandMeasureDelegate(textView!!, collapseMaxLine, lineSpacingMultiplier) {
                if (it) {
                    bottomLayout?.visibility = GONE
                } else {
                    bottomLayout?.visibility = VISIBLE
                }

                onReady?.invoke(bottomLayout)
            }
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
        onCollapse: ExpandFunction? = null,
        onReady: ExpandFunction? = null,
        arrowClick: (() -> Boolean)?
    ) {
        this.onExpand = onExpand
        this.onCollapse = onCollapse
        this.onReady = onReady
        this.arrowClick = arrowClick
    }

    override fun setText(
        charSequence: CharSequence,
        onExpand: ExpandFunction?,
        onCollapse: ExpandFunction?,
        onReady: ExpandFunction?,
        arrowClick: (() -> Boolean)?,
        overrideMeasure: Boolean
    ) {
        if (overrideMeasure) {
            ViewKits.log("setText-call override measure")
            resetMeasureDelegate()
        }
        textView?.text = charSequence
        addExpandCollapseObserver(onExpand, onCollapse, onReady, arrowClick)
    }

    override fun senEnableBottomClick(enable: Boolean) {
        val listener = if (enable) {
            safeListener
        } else null
        bottomLayout?.setOnClickListener(listener)
    }

    override fun setExpand() {
        if (collapseState) {
            // 收起时才展开
            bottomLayout?.performClick()
        }
    }

    override fun setCollapse() {
        if (collapseState) return
        // 展开时才收起
        bottomLayout?.performClick()
    }

    private var onIntercept = false

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return onIntercept
    }

    override fun getExpandHeight(): Int {
        return if (this::measureDelegate.isInitialized) {
            measureDelegate.realTotalHeight
        } else {
            -1
        }
    }

    override fun getCollapseHeight(): Int {
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
        if (arrowClick?.invoke() == false) {
            return@OnClickListener
        }
        // 因 measureDelegate 采用延迟声明 注意此处必须加上expandBottomLayoutRes的判断
        if (onIntercept) return@OnClickListener
        onIntercept = true
        val params = textView!!.layoutParams

        if (bottomLayout != null && bottomLayoutHeight == 0) {
            ViewKits.log("监听器中底部view高度获取失败")
            bottomLayout!!.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )
            bottomLayoutHeight = bottomLayout!!.height
            ViewKits.log("监听器中底部bottom重新测量值:$bottomLayoutHeight, height:${bottomLayout!!.height}")
        }

        // 底部bottom偏移量
        val offset = if (configBottomLayoutHeight == -1) {
            bottomLayoutHeight
        } else {
            configBottomLayoutHeight
        }

        val startHeight = if (collapseState) {
            measureDelegate.collapseHeight
        } else {
            measureDelegate.realTotalHeight + offset
        }
        val endHeight = if (collapseState) {
            measureDelegate.realTotalHeight + offset
        } else {
            measureDelegate.collapseHeight
        }

        collapseState = !collapseState

        val bottomParams = bottomLayout?.layoutParams

        val animation = ExpandAnimation(startHeight, endHeight, expandDuration.toLong())
        animation.executable(onEnd = { state ->
            if (state) {
                bottomParams?.height = offset
                bottomLayout?.layoutParams = bottomParams

                onExpand?.invoke(bottomLayout)
            } else {
                bottomParams?.height = bottomLayoutHeight
                bottomLayout?.layoutParams = bottomParams
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