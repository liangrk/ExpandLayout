package component.kits.view.demo

import android.os.Bundle
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.JsonUtils
import com.blankj.utilcode.util.ResourceUtils
import component.kits.view.expand.ExpandFrameLayout
import component.kits.view.expand.ExpandLinearLayout

class LinearTestAct : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linear_test)

        val json = ResourceUtils.readAssets2String("netapi.json")
        val text = JsonUtils.getString(json, "key")

        val expandLayout: ExpandLinearLayout = findViewById(R.id.expand_layout)
        expandLayout.setText(charSequence = text, onExpand = {
            println("展开!")
        }, onCollapse = {
            println("收起!")
        })

        val alphaExpandLayout: ExpandFrameLayout = findViewById(R.id.fl_expand_layout)
        alphaExpandLayout.setText(charSequence = text, onExpand = {
            println("展开!")
            val textView = it?.findViewById<TextView>(R.id.tv_frame_btn)
            textView?.setText("ahahahahah")
            textView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, 10f)
        }, onCollapse = {
            println("收起!")
            val textView = it?.findViewById<TextView>(R.id.tv_frame_btn)
            textView?.setText("shou~~~")
            textView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20f)
        })
    }
}