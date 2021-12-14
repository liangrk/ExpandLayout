package component.kits.view.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.JsonUtils
import com.blankj.utilcode.util.ResourceUtils
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
    }
}