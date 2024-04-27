package com.ifs21025.lostandfound.presentation.lostandfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ifs21025.lostandfound.R
import com.ifs21025.lostandfound.data.local.entity.DelcomLostFoundEntity
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

    private var isFavorite: Boolean = false
    private var delcomLostFound: DelcomLostFoundEntity? = null

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

            if(lostFound.cover != null){
                ivLostFoundDetailCover.visibility = View.VISIBLE

                Glide.with(this@LostFoundsDetailActivity)
                    .load(lostFound.cover)
                    .placeholder(R.drawable.ic_image_24)
                    .into(ivLostFoundDetailCover)

            }else{
                ivLostFoundDetailCover.visibility = View.GONE
            }

            cbLostFoundDetailIsCompleted.isEnabled = false;

            viewModel.getLocalLostFound(lostFound.id).observeOnce {
                if(it != null){
                    delcomLostFound = it
                    setFavorite(true)
                }else{
                    setFavorite(false)
                }
            }

            ivLostFoundDetailActionFavorite.setOnClickListener {
                if(isFavorite){
                    setFavorite(false)
                    if(delcomLostFound != null){
                        viewModel.deleteLocalLostFound(delcomLostFound!!)
                    }

                    Toast.makeText(
                        this@LostFoundsDetailActivity,
                        "Lost Found berhasil dihapus dari daftar favorite",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    delcomLostFound = DelcomLostFoundEntity(
                        id = lostFound.id,
                        title = lostFound.title,
                        description = lostFound.description,
                        isCompleted = lostFound.isCompleted,
                        cover = null,
                        createdAt = lostFound.createdAt,
                        updatedAt = lostFound.updatedAt,
                        author = lostFound.author.name,
                        status = lostFound.status,
                        userId = lostFound.userId
                    )

                    setFavorite(true)
                    viewModel.insertLocalLostFound(delcomLostFound!!)

                    Toast.makeText(
                        this@LostFoundsDetailActivity,
                        "LostFound berhasil ditambahkan ke daftar favorite",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }

    private fun setFavorite(status: Boolean){
        isFavorite = status
        if(status){
            binding.ivLostFoundDetailActionFavorite.setImageResource(R.drawable.ic_favorite_24)
        }else{
            binding.ivLostFoundDetailActionFavorite
                .setImageResource(R.drawable.ic_favorite_border_24)
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
        const val IS_FAVORITE = "is favorite"
    }
}