package com.example.galleryapps

import android.content.Context
import android.content.Intent
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
import com.example.galleryapps.data.Bucket
import com.example.galleryapps.data.GalleryImage
import com.example.galleryapps.databinding.ActivityBucketBinding
import com.example.galleryapps.databinding.ActivityMainBinding
import com.example.galleryapps.ui.BucketViewModel
import com.example.galleryapps.ui.GridSpacingDecoration
import com.example.galleryapps.ui.ImageGridAdapter
import kotlinx.coroutines.launch

class BucketActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBucketBinding
    private val viewModel: BucketViewModel by viewModels()
    private var currentImages: List<GalleryImage> = emptyList()

    private val adapter = ImageGridAdapter { image ->
        val position = currentImages.indexOfFirst { it.id == image.id }
        if (position != -1){
            ViewerActivity.start(this, currentImages, position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBucketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bucketId = intent.getLongExtra(EXTRA_BUCKET_ID, -1L)
        val bucketName = intent.getStringExtra(EXTRA_BUCKET_NAME) ?: ""

        if (bucketId == -1L){
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root){view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        binding.Toolbar.title = bucketName
        binding.Toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(GridSpacingDecoration(dp(4)))

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { loading ->
                    binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.images.collect { images ->
                    currentImages = images
                    adapter.submitList(images)
                    binding.textEmpty.visibility =
                        if (images.isEmpty() && !viewModel.loading.value) View.VISIBLE else View.GONE
                }
            }
        }

        viewModel.loadImages(bucketId)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object{
        private const val EXTRA_BUCKET_ID = "extra_bucket_id"
        private const val EXTRA_BUCKET_NAME = "extra_bucket_name"

        fun start(context: Context, bucketId: Long, bucketName: String){
            val intent = Intent(context, BucketActivity::class.java).apply {
                putExtra(EXTRA_BUCKET_ID, bucketId)
                putExtra(EXTRA_BUCKET_NAME, bucketName)
            }
            context.startActivity(intent)
        }
    }
}