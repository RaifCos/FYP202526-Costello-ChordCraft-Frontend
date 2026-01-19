package com.example.chordcraft

import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.chordcraft.ui.theme.ChordCraftTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Structure()
        }
    }
}

@Composable
fun Structure() {
    ChordCraftTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            GreetingText(
                "Welcome to ChordCraft",
                "chord extraction made easy.",
                modifier = Modifier
                    .padding(32.dp)
            )
        }
    }
}

@Composable
fun GreetingText(txtA: String, txtB: String, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = txtA,
            fontSize = 48.sp,
            lineHeight = 64.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = txtB,
            fontSize = 16.sp,
        )
        val currContext = LocalContext.current
        Button(
            onClick = { moveMenu(currContext) },
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
                .padding(32.dp)
                .width(256.dp)
                .height(64.dp)
        ) {
            Text(
                text = "Start",
                fontSize = 32.sp
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ChordCraftPreview() {
    Structure()
}

fun moveMenu(context: Context) {
    val intent = Intent(context, MainMenuActivity::class.java)
    context.startActivity(intent)
}