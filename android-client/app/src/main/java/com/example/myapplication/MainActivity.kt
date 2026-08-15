package com.example.myapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // 创建3个Fragment
    private var mHomeFragment: HomeFragment? = null
    private var mAlertFragment: AlertFragment? = null
    private var mPersonalInformationFragment: PersonalInformationFragment? = null

    private lateinit var mainBottomNv: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 设置窗口Insets处理
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 初始化控件
        mainBottomNv = findViewById(R.id.main_bottom_nv)

        // main_bottom_nv tab点击切换
        mainBottomNv.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> selectFragment(0)
                R.id.nav_alarm -> selectFragment(1)
                R.id.nav_mine -> selectFragment(2)
            }
            true
        }

        // 默认首页选中
        selectFragment(0)
    }

    private fun selectFragment(position: Int) {
        // 获取fragmentManager管理器
        val transaction = supportFragmentManager.beginTransaction()
        hideFragment(transaction)

        when (position) {
            0 -> {
                if (mHomeFragment == null) {
                    mHomeFragment = HomeFragment()
                    transaction.add(R.id.content, mHomeFragment!!)
                } else {
                    transaction.show(mHomeFragment!!)
                }
            }
            1 -> {
                if (mAlertFragment == null) {
                    mAlertFragment = AlertFragment()
                    transaction.add(R.id.content, mAlertFragment!!)
                } else {
                    transaction.show(mAlertFragment!!)
                }
            }
            2 -> {
                if (mPersonalInformationFragment == null) {
                    mPersonalInformationFragment = PersonalInformationFragment()
                    transaction.add(R.id.content, mPersonalInformationFragment!!)
                } else {
                    transaction.show(mPersonalInformationFragment!!)
                }
            }

        }

        // 提交事务
        transaction.commit()
    }

    private fun hideFragment(transaction: FragmentTransaction) {
        mHomeFragment?.let { transaction.hide(it) }
        mAlertFragment?.let { transaction.hide(it) }
        mPersonalInformationFragment?.let { transaction.hide(it) }

    }
}