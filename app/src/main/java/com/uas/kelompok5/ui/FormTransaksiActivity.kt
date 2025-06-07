package com.uas.kelompok5.ui

import android.content.Context
import android.os.Bundle
import android.print.PrintManager
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.uas.kelompok5.R
import com.uas.kelompok5.data.local.*
import com.uas.kelompok5.databinding.ActivityFormTransaksiBinding
import com.uas.kelompok5.utils.TransaksiPrintAdapter
import com.uas.kelompok5.viewmodel.TransaksiViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class FormTransaksiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormTransaksiBinding
    private lateinit var viewModel: TransaksiViewModel
    private var currentTransaksi: Transaksi? = null
    private var totalHargaSaatIni: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[TransaksiViewModel::class.java]

        setupDropdowns()
        setupInitialState()
        setupListeners()
    }

    private fun setFormEditable(isEditable: Boolean) {
        binding.etNamaPelanggan.isEnabled = isEditable
        binding.etNomorHp.isEnabled = isEditable
        binding.etBerat.isEnabled = isEditable
        for (i in 0 until binding.rgJenisLayanan.childCount) {
            binding.rgJenisLayanan.getChildAt(i).isEnabled = isEditable
        }
    }

    private fun setupInitialState() {
        currentTransaksi = intent.getSerializableExtra("EXTRA_TRANSAKSI") as? Transaksi

        if (currentTransaksi != null) { // MODE UPDATE
            title = "Update Transaksi"
            populateForm(currentTransaksi!!)
            updateTotalHarga()

            binding.tvTransaksiId.visibility = View.VISIBLE
            binding.layoutInfoTanggal.visibility = View.VISIBLE
            binding.layoutUpdate.visibility = View.VISIBLE
            binding.btnHapus.visibility = View.VISIBLE
            binding.btnPrint.visibility = View.VISIBLE
            binding.layoutUangMuka.visibility = View.GONE

            if (currentTransaksi!!.statusProses == StatusProses.SELESAI) {
                setFormEditable(false)
            }
        } else { // MODE CREATE
            title = "Tambah Transaksi Baru"
            binding.rbBiasa.isChecked = true
            binding.layoutUpdate.visibility = View.GONE
            updateTotalHarga()
        }
    }

    private fun populateForm(transaksi: Transaksi) {
        binding.tvTransaksiId.text = "ID Transaksi: #${transaksi.id}"
        binding.tvTanggalMasuk.text = "Tanggal Masuk: ${formatTanggal(transaksi.tanggalMasuk)}"
        binding.tvTanggalSelesai.text = "Estimasi Selesai: ${formatTanggal(transaksi.tanggalSelesai)}"
        binding.etNamaPelanggan.setText(transaksi.namaPelanggan)
        binding.etNomorHp.setText(transaksi.nomorHp)
        binding.etBerat.setText(transaksi.beratKg.toString())
        when (transaksi.jenisLayanan) {
            JenisLayanan.BIASA -> binding.rbBiasa.isChecked = true
            JenisLayanan.SEDANG -> binding.rbSedang.isChecked = true
            JenisLayanan.KILAT -> binding.rbKilat.isChecked = true
        }
        binding.tvUangMukaSudahBayar.text = "Uang Muka Dibayar: ${formatRupiah(transaksi.uangMuka)}"
        binding.etUangPelunasan.setText(transaksi.uangPelunasan.toString())
        binding.actvStatusProses.setText(transaksi.statusProses.name, false)
    }

    private fun setupDropdowns() {
        val statusProsesAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, StatusProses.values().map { it.name })
        binding.actvStatusProses.setAdapter(statusProsesAdapter)
    }

    private fun setupListeners() {
        binding.btnKembali.setOnClickListener { finish() }
        binding.etBerat.doOnTextChanged { _, _, _, _ -> updateTotalHarga() }
        binding.rgJenisLayanan.setOnCheckedChangeListener { _, _ -> updateTotalHarga() }
        binding.btnSimpan.setOnClickListener { saveTransaksi() }
        binding.btnHapus.setOnClickListener { confirmDelete() }
        binding.btnPrint.setOnClickListener { currentTransaksi?.let { doPrint(it) } }
        binding.etUangMuka.doOnTextChanged { _, _, _, _ -> updatePaymentInfo() }
        binding.etUangPelunasan.doOnTextChanged { _, _, _, _ -> updatePaymentInfo() }
        binding.actvStatusProses.setOnItemClickListener { _, _, _, _ ->
            if (binding.actvStatusProses.text.toString() == StatusProses.SELESAI.name) setFormEditable(false)
            else setFormEditable(true)
        }
    }

    private fun updatePaymentInfo() {
        val uangMuka = binding.etUangMuka.text.toString().toIntOrNull() ?: (currentTransaksi?.uangMuka ?: 0)
        val uangPelunasan = binding.etUangPelunasan.text.toString().toIntOrNull() ?: (currentTransaksi?.uangPelunasan ?: 0)
        val totalBayar = uangMuka + uangPelunasan
        val sisaTagihan = totalHargaSaatIni - totalBayar

        val infoText = if (sisaTagihan <= 0) "Kembalian: ${formatRupiah(-sisaTagihan)}" else "Sisa Tagihan: ${formatRupiah(sisaTagihan)}"
        val infoColor = if (sisaTagihan <= 0) R.color.purple_700 else com.google.android.material.R.color.design_default_color_error
        binding.tvInfoPembayaran.text = infoText
        binding.tvInfoPembayaran.setTextColor(ContextCompat.getColor(this, infoColor))
    }

    private fun updateTotalHarga() {
        val berat = binding.etBerat.text.toString().toDoubleOrNull() ?: 0.0
        val selectedJenis = when (binding.rgJenisLayanan.checkedRadioButtonId) {
            R.id.rbSedang -> JenisLayanan.SEDANG
            R.id.rbKilat -> JenisLayanan.KILAT
            else -> JenisLayanan.BIASA
        }
        totalHargaSaatIni = (berat * selectedJenis.hargaPerKg).toInt()
        binding.tvTotalHarga.text = "Total: ${formatRupiah(totalHargaSaatIni)}"
        updatePaymentInfo()
    }

    private fun saveTransaksi() {
        val nama = binding.etNamaPelanggan.text.toString().trim()
        val nomorHp = binding.etNomorHp.text.toString().trim()
        val berat = binding.etBerat.text.toString().toDoubleOrNull()
        if (nama.isEmpty() || nomorHp.isEmpty() || berat == null || berat <= 0) {
            Toast.makeText(this, "Nama, Nomor HP, dan Berat harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }
        val jenisLayanan = when (binding.rgJenisLayanan.checkedRadioButtonId) {
            R.id.rbSedang -> JenisLayanan.SEDANG
            R.id.rbKilat -> JenisLayanan.KILAT
            else -> JenisLayanan.BIASA
        }
        val hargaFinal = (berat * jenisLayanan.hargaPerKg).toInt()
        val tanggalMasuk = currentTransaksi?.tanggalMasuk ?: System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = tanggalMasuk; add(Calendar.DAY_OF_YEAR, jenisLayanan.durasiHari) }
        val tanggalSelesai = calendar.timeInMillis

        if (currentTransaksi == null) { // Mode Create
            val uangMuka = binding.etUangMuka.text.toString().toIntOrNull() ?: 0
            val statusBayar = if (uangMuka >= hargaFinal) StatusBayar.LUNAS else StatusBayar.BELUM_LUNAS
            val transaksiBaru = Transaksi(0, nama, nomorHp, berat, jenisLayanan, statusBayar, tanggalMasuk, tanggalSelesai, hargaFinal, uangMuka, 0, StatusProses.PROSES)
            viewModel.insert(transaksiBaru)
            Toast.makeText(this, "Transaksi baru ditambahkan!", Toast.LENGTH_SHORT).show()
        } else { // Mode Update
            val uangMuka = currentTransaksi!!.uangMuka
            val uangPelunasan = binding.etUangPelunasan.text.toString().toIntOrNull() ?: 0
            val statusBayar = if ((uangMuka + uangPelunasan) >= hargaFinal) StatusBayar.LUNAS else StatusBayar.BELUM_LUNAS
            val statusProses = StatusProses.valueOf(binding.actvStatusProses.text.toString())
            val transaksiUpdate = currentTransaksi!!.copy(namaPelanggan = nama, nomorHp = nomorHp, beratKg = berat, jenisLayanan = jenisLayanan, statusBayar = statusBayar, tanggalSelesai = tanggalSelesai, hargaTotal = hargaFinal, uangMuka = uangMuka, uangPelunasan = uangPelunasan, statusProses = statusProses)
            viewModel.update(transaksiUpdate)
            Toast.makeText(this, "Transaksi diupdate!", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    // ===================================
    // KODE FUNGSI HELPER YANG LENGKAP
    // ===================================

    private fun formatRupiah(number: Int): String {
        return NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(number.toLong())
    }

    private fun formatTanggal(timestamp: Long): String {
        return SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID")).format(Date(timestamp))
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Anda yakin ingin menghapus transaksi ini?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                currentTransaksi?.let {
                    viewModel.delete(it)
                    Toast.makeText(this, "Transaksi dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .show()
    }

    private fun doPrint(transaksi: Transaksi) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Struk_Laundry_${transaksi.id}"
        val printAdapter = TransaksiPrintAdapter(this, transaksi)
        printManager.print(jobName, printAdapter, null)
    }
}
