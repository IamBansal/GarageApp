package com.example.garageapp.ui.screens

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.garageapp.R
import com.example.garageapp.adapter.CarAdapter
import com.example.garageapp.adapter.LocalCarAdapter
import com.example.garageapp.databinding.ActivityDashboardBinding
import com.example.garageapp.databinding.CustomDialogImageSelectionBinding
import com.example.garageapp.db.CarDatabase
import com.example.garageapp.model.Result
import com.example.garageapp.repository.CarRepository
import com.example.garageapp.ui.CarViewModel
import com.example.garageapp.ui.CarViewModelProviderFactory
import com.example.garageapp.utils.Resource
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class DashBoard : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewModel: CarViewModel
    private lateinit var carAdapter: CarAdapter
    private lateinit var localCarAdapter: LocalCarAdapter
    private lateinit var car: Result
    private lateinit var carImage: ImageView

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
        localCarAdapter = LocalCarAdapter(this, viewModel)
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

    private fun pickImageFromGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        )
        galleryLauncher.launch(pickIntent)
    }

    private fun imageFromCamera() = camLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))

    private var galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {

                val selectedPhotoUri = it.data

                Glide.with(this).load(selectedPhotoUri).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("TAG", "Error loading image", e)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            resource?.let {
                                val bitmap: Bitmap = resource.toBitmap()
                                val imagePath = saveImageToInternalStorage(bitmap)
                                Toast.makeText(this@DashBoard, imagePath, Toast.LENGTH_SHORT).show()
                            }
                            return false
                        }
                    }).into(carImage)
            }
        }
    }

    private var camLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.extras?.let {
                val thumbnail: Bitmap = it.get("data") as Bitmap
                Glide.with(this).load(thumbnail).centerCrop().into(carImage)
                val imagePath = saveImageToInternalStorage(thumbnail)
                Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun customImageSelectionDialog(ivCar: ImageView) {
        val dialog = Dialog(this)
        val dialogBinding = CustomDialogImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        carImage = ivCar

        dialogBinding.camera.setOnClickListener {
            imageFromCamera()
            dialog.dismiss()
        }
        dialogBinding.gallery.setOnClickListener {
            pickImageFromGallery()
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }
}