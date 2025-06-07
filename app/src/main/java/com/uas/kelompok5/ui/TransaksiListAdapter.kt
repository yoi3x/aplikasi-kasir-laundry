
package com.uas.kelompok5.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uas.kelompok5.data.local.Transaksi
import com.uas.kelompok5.databinding.ItemTransaksiRevisedBinding // Ganti ke binding yang baru
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransaksiListAdapter : ListAdapter<Transaksi, TransaksiListAdapter.TransaksiViewHolder>(TransaksiDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiViewHolder {
        // Gunakan binding untuk layout item yang baru
        val binding = ItemTransaksiRevisedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransaksiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransaksiViewHolder, position: Int) {
        val currentTransaksi = getItem(position)
        holder.bind(currentTransaksi)
    }

    class TransaksiViewHolder(private val binding: ItemTransaksiRevisedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaksi: Transaksi) {
            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
            val formatDate = SimpleDateFormat("dd/MM/yyyy", localeID)

            // Mengisi data sesuai dengan layout item_transaksi_revised.xml
            binding.tvNamaPelanggan.text = transaksi.namaPelanggan
            binding.tvNomorHp.text = transaksi.nomorHp
            binding.tvTransaksiId.text = "ID: #${transaksi.id}"
            binding.tvHargaTotal.text = formatRupiah.format(transaksi.hargaTotal)

            // Menggunakan properti baru 'statusProses'
            binding.tvStatusProses.text = transaksi.statusProses.name

            binding.tvTanggalSelesai.text = "Selesai: ${formatDate.format(Date(transaksi.tanggalSelesai))}"

            // Listener untuk membuka mode update saat item diklik
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, FormTransaksiActivity::class.java).apply {
                    putExtra("EXTRA_TRANSAKSI", transaksi)
                }
                context.startActivity(intent)
            }
        }
    }

    class TransaksiDiffCallback : DiffUtil.ItemCallback<Transaksi>() {
        override fun areItemsTheSame(oldItem: Transaksi, newItem: Transaksi): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaksi, newItem: Transaksi): Boolean {
            return oldItem == newItem
        }
    }
}