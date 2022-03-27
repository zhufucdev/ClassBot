package com.zhufucdev

import com.zhufucdev.api.HttpServer
import com.zhufucdev.data.Database
import com.zhufucdev.data.SignUpRecord
import com.zhufucdev.data.homework.HomeworkManifest
import com.zhufucdev.serialization.dateFormat
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupAwareMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.selectMessagesUnit
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText
import java.time.Instant

object Plugin : KotlinPlugin(
    JvmPluginDescription(
        id = "com.zhufucdev.plugin",
        name = "ClassBot",
        version = "1.0",
    ) {
        author("zhufucdev")
    }
) {
    override fun onEnable() {
        logger.info("${Database.size} pieces of record have been loaded")
        GlobalCommand.register()

        signUpService()
        homeworkService()
    }

    private fun signUpService() {
        GlobalEventChannel.subscribeAlways<GroupAwareMessageEvent> {
            val index = SignUpRecord.getTodayRecord(sender.id, group)
            if (index == null) {
                if (!Database.classmates(group).contains(sender.id)) {
                    return@subscribeAlways
                }

                val instant = Instant.ofEpochSecond(time.toLong())
                Database.record(group, SignUpRecord(sender.id, instant))
                sender.sendMessage("已标记${dateFormat.format(instant)}的签到")
            }
        }
    }

    private fun homeworkService() {
        HttpServer.init()

        NaturalLanguageProcessing.onContextEnded {

        }

        GlobalEventChannel.subscribeAlways<MessageEvent> {
            val images = message.filterIsInstance<Image>()
            val text = message.filterIsInstance<PlainText>()
            val conversation =
                NaturalLanguageProcessing.beginContext(sender, if (this is GroupAwareMessageEvent) group else null)
            text.forEach { conversation.process(it.content) }
            images.forEach { conversation.process(it) }
        }
    }

    override fun onDisable() {
        Database.sync()
        HttpServer.stop()
    }

    fun getGroup(id: Long): Group? {
        for (bot in Bot.instances) {
            val index = bot.getGroup(id)
            if (index != null) {
                return index
            }
        }
        return null
    }
}