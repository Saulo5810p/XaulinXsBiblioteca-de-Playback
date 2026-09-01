package com.example.model

data class EqualizerBand(
    val bandIndex: Short,
    val centerFrequencyHz: Int,
    val minLevelMb: Short,
    val maxLevelMb: Short,
    var currentLevelMb: Short
)
