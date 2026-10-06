package com.example.galleryapps.archive

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.galleryapps.databinding.ActivityViewerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ArchiveViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewerBinding
    private var archive: LoadedArchive? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val path = intent.getStringExtra(EXTRA_PATH)
        val startIndex = intent.getIntExtra(EXTRA_INDEX, 0)
        if (path == null) {
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.Toolbar.setNavigationOnClickListener { finish() }

        lifecycleScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                runCatching { LoadedArchive.open(File(path)) }.getOrNull()
            }
            if (loaded == null) {
                finish()
                return@launch
            }
            archive = loaded
            val entries = loaded.entries
            if (entries.isEmpty()) {
                finish()
                return@launch
            }

            val target = maxOf(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
            binding.pager.adapter = ArchivePagerAdapter(lifecycleScope, loaded, entries, target)

            val safeStart = startIndex.coerceIn(0, entries.size - 1)
            binding.pager.setCurrentItem(safeStart, false)

            binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    binding.Toolbar.title = "${position + 1} / ${entries.size}"
                }
            })
            binding.Toolbar.title = "${safeStart + 1} / ${entries.size}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        archive?.close()
    }

    companion object {
        private const val EXTRA_PATH = "extra_archive_path"
        private const val EXTRA_INDEX = "extra_entry_index"

        fun start(context: Context, archivePath: String, index: Int) {
            context.startActivity(
                Intent(context, ArchiveViewerActivity::class.java).apply {
                    putExtra(EXTRA_PATH, archivePath)
                    putExtra(EXTRA_INDEX, index)
                }
            )
        }
    }
}