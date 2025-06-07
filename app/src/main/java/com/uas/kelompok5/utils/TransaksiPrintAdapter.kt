package com.uas.kelompok5.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import com.uas.kelompok5.data.local.Transaksi
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class TransaksiPrintAdapter(private val context: Context, private val transaksi: Transaksi) : PrintDocumentAdapter() {

    override fun onLayout(oldAttributes: PrintAttributes, newAttributes: PrintAttributes, cancellationSignal: CancellationSignal, callback: LayoutResultCallback, extras: Bundle?) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }
        val info = PrintDocumentInfo.Builder("struk_transaksi_${transaksi.id}.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).setPageCount(1).build()
        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(pages: Array<out PageRange>, destination: ParcelFileDescriptor, cancellationSignal: CancellationSignal, callback: WriteResultCallback) {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val paint = Paint().apply { color = Color.BLACK }
        var yPosition = 60f
        val xMargin = 40f
        val xValue = 350f

        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Struk Laundry Kiloan", xMargin, yPosition, paint)
        yPosition += 25
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("ID Transaksi: #${transaksi.id}", xMargin, yPosition, paint)
        yPosition += 30

        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
        canvas.drawText("Pelanggan: ${transaksi.namaPelanggan}", xMargin, yPosition, paint)
        yPosition += 20
        canvas.drawText("Nomor HP: ${transaksi.nomorHp}", xMargin, yPosition, paint)
        yPosition += 20
        canvas.drawText("Tanggal Masuk: ${sdf.format(Date(transaksi.tanggalMasuk))}", xMargin, yPosition, paint)
        yPosition += 20
        canvas.drawText("Estimasi Selesai: ${sdf.format(Date(transaksi.tanggalSelesai))}", xMargin, yPosition, paint)
        yPosition += 30

        canvas.drawLine(xMargin, yPosition, page.canvas.width - xMargin, yPosition, paint)
        yPosition += 20
        paint.isFakeBoldText = true
        canvas.drawText("DESKRIPSI", xMargin, yPosition, paint)
        canvas.drawText("TOTAL", xValue, yPosition, paint)
        yPosition += 25
        paint.isFakeBoldText = false
        canvas.drawLine(xMargin, yPosition, page.canvas.width - xMargin, yPosition, paint)
        yPosition += 20

        canvas.drawText("${transaksi.jenisLayanan.name} (${transaksi.beratKg} kg x Rp ${transaksi.jenisLayanan.hargaPerKg})", xMargin, yPosition, paint)
        canvas.drawText("Rp ${transaksi.hargaTotal}", xValue, yPosition, paint)
        yPosition += 30

        canvas.drawLine(xMargin, yPosition, page.canvas.width - xMargin, yPosition, paint)
        yPosition += 20

        canvas.drawText("Total Tagihan", xMargin, yPosition, paint)
        canvas.drawText("Rp ${transaksi.hargaTotal}", xValue, yPosition, paint)
        yPosition += 20

        canvas.drawText("Uang Muka (DP)", xMargin, yPosition, paint)
        canvas.drawText("Rp ${transaksi.uangMuka}", xValue, yPosition, paint)
        yPosition += 20

        canvas.drawText("Uang Pelunasan", xMargin, yPosition, paint)
        canvas.drawText("Rp ${transaksi.uangPelunasan}", xValue, yPosition, paint)
        yPosition += 20

        val totalBayar = transaksi.uangMuka + transaksi.uangPelunasan
        val sisa = transaksi.hargaTotal - totalBayar
        paint.isFakeBoldText = true
        if (sisa <= 0) {
            canvas.drawText("Kembalian", xMargin, yPosition, paint)
            canvas.drawText("Rp ${-sisa}", xValue, yPosition, paint)
        } else {
            canvas.drawText("Sisa Tagihan", xMargin, yPosition, paint)
            canvas.drawText("Rp $sisa", xValue, yPosition, paint)
        }
        yPosition += 30
        paint.isFakeBoldText = false

        canvas.drawText("Status Bayar: ${transaksi.statusBayar.name}", xMargin, yPosition, paint)
        yPosition += 20
        canvas.drawText("Status Pengerjaan: ${transaksi.statusProses.name}", xMargin, yPosition, paint)
        yPosition += 60

        val thankYouPaint = Paint().apply { textSize = 14f; isFakeBoldText = true; textAlign = Paint.Align.CENTER }
        canvas.drawText("Terima Kasih!", canvas.width / 2f, yPosition, thankYouPaint)

        document.finishPage(page)
        try {
            FileOutputStream(destination.fileDescriptor).use { document.writeTo(it) }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: IOException) {
            callback.onWriteFailed(e.toString())
        } finally {
            document.close()
        }
    }
}