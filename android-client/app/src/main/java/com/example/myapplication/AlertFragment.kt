// AlertFragment.kt
package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class AlertFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 这里加载AlertFragment的布局
        return inflater.inflate(R.layout.fragment_alert, container, false)
    }

    // 可以添加其他方法处理Fragment逻辑
}