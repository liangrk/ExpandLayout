package component.kits.view

import android.view.View

/**
 * @author : wing-hong Create by 2021/12/01 10:56
 */
interface OnExpandStateListener {

    /**
     * 展开状态更改回调
     *
     * @param isExpand 更改后的状态, true: 展开 | false: 收起
     */
    fun onExpandStateChange(bottomLayout: View?, isExpand: Boolean)
}