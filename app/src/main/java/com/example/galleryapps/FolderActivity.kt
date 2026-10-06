package com.example.galleryapps

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.galleryapps.databinding.ActivityMainBinding
import com.example.galleryapps.ui.FolderAdapter
import com.example.galleryapps.ui.FolderUiState
import com.example.galleryapps.ui.FolderViewModel
import com.example.galleryapps.ui.GridSpacingDecoration
import kotlinx.coroutines.launch

class FolderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: FolderViewModel by viewModels()

    private val  adapter = FolderAdapter{ bucket ->
        BucketActivity.start(this, bucket.id, bucket.name)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) {view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.Toolbar.setNavigationOnClickListener { finish() }
        binding.recycleView.layoutManager = GridLayoutManager(this, 2)
        binding.recycleView.adapter = adapter
        binding.recycleView.addItemDecoration(GridSpacingDecoration(dp(4)))

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.uiState.collect { render(it) }
            }
        }

        viewModel.loadBuckets()
    }
    private fun render(state: FolderUiState){
        binding.progressBar.visibility =
            if (state is FolderUiState.Loading) View.VISIBLE else View.GONE
        binding.recycleView.visibility =
            if (state is FolderUiState.Success) View.VISIBLE else View.GONE
        binding.textEmpty.visibility =
            if (state is FolderUiState.Empty || state is FolderUiState.Error) View.VISIBLE else View.GONE

        when (state) {
            is FolderUiState.Success -> adapter.submitList(state.buckets)
            is FolderUiState.Empty -> adapter.submitList(emptyList())
            is FolderUiState.Error -> binding.textEmpty.text = state.message
            FolderUiState.Loading -> Unit
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}