## 一个折叠解耦的库

本库由来: 之前采用github中其他的折叠textview时, 遇到了一些问题:

1. textview的定义在其内部, 样式定义受到了极大的限制;
2. 当折叠布局在滑动布局中时, text足够多时, 动画执行存在问题.

针对上面的问题加上自身功能需求,便有了本库

> how to use

1. Add it in your root build.gradle at the end of repositories:

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

kotlin-gradle-plugin

```

2. Add the dependency
> 当前最新版本为
>
> [![](https://jitpack.io/v/liangrk/ExpandLayout.svg)](https://jitpack.io/#liangrk/ExpandLayout)

```
dependencies {
    implementation "com.github.liangrk:ExpandLayout:${version}"         // 当前版本2.2.0
}
```

> 关于本库

对于被折叠的textview, 样式完全由外部定义, 写在提供的Layout中, 把其id传入父布局即可底部view定义在一个新的xml中, 通过引用添加到父布局,
调用者可以监听内部动画的执行来同步执行底部view的动画效果

库中提供了两种折叠的Layout可供使用:

### ExpandLinearLayout

> 一个线性的折叠布局, 主要将其放在textview底部

### ExpandFrameLayout

> 适用于底部view具有渐变或其他需求需要覆盖到textview上的控件

### layout-attr

|                         属性名 | 说明                                          |
| -----------------------------: | :-------------------------------------------- |
|             expand_textView_id | 被折叠的textview id                           |
|           expand_bottom_layout | 底部view的布局引用                            |
|       expand_collapse_max_line | 折叠后可显示的最大行数                        |
|           expand_anim_duration | 折叠动画时长(单位ms)                          |
|          expand_text_clickable | 被折叠的textview是否需要响应点击事件          |
|    expand_bottom_expand_height | 展开后底部view的高度(仅支持ExpandFrameLayout) |
| expand_line_spacing_multiplier | 兼容textView设置行高后测量不准确              |

### 用法

> 具体可参考本项目中的 LinearTestAct.kt/activity_linear_test.xml

```
<!-- 在需要用的layout中定义 textview的样式完全由调用者实现 对于bottom-view 通过layout引用传递即可 -->
<component.kits.view.expand.ExpandLinearLayout
    android:id="@+id/expand_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:expand_anim_duration="200"
    app:expand_bottom_layout="@layout/expand_bottom"
    app:expand_collapse_max_line="5"
    app:expand_textView_id="@id/ex_haha"
    app:expand_text_clickable="false">

    <androidx.appcompat.widget.AppCompatTextView
        android:id="@+id/ex_haha"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

</component.kits.view.expand.ExpandLinearLayout>
```

> expand_bottom的布局为:

```
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:background="#3c3c3c"
    android:layout_height="100dp">

    <androidx.appcompat.widget.AppCompatTextView
        android:id="@+id/tv_tips"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="点我点我"
        android:textSize="20dp"
        android:textColor="@color/white"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

> 代码中监听展开/收起状态
> onExpand和onCollapse函数中的view均为bottom-view. 调用者可在其回调中通过此view去执行该布局的动画
> 库中不作bottom-view的动画处理. 这俩函数目前都是在动画开始前回调
> 对于时长可以通过expandLayout.expandDuration获取方便设定同一时间的动画. 但目前代码暂不允许动态设置时长

```
val expandLayout: ExpandLinearLayout = findViewById(R.id.expand_layout)
expandLayout.setText(charSequence = text, onExpand = { view->
    // 已经展开 view是xml中传入的bottomLayout
}, onCollapse = { view->
    // 已经折叠 view是xml中传入的bottomLayout
}, onReady = {
    // 渲染完成
}, arrowClick = {
    true        // 是否允许点击 可以在此做业务逻辑判断
}, overrideMeasure = false)         // 是否需要重新测量, 默认false, 只有当一开始view是gone的情况下 切换为visibility测量不准确的时候应用
```

> 对于其他api

|       函数名        |             说明             |
| :-----------------: | :--------------------------: |
| enableTextClickable | 设置textview是否响应点击事件 |
|   getExpandHeight   |       获取展开后的高度       |
|  getCollapseHeight  |       获取收起后的高度       |

> 关于AGP7.0发布jetpack

[感谢这位仁兄的分享](https://www.jianshu.com/p/c0645390c070)

> 最后感谢以下库提供的思路及灵感

[Manabu-GT](https://github.com/Manabu-GT/ExpandableTextView)  
[ExpandTextView](https://github.com/hymanme/ExpandTextView)
