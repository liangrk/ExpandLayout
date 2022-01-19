package component.kits.view.expand

/**
 * @author : wing-hong Create by 2021/12/17 18:45
 */
internal interface IExpandEffective {

    /**
     * 设置是否允许底部view的点击
     */
    fun senEnableBottomClick(enable: Boolean)

    /**
     * 设置展开
     */
    fun setExpand()

    /**
     * 设置收起
     */
    fun setCollapse()

    /**
     * 返回展开的高度
     */
    fun getExpandHeight(): Int

    /**
     * 返回收起的高度
     */
    fun getCollapseHeight(): Int

    /**
     * 设置折叠的文案
     * @param onExpand 展开动画开始前回调
     * @param onCollapse 收起动画开始前回调
     * @param onReady expandLayout及bottom-view inflate后回调, 用于更新bottom-view
     * @param arrowClick 是否允许点击. 函数为空或者返回值为true时 允许点击 否则不响应点击
     * @param overrideMeasure 是否重新测量 适用于一开始view-gone的情况
     */
    fun setText(
        charSequence: CharSequence,
        onExpand: ExpandFunction? = null,
        onCollapse: ExpandFunction? = null,
        onReady: ExpandFunction? = null,
        arrowClick: (() -> Boolean)? = null,
        overrideMeasure: Boolean = false
    )
}