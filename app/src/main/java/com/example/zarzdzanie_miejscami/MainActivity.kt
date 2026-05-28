package com.example.zarzdzanie_miejscami

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Zapasowy punkt wejścia aplikacji.
 *
 * Launcher został przeniesiony do [LoginActivity], ale ta aktywność zostaje
 * w projekcie jako bezpieczny fallback i miejsce na przyszły splash screen.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
