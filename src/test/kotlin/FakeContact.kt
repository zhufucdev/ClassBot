package com.zhufucdev

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource
import kotlin.coroutines.CoroutineContext

class FakeContact(override val id: Long) : Contact {
    override val bot: Bot
        get() = TODO("Not yet implemented")
    override val coroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")

    override suspend fun sendMessage(message: Message): MessageReceipt<Contact> {
        println("Received a text: ${message.content}")
        throw NotImplementedError()
    }

    override suspend fun uploadImage(resource: ExternalResource): Image {
        println("Received an image")
        throw NotImplementedError()
    }
}