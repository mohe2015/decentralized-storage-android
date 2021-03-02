package de.selfmade4u.de.decentralized_storage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.selfmade4u.de.decentralized_storage.ui.theme.DecentralizedStorageTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DecentralizedStorageTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun MessageList(messages: List<String>) {
    Column(Modifier.fillMaxWidth())  {
        messages.forEach { message ->
            Text(text = message, textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DecentralizedStorageTheme {
        MessageList(messages = listOf("hi", "jo", "yeeh"))
    }
}