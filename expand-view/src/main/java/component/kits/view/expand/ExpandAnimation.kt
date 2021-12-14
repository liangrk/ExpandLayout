package component.kits.view.expand

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

    private var isAnimationFlag = false
    private var animation: ValueAnimator? = null

    /**
     * 执行动画
     * @param onStart 动画开始前. Boolean->是否展开意图动画开始
     * @param onEnd 动画结束. Boolean->是否展开意图动画结束
     * @param onChange 状态更改.
     * @return 是否消费此次事件. true: 消费事件,执行动画 | false 动画执行过程中, 不执行,不消费
     */
    fun executable(
        onStart: ((Boolean) -> Unit)?=null,
        onEnd: ((Boolean) -> Unit)?=null,
        onChange: ((ValueAnimator) -> Unit)
    ): Boolean {
        if (isRunning()) return false

        val expandIntent = startHeight > endHeight
        animation = ValueAnimator.ofInt(startHeight, endHeight)
        animation!!.duration = duration
        animation!!.interpolator = AccelerateDecelerateInterpolator()
        animation!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator?) {
                isAnimationFlag = true
                onStart?.invoke(expandIntent)
            }

            override fun onAnimationEnd(animator: Animator?) {
                isAnimationFlag = false
                onEnd?.invoke(expandIntent)
            }

            override fun onAnimationCancel(animator: Animator?) {
                isAnimationFlag = false
                onEnd?.invoke(expandIntent)
            }

            override fun onAnimationRepeat(animator: Animator?) {
            }
        })
        animation!!.addUpdateListener {
            onChange(it)
        }
        animation!!.start()
        return true
    }

    private fun isRunning(): Boolean {
        return isAnimationFlag || animation?.isRunning == true
    }
}