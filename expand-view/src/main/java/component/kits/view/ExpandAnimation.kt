package component.kits.view

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * @author : wing-hong Create by 2021/12/01 11:13
 * @since 折叠动画
 * @param startHeight 动画起始高度
 * @param endHeight 动画结束高度
 */
class ExpandAnimation(
    private val startHeight: Int,
    private val endHeight: Int,
    private val duration: Long
) {

    private var animation: ValueAnimator? = null

    fun start(onChange: ((ValueAnimator) -> Unit)) {
        animation = ValueAnimator.ofInt(startHeight, endHeight)
        animation!!.duration = duration
        animation!!.interpolator = AccelerateDecelerateInterpolator()
        animation!!.addUpdateListener {
            onChange(it)
        }
        animation!!.start()
    }

    fun clear(): ExpandAnimation {
        animation?.cancel()
        return this
    }
}