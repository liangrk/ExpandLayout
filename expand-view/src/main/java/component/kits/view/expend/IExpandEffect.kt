package component.kits.view.expend

import android.widget.TextView

/**
 * @author : wing-hong Create by 2021/12/01 11:00
 */
interface IExpandEffect {

    /**
     * 获取动画
     */
    fun getAnimationObj(startHeight: Int, endHeight: Int, duration:Long): ExpandAnimation {
        return ExpandAnimation(startHeight, endHeight, duration)
    }

    /**
     * 获取textview的真实高度
     */
    fun getTextViewRealHeight(textView: TextView): Int {
        val layout = textView.layout
        val textHeight = layout?.getLineTop(textView.lineCount) ?: 0
        val paddingVertical = textView.compoundPaddingBottom + textView.compoundPaddingTop
        return textHeight + paddingVertical
    }
}