package com.example.trips

import android.R
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.LeadingMarginSpan
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.example.trips.databinding.ActivityDetailBinding
import com.example.trips.ui.main.MainViewModel
import com.example.trips.ui.main.TRIP_ID_EXTRA
import com.google.android.material.appbar.AppBarLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL

class DetailActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tripId = intent.getStringExtra(TRIP_ID_EXTRA)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.getTrips().observe(this, { data ->
            val trip = data.find { item -> item.id == tripId }
            trip?.let {
                val toolbar: Toolbar = binding.toolbar
                setSupportActionBar(toolbar)
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                supportActionBar!!.title = it.name

                try {
                    doAsync {
                        val url = URL(it.imageUrl)
                        val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                        uiThread {
                            binding.appBarImage.setImageBitmap(bmp)
                        }
                    }
                } catch(e: Exception) {}

                binding.name.text = it.name
                binding.desc.text = textIndent(it.desc)
                binding.location.text = it.location
                binding.opened.text = it.opened
                binding.reviewLink.text = it.reviewUrl
                binding.reviewLink.setOnClickListener { _ ->
                    val i = Intent(Intent.ACTION_VIEW, Uri.parse(it.reviewUrl))
                    startActivity(i)
                }
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun textIndent(text: String): SpannableString {
        val spannable = SpannableString(text)
        val span = LeadingMarginSpan.Standard(100, 0)
        spannable.setSpan(span, 0, spannable.count(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }
}