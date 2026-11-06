package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.theme.GestorUpBackground
import com.example.ui.theme.GestorUpBorder
import com.example.ui.theme.GestorUpGreen
import com.example.ui.theme.GestorUpGreenDeep
import com.example.ui.theme.GestorUpGreenLight
import com.example.ui.theme.GestorUpGreenSoft
import com.example.ui.theme.GestorUpSurface
import com.example.ui.theme.GestorUpTextPrimary
import com.example.ui.theme.GestorUpTextSecondary
import com.example.ui.viewmodel.GestorUpViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MonthlyScheduleScreen(
  viewModel: GestorUpViewModel,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val selectedMonth by viewModel.selectedMonth.collectAsState()
  val escalaMensal by viewModel.escalaMensalAtual.collectAsState()

  var showFullScreenModal by remember { mutableStateOf(false) }

  // Photo Picker do Android (zero permissões perigosas, totalmente seguro e em compliance com Google Play)
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let {
      viewModel.salvarEscalaMensal(it.toString())
    }
  }

  // Navegação de Mês e Ano (ex: Outubro de 2026)
  val parts = selectedMonth.split("-")
  val currentYear = parts.getOrNull(0)?.toIntOrNull() ?: 2026
  val currentMonth = parts.getOrNull(1)?.toIntOrNull() ?: 10

  val monthCalendar = remember(currentYear, currentMonth) {
    Calendar.getInstance().apply {
      set(Calendar.YEAR, currentYear)
      set(Calendar.MONTH, currentMonth - 1)
      set(Calendar.DAY_OF_MONTH, 1)
    }
  }

  val monthName = SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR"))
    .format(monthCalendar.time)
    .replaceFirstChar { it.uppercase() }

  fun changeMonth(delta: Int) {
    val cal = (monthCalendar.clone() as Calendar).apply {
      add(Calendar.MONTH, delta)
    }
    val newYear = cal.get(Calendar.YEAR)
    val newMonth = cal.get(Calendar.MONTH) + 1
    viewModel.selecionarMes(String.format(Locale.getDefault(), "%04d-%02d", newYear, newMonth))
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(GestorUpBackground),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. SELETOR DE MÊS E ANO
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
        border = BorderStroke(1.dp, GestorUpBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { changeMonth(-1) },
            modifier = Modifier
              .size(36.dp)
              .background(GestorUpGreenLight, CircleShape)
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mês anterior", tint = GestorUpGreenDeep)
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "Escala Mensal Oficial",
              fontSize = 12.sp,
              color = GestorUpTextSecondary
            )
            Text(
              text = monthName,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = GestorUpGreenDeep
            )
          }

          IconButton(
            onClick = { changeMonth(1) },
            modifier = Modifier
              .size(36.dp)
              .background(GestorUpGreenLight, CircleShape)
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Próximo mês", tint = GestorUpGreenDeep)
          }
        }
      }
    }

    // 2. EXIBIÇÃO DA IMAGEM DA ESCALA OU ESTADO VAZIO
    item {
      if (escalaMensal != null && escalaMensal?.imageUri?.isNotBlank() == true) {
        // Imagem presente
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
          border = BorderStroke(1.dp, GestorUpBorder),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Planilha Oficial Anexada",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = GestorUpGreenDeep
                )
                val dataEnvio = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
                  .format(Date(escalaMensal!!.enviadoEm))
                Text(
                  text = "Atualizado em: $dataEnvio",
                  fontSize = 11.sp,
                  color = GestorUpTextSecondary
                )
              }

              IconButton(onClick = { viewModel.removerEscalaMensal() }) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Box com a imagem e botão de zoom
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0F4F2))
                .clickable { showFullScreenModal = true },
              contentAlignment = Alignment.Center
            ) {
              AsyncImage(
                model = escalaMensal!!.imageUri,
                contentDescription = "Escala Mensal $monthName",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
              )

              // Badge de Toque para Expandir
              Box(
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .padding(8.dp)
                  .background(GestorUpGreenDeep.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Ampliar Planilha", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botão para Substituir
            OutlinedButton(
              onClick = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, GestorUpGreen)
            ) {
              Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = GestorUpGreen)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Substituir Imagem da Escala", color = GestorUpGreen, fontWeight = FontWeight.Bold)
            }
          }
        }
      } else {
        // Estado Vazio Elegante
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
          border = BorderStroke(1.dp, GestorUpBorder),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .background(GestorUpGreenLight, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = GestorUpGreen,
                modifier = Modifier.size(32.dp)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = "Nenhuma escala anexada para $monthName",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = GestorUpGreenDeep,
              textAlign = TextAlign.Center
            )

            Text(
              text = "Anexe a foto ou arquivo de imagem da escala mensal enviada pela administração para consulta rápida da equipe do depósito.",
              fontSize = 12.sp,
              color = GestorUpTextSecondary,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
            )

            Button(
              onClick = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
              modifier = Modifier.testTag("anexar_escala_button")
            ) {
              Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Anexar Imagem da Escala", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // 3. DIÁLOGO EM TELA CHEIA COM ZOOM E PAN (PINCH TO ZOOM)
  if (showFullScreenModal && escalaMensal?.imageUri?.isNotBlank() == true) {
    FullScreenZoomableImageDialog(
      imageUri = escalaMensal!!.imageUri,
      title = "Escala Mensal — $monthName",
      onDismiss = { showFullScreenModal = false }
    )
  }
}

/**
 * Visualizador em tela cheia com zoom interativo (Pinch to Zoom e Pan)
 */
@Composable
fun FullScreenZoomableImageDialog(
  imageUri: String,
  title: String,
  onDismiss: () -> Unit,
) {
  var scale by remember { mutableFloatStateOf(1f) }
  var offset by remember { mutableStateOf(Offset.Zero) }

  val state = rememberTransformableState { zoomChange, offsetChange, _ ->
    scale = (scale * zoomChange).coerceIn(1f, 5f)
    if (scale > 1f) {
      offset += offsetChange
    } else {
      offset = Offset.Zero
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = Color.Black
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        // Imagem com transformações
        Box(
          modifier = Modifier
            .fillMaxSize()
            .transformable(state = state),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = imageUri,
            contentDescription = title,
            modifier = Modifier
              .fillMaxSize()
              .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
              ),
            contentScale = ContentScale.Fit
          )
        }

        // Barra Superior sobre a imagem
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )

          Row {
            // Resetar zoom
            if (scale > 1f) {
              IconButton(onClick = {
                scale = 1f
                offset = Offset.Zero
              }) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Resetar Zoom", tint = Color.White)
              }
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
            }
          }
        }

        // Dica de zoom na parte inferior
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp)
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(
            text = "Use o gesto de pinça para ampliar os nomes e horários",
            color = Color.White,
            fontSize = 11.sp
          )
        }
      }
    }
  }
}
