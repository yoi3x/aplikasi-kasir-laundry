// Lokasi: data/repository/TransaksiRepository.kt
package com.uas.kelompok5.data.repository

import androidx.lifecycle.LiveData
import com.uas.kelompok5.data.local.Transaksi
import com.uas.kelompok5.data.local.TransaksiDao

class TransaksiRepository(private val transaksiDao: TransaksiDao) {

    // Fungsi ini sekarang hanya fokus pada pencarian berdasarkan nama
    fun searchTransaksi(searchQuery: String): LiveData<List<Transaksi>> {
        return transaksiDao.searchTransaksi(searchQuery)
    }

    suspend fun insert(transaksi: Transaksi) {
        transaksiDao.insert(transaksi)
    }

    suspend fun update(transaksi: Transaksi) {
        transaksiDao.update(transaksi)
    }

    suspend fun delete(transaksi: Transaksi) {
        transaksiDao.delete(transaksi)
    }
}