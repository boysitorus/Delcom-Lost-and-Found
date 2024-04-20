package com.ifs21025.lostandfound.presentation.lostandfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ifs21025.lostandfound.data.remote.MyResult
import com.ifs21025.lostandfound.data.remote.response.LostFoundResponse
import com.ifs21025.lostandfound.databinding.ActivityLostFoundsDetailBinding
import com.ifs21025.lostandfound.helper.Utils.Companion.observeOnce
import com.ifs21025.lostandfound.presentation.ViewModelFactory

class LostFoundsDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostFoundsDetailBinding
    private val viewModel by viewModels<LostFoundViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private var isChanged: Boolean = false

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == LostFoundManageActivity.RESULT_CODE) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostFoundsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
    }

    private fun setupView() {
        showComponent(false)
        showLoading(false)
    }

    private fun setupAction() {
        val lostFoundId = intent.getIntExtra(KEY_TODO_ID, 0)
        if (lostFoundId == 0) {
            finish()
            return
        }

        observeGetLostFound(lostFoundId)

        binding.appbarLostFoundDetail.setNavigationOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(KEY_IS_CHANGED, isChanged)
            setResult(RESULT_CODE, resultIntent)
            finishAfterTransition()
        }
    }

    private fun observeGetLostFound(lostFoundId: Int) {
        viewModel.getLostFound(lostFoundId).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }

                is MyResult.Success -> {
                    showLoading(false)
                    loadLostFound(result.data.data.lostFound)
                }

                is MyResult.Error -> {
                    Toast.makeText(
                        this@LostFoundsDetailActivity,
                        result.error,
                        Toast.LENGTH_SHORT
                    ).show()
                    showLoading(false)
                    finishAfterTransition()
                }
            }
        }
    }

    private fun loadLostFound(lostFound: LostFoundResponse) {
        showComponent(true)

        binding.apply {
            tvLostFoundDetailTitle.text = lostFound.title
            tvLostFoundDetailDate.text = "Dibuat pada: ${lostFound.createdAt}"
            tvLostFoundDetailDesc.text = lostFound.description
            tvLostFoundDetailStatus.text = "Status: ${lostFound.status}"

            cbLostFoundDetailIsCompleted.isChecked = lostFound.isCompleted == 1

            cbLostFoundDetailIsCompleted.isEnabled = false;
        }
    }



    private fun showLoading(isLoading: Boolean) {
        binding.pbLostFoundDetail.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showComponent(status: Boolean) {
        binding.llLostFoundDetail.visibility =
            if (status) View.VISIBLE else View.GONE
    }

    companion object {
        const val KEY_TODO_ID = "lostFound_id"
        const val KEY_IS_CHANGED = "is_changed"
        const val RESULT_CODE = 1001
    }
}