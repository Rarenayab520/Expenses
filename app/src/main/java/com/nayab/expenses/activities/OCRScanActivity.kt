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

                // 📝 Extract title
                val lines = fullText.lines()
                val title = lines.firstOrNull()?.take(50) ?: "Unknown Expense"  // Optional: Trim long title

                // 💰 Extract all matched Rs. amounts and pick the highest
                val amountRegex = Regex("""Rs\.?\s?(\d+(\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
                val allAmounts = amountRegex.findAll(fullText).mapNotNull {
                    it.groups[1]?.value?.toDoubleOrNull()
                }.toList()
                val amount = allAmounts.maxOrNull()?.toString()  // 👈 pick the highest

                // 📅 Extract date
                val dateRegex = Regex("""\b(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})\b""")
                val dateMatch = dateRegex.find(fullText)?.value

                // 🎯 Pass scanned result back
                val resultIntent = Intent().apply {
                    putExtra("scanned_title", title)
                    putExtra("scanned_amount", amount)
                    putExtra("scanned_date", dateMatch)
                }

                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Scan failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
