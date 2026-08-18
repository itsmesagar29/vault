package com.example.domain.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.domain.model.ParsedReceipt
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.coroutines.resume

class ReceiptOcrEngine(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeAndParse(bitmap: Bitmap, defaultWarrantyMonths: Int = 12): Result<ParsedReceipt> =
        withContext(Dispatchers.Default) {
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val rawText = recognizeText(inputImage)
                val parsed = ReceiptParser.parse(rawText, defaultWarrantyMonths)
                Result.success(parsed)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun recognizeAndParse(uri: Uri, defaultWarrantyMonths: Int = 12): Result<ParsedReceipt> =
        withContext(Dispatchers.IO) {
            try {
                val bitmap = loadBitmapFromUri(uri)
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val rawText = recognizeText(inputImage)
                val parsed = ReceiptParser.parse(rawText, defaultWarrantyMonths)
                Result.success(parsed)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun parseFromText(rawText: String, defaultWarrantyMonths: Int = 12): ParsedReceipt =
        withContext(Dispatchers.Default) {
            ReceiptParser.parse(rawText, defaultWarrantyMonths)
        }

    private suspend fun recognizeText(image: InputImage): String =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { text ->
                    continuation.resume(text.text)
                }
                .addOnFailureListener { error ->
                    continuation.resumeWith(Result.failure(error))
                }
        }

    fun loadBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    suspend fun saveBitmapToInternalStorage(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "receipt_images")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "receipt_${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    }
}
