package component.kits.view.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.JsonUtils
import com.blankj.utilcode.util.ResourceUtils
import component.kits.view.expend.ExpandFrameLayout
import component.kits.view.expend.OnExpandStateListener

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val json = ResourceUtils.readAssets2String("netapi.json")
        val text = JsonUtils.getString(json,"key")

        // 遮挡类型的
        val expandFrameLayout: ExpandFrameLayout = findViewById(R.id.expand_frame_layout)
        expandFrameLayout.setExpandText(text, object : OnExpandStateListener {

            override fun onExpandStateChange(bottomLayout: View?, isExpand: Boolean) {
                if (bottomLayout != null) {
                    val tip: TextView = bottomLayout.findViewById(R.id.tv_frame_btn)
                    // 这是改变后的状态值
                    tip.text = if (isExpand) {
                        "展开"
                    } else {
                        "收起"
                    }
                }
            }
        })

        // 占坑的
        findViewById<AppCompatTextView>(R.id.tv_span2).text = text
    }
}