package com.example.galleryapps

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.galleryapps.data.MediaRepository
import com.example.galleryapps.data.PermissionHelper
import com.example.galleryapps.databinding.ActivityMainBinding
import com.example.galleryapps.ui.GalleryUiState
import com.example.galleryapps.ui.GalleryViewModel
import com.example.galleryapps.ui.GridSpacingDecoration
import com.example.galleryapps.ui.ImageGridAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: GalleryViewModel by viewModels()

    private val adapter = ImageGridAdapter { image ->
        // Tahap 4: buka ViewerActivity dengan image ini
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) {
            viewModel.loadImages()
        } else {
            renderPermissionRequired()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsets()
        setupRecyclerView()
        observeState()

        binding.buttonGrant.setOnClickListener {
            permissionLauncher.launch(PermissionHelper.requiredMediaPermissions())
        }
        if (PermissionHelper.hasMediaPermission(this)) {
            viewModel.loadImages()
        } else {
            permissionLauncher.launch(PermissionHelper.requiredMediaPermissions())
        }
    }

    private fun applyInsets(){
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) {view, insets ->
            val bar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bar.left, bar.top, bar.right, bar.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        binding.recycleView.layoutManager = GridLayoutManager(this, 3)
        binding.recycleView.adapter = adapter
        binding.recycleView.addItemDecoration(GridSpacingDecoration(dp(4)))
    }

    private fun observeState(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    state -> render(state)
                }
            }
        }
    }

    private fun render(state: GalleryUiState) {
        binding.permissionGroup.visibility = View.GONE
        binding.progressBar.visibility =
            if (state is GalleryUiState.Loading) View.VISIBLE else View.GONE
        binding.recycleView.visibility =
            if (state is GalleryUiState.Success) View.VISIBLE else View.GONE
        binding.textEmpty.visibility =
            if (state is GalleryUiState.Empty || state is GalleryUiState.Error) View.VISIBLE else View.GONE

        when (state) {
            is GalleryUiState.Success -> adapter.submitList(state.images)
            is GalleryUiState.Empty -> adapter.submitList(emptyList())
            is GalleryUiState.Error -> {
                binding.textEmpty.text = state.message
            }

            GalleryUiState.Loading -> Unit
        }
    }

    private fun renderPermissionRequired(){
        binding.permissionGroup.visibility = View.VISIBLE
        binding.recycleView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.textEmpty.visibility = View.GONE
    }

    private fun dp(value : Int) : Int =
        (value * resources.displayMetrics.density).toInt()
}