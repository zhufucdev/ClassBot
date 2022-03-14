package com.zhufucdev

import com.zhufucdev.data.Database
import com.zhufucdev.data.SignUpRecord
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
            if (!Database[group].any { it is SignUpRecord && it.classmate == sender.id }) {
                Database.record(group, SignUpRecord(sender.id, Instant.now()))
            }
        }
    }

    override fun onDisable() {
        Database.sync()
    }
}