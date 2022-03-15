package com.zhufucdev

import com.zhufucdev.data.Database
import com.zhufucdev.data.SignUpRecord
import com.zhufucdev.serialization.dateFormat
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.permission.Permittee
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.GroupAwareMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.info
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
    }

    private fun signUpService() {
        GlobalEventChannel.subscribeAlways<GroupAwareMessageEvent> {
            val message = message
            val index = SignUpRecord.getTodayRecord(sender.id, group)
            if (index == null) {
                if (!Database.getClassmates(group).contains(sender.id)) {
                    return@subscribeAlways
                }

                val instant = Instant.ofEpochSecond(time.toLong())
                Database.record(group, SignUpRecord(sender.id, instant))
                sender.sendMessage("已标记${dateFormat.format(instant)}的签到")
            }
        }
    }

    override fun onDisable() {
        Database.sync()
    }
}