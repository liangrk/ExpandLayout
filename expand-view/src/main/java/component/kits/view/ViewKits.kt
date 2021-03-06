package component.kits.view

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
        val minLineCount = lineCount.coerceAtMost(textView.lineCount)
        val textHeight = textView.layout?.getLineTop(minLineCount) ?: -1
        val paddingVertical = textView.compoundPaddingBottom + textView.compoundPaddingTop
        val total = textHeight + paddingVertical
        return if (total == 0) {
            -1
        } else {
            total
        }
    }

    internal fun log(msg: String) {
        // Log.w("v-kits", msg)
    }
}