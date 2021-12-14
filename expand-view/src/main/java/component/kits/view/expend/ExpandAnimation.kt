package component.kits.view.expend

import android.animation.Animator
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
        animation!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }
        })
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