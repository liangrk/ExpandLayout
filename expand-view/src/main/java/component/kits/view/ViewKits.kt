package component.kits.view

import android.util.Log
import android.view.View
import android.widget.TextView

/**
 * @author : wing-hong Create by 2021/12/14 11:46
 */
object ViewKits {

    /**
     * 获取当前textview的高度.
     * @param textView
     * @sample textView: 更改maxLines后 需要父view进行measure后 再调用
     */
    @JvmStatic
    fun measureTextViewHeight(textView: TextView, lineCount: Int = textView.lineCount): Int {
        val textHeight = textView.layout?.getLineTop(lineCount) ?: -1
        val paddingVertical = textView.compoundPaddingBottom + textView.compoundPaddingTop
        val total = textHeight + paddingVertical
        return if (total == 0) {
            -1
        } else {
            total
        }
    }

    fun measureMaxMeasureHeight(textView: TextView?): Int {
        if (textView == null) return -1
        val width = textView.measuredWidth
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec =
            View.MeasureSpec.makeMeasureSpec((Int.MAX_VALUE / 2), View.MeasureSpec.AT_MOST)
        textView.measure(widthMeasureSpec, heightMeasureSpec)
        return if (textView.measuredHeight == 0) {
            -1
        } else {
            textView.measuredHeight
        }
    }

    internal fun log(msg: String) {
        Log.w("v-kits", msg)
    }
}