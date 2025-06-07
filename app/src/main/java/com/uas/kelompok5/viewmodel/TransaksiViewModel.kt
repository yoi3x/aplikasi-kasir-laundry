package com.uas.kelompok5.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.uas.kelompok5.data.local.AppDatabase
import com.uas.kelompok5.data.local.StatusProses
import com.uas.kelompok5.data.local.Transaksi
import com.uas.kelompok5.data.repository.TransaksiRepository
import kotlinx.coroutines.launch

class TransaksiViewModel(application: Application) : AndroidViewModel(application) {

    // Properti untuk Repository, satu-satunya sumber data
    private val repository: TransaksiRepository

    // Properti internal untuk menampung query pencarian dan filter status dari UI
    private val searchQuery = MutableLiveData("")
    private val statusFilter = MutableLiveData<StatusProses?>()

    // Menggabungkan pemicu (triggers) dari search dan filter
    private val triggers = MediatorLiveData<Pair<String, StatusProses?>>().apply {
        addSource(searchQuery) { value = Pair(it, statusFilter.value) }
        addSource(statusFilter) { value = Pair(searchQuery.value ?: "", it) }
    }

    // LiveData utama yang akan diamati oleh MainActivity.
    // Menggunakan switchMap untuk secara reaktif mengambil data baru dari database
    // setiap kali pemicu (triggers) berubah.
    val filteredTransaksi: LiveData<List<Transaksi>> = triggers.switchMap { pair ->
        val query = pair.first
        val status = pair.second

        // Memanggil data dari repository, lalu memfilter berdasarkan status di sini
        repository.searchTransaksi(query).map { list ->
            if (status == null) {
                list // Jika filter "Semua", tampilkan semua hasil pencarian
            } else {
                list.filter { it.statusProses == status } // Filter berdasarkan status
            }
        }
    }

    // Blok init dieksekusi saat ViewModel pertama kali dibuat
    init {
        val transaksiDao = AppDatabase.getDatabase(application).transaksiDao()
        repository = TransaksiRepository(transaksiDao)
    }

    /**
     * Fungsi publik untuk dipanggil dari UI (MainActivity)
     */
    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setStatusFilter(status: StatusProses?) {
        statusFilter.value = status
    }

    /**
     * Fungsi untuk operasi database (CRUD), dipanggil dari UI.
     * Menggunakan viewModelScope untuk memastikan berjalan di background thread.
     */
    fun insert(transaksi: Transaksi) = viewModelScope.launch {
        repository.insert(transaksi)
    }

    fun update(transaksi: Transaksi) = viewModelScope.launch {
        repository.update(transaksi)
    }

    fun delete(transaksi: Transaksi) = viewModelScope.launch {
        repository.delete(transaksi)
    }
}