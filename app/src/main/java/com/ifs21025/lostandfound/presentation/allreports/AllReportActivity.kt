package com.ifs21025.lostandfound.presentation.allreports

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifs21025.lostandfound.R
import com.ifs21025.lostandfound.adapter.LostFoundAdapter
import com.ifs21025.lostandfound.data.remote.MyResult
import com.ifs21025.lostandfound.data.remote.response.DelcomLostFoundsResponse
import com.ifs21025.lostandfound.data.remote.response.LostFoundsItemResponse
import com.ifs21025.lostandfound.databinding.ActivityAllReportBinding
import com.ifs21025.lostandfound.presentation.ViewModelFactory
import com.ifs21025.lostandfound.presentation.login.LoginActivity
import com.ifs21025.lostandfound.presentation.lostandfound.LostFoundDetailActivity
import com.ifs21025.lostandfound.presentation.lostandfound.LostFoundManageActivity
import com.ifs21025.lostandfound.presentation.lostandfound.LostFoundsDetailActivity
import com.ifs21025.lostandfound.presentation.main.MainActivity
import com.ifs21025.lostandfound.presentation.main.MainViewModel
import com.ifs21025.lostandfound.presentation.profile.ProfileActivity

class AllReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAllReportBinding
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == LostFoundManageActivity.RESULT_CODE) {
            recreate()
        }

        if (result.resultCode == LostFoundDetailActivity.RESULT_CODE) {
            result.data?.let {
                val isChanged = it.getBooleanExtra(
                    LostFoundDetailActivity.KEY_IS_CHANGED,
                    false
                )

                if (isChanged) {
                    recreate()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
    }

    private fun setupView() {
        showComponentNotEmpty(false)
        showEmptyError(false)
        showLoading(true)

        binding.appbarMain.overflowIcon =
            ContextCompat
                .getDrawable(this, R.drawable.ic_more_vert_24)

        observeGetLostFounds()
    }

    private fun setupAction() {
        binding.appbarMain.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.mainMenuProfile -> {
                    openProfileActivity()
                    true
                }

                R.id.mainMenuLogout -> {
                    viewModel.logout()
                    openLoginActivity()
                    true
                }

                R.id.mainMenuMyReport -> {
                    openMainActivity()
                    true
                }

                else -> false
            }
        }

        binding.fabMainAddLostFound.setOnClickListener {
            openAddLostFoundActivity()
        }

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                openLoginActivity()
            } else {
                observeGetLostFounds()
            }
        }
    }

    private fun observeGetLostFounds() {
        viewModel.getAllLostFounds().observe(this) { result ->
            if (result != null) {
                when (result) {
                    is MyResult.Loading -> {
                        showLoading(true)
                    }

                    is MyResult.Success -> {
                        showLoading(false)
                        loadLostFoundsToLayout(result.data)
                    }

                    is MyResult.Error -> {
                        showLoading(false)
                        showEmptyError(true)
                    }
                }
            }
        }
    }

    private fun loadLostFoundsToLayout(response: DelcomLostFoundsResponse) {
        val lostFounds = response.data.lostFounds
        val layoutManager = LinearLayoutManager(this)
        binding.rvMainLostFounds.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(
            this,
            layoutManager.orientation
        )
        binding.rvMainLostFounds.addItemDecoration(itemDecoration)

        if (lostFounds.isEmpty()) {
            showEmptyError(true)
            binding.rvMainLostFounds.adapter = null
        } else {
            showComponentNotEmpty(true)
            showEmptyError(false)

            val adapter = LostFoundAdapter()
            adapter.submitOriginalList(lostFounds)
            binding.rvMainLostFounds.adapter = adapter
            adapter.setIsAll()
            adapter.setOnItemClickCallback(object : LostFoundAdapter.OnItemClickCallback {
                override fun onCheckedChangeListener(
                    lostFound: LostFoundsItemResponse,
                    isChecked: Boolean
                ) {

                }

                override fun onClickDetailListener(lostFoundId: Int) {
                    val intent = Intent(
                        this@AllReportActivity,
                        LostFoundsDetailActivity::class.java
                    )
                    intent.putExtra(LostFoundsDetailActivity.KEY_TODO_ID, lostFoundId)
                    launcher.launch(intent)
                }
            })

            binding.svMain.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        adapter.filter(newText)
                        binding.rvMainLostFounds.layoutManager?.scrollToPosition(0)
                        return true
                    }
                })
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbMain.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }

    private fun openProfileActivity() {
        val intent = Intent(applicationContext, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun showComponentNotEmpty(status: Boolean) {
        binding.svMain.visibility =
            if (status) View.VISIBLE else View.GONE

        binding.rvMainLostFounds.visibility =
            if (status) View.VISIBLE else View.GONE
    }

    private fun showEmptyError(isError: Boolean) {
        binding.tvMainEmptyError.visibility =
            if (isError) View.VISIBLE else View.GONE
    }

    private fun openLoginActivity() {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun openAddLostFoundActivity() {
        val intent = Intent(
            this@AllReportActivity,
            LostFoundManageActivity::class.java
        )
        intent.putExtra(LostFoundManageActivity.KEY_IS_ADD, true)
        launcher.launch(intent)
    }

    private fun openMainActivity(){
        val intent = Intent(
            this@AllReportActivity,
            MainActivity::class.java
        )
        launcher.launch(intent)
    }
}