package com.example.opennannyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.opennannyapp.ui.theme.OpenNannyAppTheme
import com.example.opennannyapp.ui.viewModels.InfoState
import com.example.opennannyapp.ui.viewModels.SensorsViewItem
import com.example.opennannyapp.ui.viewModels.SensorsViewModel

class SensorsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as OpenNannyApp

        setContent {
            OpenNannyAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    SensorsScreen(SensorsViewModel(app.serviceApi))
                }

            }
        }

    }
}

@Composable
fun SensorsScreen(viewModel: SensorsViewModel) {

    val state = viewModel.state.collectAsState()

    Box(contentAlignment = Alignment.Center) {
        when(val stateValue = state.value){
            is InfoState.Error -> {
                Text(text = stateValue.message, fontSize = 24.sp, color = Color.White)
            }

            is InfoState.Success -> {
                LazyColumn {
                    items(stateValue.list) {

                        PrintInfoItem(it)
                    }
                }
            }

            InfoState.Loading -> {
                CircularProgressIndicator()
            }
        }

    }

    Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {

        IconButton(onClick = {viewModel.loadData()}, modifier = Modifier.size(64.dp)) {
            Image(
                painter = painterResource(id = R.drawable.restart),
                contentDescription = "Restart"
            )
        }


    }

}

@Composable
fun PrintInfoItem(item: SensorsViewItem){
    Row(modifier = Modifier.height(150.dp)) {

        var img_id = -1
        var unit = ""

        if (item.name == "temperature") {
            img_id = R.drawable.temp
            unit = "\u2103"
        }
        else if (item.name == "humidity") {
            img_id = R.drawable.humidity
            unit = "%"
        }
        else if (item.name == "co2") {
            img_id = R.drawable.co2
            unit = "ppm"
        }
        else if (item.name == "charge") {
            img_id = R.drawable.battery
            unit = "%"
        }

        val v = item.value

        if(img_id != -1) {

            Image(
                painter = painterResource(id = img_id),
                contentDescription = "Photo",
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
            )

            Text(text = "$v $unit",
                style = LocalTextStyle.current.copy(fontSize = 30.sp, color = Color.White),
                modifier = Modifier.padding(30.dp))

        }

    }
}