package component.kits.view.expand

import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import component.kits.view.ViewKits
import java.math.BigDecimal
import kotlin.math.min

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

    // 缓存第一次测量的真实数据
    // 后续改变layoutParam后对应的部分数据会发生变化
    private var sourceTextViewLineCount: Int = -1
    private var sourceLineSpacingExtra: Float = -1f
    private var sourceLineSpacingMultiplier: Float = -1f

    private var sourceMeasureWidth: Int = -1
    private var sourceMeasureHeight = -1
    private var sourceTotalHeight = -1

    var realTotalHeight: Int = -1
        private set
    var collapseHeight: Int = -1
        private set

    private lateinit var copyTextView: TextView

    init {
        sourceTextView.post {
            copyTextView = TextView(sourceTextView.context)

            val spacingExtra = if (sourceLineSpacingExtra == -1f) {
                sourceLineSpacingExtra = sourceTextView.lineSpacingExtra
                sourceLineSpacingExtra
            } else {
                sourceLineSpacingExtra
            }

            val spacingMulti = if (sourceLineSpacingMultiplier == -1f) {
                sourceLineSpacingMultiplier = sourceTextView.lineSpacingMultiplier
                sourceLineSpacingMultiplier
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
            copyTextView.setPadding(
                sourceTextView.paddingLeft,
                sourceTextView.paddingTop,
                sourceTextView.paddingRight,
                sourceTextView.paddingBottom
            )

            // 设置最大行数
            copyTextView.setLines(collapseMaxLine)

            val width = sourceTextView.width
            val lineSpacingMultiplierHeight = collapseHeight * lineSpacingMultiplier
            val height = ViewKits.measureTextViewHeight(sourceTextView, collapseMaxLine)
            val measureCollapseHeight =
                height + BigDecimal.valueOf(lineSpacingMultiplierHeight.toDouble())
                    .setScale(0, BigDecimal.ROUND_UP)
                    .intValueExact()

            val widthMeasureSpec = if (sourceMeasureWidth == -1) {
                sourceMeasureWidth =
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
                sourceMeasureWidth
            } else {
                sourceMeasureWidth
            }

            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                measureCollapseHeight,
                View.MeasureSpec.AT_MOST
            )

            if (sourceMeasureHeight == -1) {
                sourceMeasureHeight = sourceTextView.measuredHeight
            }

            copyTextView.layoutParams = sourceTextView.layoutParams
            copyTextView.measure(widthMeasureSpec, heightMeasureSpec)

            val measureTotalSpaceHeight = sourceTextViewLineCount * lineSpacingMultiplier

            realTotalHeight = if (sourceTotalHeight == -1) {
                sourceTotalHeight = ViewKits.measureTextViewHeight(
                    sourceTextView,
                    sourceTextViewLineCount
                ) + BigDecimal.valueOf(
                    measureTotalSpaceHeight.toDouble()
                ).setScale(0, BigDecimal.ROUND_UP)
                    .intValueExact()
                sourceTotalHeight
            } else {
                sourceTotalHeight
            }

            collapseHeight = copyTextView.measuredHeight

            val calculatorHeight = ViewKits.measureTextViewHeight(
                sourceTextView,
                min(collapseMaxLine, sourceTextViewLineCount)            // 采用八行以内的高度对比
            ) + BigDecimal.valueOf(
                (min(collapseMaxLine, sourceTextViewLineCount) * lineSpacingMultiplier).toDouble()
            ).setScale(0, BigDecimal.ROUND_UP)
                .intValueExact()

            val params = sourceTextView.layoutParams
            params.height = calculatorHeight
            sourceTextView.layoutParams = params

            // 回调用于更改最小盖度
            ViewKits.log("真实的总高度(运算的):$realTotalHeight, 其中间距总高度(含行数):${measureTotalSpaceHeight}, 行数:${sourceTextViewLineCount}")
            ViewKits.log("折叠的高度(测量的):$collapseHeight, 运算的总高度(指定source为最大行数):$calculatorHeight")
            // true:隐藏bottomLayout
            onInit?.invoke(realTotalHeight <= calculatorHeight)
        }
    }
}