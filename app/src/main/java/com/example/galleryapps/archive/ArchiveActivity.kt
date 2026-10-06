package com.example.galleryapps.archive

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.example.galleryapps.R
import com.example.galleryapps.databinding.ActivityArchiveBinding
import com.example.galleryapps.ui.GridSpacingDecoration
import kotlinx.coroutines.launch

class ArchiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArchiveBinding
    private val viewModel: ArchiveViewModel by viewModels()

    private var adapter: ArchiveEntryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uriString = intent.getStringExtra(EXTRA_URI)
        if (uriString == null) {
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.addItemDecoration(GridSpacingDecoration(dp(4)))

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }

        viewModel.open(Uri.parse(uriString))
    }

    private fun render(state: ArchiveUiState) {
        binding.progressBar.visibility =
            if (state is ArchiveUiState.Loading) View.VISIBLE else View.GONE
        binding.recyclerView.visibility =
            if (state is ArchiveUiState.Success) View.VISIBLE else View.GONE
        binding.textEmpty.visibility =
            if (state is ArchiveUiState.Empty || state is ArchiveUiState.Error) View.VISIBLE else View.GONE

        when (state) {
            is ArchiveUiState.Success -> {
                val archive = state.archive
                if (adapter == null) {
                    val loader = ArchiveThumbLoader(archive)
                    val newAdapter = ArchiveEntryAdapter(lifecycleScope, loader) { entry ->
                        ArchiveViewerActivity.start(this, archive.file.absolutePath, entry.index)
                    }
                    adapter = newAdapter
                    binding.recyclerView.adapter = newAdapter
                }
                adapter?.submitList(state.entries)
            }
            is ArchiveUiState.Empty -> {
                binding.textEmpty.text = getString(R.string.no_archive_images)
                adapter?.submitList(emptyList())
            }
            is ArchiveUiState.Error -> binding.textEmpty.text = state.message
            ArchiveUiState.Loading -> Unit
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val EXTRA_URI = "extra_archive_uri"

        fun start(context: Context, uri: String) {
            context.startActivity(
                Intent(context, ArchiveActivity::class.java).putExtra(EXTRA_URI, uri)
            )
        }
    }
}