    package com.example.ebayhw4

    import android.content.Intent
    import androidx.appcompat.app.AppCompatActivity
    import android.os.Bundle
    import android.widget.ImageView

    class SplashActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_splash)
            val iv_note = findViewById<ImageView>(R.id.iv_note)
           iv_note.alpha=1f
            iv_note.animate().setDuration(1500).alpha(1f).withEndAction{
                val i = Intent(this,MainActivity::class.java)
                startActivity(i)
                finish()
            }
        }
    }