
package com.uas.kelompok5.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransaksiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaksi: Transaksi)

    @Update
    suspend fun update(transaksi: Transaksi)

    @Delete
    suspend fun delete(transaksi: Transaksi)

    // Query baru yang lebih fleksibel. Akan mengambil semua jika query kosong.
    @Query("SELECT * FROM tabel_transaksi WHERE namaPelanggan LIKE '%' || :searchQuery || '%' OR CAST(id AS TEXT) LIKE '%' || :searchQuery || '%' ORDER BY tanggalMasuk DESC")
    fun searchTransaksi(searchQuery: String): LiveData<List<Transaksi>>
}