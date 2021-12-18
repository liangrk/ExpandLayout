package component.kits.view.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.JsonUtils
import com.blankj.utilcode.util.ResourceUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.chad.library.adapter.base.binder.QuickDataBindingItemBinder
import component.kits.view.demo.databinding.ActivityScrollBinding
import component.kits.view.demo.databinding.ItemListScrollBinding
import component.kits.view.demo.databinding.ItemListSeatBinding

class ScrollActivity : AppCompatActivity() {

    private val adapter by lazy { RvAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityScrollBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvList.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
            adapter = this@ScrollActivity.adapter
        }

        val json = ResourceUtils.readAssets2String("netapi.json")
        val text = JsonUtils.getString(json, "key")
        val list = mutableListOf<Any>()
        list.add(1111)
        list.add(2222)
        list.add(3333)
        list.add(text)
        repeat(100) {
            list.add(it)
        }
        adapter.setList(list)
    }
}

class RvAdapter : BaseBinderAdapter() {
    init {
        addItemBinder(RvTestBinding())
        addItemBinder(SeatBing())
    }
}

class RvTestBinding : QuickDataBindingItemBinder<String, ItemListScrollBinding>() {

    override fun convert(holder: BinderDataBindingHolder<ItemListScrollBinding>, data: String) {
        val binding = holder.dataBinding
        binding.flExpandLayout.setText(data, onExpand = { bottom ->
            // 展开
            bottom?.findViewById<TextView>(R.id.tv_frame_btn)
                ?.text = "点我收起"
        }, onCollapse = { bottom ->
            bottom?.findViewById<TextView>(R.id.tv_frame_btn)
                ?.text = "点我展开"
        }, onReady = { bottom ->
            bottom?.findViewById<TextView>(R.id.tv_frame_btn)
                ?.text = "点击展开"
        }, arrowClick = {
            true
        })
    }

    override fun onCreateDataBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemListScrollBinding {
        return DataBindingUtil.inflate(
            layoutInflater, R.layout.item_list_scroll, parent, false
        )
    }
}

class SeatBing : QuickDataBindingItemBinder<Int, ItemListSeatBinding>() {
    override fun convert(holder: BinderDataBindingHolder<ItemListSeatBinding>, data: Int) {
        val binding = holder.dataBinding
        binding.data = data
    }

    override fun onCreateDataBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemListSeatBinding {
        return DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_list_seat,
            parent,
            false
        )
    }
}