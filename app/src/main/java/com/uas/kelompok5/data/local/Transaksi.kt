package com.uas.kelompok5.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

enum class JenisLayanan(val hargaPerKg: Int, val durasiHari: Int) {
    BIASA(5000, 3),
    SEDANG(7000, 2),
    KILAT(10000, 1)
}

enum class StatusBayar {
    BELUM_LUNAS, LUNAS
}

enum class StatusProses {
    PROSES, SELESAI
}


@Entity(tableName = "tabel_transaksi")
data class Transaksi(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val namaPelanggan: String,
    val nomorHp: String,
    val beratKg: Double,
    val jenisLayanan: JenisLayanan,
    var statusBayar: StatusBayar,
    val tanggalMasuk: Long,
    val tanggalSelesai: Long,
    val hargaTotal: Int,
    var uangMuka: Int,      // DP / Panjar
    var uangPelunasan: Int, // Pembayaran sisa
    var statusProses: StatusProses
) : Serializable