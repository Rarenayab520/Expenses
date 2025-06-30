package com.nayab.expenses.activities

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.nayab.smartexpensetracker.databinding.ActivityOcrScanBinding

class OCRScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOcrScanBinding
    private val IMAGE_PICK_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartScan.setOnClickListener {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                runTextRecognition(bitmap)
            }
        }
    }

    private fun runTextRecognition(bitmap: android.graphics.Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text
                val lines = fullText.lines().map { it.trim() }

                // 🏪 Extract Title (first non-empty line)
                val title = lines.firstOrNull { it.isNotBlank() } ?: "Unknown Expense"

                // 💸 Extract total amount
                val amount = extractAmountFromText(lines)

                // 📅 Extract date
                val date = extractDate(fullText)

                // ✅ Return the result
                val resultIntent = Intent().apply {
                    putExtra("scanned_title", title)
                    putExtra("scanned_amount", amount ?: "0")
                    putExtra("scanned_date", date ?: "Unknown Date")
                }

                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Scan failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔍 Total Amount Extractor with smart keyword matching
    private fun extractAmountFromText(lines: List<String>): String? {
        val totalKeywords = listOf("total", "amount", "subtotal", "cash", "grand total", "paid", "rm", "$", "rs")

        for (line in lines.reversed()) {
            val lowerLine = line.lowercase()
            if (totalKeywords.any { lowerLine.contains(it) }) {
                val amountRegex = Regex("""(\d+[,.]?\d*)""")
                val match = amountRegex.find(line)
                if (match != null) return match.value
            }
        }

        // fallback: largest number in all text
        val numberRegex = Regex("""\d+[,.]?\d*""")
        return numberRegex.findAll(lines.joinToString(" "))
            .mapNotNull { it.value.toDoubleOrNull() }
            .maxOrNull()?.toString()
    }

    // 📅 Date extractor supporting multiple formats
    private fun extractDate(text: String): String? {
        val datePatterns = listOf(
            Regex("""\b\d{1,2}[/-]\d{1,2}[/-]\d{2,4}\b"""),  // 25/06/2025 or 25-06-2025
            Regex("""\b\d{4}-\d{2}-\d{2}\b"""),              // 2025-06-25
            Regex("""\b\d{1,2}/\d{1,2}/\d{4}\b""")           // 12/28/2017
        )

        for (pattern in datePatterns) {
            val match = pattern.find(text)
            if (match != null) return match.value
        }

        return null
    }
}
