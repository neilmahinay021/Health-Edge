package com.example.healthedge.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.ContextCompat
import com.example.healthedge.R
import com.example.healthedge.models.Diagnosis
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PdfGenerator(private val context: Context) {
    fun generateDiagnosisPdf(diagnosis: Diagnosis, userId: Int): File {
        val file = File(context.getExternalFilesDir(null), "diagnosis_${userId}.pdf")
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val boldPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val regularPaint = Paint().apply {
            textSize = 14f
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val smallPaint = Paint().apply {
            textSize = 10f
            color = Color.BLACK
        }
        // Draw header text
        var y = 50
        canvas.drawText("HEALTH EDGE", 40f, y.toFloat(), headerPaint)
        y += 22
        canvas.drawText("Eulogio Amang Rodriguez Institute and Technology,", 40f, y.toFloat(), regularPaint)
        y += 18
        canvas.drawText("Sampaloc Manila.", 40f, y.toFloat(), regularPaint)
        y += 18
        canvas.drawText("PH : 095475644", 40f, y.toFloat(), regularPaint)
        // Draw logo top-right
        val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo)
        canvas.drawBitmap(logoBitmap, null, Rect(495, 30, 555, 90), null)
        // Draw horizontal line
        y += 15
        canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paint)
        y += 30
        // Patient info row
        val leftX = 40f
        val rightX = 350f
        val labelBold = Paint(boldPaint)
        val valueRegular = Paint(regularPaint)
        labelBold.textSize = 14f
        valueRegular.textSize = 14f
        // Patient Code (bold) left, Date right
        canvas.drawText("Patient Code: ", leftX.toFloat(), y.toFloat(), boldPaint)
        canvas.drawText("${diagnosis.patientCode}", (leftX + 100).toFloat(), y.toFloat(), regularPaint)
        canvas.drawText("Date :", rightX.toFloat(), y.toFloat(), boldPaint)
        // Format createdAt date
        val formattedCreatedAt = diagnosis.createdAt?.let { dateString ->
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            try {
                // Try to parse ISO 8601 format first (e.g., 2024-07-09T10:00:00.000Z)
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = isoFormat.parse(dateString)
                date?.let { outputFormat.format(it) }
            } catch (e: Exception) {
                try {
                    // Fallback to the format "yyyy-MM-dd HH:mm:ss"
                    val simpleFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val date = simpleFormat.parse(dateString)
                    date?.let { outputFormat.format(it) }
                } catch (e2: Exception) {
                    dateString // If all parsing fails, return the original string
                }
            }
        } ?: "-"
        canvas.drawText(formattedCreatedAt, (rightX + 50).toFloat(), y.toFloat(), regularPaint)
        y += 18
        // Address, Weight, Height, BP
        canvas.drawText("Address: ", leftX.toFloat(), y.toFloat(), boldPaint)
        canvas.drawText(diagnosis.address ?: "-", (leftX + 70).toFloat(), y.toFloat(), regularPaint)
        y += 18
        canvas.drawText("Weight: ", leftX.toFloat(), y.toFloat(), boldPaint)
        canvas.drawText("${diagnosis.weight ?: "-"}", (leftX + 60).toFloat(), y.toFloat(), regularPaint)
        canvas.drawText("Height: ", (leftX + 120).toFloat(), y.toFloat(), boldPaint)
        canvas.drawText("${diagnosis.height ?: "-"}", (leftX + 180).toFloat(), y.toFloat(), regularPaint)
        canvas.drawText("BP: ", (leftX + 240).toFloat(), y.toFloat(), boldPaint)
        canvas.drawText(diagnosis.bloodPressure ?: "-", (leftX + 270).toFloat(), y.toFloat(), regularPaint)
        y += 18
        // Referred By, Diagnosis
        canvas.drawText("Referred By: ", leftX.toFloat(), y.toFloat(), boldPaint)
        canvas.drawText(diagnosis.referredBy ?: "-", (leftX + 90).toFloat(), y.toFloat(), regularPaint)
        y += 18
        canvas.drawText("Diagnosis: ", leftX.toFloat(), y.toFloat(), boldPaint)
        canvas.drawText(diagnosis.diagnosis ?: "-", (leftX + 70).toFloat(), y.toFloat(), regularPaint)
        // Draw watermark logo in the background (drawn before the table so table overlays it)
        val watermarkBitmap = Bitmap.createScaledBitmap(logoBitmap, 350, 200, true)
        val watermarkPaint = Paint()
        watermarkPaint.alpha = 40 // semi-transparent
        canvas.drawBitmap(watermarkBitmap, 120f, (y + 20).toFloat(), watermarkPaint)
        y += 60
        // Medicine Table
        val tableTop = (y + 100).toFloat()
        val col1 = 40f
        val col2 = 220f
        val col3 = 340f
        val col4 = 460f
        val col5 = 550f
        val rowHeight = 28f
        // Table headers
        // Fill header row with black
        val headerBgPaint = Paint()
        headerBgPaint.style = Paint.Style.FILL
        headerBgPaint.color = Color.BLACK
        canvas.drawRect(col1, tableTop, col5, tableTop + rowHeight, headerBgPaint)
        // Draw table borders (drawn after the fill, so borders are visible)
        canvas.drawRect(col1, tableTop, col5, tableTop + rowHeight, paint)
        canvas.drawLine(col2, tableTop, col2, tableTop + rowHeight * 3, paint)
        canvas.drawLine(col3, tableTop, col3, tableTop + rowHeight * 3, paint)
        canvas.drawLine(col4, tableTop, col4, tableTop + rowHeight * 3, paint)
        canvas.drawLine(col1, tableTop, col1, tableTop + rowHeight * 3, paint)
        canvas.drawLine(col5, tableTop, col5, tableTop + rowHeight * 3, paint)
        canvas.drawLine(col1, tableTop + rowHeight, col5, tableTop + rowHeight, paint)
        canvas.drawLine(col1, tableTop + rowHeight * 2, col5, tableTop + rowHeight * 2, paint)
        canvas.drawLine(col1, tableTop + rowHeight * 3, col5, tableTop + rowHeight * 3, paint)
        // Draw header text in white and bold, vertically centered in the black bar
        val headerTextPaint = Paint(boldPaint)
        headerTextPaint.color = Color.WHITE
        headerTextPaint.textSize = 16f
        val headerY = tableTop + rowHeight / 2 + headerTextPaint.textSize / 2 - 4
        canvas.drawText("Medicine Name", col1 + 8f, headerY, headerTextPaint)
        canvas.drawText("Dosage", col2 + 8f, headerY, headerTextPaint)
        canvas.drawText("Duration", col3 + 8f, headerY, headerTextPaint)
        // Table row 1 (data)
        canvas.drawText(diagnosis.medicineName ?: "-", col1 + 8f, tableTop + rowHeight + 20f, regularPaint)
        canvas.drawText(diagnosis.dosage ?: "-", col2 + 8f, tableTop + rowHeight + 20f, regularPaint)
        canvas.drawText(diagnosis.duration ?: "-", col3 + 8f, tableTop + rowHeight + 20f, regularPaint)
        // Table row 2 (empty)
        canvas.drawText("", col1 + 8f, tableTop + rowHeight * 2 + 20f, regularPaint)
        canvas.drawText("", col2 + 8f, tableTop + rowHeight * 2 + 20f, regularPaint)
        canvas.drawText("", col3 + 8f, tableTop + rowHeight * 2 + 20f, regularPaint)
        // Advice and Next Visit
        var yAdvice = tableTop + rowHeight * 3 + 40f
        canvas.drawText("Advice Given:", leftX.toFloat(), yAdvice, boldPaint)
        yAdvice += 18f
        canvas.drawText(diagnosis.adviceGiven ?: "-", (leftX + 20).toFloat(), yAdvice, regularPaint)
        yAdvice += 22f
        canvas.drawText("Next Visit:", leftX.toFloat(), yAdvice, boldPaint)
        // Format nextVisit date
        val formattedNextVisit = diagnosis.nextVisit?.let {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(it)
                outputFormat.format(date)
            } catch (e: Exception) {
                it
            }
        } ?: "-"
        canvas.drawText(formattedNextVisit, (leftX + 80).toFloat(), yAdvice, regularPaint)
        // Signature (right-aligned)
        val signatureText = diagnosis.signature ?: "Authorized Signature"
        val signatureWidth = regularPaint.measureText(signatureText)
        canvas.drawText(signatureText, 555f - signatureWidth, 780f, regularPaint)
        // Footer (centered)
        val footerText = "This is a computer generated prescription"
        val footerWidth = smallPaint.measureText(footerText)
        canvas.drawText(footerText, (595f - footerWidth) / 2f, 810f, smallPaint)
        pdfDocument.finishPage(page)
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.close()
        return file
    }
} 