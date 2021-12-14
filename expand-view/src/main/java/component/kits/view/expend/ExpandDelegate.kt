package component.kits.view.expend

import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * @author : wing-hong Create by 2021/12/01 11:45
 */
class ExpandDelegate(
    private val maxLine: Int,
    private var duration: Int
) : View.OnClickListener, IExpandEffect {

    /**
     * 标记是否需要重新测量, 在调用setText后, 通知ExpandLayout进行重新measure
     */
    var isNeedMeasure: Boolean = true
        private set

    /**
     * 用于标记收起状态. 默认收起
     */
    private var collapseState: Boolean = true

    lateinit var textView: TextView
        private set

    var bottomLayout: View? = null

    /**
     * 真实无折叠的高度
     */
    var realTotalHeight: Int = 0

    /**
     * 记录折叠textview在 Layout中的高度差
     */
    private var marginBetweenTxtAndBottom = 0

    /**
     * 折叠后的高度
     */
    var collapseHeight: Int = 0

    /**
     * 是否拦截ExpandLayout的触摸事件
     */
    var isInterceptTouch: Boolean = false
        private set

    var onCollapseListener: OnExpandStateListener? = null

    /**
     * 额外的偏移量
     */
    private var diff: Int = 0

    fun inflateTextView(view: TextView) {
        textView = view
    }

    /**
     * 设置重新测量条件
     */
    fun onMeasureChange(isNeed: Boolean = false) {
        isNeedMeasure = isNeed
    }

    /**
     * 测量真实高度
     * @param onChange layout中重新super.onMeasure测量 返回layout的measureHeight.
     */
    fun measureTextHeight(viewGroup: ViewGroup, onChange: (() -> Int)) {
        if (textView.lineCount <= maxLine) {
            // 当前文本行数没有超出指定的行数
            bottomLayout?.visibility = View.GONE
            return
        }
        bottomLayout?.visibility = View.VISIBLE
        realTotalHeight = getTextViewRealHeight(textView)
        if (collapseState) {
            // 收起时, 需要设置允许最大的行数
            textView.maxLines = maxLine
        }
        // 调用super.onMeasure进行测量 返回测量的高度
        val measureHeight = onChange()
        if (collapseState) {
            textView.post { marginBetweenTxtAndBottom = viewGroup.height - textView.height }
            collapseHeight = measureHeight - (bottomLayout?.height?:0)
        }
    }

    /**
     * 设置点击事件, 是否允许点击textview开始折叠
     * 默认没有底部layout时允许点击
     * @param force 是否强制允许textview响应点击事件
     * @param diff 设置展开动画的偏移量, 默认为0 按所需展开 单位px
     */
    fun setOnClick(force: Boolean = false, diff: Int = 0) {
        this.diff = diff
        val listener = if (force || bottomLayout == null) this else null
        textView.setOnClickListener(listener)
        bottomLayout?.setOnClickListener(this)
    }

    fun setText(char: CharSequence) {
        textView.text = char.toString()
        onMeasureChange(true)
    }

    override fun onClick(view: View?) {
        // 预防Layout-onIntercept没有拦截
        if (isInterceptTouch) return
        isInterceptTouch = true
        // 更改折叠状态
        collapseState = !collapseState

        val params = textView.layoutParams
        matchAnim()
            .clear()
            .start {
                val change = it.animatedValue as Int
                println("变化值:${change}, collaps:$collapseHeight,diff:$diff")
                // 动画变化
                when (change - collapseHeight) {
                    0 -> {
                        // maybe 起始值
                        params.height = collapseHeight - marginBetweenTxtAndBottom
                    }
                    else -> {
                        // 展开/收起过程
                        params.height = change - diff
                    }
                }

                textView.layoutParams = params
                onCollapseListener?.onExpandStateChange(bottomLayout, collapseState)
                resetIntercept(change) {
                    isInterceptTouch = false
                }
            }
    }

    /**
     * 在动画结束后重置拦截条件
     */
    private fun resetIntercept(change: Int, onEnd: (() -> Unit)) {
        if (collapseState) {
            // 折叠
            if (change == collapseHeight) {
                onEnd()
            }
        } else {
            // 展开
            if (change == realTotalHeight) {
                onEnd()
            }
        }
    }

    /**
     * 通过判断折叠状态获取高度变化的动画
     * 此处存在一些歧义, 因[collapseState]默认是折叠的, 点击的时候变为展开
     * 所以此处需要先还原为之前的值来获取起始高度跟终点高度
     */
    private fun matchAnim(): ExpandAnimation {
        val start = if (!collapseState) {
            // 未展开
            collapseHeight
        } else {
            // 展开
            realTotalHeight + diff
        }

        val end = if (!collapseState) {
            // 未展开
            realTotalHeight + diff
        } else {
            // 暂开
            collapseHeight
        }
        return getAnimationObj(start, end, duration.toLong())
    }
}