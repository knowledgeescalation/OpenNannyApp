package com.example.opennannyapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.opennannyapp.ui.theme.OpenNannyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            OpenNannyAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    HomeScreen()
                }

            }
        }
    }
}

@Composable
fun HomeScreen() {

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Open Nanny",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(80.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavigationButton(
                text = "Sensors",
                imageRes = R.drawable.sensor,
                onClick = { context.startActivity(Intent(context, SensorsActivity::class.java)) }
            )
            NavigationButton(
                text = "Video",
                imageRes = R.drawable.video,
                onClick = { context.startActivity(Intent(context, VideoActivity::class.java)) }
            )
            NavigationButton(
                text = "Music",
                imageRes = R.drawable.music,
                onClick = { context.startActivity(Intent(context, MusicActivity::class.java)) }
            )
        }
    }
}

@Composable
fun NavigationButton(text: String, imageRes: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .padding(8.dp)
            .height(60.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = text,
                modifier = Modifier.size(24.dp) // Adjust icon size
            )
            Spacer(modifier = Modifier.width(8.dp)) // Adds space between icon and text
            Text(text = text, fontSize = 18.sp)
        }
    }
}