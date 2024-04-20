package com.ifs21025.lostandfound.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ifs21025.lostandfound.data.remote.response.LostFoundsItemResponse
import com.ifs21025.lostandfound.databinding.ItemRowLostAndFoundBinding

class LostFoundAdapter :
    ListAdapter<LostFoundsItemResponse,
            LostFoundAdapter.MyViewHolder>(DIFF_CALLBACK) {

    private lateinit var onItemClickCallback: OnItemClickCallback
    private var originalData = mutableListOf<LostFoundsItemResponse>()
    private var filteredData = mutableListOf<LostFoundsItemResponse>()
    private var isAll = false;

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemRowLostAndFoundBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )



        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = originalData[originalData.indexOf(getItem(position))]
        holder.binding.cbItemLostFoundIsCompleted.setOnCheckedChangeListener(null)
        holder.binding.cbItemLostFoundIsCompleted.setOnLongClickListener(null)

        holder.bind(data)

        holder.binding.cbItemLostFoundIsCompleted.setOnCheckedChangeListener { _, isChecked ->
            data.isCompleted = if (isChecked) 1 else 0
            holder.bind(data)
            onItemClickCallback.onCheckedChangeListener(data, isChecked)
        }

        holder.binding.ivItemLostFoundDetail.setOnClickListener {
            onItemClickCallback.onClickDetailListener(data.id)
        }

        if(isAll){
            holder.binding.cbItemLostFoundIsCompleted.isEnabled = false;
        }
    }

    class MyViewHolder(val binding: ItemRowLostAndFoundBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: LostFoundsItemResponse) {
            binding.apply {
                tvItemLostFoundTitle.text = data.title
                tvStatusItem.text = data.status
                cbItemLostFoundIsCompleted.isChecked = data.isCompleted == 1
            }
        }
    }

    fun submitOriginalList(list: List<LostFoundsItemResponse>) {
        originalData = list.toMutableList()
        filteredData = list.toMutableList()

        submitList(originalData)
    }

    fun setIsAll(){
        isAll = true
    }

    fun filter(query: String) {
        filteredData = if (query.isEmpty()) {
            originalData
        } else {
            originalData.filter {
                (it.title.contains(query, ignoreCase = true))
            }.toMutableList()
        }

        submitList(filteredData)
    }

    interface OnItemClickCallback {
        fun onCheckedChangeListener(lostFound: LostFoundsItemResponse, isChecked: Boolean)
        fun onClickDetailListener(lostFoundId: Int)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LostFoundsItemResponse>() {
            override fun areItemsTheSame(
                oldItem: LostFoundsItemResponse,
                newItem: LostFoundsItemResponse
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: LostFoundsItemResponse,
                newItem: LostFoundsItemResponse
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
} 