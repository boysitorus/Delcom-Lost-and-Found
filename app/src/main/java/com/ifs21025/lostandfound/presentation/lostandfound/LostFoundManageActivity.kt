package com.ifs21025.lostandfound.presentation.lostandfound

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ifs21025.lostandfound.R
import com.ifs21025.lostandfound.data.model.DelcomLostandFound
import com.ifs21025.lostandfound.data.remote.MyResult
import com.ifs21025.lostandfound.databinding.ActivityLostFoundManageBinding
import com.ifs21025.lostandfound.helper.Utils.Companion.observeOnce
import com.ifs21025.lostandfound.presentation.ViewModelFactory

class LostFoundManageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostFoundManageBinding
    private val viewModel by viewModels<LostFoundViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var adapterStatusItems: ArrayAdapter<String>
    val status = arrayOf("lost", "found")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostFoundManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        autoCompleteTextView = binding.acStatus
        adapterStatusItems = ArrayAdapter<String>(this, R.layout.list_status, status)

        autoCompleteTextView.setAdapter(adapterStatusItems)

        setupView()
        setupAction()
    }

    private fun setupView() {
        showLoading(false)
    }

    private fun setupAction() {
        val isAddLostFound = intent.getBooleanExtra(KEY_IS_ADD, true)
        if (isAddLostFound) {
            manageAddLostFound()
        } else {

            val delcomLostFound = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    intent.getParcelableExtra(KEY_LOST_FOUND, DelcomLostandFound::class.java)
                }

                else -> {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<DelcomLostandFound>(KEY_LOST_FOUND)
                }
            }

            if (delcomLostFound == null) {
                finishAfterTransition()
                return
            }

            manageEditLostFound(delcomLostFound)
        }

        binding.appbarLostFoundManage.setNavigationOnClickListener {
            finishAfterTransition()
        }
    }

    private fun manageAddLostFound() {

        binding.apply {
            appbarLostFoundManage.title = "Tambah Lost & Found"

            btnLostFoundManageSave.setOnClickListener {
                val title = etLostFoundManageTitle.text.toString()
                val description = etLostFoundManageDesc.text.toString()
                var status = autoCompleteTextView.text.toString()

                if (title.isEmpty() || description.isEmpty() || status == "" ) {
                    AlertDialog.Builder(this@LostFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage("Tidak boleh ada data yang kosong!")
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    return@setOnClickListener
                }

                observePostLostFound(title, description, status)
            }
        }
    }

    private fun observePostLostFound(title: String, description: String, status: String) {
        viewModel.postLostFound(title, description, status).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }

                is MyResult.Success -> {
                    showLoading(false)

                    val resultIntent = Intent()
                    setResult(RESULT_CODE, resultIntent)
                    finishAfterTransition()
                }

                is MyResult.Error -> {
                    AlertDialog.Builder(this@LostFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    showLoading(false)
                }
            }
        }
    }

    private fun manageEditLostFound(lostFound: DelcomLostandFound) {
        binding.apply {
            appbarLostFoundManage.title = "Edit Lost and Found"

            etLostFoundManageTitle.setText(lostFound.title)
            etLostFoundManageDesc.setText(lostFound.description)

            btnLostFoundManageSave.setOnClickListener {
                val title = etLostFoundManageTitle.text.toString()
                val description = etLostFoundManageDesc.text.toString()
                val status = autoCompleteTextView.text.toString() //tambahin di layout

                if (title.isEmpty() || description.isEmpty() || status == "Select Item") {
                    AlertDialog.Builder(this@LostFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage("Tidak boleh ada data yang kosong!")
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    return@setOnClickListener
                }

                observePutTodo(lostFound.id, title, description, status, lostFound.isCompleted)
            }
        }
    }

    private fun observePutTodo(
        lostFoundId: Int,
        title: String,
        description: String,
        status: String,
        isCompleted: Boolean
    ) {
        viewModel.putLostFound(
            lostFoundId,
            title,
            description,
            status,
            isCompleted
        ).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }

                is MyResult.Success -> {
                    showLoading(false)
                    val resultIntent = Intent()
                    setResult(RESULT_CODE, resultIntent)
                    finishAfterTransition()
                }

                is MyResult.Error -> {
                    AlertDialog.Builder(this@LostFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLostFoundManage.visibility =
            if (isLoading) View.VISIBLE else View.GONE

        binding.btnLostFoundManageSave.isActivated = !isLoading

        binding.btnLostFoundManageSave.text =
            if (isLoading) "" else "Simpan"
    }

    companion object {
        const val KEY_IS_ADD = "is_add"
        const val KEY_LOST_FOUND = "lostfound"
        const val RESULT_CODE = 1002
    }
} 