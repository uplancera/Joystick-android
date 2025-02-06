package com.example.joystick.pages

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.joystick.navigation.Screen
import com.example.joystick.ui.NavTopBar
import com.example.joystick.bluetooth.BluetoothViewModel
import com.example.joystick.ui.JoyStickPad
import com.example.joystick.ui.NavKey
import java.util.Locale

@Composable
fun Joystick(
    navController: NavHostController,
    viewModel: BluetoothViewModel,
) {
    fun disconnectDevice() {
        viewModel.disconnectDevice()
        navController.navigate(Screen.Home.route)
    }

    fun moveUp() {
        viewModel.write("f".toByteArray())
    }
    fun moveDown() {
        viewModel.write("b".toByteArray())
    }
    fun moveLeft() {
        viewModel.write("l".toByteArray())
    }
    fun moveRight() {
        viewModel.write("r".toByteArray())
    }
    fun moveUpRight() {

    }
    fun stop() {
        viewModel.write("s".toByteArray())
    }

    fun moveRobot(direction: NavKey) {
        when {
            direction == NavKey.ARROW_UP -> moveUp()
            direction == NavKey.ARROW_DOWN -> moveDown()
            direction == NavKey.ARROW_LEFT -> moveLeft()
            direction == NavKey.ARROW_RIGHT -> moveRight()
            direction == NavKey.ARROW_UPPER_RIGHT -> moveUpRight()
            direction == NavKey.ARROW_UPPER_LEFT -> moveUpRight()
            direction == NavKey.ARROW_LOWER_RIGHT -> moveUpRight()
            direction == NavKey.ARROW_LOWER_LEFT -> moveUpRight()
            else -> stop()
        }
    }


    val context = LocalContext.current
    var recognizedText by remember { mutableStateOf("Tap the button and say '앞으로' (forward) or '뒤로' (backward)") }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            matches?.let {
                val command = it[0].lowercase(Locale.ROOT)
                recognizedText = "Recognized: $command"

                when {
                    "forward" in command || "앞으로" in command -> moveUp()
                    "backward" in command || "뒤로" in command -> moveDown()
                    "stop" in command || "정지" in command -> stop()
                    else -> moveUpRight() // nothing to do for now
                }
            }
        }
    }

    Scaffold(
        topBar = {
            NavTopBar(
                title = "Joystick",
                canNavigateBack = true,
                navigateUp = {disconnectDevice()}
            )
        }
    ) {innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(text = recognizedText, modifier = Modifier.padding(16.dp))

                IconButton(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR") // Korean
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "앞으로 또는 뒤로 라고 말하십시오.")
                        }
                        speechLauncher.launch(intent)
                    },
                    modifier = Modifier.size(78.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Mic Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            JoyStickPad(moveRobot = {direction -> moveRobot(direction)})
        }
    }
}
