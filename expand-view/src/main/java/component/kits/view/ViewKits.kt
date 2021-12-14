package component.kits.view

import android.util.Log
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
    fun measureTestViewHeight(textView: TextView?): Int {
        if (textView == null) return 0
        val textHeight = textView.layout?.getLineTop(textView.lineCount) ?: 0
        val paddingVertical = textView.compoundPaddingBottom + textView.compoundPaddingTop
        return textHeight + paddingVertical
    }

    internal fun log(msg: String) {
        Log.w("v-kits", msg)
    }
}