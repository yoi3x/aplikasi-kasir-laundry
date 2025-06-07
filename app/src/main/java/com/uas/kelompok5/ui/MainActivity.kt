
package com.uas.kelompok5.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.uas.kelompok5.R
import com.uas.kelompok5.data.local.StatusProses
import com.uas.kelompok5.databinding.ActivityMainBinding
import com.uas.kelompok5.viewmodel.TransaksiViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TransaksiViewModel
    private lateinit var transaksiAdapter: TransaksiListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this)[TransaksiViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        transaksiAdapter = TransaksiListAdapter()
        binding.recyclerView.apply {
            adapter = transaksiAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupObservers() {
        // Mengamati data yang sudah difilter dari ViewModel
        viewModel.filteredTransaksi.observe(this) { transaksis ->
            transaksis?.let {
                transaksiAdapter.submitList(it)
            }
        }
    }

    private fun setupListeners() {
        // Listener untuk search bar
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })

        // Listener untuk filter chip
        binding.chipGroupFilter.setOnCheckedChangeListener { group, checkedId ->
            val statusFilter = when (checkedId) {
                R.id.chipProses -> StatusProses.PROSES
                R.id.chipSelesai -> StatusProses.SELESAI
                else -> null // Untuk chip "Semua"
            }
            viewModel.setStatusFilter(statusFilter)
        }

        binding.fabTambah.setOnClickListener {
            val intent = Intent(this, FormTransaksiActivity::class.java)
            startActivity(intent)
        }
    }
}