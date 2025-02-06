package com.bekirahmetli.instagramapp.utils

import android.annotation.SuppressLint
import android.util.Log
import java.io.File

class DosyaIslemleri {
    companion object {
        @SuppressLint("SuspiciousIndentation")
        fun klasordekiDosyalariGetir(klasorAdi: String): ArrayList<String> {
            val tumDosyalar = ArrayList<String>()
            val file = File(klasorAdi)
            val klasordekiTumDosyalar = file.listFiles()

            File(klasorAdi).exists().also { exists ->
                Log.e("ShareGallery", "Mevcut Klasör: $exists")
            }

            if (klasordekiTumDosyalar != null) {
                for (dosya in klasordekiTumDosyalar) {
                    if (dosya.isFile) {
                        val okunanDosyaYolu = dosya.absolutePath
                        val index = okunanDosyaYolu.lastIndexOf(".")

                        // Dosya uzantısını kontrol et ve hatayı önle
                        val dosyaTuru = if (index != -1) {
                            okunanDosyaYolu.substring(index)
                        } else {
                            ""  // Eğer dosya uzantısı yoksa boş string döner
                        }

                        // Desteklenen dosya türlerini kontrol et
                        if (dosyaTuru.equals(".jpg", ignoreCase = true) ||
                            dosyaTuru.equals(".jpeg", ignoreCase = true) ||
                            dosyaTuru.equals(".png", ignoreCase = true) ||
                            dosyaTuru.equals(".mp4", ignoreCase = true)) {
                            tumDosyalar.add(okunanDosyaYolu)
                        }
                    }
                }
            }

            return tumDosyalar
        }
    }
}
