package com.example.evencat_android.classes

import android.os.Build
import androidx.annotation.RequiresApi
import org.bouncycastle.crypto.engines.BlowfishEngine
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import java.util.Base64

object BlowfishHelper {
    private const val secretKey = "8574358d83i304"

    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptMessage(message: String): String {
        val engine = BlowfishEngine()
        val cipher = PaddedBufferedBlockCipher(engine)
        val keyBytes = secretKey.toByteArray(Charsets.UTF_8)

        cipher.init(true, KeyParameter(keyBytes))

        val input = message.toByteArray(Charsets.UTF_8)
        val output = ByteArray(cipher.getOutputSize(input.size))

        var len = cipher.processBytes(input, 0, input.size, output, 0)
        len += cipher.doFinal(output, len)

        return Base64.getEncoder().encodeToString(output.copyOf(len))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decryptMessage(encrypted: String): String {
        val engine = BlowfishEngine()
        val cipher = PaddedBufferedBlockCipher(engine)
        val keyBytes = secretKey.toByteArray(Charsets.UTF_8)

        cipher.init(false, KeyParameter(keyBytes))

        val input = Base64.getDecoder().decode(encrypted)
        val output = ByteArray(cipher.getOutputSize(input.size))

        var len = cipher.processBytes(input, 0, input.size, output, 0)
        len += cipher.doFinal(output, len)

        return String(output.copyOf(len), Charsets.UTF_8)
    }
}