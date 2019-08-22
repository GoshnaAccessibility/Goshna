package com.example.wquist.goshna

import com.example.wquist.goshna.ApiResponse.MessageResponse

interface OnMessageReceivedListener {
    fun onMessageReceived(messages: Array<out MessageResponse>)
}