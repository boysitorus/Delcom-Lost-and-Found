package com.ifs21025.lostandfound.presentation.lostandfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifs21025.lostandfound.R
import com.ifs21025.lostandfound.adapter.LostFoundAdapter
import com.ifs21025.lostandfound.data.local.entity.DelcomLostFoundEntity
import com.ifs21025.lostandfound.data.remote.response.LostFoundsItemResponse
import com.ifs21025.lostandfound.databinding.ActivityLostFoundFavoriteBinding
import com.ifs21025.lostandfound.helper.Utils.Companion.entitiesToResponses
import com.ifs21025.lostandfound.presentation.ViewModelFactory
import com.ifs21025.lostandfound.presentation.allreports.AllReportActivity
import com.ifs21025.lostandfound.presentation.login.LoginActivity
import com.ifs21025.lostandfound.presentation.main.MainActivity
import com.ifs21025.lostandfound.presentation.main.MainViewModel
import com.ifs21025.lostandfound.presentation.profile.ProfileActivity

class LostFoundFavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostFoundFavoriteBinding
    private val viewModel by viewModels<LostFoundViewModel> {
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
        binding = ActivityLostFoundFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
    }

    private fun setupAction() {
        binding.appbarMain.setNavigationOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(LostFoundDetailActivity.KEY_IS_CHANGED, true)
            setResult(LostFoundDetailActivity.RESULT_CODE, resultIntent)
            finishAfterTransition()
        }

        binding.appbarMain.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.mainMenuProfile -> {
                    openProfileActivity()
                    true
                }

                R.id.mainMenuLogout -> {
                    val vmMain by viewModels<MainViewModel> {
                        ViewModelFactory.getInstance(this)
                    }
                    vmMain.logout()
                    openLoginActivity()
                    true
                }

                R.id.mainMenuAllReport -> {
                    openAllLostFoundActivity()
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

    private fun observeGetLostFounds() {
        viewModel.getLocalLostFounds().observe(this) { lostFounds ->
            loadLostFoundsToLayout(lostFounds)
        }
    }

    private fun loadLostFoundsToLayout(lostFounds: List<DelcomLostFoundEntity>?) {
        showLoading(false)

        val layoutManager = LinearLayoutManager(this)
        binding.rvMainLostFounds.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(
            this,
            layoutManager.orientation
        )
        binding.rvMainLostFounds.addItemDecoration(itemDecoration)

        if(lostFounds.isNullOrEmpty()){
            showEmptyError(true)
            binding.rvMainLostFounds.adapter = null
        } else {
            showComponentNotEmpty(true)
            showEmptyError(false)

            val adapter = LostFoundAdapter()
            adapter.submitOriginalList(entitiesToResponses(lostFounds))
            binding.rvMainLostFounds.adapter = adapter
            adapter.setIsAll()
            adapter.setOnItemClickCallback(
                object: LostFoundAdapter.OnItemClickCallback {
                    override fun onCheckedChangeListener(
                        lostFound: LostFoundsItemResponse,
                        isChecked: Boolean
                    ) {
                        adapter.filter(binding.svMain.query.toString())
                    }

                    override fun onClickDetailListener(lostFoundId: Int) {
                        val intent = Intent(
                            this@LostFoundFavoriteActivity,
                            LostFoundsDetailActivity::class.java
                        )
                        intent.putExtra(LostFoundsDetailActivity.KEY_TODO_ID, lostFoundId)
//                        intent.putExtra(LostFoundsDetailActivity.IS_FAVORITE, true)
                        launcher.launch(intent)
                    }
                }
            )
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
            this@LostFoundFavoriteActivity,
            LostFoundManageActivity::class.java
        )
        intent.putExtra(LostFoundManageActivity.KEY_IS_ADD, true)
        launcher.launch(intent)
    }

    private fun openMainActivity(){
        val intent = Intent(
            this@LostFoundFavoriteActivity,
            MainActivity::class.java
        )
        launcher.launch(intent)
        finish()
    }

    private fun openAllLostFoundActivity(){
        val intent = Intent(
            this@LostFoundFavoriteActivity,
            AllReportActivity::class.java
        )
        launcher.launch(intent)
        finish()
    }
}