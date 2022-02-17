package com.example.trips.ui.main

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trips.DetailActivity
import com.example.trips.databinding.CardTripBinding
import com.example.trips.databinding.MainFragmentBinding
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL

const val TRIP_ID_EXTRA = "trip id extra"
class MainFragment : Fragment(), MainClickListener {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.getTrips().observe(viewLifecycleOwner, {

            binding.recyclerViewTopTrips.apply {
                layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                adapter = CardTripAdapter(it.shuffled().take(5).reversed(), this@MainFragment)
            }

            binding.recyclerViewTrips.apply {
                layoutManager = GridLayoutManager(activity, 2)
                adapter = CardTripAdapter(it, this@MainFragment)
            }
        })

        return binding.root
    }

    override fun onClick(id: String) {
        val intent = Intent(activity, DetailActivity::class.java)
        intent.putExtra(TRIP_ID_EXTRA, id)
        startActivity(intent)
    }
}

class CardTripAdapter(
    private val trips: List<MainViewModel.Trip>,
    private val onClick: MainClickListener
): RecyclerView.Adapter<CardTripViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardTripViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = CardTripBinding.inflate(from, parent, false)
        return CardTripViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: CardTripViewHolder, position: Int) {
        holder.bindTrip(trips[position])
    }

    override fun getItemCount(): Int = trips.size
}

class CardTripViewHolder(
    private val binding: CardTripBinding,
    private val onClick: MainClickListener
): RecyclerView.ViewHolder(binding.root) {
    fun bindTrip(trip: MainViewModel.Trip) {
        try {
            doAsync {
                val url = URL(trip.imageUrl)
                val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                uiThread {
                    binding.cover.setImageBitmap(bmp)
                }
            }
        } catch(e: Exception) {}

        binding.name.text = trip.name
        binding.location.text = trip.location

        binding.container.setOnClickListener {
            onClick.onClick(trip.id)
        }
    }
}

interface MainClickListener {
    fun onClick(id: String)
}