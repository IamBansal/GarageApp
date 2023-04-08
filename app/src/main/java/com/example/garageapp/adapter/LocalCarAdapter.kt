package com.example.garageapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.garageapp.model.Result
import com.example.garageapp.databinding.CustomCarItemBinding
import com.example.garageapp.ui.CarViewModel
import com.example.garageapp.ui.screens.DashBoard

class LocalCarAdapter(val context: Context, val viewModel: CarViewModel): RecyclerView.Adapter<LocalCarAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(private var binding: CustomCarItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(car: Result){
            binding.tvMake.text = car.Make_Name
            binding.tvModel.text = car.Model_Name
            binding.btnDelete.setOnClickListener {
                viewModel.deleteCar(car)
            }
            binding.btnAddCarImage.setOnClickListener {
                (context as DashBoard).customImageSelectionDialog(binding.ivCar)
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Result>(){
        override fun areItemsTheSame(oldItem: Result, newItem: Result): Boolean {
            return oldItem.Model_Name == newItem.Model_Name
        }

        override fun areContentsTheSame(oldItem: Result, newItem: Result): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(CustomCarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = differ.currentList[position]
        holder.bind(article)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}