package com.example.ui.viewmodel

data class InterjornadaConflito(
    val nome: String,
    val dataAnterior: String,
    val saidaAnterior: String,
    val dataProxima: String,
    val entradaProxima: String,
    val descansoCalculado: Int,
    val referencia: Int = 11,
    val isIncompleto: Boolean = false,
    val motivoIncompleto: String = ""
)
