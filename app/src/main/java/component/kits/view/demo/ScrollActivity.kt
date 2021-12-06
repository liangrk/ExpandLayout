package component.kits.view.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseBinderAdapter
import com.chad.library.adapter.base.binder.QuickDataBindingItemBinder
import com.google.android.material.appbar.AppBarLayout
import component.kits.view.demo.databinding.ActivityScrollBinding
import component.kits.view.demo.databinding.ItemTestBinderBinding
import kotlin.math.abs
import kotlin.math.pow

class ScrollActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScrollBinding

    private var orientation = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScrollBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val testAdapter = ItemListAdapter()
        binding.rvList.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
            adapter = testAdapter
        }

        val list = mutableListOf<String>()
        repeat(100) {
            list.add("data-$it")
        }

        testAdapter.setList(list)

        binding.appBar.addOnOffsetChangedListener(object:AppBarLayout.OnOffsetChangedListener{
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
//                val multipleNum = (appBarLayout?.totalScrollRange?:0) / 10
//                println("倍数:$multipleNum, total:${appBarLayout?.totalScrollRange}")
//                if (verticalOffset == 0) return
//                if (abs(verticalOffset) % multipleNum == 0) {
//                    binding.titleBar.alpha -= 0.1f
//                    println("改变透明度的值:$verticalOffset, 倍数:${multipleNum}")
//                }
//                println("透明度:${binding.titleBar.alpha}")

                val totalRange = appBarLayout.totalScrollRange
                val offset = totalRange - abs(verticalOffset)

                if (orientation == 0 || orientation == verticalOffset) {
                    orientation = verticalOffset
                    return
                }
                if (abs(orientation) > verticalOffset) {
                    // 下滑

                } else {
                    // 上滑

                }
                if (offset == 0) {
                    binding.titleBar.alpha = 0f
                }
                orientation = verticalOffset
                println("verticalOffset:${verticalOffset}, appBarLayout?.totalScrollRange${appBarLayout.totalScrollRange}")
            }
        })
    }
}

class ItemListAdapter : BaseBinderAdapter() {
    init {
        addItemBinder(ItemBinder())
    }
}

class ItemBinder : QuickDataBindingItemBinder<String, ItemTestBinderBinding>() {
    override fun convert(holder: BinderDataBindingHolder<ItemTestBinderBinding>, data: String) {
        val binding = holder.dataBinding
        binding.data = data
    }

    override fun onCreateDataBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemTestBinderBinding {
        return DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_test_binder,
            parent,
            false
        )
    }
}