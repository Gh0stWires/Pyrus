package tk.samgrogan.pyrus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_text.*

class TextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)
        text.text = intent?.extras?.getString(TEXT)
    }

    companion object {
        const val TEXT = "text"
        fun newIntent(context: Context, text: String): Intent {
            val intent = Intent(context, TextActivity::class.java)
            intent.putExtra(TEXT, text)
            return intent
        }
    }
}
