package component.kits.view.expand

import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import component.kits.view.ViewKits
import java.math.BigDecimal

/**
 * @author : wing-hong Create by 2021/12/14 17:46
 * @param sourceTextView 原来的textView
 *
 * 如果直接在原textView上设置属性来测量高度的话需要执行measure多次, 且通过measure来更改高度会带来动画闪烁问题.
 * 库内采用一个新的textView. copy原textView的相应属性, 通过设定copy的行数来确定最小高度
 * 增加此委托类用于获取最大及最小高度.
 */
class ExpandMeasureDelegate(
    private val sourceTextView: TextView,
    private var collapseMaxLine: Int,
    private var lineSpacingMultiplier: Float,
    private var onInit: ((Boolean) -> Unit)? = null
) {

    var realTotalHeight: Int = -1
        private set
    var collapseHeight: Int = -1
        private set

    private lateinit var copyTextView: TextView

    init {
        sourceTextView.post {
            copyTextView = TextView(sourceTextView.context)

            val spacingExtra = if (sourceLineSpacingExtra == -1f) {
                sourceTextView.lineSpacingExtra.also {
                    sourceLineSpacingExtra = it
                }
            } else {
                sourceLineSpacingExtra
            }

            val spacingMulti = if (sourceLineSpacingMultiplier == -1f) {
                sourceTextView.lineSpacingMultiplier.also {
                    sourceLineSpacingMultiplier = it
                }
            } else {
                sourceLineSpacingMultiplier
            }

            if (sourceTextViewLineCount == -1) {
                sourceTextViewLineCount = sourceTextView.lineCount
            }

            val textSize = sourceTextView.textSize
            val textSizeUnit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                sourceTextView.textSizeUnit
            } else {
                TypedValue.COMPLEX_UNIT_PX
            }

            copyTextView.setLineSpacing(spacingExtra, spacingMulti)
            copyTextView.setTextSize(textSizeUnit, textSize)
            copyTextView.setLines(collapseMaxLine)

            val width = sourceTextView.width
            val lineSpacingMultiplierHeight = collapseHeight * lineSpacingMultiplier
            val height = ViewKits.measureTextViewHeight(sourceTextView, collapseMaxLine)
            val measureCollapseHeight =
                height + BigDecimal.valueOf(lineSpacingMultiplierHeight.toDouble())
                    .setScale(0, BigDecimal.ROUND_UP)
                    .intValueExact()

            val widthMeasureSpec = if (sourceMeasureWidth == -1) {
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
                    .also {
                        sourceMeasureWidth = it
                    }
            } else {
                sourceMeasureWidth
            }

            val heightMeasureSpec = if (sourceMeasureHeight == -1) {
                View.MeasureSpec.makeMeasureSpec(measureCollapseHeight, View.MeasureSpec.AT_MOST)
                    .also {
                        sourceMeasureHeight = it
                    }
            } else {
                sourceMeasureHeight
            }

            copyTextView.layoutParams = sourceTextView.layoutParams
            copyTextView.measure(widthMeasureSpec, heightMeasureSpec)
            copyTextView.measuredHeight

            val measureTotalSpaceHeight = sourceTextViewLineCount * lineSpacingMultiplier

            realTotalHeight = if (sourceTotalHeight == -1) {
                ViewKits.measureTextViewHeight(
                    sourceTextView,
                    sourceTextViewLineCount
                ) + BigDecimal.valueOf(
                    measureTotalSpaceHeight.toDouble()
                ).setScale(0, BigDecimal.ROUND_UP)
                    .intValueExact()
                    .also {
                        sourceTotalHeight = it
                    }
            } else {
                sourceTotalHeight
            }

            collapseHeight = copyTextView.measuredHeight

            val params = sourceTextView.layoutParams
            params.height = collapseHeight
            sourceTextView.layoutParams = params

            // 回调用于更改最小盖度
            onInit?.invoke(realTotalHeight <= heightMeasureSpec)
        }
    }

    companion object {
        var sourceTextViewLineCount: Int = -1
        var sourceLineSpacingExtra: Float = -1f
        var sourceLineSpacingMultiplier: Float = -1f

        var sourceMeasureWidth: Int = -1
        var sourceMeasureHeight = -1

        var sourceTotalHeight = -1
    }
}