package com.example.garageapp.ui.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.garageapp.R
import com.example.garageapp.adapter.CarAdapter
import com.example.garageapp.adapter.LocalCarAdapter
import com.example.garageapp.databinding.ActivityDashboardBinding
import com.example.garageapp.db.CarDatabase
import com.example.garageapp.repository.CarRepository
import com.example.garageapp.ui.CarViewModel
import com.example.garageapp.ui.CarViewModelProviderFactory
import com.example.garageapp.utils.Resource
import com.google.android.material.snackbar.Snackbar
import com.example.garageapp.model.Result

class DashBoard : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewModel: CarViewModel
    private lateinit var carAdapter: CarAdapter
    private lateinit var localCarAdapter: LocalCarAdapter
    private lateinit var car: Result

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = CarRepository(CarDatabase(this))
        val factory = CarViewModelProviderFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[CarViewModel::class.java]
        carAdapter = CarAdapter()

        setUpRecyclerView()

        viewModel.getAllSavedCars().observe(this) { cars ->
            localCarAdapter.differ.submitList(cars)
        }

        viewModel.getCarMake()

        binding.btnAddCar.setOnClickListener {
            car.Model_Name = binding.atvModel.text.toString()
            if (binding.atvMake.text.isNotEmpty() && binding.atvModel.text.isNotEmpty()){
                viewModel.saveCar(car)
                Snackbar.make(it, "Car saved successfully.", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(it, "Select both maker and model first.", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.atvMake.setOnItemClickListener { _, _, position, _ ->
            binding.atvModel.text = null
            val list = carAdapter.differ.currentList
            if (list.isNotEmpty()){
                car = list[position]
                viewModel.getCarModel(list[position].Make_ID)
            }
            else {
                Toast.makeText(this, "This maker don't have any models", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.carMake.observe(this) { response ->

            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        carAdapter.differ.submitList(newsResponse.Results.toList())
                        val list = carAdapter.differ.currentList
                        val dropList = ArrayList<String>()
                        for (items in list){
                            dropList.add(items.Make_Name)
                        }
                        binding.atvMake.setAdapter(ArrayAdapter(this,
                            R.layout.dropdown_item, dropList))
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        Toast.makeText(this, "An error occurred: $it", Toast.LENGTH_SHORT).show()
                        Log.e("ErrorInMake", "An error occurred $it")
                    }
                }
                is Resource.Loading -> {
                    Log.d("LoadingInMake", "In loading state")
                    showProgressBar()
                }
            }

        }

        viewModel.carModel.observe(this) { response ->

            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        val list = newsResponse.Results.toList()
                        val dropList = ArrayList<String>()
                        for (items in list){
                            dropList.add(items.Model_Name!!)
                        }
                        binding.atvModel.setAdapter(ArrayAdapter(this,
                            R.layout.dropdown_item, dropList))
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        Toast.makeText(this, "An error occurred: $it", Toast.LENGTH_SHORT).show()
                        Log.e("ErrorInModel", "An error occurred $it")
                    }
                }
                is Resource.Loading -> {
                    Log.d("LoadingInModel", "In loading state")
                    showProgressBar()
                }
            }

        }

    }

    private fun setUpRecyclerView() {
        localCarAdapter = LocalCarAdapter(viewModel)
        binding.rvCars.apply {
            adapter = localCarAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
    }
}