package com.example.galleryapps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.galleryapps.databinding.ActivityViewerBinding
import com.example.galleryapps.ui.ViewerPagerAdapter

class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding
    private lateinit var uris: ArrayList<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uris = intent.getParcelableArrayListExtra(EXTRA_URIS) ?: arrayListOf()
        val startPosition = intent.getIntExtra(EXTRA_POSITION, 0)

        if (uris.isEmpty()){
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) {view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.pager.adapter = ViewerPagerAdapter(uris)
        binding.pager.setCurrentItem(startPosition, false)

        binding.Toolbar.setNavigationOnClickListener {finish()}
        binding.Toolbar.inflateMenu(R.menu.menu_viewer)
        binding.Toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_share){
                shareCurrent()
                true
            } else {
                false
            }
        }
    }
    private fun shareCurrent(){
        val uri = uris.getOrNull(binding.pager.currentItem) ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }


    companion object {
        const val EXTRA_URIS = "extra_uris"
        const val EXTRA_POSITION = "extra_position"
    }
}